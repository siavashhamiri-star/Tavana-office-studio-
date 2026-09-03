package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AvaTheme

enum class AvaNavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    STAGE(
        title = "Stage",
        selectedIcon = Icons.Filled.Mic,
        unselectedIcon = Icons.Outlined.Mic,
        testTag = "nav_tab_stage"
    ),
    PRACTICE(
        title = "Practice",
        selectedIcon = Icons.Filled.GraphicEq,
        unselectedIcon = Icons.Outlined.GraphicEq,
        testTag = "nav_tab_practice"
    ),
    RECORDINGS(
        title = "Recordings",
        selectedIcon = Icons.Filled.QueueMusic,
        unselectedIcon = Icons.Outlined.QueueMusic,
        testTag = "nav_tab_recordings"
    ),
    STUDIO(
        title = "Studio",
        selectedIcon = Icons.Filled.Tune,
        unselectedIcon = Icons.Outlined.Tune,
        testTag = "nav_tab_studio"
    )
}

/**
 * AVA Bottom Navigation Bar following Material 3 guidelines.
 * Accurately accounts for system navigation bars insets and enforces 48dp minimum targets.
 */
@Composable
fun AvaBottomBar(
    currentDestination: AvaNavDestination,
    onNavigateTo: (AvaNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = AvaTheme.colors.stageBorder,
                shape = AvaTheme.shapes.bottomBarShape
            ),
        shape = AvaTheme.shapes.bottomBarShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = AvaTheme.elevation.card
    ) {
        NavigationBar(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            AvaNavDestination.values().forEach { destination ->
                val isSelected = currentDestination == destination

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigateTo(destination) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = destination.title
                        )
                    },
                    label = {
                        Text(
                            text = destination.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AvaTheme.colors.brandPrimary,
                        selectedTextColor = AvaTheme.colors.brandPrimary,
                        indicatorColor = AvaTheme.colors.stageSurfaceElevated,
                        unselectedIconColor = AvaTheme.colors.textMuted,
                        unselectedTextColor = AvaTheme.colors.textMuted
                    ),
                    modifier = Modifier.testTag(destination.testTag)
                )
            }
        }
    }
}
