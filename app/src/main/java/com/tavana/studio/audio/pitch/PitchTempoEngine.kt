package com.tavana.studio.audio.pitch

import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

data class KeyDetectionResult(
    val rootNote: String,
    val mode: String, // "Major" or "Minor"
    val confidence: Float,
    val noteDistribution: Map<String, Float>
)

data class TranspositionResult(
    val originalKey: String,
    val targetKey: String,
    val semitoneShift: Int,
    val ratio: Float
)

object MusicalPitchHelper {
    private val NOTES = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    fun semitoneToRatio(semitones: Int): Float {
        return 2.0.pow(semitones.toDouble() / 12.0).toFloat()
    }

    fun transposeKey(rootNote: String, semitoneShift: Int): String {
        val cleanNote = rootNote.trim().uppercase()
        val idx = NOTES.indexOfFirst { it.equals(cleanNote, ignoreCase = true) }
        if (idx == -1) return rootNote
        val newIdx = ((idx + semitoneShift) % 12 + 12) % 12
        return NOTES[newIdx]
    }

    /**
     * Pitch shifts audio samples using sinc / linear resampling for testing and offline transformation.
     */
    fun resamplePitchShift(samples: FloatArray, semitones: Int): FloatArray {
        if (semitones == 0 || samples.isEmpty()) return samples
        val ratio = semitoneToRatio(semitones)
        val newLength = (samples.size / ratio).toInt().coerceAtLeast(1)
        val result = FloatArray(newLength)

        for (i in 0 until newLength) {
            val srcIndex = i * ratio
            val srcFloor = srcIndex.toInt()
            val frac = srcIndex - srcFloor
            val s0 = if (srcFloor < samples.size) samples[srcFloor] else 0f
            val s1 = if (srcFloor + 1 < samples.size) samples[srcFloor + 1] else s0
            result[i] = (s0 * (1f - frac) + s1 * frac).coerceIn(-1.0f, 1.0f)
        }
        return result
    }

    /**
     * Krumhansl-Schmuckler based key detection heuristic from pitch distribution.
     */
    fun detectKeyFromPitches(pitchFrequenciesHz: List<Float>): KeyDetectionResult {
        if (pitchFrequenciesHz.isEmpty()) {
            return KeyDetectionResult(rootNote = "C", mode = "Major", confidence = 0.0f, noteDistribution = emptyMap())
        }

        val noteCounts = IntArray(12)
        for (freq in pitchFrequenciesHz) {
            if (freq in 60f..1500f) {
                val midi = (69f + 12f * log2(freq / 440f)).roundToInt()
                val noteIdx = ((midi % 12) + 12) % 12
                noteCounts[noteIdx]++
            }
        }

        val total = noteCounts.sum().coerceAtLeast(1)
        val distribution = mutableMapOf<String, Float>()
        for (i in 0 until 12) {
            distribution[NOTES[i]] = noteCounts[i].toFloat() / total
        }

        // Major profile weights (Krumhansl)
        val majorProfile = doubleArrayOf(6.35, 2.23, 3.48, 2.33, 4.38, 4.09, 2.52, 5.19, 2.39, 3.66, 2.29, 2.88)
        var bestCorrelation = -1.0
        var bestRoot = 0

        for (shift in 0 until 12) {
            var correlation = 0.0
            for (i in 0 until 12) {
                val count = noteCounts[(shift + i) % 12]
                correlation += count * majorProfile[i]
            }
            if (correlation > bestCorrelation) {
                bestCorrelation = correlation
                bestRoot = shift
            }
        }

        val rootName = NOTES[bestRoot]
        return KeyDetectionResult(
            rootNote = rootName,
            mode = "Major",
            confidence = (bestCorrelation / (total * 6.35)).toFloat().coerceIn(0.1f, 1.0f),
            noteDistribution = distribution
        )
    }
}
