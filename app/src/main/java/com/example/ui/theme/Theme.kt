package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color(0xFF001E2E),
    primaryContainer = DeepBlueContainer,
    onPrimaryContainer = Color(0xFFDDF2FF),
    secondary = NeonPurple,
    onSecondary = Color(0xFF2E004F),
    secondaryContainer = DeepPurpleContainer,
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = AccentCyan,
    background = BgDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF2B3958),
    error = AccentPink,
    onError = Color.White
)

private val LightColorScheme = DarkColorScheme // Always enforce modern Dark aesthetic for KAKA AI as requested

@Composable
fun KakaAiTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
