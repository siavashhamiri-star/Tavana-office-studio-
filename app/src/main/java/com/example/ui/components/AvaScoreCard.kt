package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AvaGoldenHighlight
import com.example.ui.theme.AvaScoreHigh
import com.example.ui.theme.AvaSunsetCoral
import com.example.ui.theme.AvaTheme
import com.tavana.karaoke.domain.model.VocalScore

/**
 * Celebration modal dialog presenting vocal accuracy, score stars,
 * and joyful feedback.
 */
@Composable
fun AvaScoreDialog(
    score: VocalScore,
    songTitle: String,
    onSaveTake: () -> Unit,
    onSingAgain: () -> Unit,
    onDismiss: () -> Unit,
    onPlayRecording: (() -> Unit)? = null,
    isPlayingRecordedTake: Boolean = false
) {
    var animatedScoreProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(score.overall) {
        animatedScoreProgress = score.overall / 100f
    }

    val animatedScore by animateFloatAsState(
        targetValue = animatedScoreProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "scoreAnim"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ava_score_dialog"),
            shape = AvaTheme.shapes.dialogShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = AvaTheme.elevation.modal,
            border = androidx.compose.foundation.BorderStroke(1.dp, AvaTheme.colors.stageBorder)
        ) {
            Column(
                modifier = Modifier.padding(AvaTheme.spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Musical Badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AvaTheme.colors.stageSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = AvaTheme.colors.brandPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

                Text(
                    text = score.badge,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = AvaTheme.colors.brandPrimary
                )

                Text(
                    text = songTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(AvaTheme.spacing.large))

                // Star row (1 to 5)
                val starCount = (score.overall / 20).coerceIn(1, 5)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= starCount) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = AvaTheme.colors.scoreBadge,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

                // Big Score Display
                Text(
                    text = "${(animatedScore * 100).toInt()}%",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Text(
                    text = score.feedback,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = AvaTheme.spacing.small)
                )

                Spacer(modifier = Modifier.height(AvaTheme.spacing.large))

                // Accuracy Breakdown Bars
                AccuracyBar(label = "Pitch Accuracy", percentage = score.pitch)
                Spacer(modifier = Modifier.height(AvaTheme.spacing.small))
                AccuracyBar(label = "Rhythm & Timing", percentage = score.rhythm)
                Spacer(modifier = Modifier.height(AvaTheme.spacing.small))
                AccuracyBar(label = "Vocal Stability", percentage = score.expression)

                Spacer(modifier = Modifier.height(AvaTheme.spacing.large))

                // Actions
                if (onPlayRecording != null) {
                    AvaSecondaryButton(
                        text = if (isPlayingRecordedTake) "Pause Playback" else "Listen to My Take (شنیدن صدای ضبط‌شده)",
                        icon = if (isPlayingRecordedTake) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
                        onClick = onPlayRecording,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "score_listen_take"
                    )
                    Spacer(modifier = Modifier.height(AvaTheme.spacing.small))
                }

                AvaPrimaryButton(
                    text = "Save Recording Take",
                    icon = Icons.Default.Bookmark,
                    onClick = onSaveTake,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "score_save_take"
                )

                Spacer(modifier = Modifier.height(AvaTheme.spacing.small))

                AvaSecondaryButton(
                    text = "Sing Again",
                    icon = Icons.Default.Replay,
                    onClick = onSingAgain,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "score_sing_again"
                )
            }
        }
    }
}

@Composable
private fun AccuracyBar(
    label: String,
    percentage: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = AvaTheme.colors.brandPrimary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = AvaTheme.colors.brandPrimary,
            trackColor = AvaTheme.colors.stageSurfaceElevated
        )
    }
}
