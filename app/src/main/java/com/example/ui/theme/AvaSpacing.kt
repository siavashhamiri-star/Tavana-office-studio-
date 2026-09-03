package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AvaSpacing(
    val none: Dp = 0.dp,
    val micro: Dp = 2.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val huge: Dp = 48.dp,
    val stageHero: Dp = 64.dp
)

val LocalAvaSpacing = staticCompositionLocalOf { AvaSpacing() }

@Immutable
data class AvaElevation(
    val flat: Dp = 0.dp,
    val card: Dp = 2.dp,
    val elevated: Dp = 6.dp,
    val modal: Dp = 12.dp
)

val LocalAvaElevation = staticCompositionLocalOf { AvaElevation() }
