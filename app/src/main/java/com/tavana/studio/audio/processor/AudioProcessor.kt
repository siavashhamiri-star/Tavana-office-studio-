package com.tavana.studio.audio.processor

import com.tavana.studio.audio.project.AudioEffectType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

sealed class ProcessorStatus {
    object Active : ProcessorStatus()
    object Bypassed : ProcessorStatus()
    data class Unavailable(val reason: String) : ProcessorStatus()
}

interface AudioProcessor {
    val effectType: AudioEffectType
    val name: String
    var isEnabled: Boolean
    val isImplemented: Boolean
    val status: ProcessorStatus
        get() = if (!isImplemented) {
            ProcessorStatus.Unavailable("Planned for future release; not implemented to avoid fake DSP")
        } else if (!isEnabled) {
            ProcessorStatus.Bypassed
        } else {
            ProcessorStatus.Active
        }

    fun process(samples: FloatArray, sampleRate: Int = 44100, channels: Int = 1): FloatArray
}

/**
 * Real mathematical Gain Processor with exact dB to linear conversion.
 */
class GainAudioProcessor(
    initialGainDb: Float = 0.0f
) : AudioProcessor {
    override val effectType = AudioEffectType.GAIN
    override val name = "Studio Gain"
    override var isEnabled = true
    override val isImplemented = true

    var gainDb: Float = initialGainDb
        set(value) {
            field = value.coerceIn(-60f, 24f)
            linearGain = 10.0.pow(field / 20.0).toFloat()
        }

    private var linearGain: Float = 10.0.pow(initialGainDb / 20.0).toFloat()

    override fun process(samples: FloatArray, sampleRate: Int, channels: Int): FloatArray {
        if (!isEnabled || linearGain == 1.0f) return samples
        val output = FloatArray(samples.size)
        for (i in samples.indices) {
            output[i] = (samples[i] * linearGain).coerceIn(-1.0f, 1.0f)
        }
        return output
    }
}

/**
 * Real peak limiter preventing digital clipping over threshold.
 */
class LimiterAudioProcessor(
    var thresholdDb: Float = -0.5f // -0.5 dBFS ceiling
) : AudioProcessor {
    override val effectType = AudioEffectType.LIMITER
    override val name = "Peak Limiter"
    override var isEnabled = true
    override val isImplemented = true

    private val ceilingLinear: Float
        get() = 10.0.pow(thresholdDb / 20.0).toFloat()

    override fun process(samples: FloatArray, sampleRate: Int, channels: Int): FloatArray {
        if (!isEnabled) return samples
        val ceiling = ceilingLinear
        val output = FloatArray(samples.size)
        for (i in samples.indices) {
            val s = samples[i]
            output[i] = when {
                s > ceiling -> ceiling
                s < -ceiling -> -ceiling
                else -> s
            }
        }
        return output
    }
}

/**
 * Real Biquad Filter (Robert Bristow-Johnson Audio EQ Cookbook).
 * Supports Low-pass, High-pass, and Peaking EQ filters.
 */
enum class FilterType {
    PEAKING_EQ,
    LOW_PASS,
    HIGH_PASS
}

class BiquadFilterAudioProcessor(
    var filterType: FilterType = FilterType.PEAKING_EQ,
    var centerFrequencyHz: Float = 1000f,
    var gainDb: Float = 0f,
    var qFactor: Float = 1.0f
) : AudioProcessor {
    override val effectType = AudioEffectType.EQ_PARAMETRIC
    override val name = "Parametric Biquad EQ"
    override var isEnabled = true
    override val isImplemented = true

    // Filter coefficients
    private var b0 = 1.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var a1 = 0.0
    private var a2 = 0.0

    // Filter memory state
    private var x1 = 0.0
    private var x2 = 0.0
    private var y1 = 0.0
    private var y2 = 0.0

    private var configuredSampleRate = 44100

    private fun recomputeCoefficients(sampleRate: Int) {
        configuredSampleRate = sampleRate
        val f0 = centerFrequencyHz.toDouble().coerceIn(20.0, (sampleRate / 2.0) - 100.0)
        val omega = 2.0 * PI * f0 / sampleRate
        val sn = sin(omega)
        val cs = cos(omega)
        val alpha = sn / (2.0 * qFactor.toDouble().coerceAtLeast(0.1))
        val a = 10.0.pow(gainDb.toDouble() / 40.0)

        var b0Raw = 1.0
        var b1Raw = 0.0
        var b2Raw = 0.0
        var a0Raw = 1.0
        var a1Raw = 0.0
        var a2Raw = 0.0

        when (filterType) {
            FilterType.PEAKING_EQ -> {
                b0Raw = 1.0 + alpha * a
                b1Raw = -2.0 * cs
                b2Raw = 1.0 - alpha * a
                a0Raw = 1.0 + alpha / a
                a1Raw = -2.0 * cs
                a2Raw = 1.0 - alpha / a
            }
            FilterType.LOW_PASS -> {
                b0Raw = (1.0 - cs) / 2.0
                b1Raw = 1.0 - cs
                b2Raw = (1.0 - cs) / 2.0
                a0Raw = 1.0 + alpha
                a1Raw = -2.0 * cs
                a2Raw = 1.0 - alpha
            }
            FilterType.HIGH_PASS -> {
                b0Raw = (1.0 + cs) / 2.0
                b1Raw = -(1.0 + cs)
                b2Raw = (1.0 + cs) / 2.0
                a0Raw = 1.0 + alpha
                a1Raw = -2.0 * cs
                a2Raw = 1.0 - alpha
            }
        }

        b0 = b0Raw / a0Raw
        b1 = b1Raw / a0Raw
        b2 = b2Raw / a0Raw
        a1 = a1Raw / a0Raw
        a2 = a2Raw / a0Raw
    }

    override fun process(samples: FloatArray, sampleRate: Int, channels: Int): FloatArray {
        if (!isEnabled || (filterType == FilterType.PEAKING_EQ && gainDb == 0f)) {
            return samples
        }
        recomputeCoefficients(sampleRate)

        val output = FloatArray(samples.size)
        for (i in samples.indices) {
            val x0 = samples[i].toDouble()
            val y0 = b0 * x0 + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2

            x2 = x1
            x1 = x0
            y2 = y1
            y1 = y0

            output[i] = y0.toFloat().coerceIn(-1.0f, 1.0f)
        }
        return output
    }
}

/**
 * Placeholder for complex DSP effects (e.g. algorithmic Reverb, Multiband Compressor, De-esser,
 * AI Noise Reduction) scheduled for future milestones.
 *
 * Strictly adheres to non-fake rule: reports isImplemented = false and passes audio unaltered.
 */
class PlannedAudioProcessor(
    override val effectType: AudioEffectType,
    override val name: String
) : AudioProcessor {
    override var isEnabled: Boolean = false
    override val isImplemented: Boolean = false

    override fun process(samples: FloatArray, sampleRate: Int, channels: Int): FloatArray {
        // Transparent passthrough - no fake simulation
        return samples
    }
}
