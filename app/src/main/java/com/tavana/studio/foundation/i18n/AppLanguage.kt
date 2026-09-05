package com.tavana.studio.foundation.i18n

/**
 * Supported language definitions for TAVANA Studio Internationalization Foundation.
 * Prepares the architecture for 11 primary languages with native RTL/LTR and Unicode support.
 */
enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val isRtl: Boolean,
    val script: UnicodeScript
) {
    FA(code = "fa", nativeName = "فارسی", englishName = "Persian", isRtl = true, script = UnicodeScript.PERSIAN_ARABIC),
    EN(code = "en", nativeName = "English", englishName = "English", isRtl = false, script = UnicodeScript.LATIN),
    AR(code = "ar", nativeName = "العربية", englishName = "Arabic", isRtl = true, script = UnicodeScript.ARABIC),
    FR(code = "fr", nativeName = "Français", englishName = "French", isRtl = false, script = UnicodeScript.LATIN),
    ES(code = "es", nativeName = "Español", englishName = "Spanish", isRtl = false, script = UnicodeScript.LATIN),
    DE(code = "de", nativeName = "Deutsch", englishName = "German", isRtl = false, script = UnicodeScript.LATIN),
    RU(code = "ru", nativeName = "Русский", englishName = "Russian", isRtl = false, script = UnicodeScript.CYRILLIC),
    HI(code = "hi", nativeName = "हिन्दी", englishName = "Hindi", isRtl = false, script = UnicodeScript.DEVANAGARI),
    ZH(code = "zh", nativeName = "中文", englishName = "Chinese", isRtl = false, script = UnicodeScript.HAN),
    TL(code = "tl", nativeName = "Tagalog", englishName = "Filipino / Tagalog", isRtl = false, script = UnicodeScript.LATIN),
    TH(code = "th", nativeName = "ไทย", englishName = "Thai", isRtl = false, script = UnicodeScript.THAI),
    TR(code = "tr", nativeName = "Türkçe", englishName = "Turkish", isRtl = false, script = UnicodeScript.LATIN);

    companion object {
        fun fromCode(code: String): AppLanguage {
            val normalized = code.lowercase().trim()
            return entries.firstOrNull { it.code == normalized } ?: EN
        }
    }
}

/**
 * Unicode script classification for font rendering and typography shaping.
 */
enum class UnicodeScript {
    LATIN,
    PERSIAN_ARABIC,
    ARABIC,
    CYRILLIC,
    DEVANAGARI,
    HAN,
    THAI
}

/**
 * Extensible configuration for multilingual lyrics rendering, transliteration, and directionality.
 */
data class LyricsLanguageConfig(
    val language: AppLanguage = AppLanguage.EN,
    val isBidiSupported: Boolean = true,
    val showPhonetics: Boolean = false,
    val fontScaleMultiplier: Float = 1.0f
)

/**
 * Extensible configuration for AI system prompts, localized evaluation, and translation.
 */
data class AiLanguageConfig(
    val language: AppLanguage = AppLanguage.EN,
    val localeTag: String = language.code,
    val preferNativeResponse: Boolean = true
)

/**
 * Extensible configuration for spoken coach prompts, voice cues, and metronome count-ins.
 */
data class VoiceLanguageConfig(
    val language: AppLanguage = AppLanguage.EN,
    val ttsLocaleCode: String = language.code,
    val speechRate: Float = 1.0f,
    val isSpokenFeedbackEnabled: Boolean = true
)
