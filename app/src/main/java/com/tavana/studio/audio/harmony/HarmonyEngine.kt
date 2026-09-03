package com.tavana.studio.audio.harmony

import com.tavana.studio.audio.pitch.MusicalPitchHelper
import com.tavana.studio.audio.project.AudioTrack
import com.tavana.studio.audio.project.TrackType

enum class HarmonyInterval(val displayName: String, val semitones: Int) {
    THIRD_ABOVE("Major Third (+4)", 4),
    MINOR_THIRD_ABOVE("Minor Third (+3)", 3),
    THIRD_BELOW("Third Below (-4)", -4),
    FIFTH_ABOVE("Perfect Fifth (+7)", 7),
    OCTAVE_ABOVE("Octave (+12)", 12),
    OCTAVE_BELOW("Sub Octave (-12)", -12);
}

data class HarmonyLayer(
    val id: String,
    val sourceTrackId: String,
    val name: String,
    val interval: HarmonyInterval,
    val volume: Float = 0.7f,
    val pan: Float = 0.3f, // slightly panned for wide stereo chorus
    val isEnabled: Boolean = true
)

interface HarmonyEngine {
    fun generateHarmonyTrack(
        sourceTrack: AudioTrack,
        interval: HarmonyInterval,
        volume: Float = 0.75f,
        pan: Float = 0.4f
    ): AudioTrack

    fun createSelfDuetPair(
        leadTrack: AudioTrack,
        secondTrackName: String = "Duet Partner (Self)",
        interval: HarmonyInterval = HarmonyInterval.THIRD_ABOVE
    ): Pair<AudioTrack, AudioTrack>
}

class DefaultHarmonyEngine : HarmonyEngine {
    override fun generateHarmonyTrack(
        sourceTrack: AudioTrack,
        interval: HarmonyInterval,
        volume: Float,
        pan: Float
    ): AudioTrack {
        val harmonyTrackId = "trk_harmony_${sourceTrack.id}_${interval.name.lowercase()}"
        val harmonyTrackName = "${sourceTrack.name} (${interval.displayName})"

        return AudioTrack(
            id = harmonyTrackId,
            name = harmonyTrackName,
            type = TrackType.HARMONY,
            volume = volume.coerceIn(0f, 1f),
            pan = pan.coerceIn(-1f, 1f),
            isMuted = false,
            isSolo = false,
            clips = sourceTrack.clips.map { clip ->
                clip.copy(
                    id = "clip_harm_${clip.id}",
                    trackId = harmonyTrackId,
                    volume = volume,
                    pan = pan
                )
            },
            effects = sourceTrack.effects
        )
    }

    override fun createSelfDuetPair(
        leadTrack: AudioTrack,
        secondTrackName: String,
        interval: HarmonyInterval
    ): Pair<AudioTrack, AudioTrack> {
        val leadTrackBalanced = leadTrack.copy(
            name = "${leadTrack.name} (Lead A)",
            pan = -0.3f // pan slightly left
        )

        val duetTrack = AudioTrack(
            id = "trk_duet_b_${leadTrack.id}",
            name = secondTrackName,
            type = TrackType.VOCAL_LEAD,
            volume = leadTrack.volume * 0.9f,
            pan = 0.3f, // pan slightly right
            isMuted = false,
            isSolo = false,
            clips = leadTrack.clips.map { clip ->
                clip.copy(
                    id = "clip_duet_b_${clip.id}",
                    trackId = "trk_duet_b_${leadTrack.id}",
                    volume = leadTrack.volume * 0.9f,
                    pan = 0.3f
                )
            }
        )

        return Pair(leadTrackBalanced, duetTrack)
    }
}
