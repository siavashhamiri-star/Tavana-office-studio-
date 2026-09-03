package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AvaCard
import com.example.ui.components.AvaRecordingCard
import com.example.ui.components.AvaSongCard
import com.example.ui.components.AvaStageHeroButton
import com.example.ui.theme.AvaSunsetCoral
import com.example.ui.theme.AvaTheme
import com.tavana.karaoke.domain.model.PracticeExercise
import com.tavana.karaoke.domain.model.RecordingTake
import com.tavana.karaoke.domain.model.Song

/**
 * AVA First-Launch Home Experience.
 * Communicates: "This is a place where I want to sing."
 *
 * Prioritizes:
 * 1. Start Singing (Hero Stage Action)
 * 2. Practice (Vocal warmup)
 * 3. My Recordings (Recent takes)
 * 4. Recently Played / Curated catalog
 */
@Composable
fun HomeScreen(
    songs: List<Song>,
    recordings: List<RecordingTake>,
    exercises: List<PracticeExercise>,
    onStartSingingHero: () -> Unit,
    onSongSelected: (Song) -> Unit,
    onStartPractice: (PracticeExercise) -> Unit,
    onViewAllRecordings: () -> Unit,
    onToggleRtl: () -> Unit,
    isRtlActive: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ava_home_screen"),
        contentPadding = PaddingValues(
            start = AvaTheme.spacing.medium,
            end = AvaTheme.spacing.medium,
            top = AvaTheme.spacing.medium,
            bottom = 96.dp // Clear bottom bar
        ),
        verticalArrangement = Arrangement.spacedBy(AvaTheme.spacing.large)
    ) {
        // Brand Header with Tagline and Persian RTL toggle
        item(key = "home_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(AvaTheme.colors.brandPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(AvaTheme.spacing.small))
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = R.string.tagline),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AvaTheme.colors.brandPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // RTL switch button for quick Persian preview
                Surface(
                    shape = AvaTheme.shapes.chipShape,
                    color = if (isRtlActive) AvaTheme.colors.brandPrimary.copy(alpha = 0.15f) else AvaTheme.colors.stageSurfaceElevated,
                    modifier = Modifier
                        .clip(AvaTheme.shapes.chipShape)
                        .clickable(role = Role.Button, onClick = onToggleRtl)
                        .border(
                            1.dp,
                            if (isRtlActive) AvaTheme.colors.brandPrimary else AvaTheme.colors.stageBorder,
                            AvaTheme.shapes.chipShape
                        )
                        .semantics {
                            this.role = Role.Button
                            this.contentDescription = "Toggle Persian RTL layout"
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = if (isRtlActive) AvaTheme.colors.brandPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRtlActive) "RTL فارسی" else "LTR English",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isRtlActive) AvaTheme.colors.brandPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }

        // 1. PRIMARY HERO ACTION: "Start Singing"
        item(key = "home_hero_cta") {
            AvaStageHeroButton(
                onClick = onStartSingingHero,
                label = "Start Singing",
                subLabel = "Step into the spotlight with synchronized lyrics"
            )
        }

        // 2. PRACTICE SPOTLIGHT: Daily Vocal Warmup
        item(key = "home_practice_section") {
            Column {
                SectionTitle(
                    title = "Daily Vocal Warmup",
                    subtitle = "Tone your pitch and vocal agility"
                )
                Spacer(modifier = Modifier.height(AvaTheme.spacing.small))

                val featuredExercise = exercises.firstOrNull()
                if (featuredExercise != null) {
                    AvaCard(
                        onClick = { onStartPractice(featuredExercise) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(AvaTheme.colors.stageSurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = null,
                                        tint = AvaTheme.colors.brandPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(AvaTheme.spacing.medium))
                                Column {
                                    Text(
                                        text = featuredExercise.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${featuredExercise.category} • Target note ${featuredExercise.targetNote}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "Warm Up →",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AvaTheme.colors.brandPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // 3. RECENT TAKES / MY RECORDINGS
        item(key = "home_recordings_section") {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(
                        title = "My Recent Takes",
                        subtitle = "Relisten to your best vocal performances"
                    )
                    if (recordings.isNotEmpty()) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AvaTheme.colors.brandPrimary
                            ),
                            modifier = Modifier.clickable(onClick = onViewAllRecordings)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AvaTheme.spacing.small))

                val topRecording = recordings.firstOrNull()
                if (topRecording != null) {
                    AvaRecordingCard(
                        recording = topRecording,
                        isPlaying = false,
                        onPlayToggle = onStartSingingHero,
                        onDetailsClick = onViewAllRecordings
                    )
                }
            }
        }

        // 4. RECENTLY PLAYED & CURATED SONGS
        item(key = "home_curated_songs_header") {
            SectionTitle(
                title = "Popular on Stage",
                subtitle = "Select a song to begin your performance"
            )
        }

        items(songs, key = { it.id }) { song ->
            AvaSongCard(
                song = song,
                onSingClick = { onSongSelected(song) }
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
