package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val source: String = "chat",
    val status: String = "active" // "active", "archived"
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String? = null
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val category: String = "web",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String,
    val localUri: String,
    val createdAt: Long = System.currentTimeMillis()
)
