package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AvaBottomBar
import com.example.ui.components.AvaNavDestination
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KaraokeStageScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.RecordingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.AvaTheme
import com.example.ui.viewmodel.AvaViewModel

@Composable
fun AvaApp(
    viewModel: AvaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val recordings by viewModel.recordings.collectAsState()
    val exercises by viewModel.practiceExercises.collectAsState()

    val layoutDirection = if (uiState.isPersianRtlEnabled) LayoutDirection.Rtl else LayoutDirection.Ltr

    AvaTheme {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            if (uiState.isKaraokeScreenActive) {
                // Immersive Karaoke Singing Stage
                KaraokeStageScreen(
                    uiState = uiState,
                    onBackClick = { viewModel.closeKaraokeStage() },
                    onPlayPauseToggle = { viewModel.togglePlayPause() },
                    onSeek = { viewModel.seekTo(it) },
                    onPitchShiftChange = { viewModel.setPitchShift(it) },
                    onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                    onVocalGuideToggle = { viewModel.toggleVocalGuide() },
                    onStartRecording = { viewModel.startRecording() },
                    onStopRecording = { viewModel.stopRecordingAndEvaluate() },
                    onPauseResumeRecording = { viewModel.pauseResumeRecording() },
                    onSaveTake = { viewModel.saveCompletedTake() },
                    onSingAgain = {
                        viewModel.dismissScoreDialog()
                        viewModel.seekTo(0L)
                        viewModel.startRecording()
                    },
                    onDismissScoreDialog = { viewModel.dismissScoreDialog() },
                    onToggleRtl = { viewModel.togglePersianRtl() }
                )
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        AvaBottomBar(
                            currentDestination = uiState.currentTab,
                            onNavigateTo = { viewModel.selectTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = uiState.currentTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tabTransition"
                        ) { targetTab ->
                            when (targetTab) {
                                AvaNavDestination.STAGE -> {
                                    HomeScreen(
                                        songs = songs,
                                        recordings = recordings,
                                        exercises = exercises,
                                        onStartSingingHero = {
                                            viewModel.launchSongOnStage(songs.first())
                                        },
                                        onSongSelected = { song ->
                                            viewModel.launchSongOnStage(song)
                                        },
                                        onStartPractice = { exercise ->
                                            viewModel.startPracticeDrill(exercise)
                                        },
                                        onViewAllRecordings = {
                                            viewModel.selectTab(AvaNavDestination.RECORDINGS)
                                        },
                                        onToggleRtl = { viewModel.togglePersianRtl() },
                                        isRtlActive = uiState.isPersianRtlEnabled
                                    )
                                }
                                AvaNavDestination.PRACTICE -> {
                                    PracticeScreen(
                                        exercises = exercises,
                                        activeExercise = uiState.activePracticeExercise,
                                        onSelectExercise = { viewModel.startPracticeDrill(it) },
                                        onDismissActiveDrill = { viewModel.dismissPracticeDrill() }
                                    )
                                }
                                AvaNavDestination.RECORDINGS -> {
                                    RecordingsScreen(
                                        recordings = recordings,
                                        onSingSong = { song -> viewModel.launchSongOnStage(song) },
                                        onNavigateToStage = { viewModel.selectTab(AvaNavDestination.STAGE) }
                                    )
                                }
                                AvaNavDestination.STUDIO -> {
                                    StudioScreen(
                                        uiState = uiState,
                                        onTogglePersianRtl = { viewModel.togglePersianRtl() },
                                        onSwitchWorkspace = { viewModel.switchWorkspace(it) },
                                        onSetTrackVolume = { trackId, vol -> viewModel.setTrackVolume(trackId, vol) },
                                        onSetTrackPan = { trackId, pan -> viewModel.setTrackPan(trackId, pan) },
                                        onToggleTrackMute = { viewModel.toggleTrackMute(it) },
                                        onToggleTrackSolo = { viewModel.toggleTrackSolo(it) },
                                        onSetMasterVolume = { viewModel.setMasterVolume(it) },
                                        onExportProject = { viewModel.exportCurrentProject(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
