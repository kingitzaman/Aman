package com.example.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AIProvider
import com.example.data.ai.AIState
import com.example.data.ai.AIStreamEvent
import com.example.data.ai.ModelMode
import com.example.data.i18n.LocaleManager
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.ChatRepository
import com.example.data.repository.MemoryRepository
import com.example.ui.components.AmanixConfirmationDialog
import com.example.ui.components.AmanixLogoEmblem
import com.example.ui.components.AmanixTopBar
import com.example.ui.components.ChatBubble
import com.example.ui.components.StatusBadge
import com.example.ui.components.ThinkingIndicator
import com.example.ui.components.glassmorphic
import com.example.ui.components.keyboardAndNavigationBottomPadding
import com.example.ui.theme.AmanixAccentRed
import com.example.ui.theme.AmanixCyanContainer
import com.example.ui.theme.AmanixCyanPrimary
import com.example.ui.theme.AmanixTextMuted
import com.example.ui.theme.AmanixTextPrimary
import com.example.ui.theme.AmanixTextSecondary
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUser: UserEntity,
    chatRepository: ChatRepository,
    memoryRepository: MemoryRepository,
    aiProvider: AIProvider,
    onOpenDrawer: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val conversations by chatRepository.getConversations(currentUser.id).collectAsState(initial = emptyList())

    var activeConversation by remember { mutableStateOf<ConversationEntity?>(null) }
    var inputText by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(ModelMode.MEDIUM) }
    var aiState by remember { mutableStateOf(AIState.IDLE) }
    var currentStreamingText by remember { mutableStateOf("") }
    var streamJob by remember { mutableStateOf<Job?>(null) }

    var showModelSheet by remember { mutableStateOf(false) }
    var showConversationsSheet by remember { mutableStateOf(false) }
    var showMicNotice by remember { mutableStateOf(false) }
    var showAttachNotice by remember { mutableStateOf(false) }

    // If activeConversation is null and conversations exist, select first
    LaunchedEffect(conversations) {
        if (activeConversation == null && conversations.isNotEmpty()) {
            activeConversation = conversations.first()
        }
    }

    val messages = if (activeConversation != null) {
        chatRepository.getMessages(activeConversation!!.id).collectAsState(initial = emptyList()).value
    } else {
        emptyList()
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, currentStreamingText) {
        if (messages.isNotEmpty() || currentStreamingText.isNotEmpty()) {
            listState.animateScrollToItem(maxOf(0, messages.size))
        }
    }

    fun handleSend(promptText: String) {
        if (promptText.isBlank()) return
        val currentPrompt = promptText.trim()
        inputText = ""

        coroutineScope.launch {
            val conversation = activeConversation ?: run {
                val newConv = chatRepository.createConversation(
                    userId = currentUser.id,
                    title = currentPrompt.take(30),
                    model = selectedMode.modelId
                )
                activeConversation = newConv
                newConv
            }

            val userMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conversation.id,
                role = "user",
                content = currentPrompt,
                model = selectedMode.modelId
            )
            chatRepository.saveMessage(userMsg)

            if (currentPrompt.contains("my name is", ignoreCase = true) || currentPrompt.contains("i like", ignoreCase = true)) {
                memoryRepository.addMemory(currentUser.id, currentPrompt, "chat")
            }

            aiState = AIState.THINKING
            currentStreamingText = ""

            streamJob = launch {
                aiProvider.generateStream(
                    prompt = currentPrompt,
                    mode = selectedMode,
                    history = messages
                ).collect { event ->
                    when (event) {
                        is AIStreamEvent.Thinking -> {
                            aiState = AIState.THINKING
                        }
                        is AIStreamEvent.Chunk -> {
                            aiState = AIState.GENERATING
                            currentStreamingText += event.text
                        }
                        is AIStreamEvent.Complete -> {
                            aiState = AIState.COMPLETED
                            val assistantMsg = MessageEntity(
                                id = UUID.randomUUID().toString(),
                                conversationId = conversation.id,
                                role = "assistant",
                                content = event.fullText,
                                model = event.modelUsed,
                                state = "COMPLETED"
                            )
                            chatRepository.saveMessage(assistantMsg)
                            currentStreamingText = ""
                        }
                        is AIStreamEvent.Error -> {
                            aiState = event.state
                            val errorMsg = MessageEntity(
                                id = UUID.randomUUID().toString(),
                                conversationId = conversation.id,
                                role = "assistant",
                                content = event.message,
                                model = selectedMode.modelId,
                                state = event.state.name
                            )
                            chatRepository.saveMessage(errorMsg)
                            currentStreamingText = ""
                        }
                        is AIStreamEvent.Cancelled -> {
                            aiState = AIState.CANCELLED
                            if (currentStreamingText.isNotBlank()) {
                                val partialMsg = MessageEntity(
                                    id = UUID.randomUUID().toString(),
                                    conversationId = conversation.id,
                                    role = "assistant",
                                    content = "$currentStreamingText [Generation stopped by user]",
                                    model = selectedMode.modelId,
                                    state = "CANCELLED"
                                )
                                chatRepository.saveMessage(partialMsg)
                            }
                            currentStreamingText = ""
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AmanixTopBar(
                title = activeConversation?.title ?: "Chat",
                onMenuClick = onOpenDrawer,
                onProfileClick = onOpenProfile,
                statusText = selectedMode.label,
                isStatusOk = aiState != AIState.FAILED && aiState != AIState.OFFLINE
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
        ) {
            // Glassmorphic Sub-Bar with Mode Selector & History
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        shape = RoundedCornerShape(0.dp),
                        backgroundColor = Color(0x350A1322),
                        borderColor = Color(0x2000D2FF),
                        borderWidth = 0.5.dp
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .glassmorphic(
                            shape = RoundedCornerShape(8.dp),
                            backgroundColor = Color(0x4013233A),
                            borderColor = Color(0x5000D2FF)
                        )
                        .clickable { showModelSheet = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Model mode",
                        tint = AmanixCyanPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MODE: ${selectedMode.label}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmanixCyanPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { showConversationsSheet = true }
                    ) {
                        Text(
                            text = "History (${conversations.size})",
                            fontSize = 12.sp,
                            color = AmanixTextSecondary
                        )
                    }

                    IconButton(
                        onClick = {
                            activeConversation = null
                            currentStreamingText = ""
                            aiState = AIState.IDLE
                        },
                        modifier = Modifier
                            .size(30.dp)
                            .glassmorphic(
                                shape = CircleShape,
                                backgroundColor = Color(0x35003E5C),
                                borderColor = AmanixCyanPrimary
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat",
                            tint = AmanixCyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Message List or Empty State
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty() && currentStreamingText.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AmanixLogoEmblem(sizeDp = 64)
                        Spacer(modifier = Modifier.height(18.dp))

                        // Dynamic Greeting with user's real profile name
                        Text(
                            text = "${LocaleManager.tr("greeting_prefix")} ${currentUser.name}?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmanixTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = LocaleManager.tr("app_tagline"),
                            fontSize = 12.sp,
                            color = AmanixCyanPrimary,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Glassmorphic suggestion prompt cards
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .glassmorphic(
                                    shape = RoundedCornerShape(14.dp),
                                    backgroundColor = Color(0x2A101D30),
                                    borderColor = Color(0x3500D2FF)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Ask a deep question, request code analysis, or explore problem solving with live streaming intelligence.",
                                fontSize = 13.sp,
                                color = AmanixTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 19.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatBubble(
                                message = msg,
                                onRegenerate = if (msg.role == "assistant" && messages.indexOf(msg) == messages.size - 1) {
                                    {
                                        val lastUserMsg = messages.findLast { it.role == "user" }
                                        if (lastUserMsg != null) handleSend(lastUserMsg.content)
                                    }
                                } else null
                            )
                        }

                        if (aiState == AIState.THINKING) {
                            item { ThinkingIndicator() }
                        } else if (currentStreamingText.isNotEmpty()) {
                            item {
                                ChatBubble(
                                    message = MessageEntity(
                                        id = "streaming",
                                        conversationId = activeConversation?.id ?: "",
                                        role = "assistant",
                                        content = currentStreamingText,
                                        model = selectedMode.modelId
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Stop generation glass pill
            AnimatedVisibility(
                visible = aiState == AIState.GENERATING || aiState == AIState.THINKING,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .glassmorphic(
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = Color(0x403B0D14),
                            borderColor = AmanixAccentRed.copy(alpha = 0.7f)
                        )
                        .clickable {
                            streamJob?.cancel()
                            aiState = AIState.CANCELLED
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = AmanixAccentRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = LocaleManager.tr("stop_generation"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmanixAccentRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Glassmorphic Chat Composer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .glassmorphic(
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = Color(0x450C1628),
                        borderColor = Color(0x5500D2FF),
                        borderWidth = 1.dp
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showAttachNotice = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach file",
                            tint = AmanixTextSecondary
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = LocaleManager.tr("chat_placeholder"),
                                fontSize = 13.sp,
                                color = AmanixTextMuted
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = AmanixTextPrimary,
                            unfocusedTextColor = AmanixTextPrimary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        maxLines = 4,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (inputText.isBlank()) {
                        IconButton(
                            onClick = { showMicNotice = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = AmanixCyanPrimary
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { handleSend(inputText) },
                            enabled = aiState != AIState.THINKING && aiState != AIState.GENERATING,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(AmanixCyanPrimary, Color(0xFF0099CC))
                                    )
                                )
                                .border(1.dp, Color(0x80FFFFFF), CircleShape)
                                .testTag("send_message_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Glassmorphic Model Selector Sheet
    if (showModelSheet) {
        ModalBottomSheet(
            onDismissRequest = { showModelSheet = false },
            containerColor = Color(0xF20B1324),
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Select Intelligence Engine Mode",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmanixCyanPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                ModelMode.values().forEach { mode ->
                    val isSelected = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .glassmorphic(
                                shape = RoundedCornerShape(12.dp),
                                backgroundColor = if (isSelected) Color(0x45003E5C) else Color(0x2514243A),
                                borderColor = if (isSelected) AmanixCyanPrimary else Color(0x30FFFFFF)
                            )
                            .clickable {
                                selectedMode = mode
                                showModelSheet = false
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AmanixCyanPrimary else AmanixTextPrimary
                                )
                                Text(
                                    text = mode.description,
                                    fontSize = 12.sp,
                                    color = AmanixTextSecondary
                                )
                                Text(
                                    text = "Model ID: ${mode.modelId}",
                                    fontSize = 10.sp,
                                    color = AmanixTextMuted
                                )
                            }
                            StatusBadge(status = if (aiProvider.isConfigured()) "READY" else "NOT CONFIGURED")
                        }
                    }
                }
            }
        }
    }

    // Glassmorphic Conversations Sheet
    if (showConversationsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showConversationsSheet = false },
            containerColor = Color(0xF20B1324)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Conversations",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmanixTextPrimary
                    )
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val newConv = chatRepository.createConversation(
                                    userId = currentUser.id,
                                    title = "New Chat",
                                    model = selectedMode.modelId
                                )
                                activeConversation = newConv
                                showConversationsSheet = false
                            }
                        },
                        modifier = Modifier.glassmorphic(shape = CircleShape, backgroundColor = Color(0x35003E5C))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New", tint = AmanixCyanPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (conversations.isEmpty()) {
                    Text(
                        text = LocaleManager.tr("no_conversations"),
                        fontSize = 13.sp,
                        color = AmanixTextMuted,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(conversations, key = { it.id }) { conv ->
                            val isSelected = activeConversation?.id == conv.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .glassmorphic(
                                        shape = RoundedCornerShape(10.dp),
                                        backgroundColor = if (isSelected) Color(0x40003E5C) else Color(0x20142338),
                                        borderColor = if (isSelected) AmanixCyanPrimary else Color(0x20FFFFFF)
                                    )
                                    .clickable {
                                        activeConversation = conv
                                        showConversationsSheet = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = conv.title,
                                    fontSize = 13.sp,
                                    color = AmanixTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            chatRepository.deleteConversation(conv.id)
                                            if (activeConversation?.id == conv.id) {
                                                activeConversation = null
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete chat",
                                        tint = AmanixAccentRed.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMicNotice) {
        AmanixConfirmationDialog(
            title = "Audio Input",
            message = "Voice transcription requires RECORD_AUDIO permission and native STT service. Type in the composer to prompt Amanix.",
            confirmText = "OK",
            dismissText = "Cancel",
            onConfirm = { showMicNotice = false },
            onDismiss = { showMicNotice = false }
        )
    }

    if (showAttachNotice) {
        AmanixConfirmationDialog(
            title = "File Attachment",
            message = "Amanix supports PDF, TXT, DOCX, and CSV attachments. Files are validated for size and MIME constraints prior to ingestion.",
            confirmText = "OK",
            dismissText = "Cancel",
            onConfirm = { showAttachNotice = false },
            onDismiss = { showAttachNotice = false }
        )
    }
}
