package com.tavana.studio.foundation.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Foundational contract for all localized string resources in TAVANA Studio.
 * Ensures that UI labels, audio feedback, accessibility semantics, and error
 * messages are never hardcoded.
 */
interface TavanaStrings {
    // App & Identity
    val appName: String
    val appTagline: String

    // Navigation Tabs
    val tabStage: String
    val tabPractice: String
    val tabRecordings: String
    val tabStudio: String

    // Core Audio Actions
    val actionRecord: String
    val actionStop: String
    val actionPlay: String
    val actionPause: String
    val actionSaveTake: String
    val actionSingAgain: String
    val actionExport: String

    // Audio & Vocal Controls
    val labelVocalGuideOn: String
    val labelVocalGuideOff: String
    val labelPitchShift: String
    val labelTempoSpeed: String
    val labelMasterVolume: String
    val labelLeadVocal: String
    val labelBackingTrack: String
    val labelVocalGuideTrack: String
    val labelMute: String
    val labelSolo: String

    // Workspaces
    val workspacePersonal: String
    val workspaceEducation: String
    val workspaceProfessional: String
    val workspaceEntertainment: String

    // Status & Feedback
    val statusReady: String
    val statusRecording: String
    val statusPlaying: String
    val statusEvaluating: String
    val statusTakeSaved: String

    // Offline Indicators & Limitations
    val offlineStatusLocalOnly: String
    val offlineStatusOnline: String
    val offlineLimitationNotice: String

    // Accessibility Labels & Spoken Feedback
    val a11yRecordingActive: String
    val a11yPlayheadPosition: String
    val a11yVocalPitchDeviation: String
    val a11yAudioInputLevel: String
}

object EnglishStrings : TavanaStrings {
    override val appName = "TAVANA Studio"
    override val appTagline = "Your Digital Life & Work"

    override val tabStage = "Stage"
    override val tabPractice = "Practice"
    override val tabRecordings = "Recordings"
    override val tabStudio = "Studio"

    override val actionRecord = "Record"
    override val actionStop = "Stop"
    override val actionPlay = "Play"
    override val actionPause = "Pause"
    override val actionSaveTake = "Save Take"
    override val actionSingAgain = "Sing Again"
    override val actionExport = "Export"

    override val labelVocalGuideOn = "Guide: ON"
    override val labelVocalGuideOff = "Guide: OFF"
    override val labelPitchShift = "Pitch Shift"
    override val labelTempoSpeed = "Speed"
    override val labelMasterVolume = "Master Volume"
    override val labelLeadVocal = "Lead Vocal"
    override val labelBackingTrack = "Backing Track"
    override val labelVocalGuideTrack = "Vocal Guide"
    override val labelMute = "Mute"
    override val labelSolo = "Solo"

    override val workspacePersonal = "Personal Space"
    override val workspaceEducation = "Education & Training"
    override val workspaceProfessional = "Professional Studio"
    override val workspaceEntertainment = "Entertainment & Karaoke"

    override val statusReady = "Ready"
    override val statusRecording = "Recording in Progress"
    override val statusPlaying = "Playing"
    override val statusEvaluating = "Analyzing Vocal Performance..."
    override val statusTakeSaved = "Take saved to internal storage"

    override val offlineStatusLocalOnly = "Offline Mode (Local Storage & DSP Active)"
    override val offlineStatusOnline = "Connected"
    override val offlineLimitationNotice = "Internet required for Cloud Sync and AI Generation."

    override val a11yRecordingActive = "Recording is currently active. Audio level:"
    override val a11yPlayheadPosition = "Playhead position:"
    override val a11yVocalPitchDeviation = "Pitch deviation in cents:"
    override val a11yAudioInputLevel = "Microphone input level percentage:"
}

object PersianStrings : TavanaStrings {
    override val appName = "استودیو توانا"
    override val appTagline = "زندگی و کار دیجیتال شما"

    override val tabStage = "استیج"
    override val tabPractice = "تمرین"
    override val tabRecordings = "ضبط‌ها"
    override val tabStudio = "استودیو"

    override val actionRecord = "ضبط"
    override val actionStop = "توقف"
    override val actionPlay = "پخش"
    override val actionPause = "مکث"
    override val actionSaveTake = "ذخیره برداشت"
    override val actionSingAgain = "خواندن مجدد"
    override val actionExport = "خروجی نهایی"

    override val labelVocalGuideOn = "راهنما: روشن"
    override val labelVocalGuideOff = "راهنما: خاموش"
    override val labelPitchShift = "تغییر گام"
    override val labelTempoSpeed = "سرعت ریتم"
    override val labelMasterVolume = "حجم صدای اصلی"
    override val labelLeadVocal = "وکال اصلی"
    override val labelBackingTrack = "موسیقی زمینه"
    override val labelVocalGuideTrack = "راهنمای خواننده"
    override val labelMute = "بی‌صدا"
    override val labelSolo = "تک‌نوازی"

    override val workspacePersonal = "فضای شخصی"
    override val workspaceEducation = "آموزش و تمرین"
    override val workspaceProfessional = "استودیوی حرفه‌ای"
    override val workspaceEntertainment = "سرگرمی و کارائوکه"

    override val statusReady = "آماده به کار"
    override val statusRecording = "در حال ضبط صدا..."
    override val statusPlaying = "در حال پخش"
    override val statusEvaluating = "در حال تحلیل و ارزیابی اجرای آوازی..."
    override val statusTakeSaved = "برداشت با موفقیت در حافظه محلی ذخیره شد"

    override val offlineStatusLocalOnly = "حالت آفلاین (موتور صوتی و حافظه محلی فعال است)"
    override val offlineStatusOnline = "متصل به شبکه"
    override val offlineLimitationNotice = "برای همگام‌سازی ابری و هوش مصنوعی اتصال اینترنت الزامی است."

    override val a11yRecordingActive = "ضبط صدا در حال انجام است. سطح سیگنال:"
    override val a11yPlayheadPosition = "موقعیت پخش:"
    override val a11yVocalPitchDeviation = "انحراف فرکانس نت به سنت:"
    override val a11yAudioInputLevel = "درصد سطح ورودی میکروفن:"
}

object ArabicStrings : TavanaStrings {
    override val appName = "استوديو توانا"
    override val appTagline = "حياتك وعملك الرقمي"

    override val tabStage = "المسرح"
    override val tabPractice = "التدريب"
    override val tabRecordings = "التسجيلات"
    override val tabStudio = "الاستوديو"

    override val actionRecord = "تسجيل"
    override val actionStop = "إيقاف"
    override val actionPlay = "تشغيل"
    override val actionPause = "إيقاف مؤقت"
    override val actionSaveTake = "حفظ المقطع"
    override val actionSingAgain = "إعادة الغناء"
    override val actionExport = "تصدير"

    override val labelVocalGuideOn = "المرشد: مفعّل"
    override val labelVocalGuideOff = "المرشد: معطّل"
    override val labelPitchShift = "تغيير الطبقة"
    override val labelTempoSpeed = "السرعة"
    override val labelMasterVolume = "الصوت الرئيسي"
    override val labelLeadVocal = "الصوت الرئيسي"
    override val labelBackingTrack = "الموسيقى الخلفية"
    override val labelVocalGuideTrack = "الصوت الإرشادي"
    override val labelMute = "كتم"
    override val labelSolo = "عزف منفرد"

    override val workspacePersonal = "المساحة الشخصية"
    override val workspaceEducation = "التعليم والتدريب"
    override val workspaceProfessional = "الاستوديو الاحترافي"
    override val workspaceEntertainment = "الترفيه والكاريوكي"

    override val statusReady = "جاهز"
    override val statusRecording = "جاري التسجيل..."
    override val statusPlaying = "قيد التشغيل"
    override val statusEvaluating = "جاري تقييم الأداء الصوتي..."
    override val statusTakeSaved = "تم حفظ التسجيل في الذاكرة المحلية"

    override val offlineStatusLocalOnly = "وضع عدم الاتصال (المعالجة الصوتية والتخزين المحلي يعملان بنجاح)"
    override val offlineStatusOnline = "متصل بالإنترنت"
    override val offlineLimitationNotice = "الاتصال بالإنترنت مطلوب للمزامنة السحابية وتوليد الذكاء الاصطناعي."

    override val a11yRecordingActive = "التسجيل نشط حالياً. مستوى الإشارة:"
    override val a11yPlayheadPosition = "موضع التشغيل:"
    override val a11yVocalPitchDeviation = "انحراف النغمة بالدرجة المئوية:"
    override val a11yAudioInputLevel = "نسبة مستوى إدخال الميكروفون:"
}

/**
 * Registry mapping AppLanguage to its corresponding TavanaStrings.
 * For languages whose direct translations are pending, it provides a seamless
 * fallback to EnglishStrings to prevent any crashes or empty text blocks.
 */
object TavanaStringsRegistry {
    fun getStrings(language: AppLanguage): TavanaStrings {
        return when (language) {
            AppLanguage.FA -> PersianStrings
            AppLanguage.AR -> ArabicStrings
            AppLanguage.EN -> EnglishStrings
            AppLanguage.ES -> SpanishStrings
            AppLanguage.TR -> TurkishStrings
            AppLanguage.HI -> HindiStrings
            AppLanguage.TH -> ThaiStrings
            AppLanguage.TL -> FilipinoStrings
            AppLanguage.FR,
            AppLanguage.DE,
            AppLanguage.RU,
            AppLanguage.ZH -> EnglishStrings
        }
    }
}

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.EN }
val LocalTavanaStrings = staticCompositionLocalOf<TavanaStrings> { EnglishStrings }

object TavanaI18n {
    val currentLanguage: AppLanguage
        @Composable
        @ReadOnlyComposable
        get() = LocalAppLanguage.current

    val strings: TavanaStrings
        @Composable
        @ReadOnlyComposable
        get() = LocalTavanaStrings.current
}
