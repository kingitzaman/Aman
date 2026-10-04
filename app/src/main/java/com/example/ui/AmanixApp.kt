package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AIProvider
import com.example.data.coding.CodeExecutionService
import com.example.data.local.AppDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.MemoryRepository
import com.example.data.search.SearchService
import kotlinx.coroutines.launch
import com.example.ui.components.AmanixGlassBackground
import com.example.ui.components.AmanixLogoEmblem
import com.example.ui.components.glassmorphic
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.brainstorm.BrainstormScreen
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.coding.CodingScreen
import com.example.ui.screens.learning.LearningScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.writing.WritingScreen
import com.example.ui.theme.AmanixBorder
import com.example.ui.theme.AmanixCyanContainer
import com.example.ui.theme.AmanixCyanPrimary
import com.example.ui.theme.AmanixTextMuted
import com.example.ui.theme.AmanixTextPrimary
import com.example.ui.theme.AmanixTextSecondary

enum class AmanixScreen(val title: String, val icon: ImageVector) {
    SPLASH("Splash", Icons.AutoMirrored.Filled.Chat),
    AUTH("Authentication", Icons.AutoMirrored.Filled.Chat),
    CHAT("Chat / Home", Icons.AutoMirrored.Filled.Chat),
    SEARCH("Web Search", Icons.Default.Search),
    CODING("Coding & Debugging", Icons.Default.Code),
    LEARNING("Learning & Explain", Icons.Default.School),
    WRITING("Writing & Editing", Icons.Default.Psychology),
    BRAINSTORM("Brainstorm & Solve", Icons.Default.Lightbulb),
    SETTINGS("Settings & Profile", Icons.Default.Settings)
}

@Composable
fun AmanixApp(
    database: AppDatabase,
    authRepository: AuthRepository,
    chatRepository: ChatRepository,
    memoryRepository: MemoryRepository,
    aiProvider: AIProvider,
    searchService: SearchService,
    codeExecutionService: CodeExecutionService
) {
    var currentScreen by remember { mutableStateOf(AmanixScreen.SPLASH) }
    val currentUser by authRepository.currentUser.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Handle back button on secondary screens
    if (currentScreen != AmanixScreen.CHAT && currentScreen != AmanixScreen.SPLASH && currentScreen != AmanixScreen.AUTH) {
        BackHandler {
            currentScreen = AmanixScreen.CHAT
        }
    }

    AmanixGlassBackground {
        if (currentScreen == AmanixScreen.SPLASH) {
            SplashScreen(
                authRepository = authRepository,
                onNavigateToHome = { currentScreen = AmanixScreen.CHAT },
                onNavigateToAuth = { currentScreen = AmanixScreen.AUTH }
            )
        } else if (currentScreen == AmanixScreen.AUTH || currentUser == null) {
            AuthScreen(
                authRepository = authRepository,
                onAuthSuccess = { currentScreen = AmanixScreen.CHAT }
            )
        } else {
            // Authenticated 8-screen experience with Frosted Glass Navigation Drawer
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Color(0xED0B1426),
                        drawerContentColor = AmanixTextPrimary,
                        modifier = Modifier
                            .width(310.dp)
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color(0x5000D2FF),
                                        Color(0x20FFFFFF),
                                        Color(0x304F8CFF),
                                        Color(0x1000D2FF)
                                    )
                                ),
                                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .statusBarsPadding()
                                .navigationBarsPadding()
                                .padding(18.dp)
                        ) {
                            // Drawer Header with glowing Amanix Crest
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AmanixLogoEmblem(sizeDp = 44)
                                Column {
                                    Text(
                                        text = "AMANIX",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp,
                                        color = AmanixCyanPrimary
                                    )
                                    Text(
                                        text = "Think • Search • Create • Grow",
                                        fontSize = 10.sp,
                                        color = AmanixTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Glassmorphic User profile card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .glassmorphic(
                                        shape = RoundedCornerShape(12.dp),
                                        backgroundColor = Color(0x35142642),
                                        borderColor = Color(0x4000D2FF)
                                    )
                                    .clickable {
                                        currentScreen = AmanixScreen.SETTINGS
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(AmanixCyanContainer)
                                            .border(1.dp, AmanixCyanPrimary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentUser!!.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = AmanixCyanPrimary,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = currentUser!!.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = AmanixTextPrimary
                                        )
                                        Text(
                                            text = currentUser!!.email,
                                            fontSize = 11.sp,
                                            color = AmanixTextMuted
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color.Transparent, Color(0x4000D2FF), Color.Transparent)
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Navigation Items
                            val navScreens = listOf(
                                AmanixScreen.CHAT,
                                AmanixScreen.SEARCH,
                                AmanixScreen.CODING,
                                AmanixScreen.LEARNING,
                                AmanixScreen.WRITING,
                                AmanixScreen.BRAINSTORM,
                                AmanixScreen.SETTINGS
                            )

                            navScreens.forEach { screen ->
                                val isSelected = currentScreen == screen
                                NavigationDrawerItem(
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            tint = if (isSelected) AmanixCyanPrimary else AmanixTextSecondary
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isSelected) AmanixCyanPrimary else AmanixTextPrimary
                                        )
                                    },
                                    selected = isSelected,
                                    onClick = {
                                        currentScreen = screen
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = Color(0x35003E5C),
                                        unselectedContainerColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .padding(vertical = 3.dp)
                                        .border(
                                            width = if (isSelected) 1.dp else 0.dp,
                                            brush = if (isSelected) {
                                                Brush.linearGradient(
                                                    listOf(AmanixCyanPrimary.copy(alpha = 0.6f), Color(0x20FFFFFF))
                                                )
                                            } else {
                                                Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .testTag("nav_item_${screen.name.lowercase()}")
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Sign Out Option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .glassmorphic(
                                        shape = RoundedCornerShape(10.dp),
                                        backgroundColor = Color(0x2014233A),
                                        borderColor = Color(0x25FFFFFF)
                                    )
                                    .clickable {
                                        coroutineScope.launch {
                                            authRepository.signOut()
                                            drawerState.close()
                                            currentScreen = AmanixScreen.AUTH
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Sign Out",
                                    tint = AmanixTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Sign Out",
                                    fontSize = 13.sp,
                                    color = AmanixTextSecondary
                                )
                            }
                        }
                    }
                }
            ) {
                when (currentScreen) {
                    AmanixScreen.CHAT -> {
                        ChatScreen(
                            currentUser = currentUser!!,
                            chatRepository = chatRepository,
                            memoryRepository = memoryRepository,
                            aiProvider = aiProvider,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onOpenProfile = { currentScreen = AmanixScreen.SETTINGS }
                        )
                    }
                    AmanixScreen.SEARCH -> {
                        SearchScreen(
                            searchService = searchService,
                            searchHistoryDao = database.searchHistoryDao(),
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onOpenSettings = { currentScreen = AmanixScreen.SETTINGS }
                        )
                    }
                    AmanixScreen.CODING -> {
                        CodingScreen(
                            codeExecutionService = codeExecutionService,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onOpenSettings = { currentScreen = AmanixScreen.SETTINGS }
                        )
                    }
                    AmanixScreen.LEARNING -> {
                        LearningScreen(
                            aiProvider = aiProvider,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onOpenSettings = { currentScreen = AmanixScreen.SETTINGS }
                        )
                    }
                    AmanixScreen.WRITING -> {
                        WritingScreen(
                            aiProvider = aiProvider,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onOpenSettings = { currentScreen = AmanixScreen.SETTINGS }
                        )
                    }
                    AmanixScreen.BRAINSTORM -> {
                        BrainstormScreen(
                            aiProvider = aiProvider,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onOpenSettings = { currentScreen = AmanixScreen.SETTINGS }
                        )
                    }
                    AmanixScreen.SETTINGS -> {
                        SettingsScreen(
                            currentUser = currentUser!!,
                            database = database,
                            authRepository = authRepository,
                            memoryRepository = memoryRepository,
                            aiProvider = aiProvider,
                            searchService = searchService,
                            codeExecutionService = codeExecutionService,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onSignOut = { currentScreen = AmanixScreen.AUTH }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}
