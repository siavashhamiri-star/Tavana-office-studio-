package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// AVA COLOR SYSTEM — Warm, Joyful, Musical
// Sunset / Coral / Apricot / Peach / Warm Gold
// ==========================================

// Core Brand Tones
val AvaSunsetCoral = Color(0xFFFF5E4D)      // Primary brand anchor: energetic, joyful
val AvaSunsetCoralDark = Color(0xFFE04535)  // Focused/Pressed state
val AvaSunsetCoralLight = Color(0xFFFF8B7D) // High-contrast highlight on dark

val AvaApricot = Color(0xFFFF9E6C)          // Secondary accent: warm, creative
val AvaApricotLight = Color(0xFFFFC5A6)     // Gentle luminous tint
val AvaPeach = Color(0xFFFFB69E)            // Tertiary accent: soft, welcoming

val AvaGoldenHighlight = Color(0xFFFFC043)  // Score, celebration, active musical state
val AvaSunburstAmber = Color(0xFFFF9E1B)    // Intense warm highlight
val AvaWarmPink = Color(0xFFFF708F)         // Melodic rhythm accent

// Functional State Colors
val AvaRecordingActive = Color(0xFFFF2B44)  // Live recording status (unmistakable, WCAG safe)
val AvaKaraokeActive = Color(0xFFFFB800)    // Real-time singing note / active lyric state
val AvaScoreHigh = Color(0xFFFFB800)        // Gold achievement
val AvaScoreMid = Color(0xFFFF944D)         // Coral achievement
val AvaSuccess = Color(0xFF2E9E66)          // Positive completion
val AvaError = Color(0xFFE53935)            // High contrast error

// Light Mode Neutral Surfaces (Warm Porcelain / Cream)
val AvaPorcelainBgLight = Color(0xFFFFF9F6)     // Warm gentle canvas
val AvaSurfaceLight = Color(0xFFFFFFFF)         // Clean card elevated surface
val AvaSurfaceVariantLight = Color(0xFFFFF0EA)  // Tinted warm coral-cream container
val AvaSurfaceSubtleLight = Color(0xFFFAECE6)   // Subtle divider/inactive outline
val AvaTextPrimaryLight = Color(0xFF2A1C19)     // Rich warm espresso (>= 9:1 contrast)
val AvaTextSecondaryLight = Color(0xFF705751)   // Warm cocoa text (>= 4.5:1 contrast)
val AvaTextMutedLight = Color(0xFF9E847E)       // Caption text

// Dark Mode Neutral Surfaces (Velvety Warm Charcoal / Twilight Obsidian)
val AvaObsidianBgDark = Color(0xFF130E0D)       // Deep atmospheric stage canvas
val AvaSurfaceDark = Color(0xFF1F1715)          // Velvety warm charcoal surface
val AvaSurfaceVariantDark = Color(0xFF2E2220)   // Elevated container
val AvaSurfaceSubtleDark = Color(0xFF3F302D)    // Structural divider/card edge
val AvaTextPrimaryDark = Color(0xFFFFF5F1)      // Luminous warm ivory (>= 12:1 contrast)
val AvaTextSecondaryDark = Color(0xFFD4BFBA)    // Soft peach-cream text (>= 6:1 contrast)
val AvaTextMutedDark = Color(0xFF9E8A85)        // Ambient text

// Brand Subtle Gradients (Used selectively for Hero Stage, Record Button & Badges)
val AvaStageGradient = Brush.horizontalGradient(
    colors = listOf(AvaSunsetCoral, AvaApricot)
)

val AvaGoldenGlowGradient = Brush.linearGradient(
    colors = listOf(AvaGoldenHighlight, AvaApricot)
)

val AvaRecordActiveGradient = Brush.radialGradient(
    colors = listOf(AvaRecordingActive, AvaSunsetCoral)
)

@Immutable
data class AvaCustomColors(
    val brandPrimary: Color,
    val brandSecondary: Color,
    val brandHighlight: Color,
    val brandPink: Color,
    val recordingActive: Color,
    val karaokeActive: Color,
    val scoreBadge: Color,
    val stageSurface: Color,
    val stageSurfaceElevated: Color,
    val stageBorder: Color,
    val textMuted: Color
)

val LocalAvaCustomColors = staticCompositionLocalOf {
    AvaCustomColors(
        brandPrimary = AvaSunsetCoral,
        brandSecondary = AvaApricot,
        brandHighlight = AvaGoldenHighlight,
        brandPink = AvaWarmPink,
        recordingActive = AvaRecordingActive,
        karaokeActive = AvaKaraokeActive,
        scoreBadge = AvaScoreHigh,
        stageSurface = AvaSurfaceLight,
        stageSurfaceElevated = AvaSurfaceVariantLight,
        stageBorder = AvaSurfaceSubtleLight,
        textMuted = AvaTextMutedLight
    )
}
