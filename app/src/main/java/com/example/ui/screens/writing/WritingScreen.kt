package com.example.ui.screens.writing

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AIProvider
import com.example.data.ai.AIState
import com.example.data.ai.AIStreamEvent
import com.example.data.ai.ModelMode
import com.example.ui.components.AmanixTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.components.glassmorphic
import com.example.ui.components.keyboardAndNavigationBottomPadding
import com.example.ui.theme.AmanixAccentRed
import com.example.ui.theme.AmanixCyanContainer
import com.example.ui.theme.AmanixCyanPrimary
import com.example.ui.theme.AmanixTextMuted
import com.example.ui.theme.AmanixTextPrimary
import com.example.ui.theme.AmanixTextSecondary
import kotlinx.coroutines.launch

enum class WritingMode(val label: String, val instruction: String) {
    WRITE("Write Draft", "Draft original, compelling, high-impact content based on the provided prompt and instructions."),
    EDIT("Edit & Refine", "Critically edit the text for clarity, conciseness, rhythm, and professional vocabulary without losing meaning."),
    SUMMARIZE("Summarize", "Synthesize the provided text into a clean executive summary highlighting core takeaways."),
    REWRITE("Rewrite", "Restructure and rewrite the input text in an alternate tone while preserving all factual content."),
    PROOFREAD("Proofread", "Fix grammatical errors, typos, punctuation, syntax inconsistencies, and explain the corrections."),
    TRANSLATE("Translate", "Translate the input text accurately while respecting cultural idioms and technical precision.")
}

@Composable
fun WritingScreen(
    aiProvider: AIProvider,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var inputText by remember { mutableStateOf("") }
    var selectedWritingMode by remember { mutableStateOf(WritingMode.WRITE) }
    var targetLanguage by remember { mutableStateOf("Spanish") }
    var outputText by remember { mutableStateOf("") }
    var aiState by remember { mutableStateOf(AIState.IDLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun processWriting() {
        if (inputText.isBlank()) return
        coroutineScope.launch {
            aiState = AIState.THINKING
            outputText = ""
            errorMessage = null

            val instruction = if (selectedWritingMode == WritingMode.TRANSLATE) {
                "${selectedWritingMode.instruction} Target language: $targetLanguage"
            } else {
                selectedWritingMode.instruction
            }

            aiProvider.generateStream(
                prompt = inputText,
                mode = ModelMode.MEDIUM,
                systemInstruction = "You are Amanix Writing Engine. $instruction"
            ).collect { event ->
                when (event) {
                    is AIStreamEvent.Thinking -> aiState = AIState.THINKING
                    is AIStreamEvent.Chunk -> {
                        aiState = AIState.GENERATING
                        outputText += event.text
                    }
                    is AIStreamEvent.Complete -> aiState = AIState.COMPLETED
                    is AIStreamEvent.Error -> {
                        aiState = event.state
                        errorMessage = event.message
                    }
                    is AIStreamEvent.Cancelled -> aiState = AIState.CANCELLED
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AmanixTopBar(
                title = "Writing & Editing",
                onMenuClick = onOpenDrawer,
                onProfileClick = onOpenSettings,
                statusText = selectedWritingMode.label,
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
            // Glassmorphic Input Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color(0x350F1D33),
                        borderColor = Color(0x4000D2FF)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    // Modes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WritingMode.values().forEach { mode ->
                            val isSelected = selectedWritingMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedWritingMode = mode },
                                label = { Text(mode.label, fontSize = 12.sp) },
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

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("Input Text / Topic Prompt") },
                        placeholder = { Text("Paste text to summarize, proofread, rewrite or type draft topic...", color = AmanixTextMuted) },
                        minLines = 4,
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmanixCyanPrimary,
                            unfocusedBorderColor = Color(0x30FFFFFF),
                            focusedTextColor = AmanixTextPrimary,
                            unfocusedTextColor = AmanixTextPrimary,
                            focusedContainerColor = Color(0x20000000),
                            unfocusedContainerColor = Color(0x20000000)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("writing_input_text")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { processWriting() },
                        enabled = inputText.isNotBlank() && aiState != AIState.THINKING && aiState != AIState.GENERATING,
                        colors = ButtonDefaults.buttonColors(containerColor = AmanixCyanPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .glassmorphic(shape = RoundedCornerShape(10.dp), borderColor = Color(0x60FFFFFF))
                            .testTag("process_writing_button")
                    ) {
                        if (aiState == AIState.THINKING || aiState == AIState.GENERATING) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing with AI Engine...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Process with Amanix AI", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Glassmorphic Output Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color(0x350C1628),
                        borderColor = Color(0x3500D2FF)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Output",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AmanixCyanPrimary
                        )

                        Row {
                            if (outputText.isNotEmpty()) {
                                IconButton(onClick = { processWriting() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = AmanixCyanPrimary, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { clipboardManager.setText(AnnotatedString(outputText)) }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AmanixTextMuted, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (errorMessage != null) {
                        StatusBadge(status = if (aiState == AIState.NOT_CONFIGURED) "NOT CONFIGURED" else "ERROR")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMessage ?: "", color = AmanixAccentRed, fontSize = 13.sp)
                    } else if (outputText.isEmpty() && aiState == AIState.IDLE) {
                        Text(
                            text = "Processed content will appear here in real-time. No fake or placeholder data is ever generated.",
                            color = AmanixTextMuted,
                            fontSize = 13.sp
                        )
                    } else {
                        Text(
                            text = outputText,
                            color = AmanixTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}
