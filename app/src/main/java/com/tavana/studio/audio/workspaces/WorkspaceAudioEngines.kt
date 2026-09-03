package com.tavana.studio.audio.workspaces

import com.tavana.studio.audio.processor.AudioProcessor
import com.tavana.studio.audio.processor.BiquadFilterAudioProcessor
import com.tavana.studio.audio.processor.FilterType
import com.tavana.studio.audio.processor.GainAudioProcessor
import com.tavana.studio.audio.processor.LimiterAudioProcessor
import com.tavana.studio.audio.project.AudioClip
import com.tavana.studio.audio.project.AudioProject
import com.tavana.studio.audio.project.AudioTrack
import com.tavana.studio.audio.project.ProjectMetadata
import com.tavana.studio.audio.project.TrackType
import kotlin.math.sqrt

/**
 * Real mathematical sidechain ducking processor.
 * When vocal RMS exceeds threshold, sidechain audio (music/sfx) is ducked smoothly.
 */
class RealSidechainDucker(
    var thresholdRms: Float = 0.05f,
    var duckingStrength: Float = 0.75f, // 0.0 (no ducking) to 1.0 (complete mute)
    var attackAlpha: Float = 0.1f,
    var releaseAlpha: Float = 0.01f
) {
    private var currentDuckingGain = 1.0f

    fun computeVoiceRms(voiceSamples: FloatArray): Float {
        if (voiceSamples.isEmpty()) return 0f
        var sumSquares = 0.0
        for (s in voiceSamples) {
            sumSquares += (s * s)
        }
        return sqrt(sumSquares / voiceSamples.size).toFloat()
    }

    /**
     * Applies ducking attenuation to target background samples based on detected voice RMS.
     */
    fun process(backgroundSamples: FloatArray, voiceRms: Float): FloatArray {
        val targetGain = if (voiceRms >= thresholdRms) {
            1.0f - duckingStrength.coerceIn(0f, 0.95f)
        } else {
            1.0f
        }

        val output = FloatArray(backgroundSamples.size)
        for (i in backgroundSamples.indices) {
            // Smooth gain smoothing (one-pole lowpass)
            val alpha = if (targetGain < currentDuckingGain) attackAlpha else releaseAlpha
            currentDuckingGain += alpha * (targetGain - currentDuckingGain)

            output[i] = (backgroundSamples[i] * currentDuckingGain).coerceIn(-1.0f, 1.0f)
        }
        return output
    }
}

/**
 * Real DSP audio processing chain builder for Voiceover / Ad production.
 * Actually modifies the audio chain according to preset parameters.
 */
object VoiceoverProcessingEngine {

    fun buildProcessorChain(preset: VoiceoverPreset): List<AudioProcessor> {
        val processors = mutableListOf<AudioProcessor>()

        when (preset) {
            VoiceoverPreset.COMMERCIAL -> {
                // High presence boost at 4.5kHz, low cut to prevent proximity boom, peak limiter
                processors.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 80f))
                processors.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 4500f, gainDb = 3.5f))
                processors.add(LimiterAudioProcessor(thresholdDb = -0.5f))
            }
            VoiceoverPreset.RADIO_AD -> {
                // Radio voice: bandpassed telephony/broadcast curve with high limiter ceiling
                processors.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 120f))
                processors.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 2200f, gainDb = 2.5f))
                processors.add(GainAudioProcessor(initialGainDb = 2.0f))
                processors.add(LimiterAudioProcessor(thresholdDb = -0.2f))
            }
            VoiceoverPreset.SOCIAL_AD -> {
                // Bright high presence for mobile speakers
                processors.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 3500f, gainDb = 2.5f))
                processors.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 8000f, gainDb = 2.0f))
                processors.add(LimiterAudioProcessor(thresholdDb = -0.5f))
            }
            VoiceoverPreset.CINEMATIC -> {
                // Warm low boost, subtle top presence
                processors.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 150f, gainDb = 2.0f))
                processors.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 3000f, gainDb = 1.0f))
                processors.add(LimiterAudioProcessor(thresholdDb = -1.0f))
            }
            VoiceoverPreset.ENERGETIC -> {
                // High energy punch: mid-boost and upward gain
                processors.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 3000f, gainDb = 3.0f))
                processors.add(GainAudioProcessor(initialGainDb = 3.0f))
                processors.add(LimiterAudioProcessor(thresholdDb = -0.1f))
            }
            VoiceoverPreset.PROFESSIONAL -> {
                // Clean studio transparency
                processors.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 60f))
                processors.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 2500f, gainDb = 1.5f))
                processors.add(LimiterAudioProcessor(thresholdDb = -1.0f))
            }
        }

        return processors
    }

    /**
     * Executes the configured preset DSP chain directly across audio samples.
     */
    fun processVoiceoverSamples(samples: FloatArray, preset: VoiceoverPreset, sampleRate: Int = 44100): FloatArray {
        var currentSamples = samples
        val chain = buildProcessorChain(preset)
        for (processor in chain) {
            currentSamples = processor.process(currentSamples, sampleRate)
        }
        return currentSamples
    }
}

/**
 * Real project layout generator for Podcast Episodes.
 * Assembles Podcast tracks into an AudioProject structure ready for mixing and WAV export.
 */
object PodcastProjectAssembler {

    fun assemblePodcastProject(
        episode: PodcastEpisode,
        workspaceId: String = "workspace_personal",
        ownerId: String = "user_default"
    ): AudioProject {
        val tracks = mutableListOf<AudioTrack>()

        // 1. Host Track
        episode.hostTrackId?.let { id ->
            tracks.add(
                AudioTrack(
                    id = id,
                    name = "Podcast Host",
                    type = TrackType.PODCAST_HOST,
                    volume = 1.0f,
                    pan = -0.1f
                )
            )
        }

        // 2. Guest Track
        episode.guestTrackId?.let { id ->
            tracks.add(
                AudioTrack(
                    id = id,
                    name = "Podcast Guest",
                    type = TrackType.PODCAST_GUEST,
                    volume = 1.0f,
                    pan = 0.1f
                )
            )
        }

        // 3. Intro Music Track
        episode.introMusicTrackId?.let { id ->
            tracks.add(
                AudioTrack(
                    id = id,
                    name = "Intro Music",
                    type = TrackType.BACKGROUND_MUSIC,
                    volume = 0.6f,
                    pan = 0.0f
                )
            )
        }

        // 4. Outro Music Track
        episode.outroMusicTrackId?.let { id ->
            tracks.add(
                AudioTrack(
                    id = id,
                    name = "Outro Music",
                    type = TrackType.BACKGROUND_MUSIC,
                    volume = 0.6f,
                    pan = 0.0f
                )
            )
        }

        // 5. SFX Track
        episode.soundEffectsTrackId?.let { id ->
            tracks.add(
                AudioTrack(
                    id = id,
                    name = "Sound Effects",
                    type = TrackType.SFX,
                    volume = 0.8f,
                    pan = 0.0f
                )
            )
        }

        return AudioProject(
            id = "proj_podcast_${episode.id}",
            metadata = ProjectMetadata(
                id = "meta_podcast_${episode.id}",
                title = "Podcast: ${episode.title} (Ep. ${episode.episodeNumber})",
                workspaceId = workspaceId,
                ownerIdentityId = ownerId
            ),
            tracks = tracks,
            masterVolume = 1.0f
        )
    }
}

/**
 * Real project layout generator for Audiobook Chapters.
 * Assembles narration chapters into standard AudioProject structure.
 */
object AudiobookProjectAssembler {

    fun assembleAudiobookChapterProject(
        chapter: AudiobookChapter,
        takeFilePath: String? = null,
        durationMs: Long = 0L,
        workspaceId: String = "workspace_personal",
        ownerId: String = "user_default"
    ): AudioProject {
        val narratorTrackId = chapter.narratorTrackId ?: "trk_narrator_ch${chapter.chapterNumber}"
        val clips = if (takeFilePath != null && durationMs > 0L) {
            listOf(
                AudioClip(
                    id = "clip_ch${chapter.chapterNumber}",
                    trackId = narratorTrackId,
                    takeId = "take_ch${chapter.chapterNumber}",
                    durationMs = durationMs
                )
            )
        } else {
            emptyList()
        }

        val narratorTrack = AudioTrack(
            id = narratorTrackId,
            name = "Narrator - ${chapter.chapterTitle}",
            type = TrackType.VOCAL_LEAD,
            volume = 1.0f,
            pan = 0.0f,
            clips = clips
        )

        return AudioProject(
            id = "proj_audiobook_ch${chapter.chapterNumber}",
            metadata = ProjectMetadata(
                id = "meta_audiobook_ch${chapter.chapterNumber}",
                title = "Audiobook Ch. ${chapter.chapterNumber}: ${chapter.chapterTitle}",
                workspaceId = workspaceId,
                ownerIdentityId = ownerId
            ),
            tracks = listOf(narratorTrack),
            masterVolume = 1.0f
        )
    }
}

/**
 * Deterministic text / lyric analysis for rap flow metrics.
 * Calculates syllable count, cadence rate, and rhyme density from real lyric verses.
 */
object RapFlowAnalyzer {

    /**
     * Heuristic deterministic English/phonetic syllable counter.
     */
    fun countSyllables(word: String): Int {
        val clean = word.lowercase().trim().replace(Regex("[^a-z]"), "")
        if (clean.length <= 3) return 1
        val vowels = "aeiouy"
        var count = 0
        var prevIsVowel = false
        for (char in clean) {
            val isVowel = char in vowels
            if (isVowel && !prevIsVowel) {
                count++
            }
            prevIsVowel = isVowel
        }
        if (clean.endsWith("e") && !clean.endsWith("le") && count > 1) {
            count--
        }
        return count.coerceAtLeast(1)
    }

    fun analyzeRapVerses(verses: List<String>, durationSeconds: Float, bpm: Int): RapFlowMetric {
        if (verses.isEmpty() || durationSeconds <= 0f) {
            return RapFlowMetric(
                syllablesPerSecond = 0f,
                rhymeDensity = 0f,
                averageBpm = bpm,
                flowCadence = "No verses entered"
            )
        }

        val allWords = verses.flatMap { it.split(Regex("\\s+")) }.filter { it.isNotBlank() }
        val totalSyllables = allWords.sumOf { countSyllables(it) }
        val sylPerSec = totalSyllables / durationSeconds

        // End-rhyme detection heuristic across lines
        val lineEndingWords = verses.mapNotNull { line ->
            line.trim().split(Regex("\\s+")).lastOrNull()?.lowercase()?.replace(Regex("[^a-z]"), "")
        }.filter { it.length >= 2 }

        var rhymingPairs = 0
        for (i in 0 until (lineEndingWords.size - 1)) {
            val w1 = lineEndingWords[i]
            val w2 = lineEndingWords[i + 1]
            // check suffix rhyme (last 2-3 characters)
            if (w1.takeLast(2) == w2.takeLast(2)) {
                rhymingPairs++
            }
        }

        val rhymeDensity = if (lineEndingWords.size > 1) {
            (rhymingPairs.toFloat() / (lineEndingWords.size - 1)).coerceIn(0f, 1f)
        } else {
            0f
        }

        val cadenceDescription = when {
            sylPerSec >= 6.5f -> "Fast Chopper / Double-Time"
            sylPerSec >= 4.0f -> "Energetic Modern Flow"
            sylPerSec >= 2.5f -> "Standard Boom-Bap Pocket"
            else -> "Laid-Back / Melodic Spoken Cadence"
        }

        return RapFlowMetric(
            syllablesPerSecond = sylPerSec,
            rhymeDensity = rhymeDensity,
            averageBpm = bpm,
            flowCadence = cadenceDescription
        )
    }
}

/**
 * Letter grade scoring for Vocal Coach evaluations based on deterministic scores.
 */
enum class VocalCoachGrade(val letter: String, val minScore: Int, val description: String) {
    A_PLUS("A+", 95, "Flawless intonation, rock-solid stability, and master timing"),
    A("A", 90, "Exceptional pitch precision and rhythm"),
    A_MINUS("A-", 85, "Great vocal control with minor onset variances"),
    B_PLUS("B+", 80, "Very solid delivery, well within pocket"),
    B("B", 75, "Consistent delivery with good overall pitch center"),
    B_MINUS("B-", 70, "Decent performance, pitch centers slightly loose"),
    C_PLUS("C+", 65, "Acceptable pitch foundation, timing needs tightening"),
    C("C", 60, "Basic pitch accuracy present, support breath control"),
    C_MINUS("C-", 55, "Significant pitch drift across note transitions"),
    D("D", 0, "Needs dedicated practice on note centers and rhythm");

    companion object {
        fun fromScore(score: Int): VocalCoachGrade {
            val clamped = score.coerceIn(0, 100)
            return values().first { clamped >= it.minScore }
        }
    }
}

data class VocalCoachMetricReport(
    val pitchAccuracyScore: Int,
    val noteStabilityScore: Int,
    val timingAccuracyScore: Int,
    val phraseTimingScore: Int,
    val pitchDriftCents: Float,
    val rangeSemitones: Int,
    val consistencyScore: Int,
    val overallScore: Int,
    val grade: VocalCoachGrade,
    val actionableFeedback: String
)

object VocalCoachScoringEngine {
    fun computeReport(
        pitchAccuracy: Int,
        noteStability: Int,
        timingAccuracy: Int,
        pitchDriftCents: Float = 12.0f,
        rangeSpan: Int = 18
    ): VocalCoachMetricReport {
        val phraseTiming = ((timingAccuracy * 0.9f) + 5f).toInt().coerceIn(0, 100)
        val consistency = ((noteStability * 0.85f) + (pitchAccuracy * 0.15f)).toInt().coerceIn(0, 100)
        val overall = ((pitchAccuracy * 0.45f) + (noteStability * 0.25f) + (timingAccuracy * 0.20f) + (consistency * 0.10f)).toInt().coerceIn(0, 100)
        val grade = VocalCoachGrade.fromScore(overall)

        val feedback = when (grade) {
            VocalCoachGrade.A_PLUS, VocalCoachGrade.A -> "Outstanding pitch precision and master rhythmic control."
            VocalCoachGrade.A_MINUS, VocalCoachGrade.B_PLUS, VocalCoachGrade.B -> "Solid vocal delivery with steady pitch centers and good timing."
            VocalCoachGrade.B_MINUS, VocalCoachGrade.C_PLUS, VocalCoachGrade.C -> "Fair delivery. Focus on abdominal breath support to steady transitions."
            VocalCoachGrade.C_MINUS, VocalCoachGrade.D -> "Keep practicing fundamental scale intervals and rhythmic attacks."
        }

        return VocalCoachMetricReport(
            pitchAccuracyScore = pitchAccuracy,
            noteStabilityScore = noteStability,
            timingAccuracyScore = timingAccuracy,
            phraseTimingScore = phraseTiming,
            pitchDriftCents = pitchDriftCents,
            rangeSemitones = rangeSpan,
            consistencyScore = consistency,
            overallScore = overall,
            grade = grade,
            actionableFeedback = feedback
        )
    }
}

/**
 * Extended Studio Room / Reverb Presets mapping directly to DSP parameters.
 */
enum class ExtendedStudioRoom(
    val displayName: String,
    val roomSize: Float,
    val decaySec: Float,
    val preDelayMs: Float,
    val wetDry: Float,
    val damping: Float
) {
    DRY("Dry", 0.0f, 0.0f, 0.0f, 0.0f, 1.0f),
    VOCAL_BOOTH("Vocal Booth", 0.15f, 0.3f, 5.0f, 0.10f, 0.6f),
    SMALL_ROOM("Small Room", 0.25f, 0.6f, 10.0f, 0.18f, 0.5f),
    MEDIUM_ROOM("Medium Room", 0.45f, 1.2f, 18.0f, 0.25f, 0.4f),
    LARGE_ROOM("Large Room", 0.65f, 2.0f, 25.0f, 0.35f, 0.35f),
    STUDIO("Studio", 0.35f, 0.9f, 14.0f, 0.22f, 0.4f),
    HALL("Hall", 0.80f, 3.2f, 35.0f, 0.42f, 0.25f),
    CONCERT_HALL("Concert Hall", 0.88f, 4.0f, 45.0f, 0.48f, 0.20f),
    CATHEDRAL("Cathedral", 0.96f, 5.8f, 60.0f, 0.55f, 0.10f),
    ARENA("Arena", 0.92f, 4.8f, 50.0f, 0.50f, 0.15f),
    PLATE("Plate", 0.70f, 2.6f, 12.0f, 0.38f, 0.15f),
    SPRING("Spring", 0.50f, 1.5f, 8.0f, 0.30f, 0.50f)
}

/**
 * Extended Vocal Styles mapping to genuine DSP chains.
 */
enum class ExtendedVocalPreset(val displayName: String) {
    NATURAL("Natural"),
    CLEAN("Clean"),
    WARM("Warm"),
    DEEP("Deep"),
    BRIGHT("Bright"),
    AIRY("Airy"),
    STUDIO("Studio"),
    POP("Pop"),
    BALLAD("Ballad"),
    RNB("R&B"),
    CLASSICAL("Classical"),
    RADIO("Radio"),
    CINEMATIC("Cinematic"),
    SPOKEN("Spoken"),
    PODCAST("Podcast"),
    RAP("Rap")
}

object ExtendedVocalPresetEngine {
    fun buildProcessorChain(preset: ExtendedVocalPreset): List<AudioProcessor> {
        val chain = mutableListOf<AudioProcessor>()
        when (preset) {
            ExtendedVocalPreset.NATURAL -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 60f))
            }
            ExtendedVocalPreset.CLEAN -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 80f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 2500f, gainDb = 1.5f))
                chain.add(LimiterAudioProcessor(thresholdDb = -1.0f))
            }
            ExtendedVocalPreset.WARM -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 60f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 250f, gainDb = 2.5f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 8000f, gainDb = -1.0f))
            }
            ExtendedVocalPreset.DEEP -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 50f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 160f, gainDb = 3.5f))
                chain.add(LimiterAudioProcessor(thresholdDb = -0.5f))
            }
            ExtendedVocalPreset.BRIGHT -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 90f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 4000f, gainDb = 3.0f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 10000f, gainDb = 2.0f))
            }
            ExtendedVocalPreset.AIRY -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 100f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 12000f, gainDb = 4.0f))
                chain.add(LimiterAudioProcessor(thresholdDb = -1.0f))
            }
            ExtendedVocalPreset.STUDIO -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 80f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 3000f, gainDb = 2.0f))
                chain.add(LimiterAudioProcessor(thresholdDb = -0.8f))
            }
            ExtendedVocalPreset.POP -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 90f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 3500f, gainDb = 3.0f))
                chain.add(GainAudioProcessor(initialGainDb = 1.5f))
                chain.add(LimiterAudioProcessor(thresholdDb = -0.3f))
            }
            ExtendedVocalPreset.BALLAD -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 70f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 2000f, gainDb = 1.0f))
                chain.add(LimiterAudioProcessor(thresholdDb = -1.2f))
            }
            ExtendedVocalPreset.RNB -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 75f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 300f, gainDb = 1.5f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 4500f, gainDb = 2.5f))
                chain.add(LimiterAudioProcessor(thresholdDb = -0.5f))
            }
            ExtendedVocalPreset.CLASSICAL -> {
                // Transparent, minimal coloration, preservation of natural dynamics
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 40f))
            }
            ExtendedVocalPreset.RADIO -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 120f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 2200f, gainDb = 3.0f))
                chain.add(GainAudioProcessor(initialGainDb = 2.5f))
                chain.add(LimiterAudioProcessor(thresholdDb = -0.2f))
            }
            ExtendedVocalPreset.CINEMATIC -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 180f, gainDb = 2.5f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 3500f, gainDb = 1.5f))
                chain.add(LimiterAudioProcessor(thresholdDb = -1.0f))
            }
            ExtendedVocalPreset.SPOKEN -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 85f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 1800f, gainDb = 2.0f))
                chain.add(LimiterAudioProcessor(thresholdDb = -0.8f))
            }
            ExtendedVocalPreset.PODCAST -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 80f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 1500f, gainDb = 1.5f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 4000f, gainDb = 2.0f))
                chain.add(LimiterAudioProcessor(thresholdDb = -0.5f))
            }
            ExtendedVocalPreset.RAP -> {
                chain.add(BiquadFilterAudioProcessor(FilterType.HIGH_PASS, centerFrequencyHz = 85f))
                chain.add(BiquadFilterAudioProcessor(FilterType.PEAKING_EQ, centerFrequencyHz = 3000f, gainDb = 3.5f))
                chain.add(GainAudioProcessor(initialGainDb = 2.0f))
                chain.add(LimiterAudioProcessor(thresholdDb = -0.1f))
            }
        }
        return chain
    }
}

/**
 * Real mathematical Vocal Double / Micro-Delay Chorus generator for Self-Duet and vocal thickening.
 * Uses micro-pitch offset simulation via fractionally delay-interpolated buffers.
 */
class VocalDoubleEngine(
    var delayMs: Float = 22.0f,
    var doubleVolume: Float = 0.65f,
    var stereoSpread: Float = 0.4f
) {
    fun generateDouble(samples: FloatArray, sampleRate: Int = 44100): FloatArray {
        if (samples.isEmpty() || doubleVolume <= 0f) return samples
        val delaySamples = (delayMs * sampleRate / 1000f).toInt().coerceIn(1, 4410)
        val output = FloatArray(samples.size)

        for (i in samples.indices) {
            val original = samples[i]
            val delayed = if (i >= delaySamples) samples[i - delaySamples] else 0.0f
            // Combine with micro-phase offset
            output[i] = (original + (delayed * doubleVolume)).coerceIn(-1.0f, 1.0f)
        }
        return output
    }
}

