package com.tavana.studio.audio.analyzer

import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class PitchDetectionResult(
    val frequencyHz: Float,
    val midiNote: Int,
    val noteName: String,
    val centsOffset: Float,
    val confidence: Float,
    val isVoiced: Boolean
)

data class VocalRangeResult(
    val lowestFreqHz: Float,
    val lowestNote: String,
    val highestFreqHz: Float,
    val highestNote: String,
    val semitoneSpan: Int
)

data class TargetNote(
    val startMs: Long,
    val endMs: Long,
    val noteName: String,
    val frequencyHz: Float
)

data class UserPitchFrame(
    val timestampMs: Long,
    val frequencyHz: Float,
    val confidence: Float
)

data class DeterministicScoreResult(
    val pitchAccuracy: Int,      // 0 to 100
    val timingAccuracy: Int,     // 0 to 100
    val stabilityScore: Int,     // 0 to 100
    val overallScore: Int,       // 0 to 100
    val framesEvaluated: Int,
    val feedback: String,
    val isCalculatedDeterministically: Boolean = true
)

interface AudioAnalyzer {
    fun detectPitch(samples: FloatArray, sampleRate: Int = 44100): PitchDetectionResult
    fun computeVocalRange(pitchHistory: List<PitchDetectionResult>): VocalRangeResult?
    fun evaluatePerformance(
        targets: List<TargetNote>,
        userFrames: List<UserPitchFrame>,
        toleranceCents: Float = 50.0f
    ): DeterministicScoreResult
}

/**
 * Deterministic Pitch Detector using Autocorrelation method.
 * Detects fundamental frequency f0 within typical vocal range (~80Hz - 1100Hz).
 */
class AutocorrelationPitchDetector : AudioAnalyzer {

    private val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    override fun detectPitch(samples: FloatArray, sampleRate: Int): PitchDetectionResult {
        if (samples.isEmpty()) {
            return unvoicedResult()
        }

        // 1. Compute RMS energy to verify if voiced
        var sumSquares = 0.0
        for (s in samples) {
            sumSquares += s * s
        }
        val rms = sqrt(sumSquares / samples.size)
        if (rms < 0.01) { // below noise threshold
            return unvoicedResult()
        }

        // 2. Autocorrelation over human voice lag range
        val minFreq = 75 // Hz (~D2)
        val maxFreq = 1100 // Hz (~C6)
        val minLag = (sampleRate / maxFreq).coerceAtLeast(1)
        val maxLag = (sampleRate / minFreq).coerceAtMost(samples.size / 2)

        if (maxLag <= minLag) return unvoicedResult()

        var bestLag = -1
        var bestCorrelation = 0.0
        val zeroLagEnergy = sumSquares

        for (lag in minLag..maxLag) {
            var sum = 0.0
            for (i in 0 until (samples.size - lag)) {
                sum += samples[i] * samples[i + lag]
            }

            val normalized = if (zeroLagEnergy > 0) sum / zeroLagEnergy else 0.0
            if (normalized > bestCorrelation) {
                bestCorrelation = normalized
                bestLag = lag
            }
        }

        // Confidence threshold: at least 0.45 correlation for clear voiced periodicity
        if (bestLag > 0 && bestCorrelation > 0.45) {
            val freq = sampleRate.toFloat() / bestLag.toFloat()
            val midiNoteFloat = 69f + 12f * (log2(freq / 440f))
            val midiNote = midiNoteFloat.roundToInt()
            val exactMidiFreq = 440.0 * 2.0.pow((midiNote - 69) / 12.0)
            val centsOffset = 1200f * log2((freq / exactMidiFreq).toFloat())

            val noteIndex = ((midiNote % 12) + 12) % 12
            val octave = (midiNote / 12) - 1
            val name = "${noteNames[noteIndex]}$octave"

            return PitchDetectionResult(
                frequencyHz = freq,
                midiNote = midiNote,
                noteName = name,
                centsOffset = centsOffset,
                confidence = bestCorrelation.toFloat().coerceIn(0f, 1f),
                isVoiced = true
            )
        }

        return unvoicedResult()
    }

    private fun unvoicedResult(): PitchDetectionResult {
        return PitchDetectionResult(
            frequencyHz = 0f,
            midiNote = 0,
            noteName = "--",
            centsOffset = 0f,
            confidence = 0f,
            isVoiced = false
        )
    }

    override fun computeVocalRange(pitchHistory: List<PitchDetectionResult>): VocalRangeResult? {
        val voiced = pitchHistory.filter { it.isVoiced && it.confidence >= 0.6f && it.frequencyHz in 70f..1200f }
        if (voiced.isEmpty()) return null

        val sorted = voiced.sortedBy { it.frequencyHz }
        val lowest = sorted.first()
        val highest = sorted.last()
        val span = abs(highest.midiNote - lowest.midiNote)

        return VocalRangeResult(
            lowestFreqHz = lowest.frequencyHz,
            lowestNote = lowest.noteName,
            highestFreqHz = highest.frequencyHz,
            highestNote = highest.noteName,
            semitoneSpan = span
        )
    }

    /**
     * 100% Deterministic evaluation without any random numbers or fake approximations.
     * Evaluates pitch cent offset, onset timing alignment, and frequency stability.
     */
    override fun evaluatePerformance(
        targets: List<TargetNote>,
        userFrames: List<UserPitchFrame>,
        toleranceCents: Float
    ): DeterministicScoreResult {
        if (targets.isEmpty() || userFrames.isEmpty()) {
            return DeterministicScoreResult(
                pitchAccuracy = 0,
                timingAccuracy = 0,
                stabilityScore = 0,
                overallScore = 0,
                framesEvaluated = 0,
                feedback = "No vocal frames or target reference notes recorded.",
                isCalculatedDeterministically = true
            )
        }

        var pitchErrorSum = 0.0
        var pitchEvaluatedCount = 0
        var timingOnsetsEvaluated = 0
        var timingErrorSumMs = 0.0

        // 1. Evaluate pitch for frames falling within target windows
        for (target in targets) {
            val matchingFrames = userFrames.filter {
                it.timestampMs in target.startMs..target.endMs && it.confidence >= 0.5f && it.frequencyHz > 40f
            }

            if (matchingFrames.isNotEmpty()) {
                timingOnsetsEvaluated++
                val firstFrameTime = matchingFrames.first().timestampMs
                timingErrorSumMs += abs(firstFrameTime - target.startMs)

                for (frame in matchingFrames) {
                    val centDiff = abs(1200.0 * log2(frame.frequencyHz.toDouble() / target.frequencyHz.toDouble()))
                    pitchErrorSum += centDiff
                    pitchEvaluatedCount++
                }
            }
        }

        if (pitchEvaluatedCount == 0) {
            return DeterministicScoreResult(
                pitchAccuracy = 0,
                timingAccuracy = 0,
                stabilityScore = 0,
                overallScore = 0,
                framesEvaluated = 0,
                feedback = "No voiced frames matched the song target notes.",
                isCalculatedDeterministically = true
            )
        }

        // Pitch accuracy: 100% when avg cent error is 0, down to 0% at 2x tolerance
        val avgCentError = pitchErrorSum / pitchEvaluatedCount
        val pitchScore = (100.0 - (avgCentError / toleranceCents) * 50.0).coerceIn(0.0, 100.0).toInt()

        // Timing accuracy: average onset delta compared against 250ms threshold
        val avgOnsetDelta = if (timingOnsetsEvaluated > 0) timingErrorSumMs / timingOnsetsEvaluated else 500.0
        val timingScore = (100.0 - (avgOnsetDelta / 250.0) * 50.0).coerceIn(0.0, 100.0).toInt()

        // Stability: variance of adjacent voiced pitch frames
        var adjacentDeltaSum = 0.0
        var adjacentCount = 0
        for (i in 0 until (userFrames.size - 1)) {
            val f1 = userFrames[i]
            val f2 = userFrames[i + 1]
            if (f1.confidence >= 0.5f && f2.confidence >= 0.5f && f1.frequencyHz > 40f && f2.frequencyHz > 40f) {
                adjacentDeltaSum += abs(1200.0 * log2(f2.frequencyHz.toDouble() / f1.frequencyHz.toDouble()))
                adjacentCount++
            }
        }
        val avgAdjacentDeltaCents = if (adjacentCount > 0) adjacentDeltaSum / adjacentCount else 100.0
        val stabilityScore = (100.0 - (avgAdjacentDeltaCents / 40.0) * 50.0).coerceIn(0.0, 100.0).toInt()

        val overall = ((pitchScore * 0.5) + (timingScore * 0.3) + (stabilityScore * 0.2)).roundToInt().coerceIn(0, 100)

        val feedback = when {
            overall >= 90 -> "Outstanding pitch precision and rhythmic control."
            overall >= 75 -> "Solid vocal delivery with steady pitch and good timing."
            overall >= 60 -> "Good effort. Focus on breath support to steady note transitions."
            else -> "Keep practicing note centers and rhythmic attacks."
        }

        return DeterministicScoreResult(
            pitchAccuracy = pitchScore,
            timingAccuracy = timingScore,
            stabilityScore = stabilityScore,
            overallScore = overall,
            framesEvaluated = pitchEvaluatedCount,
            feedback = feedback,
            isCalculatedDeterministically = true
        )
    }
}
