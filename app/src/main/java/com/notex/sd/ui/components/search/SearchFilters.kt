package com.notex.sd.ui.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notex.sd.domain.model.Folder
import com.notex.sd.domain.model.NoteColor

/**
 * Search filter state representing all active filters.
 */
data class SearchFilters(
    val dateRange: DateRange = DateRange.ALL,
    val colors: Set<NoteColor> = emptySet(),
    val folders: Set<String> = emptySet(),
    val includeArchived: Boolean = false,
    val includeChecklists: Boolean = true,
    val hasLinks: Boolean? = null
) {
    val hasActiveFilters: Boolean
        get() = dateRange != DateRange.ALL ||
                colors.isNotEmpty() ||
                folders.isNotEmpty() ||
                includeArchived ||
                hasLinks != null

    val activeFilterCount: Int
        get() = listOfNotNull(
            if (dateRange != DateRange.ALL) 1 else null,
            if (colors.isNotEmpty()) colors.size else null,
            if (folders.isNotEmpty()) folders.size else null,
            if (includeArchived) 1 else null,
            if (hasLinks != null) 1 else null
        ).sum()
}

/**
 * Date range options for filtering.
 */
enum class DateRange(val label: String, val daysAgo: Int?) {
    ALL("All time", null),
    TODAY("Today", 0),
    WEEK("Last 7 days", 7),
    MONTH("Last 30 days", 30),
    THREE_MONTHS("Last 3 months", 90)
}

/**
 * Filter chips bar that scrolls horizontally.
 * Shows quick access to common filters.
 */
@Composable
fun SearchFilterChips(
    filters: SearchFilters,
    onFiltersChange: (SearchFilters) -> Unit,
    onShowAllFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Filter button with badge
        FilterButton(
            activeCount = filters.activeFilterCount,
            onClick = onShowAllFilters
        )

        // Date range chips
        DateRange.entries.drop(1).forEach { range ->
            QuickFilterChip(
                label = range.label,
                isSelected = filters.dateRange == range,
                onClick = {
                    onFiltersChange(
                        filters.copy(
                            dateRange = if (filters.dateRange == range) DateRange.ALL else range
                        )
                    )
                }
            )
        }

        // Color filter indicator
        if (filters.colors.isNotEmpty()) {
            ColorFilterChip(
                colors = filters.colors,
                onClick = onShowAllFilters
            )
        }

        // Clear all filters
        if (filters.hasActiveFilters) {
            ClearFiltersChip(
                onClick = { onFiltersChange(SearchFilters()) }
            )
        }
    }
}

@Composable
private fun FilterButton(
    activeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (activeCount > 0) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        modifier = modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filters",
                modifier = Modifier.size(18.dp),
                tint = if (activeCount > 0) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (activeCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "$activeCount",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier.height(36.dp)
    )
}

@Composable
private fun ColorFilterChip(
    colors: Set<NoteColor>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ColorLens,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            // Show color dots
            colors.take(3).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(getColorValue(color))
                )
            }

            if (colors.size > 3) {
                Text(
                    text = "+${colors.size - 3}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun ClearFiltersChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
        modifier = modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                text = "Clear",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Expanded filter panel with all filter options.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandedFilterPanel(
    filters: SearchFilters,
    folders: List<Folder>,
    onFiltersChange: (SearchFilters) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (filters.hasActiveFilters) {
                        Surface(
                            onClick = { onFiltersChange(SearchFilters()) },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = "Clear all",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Date Range Section
            FilterSection(
                title = "Date Range",
                icon = Icons.Default.CalendarToday
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateRange.entries.forEach { range ->
                        FilterChip(
                            selected = filters.dateRange == range,
                            onClick = { onFiltersChange(filters.copy(dateRange = range)) },
                            label = {
                                Text(
                                    text = range.label,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            leadingIcon = if (filters.dateRange == range) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Colors Section
            FilterSection(
                title = "Colors",
                icon = Icons.Default.ColorLens
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NoteColor.entries.filter { it != NoteColor.DEFAULT }.forEach { color ->
                        ColorFilterItem(
                            color = color,
                            isSelected = filters.colors.contains(color),
                            onClick = {
                                val newColors = if (filters.colors.contains(color)) {
                                    filters.colors - color
                                } else {
                                    filters.colors + color
                                }
                                onFiltersChange(filters.copy(colors = newColors))
                            }
                        )
                    }
                }
            }

            if (folders.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Folders Section
                FilterSection(
                    title = "Folders",
                    icon = Icons.Default.Folder
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        folders.forEach { folder ->
                            FilterChip(
                                selected = filters.folders.contains(folder.id),
                                onClick = {
                                    val newFolders = if (filters.folders.contains(folder.id)) {
                                        filters.folders - folder.id
                                    } else {
                                        filters.folders + folder.id
                                    }
                                    onFiltersChange(filters.copy(folders = newFolders))
                                },
                                label = {
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                leadingIcon = if (filters.folders.contains(folder.id)) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Additional Options
            FilterSection(
                title = "Options",
                icon = Icons.Default.FilterList
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToggleFilterItem(
                        label = "Include archived notes",
                        isEnabled = filters.includeArchived,
                        onToggle = { onFiltersChange(filters.copy(includeArchived = it)) }
                    )

                    ToggleFilterItem(
                        label = "Only notes with links",
                        isEnabled = filters.hasLinks == true,
                        onToggle = {
                            onFiltersChange(
                                filters.copy(hasLinks = if (it) true else null)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        content()
    }
}

@Composable
private fun ColorFilterItem(
    color: NoteColor,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorValue = getColorValue(color)

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colorValue)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ToggleFilterItem(
    label: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { onToggle(!isEnabled) },
        shape = RoundedCornerShape(12.dp),
        color = if (isEnabled) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isEnabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isEnabled) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

private fun getColorValue(color: NoteColor): Color {
    return when (color) {
        NoteColor.DEFAULT -> Color(0xFFF5F5F5)
        NoteColor.YELLOW -> Color(0xFFFFF176)
        NoteColor.GREEN -> Color(0xFFA5D6A7)
        NoteColor.BLUE -> Color(0xFF90CAF9)
        NoteColor.PINK -> Color(0xFFF48FB1)
        NoteColor.PURPLE -> Color(0xFFCE93D8)
        NoteColor.ORANGE -> Color(0xFFFFCC80)
        NoteColor.TEAL -> Color(0xFF80CBC4)
        NoteColor.GRAY -> Color(0xFFBDBDBD)
    }
}
