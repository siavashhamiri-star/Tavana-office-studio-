package com.tavana.studio.audio.separation

enum class SeparationMode {
    VOCAL_ONLY,
    INSTRUMENTAL_ONLY,
    KARAOKE_VOCAL_REMOVED,
    FULL_MIX
}

sealed class SeparationResult {
    data class Success(
        val originalAudioPath: String,
        val vocalTrackPath: String?,
        val instrumentalTrackPath: String?,
        val mode: SeparationMode
    ) : SeparationResult()

    data class Blocked(
        val reason: String,
        val requiredDependency: String,
        val safeNextStep: String
    ) : SeparationResult()
}

/**
 * Honest, non-fake Vocal / Instrumental Separation engine.
 *
 * Notice: High quality neural stem separation (e.g. Spleeter / Demucs / MDX-Net ONNX) requires
 * an on-device ONNX runtime model or an external separation API service.
 * In compliance with "Do not fake an AI separation engine", this module reports dependency requirements.
 */
interface StemSeparationEngine {
    val isSeparationEngineAvailable: Boolean
    fun requestSeparation(audioPath: String, mode: SeparationMode): SeparationResult
}

class DefaultStemSeparationEngine : StemSeparationEngine {
    override val isSeparationEngineAvailable: Boolean = false

    override fun requestSeparation(audioPath: String, mode: SeparationMode): SeparationResult {
        return SeparationResult.Blocked(
            reason = "Local neural stem separation requires an ONNX model runtime (Demucs/MDX) or an external separation API.",
            requiredDependency = "ONNX Runtime Mobile (ai.onnxruntime:onnxruntime-android) or Firebase ML Audio Service",
            safeNextStep = "Configure ONNX Runtime model weights (demucs_v4.onnx) or connect an external AI Separation endpoint in Settings."
        )
    }
}
