package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkPurpleColorScheme = darkColorScheme(
    primary = OmniaPrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = OmniaElevatedSurface,
    onPrimaryContainer = OmniaLavenderHighlight,
    secondary = OmniaElectricViolet,
    onSecondary = OmniaMainBg,
    secondaryContainer = OmniaCardBg,
    onSecondaryContainer = OmniaTextSecondary,
    tertiary = OmniaAccentCyan,
    background = OmniaMainBg,
    onBackground = OmniaTextPrimary,
    surface = OmniaCardBg,
    onSurface = OmniaTextPrimary,
    surfaceVariant = OmniaElevatedSurface,
    onSurfaceVariant = OmniaTextSecondary,
    outline = OmniaElectricViolet.copy(alpha = 0.25f)
)

private val LightColorScheme = lightColorScheme(
    primary = OmniaPrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = OmniaElevatedSurface,
    onPrimaryContainer = OmniaLavenderHighlight,
    background = OmniaMainBg,
    onBackground = OmniaTextPrimary,
    surface = OmniaCardBg,
    onSurface = OmniaTextPrimary,
    surfaceVariant = OmniaElevatedSurface,
    onSurfaceVariant = OmniaTextSecondary,
    outline = OmniaElectricViolet.copy(alpha = 0.25f)
)

@Composable
fun OmniaMaxTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkPurpleColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    OmniaMaxTheme(darkTheme = darkTheme, content = content)
}
