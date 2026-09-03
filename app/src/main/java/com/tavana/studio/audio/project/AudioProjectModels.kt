package com.tavana.studio.audio.project

/**
 * Clean, extensible Audio Project Domain Model for TAVANA Studio.
 * Designed to interface with the future shared Media Timeline without premature video overhead.
 */

enum class AudioEffectType {
    GAIN,
    LIMITER,
    EQ_PARAMETRIC,
    COMPRESSOR,
    REVERB,
    DELAY,
    DE_ESSER,
    NOISE_REDUCTION,
    CHORUS,
    GATE
}

data class AudioEffect(
    val id: String,
    val name: String,
    val type: AudioEffectType,
    val isEnabled: Boolean = true,
    val parameters: Map<String, Float> = emptyMap()
)

data class AudioTake(
    val id: String,
    val trackId: String,
    val filePath: String,
    val durationMs: Long,
    val recordedAtMs: Long = System.currentTimeMillis(),
    val sampleRateHz: Int = 44100,
    val channels: Int = 1,
    val bitDepth: Int = 16,
    val rmsLevel: Float = 0f,
    val isSelected: Boolean = false,
    val note: String? = null
)

data class AudioClip(
    val id: String,
    val trackId: String,
    val takeId: String,
    val startOffsetMs: Long = 0L,
    val clipStartMs: Long = 0L,
    val durationMs: Long,
    val volume: Float = 1.0f,
    val pan: Float = 0.0f
)

enum class TrackType {
    VOCAL_LEAD,
    VOCAL_BACKING,
    INSTRUMENTAL,
    VOCAL_GUIDE,
    BUS,
    MASTER,
    HARMONY,
    SFX,
    JINGLE,
    PODCAST_HOST,
    PODCAST_GUEST,
    BACKGROUND_MUSIC
}

data class AudioTrack(
    val id: String,
    val name: String,
    val type: TrackType = TrackType.VOCAL_LEAD,
    val volume: Float = 1.0f,
    val pan: Float = 0.0f,
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val clips: List<AudioClip> = emptyList(),
    val effects: List<AudioEffect> = emptyList()
)

data class ProjectMetadata(
    val id: String,
    val title: String,
    val description: String = "",
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis(),
    val ownerIdentityId: String = "user_default",
    val workspaceId: String = "workspace_personal",
    val tempoBpm: Int = 120,
    val timeSignatureNumerator: Int = 4,
    val timeSignatureDenominator: Int = 4,
    val sampleRateHz: Int = 44100,
    val bitDepth: Int = 16,
    val channels: Int = 2
)

data class AudioProject(
    val id: String,
    val metadata: ProjectMetadata,
    val tracks: List<AudioTrack> = emptyList(),
    val masterVolume: Float = 1.0f,
    val masterEffects: List<AudioEffect> = emptyList()
)
