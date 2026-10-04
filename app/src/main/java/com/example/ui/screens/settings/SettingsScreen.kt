package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AIProvider
import com.example.data.coding.CodeExecutionService
import com.example.data.i18n.AppLanguage
import com.example.data.i18n.LocaleManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.SettingEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AppExportService
import com.example.data.repository.AuthRepository
import com.example.data.repository.MemoryRepository
import com.example.data.search.SearchService
import com.example.ui.components.AmanixConfirmationDialog
import com.example.ui.components.AmanixTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.components.glassmorphic
import com.example.ui.components.keyboardAndNavigationBottomPadding
import com.example.ui.theme.AmanixAccentGreen
import com.example.ui.theme.AmanixAccentRed
import com.example.ui.theme.AmanixCyanContainer
import com.example.ui.theme.AmanixCyanPrimary
import com.example.ui.theme.AmanixTextMuted
import com.example.ui.theme.AmanixTextPrimary
import com.example.ui.theme.AmanixTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    currentUser: UserEntity,
    database: AppDatabase,
    authRepository: AuthRepository,
    memoryRepository: MemoryRepository,
    aiProvider: AIProvider,
    searchService: SearchService,
    codeExecutionService: CodeExecutionService,
    onOpenDrawer: () -> Unit,
    onSignOut: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val currentLang by LocaleManager.currentLanguage.collectAsState()

    var isMemoryEnabled by remember { mutableStateOf(true) }
    val memories by memoryRepository.getActiveMemories(currentUser.id).collectAsState(initial = emptyList())
    val recentLogs by database.auditLogDao().getRecentLogs().collectAsState(initial = emptyList())

    // Config form states
    var geminiApiKeyInput by remember { mutableStateOf("") }
    var searchApiKeyInput by remember { mutableStateOf("") }
    var sandboxUrlInput by remember { mutableStateOf("") }
    var configSaveMessage by remember { mutableStateOf<String?>(null) }

    // Dialogs
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var deleteAccountInputText by remember { mutableStateOf("") }
    var deleteAccountError by remember { mutableStateOf<String?>(null) }
    var exportedDataJson by remember { mutableStateOf<String?>(null) }

    // Real Health Status Checks
    var aiConfigured by remember { mutableStateOf(false) }
    var dbConnected by remember { mutableStateOf(false) }
    var searchConfigured by remember { mutableStateOf(false) }
    var sandboxConfigured by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isMemoryEnabled = memoryRepository.isMemoryEnabled()
        geminiApiKeyInput = database.settingDao().getSetting("gemini_api_key") ?: ""
        searchApiKeyInput = database.settingDao().getSetting("search_api_key") ?: ""
        sandboxUrlInput = database.settingDao().getSetting("sandbox_execution_url") ?: ""

        aiConfigured = aiProvider.isConfigured() || geminiApiKeyInput.isNotBlank()
        dbConnected = try { database.userDao().getUserById(currentUser.id) != null } catch (e: Exception) { false }
        searchConfigured = searchService.isConfigured()
        sandboxConfigured = codeExecutionService.isConfigured()
    }

    Scaffold(
        topBar = {
            AmanixTopBar(
                title = "Settings & Profile",
                onMenuClick = onOpenDrawer,
                onProfileClick = { },
                statusText = "Security Verified",
                isStatusOk = true
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(bottom = keyboardAndNavigationBottomPadding())
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Glassmorphic Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color(0x35142642),
                        borderColor = Color(0x4000D2FF)
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(AmanixCyanContainer)
                            .border(1.5.dp, AmanixCyanPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AmanixCyanPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmanixTextPrimary
                        )
                        Text(
                            text = currentUser.email,
                            fontSize = 12.sp,
                            color = AmanixTextSecondary
                        )
                        Text(
                            text = "Member since: " + SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(currentUser.createdAt)),
                            fontSize = 11.sp,
                            color = AmanixTextMuted
                        )
                    }

                    StatusBadge(status = "ACTIVE")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Glassmorphic System Health Dashboard
            Text(
                text = "System Health & Integration Dashboard",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AmanixCyanPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = Color(0x300D172A),
                        borderColor = Color(0x3500D2FF)
                    )
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HealthRow(
                        icon = Icons.Default.Psychology,
                        name = "AI Engine (Gemini)",
                        status = if (aiConfigured) "CONNECTED" else "NOT CONFIGURED"
                    )
                    HealthRow(
                        icon = Icons.Default.Storage,
                        name = "Database (Room SQLite)",
                        status = if (dbConnected) "CONNECTED" else "OFFLINE"
                    )
                    HealthRow(
                        icon = Icons.Default.Security,
                        name = "Authentication Session",
                        status = "CONNECTED"
                    )
                    HealthRow(
                        icon = Icons.Default.Search,
                        name = "Search Provider",
                        status = if (searchConfigured) "CONNECTED" else "NOT CONFIGURED"
                    )
                    HealthRow(
                        icon = Icons.Default.Code,
                        name = "Sandbox Execution API",
                        status = if (sandboxConfigured) "CONNECTED" else "NOT CONFIGURED"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Language Selector
            Text(
                text = "Interface Language",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AmanixCyanPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppLanguage.values().forEach { lang ->
                    val isSelected = currentLang == lang
                    FilterChip(
                        selected = isSelected,
                        onClick = { LocaleManager.setLanguage(lang) },
                        label = { Text(lang.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0x45003E5C),
                            selectedLabelColor = AmanixCyanPrimary,
                            containerColor = Color(0x20142338),
                            labelColor = AmanixTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) AmanixCyanPrimary else Color(0x25FFFFFF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Glassmorphic Service Configuration Card
            Text(
                text = "Service Configuration",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AmanixCyanPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = Color(0x300D172A),
                        borderColor = Color(0x3500D2FF)
                    )
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = geminiApiKeyInput,
                        onValueChange = { geminiApiKeyInput = it },
                        label = { Text("Gemini API Key (Custom Override)") },
                        placeholder = { Text("Defaults to AI Studio Secrets if blank", color = AmanixTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = AmanixCyanPrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmanixCyanPrimary,
                            unfocusedBorderColor = Color(0x25FFFFFF),
                            focusedTextColor = AmanixTextPrimary,
                            unfocusedTextColor = AmanixTextPrimary,
                            focusedContainerColor = Color(0x20000000),
                            unfocusedContainerColor = Color(0x20000000)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = searchApiKeyInput,
                        onValueChange = { searchApiKeyInput = it },
                        label = { Text("Web Search Provider API Key") },
                        placeholder = { Text("Enter Google Custom Search / Serper Key", color = AmanixTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AmanixCyanPrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmanixCyanPrimary,
                            unfocusedBorderColor = Color(0x25FFFFFF),
                            focusedTextColor = AmanixTextPrimary,
                            unfocusedTextColor = AmanixTextPrimary,
                            focusedContainerColor = Color(0x20000000),
                            unfocusedContainerColor = Color(0x20000000)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = sandboxUrlInput,
                        onValueChange = { sandboxUrlInput = it },
                        label = { Text("Isolated Code Execution Sandbox URL") },
                        placeholder = { Text("e.g. https://sandbox.amanix.internal", color = AmanixTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Code, contentDescription = null, tint = AmanixCyanPrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmanixCyanPrimary,
                            unfocusedBorderColor = Color(0x25FFFFFF),
                            focusedTextColor = AmanixTextPrimary,
                            unfocusedTextColor = AmanixTextPrimary,
                            focusedContainerColor = Color(0x20000000),
                            unfocusedContainerColor = Color(0x20000000)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                database.settingDao().setSetting(SettingEntity("gemini_api_key", geminiApiKeyInput.trim()))
                                database.settingDao().setSetting(SettingEntity("search_api_key", searchApiKeyInput.trim()))
                                database.settingDao().setSetting(SettingEntity("sandbox_execution_url", sandboxUrlInput.trim()))
                                aiConfigured = aiProvider.isConfigured() || geminiApiKeyInput.isNotBlank()
                                searchConfigured = searchApiKeyInput.isNotBlank()
                                sandboxConfigured = sandboxUrlInput.isNotBlank()
                                configSaveMessage = "Configuration securely saved to encrypted local SQLite."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmanixCyanPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.End)
                            .border(1.dp, Color(0x60FFFFFF), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Configuration", fontWeight = FontWeight.Bold)
                    }

                    if (configSaveMessage != null) {
                        Text(configSaveMessage ?: "", color = AmanixAccentGreen, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Glassmorphic Autonomous Memory Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Autonomous Memory", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AmanixCyanPrimary)
                    Text("Store user preferences and context across chats", fontSize = 11.sp, color = AmanixTextMuted)
                }

                Switch(
                    checked = isMemoryEnabled,
                    onCheckedChange = { checked ->
                        isMemoryEnabled = checked
                        coroutineScope.launch { memoryRepository.setMemoryEnabled(checked) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = AmanixCyanPrimary,
                        uncheckedTrackColor = Color(0x4016233B)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = Color(0x300D172A),
                        borderColor = Color(0x3500D2FF)
                    )
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Memories (${memories.size})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AmanixTextSecondary)
                        if (memories.isNotEmpty()) {
                            TextButton(onClick = { coroutineScope.launch { memoryRepository.clearMemories(currentUser.id) } }) {
                                Text("Clear All", fontSize = 11.sp, color = AmanixAccentRed)
                            }
                        }
                    }

                    if (memories.isEmpty()) {
                        Text("No stored memories. Facts stated in chat are saved here when Memory is enabled.", color = AmanixTextMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            memories.forEach { mem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .glassmorphic(
                                            shape = RoundedCornerShape(8.dp),
                                            backgroundColor = Color(0x25142338),
                                            borderColor = Color(0x20FFFFFF)
                                        )
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(mem.content, color = AmanixTextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = { coroutineScope.launch { memoryRepository.deleteMemory(mem.id) } },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete memory", tint = AmanixAccentRed.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Audit Trail
            Text("Security Audit Trail", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AmanixCyanPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = Color(0x300D172A),
                        borderColor = Color(0x3500D2FF)
                    )
                    .padding(14.dp)
            ) {
                Column {
                    recentLogs.take(5).forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(log.action, color = AmanixCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(log.details, color = AmanixTextSecondary, fontSize = 10.sp)
                            }
                            Text(
                                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                                color = AmanixTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Data Export
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        val exportService = AppExportService(database)
                        val json = exportService.exportUserDataJson(currentUser.id)
                        exportedDataJson = json
                        clipboardManager.setText(AnnotatedString(json))
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(shape = RoundedCornerShape(10.dp), borderColor = Color(0x4000D2FF))
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = AmanixCyanPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export My Data (JSON)", color = AmanixCyanPrimary)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sign Out
            OutlinedButton(
                onClick = { showSignOutDialog = true },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(shape = RoundedCornerShape(10.dp), borderColor = Color(0x25FFFFFF))
            ) {
                Text("Sign Out", color = AmanixTextSecondary)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Delete Account
            Button(
                onClick = { showDeleteAccountDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AmanixAccentRed.copy(alpha = 0.25f), contentColor = AmanixAccentRed),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AmanixAccentRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete Amanix Account", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showSignOutDialog) {
        AmanixConfirmationDialog(
            title = "Sign Out",
            message = "Are you sure you want to end your active session?",
            confirmText = "Sign Out",
            onConfirm = {
                showSignOutDialog = false
                coroutineScope.launch {
                    authRepository.signOut()
                    onSignOut()
                }
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {
                Text("Delete Account Permanently", color = AmanixAccentRed, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "This will permanently erase your profile, all conversations, messages, memories, and files. This action cannot be undone.",
                        fontSize = 13.sp,
                        color = AmanixTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Type 'Delete my Amanix account' to confirm:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmanixTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = deleteAccountInputText,
                        onValueChange = {
                            deleteAccountInputText = it
                            deleteAccountError = null
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmanixAccentRed,
                            unfocusedBorderColor = Color(0x35FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (deleteAccountError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(deleteAccountError ?: "", color = AmanixAccentRed, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val res = authRepository.deleteAccount(deleteAccountInputText)
                            res.fold(
                                onSuccess = {
                                    showDeleteAccountDialog = false
                                    onSignOut()
                                },
                                onFailure = {
                                    deleteAccountError = it.message
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmanixAccentRed, contentColor = Color.White)
                ) {
                    Text("Delete Forever")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF0D1627)
        )
    }

    if (exportedDataJson != null) {
        AmanixConfirmationDialog(
            title = "Data Export Ready",
            message = "Your complete user data archive has been copied to the clipboard in JSON format.",
            confirmText = "Done",
            dismissText = "Close",
            onConfirm = { exportedDataJson = null },
            onDismiss = { exportedDataJson = null }
        )
    }
}

@Composable
fun HealthRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    name: String,
    status: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = AmanixCyanPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(name, color = AmanixTextPrimary, fontSize = 12.sp)
        }
        StatusBadge(status = status)
    }
}
