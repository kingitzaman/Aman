package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmanixBackground
import com.example.ui.theme.AmanixCyanPrimary

fun Modifier.glassmorphic(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = Color(0x30132238),
    borderColor: Color = Color(0x4000D2FF),
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(
        Brush.linearGradient(
            colors = listOf(
                backgroundColor.copy(alpha = 0.45f),
                backgroundColor.copy(alpha = 0.20f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    )
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                borderColor,
                Color(0x33FFFFFF),
                Color(0x254F8CFF),
                Color(0x1500D2FF)
            )
        ),
        shape = shape
    )

@Composable
fun AmanixGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmanixBackground)
            .drawBehind {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Top right glowing cyan nebula orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x3800D2FF),
                            Color(0x180088AA),
                            Color.Transparent
                        ),
                        center = Offset(canvasWidth * 0.90f, canvasHeight * 0.12f),
                        radius = canvasWidth * 0.75f
                    ),
                    center = Offset(canvasWidth * 0.90f, canvasHeight * 0.12f),
                    radius = canvasWidth * 0.75f
                )

                // Middle left glowing cobalt blue orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x2E4F8CFF),
                            Color(0x141E3A8A),
                            Color.Transparent
                        ),
                        center = Offset(canvasWidth * 0.05f, canvasHeight * 0.52f),
                        radius = canvasWidth * 0.80f
                    ),
                    center = Offset(canvasWidth * 0.05f, canvasHeight * 0.52f),
                    radius = canvasWidth * 0.80f
                )

                // Bottom right glowing deep indigo orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x280284C7),
                            Color(0x100F172A),
                            Color.Transparent
                        ),
                        center = Offset(canvasWidth * 0.85f, canvasHeight * 0.88f),
                        radius = canvasWidth * 0.70f
                    ),
                    center = Offset(canvasWidth * 0.85f, canvasHeight * 0.88f),
                    radius = canvasWidth * 0.70f
                )
            }
    ) {
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = Color(0x30132238),
    borderColor: Color = Color(0x4000D2FF),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.glassmorphic(shape = shape, backgroundColor = backgroundColor, borderColor = borderColor),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        content()
    }
}

/**
 * Calculates bottom inset that unites navigation bars and soft keyboard (IME).
 * This ensures the search box or composer sits flush right above the keyboard without
 * floating or exhibiting an awkward gap.
 */
@Composable
fun keyboardAndNavigationBottomPadding(): Dp {
    val insets = WindowInsets.navigationBars.union(WindowInsets.ime)
    return insets.asPaddingValues().calculateBottomPadding()
}
