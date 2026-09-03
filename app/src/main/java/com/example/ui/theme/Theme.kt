package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// ==============================================
// AVA DESIGN SYSTEM THEME
// Warm, Luminous, Joyful Musical Identity
// ==============================================

private val AvaLightColorScheme = lightColorScheme(
    primary = AvaSunsetCoral,
    onPrimary = Color.White,
    primaryContainer = AvaSurfaceVariantLight,
    onPrimaryContainer = AvaSunsetCoralDark,

    secondary = AvaApricot,
    onSecondary = Color(0xFF381A10),
    secondaryContainer = Color(0xFFFFE8DC),
    onSecondaryContainer = Color(0xFF5A2A19),

    tertiary = AvaGoldenHighlight,
    onTertiary = Color(0xFF382600),
    tertiaryContainer = Color(0xFFFFF0CB),
    onTertiaryContainer = Color(0xFF523B00),

    background = AvaPorcelainBgLight,
    onBackground = AvaTextPrimaryLight,

    surface = AvaSurfaceLight,
    onSurface = AvaTextPrimaryLight,
    surfaceVariant = AvaSurfaceVariantLight,
    onSurfaceVariant = AvaTextSecondaryLight,

    outline = AvaSurfaceSubtleLight,
    outlineVariant = Color(0xFFECD5CD),

    error = AvaError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val AvaDarkColorScheme = darkColorScheme(
    primary = AvaSunsetCoralLight,
    onPrimary = Color(0xFF470C05),
    primaryContainer = AvaSurfaceVariantDark,
    onPrimaryContainer = AvaApricotLight,

    secondary = AvaApricot,
    onSecondary = Color(0xFF3D1A0D),
    secondaryContainer = Color(0xFF36211C),
    onSecondaryContainer = AvaPeach,

    tertiary = AvaGoldenHighlight,
    onTertiary = Color(0xFF3B2800),
    tertiaryContainer = Color(0xFF3E3117),
    onTertiaryContainer = Color(0xFFFFE299),

    background = AvaObsidianBgDark,
    onBackground = AvaTextPrimaryDark,

    surface = AvaSurfaceDark,
    onSurface = AvaTextPrimaryDark,
    surfaceVariant = AvaSurfaceVariantDark,
    onSurfaceVariant = AvaTextSecondaryDark,

    outline = AvaSurfaceSubtleDark,
    outlineVariant = Color(0xFF54423E),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

object AvaTheme {
    val colors: AvaCustomColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAvaCustomColors.current

    val spacing: AvaSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalAvaSpacing.current

    val elevation: AvaElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalAvaElevation.current

    val shapes: AvaCustomShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAvaShapes.current

    val typography: androidx.compose.material3.Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography
}

@Composable
fun AvaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AvaDarkColorScheme else AvaLightColorScheme

    val customColors = if (darkTheme) {
        AvaCustomColors(
            brandPrimary = AvaSunsetCoralLight,
            brandSecondary = AvaApricot,
            brandHighlight = AvaGoldenHighlight,
            brandPink = AvaWarmPink,
            recordingActive = AvaRecordingActive,
            karaokeActive = AvaKaraokeActive,
            scoreBadge = AvaScoreHigh,
            stageSurface = AvaSurfaceDark,
            stageSurfaceElevated = AvaSurfaceVariantDark,
            stageBorder = AvaSurfaceSubtleDark,
            textMuted = AvaTextMutedDark
        )
    } else {
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

    val customShapes = AvaCustomShapes()
    val customSpacing = AvaSpacing()
    val customElevation = AvaElevation()

    CompositionLocalProvider(
        LocalAvaCustomColors provides customColors,
        LocalAvaShapes provides customShapes,
        LocalAvaSpacing provides customSpacing,
        LocalAvaElevation provides customElevation
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AvaTypography,
            shapes = AvaMaterialShapes,
            content = content
        )
    }
}

// Backwards-compatible alias for existing tests and previews
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Dynamic color is intentionally bypassed to preserve AVA's brand identity
    AvaTheme(darkTheme = darkTheme, content = content)
}
