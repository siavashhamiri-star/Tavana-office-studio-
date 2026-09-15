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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AvaCard
import com.example.ui.components.AvaRecordingCard
import com.example.ui.components.AvaSongCard
import com.example.ui.components.AvaStageHeroButton
import com.example.ui.components.ContextualQuickGuideCard
import com.example.ui.components.StudioStepByStepGuideDialog
import com.example.ui.theme.AvaGoldenHighlight
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
    onPlayRecordingTake: (RecordingTake) -> Unit = {},
    playingRecordingId: String? = null,
    onOpenOnlineSongs: () -> Unit = {},
    onOpenAiComposer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("همه") }
    var showGuideDialog by remember { mutableStateOf(false) }
    var guideInitialStep by remember { mutableIntStateOf(0) }

    val categories = remember { listOf("همه", "پاپ", "سنتی", "بی‌کلام", "هوش مصنوعی") }

    val filteredSongs = remember(songs, searchQuery, selectedCategory) {
        songs.filter { song ->
            val matchesCategory = when (selectedCategory) {
                "همه" -> true
                "پاپ" -> song.category.contains("Pop", ignoreCase = true) || song.tags.any { it.contains("Pop", ignoreCase = true) }
                "سنتی" -> song.category.contains("Persian", ignoreCase = true) || song.category.contains("Traditional", ignoreCase = true) || song.tags.any { it.contains("سنتی", ignoreCase = true) }
                "بی‌کلام" -> song.instrumentalPath.isNotBlank()
                "هوش مصنوعی" -> song.id.startsWith("ai_song") || song.tags.any { it.contains("AI", ignoreCase = true) }
                else -> true
            }
            val query = searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                    song.title.lowercase().contains(query) ||
                    song.artist.lowercase().contains(query) ||
                    song.category.lowercase().contains(query) ||
                    song.tags.any { it.lowercase().contains(query) }
            matchesCategory && matchesQuery
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
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
            // Brand Header with Tagline, Step-by-Step Guide button, and RTL toggle
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Interactive Step-by-Step Guide Button
                        Surface(
                            shape = AvaTheme.shapes.chipShape,
                            color = AvaGoldenHighlight.copy(alpha = 0.18f),
                            modifier = Modifier
                                .clip(AvaTheme.shapes.chipShape)
                                .clickable(
                                    role = Role.Button,
                                    onClick = {
                                        guideInitialStep = 0
                                        showGuideDialog = true
                                    }
                                )
                                .border(1.dp, AvaGoldenHighlight.copy(alpha = 0.6f), AvaTheme.shapes.chipShape)
                                .semantics {
                                    this.role = Role.Button
                                    this.contentDescription = "راهنمای گام‌به‌گام برنامه"
                                }
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

                        // RTL switch button
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
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = if (isRtlActive) AvaTheme.colors.brandPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isRtlActive) "فا" else "EN",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isRtlActive) AvaTheme.colors.brandPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // In-Screen Contextual Guide Card (راهنمای سریع و آسان)
            item(key = "home_quick_guide") {
                ContextualQuickGuideCard(
                    title = "راهنمای سریع خوانندگی در ۳ گام",
                    subtitle = "محیط برنامه بسیار ساده و هدایت‌کننده است",
                    steps = listOf(
                        "آهنگ دلخواه خود را از بخش زیر یا با نوار جستجو پیدا کنید.",
                        "روی دکمه «بخوان» بزنید؛ ترانه با متن همگام روی صحنه پخش می‌شود.",
                        "با هدفون صدای خود را زنده بشنوید و در پایان نمره اجرای خود را بگیرید."
                    ),
                    onOpenFullGuide = {
                        guideInitialStep = 0
                        showGuideDialog = true
                    }
                )
            }

            // 1. PRIMARY HERO ACTION: "Start Singing"
            item(key = "home_hero_cta") {
                AvaStageHeroButton(
                    onClick = onStartSingingHero,
                    label = "Start Singing",
                    subLabel = "Step into the spotlight with synchronized lyrics"
                )
            }

        // Studio Innovations: AI Composer & Online Song Catalog
        item(key = "home_innovations_row") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AvaTheme.spacing.medium)
            ) {
                // AI Composer Card
                AvaCard(
                    onClick = onOpenAiComposer,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_open_ai_composer"),
                    borderColor = AvaGoldenHighlight.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = AvaTheme.spacing.small)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AvaGoldenHighlight.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AvaGoldenHighlight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(AvaTheme.spacing.small))
                        Text(
                            text = "AI Composer",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "آهنگساز هوش مصنوعی",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = AvaGoldenHighlight
                        )
                    }
                }

                // Online Catalog & Stream Card
                AvaCard(
                    onClick = onOpenOnlineSongs,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_open_online_songs"),
                    borderColor = AvaTheme.colors.brandPrimary.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = AvaTheme.spacing.small)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AvaTheme.colors.brandPrimary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = AvaTheme.colors.brandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(AvaTheme.spacing.small))
                        Text(
                            text = "Online Stream",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "موسیقی وب و استریم",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = AvaTheme.colors.brandPrimary
                        )
                    }
                }
            }
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
                    val isPlaying = playingRecordingId == topRecording.id
                    AvaRecordingCard(
                        recording = topRecording,
                        isPlaying = isPlaying,
                        onPlayToggle = { onPlayRecordingTake(topRecording) },
                        onDetailsClick = { onPlayRecordingTake(topRecording) }
                    )
                }
            }
        }

        // 4. RECENTLY PLAYED & CURATED SONGS (MUSIC LIBRARY) & SEARCH
        item(key = "home_curated_songs_header") {
            Column(verticalArrangement = Arrangement.spacedBy(AvaTheme.spacing.small)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(
                        title = "Music Library (آرشیو ترانه‌ها)",
                        subtitle = "جستجو و انتخاب آهنگ‌های آماده برای خواندن"
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AvaTheme.colors.brandPrimary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AvaTheme.colors.brandPrimary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "${filteredSongs.size} ترانه",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AvaTheme.colors.brandPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Dedicated Interactive Song Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "جستجوی ترانه، خواننده، سبک، یا متن شعر...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "جستجوی آهنگ",
                            tint = AvaTheme.colors.brandPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "پاک کردن",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AvaTheme.colors.brandPrimary,
                        unfocusedBorderColor = AvaTheme.colors.stageBorder,
                        focusedContainerColor = AvaTheme.colors.stageSurfaceElevated,
                        unfocusedContainerColor = AvaTheme.colors.stageSurfaceElevated
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_song_search")
                )

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(categories) { _, cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AvaTheme.colors.brandPrimary else AvaTheme.colors.stageSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AvaTheme.colors.brandPrimary else AvaTheme.colors.stageBorder
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        if (filteredSongs.isEmpty()) {
            item(key = "no_search_results") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AvaTheme.colors.stageSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AvaTheme.colors.stageBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AvaTheme.spacing.medium)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AvaTheme.spacing.large),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AvaTheme.colors.stageSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "آهنگی با عنوان «$searchQuery» یافت نشد",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "می‌توانید جستجو را پاک کنید یا از هوش مصنوعی بخواهید این ترانه را برایتان بسازد!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AvaTheme.colors.stageSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AvaTheme.colors.stageBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        searchQuery = ""
                                        selectedCategory = "همه"
                                    }
                            ) {
                                Text(
                                    text = "پاک کردن جستجو",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AvaGoldenHighlight,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(onClick = onOpenAiComposer)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ساخت با هوش مصنوعی",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredSongs, key = { it.id }) { song ->
                AvaSongCard(
                    song = song,
                    onSingClick = { onSongSelected(song) }
                )
            }
        }
    }

    // Step-by-Step Interactive Guide Dialog
    StudioStepByStepGuideDialog(
        isOpen = showGuideDialog,
        onDismiss = { showGuideDialog = false },
        initialStepIndex = guideInitialStep,
        onNavigateToSearch = {
            searchQuery = ""
            selectedCategory = "همه"
        },
        onNavigateToStage = onStartSingingHero,
        onOpenAiComposer = onOpenAiComposer,
        onNavigateToPractice = {
            val ex = exercises.firstOrNull()
            if (ex != null) onStartPractice(ex)
        },
        onNavigateToRecordings = onViewAllRecordings
    )
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
