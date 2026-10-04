package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.ai.GeminiAIProvider
import com.example.data.coding.CodeExecutionService
import com.example.data.local.AppDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.MemoryRepository
import com.example.data.search.SearchService
import com.example.ui.AmanixApp
import com.example.ui.theme.AmanixBackground
import com.example.ui.theme.AmanixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val authRepository = AuthRepository(database)
        val chatRepository = ChatRepository(database)
        val memoryRepository = MemoryRepository(database)
        val aiProvider = GeminiAIProvider(database.settingDao())
        val searchService = SearchService(database.settingDao(), database.searchHistoryDao())
        val codeExecutionService = CodeExecutionService(database.settingDao())

        setContent {
            AmanixTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AmanixBackground
                ) {
                    AmanixApp(
                        database = database,
                        authRepository = authRepository,
                        chatRepository = chatRepository,
                        memoryRepository = memoryRepository,
                        aiProvider = aiProvider,
                        searchService = searchService,
                        codeExecutionService = codeExecutionService
                    )
                }
            }
        }
    }
}
