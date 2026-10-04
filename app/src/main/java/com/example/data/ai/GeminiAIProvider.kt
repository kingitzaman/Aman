package com.example.data.ai

import com.example.BuildConfig
import com.example.data.local.dao.SettingDao
import com.example.data.local.entity.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class GeminiAIProvider(
    private val settingDao: SettingDao
) : AIProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getEffectiveApiKey(): String {
        val userConfiguredKey = settingDao.getSetting("gemini_api_key")
        if (!userConfiguredKey.isNullOrBlank()) {
            return userConfiguredKey.trim()
        }
        val buildKey = BuildConfig.GEMINI_API_KEY
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey.trim()
        }
        return ""
    }

    override fun isConfigured(): Boolean {
        // Fast synchronous check using BuildConfig; suspend version is authoritative
        val buildKey = BuildConfig.GEMINI_API_KEY
        return buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY"
    }

    suspend fun checkIsConfigured(): Boolean {
        return getEffectiveApiKey().isNotBlank()
    }

    override fun getConfigurationDetails(): String {
        val buildKey = BuildConfig.GEMINI_API_KEY
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
            "Configured via Environment / Secrets"
        } else {
            "Custom or Unset"
        }
    }

    override fun getAvailableModels(): List<ModelInfo> {
        val configured = isConfigured()
        return listOf(
            ModelInfo(ModelMode.MAX, ModelMode.MAX.modelId, configured, "Gemini Pro Reasoning"),
            ModelInfo(ModelMode.MEDIUM, ModelMode.MEDIUM.modelId, configured, "Gemini Flash"),
            ModelInfo(ModelMode.LOW, ModelMode.LOW.modelId, configured, "Gemini Flash-Lite")
        )
    }

    override suspend fun generateStream(
        prompt: String,
        mode: ModelMode,
        history: List<MessageEntity>,
        systemInstruction: String?
    ): Flow<AIStreamEvent> = flow {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            emit(AIStreamEvent.Error(
                "AI model is not configured. Please enter your Gemini API Key in Settings or the Secrets panel to activate live AI.",
                AIState.NOT_CONFIGURED
            ))
            return@flow
        }

        emit(AIStreamEvent.Thinking)

        val modelName = mode.modelId
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:streamGenerateContent?alt=sse&key=$apiKey"

        val contentsArray = JSONArray()

        // Append past history turns (limit last 10 messages for token safety)
        val recentHistory = history.takeLast(10)
        for (msg in recentHistory) {
            val role = if (msg.role == "user") "user" else "model"
            val turn = JSONObject()
            turn.put("role", role)
            val parts = JSONArray()
            val part = JSONObject()
            part.put("text", msg.content)
            parts.put(part)
            turn.put("parts", parts)
            contentsArray.put(turn)
        }

        // Current prompt
        val currentTurn = JSONObject()
        currentTurn.put("role", "user")
        val currentParts = JSONArray()
        val currentPart = JSONObject()
        currentPart.put("text", prompt)
        currentParts.put(currentPart)
        currentTurn.put("parts", currentParts)
        contentsArray.put(currentTurn)

        val payload = JSONObject()
        payload.put("contents", contentsArray)

        if (!systemInstruction.isNullOrBlank()) {
            val sysObj = JSONObject()
            val sysParts = JSONArray()
            val sPart = JSONObject()
            sPart.put("text", systemInstruction)
            sysParts.put(sPart)
            sysObj.put("parts", sysParts)
            payload.put("systemInstruction", sysObj)
        }

        val generationConfig = JSONObject()
        generationConfig.put("temperature", 0.7)
        payload.put("generationConfig", generationConfig)

        val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        val fullTextBuilder = StringBuilder()

        try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                val parsedMsg = try {
                    val errJson = JSONObject(errorBody)
                    errJson.optJSONObject("error")?.optString("message") ?: errorBody
                } catch (e: Exception) {
                    errorBody
                }
                emit(AIStreamEvent.Error(
                    "AI generation failed (HTTP ${response.code}): $parsedMsg",
                    AIState.FAILED
                ))
                return@flow
            }

            val responseBody = response.body
            if (responseBody == null) {
                emit(AIStreamEvent.Error("Empty response from AI engine", AIState.FAILED))
                return@flow
            }

            val reader = BufferedReader(InputStreamReader(responseBody.byteStream()))
            var line: String? = null
            while (currentCoroutineContext().isActive && reader.readLine().also { line = it } != null) {
                val currentLine = line?.trim() ?: continue
                if (!currentLine.startsWith("data:")) continue
                val jsonPayload = currentLine.removePrefix("data:").trim()
                if (jsonPayload.isEmpty() || jsonPayload == "[DONE]") continue

                try {
                    val root = JSONObject(jsonPayload)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val textPart = parts.getJSONObject(0).optString("text", "")
                            if (textPart.isNotEmpty()) {
                                fullTextBuilder.append(textPart)
                                emit(AIStreamEvent.Chunk(textPart))
                            }
                        }
                    }
                } catch (parseEx: Exception) {
                    // Ignore transient malformed SSE chunks
                }
            }

            if (currentCoroutineContext().isActive) {
                val result = fullTextBuilder.toString()
                if (result.isNotBlank()) {
                    emit(AIStreamEvent.Complete(result, modelName))
                } else {
                    emit(AIStreamEvent.Error("AI engine returned empty content.", AIState.FAILED))
                }
            } else {
                emit(AIStreamEvent.Cancelled)
            }
        } catch (ioEx: Exception) {
            emit(AIStreamEvent.Error(
                "Unable to connect to the AI engine: ${ioEx.localizedMessage ?: "Network error"}",
                AIState.OFFLINE
            ))
        }
    }.flowOn(Dispatchers.IO)
}
