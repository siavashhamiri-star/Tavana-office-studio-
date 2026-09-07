package com.tavana.studio.audio.diagnostics

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.sin

/**
 * Diagnostic Audio Status and Route details.
 */
data class AudioRouteInfo(
    val isMicPermissionGranted: Boolean = false,
    val isMicMutedInSystem: Boolean = false,
    val isHeadsetConnected: Boolean = false,
    val isBluetoothAudioConnected: Boolean = false,
    val activeOutputDeviceName: String = "بلندگوی اصلی گوشی (Built-in Speaker)",
    val activeOutputDeviceType: String = "SPEAKER",
    val mediaVolumePercent: Int = 0,
    val isMediaMuted: Boolean = false,
    val ringerMode: String = "عادی (Normal)"
)

/**
 * Result of the 3-second live mic loopback diagnostic test.
 */
sealed class MicDiagnosticTestState {
    object Idle : MicDiagnosticTestState()
    data class Recording(val secondsLeft: Int, val currentAmplitude: Float) : MicDiagnosticTestState()
    object Processing : MicDiagnosticTestState()
    data class PlayingBack(val secondsLeft: Int) : MicDiagnosticTestState()
    data class Success(val peakLevelDb: Float, val message: String) : MicDiagnosticTestState()
    data class Failed(val reason: String) : MicDiagnosticTestState()
}

/**
 * TAVANA Audio Diagnostics & Hardware Monitoring Manager.
 * Solves audio route confusion, mic/headset absence, speaker volume issues,
 * and provides instant self-tests and guided root-cause explanations.
 */
class AudioDiagnosticsManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _routeInfo = MutableStateFlow(AudioRouteInfo())
    val routeInfo: StateFlow<AudioRouteInfo> = _routeInfo.asStateFlow()

    private val _micTestState = MutableStateFlow<MicDiagnosticTestState>(MicDiagnosticTestState.Idle)
    val micTestState: StateFlow<MicDiagnosticTestState> = _micTestState.asStateFlow()

    private val _isSpeakerTestPlaying = MutableStateFlow(false)
    val isSpeakerTestPlaying: StateFlow<Boolean> = _isSpeakerTestPlaying.asStateFlow()

    private var micTestJob: Job? = null
    private var speakerTestJob: Job? = null

    init {
        refreshAudioStatus()
    }

    /**
     * Inspects active audio hardware route, volume levels, and permission status.
     */
    fun refreshAudioStatus() {
        val hasMicPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val isMicMuted = try {
            audioManager.isMicrophoneMute
        } catch (_: Exception) {
            false
        }

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val volumePercent = ((currentVolume.toFloat() / maxVolume) * 100).toInt()
        val isMuted = currentVolume == 0

        var isWiredHeadset = false
        var isBluetooth = false
        var outputName = "بلندگوی اصلی گوشی"
        var outputType = "SPEAKER"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                when (device.type) {
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_USB_HEADSET -> {
                        isWiredHeadset = true
                        outputName = "هدفون / هندزفری سیمی (${device.productName.ifBlank { "جک صوتی / USB-C" }})"
                        outputType = "WIRED_HEADSET"
                    }
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_BLE_HEADSET -> {
                        isBluetooth = true
                        outputName = "هندزفری یا اسپیکر بلوتوث (${device.productName.ifBlank { "Bluetooth Audio" }})"
                        outputType = "BLUETOOTH"
                    }
                }
            }
        } else {
            @Suppress("DEPRECATION")
            isWiredHeadset = audioManager.isWiredHeadsetOn
            @Suppress("DEPRECATION")
            isBluetooth = audioManager.isBluetoothA2dpOn
            if (isWiredHeadset) {
                outputName = "هدفون سیمی"
                outputType = "WIRED_HEADSET"
            } else if (isBluetooth) {
                outputName = "هندزفری بلوتوث"
                outputType = "BLUETOOTH"
            }
        }

        val ringerText = when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> "کاملاً بی‌صدا (Silent)"
            AudioManager.RINGER_MODE_VIBRATE -> "لرزش (Vibrate)"
            else -> "عادی (Normal)"
        }

        _routeInfo.value = AudioRouteInfo(
            isMicPermissionGranted = hasMicPermission,
            isMicMutedInSystem = isMicMuted,
            isHeadsetConnected = isWiredHeadset,
            isBluetoothAudioConnected = isBluetooth,
            activeOutputDeviceName = outputName,
            activeOutputDeviceType = outputType,
            mediaVolumePercent = volumePercent,
            isMediaMuted = isMuted,
            ringerMode = ringerText
        )
    }

    /**
     * Plays a pleasant 3-note harmonic chime (C5 - E5 - G5) through the active output device
     * to immediately test whether headphones/speakers and media volume are functional.
     */
    fun playSpeakerAndHeadsetTestChime() {
        if (_isSpeakerTestPlaying.value) return
        speakerTestJob?.cancel()

        speakerTestJob = scope.launch(Dispatchers.IO) {
            _isSpeakerTestPlaying.value = true
            try {
                // Ensure volume is at least audible if it was muted
                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (currentVol == 0) {
                    val target = (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.6f).toInt()
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target.coerceAtLeast(1), 0)
                    refreshAudioStatus()
                }

                val sampleRate = 44100
                val chordFrequencies = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6 arpeggio
                val noteDurationSec = 0.28
                val totalSamplesPerNote = (sampleRate * noteDurationSec).toInt()

                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(minBuf.coerceAtLeast(4096))
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        minBuf.coerceAtLeast(4096),
                        AudioTrack.MODE_STREAM
                    )
                }

                audioTrack.setVolume(1.0f)
                audioTrack.play()

                for (freq in chordFrequencies) {
                    val buffer = ShortArray(totalSamplesPerNote)
                    for (i in 0 until totalSamplesPerNote) {
                        val t = i.toDouble() / sampleRate
                        val wave = sin(2.0 * PI * freq * t) + 0.3 * sin(2.0 * PI * (freq * 2.0) * t)
                        // Smooth envelope to prevent clicks
                        val env = when {
                            i < 200 -> i / 200.0
                            i > totalSamplesPerNote - 600 -> (totalSamplesPerNote - i) / 600.0
                            else -> 1.0
                        }
                        buffer[i] = (wave * 0.7 * env * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
                    }
                    audioTrack.write(buffer, 0, buffer.size)
                }

                delay(300)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
            } finally {
                _isSpeakerTestPlaying.value = false
                refreshAudioStatus()
            }
        }
    }

    /**
     * Interactive 3-second live microphone diagnostic test:
     * Records PCM audio from mic for 3 seconds, displays countdown,
     * then immediately plays it back through the current audio output.
     */
    fun startMicDiagnosticTest() {
        if (!ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO).equals(PackageManager.PERMISSION_GRANTED)) {
            _micTestState.value = MicDiagnosticTestState.Failed("مجوز دسترسی به میکروفون داده نشده است. لطفاً ابتدا به میکروفون دسترسی بدهید.")
            return
        }

        micTestJob?.cancel()
        micTestJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBuf = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = minBuf.coerceAtLeast(2048)

            var audioRecord: AudioRecord? = null
            val recordedData = ByteArrayOutputStream()

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize * 2
                )

                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    _micTestState.value = MicDiagnosticTestState.Failed("سخت‌افزار میکروفون در حال حاضر توسط برنامه دیگری مشغول است.")
                    return@launch
                }

                audioRecord.startRecording()

                val buffer = ShortArray(1024)
                val totalTestDurationMs = 3000L
                val startTime = System.currentTimeMillis()
                var maxAmplitude = 0f

                // 3-second recording loop
                while (System.currentTimeMillis() - startTime < totalTestDurationMs) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        for (i in 0 until read) {
                            val amp = Math.abs(buffer[i].toFloat()) / 32768.0f
                            if (amp > maxAmplitude) maxAmplitude = amp

                            // Write raw bytes
                            val byte1 = (buffer[i].toInt() and 0xFF).toByte()
                            val byte2 = ((buffer[i].toInt() shr 8) and 0xFF).toByte()
                            recordedData.write(byte1.toInt())
                            recordedData.write(byte2.toInt())
                        }
                    }

                    val elapsed = System.currentTimeMillis() - startTime
                    val secondsLeft = (3 - (elapsed / 1000)).toInt().coerceIn(1, 3)
                    _micTestState.value = MicDiagnosticTestState.Recording(secondsLeft, maxAmplitude)
                    delay(50)
                }

                audioRecord.stop()
                audioRecord.release()
                audioRecord = null

                _micTestState.value = MicDiagnosticTestState.Processing
                delay(200)

                // Now play back the recorded 3 seconds of user's voice
                val audioBytes = recordedData.toByteArray()
                if (audioBytes.isEmpty() || maxAmplitude < 0.01f) {
                    _micTestState.value = MicDiagnosticTestState.Failed("صدایی از میکروفون دریافت نشد! لطفاً مطمئن شوید میکروفون گوشی مسدود نیست و صدای خود را بالا ببرید.")
                    return@launch
                }

                val outMinBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val playbackTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(outMinBuf.coerceAtLeast(4096))
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        outMinBuf.coerceAtLeast(4096),
                        AudioTrack.MODE_STREAM
                    )
                }

                playbackTrack.setVolume(1.0f)
                playbackTrack.play()

                val playbackStartTime = System.currentTimeMillis()
                val playbackDurationMs = 3000L

                scope.launch {
                    while (System.currentTimeMillis() - playbackStartTime < playbackDurationMs) {
                        val left = (3 - ((System.currentTimeMillis() - playbackStartTime) / 1000)).toInt().coerceIn(1, 3)
                        _micTestState.value = MicDiagnosticTestState.PlayingBack(left)
                        delay(200)
                    }
                }

                playbackTrack.write(audioBytes, 0, audioBytes.size)
                delay(300)
                playbackTrack.stop()
                playbackTrack.release()

                val peakDb = (20 * Math.log10(maxAmplitude.toDouble().coerceAtLeast(0.0001))).toFloat()
                _micTestState.value = MicDiagnosticTestState.Success(
                    peakLevelDb = peakDb,
                    message = "میکروفون با موفقیت صدا را دریافت و از بلندگو/هدست پخش کرد! سخت‌افزار صوتی سالم است."
                )
            } catch (e: Exception) {
                _micTestState.value = MicDiagnosticTestState.Failed("خطا در تست میکروفون: ${e.message ?: "نامشخص"}")
            } finally {
                audioRecord?.release()
            }
        }
    }

    fun resetMicTest() {
        micTestJob?.cancel()
        _micTestState.value = MicDiagnosticTestState.Idle
    }

    /**
     * Sets media volume to a comfortable audible level (e.g. 80%).
     */
    fun boostMediaVolume() {
        try {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = (max * 0.85f).toInt().coerceAtLeast(1)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
            refreshAudioStatus()
        } catch (_: Exception) {
        }
    }

    /**
     * Opens Android System Sound Settings.
     */
    fun openSystemSoundSettings() {
        try {
            val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    /**
     * Opens Application Details Settings to allow granting permissions.
     */
    fun openAppPermissionSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }
}
