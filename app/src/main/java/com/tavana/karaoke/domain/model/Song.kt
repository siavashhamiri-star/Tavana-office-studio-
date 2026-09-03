package com.tavana.karaoke.domain.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val instrumentalPath: String,
    val lyricsPath: String?,
    val durationMs: Long
)
