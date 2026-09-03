package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AvaTheme

/**
 * Full musical playback control bar for karaoke backing tracks.
 */
@Composable
fun AvaPlaybackBar(
    isPlaying: Boolean,
    currentTimeMs: Long,
    totalDurationMs: Long,
    pitchShiftSemitones: Int,
    playbackSpeed: Float,
    isVocalGuideOn: Boolean,
    onPlayPauseToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    onPitchShiftChange: (Int) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onVocalGuideToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalDurationMs > 0) {
        (currentTimeMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AvaTheme.shapes.cardShape,
        color = AvaTheme.colors.stageSurface,
        tonalElevation = AvaTheme.elevation.elevated
    ) {
        Column(
            modifier = Modifier.padding(AvaTheme.spacing.medium)
        ) {
            // Scrub Slider
            Slider(
                value = progress,
                onValueChange = { newFrac ->
                    onSeek((newFrac * totalDurationMs).toLong())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("playback_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = AvaTheme.colors.brandPrimary,
                    activeTrackColor = AvaTheme.colors.brandPrimary,
                    inactiveTrackColor = AvaTheme.colors.stageSurfaceElevated
                )
            )

            // Timestamps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(currentTimeMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = AvaTheme.colors.textMuted
                )
                Text(
                    text = formatMs(totalDurationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = AvaTheme.colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(AvaTheme.spacing.small))

            // Control Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Replay 10s
                AvaIconButton(
                    icon = Icons.Default.Replay10,
                    contentDescription = "Replay 10 seconds",
                    onClick = { onSeek((currentTimeMs - 10_000L).coerceAtLeast(0L)) },
                    testTag = "playback_rewind_10"
                )

                // Master Play/Pause
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AvaTheme.colors.brandPrimary)
                        .clickable(
                            role = Role.Button,
                            onClick = onPlayPauseToggle
                        )
                        .semantics {
                            this.role = Role.Button
                            this.contentDescription = if (isPlaying) "Pause Track" else "Play Track"
                        }
                        .testTag("playback_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Forward 10s
                AvaIconButton(
                    icon = Icons.Default.Forward10,
                    contentDescription = "Forward 10 seconds",
                    onClick = { onSeek((currentTimeMs + 10_000L).coerceAtMost(totalDurationMs)) },
                    testTag = "playback_forward_10"
                )
            }

            Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

            // Pitch & Vocal Track Adjustment Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pitch key shift control
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(AvaTheme.shapes.chipShape)
                        .background(AvaTheme.colors.stageSurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AvaTheme.colors.brandPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Key: ${if (pitchShiftSemitones >= 0) "+$pitchShiftSemitones" else "$pitchShiftSemitones"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "▼",
                        modifier = Modifier
                            .clickable { onPitchShiftChange((pitchShiftSemitones - 1).coerceAtLeast(-4)) }
                            .padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = AvaTheme.colors.brandPrimary
                    )
                    Text(
                        text = "▲",
                        modifier = Modifier
                            .clickable { onPitchShiftChange((pitchShiftSemitones + 1).coerceAtMost(4)) }
                            .padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = AvaTheme.colors.brandPrimary
                    )
                }

                // Vocal Guide Toggle
                Box(
                    modifier = Modifier
                        .clip(AvaTheme.shapes.chipShape)
                        .background(
                            if (isVocalGuideOn) AvaTheme.colors.brandPrimary.copy(alpha = 0.15f)
                            else AvaTheme.colors.stageSurfaceElevated
                        )
                        .clickable(onClick = onVocalGuideToggle)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .semantics {
                            this.role = Role.Button
                            this.contentDescription = "Toggle vocal guide"
                        }
                ) {
                    Text(
                        text = if (isVocalGuideOn) "Guide: ON" else "Guide: OFF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isVocalGuideOn) AvaTheme.colors.brandPrimary else AvaTheme.colors.textMuted
                        )
                    )
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
