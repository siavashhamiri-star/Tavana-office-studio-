package com.tavana.studio.audio.workspaces

data class SongwritingSection(
    val sectionType: String, // "Verse", "Chorus", "Bridge", "Outro"
    val lines: List<String>,
    val chords: List<String> = emptyList(),
    val notes: String? = null
)

data class SongwritingIdea(
    val id: String,
    val title: String,
    val theme: String,
    val mood: String,
    val genre: String,
    val key: String = "C Major",
    val tempoBpm: Int = 120,
    val sections: List<SongwritingSection> = emptyList()
)

data class RapFlowMetric(
    val syllablesPerSecond: Float,
    val rhymeDensity: Float,
    val averageBpm: Int,
    val flowCadence: String
)

data class RapStudioSession(
    val id: String,
    val title: String,
    val bpm: Int = 90,
    val beatTrackPath: String? = null,
    val verses: List<String> = emptyList(),
    val hook: String? = null,
    val rhymeSuggestions: List<String> = emptyList(),
    val flowMetrics: RapFlowMetric? = null
)

data class PodcastEpisode(
    val id: String,
    val title: String,
    val episodeNumber: Int,
    val hostTrackId: String? = null,
    val guestTrackId: String? = null,
    val introMusicTrackId: String? = null,
    val outroMusicTrackId: String? = null,
    val soundEffectsTrackId: String? = null,
    val duckingEnabled: Boolean = true,
    val exportMasterPath: String? = null
)

data class AudiobookChapter(
    val id: String,
    val chapterNumber: Int,
    val chapterTitle: String,
    val narratorTrackId: String? = null,
    val wordCount: Int = 0,
    val roomPreset: String = "Studio",
    val isCleaned: Boolean = true
)

enum class VoiceoverPreset(val displayName: String, val eqProfile: String, val targetDecaySec: Float) {
    COMMERCIAL("Commercial", "Vocal Presence", 0.4f),
    RADIO_AD("Radio Ad", "Radio Voice", 0.3f),
    SOCIAL_AD("Social Ad", "Vocal Bright", 0.5f),
    CINEMATIC("Cinematic", "Vocal Warm", 1.8f),
    ENERGETIC("Energetic", "Vocal Bright", 0.6f),
    PROFESSIONAL("Professional", "Studio", 0.5f)
}

data class VoiceoverAdSession(
    val id: String,
    val clientOrCampaign: String,
    val scriptText: String,
    val selectedPreset: VoiceoverPreset = VoiceoverPreset.PROFESSIONAL,
    val backgroundMusicVolume: Float = 0.25f,
    val duckingStrength: Float = 0.8f
)

data class SoundboardClip(
    val id: String,
    val name: String,
    val category: String, // "Applause", "Cheers", "Crowd", "Laugh", "Bell", "Transition", "Station ID", "Jingle"
    val assetResName: String,
    val defaultVolume: Float = 1.0f,
    val isAssetPresent: Boolean = false,
    val localFilePath: String? = null
)

object SoundboardCatalog {
    val BUILT_IN_CLIPS = listOf(
        SoundboardClip("sb_applause", "Studio Applause", "Applause", "sfx_applause", isAssetPresent = false),
        SoundboardClip("sb_cheers", "Crowd Cheers", "Cheers", "sfx_cheers", isAssetPresent = false),
        SoundboardClip("sb_laugh", "Chuckle / Laugh", "Laugh", "sfx_laugh", isAssetPresent = false),
        SoundboardClip("sb_bell", "Service Bell", "Bell", "sfx_bell", isAssetPresent = false),
        SoundboardClip("sb_transition", "Swoosh Transition", "Transition", "sfx_transition", isAssetPresent = false),
        SoundboardClip("sb_station_id", "TAVANA Radio ID", "Station ID", "jingle_station_id", isAssetPresent = false),
        SoundboardClip("sb_jingle", "Upbeat Stinger", "Jingle", "jingle_upbeat", isAssetPresent = false)
    )

    fun hasMissingAssets(): Boolean = BUILT_IN_CLIPS.any { !it.isAssetPresent && it.localFilePath == null }
}

data class LiveRadioState(
    val isLive: Boolean = false,
    val microphoneGain: Float = 1.0f,
    val bgMusicVolume: Float = 0.5f,
    val sfxVolume: Float = 0.8f,
    val jingleVolume: Float = 0.9f,
    val isAutoDuckingActive: Boolean = true,
    val duckingThresholdRms: Float = 0.05f
)

data class TeachingSession(
    val id: String,
    val title: String,
    val instructorId: String,
    val studentId: String?,
    val lessonNotes: String = "",
    val referenceAudioUrl: String? = null,
    val studentTakeId: String? = null,
    val targetScore: Int = 80
)

data class PartyRoomFoundation(
    val roomId: String,
    val hostIdentityId: String,
    val roomTitle: String,
    val participantsCount: Int = 1,
    val isRealTimeLive: Boolean = false,
    val readinessStatus: String = "READY FOR BACKEND / REAL-TIME INTEGRATION"
)
