package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MessageEntity
import com.example.ui.theme.AmanixAccentAmber
import com.example.ui.theme.AmanixAccentGreen
import com.example.ui.theme.AmanixAccentRed
import com.example.ui.theme.AmanixBlueSecondary
import com.example.ui.theme.AmanixCyanContainer
import com.example.ui.theme.AmanixCyanOnContainer
import com.example.ui.theme.AmanixCyanPrimary
import com.example.ui.theme.AmanixTextMuted
import com.example.ui.theme.AmanixTextPrimary
import com.example.ui.theme.AmanixTextSecondary

@Composable
fun AmanixLogoEmblem(
    modifier: Modifier = Modifier,
    sizeDp: Int = 36
) {
    val infiniteTransition = rememberInfiniteTransition(label = "crestGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(sizeDp / 3))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0x7700384D),
                        Color(0x55081528)
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        AmanixCyanPrimary.copy(alpha = glowAlpha),
                        Color(0x40FFFFFF),
                        Color(0x334F8CFF)
                    )
                ),
                RoundedCornerShape(sizeDp / 3)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Amanix Crest",
            tint = AmanixCyanPrimary,
            modifier = Modifier.size((sizeDp * 0.6).dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmanixTopBar(
    title: String,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit,
    statusText: String? = null,
    isStatusOk: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0x500C1629),
                        Color(0x28080F1D)
                    )
                )
            )
            .border(
                width = 0.8.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0x1000D2FF),
                        Color(0x4000D2FF),
                        Color(0x20FFFFFF),
                        Color(0x1000D2FF)
                    )
                ),
                shape = RoundedCornerShape(0.dp)
            )
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = AmanixTextPrimary
            ),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AmanixLogoEmblem(sizeDp = 30)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AMANIX",
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                fontSize = 16.sp,
                                color = AmanixCyanPrimary
                            )
                            if (title.isNotEmpty() && title != "AMANIX") {
                                Text(
                                    text = " • $title",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AmanixTextSecondary
                                )
                            }
                        }
                        if (statusText != null) {
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                color = if (isStatusOk) AmanixAccentGreen else AmanixAccentAmber
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.testTag("nav_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Navigation Menu",
                        tint = AmanixTextPrimary
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.testTag("profile_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0x4414243D))
                            .border(
                                1.dp,
                                Brush.linearGradient(listOf(AmanixCyanPrimary, Color(0x33FFFFFF))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Profile",
                            tint = AmanixCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (status.uppercase()) {
        "CONNECTED", "ONLINE", "ACTIVE", "READY" -> Triple(
            AmanixAccentGreen.copy(alpha = 0.20f),
            AmanixAccentGreen,
            AmanixAccentGreen.copy(alpha = 0.5f)
        )
        "NOT CONFIGURED", "NOT_CONFIGURED", "UNAVAILABLE" -> Triple(
            AmanixAccentAmber.copy(alpha = 0.20f),
            AmanixAccentAmber,
            AmanixAccentAmber.copy(alpha = 0.5f)
        )
        "OFFLINE", "FAILED", "ERROR" -> Triple(
            AmanixAccentRed.copy(alpha = 0.20f),
            AmanixAccentRed,
            AmanixAccentRed.copy(alpha = 0.5f)
        )
        else -> Triple(
            AmanixCyanPrimary.copy(alpha = 0.20f),
            AmanixCyanPrimary,
            AmanixCyanPrimary.copy(alpha = 0.5f)
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                1.dp,
                Brush.linearGradient(listOf(borderColor, Color(0x30FFFFFF), borderColor)),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ChatBubble(
    message: MessageEntity,
    onRegenerate: (() -> Unit)? = null
) {
    val isUser = message.role == "user"
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .testTag("chat_bubble_${message.role}")
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) {
                        Brush.linearGradient(
                            listOf(
                                Color(0x55004A6B),
                                Color(0x30002A3D)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color(0x4014233B),
                                Color(0x200C1628)
                            )
                        )
                    }
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        if (isUser) {
                            listOf(
                                AmanixCyanPrimary.copy(alpha = 0.65f),
                                Color(0x40FFFFFF),
                                AmanixCyanPrimary.copy(alpha = 0.35f)
                            )
                        } else {
                            listOf(
                                Color(0x4000D2FF),
                                Color(0x20FFFFFF),
                                Color(0x304F8CFF),
                                Color(0x1500D2FF)
                            )
                        }
                    ),
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(14.dp)
        ) {
            Column {
                // Header for assistant message
                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        AmanixLogoEmblem(sizeDp = 18)
                        Text(
                            text = "AMANIX",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmanixCyanPrimary,
                            letterSpacing = 1.sp
                        )
                        if (message.model != null) {
                            Text(
                                text = "• ${message.model}",
                                fontSize = 10.sp,
                                color = AmanixTextMuted
                            )
                        }
                        if (message.toolUsed != null) {
                            Text(
                                text = "• Used ${message.toolUsed}",
                                fontSize = 10.sp,
                                color = AmanixBlueSecondary
                            )
                        }
                    }
                }

                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = if (isUser) AmanixCyanOnContainer else AmanixTextPrimary
                )

                // Actions row
                if (!isUser && message.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x30101E35))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(message.content))
                                    isCopied = true
                                }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = if (isCopied) AmanixAccentGreen else AmanixTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCopied) "Copied" else "Copy",
                                fontSize = 11.sp,
                                color = if (isCopied) AmanixAccentGreen else AmanixTextMuted
                            )
                        }

                        if (onRegenerate != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x30101E35))
                                    .clickable { onRegenerate() }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Regenerate",
                                    fontSize = 11.sp,
                                    color = AmanixCyanPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinkingPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .glassmorphic(
                shape = RoundedCornerShape(14.dp),
                backgroundColor = Color(0x40102035),
                borderColor = AmanixCyanPrimary.copy(alpha = alpha)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AmanixLogoEmblem(sizeDp = 18)
        Text(
            text = "Amanix is thinking...",
            fontSize = 12.sp,
            color = AmanixCyanPrimary.copy(alpha = alpha),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AmanixConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isDestructive) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AmanixAccentRed)
                }
                Text(text = title, fontWeight = FontWeight.Bold, color = AmanixTextPrimary)
            }
        },
        text = {
            Text(text = message, fontSize = 14.sp, color = AmanixTextSecondary)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) AmanixAccentRed else AmanixCyanPrimary,
                    contentColor = if (isDestructive) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("dialog_confirm_button")
            ) {
                Text(confirmText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("dialog_cancel_button")
            ) {
                Text(dismissText, color = AmanixTextSecondary)
            }
        },
        containerColor = Color(0xFF0F1829),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.border(
            1.dp,
            Brush.linearGradient(
                listOf(
                    if (isDestructive) AmanixAccentRed.copy(alpha = 0.5f) else Color(0x5500D2FF),
                    Color(0x25FFFFFF),
                    Color(0x304F8CFF)
                )
            ),
            RoundedCornerShape(20.dp)
        )
    )
}
