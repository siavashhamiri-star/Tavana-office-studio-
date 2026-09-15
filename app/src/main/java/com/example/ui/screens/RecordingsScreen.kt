package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AvaEmptyState
import com.example.ui.components.AvaRecordingCard
import com.example.ui.components.ContextualQuickGuideCard
import com.example.ui.components.StudioStepByStepGuideDialog
import com.example.ui.theme.AvaGoldenHighlight
import com.example.ui.theme.AvaTheme
import com.tavana.karaoke.domain.model.RecordingTake
import com.tavana.karaoke.domain.model.Song
import com.tavana.studio.audio.sharing.ShareHelper

/**
 * AVA Recordings Screen — Displays saved takes and performance logs.
 */
@Composable
fun RecordingsScreen(
    recordings: List<RecordingTake>,
    onSingSong: (Song) -> Unit,
    onNavigateToStage: () -> Unit,
    onPlayRecordingTake: (RecordingTake) -> Unit = {},
    playingTakeId: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showRecordingsGuide by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ava_recordings_screen"),
            contentPadding = PaddingValues(
                start = AvaTheme.spacing.medium,
                end = AvaTheme.spacing.medium,
                top = AvaTheme.spacing.medium,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(AvaTheme.spacing.medium)
        ) {
            item(key = "recordings_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Recordings",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Review your vocal progress and stage takes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = AvaTheme.shapes.chipShape,
                        color = AvaGoldenHighlight.copy(alpha = 0.18f),
                        modifier = Modifier
                            .clip(AvaTheme.shapes.chipShape)
                            .clickable { showRecordingsGuide = true }
                            .border(1.dp, AvaGoldenHighlight.copy(alpha = 0.6f), AvaTheme.shapes.chipShape)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = AvaGoldenHighlight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "راهنما",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AvaGoldenHighlight
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(AvaTheme.spacing.small))
            }

            // Quick Guide Card
            item(key = "recordings_quick_guide") {
                ContextualQuickGuideCard(
                    title = "راهنمای ضبط‌ها و اشتراک‌گذاری",
                    subtitle = "مدیریت فایل‌های صوتی و اشتراک در شبکه‌های اجتماعی",
                    steps = listOf(
                        "روی هر فایل ضبط‌شده ضربه بزنید تا با کیفیت بالا پخش شود.",
                        "با زدن دکمه اشتراک‌گذاری (Share)، قطعه را مستقیم برای دوستانتان بفرستید.",
                        "می‌توانید همان آهنگ را مجدداً روی صحنه بخوانید تا نمره بالاتری کسب کنید."
                    ),
                    onOpenFullGuide = { showRecordingsGuide = true }
                )
            }

            if (recordings.isEmpty()) {
                item(key = "empty_recordings") {
                    AvaEmptyState(
                        title = "Your Stage Awaits",
                        message = "You haven't recorded any vocal takes yet. Step onto the stage and sing your heart out!",
                        actionLabel = "Start Singing",
                        onActionClick = onNavigateToStage,
                        icon = Icons.AutoMirrored.Rounded.QueueMusic
                    )
                }
            } else {
                items(recordings, key = { it.id }) { take ->
                    val isPlaying = playingTakeId == take.id
                    AvaRecordingCard(
                        recording = take,
                        isPlaying = isPlaying,
                        onPlayToggle = {
                            onPlayRecordingTake(take)
                        },
                        onDetailsClick = {
                            onPlayRecordingTake(take)
                        },
                        onShareClick = {
                            ShareHelper.shareRecordingTake(context, take)
                        }
                    )
                }
            }
        }

        // Step-by-Step Interactive Guide Dialog
        StudioStepByStepGuideDialog(
            isOpen = showRecordingsGuide,
            onDismiss = { showRecordingsGuide = false },
            initialStepIndex = 6,
            onNavigateToStage = onNavigateToStage
        )
    }
}
