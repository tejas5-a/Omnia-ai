package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// CHATGPT NEUTRAL PALETTE — OMNIA MAX
// ==========================================

// DARK MODE (DEFAULT)
val ChatDarkMainBg = Color(0xFF212121)
val ChatDarkDrawerBg = Color(0xFF171717)
val ChatDarkChatBg = Color(0xFF212121)
val ChatDarkComposerBg = Color(0xFF303030)
val ChatDarkSurfaceSecondary = Color(0xFF2A2A2A)
val ChatDarkBorderDivider = Color(0xFF424242)
val ChatDarkTextPrimary = Color(0xFFF5F5F5)
val ChatDarkTextSecondary = Color(0xFFB4B4B4)
val ChatDarkTextMuted = Color(0xFF8E8E8E)
val ChatDarkBtnBg = Color(0xFFFFFFFF)
val ChatDarkBtnText = Color(0xFF171717)
val ChatDarkSelectedItemBg = Color(0xFF303030)
val ChatDarkErrorRed = Color(0xFFFF6B6B)
val ChatDarkSuccessGreen = Color(0xFF81C995)

// LIGHT MODE
val ChatLightMainBg = Color(0xFFFFFFFF)
val ChatLightDrawerBg = Color(0xFFF9F9F9)
val ChatLightChatBg = Color(0xFFFFFFFF)
val ChatLightComposerBg = Color(0xFFF4F4F4)
val ChatLightSurfaceSecondary = Color(0xFFF7F7F7)
val ChatLightBorderDivider = Color(0xFFE5E5E5)
val ChatLightTextPrimary = Color(0xFF0D0D0D)
val ChatLightTextSecondary = Color(0xFF5D5D5D)
val ChatLightTextMuted = Color(0xFF8F8F8F)
val ChatLightBtnBg = Color(0xFF171717)
val ChatLightBtnText = Color(0xFFFFFFFF)
val ChatLightSelectedItemBg = Color(0xFFEEEEEE)
val ChatLightErrorRed = Color(0xFFC62828)
val ChatLightSuccessGreen = Color(0xFF238636)

// OMNIA MAX Brand Accent Colors (Restrained)
val OmniaElectricBlue = Color(0xFF1E56FD)
val OmniaRoyalBlue = Color(0xFF2F80ED)
val OmniaNeonCyan = Color(0xFF38BDF8)
val OmniaBrightCyan = Color(0xFF00D2FF)

// Legacy Aliases for backwards compatibility across all screens
val OmniaPrimaryPurple = OmniaElectricBlue
val OmniaElectricViolet = OmniaRoyalBlue
val OmniaLavenderHighlight = OmniaNeonCyan
val OmniaAccentCyan = OmniaBrightCyan

val AgentResearchColor = OmniaNeonCyan
val AgentCodingColor = OmniaElectricBlue
val AgentTestingColor = ChatDarkSuccessGreen
val AgentWritingColor = OmniaBrightCyan
val AgentSecurityColor = ChatDarkErrorRed
val AgentReviewColor = OmniaRoyalBlue

val OmniaMainBg = ChatDarkMainBg
val OmniaSecondaryBg = ChatDarkDrawerBg
val OmniaCardBg = ChatDarkSurfaceSecondary
val OmniaElevatedSurface = ChatDarkComposerBg
val OmniaSubtleBorder = ChatDarkBorderDivider
val OmniaTextPrimary = ChatDarkTextPrimary
val OmniaTextSecondary = ChatDarkTextSecondary
val OmniaTextMuted = ChatDarkTextMuted
val OmniaErrorRed = ChatDarkErrorRed
val OmniaSuccessGreen = ChatDarkSuccessGreen
val OmniaWarningAmber = Color(0xFFF59E0B)

val OmniaPrimaryGradient = Brush.horizontalGradient(
    colors = listOf(ChatDarkMainBg, ChatDarkSurfaceSecondary)
)
val OmniaCyanGradient = Brush.horizontalGradient(
    colors = listOf(OmniaElectricBlue, OmniaNeonCyan)
)
val OmniaCardBorderGradient = Brush.horizontalGradient(
    colors = listOf(ChatDarkBorderDivider, ChatDarkBorderDivider)
)

val WarmDarkBackground = ChatDarkMainBg
val WarmDarkSurface = ChatDarkSurfaceSecondary
val WarmDarkSurfaceVariant = ChatDarkComposerBg
val WarmDarkPrimary = ChatDarkBtnBg
val WarmDarkOnPrimary = ChatDarkBtnText
val WarmDarkOnBackground = ChatDarkTextPrimary
val WarmDarkOnSurface = ChatDarkTextPrimary
val WarmDarkOnSurfaceVariant = ChatDarkTextSecondary
val WarmDarkOutline = ChatDarkBorderDivider
