package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val model: String? = null,
    val toolUsed: String? = null,
    val state: String = "COMPLETED" // "IDLE", "THINKING", "GENERATING", "COMPLETED", "FAILED"
)
