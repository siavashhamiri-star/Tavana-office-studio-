package com.tavana.studio.foundation.accessibility

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Core Accessibility Architecture for TAVANA Studio.
 * Enforces WCAG 2.1 AA / AAA standards, minimum 48dp touch targets, TalkBack semantics,
 * multimodal feedback (audio/visual/haptic), and text alternatives for deaf and hard-of-hearing users.
 */
object TavanaTouchTargets {
    /**
     * Absolute minimum touch target size across all platforms and form factors.
     * Complies strictly with Material Design 3 and Android Accessibility requirements.
     */
    val MIN_TOUCH_TARGET: Dp = 48.dp
    val COMFORTABLE_TOUCH_TARGET: Dp = 56.dp
}

enum class FontScaleMode(val scaleFactor: Float) {
    DEFAULT(1.0f),
    LARGE(1.3f),
    EXTRA_LARGE(1.6f)
}

/**
 * Unified Accessibility Profile capturing multi-sensory user needs.
 */
data class AccessibilityProfile(
    val isScreenReaderActive: Boolean = false,
    val isHighContrastEnabled: Boolean = false,
    val fontScale: FontScaleMode = FontScaleMode.DEFAULT,
    val isHapticFeedbackEnabled: Boolean = true,
    val isVisualCaptionsEnabled: Boolean = true,
    val isAudioGuidanceEnabled: Boolean = false,
    val isSimplifiedNavigationEnabled: Boolean = false
)

/**
 * Text alternatives for real-time auditory signals.
 * Enables deaf and hard-of-hearing users to visually perceive audio events.
 */
object AccessibleAudioDescriptions {
    fun describeInputLevel(levelNormalized: Float): String {
        val percentage = (levelNormalized.coerceIn(0f, 1f) * 100).toInt()
        return when {
            percentage < 5 -> "Microphone silent (0% signal)"
            percentage < 40 -> "Microphone low signal ($percentage%)"
            percentage < 80 -> "Microphone optimal recording level ($percentage%)"
            else -> "Microphone hot/peaking ($percentage% - reduce gain to avoid distortion)"
        }
    }

    fun describePitchEvaluation(noteName: String, deviationCents: Int): String {
        return when {
            Math.abs(deviationCents) <= 15 -> "Pitch on target: $noteName (Perfect tuning)"
            deviationCents > 15 -> "Pitch sharp by $deviationCents cents for $noteName"
            else -> "Pitch flat by ${-deviationCents} cents for $noteName"
        }
    }

    fun describeRecordingState(isRecording: Boolean, elapsedSeconds: Long): String {
        return if (isRecording) {
            "Recording in progress: ${elapsedSeconds / 60} minutes ${elapsedSeconds % 60} seconds elapsed."
        } else {
            "Recording stopped. Take ready for playback or review."
        }
    }
}

/**
 * Accessible modifier ensuring interactive elements meet minimum touch target bounds
 * and expose unambiguous semantic roles and content descriptions to screen readers.
 */
fun Modifier.tavanaAccessibleAction(
    contentDescription: String,
    actionLabel: String? = null,
    role: Role = Role.Button,
    minTouchTarget: Dp = TavanaTouchTargets.MIN_TOUCH_TARGET,
    onClick: (() -> Unit)? = null
): Modifier = this
    .defaultMinSize(minWidth = minTouchTarget, minHeight = minTouchTarget)
    .semantics(mergeDescendants = true) {
        this.contentDescription = contentDescription
        this.role = role
        if (onClick != null && actionLabel != null) {
            this.onClick(label = actionLabel) {
                onClick()
                true
            }
        }
    }

/**
 * Marks an element as an accessibility heading for clear TalkBack navigation hierarchy.
 */
fun Modifier.tavanaHeading(): Modifier = this.semantics {
    heading()
}

/**
 * Live announcement region for TalkBack screen reader to announce dynamic status updates
 * (e.g. recording started, audio take saved, pitch score ready).
 */
fun Modifier.tavanaLiveAnnouncement(statusDescription: String): Modifier = this.semantics {
    liveRegion = LiveRegionMode.Polite
    contentDescription = statusDescription
}

val LocalAccessibilityProfile = staticCompositionLocalOf { AccessibilityProfile() }

object TavanaAccessibility {
    val profile: AccessibilityProfile
        @Composable
        @ReadOnlyComposable
        get() = LocalAccessibilityProfile.current
}
