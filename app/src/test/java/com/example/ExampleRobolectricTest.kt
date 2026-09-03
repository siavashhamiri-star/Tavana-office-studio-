package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.AvaNavDestination
import com.example.ui.components.RecordingState
import com.example.ui.viewmodel.AvaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TAVANA Studio", appName)
  }

  @Test
  fun `ava viewmodel initial state has songs and lyrics`() {
    val viewModel = AvaViewModel()
    val state = viewModel.uiState.value
    assertNotNull(state.activeSong)
    assertTrue(state.activeLyrics.isNotEmpty())
    assertEquals(AvaNavDestination.STAGE, state.currentTab)
    assertFalse(state.isKaraokeScreenActive)
  }

  @Test
  fun `ava viewmodel toggle RTL switches layout state`() {
    val viewModel = AvaViewModel()
    val initialRtl = viewModel.uiState.value.isPersianRtlEnabled
    viewModel.togglePersianRtl()
    assertEquals(!initialRtl, viewModel.uiState.value.isPersianRtlEnabled)
  }

  @Test
  fun `ava viewmodel song launch opens stage and sets active song`() {
    val viewModel = AvaViewModel()
    val songs = viewModel.songs.value
    assertTrue(songs.isNotEmpty())
    val secondSong = songs.getOrNull(1) ?: songs.first()

    viewModel.launchSongOnStage(secondSong)
    val state = viewModel.uiState.value
    assertEquals(secondSong.id, state.activeSong.id)
    assertTrue(state.isKaraokeScreenActive)
    assertTrue(state.isPlaying)
  }

  @Test
  fun `ava viewmodel recording flow generates score`() {
    val viewModel = AvaViewModel()
    viewModel.startRecording()
    assertEquals(RecordingState.RECORDING, viewModel.uiState.value.recordingState)

    viewModel.stopRecordingAndEvaluate()
    assertEquals(RecordingState.IDLE, viewModel.uiState.value.recordingState)
    assertNotNull(viewModel.uiState.value.activeScoreDialog)

    viewModel.saveCompletedTake()
    assertEquals(null, viewModel.uiState.value.activeScoreDialog)
  }
}
