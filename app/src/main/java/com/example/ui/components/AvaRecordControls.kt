package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AvaGoldenHighlight
import com.example.ui.theme.AvaRecordingActive
import com.example.ui.theme.AvaSunsetCoral
import com.example.ui.theme.AvaTheme
import kotlin.math.sin

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED
}

/**
 * Recording controls featuring pulsing glow, clear state badges,
 * animated waveform VU meter, and accessible 48dp+ buttons.
 */
@Composable
fun AvaRecordControlPanel(
    state: RecordingState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseResume: () -> Unit,
    audioLevel: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    testTag: String = "ava_record_panel"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recordingPulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = AvaTheme.shapes.cardShape,
        color = AvaTheme.colors.stageSurface,
        tonalElevation = AvaTheme.elevation.card
    ) {
        Column(
            modifier = Modifier.padding(AvaTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Status Indicator Chip (Text + Icon, not color alone)
            Box(
                modifier = Modifier
                    .clip(AvaTheme.shapes.chipShape)
                    .background(
                        when (state) {
                            RecordingState.RECORDING -> AvaTheme.colors.recordingActive.copy(alpha = 0.15f)
                            RecordingState.PAUSED -> AvaTheme.colors.brandHighlight.copy(alpha = 0.2f)
                            RecordingState.IDLE -> AvaTheme.colors.stageSurfaceElevated
                        }
                    )
                    .border(
                        1.dp,
                        when (state) {
                            RecordingState.RECORDING -> AvaTheme.colors.recordingActive
                            RecordingState.PAUSED -> AvaTheme.colors.brandHighlight
                            RecordingState.IDLE -> AvaTheme.colors.stageBorder
                        },
                        AvaTheme.shapes.chipShape
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (state) {
                                    RecordingState.RECORDING -> AvaTheme.colors.recordingActive
                                    RecordingState.PAUSED -> AvaTheme.colors.brandHighlight
                                    RecordingState.IDLE -> AvaTheme.colors.textMuted
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (state) {
                            RecordingState.RECORDING -> "LIVE VOCAL RECORDING"
                            RecordingState.PAUSED -> "RECORDING PAUSED"
                            RecordingState.IDLE -> "STAGE MIC READY"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

            // Animated Vocal Waveform / Pitch VU meter
            AvaWaveformVisualizer(
                isLive = state == RecordingState.RECORDING,
                audioLevel = audioLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            )

            Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

            // Center Recording Button with Stage Glow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AvaTheme.spacing.large)
            ) {
                if (state == RecordingState.RECORDING || state == RecordingState.PAUSED) {
                    // Stop & Save button
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AvaTheme.colors.stageSurfaceElevated)
                            .clickable(role = Role.Button, onClick = onStopRecording)
                            .semantics {
                                this.role = Role.Button
                                this.contentDescription = "Stop Recording and finish take"
                            }
                            .testTag("record_btn_stop"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            tint = AvaTheme.colors.recordingActive,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Main Stage Mic Record Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp)
                ) {
                    if (state == RecordingState.RECORDING) {
                        // Pulsing halo glow
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .scale(pulseGlow)
                                .background(
                                    AvaTheme.colors.recordingActive.copy(alpha = 0.25f),
                                    CircleShape
                                )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                if (state == RecordingState.RECORDING) AvaTheme.colors.recordingActive
                                else AvaTheme.colors.brandPrimary
                            )
                            .clickable(
                                role = Role.Button,
                                onClick = {
                                    when (state) {
                                        RecordingState.IDLE -> onStartRecording()
                                        RecordingState.RECORDING -> onPauseResume()
                                        RecordingState.PAUSED -> onPauseResume()
                                    }
                                }
                            )
                            .semantics {
                                this.role = Role.Button
                                this.contentDescription = when (state) {
                                    RecordingState.IDLE -> "Start Recording Vocal Take"
                                    RecordingState.RECORDING -> "Pause Vocal Recording"
                                    RecordingState.PAUSED -> "Resume Vocal Recording"
                                }
                            }
                            .testTag("record_btn_main"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state == RecordingState.RECORDING) Icons.Default.FiberManualRecord else Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Real-time dynamic visualizer rendering vocal waveform bars.
 */
@Composable
fun AvaWaveformVisualizer(
    isLive: Boolean,
    audioLevel: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 24
) {
    val barColor = if (isLive) AvaSunsetCoral else AvaTheme.colors.textMuted.copy(alpha = 0.35f)
    val activeHighlight = AvaGoldenHighlight

    val infiniteTransition = rememberInfiniteTransition(label = "waveAnim")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val maxHeight = size.height
        val barWidth = (totalWidth / (barCount * 1.6f)).coerceAtLeast(3f)
        val gap = (totalWidth - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)

        for (i in 0 until barCount) {
            val normalizedI = i.toFloat() / barCount.toFloat()
            val sineVal = (sin((normalizedI * 4f + waveOffset).toDouble()).toFloat() + 1f) / 2f
            val baseMultiplier = if (isLive) (0.25f + 0.75f * (sineVal * audioLevel.coerceIn(0.1f, 1f))) else 0.12f
            val barH = (maxHeight * baseMultiplier).coerceIn(4f, maxHeight)

            val x = i * (barWidth + gap)
            val y = (maxHeight - barH) / 2f

            drawRoundRect(
                color = if (isLive && i % 4 == 0) activeHighlight else barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
