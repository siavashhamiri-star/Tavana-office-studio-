package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AvaEmptyState
import com.example.ui.components.AvaRecordingCard
import com.example.ui.theme.AvaTheme
import com.tavana.karaoke.domain.model.RecordingTake
import com.tavana.karaoke.domain.model.Song

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
    LazyColumn(
        modifier = modifier
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
            Column {
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
            Spacer(modifier = Modifier.height(AvaTheme.spacing.small))
        }

        if (recordings.isEmpty()) {
            item(key = "empty_recordings") {
                AvaEmptyState(
                    title = "Your Stage Awaits",
                    message = "You haven't recorded any vocal takes yet. Step onto the stage and sing your heart out!",
                    actionLabel = "Start Singing",
                    onActionClick = onNavigateToStage,
                    icon = Icons.Rounded.QueueMusic
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
                    }
                )
            }
        }
    }
}
