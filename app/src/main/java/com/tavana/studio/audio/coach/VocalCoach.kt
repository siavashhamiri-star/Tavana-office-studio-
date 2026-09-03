package com.tavana.studio.audio.coach

import com.tavana.studio.audio.analyzer.DeterministicScoreResult
import com.tavana.studio.audio.analyzer.PitchDetectionResult
import com.tavana.studio.audio.analyzer.VocalRangeResult

enum class VocalGrade(val symbol: String, val minScore: Int, val description: String) {
    A_PLUS("A+", 97, "Exceptional vocal control and mastery"),
    A("A", 93, "Outstanding pitch precision and timing"),
    A_MINUS("A-", 90, "Excellent performance with minor variance"),
    B_PLUS("B+", 87, "Very good intonation and strong rhythm"),
    B("B", 83, "Solid delivery with consistent pitch center"),
    B_MINUS("B-", 80, "Good performance with slight drift"),
    C_PLUS("C+", 77, "Above average; watch note attacks"),
    C("C", 73, "Fair; breath support practice recommended"),
    C_MINUS("C-", 70, "Passing; pitch centering needs work"),
    D("D", 60, "Needs improvement on pitch accuracy"),
    F("F", 0, "Significant deviation from reference pitch");

    companion object {
        fun fromScore(score: Int): VocalGrade {
            val clamped = score.coerceIn(0, 100)
            return entries.first { clamped >= it.minScore }
        }
    }
}

data class VocalMetricScore(
    val score: Int,
    val grade: VocalGrade,
    val metricName: String,
    val details: String
)

data class VocalCoachAnalysisReport(
    val overallGrade: VocalGrade,
    val overallScore: Int,
    val pitchAccuracy: VocalMetricScore,
    val noteAccuracy: VocalMetricScore,
    val timingAccuracy: VocalMetricScore,
    val rhythmAccuracy: VocalMetricScore,
    val stabilityScore: VocalMetricScore,
    val consistencyScore: VocalMetricScore,
    val detectedKey: String?,
    val vocalRange: VocalRangeResult?,
    val reasonsSummary: List<String>,
    val coachingRecommendations: List<String>,
    val isDeterministicallyCalculated: Boolean = true
)

interface VocalCoach {
    fun analyzePerformance(
        baseScore: DeterministicScoreResult,
        pitchHistory: List<PitchDetectionResult>,
        vocalRange: VocalRangeResult? = null,
        detectedKey: String? = null
    ): VocalCoachAnalysisReport
}

class DefaultVocalCoach : VocalCoach {
    override fun analyzePerformance(
        baseScore: DeterministicScoreResult,
        pitchHistory: List<PitchDetectionResult>,
        vocalRange: VocalRangeResult?,
        detectedKey: String?
    ): VocalCoachAnalysisReport {
        val pitchScore = baseScore.pitchAccuracy
        val timingScore = baseScore.timingAccuracy
        val stabilityScore = baseScore.stabilityScore

        // Deterministic note transition accuracy
        val voiced = pitchHistory.filter { it.isVoiced && it.confidence >= 0.5f }
        val noteAccuracyScore = if (voiced.isNotEmpty()) {
            val inTuneCount = voiced.count { kotlin.math.abs(it.centsOffset) <= 25f }
            ((inTuneCount.toFloat() / voiced.size) * 100f).toInt().coerceIn(0, 100)
        } else {
            pitchScore
        }

        // Consistency across 4 performance quarters
        val consistencyScore = if (voiced.size >= 8) {
            val quarterSize = voiced.size / 4
            val quartersAvgCents = (0 until 4).map { q ->
                val chunk = voiced.subList(q * quarterSize, (q + 1) * quarterSize)
                chunk.map { kotlin.math.abs(it.centsOffset) }.average()
            }
            val maxDiff = (quartersAvgCents.maxOrNull() ?: 0.0) - (quartersAvgCents.minOrNull() ?: 0.0)
            (100.0 - maxDiff * 2.0).toInt().coerceIn(0, 100)
        } else {
            stabilityScore
        }

        val overall = baseScore.overallScore
        val overallGrade = VocalGrade.fromScore(overall)

        val pitchMetric = VocalMetricScore(
            score = pitchScore,
            grade = VocalGrade.fromScore(pitchScore),
            metricName = "Pitch Accuracy",
            details = "Cent deviation adherence to harmonic reference frequencies."
        )

        val noteMetric = VocalMetricScore(
            score = noteAccuracyScore,
            grade = VocalGrade.fromScore(noteAccuracyScore),
            metricName = "Note Centering",
            details = "Ratio of vocal frames residing within ±25 cents of exact pitch."
        )

        val timingMetric = VocalMetricScore(
            score = timingScore,
            grade = VocalGrade.fromScore(timingScore),
            metricName = "Onset Timing",
            details = "Precision of vocal attacks relative to reference note start timestamps."
        )

        val rhythmMetric = VocalMetricScore(
            score = timingScore,
            grade = VocalGrade.fromScore(timingScore),
            metricName = "Rhythmic Cadence",
            details = "Alignment with metric song subdivisions and bar divisions."
        )

        val stabilityMetric = VocalMetricScore(
            score = stabilityScore,
            grade = VocalGrade.fromScore(stabilityScore),
            metricName = "Sustained Stability",
            details = "Absence of uncontrolled micro-pitch fluctuation on sustained phrases."
        )

        val consistencyMetric = VocalMetricScore(
            score = consistencyScore,
            grade = VocalGrade.fromScore(consistencyScore),
            metricName = "Performance Consistency",
            details = "Maintenance of vocal stamina and intonation across song quarters."
        )

        val reasons = mutableListOf<String>()
        reasons.add("Pitch: ${pitchMetric.grade.symbol} (${pitchMetric.score}%) — ${pitchMetric.details}")
        reasons.add("Timing: ${timingMetric.grade.symbol} (${timingMetric.score}%) — ${timingMetric.details}")
        reasons.add("Stability: ${stabilityMetric.grade.symbol} (${stabilityMetric.score}%) — ${stabilityMetric.details}")
        reasons.add("Overall: ${overallGrade.symbol} (${overall}%) — Grade is calculated mathematically without random scoring.")

        val recommendations = mutableListOf<String>()
        if (pitchScore < 85) recommendations.add("Focus on ear-training intervals and checking sharp/flat drift on entry notes.")
        if (timingScore < 85) recommendations.add("Practice with a metronome or backing track to lock in vocal onsets.")
        if (stabilityScore < 85) recommendations.add("Strengthen diaphragmatic breath support on sustained vowels.")
        if (recommendations.isEmpty()) {
            recommendations.add("Excellent balance and control. Ready for multi-track layering or harmony recording.")
        }

        return VocalCoachAnalysisReport(
            overallGrade = overallGrade,
            overallScore = overall,
            pitchAccuracy = pitchMetric,
            noteAccuracy = noteMetric,
            timingAccuracy = timingMetric,
            rhythmAccuracy = rhythmMetric,
            stabilityScore = stabilityMetric,
            consistencyScore = consistencyMetric,
            detectedKey = detectedKey,
            vocalRange = vocalRange,
            reasonsSummary = reasons,
            coachingRecommendations = recommendations,
            isDeterministicallyCalculated = true
        )
    }
}
