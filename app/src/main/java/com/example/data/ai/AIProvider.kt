package com.example.data.ai

import com.example.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

enum class ModelMode(val label: String, val description: String, val modelId: String) {
    MAX("MAX", "Deep reasoning & complex tasks", "gemini-3.1-pro-preview"),
    MEDIUM("MEDIUM", "Balanced speed & intelligence", "gemini-3.5-flash"),
    LOW("LOW", "Ultra-fast lightweight queries", "gemini-3.1-flash-lite-preview")
}

enum class AIState {
    IDLE,
    THINKING,
    GENERATING,
    COMPLETED,
    FAILED,
    CANCELLED,
    OFFLINE,
    NOT_CONFIGURED
}

sealed class AIStreamEvent {
    object Thinking : AIStreamEvent()
    data class Chunk(val text: String) : AIStreamEvent()
    data class Complete(val fullText: String, val modelUsed: String) : AIStreamEvent()
    data class Error(val message: String, val state: AIState) : AIStreamEvent()
    object Cancelled : AIStreamEvent()
}

data class ModelInfo(
    val mode: ModelMode,
    val modelId: String,
    val isConfigured: Boolean,
    val providerName: String
)

interface AIProvider {
    fun isConfigured(): Boolean
    fun getConfigurationDetails(): String
    fun getAvailableModels(): List<ModelInfo>
    suspend fun generateStream(
        prompt: String,
        mode: ModelMode,
        history: List<MessageEntity> = emptyList(),
        systemInstruction: String? = null
    ): Flow<AIStreamEvent>
}
