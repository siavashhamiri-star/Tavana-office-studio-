package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AvaSunsetCoral
import com.example.ui.theme.AvaTheme
import com.tavana.studio.audio.diagnostics.AudioDiagnosticsManager
import com.tavana.studio.audio.diagnostics.MicDiagnosticTestState

enum class AudioDiagTab {
    STATUS_AND_TESTS,
    TROUBLESHOOTING_GUIDE
}

@Composable
fun AudioMonitoringDialog(
    diagnosticsManager: AudioDiagnosticsManager,
    isVoiceMonitoringEnabled: Boolean,
    onToggleVoiceMonitoring: () -> Unit,
    liveAudioLevel: Float,
    onRequestMicPermission: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val routeInfo by diagnosticsManager.routeInfo.collectAsState()
    val micTestState by diagnosticsManager.micTestState.collectAsState()
    val isSpeakerTestPlaying by diagnosticsManager.isSpeakerTestPlaying.collectAsState()

    var selectedTab by remember { mutableStateOf(AudioDiagTab.STATUS_AND_TESTS) }
    var expandedTroubleIndex by remember { mutableStateOf<Int?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, AvaTheme.colors.stageBorder, RoundedCornerShape(24.dp))
                .testTag("audio_monitoring_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Dialog Header
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
                                .background(AvaSunsetCoral.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = AvaSunsetCoral,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "مانیتورینگ افکت و عیب‌یابی صدا",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "بررسی زنده هدست، میکروفون، بلندگو و رفع قطعی صدا",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { diagnosticsManager.refreshAudioStatus() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh audio status",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AvaTheme.colors.stageSurfaceElevated)
                        .padding(4.dp)
                ) {
                    TabButton(
                        text = "وضعیت سخت‌افزار و تست زنده",
                        selected = selectedTab == AudioDiagTab.STATUS_AND_TESTS,
                        onClick = { selectedTab = AudioDiagTab.STATUS_AND_TESTS },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        text = "راهنمای رفع مشکل قطعی صدا",
                        selected = selectedTab == AudioDiagTab.TROUBLESHOOTING_GUIDE,
                        onClick = { selectedTab = AudioDiagTab.TROUBLESHOOTING_GUIDE },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content scroll area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedTab) {
                        AudioDiagTab.STATUS_AND_TESTS -> {
                            // Section 1: Live Hardware Status Cards
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AvaTheme.colors.stageSurface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "۱. وضعیت زنده سخت‌افزار صوتی",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AvaSunsetCoral
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Microphone Status Row
                                    StatusRow(
                                        icon = Icons.Default.Mic,
                                        title = "میکروفون ورودی (Microphone)",
                                        subtitle = if (routeInfo.isMicPermissionGranted) {
                                            if (routeInfo.isMicMutedInSystem) "هشدار: میکروفون در حریم خصوصی گوشی MUTE است!"
                                            else "فعال و آماده دریافت صدا (سطح زنده در نوار زیر)"
                                        } else "غیرفعال: نیاز به اعطای دسترسی میکروفون دارد",
                                        isOk = routeInfo.isMicPermissionGranted && !routeInfo.isMicMutedInSystem,
                                        actionButton = if (!routeInfo.isMicPermissionGranted) {
                                            {
                                                Button(
                                                    onClick = onRequestMicPermission,
                                                    colors = ButtonDefaults.buttonColors(containerColor = AvaSunsetCoral),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("اعطای مجوز", fontSize = 11.sp)
                                                }
                                            }
                                        } else null
                                    )

                                    // Live Mic Level VU Meter
                                    if (routeInfo.isMicPermissionGranted) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "تراز زنده ورودی:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            LinearProgressIndicator(
                                                progress = { liveAudioLevel.coerceIn(0f, 1f) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(8.dp)
                                                    .clip(CircleShape),
                                                color = if (liveAudioLevel > 0.8f) Color(0xFFE53935) else AvaSunsetCoral,
                                                trackColor = AvaTheme.colors.stageSurfaceElevated
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${(liveAudioLevel * 100).toInt()}%",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        color = AvaTheme.colors.stageBorder
                                    )

                                    // Audio Output Device (Headset / Speaker)
                                    StatusRow(
                                        icon = if (routeInfo.isHeadsetConnected || routeInfo.isBluetoothAudioConnected) Icons.Default.Headphones else Icons.Default.Speaker,
                                        title = "دستگاه خروجی صدا (Output Route)",
                                        subtitle = routeInfo.activeOutputDeviceName,
                                        isOk = true,
                                        actionButton = null
                                    )

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        color = AvaTheme.colors.stageBorder
                                    )

                                    // Media Volume
                                    StatusRow(
                                        icon = if (routeInfo.isMediaMuted) Icons.Default.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                        title = "میزان صدای رسانه (Media Volume)",
                                        subtitle = if (routeInfo.isMediaMuted) "هشدار: صدای رسانه روی ۰ یا قطع (Muted) است!"
                                        else "${routeInfo.mediaVolumePercent}% — وضعیت زنگ: ${routeInfo.ringerMode}",
                                        isOk = !routeInfo.isMediaMuted && routeInfo.mediaVolumePercent >= 30,
                                        actionButton = if (routeInfo.isMediaMuted || routeInfo.mediaVolumePercent < 40) {
                                            {
                                                Button(
                                                    onClick = { diagnosticsManager.boostMediaVolume() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = AvaSunsetCoral),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("افزایش صدا", fontSize = 11.sp)
                                                }
                                            }
                                        } else null
                                    )
                                }
                            }

                            // Section 2: Ear Monitoring Controller
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AvaTheme.colors.stageSurface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "۲. مانیتورینگ افکت صدای زنده (Ear Monitoring)",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = AvaSunsetCoral
                                            )
                                            Text(
                                                text = if (isVoiceMonitoringEnabled) "فعال — صدای خواندن خود را زنده در هدفون می‌شنوید"
                                                else "غیرفعال — خاموش برای جلوگیری از سوت کشیدن اسپیکر",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = isVoiceMonitoringEnabled,
                                            onCheckedChange = { onToggleVoiceMonitoring() },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = AvaSunsetCoral
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (routeInfo.isHeadsetConnected || routeInfo.isBluetoothAudioConnected)
                                                    Color(0xFF2E7D32).copy(alpha = 0.12f)
                                                else Color(0xFFF57C00).copy(alpha = 0.12f)
                                            )
                                            .padding(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (routeInfo.isHeadsetConnected || routeInfo.isBluetoothAudioConnected)
                                                    Icons.Default.CheckCircle else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (routeInfo.isHeadsetConnected || routeInfo.isBluetoothAudioConnected)
                                                    Color(0xFF2E7D32) else Color(0xFFF57C00),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (routeInfo.isHeadsetConnected || routeInfo.isBluetoothAudioConnected)
                                                    "هدفون متصل است. می‌توانید مانیتورینگ زنده را با خیال راحت روشن کنید."
                                                else "نکته ایمنی: هدفون متصل نیست. بدون هدفون اگر مانیتورینگ روشن باشد صدا در اسپیکر می‌پیچد و سوت می‌کشد!",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // Section 3: Interactive Diagnostics Tests
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AvaTheme.colors.stageSurface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "۳. آزمون‌های تعاملی عیب‌یابی صدا",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AvaSunsetCoral
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Test A: Speaker / Headphone chime test
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "آزمون ۱: تست خروجی بلندگو و هدست",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "پخش زنگ هارمونیک ۴ نت برای اطمینان از شنیده شدن صدا",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Button(
                                            onClick = { diagnosticsManager.playSpeakerAndHeadsetTestChime() },
                                            enabled = !isSpeakerTestPlaying,
                                            colors = ButtonDefaults.buttonColors(containerColor = AvaSunsetCoral),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            if (isSpeakerTestPlaying) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("در حال پخش...", fontSize = 12.sp)
                                            } else {
                                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("تست بلندگو", fontSize = 12.sp)
                                            }
                                        }
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = AvaTheme.colors.stageBorder
                                    )

                                    // Test B: 3-Second Mic Loopback Test
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "آزمون ۲: تست ۳ ثانیه‌ای ضبط و پخش میکروفون",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "۳ ثانیه صدای شما را ضبط کرده و بلافاصله پخش می‌کند",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Button(
                                                onClick = { diagnosticsManager.startMicDiagnosticTest() },
                                                enabled = micTestState !is MicDiagnosticTestState.Recording && micTestState !is MicDiagnosticTestState.PlayingBack,
                                                colors = ButtonDefaults.buttonColors(containerColor = AvaSunsetCoral),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("شروع تست مایک", fontSize = 12.sp)
                                            }
                                        }

                                        // Test feedback banner
                                        when (val state = micTestState) {
                                            is MicDiagnosticTestState.Recording -> {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFE53935).copy(alpha = 0.15f))
                                                        .padding(10.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(16.dp),
                                                            color = Color(0xFFE53935),
                                                            strokeWidth = 2.dp
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "در حال ضبط صدای شما... صحبت کنید! (${state.secondsLeft} ثانیه مانده)",
                                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                            color = Color(0xFFE53935)
                                                        )
                                                    }
                                                }
                                            }
                                            is MicDiagnosticTestState.PlayingBack -> {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                                        .padding(10.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "در حال پخش صدای ضبط شده شما... آیا صدا را می‌شنوید؟ (${state.secondsLeft} ثانیه)",
                                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                            color = Color(0xFF2E7D32)
                                                        )
                                                    }
                                                }
                                            }
                                            is MicDiagnosticTestState.Success -> {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                                        .padding(10.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = state.message,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color(0xFF2E7D32)
                                                        )
                                                    }
                                                }
                                            }
                                            is MicDiagnosticTestState.Failed -> {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFE53935).copy(alpha = 0.15f))
                                                        .padding(10.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = state.reason,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color(0xFFE53935)
                                                        )
                                                    }
                                                }
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            }
                        }

                        AudioDiagTab.TROUBLESHOOTING_GUIDE -> {
                            // Detailed Root-Cause Troubleshooter for the 4 scenarios
                            Text(
                                text = "بررسی دلایل قطعی صدا و نحوه حل آن‌ها:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            TroubleCard(
                                title = "۱. چرا صدای میکروفون را موقع خواندن نمی‌شنوم؟",
                                isOpen = expandedTroubleIndex == 0,
                                onToggle = { expandedTroubleIndex = if (expandedTroubleIndex == 0) null else 0 },
                                content = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "• دلیل اول (محافظت از بلندگو): در سیستم اندروید، تا زمانی که هدفون وصل نکرده باشید و کلید «Ear Monitoring» را روشن نکنید، صدای میکروفون به بلندگو فرستاده نمی‌شود چون فیدبک شدید صوتی و سوت کشیدن مخرب رخ می‌دهد.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "• دلیل دوم: کلید Ear Monitoring روی صفحه خاموش است. با زدن دکمه هدست در بالای این پنل یا در استیج آن را روشن کنید.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "• دلیل سوم: مجوز دسترسی به میکروفون تایید نشده یا کلید فیزیکی قطع میکروفون در منوی حریم خصوصی اندروید فعال است.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            )

                            TroubleCard(
                                title = "۲. چرا صدای هدست یا هندزفری را نمی‌شنوم؟",
                                isOpen = expandedTroubleIndex == 1,
                                onToggle = { expandedTroubleIndex = if (expandedTroubleIndex == 1) null else 1 },
                                content = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "• دلیل اول: در گوشی‌های جدید، میزان صدای بلوتوث جداگانه از اسپیکر است. هنگام اتصال هندزفری دکمه افزایش صدای گوشی را بزنید.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "• دلیل دوم: جک هندزفری سیمی یا رابط تبدیل به طور کامل جا نرفته است.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "• دلیل سوم: هندزفری بلوتوث فقط برای تماس فعال شده و دسترسی مدیا ندارد؛ در تنظیمات بلوتوث تیک Media Audio را بررسی کنید.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            )

                            TroubleCard(
                                title = "۳. چرا صدای بلندگوی گوشی یا موسیقی را نمی‌شنوم؟",
                                isOpen = expandedTroubleIndex == 2,
                                onToggle = { expandedTroubleIndex = if (expandedTroubleIndex == 2) null else 2 },
                                content = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "• دلیل اول: ولوم رسانه (Media Volume) روی صفر یا بی‌صدا است. دکمه «افزایش صدا» در این صفحه را فشار دهید.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "• دلیل دوم: گوشی ممکن است فکر کند هندزفری وصل است (در صورت گرد و غبار در جک صدا).",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "• دلیل سوم: دستگاه روی حالت Do Not Disturb یا سایلنت کامل است.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            )

                            TroubleCard(
                                title = "۴. چرا صدای موزیک ضبط شده یا وکال را نمی‌شنوم؟",
                                isOpen = expandedTroubleIndex == 3,
                                onToggle = { expandedTroubleIndex = if (expandedTroubleIndex == 3) null else 3 },
                                content = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "• دلیل اول: در بخش میکس استودیو، ممکن است لاین وکال یا موسیقی دکمه Mute (بی‌صدا) خورده باشد یا ولوم مستر پایین باشد.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "• دلیل دوم: ضبط کمتر از ۱ ثانیه انجام شده یا صدایی به میکروفون نرسیده است (تست ۲ را برای تست مایک انجام دهید).",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Action Bar at Bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { diagnosticsManager.openSystemSoundSettings() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تنظیمات صدای گوشی", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AvaSunsetCoral),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("متوجه شدم", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) AvaSunsetCoral else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun StatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isOk: Boolean,
    actionButton: (@Composable () -> Unit)?
) {
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
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isOk) Color(0xFF2E7D32).copy(alpha = 0.15f)
                        else Color(0xFFE53935).copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isOk) Color(0xFF2E7D32) else Color(0xFFE53935),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (isOk) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFE53935)
                )
            }
        }

        if (actionButton != null) {
            Spacer(modifier = Modifier.width(8.dp))
            actionButton()
        }
    }
}

@Composable
private fun TroubleCard(
    title: String,
    isOpen: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AvaTheme.colors.stageSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (isOpen) "▲ بستن" else "▼ مشاهده راهکار",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AvaSunsetCoral
                )
            }

            AnimatedVisibility(visible = isOpen) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = AvaTheme.colors.stageBorder)
                    Spacer(modifier = Modifier.height(10.dp))
                    content()
                }
            }
        }
    }
}
