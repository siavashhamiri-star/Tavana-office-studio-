package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AvaApricot
import com.example.ui.theme.AvaGoldenHighlight
import com.example.ui.theme.AvaPeach
import com.example.ui.theme.AvaSunsetCoral
import com.example.ui.theme.AvaTheme
import com.tavana.karaoke.domain.model.PracticeExercise
import com.tavana.karaoke.domain.model.RecordingTake
import com.tavana.karaoke.domain.model.Song

/**
 * Reusable base card with AVA warm styling and subtle border.
 */
@Composable
fun AvaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = AvaTheme.colors.stageSurface,
    borderColor: Color = AvaTheme.colors.stageBorder,
    testTag: String = "ava_card",
    content: @Composable () -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .minimumInteractiveComponentSize()
            .clip(AvaTheme.shapes.cardShape)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(testTag)
    } else {
        modifier.testTag(testTag)
    }

    Surface(
        modifier = cardModifier
            .border(1.dp, borderColor, AvaTheme.shapes.cardShape),
        shape = AvaTheme.shapes.cardShape,
        color = containerColor,
        tonalElevation = AvaTheme.elevation.card
    ) {
        Box(modifier = Modifier.padding(AvaTheme.spacing.medium)) {
            content()
        }
    }
}

/**
 * Song card displaying title, artist, duration, musical tag, and instant Sing action.
 * Consumes [Song] from com.tavana.karaoke.domain.model.Song.
 */
@Composable
fun AvaSongCard(
    song: Song,
    onSingClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String = "Karaoke Ready",
    testTag: String = "ava_song_card_${song.id}"
) {
    val durationMinSec = formatDuration(song.durationMs)

    AvaCard(
        onClick = onSingClick,
        modifier = modifier.fillMaxWidth(),
        testTag = testTag
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
                // Musical icon badge with warm sunset styling
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(AvaTheme.shapes.cardShapeSmall)
                        .background(AvaTheme.colors.stageSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = AvaTheme.colors.brandPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(AvaTheme.spacing.medium))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AvaTheme.spacing.small)
                    ) {
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = AvaTheme.colors.textMuted
                        )
                        Text(
                            text = durationMinSec,
                            style = MaterialTheme.typography.bodySmall,
                            color = AvaTheme.colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(AvaTheme.spacing.small))

            // Primary Sing action pill
            Surface(
                shape = AvaTheme.shapes.buttonShape,
                color = AvaTheme.colors.brandPrimary,
                modifier = Modifier
                    .clip(AvaTheme.shapes.buttonShape)
                    .clickable(onClick = onSingClick)
                    .semantics {
                        this.role = Role.Button
                        this.contentDescription = "Sing ${song.title}"
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sing",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Recording Take Card showing saved audio performance, score badge, and playback trigger.
 */
@Composable
fun AvaRecordingCard(
    recording: RecordingTake,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "ava_recording_card_${recording.id}"
) {
    AvaCard(
        onClick = onDetailsClick,
        modifier = modifier.fillMaxWidth(),
        testTag = testTag
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
                // Play / Pause audio trigger
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPlaying) AvaTheme.colors.brandPrimary
                            else AvaTheme.colors.stageSurfaceElevated
                        )
                        .clickable(onClick = onPlayToggle)
                        .semantics {
                            this.role = Role.Button
                            this.contentDescription = if (isPlaying) "Pause take" else "Play take"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = if (isPlaying) Color.White else AvaTheme.colors.brandPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(AvaTheme.spacing.medium))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recording.songTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${recording.artist} • ${formatDuration(recording.durationMs)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Score Badge (Celebration Gold / Coral)
            Box(
                modifier = Modifier
                    .clip(AvaTheme.shapes.chipShape)
                    .background(AvaTheme.colors.stageSurfaceElevated)
                    .border(1.dp, AvaTheme.colors.brandHighlight.copy(alpha = 0.5f), AvaTheme.shapes.chipShape)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AvaTheme.colors.scoreBadge,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${recording.overallScore}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

/**
 * Vocal Practice Exercise Card with target note badge and drill launcher.
 */
@Composable
fun AvaPracticeCard(
    exercise: PracticeExercise,
    onStartDrill: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "ava_practice_card_${exercise.id}"
) {
    AvaCard(
        onClick = onStartDrill,
        modifier = modifier.fillMaxWidth(),
        testTag = testTag
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
                        .size(48.dp)
                        .clip(AvaTheme.shapes.cardShapeSmall)
                        .background(AvaTheme.colors.stageSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = exercise.targetNote,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = AvaTheme.colors.brandPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.width(AvaTheme.spacing.medium))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${exercise.category} • ${exercise.durationSeconds}s drill",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AvaSecondaryButton(
                text = "Warm Up",
                onClick = onStartDrill,
                modifier = Modifier.height(40.dp)
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
