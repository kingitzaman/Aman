package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.ConversationDao
import com.example.data.local.dao.FileDao
import com.example.data.local.dao.MemoryDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.SearchHistoryDao
import com.example.data.local.dao.SettingDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.FileEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.SearchHistoryEntity
import com.example.data.local.entity.SettingEntity
import com.example.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        MemoryEntity::class,
        SettingEntity::class,
        AuditLogEntity::class,
        SearchHistoryEntity::class,
        FileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun settingDao(): SettingDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun fileDao(): FileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "amanix_master.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
