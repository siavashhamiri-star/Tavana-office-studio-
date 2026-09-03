package com.tavana.studio.audio.engine

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import kotlin.math.sqrt

enum class RecordingEngineState {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED,
    ERROR
}

data class AudioTakeResult(
    val filePath: String,
    val durationMs: Long,
    val sampleRateHz: Int,
    val channels: Int,
    val bitDepth: Int,
    val averageRms: Float
)

interface AudioRecordingEngine {
    val state: StateFlow<RecordingEngineState>
    val audioLevel: StateFlow<Float>
    val recordingDurationMs: StateFlow<Long>
    val errorMessage: StateFlow<String?>

    fun startRecording(outputFile: File): Result<Unit>
    fun pauseRecording(): Result<Unit>
    fun resumeRecording(): Result<Unit>
    fun stopRecording(): Result<AudioTakeResult>
    fun cancelRecording(): Result<Unit>
    fun release()
}

/**
 * Production Android AudioRecordingEngine utilizing real AudioRecord PCM sampling
 * and real RMS amplitude calculations.
 */
class AndroidAudioRecordingEngine(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : AudioRecordingEngine {

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val _state = MutableStateFlow(RecordingEngineState.IDLE)
    override val state: StateFlow<RecordingEngineState> = _state.asStateFlow()

    private val _audioLevel = MutableStateFlow(0.0f)
    override val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    override val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var currentOutputFile: File? = null
    private var startTimeMs: Long = 0L
    private var accumulatedDurationMs: Long = 0L
    private var lastResumeTimeMs: Long = 0L
    private var totalSamplesRead: Long = 0L
    private var totalRmsSum: Double = 0.0
    private var rmsCount: Long = 0L

    @Volatile
    private var isRecordingActive: Boolean = false

    @Volatile
    private var isPausedInternal: Boolean = false

    @SuppressLint("MissingPermission")
    override fun startRecording(outputFile: File): Result<Unit> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            val err = "RECORD_AUDIO permission not granted"
            _errorMessage.value = err
            _state.value = RecordingEngineState.ERROR
            return Result.failure(SecurityException(err))
        }

        if (_state.value == RecordingEngineState.RECORDING) {
            return Result.failure(IllegalStateException("Already recording"))
        }

        try {
            val minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = minBufSize.coerceAtLeast(4096)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                val err = "AudioRecord initialization failed"
                _errorMessage.value = err
                _state.value = RecordingEngineState.ERROR
                audioRecord?.release()
                audioRecord = null
                return Result.failure(IllegalStateException(err))
            }

            outputFile.parentFile?.mkdirs()
            if (outputFile.exists()) {
                outputFile.delete()
            }
            outputFile.createNewFile()

            // Pre-write empty 44-byte WAV header placeholder
            RandomAccessFile(outputFile, "rw").use { raf ->
                raf.write(ByteArray(44))
            }

            currentOutputFile = outputFile
            accumulatedDurationMs = 0L
            totalSamplesRead = 0L
            totalRmsSum = 0.0
            rmsCount = 0L
            startTimeMs = System.currentTimeMillis()
            lastResumeTimeMs = startTimeMs
            _recordingDurationMs.value = 0L
            _errorMessage.value = null

            audioRecord?.startRecording()
            isRecordingActive = true
            isPausedInternal = false
            _state.value = RecordingEngineState.RECORDING

            recordingJob = scope.launch {
                val audioBuffer = ShortArray(1024)
                val byteBuffer = ByteArray(audioBuffer.size * 2)

                FileOutputStream(outputFile, true).use { fos ->
                    while (isActive && isRecordingActive) {
                        if (isPausedInternal) {
                            _audioLevel.value = 0f
                            kotlinx.coroutines.delay(50)
                            continue
                        }

                        val readCount = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: -1
                        if (readCount > 0) {
                            totalSamplesRead += readCount

                            // Calculate real RMS level
                            var sumSquares = 0.0
                            for (i in 0 until readCount) {
                                val sample = audioBuffer[i]
                                sumSquares += sample * sample

                                // Convert Short to Little Endian bytes
                                val byteIndex = i * 2
                                byteBuffer[byteIndex] = (sample.toInt() and 0xFF).toByte()
                                byteBuffer[byteIndex + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
                            }

                            val rms = sqrt(sumSquares / readCount).toFloat()
                            val normalizedRms = (rms / 32768.0f).coerceIn(0.0f, 1.0f)
                            _audioLevel.value = normalizedRms
                            totalRmsSum += normalizedRms
                            rmsCount++

                            fos.write(byteBuffer, 0, readCount * 2)

                            val now = System.currentTimeMillis()
                            _recordingDurationMs.value = accumulatedDurationMs + (now - lastResumeTimeMs)
                        } else {
                            kotlinx.coroutines.delay(10)
                        }
                    }
                }
            }

            return Result.success(Unit)
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage ?: "Unknown recording error"
            _state.value = RecordingEngineState.ERROR
            release()
            return Result.failure(e)
        }
    }

    override fun pauseRecording(): Result<Unit> {
        if (_state.value != RecordingEngineState.RECORDING) {
            return Result.failure(IllegalStateException("Cannot pause when not recording"))
        }
        isPausedInternal = true
        accumulatedDurationMs += System.currentTimeMillis() - lastResumeTimeMs
        _state.value = RecordingEngineState.PAUSED
        _audioLevel.value = 0f
        return Result.success(Unit)
    }

    override fun resumeRecording(): Result<Unit> {
        if (_state.value != RecordingEngineState.PAUSED) {
            return Result.failure(IllegalStateException("Cannot resume when not paused"))
        }
        lastResumeTimeMs = System.currentTimeMillis()
        isPausedInternal = false
        _state.value = RecordingEngineState.RECORDING
        return Result.success(Unit)
    }

    override fun stopRecording(): Result<AudioTakeResult> {
        if (_state.value != RecordingEngineState.RECORDING && _state.value != RecordingEngineState.PAUSED) {
            return Result.failure(IllegalStateException("Recording is not running"))
        }

        isRecordingActive = false
        isPausedInternal = false
        recordingJob?.cancel()

        try {
            audioRecord?.stop()
        } catch (_: Exception) {
        }

        val file = currentOutputFile
        if (file != null && file.exists()) {
            WavWriter.finalizeWav(
                file = file,
                sampleRate = sampleRate,
                channels = 1,
                bitDepth = 16
            )
        }

        val finalDuration = _recordingDurationMs.value
        val avgRms = if (rmsCount > 0) (totalRmsSum / rmsCount).toFloat() else 0f

        _state.value = RecordingEngineState.STOPPED
        _audioLevel.value = 0f

        val result = AudioTakeResult(
            filePath = file?.absolutePath.orEmpty(),
            durationMs = finalDuration,
            sampleRateHz = sampleRate,
            channels = 1,
            bitDepth = 16,
            averageRms = avgRms
        )

        release()
        _state.value = RecordingEngineState.IDLE
        return Result.success(result)
    }

    override fun cancelRecording(): Result<Unit> {
        isRecordingActive = false
        isPausedInternal = false
        recordingJob?.cancel()
        try {
            audioRecord?.stop()
        } catch (_: Exception) {
        }
        currentOutputFile?.let {
            if (it.exists()) it.delete()
        }
        release()
        _state.value = RecordingEngineState.IDLE
        _audioLevel.value = 0f
        _recordingDurationMs.value = 0L
        return Result.success(Unit)
    }

    override fun release() {
        audioRecord?.release()
        audioRecord = null
    }
}

/**
 * Deterministic In-Memory implementation for unit and Robolectric tests,
 * allowing full validation of all lifecycle state transitions, durations, and takes.
 */
class InMemoryAudioRecordingEngine : AudioRecordingEngine {
    private val _state = MutableStateFlow(RecordingEngineState.IDLE)
    override val state: StateFlow<RecordingEngineState> = _state.asStateFlow()

    private val _audioLevel = MutableStateFlow(0.0f)
    override val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    override val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var targetFile: File? = null

    override fun startRecording(outputFile: File): Result<Unit> {
        if (_state.value == RecordingEngineState.RECORDING) {
            return Result.failure(IllegalStateException("Already recording"))
        }
        targetFile = outputFile
        outputFile.parentFile?.mkdirs()
        outputFile.writeBytes(ByteArray(44)) // minimal mock wav header
        _state.value = RecordingEngineState.RECORDING
        _recordingDurationMs.value = 0L
        _audioLevel.value = 0.25f
        return Result.success(Unit)
    }

    override fun pauseRecording(): Result<Unit> {
        if (_state.value != RecordingEngineState.RECORDING) {
            return Result.failure(IllegalStateException("Cannot pause"))
        }
        _state.value = RecordingEngineState.PAUSED
        _audioLevel.value = 0f
        return Result.success(Unit)
    }

    override fun resumeRecording(): Result<Unit> {
        if (_state.value != RecordingEngineState.PAUSED) {
            return Result.failure(IllegalStateException("Cannot resume"))
        }
        _state.value = RecordingEngineState.RECORDING
        _audioLevel.value = 0.35f
        return Result.success(Unit)
    }

    fun simulateDurationTick(additionalMs: Long) {
        _recordingDurationMs.value += additionalMs
    }

    override fun stopRecording(): Result<AudioTakeResult> {
        if (_state.value != RecordingEngineState.RECORDING && _state.value != RecordingEngineState.PAUSED) {
            return Result.failure(IllegalStateException("Not recording"))
        }
        val dur = _recordingDurationMs.value.coerceAtLeast(1200L)
        val result = AudioTakeResult(
            filePath = targetFile?.absolutePath ?: "/mock/take.wav",
            durationMs = dur,
            sampleRateHz = 44100,
            channels = 1,
            bitDepth = 16,
            averageRms = 0.42f
        )
        _state.value = RecordingEngineState.IDLE
        _audioLevel.value = 0f
        return Result.success(result)
    }

    override fun cancelRecording(): Result<Unit> {
        targetFile?.delete()
        _state.value = RecordingEngineState.IDLE
        _audioLevel.value = 0f
        _recordingDurationMs.value = 0L
        return Result.success(Unit)
    }

    override fun release() {
        _state.value = RecordingEngineState.IDLE
    }
}
