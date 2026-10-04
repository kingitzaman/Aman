package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AmanixDarkColorScheme = darkColorScheme(
    primary = AmanixCyanPrimary,
    onPrimary = AmanixCyanOnPrimary,
    primaryContainer = AmanixCyanContainer,
    onPrimaryContainer = AmanixCyanOnContainer,
    secondary = AmanixBlueSecondary,
    onSecondary = AmanixBlueOnSecondary,
    secondaryContainer = AmanixBlueContainer,
    onSecondaryContainer = AmanixBlueOnContainer,
    tertiary = AmanixAccentGreen,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    error = AmanixAccentRed,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),
    background = AmanixBackground,
    onBackground = AmanixTextPrimary,
    surface = AmanixSurface,
    onSurface = AmanixTextPrimary,
    surfaceVariant = AmanixSurfaceVariant,
    onSurfaceVariant = AmanixTextSecondary,
    surfaceContainer = AmanixSurfaceContainer,
    surfaceContainerHigh = AmanixSurfaceContainerHigh,
    outline = AmanixBorder,
    outlineVariant = Color(0xFF0F172A)
)

@Composable
fun AmanixTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AmanixDarkColorScheme,
        typography = Typography,
        content = content
    )
}
