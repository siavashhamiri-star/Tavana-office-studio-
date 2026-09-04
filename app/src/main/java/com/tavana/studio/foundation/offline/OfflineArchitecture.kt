package com.tavana.studio.foundation.offline

/**
 * TAVANA Studio Offline-First Foundation.
 * Strictly separates offline-native capabilities from cloud-reliant features.
 * Guarantees that local recording, playback, mixing, DSP, lyrics, pitch analysis,
 * and project persistence function 100% reliably without network connectivity.
 */

enum class CapabilityTier {
    /**
     * Runs 100% locally on device hardware. Never requires network access.
     */
    OFFLINE_NATIVE,

    /**
     * Works seamlessly offline using local cache; syncs metadata when connected.
     */
    HYBRID_CACHED,

    /**
     * Strictly requires active internet connection. Degrades gracefully offline.
     */
    ONLINE_REQUIRED
}

enum class StudioFeature(
    val displayName: String,
    val tier: CapabilityTier,
    val offlineBehavior: String
) {
    AUDIO_RECORDING(
        displayName = "Audio Recording (PCM / WAV)",
        tier = CapabilityTier.OFFLINE_NATIVE,
        offlineBehavior = "Full native hardware recording via AudioRecord. Stored to internal sandbox."
    ),
    AUDIO_PLAYBACK(
        displayName = "Audio Playback & Scrubbing",
        tier = CapabilityTier.OFFLINE_NATIVE,
        offlineBehavior = "Full offline playback of local files and stems via MediaPlayer."
    ),
    LOCAL_PROJECTS(
        displayName = "Local Project Workspace & State",
        tier = CapabilityTier.OFFLINE_NATIVE,
        offlineBehavior = "Project metadata, tracks, and takes persisted in local repository."
    ),
    LOCAL_AUDIO_FILES(
        displayName = "Local Audio File Management",
        tier = CapabilityTier.OFFLINE_NATIVE,
        offlineBehavior = "WAV file reader/writer operates entirely on private app storage."
    ),
    BASIC_EDITING(
        displayName = "Basic Audio Editing & Gain Staging",
        tier = CapabilityTier.OFFLINE_NATIVE,
        offlineBehavior = "Track volume faders, solo, mute, stereo pan computed in memory."
    ),
    MULTI_TRACK_MIXER(
        displayName = "Multi-track Audio Mixer",
        tier = CapabilityTier.OFFLINE_NATIVE,
        offlineBehavior = "Real-time stereo channel summing executed by local DSP mixer."
    ),
    LOCAL_KARAOKE_LYRICS(
        displayName = "Karaoke Stage & Synchronized Lyrics",
        tier = CapabilityTier.OFFLINE_NATIVE,
        offlineBehavior = "Time-coded lyrics synced to local playhead with zero network latency."
    ),
    PITCH_AUDIO_ANALYSIS(
        displayName = "Pitch & Audio Frequency Analysis",
        tier = CapabilityTier.OFFLINE_NATIVE,
        offlineBehavior = "DSP autocorrelation pitch extraction runs locally on CPU."
    ),
    DSP_EFFECTS(
        displayName = "DSP Effects (EQ, Reverb, Delay)",
        tier = CapabilityTier.OFFLINE_NATIVE,
        offlineBehavior = "Algorithmic Biquad filters and comb reverbs process audio in-memory."
    ),
    VOCAL_COACH_SCORING(
        displayName = "Vocal Coach Local Pitch & Timing Evaluation",
        tier = CapabilityTier.OFFLINE_NATIVE,
        offlineBehavior = "Deterministic pitch accuracy and cadence scoring runs completely offline."
    ),
    ACCESSIBILITY_SERVICES(
        displayName = "Accessibility & TalkBack Semantics",
        tier = CapabilityTier.OFFLINE_NATIVE,
        offlineBehavior = "All screen reader announcements and touch targets operate offline."
    ),
    MULTILINGUAL_SWITCHING(
        displayName = "Multilingual & RTL/LTR Text Engine",
        tier = CapabilityTier.OFFLINE_NATIVE,
        offlineBehavior = "Static language strings and directionality bundled inside the APK."
    ),
    // Hybrid
    SONG_CATALOG_SYNC(
        displayName = "Song & Exercise Catalog",
        tier = CapabilityTier.HYBRID_CACHED,
        offlineBehavior = "Pre-packaged local library available immediately; syncs additions when online."
    ),
    // Online-only features with clear boundaries
    CLOUD_PROJECT_BACKUP(
        displayName = "Cloud Backup & Drive Sync",
        tier = CapabilityTier.ONLINE_REQUIRED,
        offlineBehavior = "Queued locally. Automatic sync resumes when network connection is restored."
    ),
    REMOTE_AI_GENERATION(
        displayName = "Remote Gemini AI Deep Vocal Feedback",
        tier = CapabilityTier.ONLINE_REQUIRED,
        offlineBehavior = "Unavailable offline. App transparently falls back to local deterministic coach scoring."
    ),
    LIVE_RADIO_BROADCAST(
        displayName = "Live Radio Webcast Streaming",
        tier = CapabilityTier.ONLINE_REQUIRED,
        offlineBehavior = "Live broadcast streaming paused. Local recording remains active for later upload."
    ),
    PARTY_ROOM_COLLABORATION(
        displayName = "Party Room Multi-user WebRTC Stage",
        tier = CapabilityTier.ONLINE_REQUIRED,
        offlineBehavior = "Multiplayer room requires internet. Local solo karaoke stage remains fully functional."
    )
}

/**
 * Real-time network connectivity representation.
 */
data class NetworkState(
    val isOnline: Boolean = false,
    val connectionType: ConnectionType = ConnectionType.NONE,
    val isMetered: Boolean = false
) {
    enum class ConnectionType {
        WIFI,
        CELLULAR,
        ETHERNET,
        NONE
    }
}

/**
 * Explicit, user-facing offline limitations preventing unexpected crashes or deadlocks.
 */
sealed interface OfflineLimitation {
    data class FeatureUnavailable(
        val feature: StudioFeature,
        val reason: String,
        val localAlternative: String
    ) : OfflineLimitation

    data class SyncDeferred(
        val pendingCount: Int,
        val message: String
    ) : OfflineLimitation
}

/**
 * Utility executing cloud actions safely with guaranteed offline fallback,
 * ensuring the application never crashes due to lost or absent internet connectivity.
 */
object OfflineSafetyGuard {
    inline fun <T> executeSafe(
        isOnline: Boolean,
        onLocalFallback: () -> T,
        onCloudAction: () -> T
    ): T {
        return if (!isOnline) {
            onLocalFallback()
        } else {
            try {
                onCloudAction()
            } catch (e: Exception) {
                // If remote network request fails unexpectedly, fallback safely to local logic
                onLocalFallback()
            }
        }
    }

    fun getLimitationNotice(feature: StudioFeature): OfflineLimitation.FeatureUnavailable {
        return OfflineLimitation.FeatureUnavailable(
            feature = feature,
            reason = "Active internet connection is currently unavailable.",
            localAlternative = feature.offlineBehavior
        )
    }
}
