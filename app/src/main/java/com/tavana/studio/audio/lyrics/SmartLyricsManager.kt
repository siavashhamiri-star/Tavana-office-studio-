package com.tavana.studio.audio.lyrics

import com.tavana.karaoke.domain.model.LyricLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SynchronizedLyricsState(
    val lines: List<LyricLine> = emptyList(),
    val currentLineIndex: Int = -1,
    val currentLine: LyricLine? = null,
    val isUserProvided: Boolean = false,
    val songTitle: String = "",
    val copyrightNotice: String? = null
)

interface SmartLyricsManager {
    val state: StateFlow<SynchronizedLyricsState>

    fun loadLyrics(lines: List<LyricLine>, songTitle: String, copyrightNotice: String? = null, isUserProvided: Boolean = false)
    fun updatePlaybackPosition(positionMs: Long)
    fun adjustLineTiming(lineId: Int, startOffsetDeltaMs: Long, endOffsetDeltaMs: Long)
    fun addUserLine(startMs: Long, endMs: Long, text: String, secondaryText: String? = null)
    fun removeLine(lineId: Int)
}

class DefaultSmartLyricsManager : SmartLyricsManager {
    private val _state = MutableStateFlow(SynchronizedLyricsState())
    override val state: StateFlow<SynchronizedLyricsState> = _state.asStateFlow()

    override fun loadLyrics(
        lines: List<LyricLine>,
        songTitle: String,
        copyrightNotice: String?,
        isUserProvided: Boolean
    ) {
        val sorted = lines.sortedBy { it.startMs }
        _state.value = SynchronizedLyricsState(
            lines = sorted,
            currentLineIndex = if (sorted.isNotEmpty()) 0 else -1,
            currentLine = sorted.firstOrNull(),
            isUserProvided = isUserProvided,
            songTitle = songTitle,
            copyrightNotice = copyrightNotice
        )
    }

    override fun updatePlaybackPosition(positionMs: Long) {
        val currentLines = _state.value.lines
        if (currentLines.isEmpty()) return

        val foundIndex = currentLines.indexOfFirst { line ->
            positionMs in line.startMs..line.endMs
        }

        if (foundIndex != -1) {
            if (_state.value.currentLineIndex != foundIndex) {
                _state.value = _state.value.copy(
                    currentLineIndex = foundIndex,
                    currentLine = currentLines[foundIndex]
                )
            }
        } else {
            // Find preceding line or next line
            val nextIndex = currentLines.indexOfFirst { it.startMs > positionMs }
            val resolvedIndex = when {
                nextIndex == 0 -> 0
                nextIndex > 0 -> nextIndex - 1
                else -> currentLines.lastIndex
            }
            if (_state.value.currentLineIndex != resolvedIndex) {
                _state.value = _state.value.copy(
                    currentLineIndex = resolvedIndex,
                    currentLine = currentLines.getOrNull(resolvedIndex)
                )
            }
        }
    }

    override fun adjustLineTiming(lineId: Int, startOffsetDeltaMs: Long, endOffsetDeltaMs: Long) {
        val updated = _state.value.lines.map { line ->
            if (line.id == lineId) {
                val newStart = (line.startMs + startOffsetDeltaMs).coerceAtLeast(0L)
                val newEnd = (line.endMs + endOffsetDeltaMs).coerceAtLeast(newStart + 100L)
                line.copy(startMs = newStart, endMs = newEnd)
            } else {
                line
            }
        }.sortedBy { it.startMs }

        _state.value = _state.value.copy(lines = updated)
    }

    override fun addUserLine(startMs: Long, endMs: Long, text: String, secondaryText: String?) {
        val newId = (_state.value.lines.maxOfOrNull { it.id } ?: 0) + 1
        val newLine = LyricLine(
            id = newId,
            startMs = startMs.coerceAtLeast(0L),
            endMs = endMs.coerceAtLeast(startMs + 500L),
            text = text,
            secondaryText = secondaryText
        )
        val updated = (_state.value.lines + newLine).sortedBy { it.startMs }
        _state.value = _state.value.copy(lines = updated, isUserProvided = true)
    }

    override fun removeLine(lineId: Int) {
        val updated = _state.value.lines.filterNot { it.id == lineId }
        _state.value = _state.value.copy(lines = updated)
    }
}
