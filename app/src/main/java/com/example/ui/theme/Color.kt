package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// OMNIA MAX PREMIUM PALETTE
// ==========================================

// DARK MODE (DEFAULT)
val ChatDarkMainBg = Color(0xFF121212)
val ChatDarkDrawerBg = Color(0xFF171B27)
val ChatDarkChatBg = Color(0xFF121212)
val ChatDarkComposerBg = Color(0xFF242A35)
val ChatDarkSurfaceSecondary = Color(0xFF202635)
val ChatDarkElevatedSurface = Color(0xFF282F40)
val ChatDarkBorderDivider = Color(0xFF343C4C)
val ChatDarkTextPrimary = Color(0xFFF5F7FA)
val ChatDarkTextSecondary = Color(0xFFA7B0C0)
val ChatDarkTextMuted = Color(0xFF778196)
val ChatDarkBtnBg = Color(0xFF3478F6)
val ChatDarkBtnText = Color(0xFFFFFFFF)
val ChatDarkSelectedItemBg = Color(0xFF282F40)
val ChatDarkErrorRed = Color(0xFFF87171)
val ChatDarkSuccessGreen = Color(0xFF34D399)
val ChatDarkSecondaryAccent = Color(0xFF60A5FA)

// LIGHT MODE
val ChatLightMainBg = Color(0xFFFFFFFF)
val ChatLightDrawerBg = Color(0xFFF8FAFC)
val ChatLightChatBg = Color(0xFFFFFFFF)
val ChatLightComposerBg = Color(0xFFF1F5F9)
val ChatLightSurfaceSecondary = Color(0xFFF8FAFC)
val ChatLightElevatedSurface = Color(0xFFE2E8F0)
val ChatLightBorderDivider = Color(0xFFCBD5E1)
val ChatLightTextPrimary = Color(0xFF0F172A)
val ChatLightTextSecondary = Color(0xFF475569)
val ChatLightTextMuted = Color(0xFF64748B)
val ChatLightBtnBg = Color(0xFF2563EB)
val ChatLightBtnText = Color(0xFFFFFFFF)
val ChatLightSelectedItemBg = Color(0xFFE2E8F0)
val ChatLightErrorRed = Color(0xFFEF4444)
val ChatLightSuccessGreen = Color(0xFF10B981)
val ChatLightSecondaryAccent = Color(0xFF3B82F6)

// OMNIA Brand Accent Colors
val OmniaElectricBlue = Color(0xFF3478F6)
val OmniaRoyalBlue = Color(0xFF2563EB)
val OmniaNeonCyan = Color(0xFF60A5FA)
val OmniaBrightCyan = Color(0xFF38BDF8)

// Legacy / Compatibility Aliases
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
val OmniaElevatedSurface = ChatDarkElevatedSurface
val OmniaSubtleBorder = ChatDarkBorderDivider
val OmniaTextPrimary = ChatDarkTextPrimary
val OmniaTextSecondary = ChatDarkTextSecondary
val OmniaTextMuted = ChatDarkTextMuted
val OmniaErrorRed = ChatDarkErrorRed
val OmniaSuccessGreen = ChatDarkSuccessGreen
val OmniaWarningAmber = Color(0xFFF59E0B)

val OmniaPrimaryGradient = Brush.horizontalGradient(
    colors = listOf(OmniaElectricBlue, OmniaNeonCyan)
)
val OmniaCyanGradient = Brush.horizontalGradient(
    colors = listOf(OmniaElectricBlue, OmniaBrightCyan)
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
