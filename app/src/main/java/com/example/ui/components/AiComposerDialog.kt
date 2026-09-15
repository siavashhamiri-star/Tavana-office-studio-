package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AvaGoldenHighlight
import com.example.ui.theme.AvaScoreHigh
import com.example.ui.theme.AvaSunsetCoral
import com.example.ui.theme.AvaTheme
import com.tavana.studio.ai.composer.AiSongResult
import com.tavana.studio.audio.sharing.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiComposerDialog(
    isGenerating: Boolean,
    generatedResult: AiSongResult?,
    isPlayingMelody: Boolean,
    onGenerateSong: (prompt: String, style: String, mood: String, language: String) -> Unit,
    onTogglePlayMelody: () -> Unit,
    onLoadToStageForRecording: (AiSongResult) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var promptInput by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("پاپ") }
    var selectedMood by remember { mutableStateOf("عاشقانه") }
    var selectedLang by remember { mutableStateOf("fa") }

    val styles = listOf("پاپ", "سنتی", "رپ و هیپ‌هاپ", "راک", "آراندبی")
    val moods = listOf("عاشقانه", "شاد و پرانرژی", "نوستالژیک و دلتنگ", "امیدبخش و انگیزشی")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("ai_composer_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AvaTheme.spacing.medium)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AvaGoldenHighlight.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AvaGoldenHighlight
                        )
                    }
                    Spacer(modifier = Modifier.width(AvaTheme.spacing.small))
                    Column {
                        Text(
                            text = "هوش مصنوعی ساخت شعر و آهنگ",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "تولید ترانه، آکورد و ملودی اختصاصی برای ضبط وکال",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
            }

            Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

            // Topic / Prompt Input
            Text(
                text = "موضوع ترانه و ایده شعر:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = { Text("مثلاً: عشق در باران پاییزی، امید به فردا، صدای خیابان...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_composer_prompt_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

            // Style Selection
            Text(
                text = "سبک موسیقی:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(styles) { st ->
                    val isSelected = selectedStyle == st
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStyle = st },
                        label = { Text(st, fontSize = 13.sp) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AvaGoldenHighlight.copy(alpha = 0.2f),
                            selectedLabelColor = AvaGoldenHighlight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(AvaTheme.spacing.small))

            // Mood Selection
            Text(
                text = "حس و حال ترانه:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(moods) { md ->
                    val isSelected = selectedMood == md
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMood = md },
                        label = { Text(md, fontSize = 13.sp) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AvaSunsetCoral.copy(alpha = 0.2f),
                            selectedLabelColor = AvaSunsetCoral
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

            // Generate Button
            AvaPrimaryButton(
                text = if (isGenerating) "در حال سرودن و ساخت آهنگ..." else "✨ ساخت شعر و ملودی با هوش مصنوعی",
                onClick = {
                    onGenerateSong(promptInput, selectedStyle, selectedMood, selectedLang)
                },
                enabled = !isGenerating,
                icon = Icons.Default.AutoAwesome,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_generate_button")
            )

            if (isGenerating) {
                Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AvaGoldenHighlight)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "هوش مصنوعی در حال سرایش ابیات و تنظیم ملودی...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Display Generated Result
            generatedResult?.let { result ->
                Spacer(modifier = Modifier.height(AvaTheme.spacing.large))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, AvaGoldenHighlight.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Column(modifier = Modifier.padding(AvaTheme.spacing.medium)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = result.title,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = AvaGoldenHighlight
                                )
                                Text(
                                    text = "${result.style} • ${result.musicalKey} • ${result.tempoBpm} BPM",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                ShareHelper.shareLyricsAndSong(context, result.title, result.lyricsText, result.chords)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "اشتراک‌گذاری شعر",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Chords
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = AvaScoreHigh,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "آکوردها: ${result.chords}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Lyrics Text Display
                        Text(
                            text = "متن ترانه و شعر:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = result.lyricsText,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

                        // Control Buttons: Play Melody & Record Vocals
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Play/Stop Melody
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isPlayingMelody) AvaScoreHigh else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = onTogglePlayMelody)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingMelody) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = if (isPlayingMelody) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPlayingMelody) "توقف ملودی" else "پخش ملودی",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (isPlayingMelody) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Start Recording on Stage
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AvaSunsetCoral,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .clickable {
                                        onLoadToStageForRecording(result)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🎤 خواندن و ضبط وکال",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
