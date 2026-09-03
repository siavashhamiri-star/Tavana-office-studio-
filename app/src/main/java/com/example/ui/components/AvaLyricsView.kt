package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AvaGoldenHighlight
import com.example.ui.theme.AvaSunsetCoral
import com.example.ui.theme.AvaTheme
import com.tavana.karaoke.domain.model.LyricLine

/**
 * Synchronized Karaoke Lyrics View with active line highlighting,
 * Persian RTL support, and adjustable typography scale.
 */
@Composable
fun AvaLyricsView(
    lyrics: List<LyricLine>,
    currentTimeMs: Long,
    onLineClick: (LyricLine) -> Unit,
    modifier: Modifier = Modifier,
    isRtlGlobally: Boolean = false
) {
    var fontSizeMultiplier by remember { mutableFloatStateOf(1f) }
    val listState = rememberLazyListState()

    // Find currently active lyric line index
    val activeIndex = lyrics.indexOfFirst {
        currentTimeMs in it.startMs..it.endMs
    }.let { if (it == -1 && lyrics.isNotEmpty() && currentTimeMs > (lyrics.lastOrNull()?.endMs ?: 0)) lyrics.lastIndex else it }

    // Auto-scroll to keep active line centered
    LaunchedEffect(activeIndex) {
        if (activeIndex in lyrics.indices) {
            val targetScroll = (activeIndex - 1).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ava_lyrics_container"),
        shape = AvaTheme.shapes.cardShape,
        color = AvaTheme.colors.stageSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AvaTheme.colors.stageBorder)
    ) {
        Column(modifier = Modifier.padding(AvaTheme.spacing.medium)) {
            // Header with font sizing controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LYRICS & VOCAL GUIDE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AvaTheme.colors.textMuted
                    )
                )

                // Font size controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(AvaTheme.shapes.chipShape)
                        .background(AvaTheme.colors.stageSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FormatSize,
                        contentDescription = null,
                        tint = AvaTheme.colors.brandPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "A-",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AvaTheme.colors.brandPrimary,
                        modifier = Modifier
                            .clickable {
                                fontSizeMultiplier = (fontSizeMultiplier - 0.15f).coerceAtLeast(0.85f)
                            }
                            .padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "A+",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AvaTheme.colors.brandPrimary,
                        modifier = Modifier
                            .clickable {
                                fontSizeMultiplier = (fontSizeMultiplier + 0.15f).coerceAtMost(1.4f)
                            }
                            .padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AvaTheme.spacing.small))

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = AvaTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(AvaTheme.spacing.small),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                itemsIndexed(lyrics, key = { _, line -> line.id }) { index, line ->
                    val isActive = index == activeIndex
                    val isPast = activeIndex != -1 && index < activeIndex

                    val lineDirection = if (line.isRtl || isRtlGlobally) LayoutDirection.Rtl else LayoutDirection.Ltr

                    CompositionLocalProvider(LocalLayoutDirection provides lineDirection) {
                        LyricLineItem(
                            line = line,
                            isActive = isActive,
                            isPast = isPast,
                            fontMultiplier = fontSizeMultiplier,
                            onClick = { onLineClick(line) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricLineItem(
    line: LyricLine,
    isActive: Boolean,
    isPast: Boolean,
    fontMultiplier: Float,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) AvaTheme.colors.stageSurfaceElevated else Color.Transparent,
        animationSpec = tween(250),
        label = "lyricBg"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isActive -> AvaTheme.colors.brandPrimary
            isPast -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
        },
        animationSpec = tween(250),
        label = "lyricText"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AvaTheme.shapes.lyricBubbleShape)
            .background(bgColor)
            .border(
                width = if (isActive) 1.5.dp else 0.dp,
                color = if (isActive) AvaGoldenHighlight.copy(alpha = 0.8f) else Color.Transparent,
                shape = AvaTheme.shapes.lyricBubbleShape
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .semantics {
                this.role = Role.Button
                this.contentDescription = "Lyric: ${line.text}"
            }
    ) {
        Column {
            Text(
                text = line.text,
                fontSize = (if (isActive) 20.sp else 16.sp) * fontMultiplier,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                textAlign = if (line.isRtl) TextAlign.Right else TextAlign.Left,
                modifier = Modifier.fillMaxWidth()
            )

            if (!line.secondaryText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = line.secondaryText,
                    fontSize = 13.sp * fontMultiplier,
                    fontWeight = FontWeight.Normal,
                    color = AvaTheme.colors.textMuted,
                    textAlign = if (line.isRtl) TextAlign.Right else TextAlign.Left,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
