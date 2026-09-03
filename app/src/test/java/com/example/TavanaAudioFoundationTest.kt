package com.example

import com.tavana.karaoke.domain.model.LyricLine
import com.tavana.studio.architecture.Identity
import com.tavana.studio.architecture.Permission
import com.tavana.studio.architecture.Role
import com.tavana.studio.architecture.Workspace
import com.tavana.studio.architecture.WorkspaceAccessPolicy
import com.tavana.studio.architecture.WorkspaceType
import com.tavana.studio.audio.analyzer.AutocorrelationPitchDetector
import com.tavana.studio.audio.analyzer.TargetNote
import com.tavana.studio.audio.analyzer.UserPitchFrame
import com.tavana.studio.audio.engine.InMemoryAudioPlaybackEngine
import com.tavana.studio.audio.engine.InMemoryAudioRecordingEngine
import com.tavana.studio.audio.engine.PlaybackEngineState
import com.tavana.studio.audio.engine.RecordingEngineState
import com.tavana.studio.audio.mixer.DefaultAudioMixer
import com.tavana.studio.audio.processor.GainAudioProcessor
import com.tavana.studio.audio.processor.LimiterAudioProcessor
import com.tavana.studio.audio.project.AudioProject
import com.tavana.studio.audio.project.AudioTake
import com.tavana.studio.audio.project.AudioTrack
import com.tavana.studio.audio.project.InMemoryProjectRepository
import com.tavana.studio.audio.project.ProjectMetadata
import com.tavana.studio.audio.project.TrackType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlinx.coroutines.test.runTest

class TavanaAudioFoundationTest {

    // --- 1. RECORDING STATE TRANSITIONS ---
    @Test
    fun `recording engine lifecycle state transitions`() {
        val engine = InMemoryAudioRecordingEngine()
        assertEquals(RecordingEngineState.IDLE, engine.state.value)

        val file = File("/tmp/test_take.wav")
        val startRes = engine.startRecording(file)
        assertTrue(startRes.isSuccess)
        assertEquals(RecordingEngineState.RECORDING, engine.state.value)

        // Cannot start when already recording
        val doubleStart = engine.startRecording(file)
        assertTrue(doubleStart.isFailure)

        // Pause
        val pauseRes = engine.pauseRecording()
        assertTrue(pauseRes.isSuccess)
        assertEquals(RecordingEngineState.PAUSED, engine.state.value)

        // Resume
        val resumeRes = engine.resumeRecording()
        assertTrue(resumeRes.isSuccess)
        assertEquals(RecordingEngineState.RECORDING, engine.state.value)

        // Stop
        val stopRes = engine.stopRecording()
        assertTrue(stopRes.isSuccess)
        val take = stopRes.getOrNull()
        assertNotNull(take)
        assertEquals(RecordingEngineState.IDLE, engine.state.value)
        assertTrue(take!!.durationMs > 0)
        assertEquals(44100, take.sampleRateHz)
    }

    // --- 2. PLAYBACK STATE TRANSITIONS ---
    @Test
    fun `playback engine lifecycle state transitions and seeking`() {
        val engine = InMemoryAudioPlaybackEngine()
        assertEquals(PlaybackEngineState.IDLE, engine.state.value)

        engine.prepare("file:///mock/song.mp3")
        assertEquals(PlaybackEngineState.PAUSED, engine.state.value)

        engine.play()
        assertEquals(PlaybackEngineState.PLAYING, engine.state.value)

        engine.seekTo(15000L)
        assertEquals(15000L, engine.currentPositionMs.value)

        engine.pause()
        assertEquals(PlaybackEngineState.PAUSED, engine.state.value)
        assertEquals(15000L, engine.currentPositionMs.value)

        engine.stop()
        assertEquals(PlaybackEngineState.STOPPED, engine.state.value)
        assertEquals(0L, engine.currentPositionMs.value)

        engine.replay()
        assertEquals(PlaybackEngineState.PLAYING, engine.state.value)
        assertEquals(0L, engine.currentPositionMs.value)
    }

    // --- 3. PROJECT REPOSITORY & PERSISTENCE ---
    @Test
    fun `project repository saves retrieves and updates audio project`() = runTest {
        val repo = InMemoryProjectRepository()
        val project = AudioProject(
            id = "proj_test_1",
            metadata = ProjectMetadata(
                id = "proj_test_1",
                title = "Acoustic Take 1",
                tempoBpm = 110,
                sampleRateHz = 44100
            ),
            tracks = listOf(
                AudioTrack(id = "trk_1", name = "Vocal Lead", type = TrackType.VOCAL_LEAD, volume = 0.9f)
            )
        )

        repo.saveProject(project)
        val retrieved = repo.getProjectById("proj_test_1")
        assertNotNull(retrieved)
        assertEquals("Acoustic Take 1", retrieved!!.metadata.title)
        assertEquals(1, retrieved.tracks.size)

        // Add track
        val newTrack = AudioTrack(id = "trk_2", name = "Guitar", type = TrackType.INSTRUMENTAL, volume = 0.8f)
        repo.addTrack("proj_test_1", newTrack)
        val withGuitar = repo.getProjectById("proj_test_1")
        assertEquals(2, withGuitar!!.tracks.size)

        // Save take
        val take = AudioTake(
            id = "take_1",
            trackId = "trk_1",
            filePath = "/audio/take_1.wav",
            durationMs = 45000L,
            rmsLevel = 0.45f
        )
        repo.saveTake(take)
        val trackTakes = repo.getTakesForTrack("trk_1")
        assertEquals(1, trackTakes.size)
        assertEquals("take_1", trackTakes.first().id)
    }

    // --- 4. MIXER STATE & PAN LAW ---
    @Test
    fun `audio mixer volume pan mute and solo calculations`() {
        val mixer = DefaultAudioMixer()
        mixer.addChannel("vocal", "Lead Vocal", volume = 1.0f, pan = 0.0f)
        mixer.addChannel("music", "Backing Track", volume = 0.8f, pan = 0.0f)

        // Both audible initially
        val vocalInitial = mixer.calculateStereoGain("vocal")
        val musicInitial = mixer.calculateStereoGain("music")
        assertTrue(vocalInitial.isAudible)
        assertTrue(musicInitial.isAudible)

        // Center pan has equal left and right gain
        assertEquals(vocalInitial.leftGain, vocalInitial.rightGain, 0.001f)

        // Hard left pan (-1.0)
        mixer.setTrackPan("vocal", -1.0f)
        val vocalLeft = mixer.calculateStereoGain("vocal")
        assertTrue(vocalLeft.leftGain > 0.9f)
        assertEquals(0.0f, vocalLeft.rightGain, 0.001f)

        // Mute music
        mixer.setTrackMute("music", true)
        val musicMuted = mixer.calculateStereoGain("music")
        assertFalse(musicMuted.isAudible)
        assertEquals(0f, musicMuted.leftGain, 0.001f)

        // Solo music: even though solo is set, because it is muted it remains inaudible,
        // and non-soloed vocal becomes inaudible!
        mixer.setTrackSolo("music", true)
        val vocalWhenMusicSolo = mixer.calculateStereoGain("vocal")
        assertFalse(vocalWhenMusicSolo.isAudible)

        // Unmute music -> now only soloed music is audible
        mixer.setTrackMute("music", false)
        val musicSoloActive = mixer.calculateStereoGain("music")
        assertTrue(musicSoloActive.isAudible)
    }

    // --- 5. LYRICS SYNCHRONIZATION ---
    @Test
    fun `lyrics synchronization matching by timestamp`() {
        val lines = listOf(
            LyricLine(id = 1, startMs = 1000L, endMs = 4000L, text = "First line"),
            LyricLine(id = 2, startMs = 4500L, endMs = 8000L, text = "Second line"),
            LyricLine(id = 3, startMs = 9000L, endMs = 12000L, text = "Third line")
        )

        fun findActiveLine(timeMs: Long): LyricLine? {
            return lines.find { timeMs in it.startMs..it.endMs }
        }

        assertEquals(null, findActiveLine(500L)) // Before start
        assertEquals(1, findActiveLine(2500L)?.id) // In first line
        assertEquals(null, findActiveLine(4200L)) // Interlude gap
        assertEquals(2, findActiveLine(6000L)?.id) // In second line
        assertEquals(3, findActiveLine(11500L)?.id) // In third line
        assertEquals(null, findActiveLine(15000L)) // After song
    }

    // --- 6. DETERMINISTIC SCORE CALCULATION ---
    @Test
    fun `deterministic scoring produces identical reproducible results without randomness`() {
        val analyzer = AutocorrelationPitchDetector()

        val targets = listOf(
            TargetNote(startMs = 1000L, endMs = 5000L, noteName = "A4", frequencyHz = 440.0f)
        )

        // Pitch frames exactly on target (440Hz)
        val perfectFrames = (1000L..5000L step 200L).map { time ->
            UserPitchFrame(timestampMs = time, frequencyHz = 440.0f, confidence = 0.9f)
        }

        val score1 = analyzer.evaluatePerformance(targets, perfectFrames)
        val score2 = analyzer.evaluatePerformance(targets, perfectFrames)

        // 100% deterministic reproducibility
        assertEquals(score1.overallScore, score2.overallScore)
        assertEquals(score1.pitchAccuracy, score2.pitchAccuracy)
        assertEquals(score1.timingAccuracy, score2.timingAccuracy)
        assertEquals(score1.stabilityScore, score2.stabilityScore)
        assertTrue(score1.isCalculatedDeterministically)
        assertEquals(100, score1.pitchAccuracy)

        // Off-pitch frames (460Hz instead of 440Hz, ~76 cents sharp)
        val sharpFrames = (1000L..5000L step 200L).map { time ->
            UserPitchFrame(timestampMs = time, frequencyHz = 460.0f, confidence = 0.9f)
        }
        val sharpScore = analyzer.evaluatePerformance(targets, sharpFrames)
        assertTrue(sharpScore.pitchAccuracy < score1.pitchAccuracy)

        // Empty frames return 0
        val emptyScore = analyzer.evaluatePerformance(targets, emptyList())
        assertEquals(0, emptyScore.overallScore)
        assertEquals(0, emptyScore.framesEvaluated)
    }

    // --- 7. DSP EFFECTS REAL MATH ---
    @Test
    fun `dsp gain processor calculates linear dB scale accurately`() {
        val gainProcessor = GainAudioProcessor(initialGainDb = -6.0f) // ~0.501 linear
        val samples = floatArrayOf(1.0f, -1.0f, 0.5f, 0.0f)
        val processed = gainProcessor.process(samples)

        assertEquals(0.501f, processed[0], 0.01f)
        assertEquals(-0.501f, processed[1], 0.01f)

        // Limiter prevents clipping beyond threshold
        val limiter = LimiterAudioProcessor(thresholdDb = -1.0f) // ceiling ~0.891
        val hotSamples = floatArrayOf(1.2f, -1.5f, 0.5f)
        val limited = limiter.process(hotSamples)
        assertTrue(limited[0] <= 0.892f)
        assertTrue(limited[1] >= -0.892f)
    }

    // --- 8. WORKSPACE ARCHITECTURE & GOVERNANCE BOUNDARIES ---
    @Test
    fun `governance access is strictly denied to personal space`() {
        val user1 = Identity("user_1", "Alice", "alice@example.com")
        val auditor = Identity("user_auditor", "Auditor Bob", "bob@governance.org")

        val personalSpace = Workspace(
            id = "ws_alice_personal",
            name = "Alice's Private Vault",
            type = WorkspaceType.PERSONAL_SPACE,
            ownerIdentityId = user1.id
        )

        val governanceWs = Workspace(
            id = "ws_gov",
            name = "TAVANA Oversight",
            type = WorkspaceType.TAVANA_GOVERNANCE,
            ownerIdentityId = "gov_admin"
        )

        // Owner has full permission in personal space
        assertTrue(
            WorkspaceAccessPolicy.hasPermission(
                user1.id,
                Role.OWNER,
                personalSpace,
                Permission.ACCESS_PERSONAL_PROJECTS
            )
        )

        // Auditor has permission in Governance workspace
        assertTrue(
            WorkspaceAccessPolicy.hasPermission(
                auditor.id,
                Role.AUDITOR_GOVERNANCE,
                governanceWs,
                Permission.AUDIT_GOVERNANCE
            )
        )

        // CRITICAL DIRECTIVE: Governance access must NEVER automatically provide access to Personal Space
        assertFalse(
            WorkspaceAccessPolicy.hasPermission(
                auditor.id,
                Role.AUDITOR_GOVERNANCE,
                personalSpace,
                Permission.ACCESS_PERSONAL_PROJECTS
            )
        )
        assertFalse(
            WorkspaceAccessPolicy.hasPermission(
                auditor.id,
                Role.AUDITOR_GOVERNANCE,
                personalSpace,
                Permission.VIEW_WORKSPACE
            )
        )
    }
}
