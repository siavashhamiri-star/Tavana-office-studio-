package com.tavana.studio.audio.mixer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class MixerChannelState(
    val trackId: String,
    val name: String,
    val volume: Float = 1.0f,
    val pan: Float = 0.0f, // -1.0f (Left) to +1.0f (Right)
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val busGroup: String = "MASTER"
)

data class StereoGain(
    val leftGain: Float,
    val rightGain: Float,
    val isAudible: Boolean
)

interface AudioMixer {
    val channelStates: StateFlow<Map<String, MixerChannelState>>
    val masterVolume: StateFlow<Float>

    fun setMasterVolume(volume: Float)
    fun addChannel(trackId: String, name: String, volume: Float = 1.0f, pan: Float = 0.0f)
    fun removeChannel(trackId: String)
    fun setTrackVolume(trackId: String, volume: Float)
    fun setTrackPan(trackId: String, pan: Float)
    fun setTrackMute(trackId: String, isMuted: Boolean)
    fun setTrackSolo(trackId: String, isSolo: Boolean)
    fun calculateStereoGain(trackId: String): StereoGain
    fun resetMixer()
}

class DefaultAudioMixer : AudioMixer {
    private val _channelStates = MutableStateFlow<Map<String, MixerChannelState>>(emptyMap())
    override val channelStates: StateFlow<Map<String, MixerChannelState>> = _channelStates.asStateFlow()

    private val _masterVolume = MutableStateFlow(1.0f)
    override val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()

    override fun setMasterVolume(volume: Float) {
        _masterVolume.value = volume.coerceIn(0.0f, 1.0f)
    }

    override fun addChannel(trackId: String, name: String, volume: Float, pan: Float) {
        val channel = MixerChannelState(
            trackId = trackId,
            name = name,
            volume = volume.coerceIn(0f, 1f),
            pan = pan.coerceIn(-1f, 1f)
        )
        _channelStates.value = _channelStates.value + (trackId to channel)
    }

    override fun removeChannel(trackId: String) {
        _channelStates.value = _channelStates.value - trackId
    }

    override fun setTrackVolume(trackId: String, volume: Float) {
        val current = _channelStates.value[trackId] ?: return
        val updated = current.copy(volume = volume.coerceIn(0f, 1f))
        _channelStates.value = _channelStates.value + (trackId to updated)
    }

    override fun setTrackPan(trackId: String, pan: Float) {
        val current = _channelStates.value[trackId] ?: return
        val updated = current.copy(pan = pan.coerceIn(-1f, 1f))
        _channelStates.value = _channelStates.value + (trackId to updated)
    }

    override fun setTrackMute(trackId: String, isMuted: Boolean) {
        val current = _channelStates.value[trackId] ?: return
        val updated = current.copy(isMuted = isMuted)
        _channelStates.value = _channelStates.value + (trackId to updated)
    }

    override fun setTrackSolo(trackId: String, isSolo: Boolean) {
        val current = _channelStates.value[trackId] ?: return
        val updated = current.copy(isSolo = isSolo)
        _channelStates.value = _channelStates.value + (trackId to updated)
    }

    /**
     * Computes the deterministic stereo gain for a given channel using constant power pan law:
     * - If ANY channel in the mixer has solo == true, only soloed, non-muted channels are audible.
     * - If NO channels have solo == true, all non-muted channels are audible.
     * - Multiplies by master volume.
     */
    override fun calculateStereoGain(trackId: String): StereoGain {
        val channels = _channelStates.value
        val channel = channels[trackId] ?: return StereoGain(0f, 0f, false)

        val anySoloActive = channels.values.any { it.isSolo }

        val isAudible = if (anySoloActive) {
            channel.isSolo && !channel.isMuted
        } else {
            !channel.isMuted
        }

        if (!isAudible) {
            return StereoGain(0f, 0f, false)
        }

        val baseVolume = channel.volume * _masterVolume.value

        // Constant-power panning (equal energy)
        // angle ranges from 0 (hard left) to PI/2 (hard right)
        val normalizedPan = (channel.pan.coerceIn(-1f, 1f) + 1f) / 2f
        val angle = normalizedPan * (PI / 2.0)
        val leftMultiplier = cos(angle).toFloat()
        val rightMultiplier = sin(angle).toFloat()

        return StereoGain(
            leftGain = baseVolume * leftMultiplier,
            rightGain = baseVolume * rightMultiplier,
            isAudible = true
        )
    }

    override fun resetMixer() {
        _channelStates.value = emptyMap()
        _masterVolume.value = 1.0f
    }
}
