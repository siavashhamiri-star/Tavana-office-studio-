package com.example

import com.example.ui.viewmodel.AvaViewModel
import com.tavana.studio.foundation.accessibility.AccessibleAudioDescriptions
import com.tavana.studio.foundation.accessibility.FontScaleMode
import com.tavana.studio.foundation.accessibility.TavanaTouchTargets
import com.tavana.studio.foundation.i18n.AiLanguageConfig
import com.tavana.studio.foundation.i18n.AppLanguage
import com.tavana.studio.foundation.i18n.LyricsLanguageConfig
import com.tavana.studio.foundation.i18n.TavanaStringsRegistry
import com.tavana.studio.foundation.i18n.UnicodeScript
import com.tavana.studio.foundation.i18n.VoiceLanguageConfig
import com.tavana.studio.foundation.offline.CapabilityTier
import com.tavana.studio.foundation.offline.OfflineSafetyGuard
import com.tavana.studio.foundation.offline.StudioFeature
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.compose.ui.unit.dp

class TavanaArchitecturalFoundationsTest {

    // =========================================================================
    // 1. Multilingual / Internationalization Foundation Tests
    // =========================================================================

    @Test
    fun `multilingual foundation supports all 11 required languages with correct metadata`() {
        val expectedCodes = listOf("fa", "en", "ar", "fr", "es", "de", "ru", "hi", "zh", "tl", "th")
        val supportedCodes = AppLanguage.entries.map { it.code }

        assertEquals("All 11 languages must be present", expectedCodes.sorted(), supportedCodes.sorted())

        // Check RTL languages
        assertTrue("Persian must be RTL", AppLanguage.FA.isRtl)
        assertTrue("Arabic must be RTL", AppLanguage.AR.isRtl)
        assertFalse("English must be LTR", AppLanguage.EN.isRtl)
        assertFalse("French must be LTR", AppLanguage.FR.isRtl)
        assertFalse("Spanish must be LTR", AppLanguage.ES.isRtl)
        assertFalse("German must be LTR", AppLanguage.DE.isRtl)
        assertFalse("Russian must be LTR", AppLanguage.RU.isRtl)
        assertFalse("Hindi must be LTR", AppLanguage.HI.isRtl)
        assertFalse("Chinese must be LTR", AppLanguage.ZH.isRtl)
        assertFalse("Tagalog must be LTR", AppLanguage.TL.isRtl)
        assertFalse("Thai must be LTR", AppLanguage.TH.isRtl)

        // Check Unicode scripts
        assertEquals(UnicodeScript.PERSIAN_ARABIC, AppLanguage.FA.script)
        assertEquals(UnicodeScript.ARABIC, AppLanguage.AR.script)
        assertEquals(UnicodeScript.LATIN, AppLanguage.EN.script)
        assertEquals(UnicodeScript.CYRILLIC, AppLanguage.RU.script)
        assertEquals(UnicodeScript.DEVANAGARI, AppLanguage.HI.script)
        assertEquals(UnicodeScript.HAN, AppLanguage.ZH.script)
        assertEquals(UnicodeScript.THAI, AppLanguage.TH.script)
    }

    @Test
    fun `language resolution handles case insensitivity and safe fallbacks`() {
        assertEquals(AppLanguage.FA, AppLanguage.fromCode("FA"))
        assertEquals(AppLanguage.AR, AppLanguage.fromCode("ar"))
        assertEquals(AppLanguage.ZH, AppLanguage.fromCode("zh "))
        assertEquals(AppLanguage.EN, AppLanguage.fromCode("unknown_code"))
    }

    @Test
    fun `localized strings registry provides non-null and valid strings for all languages`() {
        for (lang in AppLanguage.entries) {
            val strings = TavanaStringsRegistry.getStrings(lang)
            assertNotNull("Strings must not be null for ${lang.code}", strings)
            assertTrue("App name must not be blank", strings.appName.isNotBlank())
            assertTrue("Record action must not be blank", strings.actionRecord.isNotBlank())
            assertTrue("Offline indicator must not be blank", strings.offlineStatusLocalOnly.isNotBlank())
        }

        val persian = TavanaStringsRegistry.getStrings(AppLanguage.FA)
        assertEquals("استودیو توانا", persian.appName)
        assertEquals("استیج", persian.tabStage)

        val arabic = TavanaStringsRegistry.getStrings(AppLanguage.AR)
        assertEquals("استوديو توانا", arabic.appName)
        assertEquals("المسرح", arabic.tabStage)
    }

    @Test
    fun `extensible language configurations support lyrics, AI, and voice prompts`() {
        val lyricsConfig = LyricsLanguageConfig(language = AppLanguage.FA, isBidiSupported = true, showPhonetics = true)
        assertTrue(lyricsConfig.isBidiSupported)
        assertTrue(lyricsConfig.showPhonetics)

        val aiConfig = AiLanguageConfig(language = AppLanguage.FA, localeTag = "fa-IR", preferNativeResponse = true)
        assertEquals("fa-IR", aiConfig.localeTag)
        assertTrue(aiConfig.preferNativeResponse)

        val voiceConfig = VoiceLanguageConfig(language = AppLanguage.EN, speechRate = 1.1f, isSpokenFeedbackEnabled = true)
        assertEquals(1.1f, voiceConfig.speechRate)
        assertTrue(voiceConfig.isSpokenFeedbackEnabled)
    }

    @Test
    fun `viewmodel language switching updates appLanguage and synchronizes RTL direction`() {
        val viewModel = AvaViewModel()
        assertEquals(AppLanguage.EN, viewModel.uiState.value.appLanguage)
        assertFalse(viewModel.uiState.value.isPersianRtlEnabled)

        viewModel.setAppLanguage(AppLanguage.FA)
        assertEquals(AppLanguage.FA, viewModel.uiState.value.appLanguage)
        assertTrue("RTL should be enabled for Persian", viewModel.uiState.value.isPersianRtlEnabled)

        viewModel.setAppLanguage(AppLanguage.DE)
        assertEquals(AppLanguage.DE, viewModel.uiState.value.appLanguage)
        assertFalse("RTL should be disabled for German", viewModel.uiState.value.isPersianRtlEnabled)

        viewModel.togglePersianRtl()
        assertTrue(viewModel.uiState.value.isPersianRtlEnabled)
        assertEquals(AppLanguage.FA, viewModel.uiState.value.appLanguage)
    }

    // =========================================================================
    // 2. Accessibility by Design Foundation Tests
    // =========================================================================

    @Test
    fun `accessibility policy strictly enforces minimum 48dp touch targets`() {
        assertEquals(48.dp, TavanaTouchTargets.MIN_TOUCH_TARGET)
        assertTrue(TavanaTouchTargets.COMFORTABLE_TOUCH_TARGET >= 48.dp)
    }

    @Test
    fun `accessible audio descriptions generate informative text alternatives for deaf users`() {
        val silentDesc = AccessibleAudioDescriptions.describeInputLevel(0.01f)
        assertTrue("Must describe silence", silentDesc.contains("silent", ignoreCase = true))

        val optimalDesc = AccessibleAudioDescriptions.describeInputLevel(0.65f)
        assertTrue("Must describe optimal level", optimalDesc.contains("optimal", ignoreCase = true))

        val inTuneDesc = AccessibleAudioDescriptions.describePitchEvaluation("A4", 4)
        assertTrue("Must indicate in-tune", inTuneDesc.contains("Perfect tuning", ignoreCase = true))

        val sharpDesc = AccessibleAudioDescriptions.describePitchEvaluation("C5", 25)
        assertTrue("Must indicate sharp pitch", sharpDesc.contains("sharp", ignoreCase = true))

        val recordingDesc = AccessibleAudioDescriptions.describeRecordingState(true, 75L)
        assertTrue("Must describe duration elapsed", recordingDesc.contains("1 minutes 15 seconds"))
    }

    @Test
    fun `viewmodel accessibility profile updates correctly`() {
        val viewModel = AvaViewModel()
        assertFalse(viewModel.uiState.value.accessibilityProfile.isHighContrastEnabled)

        viewModel.updateAccessibilityProfile {
            it.copy(isHighContrastEnabled = true, fontScale = FontScaleMode.LARGE)
        }

        assertTrue(viewModel.uiState.value.accessibilityProfile.isHighContrastEnabled)
        assertEquals(FontScaleMode.LARGE, viewModel.uiState.value.accessibilityProfile.fontScale)
    }

    // =========================================================================
    // 3. Offline-First Foundation Tests
    // =========================================================================

    @Test
    fun `all core audio and studio features are categorized as OFFLINE_NATIVE`() {
        val nativeFeatures = listOf(
            StudioFeature.AUDIO_RECORDING,
            StudioFeature.AUDIO_PLAYBACK,
            StudioFeature.LOCAL_PROJECTS,
            StudioFeature.LOCAL_AUDIO_FILES,
            StudioFeature.BASIC_EDITING,
            StudioFeature.MULTI_TRACK_MIXER,
            StudioFeature.LOCAL_KARAOKE_LYRICS,
            StudioFeature.PITCH_AUDIO_ANALYSIS,
            StudioFeature.DSP_EFFECTS,
            StudioFeature.VOCAL_COACH_SCORING,
            StudioFeature.ACCESSIBILITY_SERVICES,
            StudioFeature.MULTILINGUAL_SWITCHING
        )

        for (feature in nativeFeatures) {
            assertEquals("${feature.name} must be OFFLINE_NATIVE", CapabilityTier.OFFLINE_NATIVE, feature.tier)
            assertTrue("${feature.name} must have documented offline behavior", feature.offlineBehavior.isNotBlank())
        }
    }

    @Test
    fun `cloud features have explicit boundaries and offline limitation descriptors`() {
        val cloudFeatures = listOf(
            StudioFeature.CLOUD_PROJECT_BACKUP,
            StudioFeature.REMOTE_AI_GENERATION,
            StudioFeature.LIVE_RADIO_BROADCAST,
            StudioFeature.PARTY_ROOM_COLLABORATION
        )

        for (feature in cloudFeatures) {
            assertEquals("${feature.name} must be ONLINE_REQUIRED", CapabilityTier.ONLINE_REQUIRED, feature.tier)
            val limitation = OfflineSafetyGuard.getLimitationNotice(feature)
            assertEquals(feature, limitation.feature)
            assertTrue(limitation.localAlternative.isNotBlank())
        }
    }

    @Test
    fun `offline safety guard executes local fallback and prevents crash when offline`() {
        var localFallbackCalled = false
        var cloudActionCalled = false

        val result = OfflineSafetyGuard.executeSafe(
            isOnline = false,
            onLocalFallback = {
                localFallbackCalled = true
                "LOCAL_SCORE"
            },
            onCloudAction = {
                cloudActionCalled = true
                "CLOUD_SCORE"
            }
        )

        assertEquals("LOCAL_SCORE", result)
        assertTrue("Local fallback must be called", localFallbackCalled)
        assertFalse("Cloud action must NOT be called when offline", cloudActionCalled)
    }

    @Test
    fun `offline safety guard handles exceptions in online block by falling back to local action`() {
        var localFallbackCalled = false

        val result = OfflineSafetyGuard.executeSafe(
            isOnline = true,
            onLocalFallback = {
                localFallbackCalled = true
                "FALLBACK_SUCCESS"
            },
            onCloudAction = {
                throw java.io.IOException("Simulated network loss / socket timeout")
            }
        )

        assertEquals("FALLBACK_SUCCESS", result)
        assertTrue("Local fallback must catch remote network exceptions gracefully", localFallbackCalled)
    }
}
