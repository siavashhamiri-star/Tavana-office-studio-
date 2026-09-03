package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

val AvaMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Immutable
data class AvaCustomShapes(
    val buttonShape: Shape = RoundedCornerShape(50),
    val cardShape: Shape = RoundedCornerShape(22.dp),
    val cardShapeSmall: Shape = RoundedCornerShape(16.dp),
    val chipShape: Shape = RoundedCornerShape(50),
    val lyricBubbleShape: Shape = RoundedCornerShape(18.dp),
    val bottomBarShape: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    val dialogShape: Shape = RoundedCornerShape(28.dp),
    val circleShape: Shape = CircleShape
)

val LocalAvaShapes = staticCompositionLocalOf { AvaCustomShapes() }
