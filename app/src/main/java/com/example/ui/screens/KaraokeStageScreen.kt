package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.AvaIconButton
import com.example.ui.components.AvaLyricsView
import com.example.ui.components.AvaPlaybackBar
import com.example.ui.components.AvaRecordControlPanel
import com.example.ui.components.AvaScoreDialog
import com.example.ui.components.ContextualQuickGuideCard
import com.example.ui.components.StudioStepByStepGuideDialog
import com.example.ui.theme.AvaGoldenHighlight
import com.example.ui.theme.AvaSunsetCoral
import com.example.ui.theme.AvaTheme
import com.example.ui.viewmodel.AvaUiState
import com.tavana.karaoke.domain.model.RecordingTake
import com.tavana.karaoke.domain.model.Song
import com.tavana.studio.audio.sharing.ShareHelper

/**
 * Active Karaoke Stage Screen.
 * Implements real-time synchronized singing, key-shifting, live audio VU meters,
 * native mic permission handling, live ear monitoring, and performance score evaluations.
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
    onToggleVoiceMonitoring: () -> Unit = {},
    onPlayRecordedTake: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showStageGuide by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartRecording()
        }
    }

    val handleRecordClick = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            onStartRecording()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = AvaTheme.colors.brandPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.activeSong.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = AvaTheme.colors.brandPrimary
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AvaIconButton(
                        icon = Icons.Default.HelpOutline,
                        contentDescription = "راهنمای گام‌به‌گام صحنه",
                        onClick = { showStageGuide = true },
                        testTag = "stage_help_button"
                    )
                    AvaIconButton(
                        icon = Icons.Default.Translate,
                        contentDescription = "Toggle Persian RTL view",
                        onClick = onToggleRtl,
                        testTag = "stage_rtl_toggle"
                    )
                }
            }

            // In-Stage Quick Guide
            ContextualQuickGuideCard(
                title = "راهنمای اجرای روی صحنه",
                subtitle = "نکات کلیدی برای اجرای بهتر و ضبط بدون نقص",
                steps = listOf(
                    "متن شعر به صورت همگام با ضرباهنگ تغییر رنگ می‌دهد.",
                    "برای شروع ضبط صدای خود، دکمه قرمز پایین صفحه را فشار دهید.",
                    "هدفون وصل کنید و دکمه «شنیدن زنده صدای خواننده» را بزنید تا صدایتان در گوش پخش شود."
                ),
                onOpenFullGuide = { showStageGuide = true },
                iconTint = AvaSunsetCoral
            )

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

            // Voice Monitoring Earphone Control Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (uiState.isVoiceMonitoringEnabled) AvaTheme.colors.brandPrimary else AvaTheme.colors.stageBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(role = Role.Switch, onClick = onToggleVoiceMonitoring)
                    .semantics {
                        this.role = Role.Switch
                        this.contentDescription = "Toggle live voice ear monitoring"
                    },
                color = if (uiState.isVoiceMonitoringEnabled) AvaTheme.colors.stageSurfaceElevated else AvaTheme.colors.stageSurface,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isVoiceMonitoringEnabled) AvaTheme.colors.brandPrimary
                                    else AvaTheme.colors.stageSurfaceElevated
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Headphones,
                                contentDescription = null,
                                tint = if (uiState.isVoiceMonitoringEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ear Monitoring (شنیدن صدای خود در هدفون)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (uiState.isVoiceMonitoringEnabled) "فعال — صدای زنده میکروفون در هدفون پخش می‌شود" else "غیرفعال — برای جلوگیری از اکو روی اسپیکر خاموش است",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = if (uiState.isVoiceMonitoringEnabled) AvaTheme.colors.brandPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = if (uiState.isVoiceMonitoringEnabled) "ON" else "OFF",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (uiState.isVoiceMonitoringEnabled) AvaTheme.colors.brandPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Recording Controls Panel (Live VU Meter & Stage Glow Button)
            AvaRecordControlPanel(
                state = uiState.recordingState,
                onStartRecording = handleRecordClick,
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
                onDismiss = onDismissScoreDialog,
                onPlayRecording = onPlayRecordedTake,
                isPlayingRecordedTake = uiState.playingRecordingId != null,
                onShareTake = uiState.lastRecordedTake?.let { take ->
                    { ShareHelper.shareRecording(context, take) }
                }
            )
        }
        // Step-by-Step Guide Dialog
        StudioStepByStepGuideDialog(
            isOpen = showStageGuide,
            onDismiss = { showStageGuide = false },
            initialStepIndex = 1
        )
    }
}
