package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.SettingEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

class AuthRepository(
    private val database: AppDatabase
) {
    private val userDao = database.userDao()
    private val settingDao = database.settingDao()
    private val auditDao = database.auditLogDao()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun checkSession(): UserEntity? = withContext(Dispatchers.IO) {
        val activeUserId = settingDao.getSetting("active_user_id")
        if (!activeUserId.isNullOrBlank()) {
            val user = userDao.getUserById(activeUserId)
            if (user != null) {
                _currentUser.value = user
                return@withContext user
            }
        }
        _currentUser.value = null
        null
    }

    suspend fun signIn(email: String, password: String):Result<UserEntity> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return@withContext Result.failure(Exception("No account found with this email."))

        if (user.passwordHash != hashPassword(password)) {
            return@withContext Result.failure(Exception("Incorrect password."))
        }

        settingDao.setSetting(SettingEntity("active_user_id", user.id))
        auditDao.insertLog(AuditLogEntity(action = "LOGIN", details = "User signed in: $trimmedEmail", userId = user.id))
        _currentUser.value = user
        Result.success(user)
    }

    suspend fun signUp(name: String, email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim().lowercase()

        if (trimmedName.isEmpty()) return@withContext Result.failure(Exception("Name cannot be empty."))
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) return@withContext Result.failure(Exception("Invalid email format."))
        if (password.length < 6) return@withContext Result.failure(Exception("Password must be at least 6 characters."))

        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return@withContext Result.failure(Exception("An account already exists with this email."))
        }

        val newUser = UserEntity(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            email = trimmedEmail,
            passwordHash = hashPassword(password)
        )

        userDao.insertUser(newUser)
        settingDao.setSetting(SettingEntity("active_user_id", newUser.id))
        auditDao.insertLog(AuditLogEntity(action = "ACCOUNT_CREATED", details = "New user registered: $trimmedEmail", userId = newUser.id))
        _currentUser.value = newUser
        Result.success(newUser)
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        val userId = _currentUser.value?.id
        settingDao.deleteSetting("active_user_id")
        auditDao.insertLog(AuditLogEntity(action = "LOGOUT", details = "User logged out", userId = userId))
        _currentUser.value = null
    }

    suspend fun deleteAccount(confirmationText: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (confirmationText != "Delete my Amanix account") {
            return@withContext Result.failure(Exception("Confirmation text must match 'Delete my Amanix account' exactly."))
        }
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("No active user session."))

        // Delete user's conversations, messages, memories, files, and user record
        database.conversationDao().deleteAllConversationsForUser(user.id)
        database.memoryDao().clearMemoriesForUser(user.id)
        database.fileDao().clearFilesForUser(user.id)
        userDao.deleteUser(user.id)
        settingDao.deleteSetting("active_user_id")
        auditDao.insertLog(AuditLogEntity(action = "ACCOUNT_DELETED", details = "Account permanently deleted: ${user.email}", userId = user.id))
        _currentUser.value = null
        Result.success(Unit)
    }
}

class ChatRepository(
    private val database: AppDatabase
) {
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()

    fun getConversations(userId: String): Flow<List<ConversationEntity>> =
        conversationDao.getConversationsForUser(userId)

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)

    suspend fun createConversation(userId: String, title: String, model: String): ConversationEntity = withContext(Dispatchers.IO) {
        val conv = ConversationEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            model = model
        )
        conversationDao.insertConversation(conv)
        conv
    }

    suspend fun updateConversation(conversation: ConversationEntity) = withContext(Dispatchers.IO) {
        conversationDao.updateConversation(conversation.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteConversation(id: String) = withContext(Dispatchers.IO) {
        messageDao.deleteMessagesForConversation(id)
        conversationDao.deleteConversationById(id)
    }

    suspend fun saveMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        messageDao.insertMessage(message)
    }

    suspend fun updateMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        messageDao.updateMessage(message)
    }
}

class MemoryRepository(
    private val database: AppDatabase
) {
    private val memoryDao = database.memoryDao()
    private val settingDao = database.settingDao()

    fun getActiveMemories(userId: String): Flow<List<MemoryEntity>> =
        memoryDao.getActiveMemoriesForUser(userId)

    suspend fun isMemoryEnabled(): Boolean = withContext(Dispatchers.IO) {
        (settingDao.getSetting("memory_enabled") ?: "true") == "true"
    }

    suspend fun setMemoryEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        settingDao.setSetting(SettingEntity("memory_enabled", enabled.toString()))
    }

    suspend fun addMemory(userId: String, content: String, source: String = "chat") = withContext(Dispatchers.IO) {
        if (!isMemoryEnabled()) return@withContext
        val memory = MemoryEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            content = content.trim(),
            source = source
        )
        memoryDao.insertMemory(memory)
    }

    suspend fun deleteMemory(id: String) = withContext(Dispatchers.IO) {
        memoryDao.deleteMemoryById(id)
    }

    suspend fun clearMemories(userId: String) = withContext(Dispatchers.IO) {
        memoryDao.clearMemoriesForUser(userId)
    }
}

class AppExportService(
    private val database: AppDatabase
) {
    suspend fun exportUserDataJson(userId: String): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val user = database.userDao().getUserById(userId)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("user", JSONObject().apply {
            put("id", user?.id)
            put("name", user?.name)
            put("email", user?.email)
            put("createdAt", user?.createdAt)
        })

        val memories = database.memoryDao().getAllMemoriesForUser(userId)
        val memoriesArray = JSONArray()
        for (m in memories) {
            memoriesArray.put(JSONObject().apply {
                put("id", m.id)
                put("content", m.content)
                put("createdAt", m.createdAt)
                put("source", m.source)
            })
        }
        root.put("memories", memoriesArray)

        val logs = database.auditLogDao().getAllLogs().filter { it.userId == userId }
        val logsArray = JSONArray()
        for (l in logs) {
            logsArray.put(JSONObject().apply {
                put("action", l.action)
                put("details", l.details)
                put("timestamp", l.timestamp)
            })
        }
        root.put("auditLogs", logsArray)

        root.toString(2)
    }
}
