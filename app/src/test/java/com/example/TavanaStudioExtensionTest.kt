package com.example

import com.tavana.karaoke.domain.model.LyricLine
import com.tavana.studio.audio.analyzer.DeterministicScoreResult
import com.tavana.studio.audio.analyzer.PitchDetectionResult
import com.tavana.studio.audio.coach.DefaultVocalCoach
import com.tavana.studio.audio.coach.VocalGrade
import com.tavana.studio.audio.harmony.DefaultHarmonyEngine
import com.tavana.studio.audio.harmony.HarmonyInterval
import com.tavana.studio.audio.lyrics.DefaultSmartLyricsManager
import com.tavana.studio.audio.pitch.MusicalPitchHelper
import com.tavana.studio.audio.processor.AlgorithmicReverbAudioProcessor
import com.tavana.studio.audio.processor.DelayAudioProcessor
import com.tavana.studio.audio.processor.EqPresetCatalog
import com.tavana.studio.audio.processor.ReverbParams
import com.tavana.studio.audio.project.AudioTrack
import com.tavana.studio.audio.project.TrackType
import com.tavana.studio.audio.separation.DefaultStemSeparationEngine
import com.tavana.studio.audio.separation.SeparationMode
import com.tavana.studio.audio.separation.SeparationResult
import com.tavana.studio.audio.workspaces.LiveRadioState
import com.tavana.studio.audio.workspaces.PartyRoomFoundation
import com.tavana.studio.audio.workspaces.SoundboardCatalog
import com.tavana.studio.audio.workspaces.VoiceoverPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TavanaStudioExtensionTest {

    // --- 1. VOCAL COACH & DETERMINISTIC GRADING ---
    @Test
    fun `vocal coach grades deterministically and explains score metrics`() {
        val coach = DefaultVocalCoach()
        val score = DeterministicScoreResult(
            pitchAccuracy = 94,
            timingAccuracy = 91,
            stabilityScore = 95,
            overallScore = 93,
            framesEvaluated = 120,
            feedback = "Great control"
        )
        val history = listOf(
            PitchDetectionResult(440f, 69, "A4", 2f, 0.9f, true),
            PitchDetectionResult(441f, 69, "A4", 4f, 0.88f, true),
            PitchDetectionResult(440f, 69, "A4", 1f, 0.92f, true)
        )

        val report = coach.analyzePerformance(score, history)
        assertEquals(VocalGrade.A, report.overallGrade)
        assertEquals(93, report.overallScore)
        assertEquals(VocalGrade.A, report.pitchAccuracy.grade)
        assertTrue(report.reasonsSummary.isNotEmpty())
        assertTrue(report.coachingRecommendations.isNotEmpty())
        assertTrue(report.isDeterministicallyCalculated)
    }

    // --- 2. SMART LYRICS SYNCHRONIZATION ---
    @Test
    fun `smart lyrics manager synchronizes lines and allows manual timing correction`() {
        val manager = DefaultSmartLyricsManager()
        val lines = listOf(
            LyricLine(1, 0L, 3000L, "First line of verse"),
            LyricLine(2, 3500L, 7000L, "Second line of chorus")
        )
        manager.loadLyrics(lines, "Original Song", copyrightNotice = "All Rights Reserved", isUserProvided = false)

        manager.updatePlaybackPosition(1500L)
        assertEquals(0, manager.state.value.currentLineIndex)
        assertEquals("First line of verse", manager.state.value.currentLine?.text)

        manager.updatePlaybackPosition(5000L)
        assertEquals(1, manager.state.value.currentLineIndex)
        assertEquals("Second line of chorus", manager.state.value.currentLine?.text)

        // Adjust line timing
        manager.adjustLineTiming(1, startOffsetDeltaMs = 200L, endOffsetDeltaMs = 400L)
        val adjustedFirst = manager.state.value.lines.first { it.id == 1 }
        assertEquals(200L, adjustedFirst.startMs)
        assertEquals(3400L, adjustedFirst.endMs)
    }

    // --- 3. KEY DETECTION & TRANSPOSITION ---
    @Test
    fun `musical pitch transposer correctly shifts notes and calculates semitone ratios`() {
        assertEquals("E", MusicalPitchHelper.transposeKey("C", 4))
        assertEquals("G", MusicalPitchHelper.transposeKey("C", 7))
        assertEquals("A", MusicalPitchHelper.transposeKey("C", 9))
        assertEquals("C", MusicalPitchHelper.transposeKey("C", 12))

        val ratioOctave = MusicalPitchHelper.semitoneToRatio(12)
        assertEquals(2.0f, ratioOctave, 0.001f)

        val ratioFifth = MusicalPitchHelper.semitoneToRatio(7)
        assertTrue(ratioFifth in 1.49f..1.50f)
    }

    // --- 4. HARMONY & SELF DUET GENERATOR ---
    @Test
    fun `harmony engine creates independent controllable harmony track and duet pair`() {
        val engine = DefaultHarmonyEngine()
        val leadTrack = AudioTrack(
            id = "trk_lead_1",
            name = "Main Vocal",
            type = TrackType.VOCAL_LEAD,
            volume = 1.0f,
            pan = 0.0f
        )

        val harmonyTrack = engine.generateHarmonyTrack(leadTrack, HarmonyInterval.THIRD_ABOVE, volume = 0.7f, pan = 0.4f)
        assertEquals("trk_harmony_trk_lead_1_third_above", harmonyTrack.id)
        assertEquals(TrackType.HARMONY, harmonyTrack.type)
        assertEquals(0.7f, harmonyTrack.volume, 0.001f)
        assertEquals(0.4f, harmonyTrack.pan, 0.001f)

        val (leadA, duetB) = engine.createSelfDuetPair(leadTrack, "Duet Partner")
        assertEquals(-0.3f, leadA.pan, 0.001f)
        assertEquals(0.3f, duetB.pan, 0.001f)
        assertEquals("trk_duet_b_trk_lead_1", duetB.id)
    }

    // --- 5. AUDIO EFFECTS: REVERB, DELAY & EQ PRESETS ---
    @Test
    fun `reverb and delay processors process audio without digital clipping`() {
        val delayProc = DelayAudioProcessor(delayMs = 100f, feedback = 0.3f, mix = 0.4f)
        val samples = FloatArray(1000) { 0.5f }
        val delayed = delayProc.process(samples, sampleRate = 44100)
        assertEquals(1000, delayed.size)
        assertTrue(delayed.all { it in -1.0f..1.0f })

        val reverbProc = AlgorithmicReverbAudioProcessor(ReverbParams(roomSize = 0.5f, wetDry = 0.3f))
        val reverbed = reverbProc.process(samples, sampleRate = 44100)
        assertEquals(1000, reverbed.size)
        assertTrue(reverbed.all { it in -1.0f..1.0f })

        // Check EQ preset catalog
        assertEquals(8, EqPresetCatalog.ALL_PRESETS.size)
        assertNotNull(EqPresetCatalog.VOCAL_CLEAN)
        assertNotNull(EqPresetCatalog.PODCAST_VOICE)
        assertNotNull(EqPresetCatalog.RAP_VOCAL)
    }

    // --- 6. HONEST STEM SEPARATION STATUS ---
    @Test
    fun `stem separation reports dependency requirements without faking neural inference`() {
        val separationEngine = DefaultStemSeparationEngine()
        assertFalse(separationEngine.isSeparationEngineAvailable)

        val result = separationEngine.requestSeparation("/path/to/song.wav", SeparationMode.VOCAL_ONLY)
        assertTrue(result is SeparationResult.Blocked)
        val blocked = result as SeparationResult.Blocked
        assertTrue(blocked.requiredDependency.contains("ONNX Runtime"))
    }

    // --- 7. WORKSPACE DOMAIN MODELS (PODCAST, RADIO, VOICEOVER, PARTY ROOM) ---
    @Test
    fun `workspace domain models verify voiceover presets soundboard and party room readiness`() {
        assertEquals(6, VoiceoverPreset.entries.size)
        assertTrue(SoundboardCatalog.BUILT_IN_CLIPS.isNotEmpty())

        val radioState = LiveRadioState(isLive = true, microphoneGain = 1.0f, bgMusicVolume = 0.4f)
        assertTrue(radioState.isAutoDuckingActive)

        val partyRoom = PartyRoomFoundation(
            roomId = "room_1",
            hostIdentityId = "user_me",
            roomTitle = "Vocal Jam Session"
        )
        assertEquals("READY FOR BACKEND / REAL-TIME INTEGRATION", partyRoom.readinessStatus)

        // Asset check: verified honestly that assets are missing from app bundle
        assertTrue(SoundboardCatalog.hasMissingAssets())
    }

    // --- 8. REAL DUCKING & DSP PROCESSOR CHAIN VERIFICATION ---
    @Test
    fun `real sidechain ducker attenuates background music when vocal RMS is above threshold`() {
        val ducker = com.tavana.studio.audio.workspaces.RealSidechainDucker(
            thresholdRms = 0.05f,
            duckingStrength = 0.8f
        )

        val silentVoice = FloatArray(500) { 0.0f }
        val loudVoice = FloatArray(500) { 0.5f } // RMS = 0.5 > 0.05
        val bgMusic = FloatArray(500) { 0.5f }

        val voiceRmsSilent = ducker.computeVoiceRms(silentVoice)
        val notDucked = ducker.process(bgMusic, voiceRmsSilent)
        assertEquals(0.5f, notDucked.last(), 0.05f)

        val voiceRmsLoud = ducker.computeVoiceRms(loudVoice)
        assertTrue(voiceRmsLoud >= 0.05f)
        val ducked = ducker.process(bgMusic, voiceRmsLoud)
        // Background should be visibly ducked
        assertTrue(ducked.last() < 0.3f)
    }

    // --- 9. REAL VOICEOVER DSP CHAIN EXECUTION ---
    @Test
    fun `voiceover preset builds valid real DSP chain and processes samples without clipping`() {
        val preset = VoiceoverPreset.COMMERCIAL
        val chain = com.tavana.studio.audio.workspaces.VoiceoverProcessingEngine.buildProcessorChain(preset)
        assertEquals(3, chain.size) // HPF, Peaking EQ, Limiter

        val samples = FloatArray(1000) { 0.4f }
        val processed = com.tavana.studio.audio.workspaces.VoiceoverProcessingEngine.processVoiceoverSamples(samples, preset)
        assertEquals(1000, processed.size)
        assertTrue(processed.all { it in -1.0f..1.0f })
    }

    // --- 10. REAL PODCAST & AUDIOBOOK PROJECT ASSEMBLY ---
    @Test
    fun `podcast episode and audiobook chapter assemble into valid AudioProject structures`() {
        val episode = com.tavana.studio.audio.workspaces.PodcastEpisode(
            id = "ep_01",
            title = "Tech Talk",
            episodeNumber = 1,
            hostTrackId = "trk_host",
            guestTrackId = "trk_guest",
            introMusicTrackId = "trk_intro"
        )
        val project = com.tavana.studio.audio.workspaces.PodcastProjectAssembler.assemblePodcastProject(episode)
        assertEquals("proj_podcast_ep_01", project.id)
        assertEquals(3, project.tracks.size)
        assertEquals("Podcast Host", project.tracks[0].name)
        assertEquals(TrackType.PODCAST_HOST, project.tracks[0].type)

        val chapter = com.tavana.studio.audio.workspaces.AudiobookChapter(
            id = "ch_01",
            chapterNumber = 1,
            chapterTitle = "The Beginning"
        )
        val bookProject = com.tavana.studio.audio.workspaces.AudiobookProjectAssembler.assembleAudiobookChapterProject(chapter)
        assertEquals("proj_audiobook_ch1", bookProject.id)
        assertEquals(1, bookProject.tracks.size)
    }

    // --- 11. REAL RAP FLOW METRIC CALCULATION ---
    @Test
    fun `rap flow analyzer calculates real syllable density and rhyme cadence from verses`() {
        val verses = listOf(
            "I write the rhythm in the dark of night",
            "The flow is steady and the beats are tight"
        )
        val metrics = com.tavana.studio.audio.workspaces.RapFlowAnalyzer.analyzeRapVerses(
            verses = verses,
            durationSeconds = 4.0f,
            bpm = 90
        )
        assertTrue(metrics.syllablesPerSecond > 0f)
        assertTrue(metrics.rhymeDensity > 0f) // night / tight rhyme
        assertEquals(90, metrics.averageBpm)
    }

    // --- 12. VOCAL COACH DETERMINISTIC LETTER GRADING ---
    @Test
    fun `vocal coach scoring engine calculates deterministic letter grade and feedback`() {
        val reportA = com.tavana.studio.audio.workspaces.VocalCoachScoringEngine.computeReport(
            pitchAccuracy = 95,
            noteStability = 92,
            timingAccuracy = 94
        )
        assertEquals(com.tavana.studio.audio.workspaces.VocalCoachGrade.A, reportA.grade)
        assertTrue(reportA.overallScore >= 90)

        val reportD = com.tavana.studio.audio.workspaces.VocalCoachScoringEngine.computeReport(
            pitchAccuracy = 40,
            noteStability = 45,
            timingAccuracy = 35
        )
        assertEquals(com.tavana.studio.audio.workspaces.VocalCoachGrade.D, reportD.grade)
        assertTrue(reportD.overallScore < 55)
    }

    // --- 13. EXTENDED VOCAL PRESET DSP BUILDER ---
    @Test
    fun `extended vocal presets build valid real DSP chains and process samples`() {
        for (preset in com.tavana.studio.audio.workspaces.ExtendedVocalPreset.values()) {
            val chain = com.tavana.studio.audio.workspaces.ExtendedVocalPresetEngine.buildProcessorChain(preset)
            assertTrue(chain.isNotEmpty())
            val samples = FloatArray(500) { 0.2f }
            var current = samples
            for (p in chain) {
                current = p.process(current, 44100)
            }
            assertEquals(500, current.size)
            assertTrue(current.all { it in -1.0f..1.0f })
        }
    }

    // --- 14. VOCAL DOUBLE / SELF DUET ENGINE ---
    @Test
    fun `vocal double engine generates micro-delay stereo thickening without clipping`() {
        val doubleEngine = com.tavana.studio.audio.workspaces.VocalDoubleEngine(
            delayMs = 20.0f,
            doubleVolume = 0.5f
        )
        val samples = FloatArray(2000) { 0.4f }
        val doubled = doubleEngine.generateDouble(samples, 44100)
        assertEquals(2000, doubled.size)
        assertTrue(doubled.all { it in -1.0f..1.0f })
    }

    // --- 15. EXTENDED STUDIO ROOM PRESETS ---
    @Test
    fun `all 12 studio room presets define valid acoustic parameters`() {
        val rooms = com.tavana.studio.audio.workspaces.ExtendedStudioRoom.values()
        assertEquals(12, rooms.size)
        assertTrue(rooms.any { it == com.tavana.studio.audio.workspaces.ExtendedStudioRoom.DRY })
        assertTrue(rooms.any { it == com.tavana.studio.audio.workspaces.ExtendedStudioRoom.CATHEDRAL })
        assertTrue(rooms.all { it.decaySec >= 0f && it.wetDry in 0f..1f && it.damping in 0f..1f })
    }
}

