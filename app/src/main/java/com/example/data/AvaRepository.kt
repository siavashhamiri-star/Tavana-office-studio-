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
                artist = "Morteza Neydavoud / Shajarian",
                instrumentalPath = "asset://audio/morgh_sahar_inst.mp3",
                lyricsPath = "asset://lyrics/morgh_sahar.lrc",
                durationMs = 184_000L,
                category = "سنتی",
                language = "fa",
                tags = listOf("سنتی", "دشتی", "نی داوود", "شجریان", "قمرالملوک", "ایرانی", "persian", "traditional")
            ),
            Song(
                id = "song_soltane_ghalbha",
                title = "Soltan-e Ghalbha (سلطان قلب‌ها)",
                artist = "Aref / Anoushiravan Rohani",
                instrumentalPath = "asset://audio/soltane_ghalbha.mp3",
                lyricsPath = "asset://lyrics/soltane_ghalbha.lrc",
                durationMs = 210_000L,
                category = "پاپ",
                language = "fa",
                tags = listOf("پاپ", "عارف", "روحانی", "عاشقانه", "نوستالژی", "persian", "pop")
            ),
            Song(
                id = "song_gole_sangam",
                title = "Gole Sangam (گل سنگم)",
                artist = "Hayedeh / Anoushiravan Rohani",
                instrumentalPath = "asset://audio/gole_sangam.mp3",
                lyricsPath = "asset://lyrics/gole_sangam.lrc",
                durationMs = 190_000L,
                category = "پاپ",
                language = "fa",
                tags = listOf("هایده", "گل سنگم", "روحانی", "کلاسیک", "خاطره‌انگیز", "persian", "pop")
            ),
            Song(
                id = "song_jane_maryam",
                title = "Jane Maryam (جان مریم)",
                artist = "Mohammad Nouri",
                instrumentalPath = "asset://audio/jane_maryam.mp3",
                lyricsPath = "asset://lyrics/jane_maryam.lrc",
                durationMs = 175_000L,
                category = "فولکلور",
                language = "fa",
                tags = listOf("نوری", "محمد نوری", "جان مریم", "نازنین مریم", "فولکلور", "گیلکی", "persian")
            ),
            Song(
                id = "song_ey_iran",
                title = "Ey Iran (ای ایران)",
                artist = "Gholam-Hossein Banan / Ruhollah Khaleqi",
                instrumentalPath = "asset://audio/ey_iran.mp3",
                lyricsPath = "asset://lyrics/ey_iran.lrc",
                durationMs = 198_000L,
                category = "ملی و میهنی",
                language = "fa",
                tags = listOf("ایران", "سرود", "بنان", "خالقی", "میهنی", "persian")
            ),
            Song(
                id = "song_soghati",
                title = "Soghati (سوغاتی - وقتی میای)",
                artist = "Hayedeh / Mohammad Heydari",
                instrumentalPath = "asset://audio/soghati.mp3",
                lyricsPath = "asset://lyrics/soghati.lrc",
                durationMs = 215_000L,
                category = "پاپ",
                language = "fa",
                tags = listOf("سوغاتی", "هایده", "وقتی میای", "حیدری", "اردلان سرفراز", "persian")
            ),
            Song(
                id = "song_golden_sunset",
                title = "Sunset Serenade",
                artist = "AVA Acoustic Collective",
                instrumentalPath = "asset://audio/sunset_serenade.mp3",
                lyricsPath = "asset://lyrics/sunset_serenade.lrc",
                durationMs = 195_000L,
                category = "آکوستیک",
                language = "en",
                tags = listOf("acoustic", "guitar", "sunset", "english", "ballad")
            ),
            Song(
                id = "song_radiant_stage",
                title = "Your Voice, Your Stage",
                artist = "AVA Studio Ensemble",
                instrumentalPath = "asset://audio/your_voice_your_stage.mp3",
                lyricsPath = "asset://lyrics/your_voice_your_stage.lrc",
                durationMs = 160_000L,
                category = "استودیو",
                language = "en",
                tags = listOf("stage", "modern", "pop", "inspiration", "english")
            ),
            Song(
                id = "song_fly_me_to_the_moon",
                title = "Fly Me to the Moon",
                artist = "Frank Sinatra / Bart Howard",
                instrumentalPath = "asset://audio/fly_me_to_the_moon.mp3",
                lyricsPath = "asset://lyrics/fly_me_to_the_moon.lrc",
                durationMs = 165_000L,
                category = "بین‌المللی",
                language = "en",
                tags = listOf("jazz", "sinatra", "moon", "english", "standard", "international")
            ),
            Song(
                id = "song_bella_ciao",
                title = "Bella Ciao (بلا چاو)",
                artist = "Italian Folk Acoustic",
                instrumentalPath = "asset://audio/bella_ciao.mp3",
                lyricsPath = "asset://lyrics/bella_ciao.lrc",
                durationMs = 145_000L,
                category = "بین‌المللی",
                language = "it",
                tags = listOf("italian", "folk", "bella ciao", "money heist", "international")
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
            ),
            "song_gole_sangam" to listOf(
                LyricLine(1, 0L, 7_000L, "گل سنگم گل سنگم چی بگم از دل تنگم", "Gole sangam gole sangam chi begam az dele tangam", isRtl = true),
                LyricLine(2, 7_000L, 16_000L, "مثل آفتاب اگه بر من نتابی سردم و بی‌رنگم", "Mesle aftab age bar man natabi sardam o bi rangam", isRtl = true),
                LyricLine(3, 16_000L, 25_000L, "همه آهم همه دردم مثل طوفان پر گردم", "Hame aham hame dardam mesle toofan pore gardam", isRtl = true),
                LyricLine(4, 25_000L, 38_000L, "باد پاییزی بشم من توی صحرا سرگردم", "Bade paeezi besham man tooye sahra sargardam", isRtl = true)
            ),
            "song_jane_maryam" to listOf(
                LyricLine(1, 0L, 8_000L, "جان مریم چشماتو وا کن سری بالا کن", "Jane Maryam cheshmato va kon sari bala kon", isRtl = true),
                LyricLine(2, 8_000L, 17_000L, "در اومد خورشید شد هوا سفید", "Dar oomad khorshid shod hava sefid", isRtl = true),
                LyricLine(3, 17_000L, 27_000L, "وقت اون شد که بریم به صحرا آی نازنین مریم", "Vaghte oon shod ke berim be sahra ay nazanin Maryam", isRtl = true),
                LyricLine(4, 27_000L, 40_000L, "باز دوباره صبح شد من زمین بیدار شد", "Baz do bare sobh shod man zamin bidar shod", isRtl = true)
            ),
            "song_ey_iran" to listOf(
                LyricLine(1, 0L, 8_000L, "ای ایران ای مرز پرگهر", "Ey Iran ey marze por gohar", isRtl = true),
                LyricLine(2, 8_000L, 16_000L, "ای خاکت سرچشمه هنر", "Ey khakat sar cheshmeye honar", isRtl = true),
                LyricLine(3, 16_000L, 26_000L, "دور از تو اندیشه بدان", "Door az to andisheye badan", isRtl = true),
                LyricLine(4, 26_000L, 38_000L, "پاینده مانی و جاودان", "Payandeh mani o javedan", isRtl = true)
            ),
            "song_soghati" to listOf(
                LyricLine(1, 0L, 8_000L, "وقتی میای صدای پات از همه جاده‌ها میاد", "Vaghti miay sedaye pat az hame jaddeha miad", isRtl = true),
                LyricLine(2, 8_000L, 18_000L, "انگار نه از یه شهر دور که از همه دنیا میاد", "Engar na az ye shahre door ke az hame donya miad", isRtl = true),
                LyricLine(3, 18_000L, 28_000L, "تا وقتی که در وا میشه لحظه دیدن میرسه", "Ta vaghti ke dar va mishe lahzeye didan mireseh", isRtl = true),
                LyricLine(4, 28_000L, 42_000L, "هر چی که جاده‌س رو زمین به زیر پاهام میرسه", "Har chi ke jaddas roo zamin be zire paham mireseh", isRtl = true)
            ),
            "song_fly_me_to_the_moon" to listOf(
                LyricLine(1, 0L, 6_000L, "Fly me to the moon, let me play among the stars"),
                LyricLine(2, 6_000L, 13_000L, "Let me see what spring is like on a-Jupiter and Mars"),
                LyricLine(3, 13_000L, 21_000L, "In other words, hold my hand"),
                LyricLine(4, 21_000L, 32_000L, "In other words, baby, kiss me")
            ),
            "song_bella_ciao" to listOf(
                LyricLine(1, 0L, 6_000L, "Una mattina mi son svegliato"),
                LyricLine(2, 6_000L, 12_000L, "O bella ciao, bella ciao, bella ciao ciao ciao"),
                LyricLine(3, 12_000L, 20_000L, "Una mattina mi son svegliato"),
                LyricLine(4, 20_000L, 30_000L, "E ho trovato l'invasor")
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
