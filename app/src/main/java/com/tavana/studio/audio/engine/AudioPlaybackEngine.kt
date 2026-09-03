package com.tavana.studio.audio.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PlaybackEngineState {
    IDLE,
    PREPARING,
    PLAYING,
    PAUSED,
    STOPPED,
    COMPLETED,
    ERROR
}

interface AudioPlaybackEngine {
    val state: StateFlow<PlaybackEngineState>
    val currentPositionMs: StateFlow<Long>
    val durationMs: StateFlow<Long>
    val volume: StateFlow<Float>
    val isLooping: StateFlow<Boolean>
    val playbackSpeed: StateFlow<Float>
    val errorMessage: StateFlow<String?>

    fun prepare(sourceUri: String): Result<Unit>
    fun play(): Result<Unit>
    fun pause(): Result<Unit>
    fun stop(): Result<Unit>
    fun seekTo(positionMs: Long): Result<Unit>
    fun replay(): Result<Unit>
    fun setLooping(enabled: Boolean)
    fun setVolume(volume: Float)
    fun setPlaybackSpeed(speed: Float)
    fun release()
}

/**
 * Android MediaPlayer implementation supporting real audio playback, seeking,
 * volume, pitch/speed adjustments on API 24+, and live progress tracking.
 */
class AndroidAudioPlaybackEngine(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : AudioPlaybackEngine {

    private val _state = MutableStateFlow(PlaybackEngineState.IDLE)
    override val state: StateFlow<PlaybackEngineState> = _state.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    override val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    override val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    override val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isLooping = MutableStateFlow(false)
    override val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    override val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressPollJob: Job? = null

    override fun prepare(sourceUri: String): Result<Unit> {
        try {
            release()
            _state.value = PlaybackEngineState.PREPARING
            _errorMessage.value = null

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                if (sourceUri.startsWith("http") || sourceUri.startsWith("content://") || sourceUri.startsWith("file://")) {
                    setDataSource(context, Uri.parse(sourceUri))
                } else {
                    setDataSource(sourceUri)
                }

                setOnPreparedListener { mp ->
                    _durationMs.value = mp.duration.toLong()
                    _state.value = PlaybackEngineState.PAUSED
                    mp.isLooping = _isLooping.value
                    mp.setVolume(_volume.value, _volume.value)
                    applyPlaybackParams(mp, _playbackSpeed.value)
                }

                setOnCompletionListener {
                    if (!_isLooping.value) {
                        _state.value = PlaybackEngineState.COMPLETED
                        stopPolling()
                    }
                }

                setOnErrorListener { _, what, extra ->
                    val err = "MediaPlayer error: what=$what, extra=$extra"
                    _errorMessage.value = err
                    _state.value = PlaybackEngineState.ERROR
                    stopPolling()
                    true
                }

                prepareAsync()
            }

            mediaPlayer = player
            return Result.success(Unit)
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage ?: "Failed to prepare audio"
            _state.value = PlaybackEngineState.ERROR
            return Result.failure(e)
        }
    }

    override fun play(): Result<Unit> {
        val player = mediaPlayer ?: return Result.failure(IllegalStateException("Not prepared"))
        return try {
            player.start()
            _state.value = PlaybackEngineState.PLAYING
            startPolling()
            Result.success(Unit)
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage
            _state.value = PlaybackEngineState.ERROR
            Result.failure(e)
        }
    }

    override fun pause(): Result<Unit> {
        val player = mediaPlayer ?: return Result.failure(IllegalStateException("Not prepared"))
        return try {
            if (player.isPlaying) {
                player.pause()
            }
            _state.value = PlaybackEngineState.PAUSED
            stopPolling()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun stop(): Result<Unit> {
        val player = mediaPlayer ?: return Result.failure(IllegalStateException("Not prepared"))
        return try {
            player.stop()
            _state.value = PlaybackEngineState.STOPPED
            _currentPositionMs.value = 0L
            stopPolling()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun seekTo(positionMs: Long): Result<Unit> {
        val player = mediaPlayer ?: return Result.failure(IllegalStateException("Not prepared"))
        return try {
            val clamped = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(0L))
            player.seekTo(clamped.toInt())
            _currentPositionMs.value = clamped
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun replay(): Result<Unit> {
        val seekResult = seekTo(0L)
        return if (seekResult.isSuccess) play() else seekResult
    }

    override fun setLooping(enabled: Boolean) {
        _isLooping.value = enabled
        mediaPlayer?.isLooping = enabled
    }

    override fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0.0f, 1.0f)
        _volume.value = clamped
        mediaPlayer?.setVolume(clamped, clamped)
    }

    override fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 2.0f)
        _playbackSpeed.value = clamped
        mediaPlayer?.let { applyPlaybackParams(it, clamped) }
    }

    private fun applyPlaybackParams(mp: MediaPlayer, speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = mp.playbackParams
                params.speed = speed
                mp.playbackParams = params
            } catch (_: Exception) {
            }
        }
    }

    private fun startPolling() {
        stopPolling()
        progressPollJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _currentPositionMs.value = mp.currentPosition.toLong()
                    }
                }
                delay(100)
            }
        }
    }

    private fun stopPolling() {
        progressPollJob?.cancel()
        progressPollJob = null
    }

    override fun release() {
        stopPolling()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        _state.value = PlaybackEngineState.IDLE
    }
}

/**
 * Deterministic In-Memory implementation for unit and Robolectric test verification.
 */
class InMemoryAudioPlaybackEngine : AudioPlaybackEngine {
    private val _state = MutableStateFlow(PlaybackEngineState.IDLE)
    override val state: StateFlow<PlaybackEngineState> = _state.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    override val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(180000L) // 3 mins default
    override val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    override val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isLooping = MutableStateFlow(false)
    override val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    override val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    override fun prepare(sourceUri: String): Result<Unit> {
        _state.value = PlaybackEngineState.PAUSED
        _currentPositionMs.value = 0L
        return Result.success(Unit)
    }

    override fun play(): Result<Unit> {
        _state.value = PlaybackEngineState.PLAYING
        return Result.success(Unit)
    }

    override fun pause(): Result<Unit> {
        _state.value = PlaybackEngineState.PAUSED
        return Result.success(Unit)
    }

    override fun stop(): Result<Unit> {
        _state.value = PlaybackEngineState.STOPPED
        _currentPositionMs.value = 0L
        return Result.success(Unit)
    }

    override fun seekTo(positionMs: Long): Result<Unit> {
        _currentPositionMs.value = positionMs.coerceIn(0L, _durationMs.value)
        return Result.success(Unit)
    }

    override fun replay(): Result<Unit> {
        seekTo(0L)
        return play()
    }

    override fun setLooping(enabled: Boolean) {
        _isLooping.value = enabled
    }

    override fun setVolume(volume: Float) {
        _volume.value = volume.coerceIn(0f, 1f)
    }

    override fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed.coerceIn(0.25f, 2f)
    }

    fun setTestDuration(dur: Long) {
        _durationMs.value = dur
    }

    fun simulateTick(ms: Long) {
        if (_state.value == PlaybackEngineState.PLAYING) {
            val next = _currentPositionMs.value + ms
            if (next >= _durationMs.value) {
                if (_isLooping.value) {
                    _currentPositionMs.value = 0L
                } else {
                    _currentPositionMs.value = _durationMs.value
                    _state.value = PlaybackEngineState.COMPLETED
                }
            } else {
                _currentPositionMs.value = next
            }
        }
    }

    override fun release() {
        _state.value = PlaybackEngineState.IDLE
    }
}
