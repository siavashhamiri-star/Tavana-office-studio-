package com.example.data

import com.tavana.karaoke.domain.model.LyricLine
import com.tavana.karaoke.domain.model.PracticeExercise
import com.tavana.karaoke.domain.model.RecordingTake
import com.tavana.karaoke.domain.model.Song
import com.tavana.karaoke.domain.model.VocalScore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AvaRepository {

    private val _songs = MutableStateFlow(sampleSongs)
    val songs: Flow<List<Song>> = _songs.asStateFlow()

    private val _recordings = MutableStateFlow(sampleRecordings)
    val recordings: Flow<List<RecordingTake>> = _recordings.asStateFlow()

    private val _practiceExercises = MutableStateFlow(sampleExercises)
    val practiceExercises: Flow<List<PracticeExercise>> = _practiceExercises.asStateFlow()

    fun getSongById(id: String): Song? {
        return _songs.value.find { it.id == id } ?: _songs.value.firstOrNull()
    }

    fun getLyricsForSong(songId: String): List<LyricLine> {
        return sampleLyricsMap[songId] ?: defaultSampleLyrics
    }

    fun saveRecording(take: RecordingTake) {
        _recordings.update { current ->
            listOf(take) + current
        }
    }

    fun deleteRecording(takeId: String) {
        _recordings.update { current ->
            current.filterNot { it.id == takeId }
        }
    }

    fun updateSongs(newSongs: List<Song>) {
        _songs.value = newSongs
    }

    companion object {
        val sampleSongs = listOf(
            Song(
                id = "song_morgh_sahar",
                title = "Morgh-e Sahar (مرغ سحر)",
                artist = "Morteza Neydavoud",
                instrumentalPath = "asset://audio/morgh_sahar_inst.mp3",
                lyricsPath = "asset://lyrics/morgh_sahar.lrc",
                durationMs = 184_000L
            ),
            Song(
                id = "song_golden_sunset",
                title = "Sunset Serenade",
                artist = "AVA Acoustic Collective",
                instrumentalPath = "asset://audio/sunset_serenade.mp3",
                lyricsPath = "asset://lyrics/sunset_serenade.lrc",
                durationMs = 195_000L
            ),
            Song(
                id = "song_soltane_ghalbha",
                title = "Soltan-e Ghalbha (سلطان قلب‌ها)",
                artist = "Aref / Anoushiravan Rohani",
                instrumentalPath = "asset://audio/soltane_ghalbha.mp3",
                lyricsPath = "asset://lyrics/soltane_ghalbha.lrc",
                durationMs = 210_000L
            ),
            Song(
                id = "song_radiant_stage",
                title = "Your Voice, Your Stage",
                artist = "AVA Studio Ensemble",
                instrumentalPath = "asset://audio/your_voice_your_stage.mp3",
                lyricsPath = "asset://lyrics/your_voice_your_stage.lrc",
                durationMs = 160_000L
            )
        )

        val sampleLyricsMap = mapOf(
            "song_morgh_sahar" to listOf(
                LyricLine(1, 0L, 8_000L, "مرغ سحر ناله سر کن", "Morgh-e sahar naaleh sar kon", isRtl = true),
                LyricLine(2, 8_000L, 16_000L, "داغ مرا تازه‌تر کن", "Daghe mara tazetar kon", isRtl = true),
                LyricLine(3, 16_000L, 25_000L, "ز آه شرربار این قفس را", "Ze ahe shararbar in ghafas ra", isRtl = true),
                LyricLine(4, 25_000L, 35_000L, "برشکن و زیر و زبر کن", "Bar-shekan o zir o zebar kon", isRtl = true),
                LyricLine(5, 35_000L, 46_000L, "بلبل پربسته ز کنج قفس درآ", "Bolbole par-basteh ze konje ghafas dara", isRtl = true),
                LyricLine(6, 46_000L, 60_000L, "نغمه آزادی نوع بشر سرا", "Naghmehye azadiye noe bashar sara", isRtl = true)
            ),
            "song_soltane_ghalbha" to listOf(
                LyricLine(1, 0L, 7_000L, "یه دل می‌گه برم برم", "Ye del mige beram beram", isRtl = true),
                LyricLine(2, 7_000L, 15_000L, "یه دل می‌گه نرم نرم", "Ye del mige naram naram", isRtl = true),
                LyricLine(3, 15_000L, 23_000L, "طاقت نداره دلم بی تو", "Taghat nadare delam bi to", isRtl = true),
                LyricLine(4, 23_000L, 34_000L, "بی تو چه کنم چه کنم", "Bi to che konam che konam", isRtl = true),
                LyricLine(5, 34_000L, 48_000L, "سلطان قلبم تو هستی تو هستی", "Soltane ghalbam to hasti to hasti", isRtl = true)
            )
        )

        val defaultSampleLyrics = listOf(
            LyricLine(1, 0L, 6_000L, "Step into the light, feel the melody rise"),
            LyricLine(2, 6_000L, 14_000L, "Every note you sing brings the sunset to life"),
            LyricLine(3, 14_000L, 22_000L, "Your voice is the courage, your heart is the stage"),
            LyricLine(4, 22_000L, 32_000L, "Sing without fear, turn a brand new page"),
            LyricLine(5, 32_000L, 44_000L, "AVA calls you forward: Your Voice, Your Stage!"),
            LyricLine(6, 44_000L, 56_000L, "Let the warm golden harmony carry away the night")
        )

        val sampleRecordings = listOf(
            RecordingTake(
                id = "take_101",
                songId = "song_morgh_sahar",
                songTitle = "Morgh-e Sahar (مرغ سحر)",
                artist = "Morteza Neydavoud",
                timestamp = System.currentTimeMillis() - 86_400_000L,
                durationMs = 184_000L,
                overallScore = 94,
                pitchAccuracy = 95,
                rhythmAccuracy = 92,
                vocalPower = 96,
                isFavorite = true
            ),
            RecordingTake(
                id = "take_102",
                songId = "song_golden_sunset",
                songTitle = "Sunset Serenade",
                artist = "AVA Acoustic Collective",
                timestamp = System.currentTimeMillis() - 172_800_000L,
                durationMs = 195_000L,
                overallScore = 88,
                pitchAccuracy = 89,
                rhythmAccuracy = 87,
                vocalPower = 90,
                isFavorite = false
            )
        )

        val sampleExercises = listOf(
            PracticeExercise(
                id = "drill_lip_trill",
                title = "Lip Trill Glide",
                description = "Gentle airflow release to warm up vocal cords without strain",
                targetNote = "A3",
                targetFreqHz = 220f,
                durationSeconds = 60,
                category = "Warm-up"
            ),
            PracticeExercise(
                id = "drill_five_tone",
                title = "Five-Tone Scale Arpeggio",
                description = "Strengthen pitch accuracy and transition smoothly across intervals",
                targetNote = "C4",
                targetFreqHz = 261.63f,
                durationSeconds = 90,
                category = "Pitch Accuracy"
            ),
            PracticeExercise(
                id = "drill_breath_hold",
                title = "Diaphragmatic Breath Hold",
                description = "Control steady exhalation for sustained vibrato and power",
                targetNote = "E4",
                targetFreqHz = 329.63f,
                durationSeconds = 45,
                category = "Breath Support"
            ),
            PracticeExercise(
                id = "drill_vowel_resonance",
                title = "Persian Dastgah Resonance (شور / همایون)",
                description = "Rich microtone articulation and warm chest-to-head resonance",
                targetNote = "D4",
                targetFreqHz = 293.66f,
                durationSeconds = 120,
                category = "Resonance"
            )
        )
    }
}
