package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AvaApricot
import com.example.ui.theme.AvaApricotLight
import com.example.ui.theme.AvaGoldenHighlight
import com.example.ui.theme.AvaSunsetCoral
import com.example.ui.theme.AvaSunsetCoralDark
import com.example.ui.theme.AvaTheme

/**
 * High-contrast primary button for AVA brand actions.
 * Respects 48dp touch target, tactile feedback, and accessible semantics.
 */
@Composable
fun AvaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "ava_primary_button"
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .defaultMinSize(minHeight = 52.dp)
            .testTag(testTag),
        enabled = enabled,
        shape = AvaTheme.shapes.buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AvaTheme.colors.brandPrimary,
            contentColor = Color.White,
            disabledContainerColor = AvaTheme.colors.stageBorder,
            disabledContentColor = AvaTheme.colors.textMuted
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = AvaTheme.elevation.card,
            pressedElevation = AvaTheme.elevation.elevated
        ),
        contentPadding = PaddingValues(horizontal = AvaTheme.spacing.large, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(AvaTheme.spacing.small))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

/**
 * Secondary button with warm apricot/peach styling.
 */
@Composable
fun AvaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "ava_secondary_button"
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTag),
        enabled = enabled,
        shape = AvaTheme.shapes.buttonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AvaTheme.colors.brandPrimary
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(
                listOf(AvaSunsetCoral, AvaApricot)
            ),
            width = 1.5.dp
        ),
        contentPadding = PaddingValues(horizontal = AvaTheme.spacing.medium, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = AvaTheme.colors.brandPrimary
                )
                Spacer(modifier = Modifier.width(AvaTheme.spacing.small))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = AvaTheme.colors.brandPrimary
            )
        }
    }
}

/**
 * Hero "Start Singing" Stage button with warm ambient pulse.
 * The centerpiece CTA communicating: "This is a place where I want to sing."
 */
@Composable
fun AvaStageHeroButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Start Singing",
    subLabel: String = "Take the stage with live lyrics & score",
    testTag: String = "ava_stage_hero_button"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag(testTag),
        shape = AvaTheme.shapes.cardShape,
        color = Color.Transparent,
        tonalElevation = AvaTheme.elevation.elevated
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(AvaSunsetCoral, AvaSunsetCoralDark)
                    ),
                    shape = AvaTheme.shapes.cardShape
                )
                .padding(AvaTheme.spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape)
                            .padding(4.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = AvaSunsetCoral,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(AvaTheme.spacing.medium))
                    androidx.compose.foundation.layout.Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = subLabel,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Go to stage",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Accessible Icon Button ensuring 48dp target with Material ripple.
 */
@Composable
fun AvaIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = AvaTheme.colors.brandPrimary,
    containerColor: Color = Color.Transparent,
    testTag: String = "ava_icon_button"
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(48.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = tint),
                role = Role.Button,
                onClick = onClick
            )
            .semantics {
                this.role = Role.Button
                this.contentDescription = contentDescription
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}
