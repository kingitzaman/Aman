package com.example.ui.screens.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.School
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
import kotlinx.coroutines.launch
import com.example.ui.theme.AmanixAccentRed
import com.example.ui.theme.AmanixCyanContainer
import com.example.ui.theme.AmanixCyanPrimary
import com.example.ui.theme.AmanixTextMuted
import com.example.ui.theme.AmanixTextPrimary
import com.example.ui.theme.AmanixTextSecondary

enum class LearnMode(val label: String, val instruction: String) {
    SIMPLE("Simple Explanation", "Explain the concept in simple, accessible words suitable for a beginner with clear metaphors."),
    DETAILED("Detailed Breakdown", "Provide an authoritative, in-depth academic explanation detailing mechanics, principles, and nuances."),
    STEP_BY_STEP("Step-by-Step", "Break down the topic into numbered, sequential, easy-to-follow chronological or logical steps."),
    EXAMPLES("Real-world Examples", "Provide practical, concrete real-world applications and use cases illustrating the concept."),
    QUIZ("Quiz Generation", "Create a 3-question conceptual quiz with multiple choice options and hidden explanations to test comprehension.")
}

@Composable
fun LearningScreen(
    aiProvider: AIProvider,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var topicInput by remember { mutableStateOf("") }
    var selectedLearnMode by remember { mutableStateOf(LearnMode.SIMPLE) }
    var explanationText by remember { mutableStateOf("") }
    var aiState by remember { mutableStateOf(AIState.IDLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var followUpInput by remember { mutableStateOf("") }

    fun requestExplanation(prompt: String, isFollowUp: Boolean = false) {
        if (prompt.isBlank()) return
        coroutineScope.launch {
            aiState = AIState.THINKING
            errorMessage = null
            if (!isFollowUp) explanationText = ""

            val fullPrompt = if (isFollowUp) {
                "Previous explanation context:\n$explanationText\n\nFollow-up question: $prompt"
            } else {
                "Explain the topic: '$prompt' according to the format style."
            }

            aiProvider.generateStream(
                prompt = fullPrompt,
                mode = ModelMode.MEDIUM,
                systemInstruction = "You are Amanix Learning Engine. ${selectedLearnMode.instruction}"
            ).collect { event ->
                when (event) {
                    is AIStreamEvent.Thinking -> aiState = AIState.THINKING
                    is AIStreamEvent.Chunk -> {
                        aiState = AIState.GENERATING
                        explanationText += event.text
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
                title = "Learning & Explain",
                onMenuClick = onOpenDrawer,
                onProfileClick = onOpenSettings,
                statusText = selectedLearnMode.label,
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
            // Glassmorphic Input Card
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
                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        label = { Text("What concept or topic do you want to learn?") },
                        placeholder = { Text("e.g., Photosynthesis, Quantum computing, or Neural Networks", color = AmanixTextMuted) },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = AmanixCyanPrimary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmanixCyanPrimary,
                            unfocusedBorderColor = Color(0x30FFFFFF),
                            focusedTextColor = AmanixTextPrimary,
                            unfocusedTextColor = AmanixTextPrimary,
                            focusedLabelColor = AmanixCyanPrimary,
                            focusedContainerColor = Color(0x20000000),
                            unfocusedContainerColor = Color(0x20000000)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("learning_topic_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mode Selector Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LearnMode.values().forEach { mode ->
                            val isSelected = selectedLearnMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedLearnMode = mode },
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

                    Button(
                        onClick = { requestExplanation(topicInput) },
                        enabled = topicInput.isNotBlank() && aiState != AIState.THINKING && aiState != AIState.GENERATING,
                        colors = ButtonDefaults.buttonColors(containerColor = AmanixCyanPrimary, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, Color(0x60FFFFFF), RoundedCornerShape(10.dp))
                            .testTag("generate_explanation_button")
                    ) {
                        if (aiState == AIState.THINKING || aiState == AIState.GENERATING) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Streaming Explanation...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Explain with Real AI Engine", fontWeight = FontWeight.Bold)
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
                            text = "Amanix Learning Engine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AmanixCyanPrimary
                        )

                        if (explanationText.isNotEmpty()) {
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(explanationText)) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AmanixTextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (errorMessage != null) {
                        StatusBadge(status = if (aiState == AIState.NOT_CONFIGURED) "NOT CONFIGURED" else "ERROR")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMessage ?: "", color = AmanixAccentRed, fontSize = 13.sp)
                    } else if (explanationText.isEmpty() && aiState == AIState.IDLE) {
                        Text(
                            text = "Enter a topic and select an explanation mode above. Live explanations will stream directly with authentic pedagogical formatting.",
                            color = AmanixTextMuted,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    } else {
                        Text(
                            text = explanationText,
                            color = AmanixTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Follow-up questions section
            if (explanationText.isNotEmpty() && aiState == AIState.COMPLETED) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic(
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = Color(0x400C1628),
                            borderColor = Color(0x5000D2FF)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = followUpInput,
                            onValueChange = { followUpInput = it },
                            placeholder = { Text("Ask a follow-up question...", fontSize = 13.sp, color = AmanixTextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = AmanixTextPrimary,
                                unfocusedTextColor = AmanixTextPrimary,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val q = followUpInput
                                followUpInput = ""
                                requestExplanation(q, isFollowUp = true)
                            },
                            enabled = followUpInput.isNotBlank()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send follow-up", tint = AmanixCyanPrimary)
                        }
                    }
                }
            }
        }
    }
}
