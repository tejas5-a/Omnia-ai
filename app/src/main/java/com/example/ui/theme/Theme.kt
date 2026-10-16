package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ChatDarkBtnBg,
    onPrimary = ChatDarkBtnText,
    primaryContainer = ChatDarkComposerBg,
    onPrimaryContainer = ChatDarkTextPrimary,
    secondary = ChatDarkSurfaceSecondary,
    onSecondary = ChatDarkTextPrimary,
    secondaryContainer = ChatDarkSelectedItemBg,
    onSecondaryContainer = ChatDarkTextPrimary,
    tertiary = OmniaNeonCyan,
    onTertiary = Color.Black,
    tertiaryContainer = ChatDarkComposerBg,
    onTertiaryContainer = ChatDarkTextPrimary,
    background = ChatDarkMainBg,
    onBackground = ChatDarkTextPrimary,
    surface = ChatDarkSurfaceSecondary,
    onSurface = ChatDarkTextPrimary,
    surfaceVariant = ChatDarkComposerBg,
    onSurfaceVariant = ChatDarkTextSecondary,
    outline = ChatDarkBorderDivider,
    outlineVariant = ChatDarkBorderDivider.copy(alpha = 0.6f),
    error = ChatDarkErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ChatLightBtnBg,
    onPrimary = ChatLightBtnText,
    primaryContainer = ChatLightComposerBg,
    onPrimaryContainer = ChatLightTextPrimary,
    secondary = ChatLightSurfaceSecondary,
    onSecondary = ChatLightTextPrimary,
    secondaryContainer = ChatLightSelectedItemBg,
    onSecondaryContainer = ChatLightTextPrimary,
    tertiary = OmniaElectricBlue,
    onTertiary = Color.White,
    tertiaryContainer = ChatLightComposerBg,
    onTertiaryContainer = ChatLightTextPrimary,
    background = ChatLightMainBg,
    onBackground = ChatLightTextPrimary,
    surface = ChatLightSurfaceSecondary,
    onSurface = ChatLightTextPrimary,
    surfaceVariant = ChatLightComposerBg,
    onSurfaceVariant = ChatLightTextSecondary,
    outline = ChatLightBorderDivider,
    outlineVariant = ChatLightBorderDivider.copy(alpha = 0.6f),
    error = ChatLightErrorRed,
    onError = Color.White
)

@Composable
fun OmniaMaxTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    OmniaMaxTheme(darkTheme = darkTheme, content = content)
}
