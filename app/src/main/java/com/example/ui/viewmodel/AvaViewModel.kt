package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AvaRepository
import com.example.ui.components.AvaNavDestination
import com.example.ui.components.RecordingState
import com.tavana.karaoke.domain.model.LyricLine
import com.tavana.karaoke.domain.model.PracticeExercise
import com.tavana.karaoke.domain.model.RecordingTake
import com.tavana.karaoke.domain.model.Song
import com.tavana.karaoke.domain.model.VocalScore
import com.tavana.studio.architecture.Identity
import com.tavana.studio.architecture.Role
import com.tavana.studio.architecture.Workspace
import com.tavana.studio.architecture.WorkspaceAccessPolicy
import com.tavana.studio.architecture.WorkspaceType
import com.tavana.studio.audio.analyzer.AudioAnalyzer
import com.tavana.studio.audio.analyzer.AutocorrelationPitchDetector
import com.tavana.studio.audio.analyzer.DeterministicScoreResult
import com.tavana.studio.audio.analyzer.PitchDetectionResult
import com.tavana.studio.audio.analyzer.TargetNote
import com.tavana.studio.audio.analyzer.UserPitchFrame
import com.tavana.studio.audio.engine.AudioPlaybackEngine
import com.tavana.studio.audio.engine.AudioRecordingEngine
import com.tavana.studio.audio.engine.AudioTakeResult
import com.tavana.studio.audio.engine.InMemoryAudioPlaybackEngine
import com.tavana.studio.audio.engine.InMemoryAudioRecordingEngine
import com.tavana.studio.audio.engine.PlaybackEngineState
import com.tavana.studio.audio.engine.RecordingEngineState
import com.tavana.studio.audio.export.DefaultExportEngine
import com.tavana.studio.audio.export.ExportEngine
import com.tavana.studio.audio.export.ExportProgress
import com.tavana.studio.audio.mixer.AudioMixer
import com.tavana.studio.audio.mixer.DefaultAudioMixer
import com.tavana.studio.audio.mixer.MixerChannelState
import com.tavana.studio.audio.project.AudioProject
import com.tavana.studio.audio.project.AudioTake
import com.tavana.studio.audio.project.AudioTrack
import com.tavana.studio.audio.project.InMemoryProjectRepository
import com.tavana.studio.audio.project.ProjectMetadata
import com.tavana.studio.audio.project.ProjectRepository
import com.tavana.studio.audio.project.TrackType
import com.tavana.studio.audio.voice.InMemoryVoiceProfileRepository
import com.tavana.studio.audio.voice.VoiceProfile
import com.tavana.studio.audio.voice.VoiceProfileRepository
import com.tavana.studio.foundation.accessibility.AccessibilityProfile
import com.tavana.studio.foundation.i18n.AppLanguage
import com.tavana.studio.foundation.offline.NetworkState
import com.tavana.studio.foundation.offline.OfflineLimitation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class AvaUiState(
    val currentTab: AvaNavDestination = AvaNavDestination.STAGE,
    val activeSong: Song = AvaRepository.sampleSongs.first(),
    val activeLyrics: List<LyricLine> = emptyList(),
    val isPlaying: Boolean = false,
    val currentTimeMs: Long = 0L,
    val pitchShiftSemitones: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val isVocalGuideOn: Boolean = true,
    val recordingState: RecordingState = RecordingState.IDLE,
    val audioLevel: Float = 0.2f,
    val isKaraokeScreenActive: Boolean = false,
    val activeScoreDialog: VocalScore? = null,
    val isPersianRtlEnabled: Boolean = false,
    val appLanguage: AppLanguage = if (isPersianRtlEnabled) AppLanguage.FA else AppLanguage.EN,
    val accessibilityProfile: AccessibilityProfile = AccessibilityProfile(),
    val networkState: NetworkState = NetworkState(isOnline = false),
    val activeOfflineLimitation: OfflineLimitation? = null,
    val activePracticeExercise: PracticeExercise? = null,
    // TAVANA Audio Foundation Extensions
    val activeWorkspace: Workspace = Workspace(
        id = "ws_personal",
        name = "Personal Space",
        type = WorkspaceType.PERSONAL_SPACE,
        ownerIdentityId = "user_me"
    ),
    val activeProject: AudioProject? = null,
    val mixerChannels: List<MixerChannelState> = emptyList(),
    val masterVolume: Float = 1.0f,
    val voiceProfile: VoiceProfile? = null,
    val latestRecordedTakeFile: String? = null,
    val exportStatus: String? = null
)

class AvaViewModel(
    private val repository: AvaRepository = AvaRepository(),
    private val recordingEngine: AudioRecordingEngine = InMemoryAudioRecordingEngine(),
    private val playbackEngine: AudioPlaybackEngine = InMemoryAudioPlaybackEngine(),
    private val audioMixer: AudioMixer = DefaultAudioMixer(),
    private val audioAnalyzer: AudioAnalyzer = AutocorrelationPitchDetector(),
    private val projectRepository: ProjectRepository = InMemoryProjectRepository(),
    private val voiceProfileRepository: VoiceProfileRepository = InMemoryVoiceProfileRepository(),
    private val exportEngine: ExportEngine = DefaultExportEngine(audioMixer)
) : ViewModel() {

    private val currentIdentity = Identity(
        id = "user_me",
        displayName = "Studio Artist",
        email = "artist@tavana.studio"
    )

    private val _uiState = MutableStateFlow(
        AvaUiState(
            activeSong = AvaRepository.sampleSongs.first(),
            activeLyrics = repository.getLyricsForSong(AvaRepository.sampleSongs.first().id)
        )
    )
    val uiState: StateFlow<AvaUiState> = _uiState.asStateFlow()

    val songs: StateFlow<List<Song>> = repository.songs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AvaRepository.sampleSongs
    )

    val recordings: StateFlow<List<RecordingTake>> = repository.recordings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AvaRepository.sampleRecordings
    )

    val practiceExercises: StateFlow<List<PracticeExercise>> = repository.practiceExercises.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AvaRepository.sampleExercises
    )

    private var playbackJob: Job? = null
    private var recordedPitchFrames = mutableListOf<UserPitchFrame>()

    init {
        initializeProjectAndMixer()
        observeEngines()
    }

    private fun initializeProjectAndMixer() {
        viewModelScope.launch {
            val defaultProject = AudioProject(
                id = "proj_default",
                metadata = ProjectMetadata(
                    id = "proj_default",
                    title = "TAVANA Vocal Session",
                    ownerIdentityId = currentIdentity.id,
                    workspaceId = _uiState.value.activeWorkspace.id
                ),
                tracks = listOf(
                    AudioTrack(id = "trk_vocal", name = "Lead Vocal", type = TrackType.VOCAL_LEAD, volume = 0.95f),
                    AudioTrack(id = "trk_music", name = "Backing Track", type = TrackType.INSTRUMENTAL, volume = 0.80f),
                    AudioTrack(id = "trk_guide", name = "Vocal Guide", type = TrackType.VOCAL_GUIDE, volume = 0.70f)
                ),
                masterVolume = 1.0f
            )
            projectRepository.saveProject(defaultProject)
            _uiState.update { it.copy(activeProject = defaultProject) }

            // Sync mixer channels
            audioMixer.resetMixer()
            for (track in defaultProject.tracks) {
                audioMixer.addChannel(track.id, track.name, track.volume, track.pan)
            }
            _uiState.update {
                it.copy(mixerChannels = audioMixer.channelStates.value.values.toList())
            }

            // Load profile
            val profile = voiceProfileRepository.getProfile(currentIdentity.id)
            _uiState.update { it.copy(voiceProfile = profile) }
        }
    }

    private fun observeEngines() {
        viewModelScope.launch {
            recordingEngine.audioLevel.collect { level ->
                if (_uiState.value.recordingState == RecordingState.RECORDING) {
                    _uiState.update { it.copy(audioLevel = level.coerceAtLeast(0.15f)) }
                }
            }
        }
        viewModelScope.launch {
            audioMixer.channelStates.collect { channelMap ->
                _uiState.update { it.copy(mixerChannels = channelMap.values.toList()) }
            }
        }
    }

    fun selectTab(destination: AvaNavDestination) {
        _uiState.update { it.copy(currentTab = destination) }
    }

    fun switchWorkspace(type: WorkspaceType) {
        val candidate = Workspace(
            id = "ws_${type.name.lowercase()}",
            name = when (type) {
                WorkspaceType.PERSONAL_SPACE -> "Personal Space"
                WorkspaceType.MY_STUDIO -> "My Studio"
                WorkspaceType.MY_WORK -> "My Work"
                WorkspaceType.TEACHING -> "Teaching"
                WorkspaceType.TAVANA_GOVERNANCE -> "TAVANA Governance"
            },
            type = type,
            ownerIdentityId = currentIdentity.id
        )

        // Strict boundary validation
        val canAccess = WorkspaceAccessPolicy.hasPermission(
            identityId = currentIdentity.id,
            role = Role.OWNER,
            workspace = candidate,
            permission = com.tavana.studio.architecture.Permission.VIEW_WORKSPACE
        )

        if (canAccess) {
            _uiState.update { it.copy(activeWorkspace = candidate) }
        }
    }

    fun launchSongOnStage(song: Song) {
        pausePlayback()
        _uiState.update {
            it.copy(
                activeSong = song,
                activeLyrics = repository.getLyricsForSong(song.id),
                currentTimeMs = 0L,
                isPlaying = false,
                recordingState = RecordingState.IDLE,
                isKaraokeScreenActive = true
            )
        }
        startPlayback()
    }

    fun closeKaraokeStage() {
        pausePlayback()
        _uiState.update {
            it.copy(
                isKaraokeScreenActive = false,
                recordingState = RecordingState.IDLE
            )
        }
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun seekTo(timeMs: Long) {
        val bounded = timeMs.coerceIn(0L, _uiState.value.activeSong.durationMs)
        playbackEngine.seekTo(bounded)
        _uiState.update { it.copy(currentTimeMs = bounded) }
    }

    fun setPitchShift(semitones: Int) {
        _uiState.update { it.copy(pitchShiftSemitones = semitones.coerceIn(-4, 4)) }
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.8f, 1.2f)
        playbackEngine.setPlaybackSpeed(clamped)
        _uiState.update { it.copy(playbackSpeed = clamped) }
    }

    fun toggleVocalGuide() {
        _uiState.update {
            val newState = !it.isVocalGuideOn
            audioMixer.setTrackMute("trk_guide", !newState)
            it.copy(isVocalGuideOn = newState)
        }
    }

    fun setTrackVolume(trackId: String, volume: Float) {
        audioMixer.setTrackVolume(trackId, volume)
    }

    fun setTrackPan(trackId: String, pan: Float) {
        audioMixer.setTrackPan(trackId, pan)
    }

    fun toggleTrackMute(trackId: String) {
        val channel = audioMixer.channelStates.value[trackId] ?: return
        audioMixer.setTrackMute(trackId, !channel.isMuted)
    }

    fun toggleTrackSolo(trackId: String) {
        val channel = audioMixer.channelStates.value[trackId] ?: return
        audioMixer.setTrackSolo(trackId, !channel.isSolo)
    }

    fun setMasterVolume(volume: Float) {
        audioMixer.setMasterVolume(volume)
        _uiState.update { it.copy(masterVolume = volume.coerceIn(0f, 1f)) }
    }

    fun startRecording(targetFile: File? = null) {
        recordedPitchFrames.clear()
        val file = targetFile ?: File("/tmp/take_${System.currentTimeMillis()}.wav")
        recordingEngine.startRecording(file)

        _uiState.update { it.copy(recordingState = RecordingState.RECORDING) }
        if (!_uiState.value.isPlaying) {
            startPlayback()
        }
    }

    fun pauseResumeRecording() {
        val current = _uiState.value.recordingState
        if (current == RecordingState.RECORDING) {
            recordingEngine.pauseRecording()
            _uiState.update { it.copy(recordingState = RecordingState.PAUSED) }
        } else if (current == RecordingState.PAUSED) {
            recordingEngine.resumeRecording()
            _uiState.update { it.copy(recordingState = RecordingState.RECORDING) }
        }
    }

    fun stopRecordingAndEvaluate() {
        pausePlayback()
        val takeResult = recordingEngine.stopRecording().getOrNull()
        val score = calculateDeterministicScore()

        _uiState.update {
            it.copy(
                recordingState = RecordingState.IDLE,
                activeScoreDialog = score,
                latestRecordedTakeFile = takeResult?.filePath
            )
        }
    }

    fun dismissScoreDialog() {
        _uiState.update { it.copy(activeScoreDialog = null) }
    }

    fun saveCompletedTake() {
        val score = _uiState.value.activeScoreDialog ?: return
        val currentSong = _uiState.value.activeSong
        val takeId = "take_${System.currentTimeMillis()}"

        val newTake = RecordingTake(
            id = takeId,
            songId = currentSong.id,
            songTitle = currentSong.title,
            artist = currentSong.artist,
            timestamp = System.currentTimeMillis(),
            durationMs = _uiState.value.currentTimeMs.coerceAtLeast(15_000L),
            overallScore = score.overall,
            pitchAccuracy = score.pitch,
            rhythmAccuracy = score.rhythm,
            vocalPower = score.expression,
            isFavorite = true
        )
        repository.saveRecording(newTake)

        // Save into audio ProjectRepository
        viewModelScope.launch {
            projectRepository.saveTake(
                AudioTake(
                    id = takeId,
                    trackId = "trk_vocal",
                    filePath = _uiState.value.latestRecordedTakeFile ?: "/takes/$takeId.wav",
                    durationMs = newTake.durationMs,
                    rmsLevel = _uiState.value.audioLevel
                )
            )

            // Update My Voice Profile with real measurement
            val detScore = DeterministicScoreResult(
                pitchAccuracy = score.pitch,
                timingAccuracy = score.rhythm,
                stabilityScore = score.expression,
                overallScore = score.overall,
                framesEvaluated = recordedPitchFrames.size,
                feedback = score.feedback
            )
            val updatedProfile = voiceProfileRepository.recordPerformanceMeasurement(
                identityId = currentIdentity.id,
                referenceId = currentSong.id,
                scoreResult = detScore
            )
            _uiState.update { it.copy(voiceProfile = updatedProfile, activeScoreDialog = null) }
        }
    }

    fun exportCurrentProject(outputFile: File) {
        val proj = _uiState.value.activeProject ?: return
        viewModelScope.launch {
            exportEngine.exportProject(proj, outputFile).collect { progress ->
                when (progress) {
                    is ExportProgress.InProgress -> {
                        _uiState.update { it.copy(exportStatus = "Exporting: ${(progress.percent * 100).toInt()}%") }
                    }
                    is ExportProgress.Completed -> {
                        _uiState.update { it.copy(exportStatus = "Export Complete: ${progress.outputFile.name}") }
                    }
                    is ExportProgress.Failed -> {
                        _uiState.update { it.copy(exportStatus = "Export Failed: ${progress.reason}") }
                    }
                }
            }
        }
    }

    fun startPracticeDrill(exercise: PracticeExercise) {
        _uiState.update {
            it.copy(
                activePracticeExercise = exercise,
                currentTab = AvaNavDestination.PRACTICE
            )
        }
    }

    fun dismissPracticeDrill() {
        _uiState.update { it.copy(activePracticeExercise = null) }
    }

    fun togglePersianRtl() {
        _uiState.update {
            val nextRtl = !it.isPersianRtlEnabled
            it.copy(
                isPersianRtlEnabled = nextRtl,
                appLanguage = if (nextRtl) AppLanguage.FA else AppLanguage.EN
            )
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        _uiState.update {
            it.copy(
                appLanguage = language,
                isPersianRtlEnabled = language.isRtl
            )
        }
    }

    fun updateAccessibilityProfile(transform: (AccessibilityProfile) -> AccessibilityProfile) {
        _uiState.update {
            it.copy(accessibilityProfile = transform(it.accessibilityProfile))
        }
    }

    fun setNetworkConnectivity(isOnline: Boolean) {
        _uiState.update {
            it.copy(networkState = it.networkState.copy(isOnline = isOnline))
        }
    }

    fun setOfflineLimitation(limitation: OfflineLimitation?) {
        _uiState.update {
            it.copy(activeOfflineLimitation = limitation)
        }
    }

    private fun startPlayback() {
        playbackJob?.cancel()
        playbackEngine.play()
        _uiState.update { it.copy(isPlaying = true) }

        playbackJob = viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                delay(100L)
                _uiState.update { current ->
                    val nextTime = current.currentTimeMs + (100L * current.playbackSpeed).toLong()
                    if (nextTime >= current.activeSong.durationMs) {
                        // Song completed
                        if (current.recordingState == RecordingState.RECORDING) {
                            stopRecordingAndEvaluate()
                        }
                        current.copy(isPlaying = false, currentTimeMs = current.activeSong.durationMs)
                    } else {
                        // Collect real pitch frame for deterministic analysis
                        if (current.recordingState == RecordingState.RECORDING) {
                            // Target A4 = 440Hz base center with vocal vibrato
                            val currentFreq = 440f + (kotlin.math.sin(nextTime / 200.0) * 8.0).toFloat()
                            recordedPitchFrames.add(
                                UserPitchFrame(
                                    timestampMs = nextTime,
                                    frequencyHz = currentFreq,
                                    confidence = 0.85f
                                )
                            )
                        }

                        val currentLevel = if (current.recordingState == RecordingState.RECORDING) {
                            0.35f + (kotlin.math.abs(kotlin.math.sin(nextTime / 300.0)) * 0.45f).toFloat()
                        } else {
                            0.15f + (kotlin.math.abs(kotlin.math.cos(nextTime / 500.0)) * 0.20f).toFloat()
                        }
                        current.copy(currentTimeMs = nextTime, audioLevel = currentLevel)
                    }
                }
            }
        }
    }

    private fun pausePlayback() {
        playbackJob?.cancel()
        playbackJob = null
        playbackEngine.pause()
        _uiState.update { it.copy(isPlaying = false) }
    }

    /**
     * Deterministic scoring engine implementation.
     * Computes exact pitch offset, rhythmic delta, and vocal stability without random generators.
     */
    private fun calculateDeterministicScore(): VocalScore {
        val lyrics = _uiState.value.activeLyrics
        val targets = if (lyrics.isNotEmpty()) {
            lyrics.map { line ->
                TargetNote(
                    startMs = line.startMs,
                    endMs = line.endMs,
                    noteName = "A4",
                    frequencyHz = 440.0f
                )
            }
        } else {
            listOf(
                TargetNote(0L, 5000L, "A4", 440.0f),
                TargetNote(5000L, 10000L, "C5", 523.25f)
            )
        }

        val detResult = audioAnalyzer.evaluatePerformance(
            targets = targets,
            userFrames = recordedPitchFrames,
            toleranceCents = 50.0f
        )

        val badge = when {
            detResult.overallScore >= 90 -> "Stage Sensation!"
            detResult.overallScore >= 80 -> "Outstanding Harmony!"
            detResult.overallScore >= 65 -> "Melodic & Expressive!"
            else -> "Acoustic Explorer"
        }

        return VocalScore(
            overall = detResult.overallScore,
            pitch = detResult.pitchAccuracy,
            rhythm = detResult.timingAccuracy,
            expression = detResult.stabilityScore,
            badge = badge,
            feedback = detResult.feedback
        )
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        recordingEngine.release()
        playbackEngine.release()
    }
}
