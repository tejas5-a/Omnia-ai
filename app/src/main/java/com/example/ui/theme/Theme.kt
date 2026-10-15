package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = WarmDarkPrimary,
    onPrimary = WarmDarkOnPrimary,
    primaryContainer = WarmDarkPrimaryContainer,
    onPrimaryContainer = WarmDarkOnPrimaryContainer,
    background = WarmDarkBackground,
    onBackground = WarmDarkOnBackground,
    surface = WarmDarkSurface,
    onSurface = WarmDarkOnSurface,
    surfaceVariant = WarmDarkSurfaceVariant,
    onSurfaceVariant = WarmDarkOnSurfaceVariant,
    outline = WarmDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = WarmLightPrimary,
    onPrimary = WarmLightOnPrimary,
    primaryContainer = WarmLightPrimaryContainer,
    onPrimaryContainer = WarmLightOnPrimaryContainer,
    background = WarmLightBackground,
    onBackground = WarmLightOnBackground,
    surface = WarmLightSurface,
    onSurface = WarmLightOnSurface,
    surfaceVariant = WarmLightSurfaceVariant,
    onSurfaceVariant = WarmLightOnSurfaceVariant,
    outline = WarmLightOutline
)

@Composable
fun OmniaMaxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias for template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    OmniaMaxTheme(darkTheme = darkTheme, content = content)
}
