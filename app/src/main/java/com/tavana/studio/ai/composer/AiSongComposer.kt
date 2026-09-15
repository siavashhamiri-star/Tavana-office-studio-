package com.tavana.studio.ai.composer

import android.content.Context
import com.example.BuildConfig
import com.tavana.karaoke.domain.model.LyricLine
import com.tavana.karaoke.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit

/**
 * Result of an AI music & lyrics generation session.
 */
data class AiSongResult(
    val id: String,
    val title: String,
    val style: String,
    val mood: String,
    val lyricsText: String,
    val lyricsLines: List<LyricLine>,
    val chords: String,
    val musicalKey: String,
    val tempoBpm: Int,
    val musicalNotes: List<Pair<Double, Double>>, // Freq (Hz) to Duration (Sec)
    val audioFilePath: String,
    val generatedBy: String = "Google Gemini 3.5 Flash"
)

/**
 * AI Song & Lyrics Composer.
 * Generates poetry, lyrics, musical arrangement (chords, scale, tempo),
 * and synthesizes real acoustic/harmonic melody audio tracks for recording.
 */
class AiSongComposer(private val context: Context) {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val composerStorageDir: File by lazy {
        File(context.filesDir, "music_library/ai_composed").apply { mkdirs() }
    }

    /**
     * Generates song lyrics, musical structure and synthesized melody audio.
     */
    suspend fun composeSong(
        prompt: String,
        style: String = "پاپ",
        mood: String = "عاشقانه",
        language: String = "fa"
    ): Result<AiSongResult> = withContext(Dispatchers.IO) {
        val songId = "ai_song_${System.currentTimeMillis()}"
        val outputFile = File(composerStorageDir, "${songId}.wav")

        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasKey = !apiKey.isNullOrBlank() && !apiKey.contains("YOUR_")

        var title = ""
        var lyrics = ""
        var chords = ""
        var musicalKey = ""
        var tempoBpm = 100
        var notes = listOf<Pair<Double, Double>>()
        var generatorSource = "Google Gemini 3.5 Flash"

        if (hasKey) {
            val apiResult = callGeminiForMusicAndLyrics(apiKey, prompt, style, mood, language)
            if (apiResult != null) {
                title = apiResult.title
                lyrics = apiResult.lyrics
                chords = apiResult.chords
                musicalKey = apiResult.musicalKey
                tempoBpm = apiResult.tempoBpm
                notes = apiResult.notes
            }
        }

        // Graceful poetic fallback if Gemini API call was skipped or failed
        if (title.isBlank() || lyrics.isBlank()) {
            val localGenerated = generateLocalSongTemplate(prompt, style, mood, language)
            title = localGenerated.title
            lyrics = localGenerated.lyrics
            chords = localGenerated.chords
            musicalKey = localGenerated.musicalKey
            tempoBpm = localGenerated.tempoBpm
            notes = localGenerated.notes
            generatorSource = "TAVANA AI Melodic Engine"
        }

        // Synthesize genuine WAV audio track for the melody and backing harmony
        synthesizeMelodyToWav(outputFile, notes, tempoBpm)

        val lyricLines = parseLyricsToLines(lyrics, notes.sumOf { it.second }.toLong() * 1000L)

        val result = AiSongResult(
            id = songId,
            title = title,
            style = style,
            mood = mood,
            lyricsText = lyrics,
            lyricsLines = lyricLines,
            chords = chords,
            musicalKey = musicalKey,
            tempoBpm = tempoBpm,
            musicalNotes = notes,
            audioFilePath = outputFile.absolutePath,
            generatedBy = generatorSource
        )
        Result.success(result)
    }

    /**
     * Converts an [AiSongResult] into a playable [Song] for the Karaoke Stage.
     */
    fun convertToSong(aiResult: AiSongResult): Song {
        return Song(
            id = aiResult.id,
            title = aiResult.title,
            artist = "هوش مصنوعی (${aiResult.style})",
            instrumentalPath = aiResult.audioFilePath,
            lyricsPath = "ai://${aiResult.id}",
            durationMs = (aiResult.musicalNotes.sumOf { it.second } * 1000.0).toLong().coerceAtLeast(60_000L),
            category = aiResult.style,
            language = "fa",
            tags = listOf("هوش مصنوعی", "شعر و آهنگ", aiResult.style, aiResult.mood)
        )
    }

    private data class ParsedAiPayload(
        val title: String,
        val lyrics: String,
        val chords: String,
        val musicalKey: String,
        val tempoBpm: Int,
        val notes: List<Pair<Double, Double>>
    )

    private fun callGeminiForMusicAndLyrics(
        apiKey: String,
        prompt: String,
        style: String,
        mood: String,
        language: String
    ): ParsedAiPayload? {
        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val systemInstruction = """
                You are a master song composer and poet in TAVANA Voice Studio.
                Compose complete lyrics (شعر و ترانه) and musical arrangement for a new song based on user request.
                Style: $style, Mood: $mood, Language: $language.
                
                You must return a valid JSON object ONLY, with these exact keys:
                - "title": (String) evocative song title
                - "lyrics": (String) full lyrics with Verses (بندها) and Chorus (همخوان / ترجیع بند) with beautiful rhymes
                - "chords": (String) guitar/piano chord progression, e.g. "Am - F - C - G"
                - "musicalKey": (String) scale or Persian dastgah, e.g. "A Minor / دستگاه اصفهان"
                - "tempoBpm": (Integer) tempo between 70 and 140
                - "notes": (Array of objects) 8 to 16 melody note elements each having {"freq": Double (Hz), "duration": Double (seconds between 0.8 and 3.0)}. Standard pitch frequencies e.g. A4=440.0, B4=493.88, C5=523.25, D5=587.33, E5=659.25, G4=392.0, F4=349.23, E4=329.63.
            """.trimIndent()

            val userText = "موضوع شعر و آهنگ: ${if (prompt.isNotBlank()) prompt else "عاشقانه و امید به آینده"}"

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", userText))
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.7)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url(endpoint)
                .post(requestJson.toString().toRequestBody(mediaType))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                val bodyString = response.body!!.string()
                val rootJson = JSONObject(bodyString)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentObj = candidate.getJSONObject("content")
                    val parts = contentObj.getJSONArray("parts")
                    val rawText = parts.getJSONObject(0).getString("text")
                    val json = JSONObject(rawText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim())

                    val title = json.optString("title", "آهنگ هوش مصنوعی")
                    val lyrics = json.optString("lyrics", "")
                    val chords = json.optString("chords", "Am - F - C - G")
                    val musicalKey = json.optString("musicalKey", "A Minor")
                    val tempoBpm = json.optInt("tempoBpm", 100)

                    val notesList = mutableListOf<Pair<Double, Double>>()
                    val notesArray = json.optJSONArray("notes")
                    if (notesArray != null) {
                        for (i in 0 until notesArray.length()) {
                            val nObj = notesArray.getJSONObject(i)
                            val f = nObj.optDouble("freq", 440.0)
                            val d = nObj.optDouble("duration", 1.5)
                            notesList.add(f to d)
                        }
                    }

                    return ParsedAiPayload(
                        title = title,
                        lyrics = lyrics,
                        chords = chords,
                        musicalKey = musicalKey,
                        tempoBpm = tempoBpm,
                        notes = if (notesList.isNotEmpty()) notesList else defaultAiNotes
                    )
                }
            }
        } catch (_: Exception) {
            // fallback
        }
        return null
    }

    private fun generateLocalSongTemplate(
        prompt: String,
        style: String,
        mood: String,
        language: String
    ): ParsedAiPayload {
        return when {
            style.contains("سنتی") -> ParsedAiPayload(
                title = if (prompt.isNotBlank()) "نغمه‌ی $prompt" else "آوای دلشدگان",
                lyrics = """
                    [بند اول]
                    سحر بلبل حکایت با صبا کرد
                    دل تنگ مرا از غم رها کرد
                    بیا ساقی که شور عشق باقی‌ست
                    هوای کوی تو دارالشفا کرد
                    
                    [همخوان]
                    ای مهربان من، آرام جان من
                    نغمه بزن در دل، سوز و گداز دل
                    
                    [بند دوم]
                    اگر دوری ولی در سینه جایت
                    به گوش دل رسد هر دم صدایت
                """.trimIndent(),
                chords = "شور و دشتی (Dm - Gm - C - Dm)",
                musicalKey = "دستگاه شور / Dstgah Shur",
                tempoBpm = 85,
                notes = listOf(
                    392.00 to 2.0, // G4
                    440.00 to 2.0, // A4
                    466.16 to 2.5, // Bb4
                    523.25 to 2.5, // C5
                    466.16 to 1.5, // Bb4
                    440.00 to 2.0, // A4
                    392.00 to 3.0  // G4
                )
            )
            style.contains("رپ") || style.contains("هیپ") -> ParsedAiPayload(
                title = if (prompt.isNotBlank()) "بیت $prompt" else "ضربان خیابان",
                lyrics = """
                    [ورس ۱]
                    میکروفون دستم، صدام توی گوش شهر
                    رد میشم از توی تاریکی و از دل قهر
                    هر کلمه یه نوره توی این مسیر مبهم
                    می‌سازم فردامو با دستای خودم محکم
                    
                    [کورس]
                    ریتم پایدار، ضربان بی‌قرار
                    رویاتو بساز، تسلیم نشو این بار
                    
                    [ورس ۲]
                    بیت می‌کوبه توی سینه، انرژی بی‌نهایت
                    این صدای نسلیه که می‌خواد بشه حکایت
                """.trimIndent(),
                chords = "Em - C - Am - Bm",
                musicalKey = "E Minor (Modern 808 Trap)",
                tempoBpm = 95,
                notes = listOf(
                    220.0 to 1.0, // A3
                    246.94 to 1.0, // B3
                    261.63 to 1.0, // C4
                    293.66 to 1.0, // D4
                    246.94 to 1.0, // B3
                    220.0 to 2.0   // A3
                )
            )
            else -> ParsedAiPayload(
                title = if (prompt.isNotBlank()) "ترانه‌ی $prompt" else "پرواز در ستاره‌ها",
                lyrics = """
                    [بند اول]
                    توی چشمات یه اقیانوس نوره
                    صدات مرهم دل‌های صبوره
                    قدم بردار با من زیر بارون
                    که غم از کوچه‌های ما به دوره
                    
                    [کورس]
                    می‌خونم برات از عمق ترانه
                    تویی شعر من، پاک و عاشقانه
                    بیا تا ابد هم‌صدا بمونیم
                    در این لحظه‌های ناب و جاودانه
                    
                    [بند دوم]
                    دنیا قشنگه با نغمه‌های تو
                    قلبم تپید باز به هوای تو
                """.trimIndent(),
                chords = "Am - F - C - G",
                musicalKey = "C Major / A Minor (Pop Ballad)",
                tempoBpm = 112,
                notes = listOf(
                    440.0 to 1.5, // A4
                    523.25 to 1.5, // C5
                    587.33 to 2.0, // D5
                    659.25 to 2.5, // E5
                    587.33 to 1.5, // D5
                    523.25 to 2.0, // C5
                    440.0 to 3.0   // A4
                )
            )
        }
    }

    private fun parseLyricsToLines(lyrics: String, totalDurationMs: Long): List<LyricLine> {
        val rawLines = lyrics.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("[") && !it.endsWith("]") }

        if (rawLines.isEmpty()) return emptyList()

        val safeDuration = totalDurationMs.coerceAtLeast(60_000L)
        val stepMs = (safeDuration / (rawLines.size + 1)).coerceAtLeast(4000L)

        return rawLines.mapIndexed { index, text ->
            val start = (index + 1) * stepMs
            LyricLine(
                id = index + 1,
                startMs = start,
                endMs = start + stepMs - 300L,
                text = text,
                secondaryText = null,
                isRtl = true
            )
        }
    }

    private val defaultAiNotes = listOf(
        440.0 to 1.5,
        493.88 to 1.5,
        523.25 to 2.0,
        587.33 to 2.0,
        523.25 to 1.5,
        440.0 to 3.0
    )

    private fun synthesizeMelodyToWav(
        outputFile: File,
        notes: List<Pair<Double, Double>>,
        tempoBpm: Int
    ) {
        val sampleRate = 44100
        val numChannels = 2
        val bytesPerSample = 2

        var totalDurationSec = notes.sumOf { it.second }
        if (totalDurationSec < 10.0) totalDurationSec = 45.0

        val totalSamples = (totalDurationSec * sampleRate).toInt()
        val tempPcmFile = File(context.cacheDir, "temp_ai_synth_${System.currentTimeMillis()}.pcm")

        FileOutputStream(tempPcmFile).use { fos ->
            val bufferSize = 4096
            val buffer = ByteArray(bufferSize)
            var bufferPos = 0

            var noteIdx = 0
            var noteSampleCounter = 0
            var currentFreq = notes.firstOrNull()?.first ?: 440.0
            var currentNoteSamples = ((notes.firstOrNull()?.second ?: 2.0) * sampleRate).toInt()

            // Beat calculation from tempo
            val beatIntervalSamples = ((60.0 / tempoBpm.coerceIn(60, 180)) * sampleRate).toInt()

            for (i in 0 until totalSamples) {
                noteSampleCounter++
                if (noteSampleCounter >= currentNoteSamples) {
                    noteIdx = (noteIdx + 1) % notes.size
                    val next = notes[noteIdx]
                    currentFreq = next.first
                    currentNoteSamples = (next.second * sampleRate).toInt()
                    noteSampleCounter = 0
                }

                val t = i.toDouble() / sampleRate
                val attackDecay = (1.0 - (noteSampleCounter.toDouble() / currentNoteSamples)).coerceIn(0.15, 1.0)
                
                // Rich acoustic instrument synthesis (Fundamental + 2nd & 3rd harmonic + subtle sub-bass)
                val fundamental = Math.sin(2.0 * Math.PI * currentFreq * t)
                val harmonic2 = 0.35 * Math.sin(2.0 * Math.PI * currentFreq * 2.0 * t)
                val harmonic3 = 0.15 * Math.sin(2.0 * Math.PI * currentFreq * 3.0 * t)
                val subBass = 0.25 * Math.sin(2.0 * Math.PI * (currentFreq / 2.0) * t)

                // Rhythm pulse / metronome beat
                val beatPos = i % beatIntervalSamples
                val beatClick = if (beatPos < 600) (1.0 - beatPos / 600.0) * 0.25 * Math.sin(2.0 * Math.PI * 180.0 * (beatPos.toDouble() / sampleRate)) else 0.0

                val combined = (fundamental + harmonic2 + harmonic3 + subBass) * attackDecay * 0.6 + beatClick
                val sampleValue = (combined * 32767.0).toInt().coerceIn(-32768, 32767).toShort()

                // Left channel
                buffer[bufferPos++] = (sampleValue.toInt() and 0xFF).toByte()
                buffer[bufferPos++] = ((sampleValue.toInt() shr 8) and 0xFF).toByte()
                // Right channel
                buffer[bufferPos++] = (sampleValue.toInt() and 0xFF).toByte()
                buffer[bufferPos++] = ((sampleValue.toInt() shr 8) and 0xFF).toByte()

                if (bufferPos >= bufferSize) {
                    fos.write(buffer, 0, bufferPos)
                    bufferPos = 0
                }
            }
            if (bufferPos > 0) {
                fos.write(buffer, 0, bufferPos)
            }
        }

        // Package into standard 44-byte WAV
        val pcmDataLength = tempPcmFile.length()
        FileOutputStream(outputFile).use { fos ->
            writeWavHeader(fos, pcmDataLength, sampleRate, numChannels, bytesPerSample * 8)
            tempPcmFile.inputStream().use { pis ->
                pis.copyTo(fos)
            }
        }
        tempPcmFile.delete()
    }

    private fun writeWavHeader(
        out: FileOutputStream,
        totalAudioLen: Long,
        sampleRate: Int,
        channels: Int,
        bitDepth: Int
    ) {
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * (bitDepth / 8)
        val blockAlign = channels * (bitDepth / 8)

        val header = ByteArray(44)
        val bb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        bb.put("RIFF".toByteArray())
        bb.putInt(totalDataLen.toInt())
        bb.put("WAVE".toByteArray())
        bb.put("fmt ".toByteArray())
        bb.putInt(16)
        bb.putShort(1.toShort())
        bb.putShort(channels.toShort())
        bb.putInt(sampleRate)
        bb.putInt(byteRate)
        bb.putShort(blockAlign.toShort())
        bb.putShort(bitDepth.toShort())
        bb.put("data".toByteArray())
        bb.putInt(totalAudioLen.toInt())

        out.write(header, 0, 44)
    }
}
