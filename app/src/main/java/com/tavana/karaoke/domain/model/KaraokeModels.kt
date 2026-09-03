package com.tavana.karaoke.domain.model

data class LyricLine(
    val id: Int,
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val secondaryText: String? = null,
    val isRtl: Boolean = false
)

data class RecordingTake(
    val id: String,
    val songId: String,
    val songTitle: String,
    val artist: String,
    val timestamp: Long,
    val durationMs: Long,
    val overallScore: Int,
    val pitchAccuracy: Int,
    val rhythmAccuracy: Int,
    val vocalPower: Int,
    val isFavorite: Boolean = false
)

data class PracticeExercise(
    val id: String,
    val title: String,
    val description: String,
    val targetNote: String,
    val targetFreqHz: Float,
    val durationSeconds: Int,
    val category: String
)

data class VocalScore(
    val overall: Int,
    val pitch: Int,
    val rhythm: Int,
    val expression: Int,
    val badge: String,
    val feedback: String
)
