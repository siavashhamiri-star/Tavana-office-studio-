package com.tavana.studio.audio.voice

import com.tavana.studio.audio.analyzer.DeterministicScoreResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

data class VocalRange(
    val lowestNote: String,
    val lowestFreqHz: Float,
    val highestNote: String,
    val highestFreqHz: Float,
    val semitoneSpan: Int
)

data class AccuracyMetricRecord(
    val timestampMs: Long,
    val referenceId: String,
    val pitchAccuracy: Int,
    val timingAccuracy: Int,
    val stabilityScore: Int
)

data class VoiceProfile(
    val id: String,
    val identityId: String,
    val measuredVocalRange: VocalRange? = null,
    val comfortableRange: VocalRange? = null,
    val preferredKeys: List<String> = emptyList(),
    val accuracyHistory: List<AccuracyMetricRecord> = emptyList(),
    val averageTimingAccuracy: Float? = null,
    val averagePitchStability: Float? = null,
    val timbreCharacteristics: String? = null,
    val preferredGainDb: Float? = null,
    val preferredEqSettings: Map<String, Float>? = null,
    val preferredCompressionSettings: Map<String, Float>? = null,
    val preferredReverbDecaySec: Float? = null,
    val preferredRoomSize: Float? = null
)

interface VoiceProfileRepository {
    fun getProfileFlow(identityId: String): Flow<VoiceProfile>
    suspend fun getProfile(identityId: String): VoiceProfile
    suspend fun saveProfile(profile: VoiceProfile)
    suspend fun recordPerformanceMeasurement(
        identityId: String,
        referenceId: String,
        scoreResult: DeterministicScoreResult
    ): VoiceProfile
    suspend fun updateMeasuredRange(identityId: String, range: VocalRange): VoiceProfile
}

class InMemoryVoiceProfileRepository : VoiceProfileRepository {
    private val _profiles = MutableStateFlow<Map<String, VoiceProfile>>(emptyMap())

    override fun getProfileFlow(identityId: String): Flow<VoiceProfile> {
        return _profiles.asStateFlow().map { map ->
            map[identityId] ?: VoiceProfile(id = "profile_$identityId", identityId = identityId)
        }
    }

    override suspend fun getProfile(identityId: String): VoiceProfile {
        return _profiles.value[identityId] ?: VoiceProfile(id = "profile_$identityId", identityId = identityId)
    }

    override suspend fun saveProfile(profile: VoiceProfile) {
        _profiles.value = _profiles.value + (profile.identityId to profile)
    }

    override suspend fun recordPerformanceMeasurement(
        identityId: String,
        referenceId: String,
        scoreResult: DeterministicScoreResult
    ): VoiceProfile {
        val current = getProfile(identityId)
        val newRecord = AccuracyMetricRecord(
            timestampMs = System.currentTimeMillis(),
            referenceId = referenceId,
            pitchAccuracy = scoreResult.pitchAccuracy,
            timingAccuracy = scoreResult.timingAccuracy,
            stabilityScore = scoreResult.stabilityScore
        )
        val updatedHistory = current.accuracyHistory + newRecord
        val avgTiming = updatedHistory.map { it.timingAccuracy }.average().toFloat()
        val avgStability = updatedHistory.map { it.stabilityScore }.average().toFloat()

        val updated = current.copy(
            accuracyHistory = updatedHistory,
            averageTimingAccuracy = avgTiming,
            averagePitchStability = avgStability
        )
        saveProfile(updated)
        return updated
    }

    override suspend fun updateMeasuredRange(identityId: String, range: VocalRange): VoiceProfile {
        val current = getProfile(identityId)
        val updated = current.copy(measuredVocalRange = range)
        saveProfile(updated)
        return updated
    }
}
