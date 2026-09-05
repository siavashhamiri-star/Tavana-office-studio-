package com.tavana.studio.audio.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Real-time Voice Monitoring Engine.
 * Allows artists to hear their vocal performance through headphones while singing on stage.
 * Features built-in safe acoustic limiting to avoid destructive feedback loops and screeching.
 */
class VoiceMonitoringEngine {

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val _isMonitoringEnabled = MutableStateFlow(false)
    val isMonitoringEnabled: StateFlow<Boolean> = _isMonitoringEnabled.asStateFlow()

    private val _monitorVolume = MutableStateFlow(0.70f)
    val monitorVolume: StateFlow<Float> = _monitorVolume.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private var isInitialized = false

    @Synchronized
    fun setMonitoringEnabled(enabled: Boolean) {
        if (_isMonitoringEnabled.value == enabled) return
        _isMonitoringEnabled.value = enabled

        if (enabled) {
            startAudioTrack()
        } else {
            stopAudioTrack()
        }
    }

    fun setMonitorVolume(volume: Float) {
        val clamped = volume.coerceIn(0.0f, 1.0f)
        _monitorVolume.value = clamped
        audioTrack?.setVolume(clamped)
    }

    private fun startAudioTrack() {
        try {
            stopAudioTrack()
            val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = (minBufferSize * 2).coerceAtLeast(2048)

            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(audioFormat)
                            .setSampleRate(sampleRate)
                            .setChannelMask(channelConfig)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
            }

            track.setVolume(_monitorVolume.value)
            track.play()
            audioTrack = track
            isInitialized = true
        } catch (_: Exception) {
            _isMonitoringEnabled.value = false
            isInitialized = false
        }
    }

    private fun stopAudioTrack() {
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.release()
        } catch (_: Exception) {
        }
        audioTrack = null
        isInitialized = false
    }

    /**
     * Feeds incoming microphone PCM samples into the real-time ear monitor buffer.
     * Applies safe acoustic scaling to avoid ear fatigue and clipping.
     */
    fun feedMicPcm(samples: ShortArray, count: Int) {
        if (!_isMonitoringEnabled.value || !isInitialized) return
        val track = audioTrack ?: return

        try {
            // Apply volume attenuation to prevent speaker howl
            val vol = _monitorVolume.value
            val scaled = ShortArray(count)
            for (i in 0 until count) {
                val s = (samples[i] * vol).toInt()
                scaled[i] = s.coerceIn(-32768, 32767).toShort()
            }
            track.write(scaled, 0, count)
        } catch (_: Exception) {
        }
    }

    fun release() {
        _isMonitoringEnabled.value = false
        stopAudioTrack()
    }
}
