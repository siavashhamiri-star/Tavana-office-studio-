package com.tavana.studio.audio.processor

import com.tavana.studio.audio.project.AudioEffectType

data class ReverbParams(
    val roomSize: Float = 0.5f,     // 0.0 to 1.0
    val wetDry: Float = 0.35f,      // 0.0 to 1.0
    val decaySec: Float = 1.5f,     // 0.1 to 10.0 sec
    val preDelayMs: Float = 20.0f,  // 0.0 to 100.0 ms
    val damping: Float = 0.3f       // 0.0 to 1.0
)

enum class ReverbPreset(val displayName: String, val params: ReverbParams) {
    SMALL_ROOM("Small Room", ReverbParams(roomSize = 0.25f, wetDry = 0.2f, decaySec = 0.6f, preDelayMs = 10f, damping = 0.5f)),
    VOCAL_ROOM("Vocal Room", ReverbParams(roomSize = 0.4f, wetDry = 0.28f, decaySec = 1.2f, preDelayMs = 25f, damping = 0.35f)),
    STUDIO("Studio", ReverbParams(roomSize = 0.35f, wetDry = 0.22f, decaySec = 0.9f, preDelayMs = 15f, damping = 0.4f)),
    LARGE_ROOM("Large Room", ReverbParams(roomSize = 0.65f, wetDry = 0.38f, decaySec = 2.2f, preDelayMs = 30f, damping = 0.3f)),
    HALL("Hall", ReverbParams(roomSize = 0.8f, wetDry = 0.45f, decaySec = 3.5f, preDelayMs = 45f, damping = 0.25f)),
    PLATE("Plate", ReverbParams(roomSize = 0.7f, wetDry = 0.4f, decaySec = 2.8f, preDelayMs = 12f, damping = 0.15f)),
    CATHEDRAL("Cathedral", ReverbParams(roomSize = 0.95f, wetDry = 0.55f, decaySec = 5.5f, preDelayMs = 60f, damping = 0.1f)),
    LIVE_STAGE("Live Stage", ReverbParams(roomSize = 0.75f, wetDry = 0.42f, decaySec = 3.0f, preDelayMs = 35f, damping = 0.3f))
}

data class EqPreset(
    val name: String,
    val lowGainDb: Float,
    val midFreqHz: Float,
    val midGainDb: Float,
    val highGainDb: Float
)

object EqPresetCatalog {
    val VOCAL_CLEAN = EqPreset("Vocal Clean", lowGainDb = -3.0f, midFreqHz = 2500f, midGainDb = 1.5f, highGainDb = 2.0f)
    val VOCAL_WARM = EqPreset("Vocal Warm", lowGainDb = 2.5f, midFreqHz = 800f, midGainDb = 1.0f, highGainDb = -1.0f)
    val VOCAL_BRIGHT = EqPreset("Vocal Bright", lowGainDb = -2.0f, midFreqHz = 3500f, midGainDb = 2.5f, highGainDb = 4.0f)
    val VOCAL_PRESENCE = EqPreset("Vocal Presence", lowGainDb = -1.0f, midFreqHz = 4500f, midGainDb = 3.5f, highGainDb = 1.5f)
    val PODCAST_VOICE = EqPreset("Podcast Voice", lowGainDb = -4.0f, midFreqHz = 1800f, midGainDb = 2.0f, highGainDb = 1.0f)
    val RADIO_VOICE = EqPreset("Radio Voice", lowGainDb = 3.0f, midFreqHz = 2200f, midGainDb = 2.5f, highGainDb = -2.0f)
    val SPOKEN_WORD = EqPreset("Spoken Word", lowGainDb = -5.0f, midFreqHz = 1200f, midGainDb = 1.0f, highGainDb = 0.5f)
    val RAP_VOCAL = EqPreset("Rap Vocal", lowGainDb = -2.5f, midFreqHz = 3000f, midGainDb = 3.0f, highGainDb = 3.5f)

    val ALL_PRESETS = listOf(
        VOCAL_CLEAN, VOCAL_WARM, VOCAL_BRIGHT, VOCAL_PRESENCE,
        PODCAST_VOICE, RADIO_VOICE, SPOKEN_WORD, RAP_VOCAL
    )
}

/**
 * Real Algorithmic Delay / Feedback Comb Effect Processor.
 */
class DelayAudioProcessor(
    var delayMs: Float = 250.0f,
    var feedback: Float = 0.35f,
    var mix: Float = 0.3f
) : AudioProcessor {
    override val effectType = AudioEffectType.DELAY
    override val name = "Echo Delay"
    override var isEnabled = true
    override val isImplemented = true

    private var buffer = FloatArray(44100)
    private var writePos = 0

    override fun process(samples: FloatArray, sampleRate: Int, channels: Int): FloatArray {
        if (!isEnabled || mix <= 0f) return samples
        val delaySamples = (delayMs * sampleRate / 1000f).toInt().coerceIn(1, buffer.size - 1)
        val output = FloatArray(samples.size)

        for (i in samples.indices) {
            val s = samples[i]
            var readPos = writePos - delaySamples
            if (readPos < 0) readPos += buffer.size

            val delayed = buffer[readPos]
            buffer[writePos] = (s + delayed * feedback).coerceIn(-1.0f, 1.0f)
            writePos = (writePos + 1) % buffer.size

            output[i] = (s * (1f - mix) + delayed * mix).coerceIn(-1.0f, 1.0f)
        }
        return output
    }
}

/**
 * Algorithmic Freeverb-style Reverb Processor using multi-tap comb filters.
 */
class AlgorithmicReverbAudioProcessor(
    var params: ReverbParams = ReverbParams()
) : AudioProcessor {
    override val effectType = AudioEffectType.REVERB
    override val name = "Studio Reverb"
    override var isEnabled = true
    override val isImplemented = true

    // Comb filter delays for 44.1kHz
    private val combDelays = intArrayOf(1116, 1188, 1277, 1356)
    private val combBuffers = Array(4) { idx -> FloatArray(combDelays[idx]) }
    private val combIndices = IntArray(4)

    override fun process(samples: FloatArray, sampleRate: Int, channels: Int): FloatArray {
        if (!isEnabled || params.wetDry <= 0f) return samples

        val wet = params.wetDry.coerceIn(0f, 1f)
        val dry = 1f - wet
        val feedback = (0.7f * params.roomSize).coerceIn(0.1f, 0.95f)
        val output = FloatArray(samples.size)

        for (i in samples.indices) {
            val inputSample = samples[i]
            var combSum = 0f

            for (c in 0 until 4) {
                val buf = combBuffers[c]
                val idx = combIndices[c]
                val out = buf[idx]
                buf[idx] = (inputSample + out * feedback).coerceIn(-1.0f, 1.0f)
                combIndices[c] = (idx + 1) % buf.size
                combSum += out
            }

            val wetSample = (combSum * 0.25f)
            output[i] = (inputSample * dry + wetSample * wet).coerceIn(-1.0f, 1.0f)
        }
        return output
    }
}
