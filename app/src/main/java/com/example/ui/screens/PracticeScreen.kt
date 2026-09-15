package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Tune
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AvaCard
import com.example.ui.components.AvaIconButton
import com.example.ui.components.AvaPracticeCard
import com.example.ui.components.AvaPrimaryButton
import com.example.ui.components.AvaWaveformVisualizer
import com.example.ui.components.ContextualQuickGuideCard
import com.example.ui.components.StudioStepByStepGuideDialog
import com.example.ui.theme.AvaGoldenHighlight
import com.example.ui.theme.AvaSunsetCoral
import com.example.ui.theme.AvaTheme
import com.tavana.karaoke.domain.model.PracticeExercise

/**
 * AVA Practice Screen — Vocal Pitch Trainer & Warm-up Studio.
 */
@Composable
fun PracticeScreen(
    exercises: List<PracticeExercise>,
    activeExercise: PracticeExercise?,
    onSelectExercise: (PracticeExercise) -> Unit,
    onDismissActiveDrill: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDrillActive by remember { mutableStateOf(false) }
    var showPracticeGuide by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ava_practice_screen"),
            contentPadding = PaddingValues(
                start = AvaTheme.spacing.medium,
                end = AvaTheme.spacing.medium,
                top = AvaTheme.spacing.medium,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(AvaTheme.spacing.large)
        ) {
            item(key = "practice_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vocal Practice & Warmup",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Refine intonation, resonance, and breath control",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = AvaTheme.shapes.chipShape,
                        color = AvaGoldenHighlight.copy(alpha = 0.18f),
                        modifier = Modifier
                            .clip(AvaTheme.shapes.chipShape)
                            .clickable { showPracticeGuide = true }
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
            }

            // Contextual Guide Card
            item(key = "practice_quick_guide") {
                ContextualQuickGuideCard(
                    title = "راهنمای گام‌به‌گام گرم کردن صدا",
                    subtitle = "۳ گام ساده برای جلوگیری از خستگی حنجره و تثبیت کوک",
                    steps = listOf(
                        "یکی از تمرینات وسعت صدا یا آرپژ را از لیست زیر انتخاب کنید.",
                        "نت پخش‌شده را گوش کنید و همزمان تلاش کنید روی همان فرکانس بخوانید.",
                        "نشانگر موج صوتی به شما نشان می‌دهد آیا صدایتان دقیقاً روی پرده نشسته است."
                    ),
                    onOpenFullGuide = { showPracticeGuide = true }
                )
            }

        // Active Pitch Tuner / Drill Card
        item(key = "practice_tuner_card") {
            val exercise = activeExercise ?: exercises.first()
            AvaCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = AvaTheme.colors.stageSurfaceElevated
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = null,
                                tint = AvaTheme.colors.brandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE PITCH DETECTOR",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AvaTheme.colors.brandPrimary
                            )
                        }

                        if (activeExercise != null) {
                            AvaIconButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Close Drill",
                                onClick = onDismissActiveDrill
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

                    // Target Note Display
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(AvaTheme.colors.stageSurface)
                            .border(2.dp, AvaTheme.colors.brandHighlight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = exercise.targetNote,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AvaTheme.colors.brandPrimary
                                )
                            )
                            Text(
                                text = "${exercise.targetFreqHz.toInt()} Hz",
                                style = MaterialTheme.typography.labelSmall,
                                color = AvaTheme.colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AvaTheme.spacing.small))

                    Text(
                        text = exercise.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = exercise.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

                    // Pitch Visualizer
                    AvaWaveformVisualizer(
                        isLive = isDrillActive,
                        audioLevel = if (isDrillActive) 0.8f else 0.2f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                    )

                    Spacer(modifier = Modifier.height(AvaTheme.spacing.medium))

                    AvaPrimaryButton(
                        text = if (isDrillActive) "Stop Drill" else "Start Pitch Match (${exercise.durationSeconds}s)",
                        icon = if (isDrillActive) null else Icons.Default.Mic,
                        onClick = { isDrillActive = !isDrillActive },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item(key = "exercises_list_header") {
            Text(
                text = "Warmup Exercises",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(exercises, key = { it.id }) { ex ->
            AvaPracticeCard(
                exercise = ex,
                onStartDrill = {
                    onSelectExercise(ex)
                    isDrillActive = true
                }
            )
        }
    }

    // Step-by-Step Interactive Guide Dialog
    StudioStepByStepGuideDialog(
        isOpen = showPracticeGuide,
        onDismiss = { showPracticeGuide = false },
        initialStepIndex = 5
    )
    }
}
