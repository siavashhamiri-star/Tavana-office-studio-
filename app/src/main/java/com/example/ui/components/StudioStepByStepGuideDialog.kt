package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AvaGoldenHighlight
import com.example.ui.theme.AvaSunsetCoral
import com.example.ui.theme.AvaTheme

/**
 * Data representation of an interactive step-by-step tutorial item.
 */
data class StudioGuideStep(
    val stepNumber: Int,
    val titleFa: String,
    val titleEn: String,
    val subtitleFa: String,
    val icon: ImageVector,
    val iconTint: Color,
    val instructionsFa: List<String>,
    val proTipFa: String,
    val actionButtonText: String,
    val onActionTrigger: () -> Unit
)

/**
 * Interactive, highly accessible Step-by-Step Guided Walkthrough Dialog for all app operations.
 */
@Composable
fun StudioStepByStepGuideDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToStage: () -> Unit = {},
    onOpenAiComposer: () -> Unit = {},
    onNavigateToStudio: () -> Unit = {},
    onNavigateToPractice: () -> Unit = {},
    onNavigateToRecordings: () -> Unit = {},
    initialStepIndex: Int = 0
) {
    if (!isOpen) return

    val brandPrimary = AvaTheme.colors.brandPrimary
    val guideSteps = remember(brandPrimary) {
        listOf(
            StudioGuideStep(
                stepNumber = 1,
                titleFa = "جستجو و انتخاب آهنگ دلخواه",
                titleEn = "Search & Pick Songs",
                subtitleFa = "پیدا کردن سریع آهنگ بر اساس نام، خواننده، یا سبک",
                icon = Icons.Default.Search,
                iconTint = brandPrimary,
                instructionsFa = listOf(
                    "روی کلید یا نوار جستجوی آهنگ در بالای صفحه بزنید.",
                    "نام آهنگ، خواننده یا کلمه‌ای از متن شعر را تایپ کنید (فارسی یا انگلیسی).",
                    "از دکمه‌های فیلتر سریع (پاپ، سنتی، بی‌کلام، هوش مصنوعی) برای دسته‌بندی استفاده کنید.",
                    "با زدن دکمه «بخوان / Sing»، آهنگ روی صحنه بارگذاری می‌شود."
                ),
                proTipFa = "نکته طلایی: اگر آهنگ خاصی در آرشیو نبود، دکمه «ساخت با هوش مصنوعی» دقیقاً همان ترانه را برایتان خلق می‌کند!",
                actionButtonText = "برو به بخش جستجوی آهنگ",
                onActionTrigger = {
                    onDismiss()
                    onNavigateToSearch()
                }
            ),
            StudioGuideStep(
                stepNumber = 2,
                titleFa = "اجرای زنده روی صحنه کارائوکه",
                titleEn = "Karaoke Stage & Live Singing",
                subtitleFa = "خواندن همگام با متن شعر و ضبط باکیفیت وکال",
                icon = Icons.Default.Mic,
                iconTint = AvaSunsetCoral,
                instructionsFa = listOf(
                    "با شروع آهنگ، کلمات شعر به صورت رنگی و همگام با ضرباهنگ مشخص می‌شوند.",
                    "با دکمه «شروع ضبط»، صدای شما همزمان با موسیقی متن رکورد می‌شود.",
                    "اگر نیاز دارید گام صدا بالا یا پایین‌تر برود، از دکمه Pitch استفاده کنید.",
                    "راهنمای ملودی (Guide) را فعال کنید تا صدای خواننده اصلی به شما کمک کند."
                ),
                proTipFa = "نکته طلایی: هنگام خواندن، خط نوت‌ها و دقت تحریر صدای شما به صورت زنده ارزیابی می‌شود.",
                actionButtonText = "ورود به صحنه خوانندگی",
                onActionTrigger = {
                    onDismiss()
                    onNavigateToStage()
                }
            ),
            StudioGuideStep(
                stepNumber = 3,
                titleFa = "مانیتورینگ صدا با هدفون",
                titleEn = "Zero-Latency Vocal Monitoring",
                subtitleFa = "شنیدن لحظه‌ای صدای خود در گوش برای فالش نخواندن",
                icon = Icons.Default.Headphones,
                iconTint = AvaGoldenHighlight,
                instructionsFa = listOf(
                    "پیش از شروع، هدفون باسیم یا هندزفری را متصل کنید.",
                    "آیکون هدفون روی صحنه را لمس کنید تا مانیتورینگ زنده فعال شود.",
                    "صدای حنجره شما بدون تاخیر از میکروفون به گوش منتقل می‌شود تا کوک بخوانید.",
                    "برای جلوگیری از صدای سوت (فیدبک)، حتماً از هدفون استفاده نمایید."
                ),
                proTipFa = "نکته طلایی: خوانندگان حرفه‌ای همیشه یک گوش هدفون را روی گوش قرار می‌دهند تا رزونانس طبیعی صدای خود را بشنوند.",
                actionButtonText = "تست و فعال‌سازی در صحنه",
                onActionTrigger = {
                    onDismiss()
                    onNavigateToStage()
                }
            ),
            StudioGuideStep(
                stepNumber = 4,
                titleFa = "ساخت آهنگ جدید با هوش مصنوعی",
                titleEn = "AI Song Composer",
                subtitleFa = "تولید ترانه، ملودی و آکورد بدون هیچ پیش‌نیاز",
                icon = Icons.Default.AutoAwesome,
                iconTint = AvaGoldenHighlight,
                instructionsFa = listOf(
                    "دکمه «AI Composer» در صفحه اصلی یا منو را بزنید.",
                    "سبک دلخواه (پاپ، شاد، آرام، سنتی) و حس و حال را انتخاب کنید.",
                    "دکمه «تولید ترانه و ساخت آهنگ» را لمس کنید.",
                    "در چند ثانیه قطعه‌ای با فایل صوتی واقعی و شعر همگام در آرشیو شما اضافه می‌شود!"
                ),
                proTipFa = "نکته طلایی: این موتور به صورت کاملاً خودکار فعال است و حتی در حالت آفلاین قطعات ملودیک زیبایی می‌سازد.",
                actionButtonText = "باز کردن آهنگساز هوش مصنوعی",
                onActionTrigger = {
                    onDismiss()
                    onOpenAiComposer()
                }
            ),
            StudioGuideStep(
                stepNumber = 5,
                titleFa = "تنظیمات استودیو و افکت‌های صوتی",
                titleEn = "Studio Mixer & Effects",
                subtitleFa = "افزودن ریورب، اکو، اکولایزر و میکس صدا با موزیک",
                icon = Icons.Default.Tune,
                iconTint = brandPrimary,
                instructionsFa = listOf(
                    "به تب «Studio» در نوار پایین برنامه بروید.",
                    "اسلایدر ریورب (Reverb) را زیاد کنید تا صدایتان فضاسازی کنسرتی پیدا کند.",
                    "بالانس ولوم صدای خود با صدای موزیک متن را به دقت تنظیم کنید.",
                    "از پریست‌های آماده (Vocal Studio، Hall، Warm Intimate) استفاده کنید."
                ),
                proTipFa = "نکته طلایی: برای سبک‌های پاپ، ریورب متوسط (۴۰٪) صدای گرم و مخملی به اجرای شما می‌دهد.",
                actionButtonText = "رفتن به میز میکسر استودیو",
                onActionTrigger = {
                    onDismiss()
                    onNavigateToStudio()
                }
            ),
            StudioGuideStep(
                stepNumber = 6,
                titleFa = "گرم کردن صدا و تمرین حنجره",
                titleEn = "Vocal Warmup & Agility",
                subtitleFa = "تمرین روزانه برای افزایش وسعت صدا و تثبیت کوک",
                icon = Icons.Default.MusicNote,
                iconTint = AvaSunsetCoral,
                instructionsFa = listOf(
                    "به تب «Practice» بروید و یک تمرین انتخاب کنید.",
                    "نت پخش‌شده را گوش دهید و صدای خود را با همان فرکانس ادا کنید.",
                    "سنجش هوشمند صدا میزان دقت نت شما را در لحظه اندازه می‌گیرد.",
                    "هر روز ۳ دقیقه قبل از خواندن آهنگ اصلی، حنجره‌تان را گرم کنید."
                ),
                proTipFa = "نکته طلایی: گرم کردن مانع از خستگی و گرفتگی تارهای صوتی در نت‌های اوج می‌شود.",
                actionButtonText = "ورود به تمرینات روزانه",
                onActionTrigger = {
                    onDismiss()
                    onNavigateToPractice()
                }
            ),
            StudioGuideStep(
                stepNumber = 7,
                titleFa = "ذخیره، ارزیابی و اشتراک‌گذاری",
                titleEn = "Save, Score & Share",
                subtitleFa = "مشاهده نمره هوشمند اجرا و ارسال قطعه صوتی به دیگران",
                icon = Icons.Default.Share,
                iconTint = brandPrimary,
                instructionsFa = listOf(
                    "پس از اتمام ترانه، کارت ارزیابی هوش مصنوعی نمره دقت تحریر را به شما می‌دهد.",
                    "دکمه «ذخیره اجرا» را بزنید تا فایل صوتی WAV در آرشیو ضبط‌ها ذخیره شود.",
                    "به تب «Recordings» بروید و اجرای خود را دوباره با دقت گوش کنید.",
                    "با دکمه «اشتراک‌گذاری»، فایل موسیقی را مستقیماً به تلگرام، واتساپ یا اینستاگرام بفرستید."
                ),
                proTipFa = "نکته طلایی: فایل‌های ضبط شده با فرمت بدون افت کیفیت (WAV) ذخیره می‌شوند و برای اشتراک عالی هستند.",
                actionButtonText = "مشاهده آرشیو ضبط‌های من",
                onActionTrigger = {
                    onDismiss()
                    onNavigateToRecordings()
                }
            )
        )
    }

    var currentStepIndex by remember(initialStepIndex) {
        mutableIntStateOf(initialStepIndex.coerceIn(0, guideSteps.lastIndex))
    }
    val currentStep = guideSteps[currentStepIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("dialog_studio_guide"),
            shape = RoundedCornerShape(24.dp),
            color = AvaTheme.colors.stageSurfaceElevated,
            tonalElevation = 16.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, AvaTheme.colors.stageBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(AvaTheme.spacing.medium)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(AvaTheme.colors.brandPrimary, AvaSunsetCoral)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(AvaTheme.spacing.small))
                        Column {
                            Text(
                                text = "راهنمای گام‌به‌گام استودیو",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "راهنمای سریع و آسان تمامی امکانات برنامه",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن راهنما",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

                // Step Selector Chips (Interactive Horizontal Carousel)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(guideSteps) { index, step ->
                        val isSelected = index == currentStepIndex
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AvaTheme.colors.brandPrimary else AvaTheme.colors.stageSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AvaTheme.colors.brandPrimary else AvaTheme.colors.stageBorder
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { currentStepIndex = index }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "گام ${step.stepNumber}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

                // Active Step Content with Animated Switch
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "step_content_anim"
                ) { step ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AvaTheme.colors.stageSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AvaTheme.colors.stageBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(AvaTheme.spacing.medium)) {
                            // Step Title & Icon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(step.iconTint.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = step.icon,
                                        contentDescription = null,
                                        tint = step.iconTint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(AvaTheme.spacing.medium))
                                Column {
                                    Text(
                                        text = "گام ${step.stepNumber}: ${step.titleFa}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = step.subtitleFa,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = step.iconTint
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

                            // Instructions List
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                step.instructionsFa.forEachIndexed { i, instruction ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(step.iconTint.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${i + 1}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                ),
                                                color = step.iconTint
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = instruction,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

                            // Pro Tip Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AvaGoldenHighlight.copy(alpha = 0.12f))
                                    .border(1.dp, AvaGoldenHighlight.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AvaGoldenHighlight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = step.proTipFa,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

                            // Action Button ("الان امتحان کن")
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = step.iconTint,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClick = step.onActionTrigger)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = step.icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = step.actionButtonText,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

                // Navigation Controls (Prev / Next Buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AvaTheme.colors.stageSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AvaTheme.colors.stageBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { currentStepIndex-- }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "گام قبلی",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Text(
                        text = "${currentStepIndex + 1} از ${guideSteps.size}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (currentStepIndex < guideSteps.lastIndex) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AvaTheme.colors.brandPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { currentStepIndex++ }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "گام بعدی",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AvaTheme.colors.stageSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AvaTheme.colors.stageBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onDismiss() }
                        ) {
                            Text(
                                text = "متوجه شدم (پایان)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = AvaTheme.colors.brandPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact, friendly contextual Step-by-Step Card embedded inside screens.
 * Makes the app feel deeply helpful, friendly, and easy to navigate!
 */
@Composable
fun ContextualQuickGuideCard(
    title: String,
    subtitle: String,
    steps: List<String>,
    onOpenFullGuide: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.HelpOutline,
    iconTint: Color = AvaTheme.colors.brandPrimary
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AvaTheme.colors.stageSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, iconTint.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(AvaTheme.spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconTint.copy(alpha = 0.15f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenFullGuide)
                ) {
                    Text(
                        text = "راهنمای کامل",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = iconTint
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Short scannable steps
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                steps.forEachIndexed { idx, stepText ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(iconTint.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = iconTint
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stepText,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
