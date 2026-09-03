package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.AvaIconButton
import com.example.ui.components.AvaLyricsView
import com.example.ui.components.AvaPlaybackBar
import com.example.ui.components.AvaRecordControlPanel
import com.example.ui.components.AvaScoreDialog
import com.example.ui.theme.AvaTheme
import com.example.ui.viewmodel.AvaUiState
import com.tavana.karaoke.domain.model.Song

/**
 * Active Karaoke Stage Screen.
 * Implements real-time synchronized singing, key-shifting, live audio VU meters,
 * and performance score evaluations.
 */
@Composable
fun KaraokeStageScreen(
    uiState: AvaUiState,
    onBackClick: () -> Unit,
    onPlayPauseToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    onPitchShiftChange: (Int) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onVocalGuideToggle: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseResumeRecording: () -> Unit,
    onSaveTake: () -> Unit,
    onSingAgain: () -> Unit,
    onDismissScoreDialog: () -> Unit,
    onToggleRtl: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .testTag("karaoke_stage_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AvaTheme.spacing.medium)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(AvaTheme.spacing.medium)
        ) {
            // Stage Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AvaTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AvaIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Return from Stage",
                        onClick = onBackClick,
                        testTag = "stage_back_button"
                    )
                    Spacer(modifier = Modifier.width(AvaTheme.spacing.small))
                    Column {
                        Text(
                            text = uiState.activeSong.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = uiState.activeSong.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = AvaTheme.colors.brandPrimary
                        )
                    }
                }

                AvaIconButton(
                    icon = Icons.Default.Translate,
                    contentDescription = "Toggle Persian RTL view",
                    onClick = onToggleRtl,
                    testTag = "stage_rtl_toggle"
                )
            }

            // Real-time Lyrics View
            AvaLyricsView(
                lyrics = uiState.activeLyrics,
                currentTimeMs = uiState.currentTimeMs,
                onLineClick = { clickedLine ->
                    onSeek(clickedLine.startMs)
                },
                isRtlGlobally = uiState.isPersianRtlEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )

            // Playback Bar (Scrubbing, Pitch Shift, Tempo, Vocal Guide)
            AvaPlaybackBar(
                isPlaying = uiState.isPlaying,
                currentTimeMs = uiState.currentTimeMs,
                totalDurationMs = uiState.activeSong.durationMs,
                pitchShiftSemitones = uiState.pitchShiftSemitones,
                playbackSpeed = uiState.playbackSpeed,
                isVocalGuideOn = uiState.isVocalGuideOn,
                onPlayPauseToggle = onPlayPauseToggle,
                onSeek = onSeek,
                onPitchShiftChange = onPitchShiftChange,
                onSpeedChange = onSpeedChange,
                onVocalGuideToggle = onVocalGuideToggle
            )

            // Recording Controls Panel (Live VU Meter & Stage Glow Button)
            AvaRecordControlPanel(
                state = uiState.recordingState,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording,
                onPauseResume = onPauseResumeRecording,
                audioLevel = uiState.audioLevel
            )

            Spacer(modifier = Modifier.height(AvaTheme.spacing.large))
        }

        // Performance Score Evaluation Dialog
        if (uiState.activeScoreDialog != null) {
            AvaScoreDialog(
                score = uiState.activeScoreDialog,
                songTitle = uiState.activeSong.title,
                onSaveTake = onSaveTake,
                onSingAgain = onSingAgain,
                onDismiss = onDismissScoreDialog
            )
        }
    }
}
