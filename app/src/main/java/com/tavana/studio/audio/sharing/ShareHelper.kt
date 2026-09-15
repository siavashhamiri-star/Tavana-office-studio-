package com.tavana.studio.audio.sharing

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.tavana.karaoke.domain.model.RecordingTake
import java.io.File

/**
 * Utility for sharing vocal recordings and audio takes via Android system share sheet.
 */
object ShareHelper {

    /**
     * Shares a vocal recording take via [Intent.ACTION_SEND].
     * Attaches the physical WAV audio file via [FileProvider] and shares song details.
     */
    fun shareRecordingTake(context: Context, take: RecordingTake) {
        val path = take.filePath
        val audioFile = if (!path.isNullOrEmpty()) File(path) else null
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            val shareText = buildString {
                appendLine("🎙️ ضبط صدای من در استودیو آواز TAVANA")
                appendLine("قطعه: ${take.songTitle}")
                appendLine("خواننده: ${take.artist}")
                appendLine("امتیاز اجرا: ${take.overallScore} از ۱۰۰ (دقت گام: ${take.pitchAccuracy}٪)")
                appendLine("#TAVANA_Studio #وکال #خوانندگی")
            }

            putExtra(Intent.EXTRA_SUBJECT, "ضبط صدای ${take.songTitle}")
            putExtra(Intent.EXTRA_TEXT, shareText)

            if (audioFile != null && audioFile.exists() && audioFile.length() > 0) {
                try {
                    val authority = "${context.packageName}.fileprovider"
                    val contentUri = FileProvider.getUriForFile(context, authority, audioFile)
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    type = "audio/wav"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {
                    type = "text/plain"
                }
            } else {
                type = "text/plain"
            }
        }

        val chooser = Intent.createChooser(shareIntent, "اشتراک‌گذاری قطعه‌ی ضبط‌شده با:")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun shareRecording(context: Context, take: RecordingTake) = shareRecordingTake(context, take)

    /**
     * Shares AI-generated poetry and musical chords as text.
     */
    fun shareLyricsAndSong(context: Context, title: String, lyrics: String, chords: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            val body = buildString {
                appendLine("✨ ترانه و آهنگ ساخته‌شده با هوش مصنوعی TAVANA")
                appendLine("نام ترانه: $title")
                appendLine("آکوردها: $chords")
                appendLine("-------------------")
                appendLine(lyrics)
                appendLine("-------------------")
                appendLine("ساخته‌شده در استودیو هوشمند TAVANA")
            }
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        val chooser = Intent.createChooser(shareIntent, "اشتراک‌گذاری شعر و آهنگ:")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
