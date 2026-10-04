package com.example.data.coding

import com.example.data.local.dao.SettingDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class ExecutionResult {
    object Idle : ExecutionResult()
    object Running : ExecutionResult()
    data class Output(val stdout: String, val stderr: String, val exitCode: Int, val executionTimeMs: Long) : ExecutionResult()
    data class Error(val message: String) : ExecutionResult()
    data class NotConfigured(val message: String) : ExecutionResult()
}

data class CodeFile(
    val name: String,
    val language: String,
    val content: String
)

class CodeExecutionService(
    private val settingDao: SettingDao
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun isConfigured(): Boolean {
        val sandboxUrl = settingDao.getSetting("sandbox_execution_url")
        return !sandboxUrl.isNullOrBlank()
    }

    suspend fun executeCode(language: String, code: String): ExecutionResult = withContext(Dispatchers.IO) {
        val sandboxUrl = settingDao.getSetting("sandbox_execution_url")
        val sandboxToken = settingDao.getSetting("sandbox_auth_token")

        if (sandboxUrl.isNullOrBlank()) {
            return@withContext ExecutionResult.NotConfigured(
                "Code execution is not configured. An isolated sandbox backend execution server is required to safely run untrusted code. Configure the Sandbox Endpoint in Settings."
            )
        }

        try {
            val payload = JSONObject().apply {
                put("language", language)
                put("code", code)
                put("timeoutSeconds", 10)
            }

            val requestBuilder = Request.Builder()
                .url("$sandboxUrl/execute")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))

            if (!sandboxToken.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $sandboxToken")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                return@withContext ExecutionResult.Error("Sandbox server returned HTTP ${response.code}")
            }

            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val stdout = json.optString("stdout", "")
            val stderr = json.optString("stderr", "")
            val exitCode = json.optInt("exitCode", 0)
            val timeMs = json.optLong("timeMs", 0)

            ExecutionResult.Output(stdout, stderr, exitCode, timeMs)
        } catch (e: Exception) {
            ExecutionResult.Error("Sandbox execution failed: ${e.localizedMessage ?: "Connection error"}")
        }
    }
}
