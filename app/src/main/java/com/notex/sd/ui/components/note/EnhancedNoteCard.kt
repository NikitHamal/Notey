package com.notex.sd.ui.components.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notex.sd.core.theme.NoteBlue
import com.notex.sd.core.theme.NoteBlueDark
import com.notex.sd.core.theme.NoteGray
import com.notex.sd.core.theme.NoteGrayDark
import com.notex.sd.core.theme.NoteGreen
import com.notex.sd.core.theme.NoteGreenDark
import com.notex.sd.core.theme.NoteOrange
import com.notex.sd.core.theme.NoteOrangeDark
import com.notex.sd.core.theme.NotePink
import com.notex.sd.core.theme.NotePinkDark
import com.notex.sd.core.theme.NotePurple
import com.notex.sd.core.theme.NotePurpleDark
import com.notex.sd.core.theme.NoteTeal
import com.notex.sd.core.theme.NoteTealDark
import com.notex.sd.core.theme.NoteYellow
import com.notex.sd.core.theme.NoteYellowDark
import com.notex.sd.domain.model.Note
import com.notex.sd.domain.model.NoteColor
import com.notex.sd.domain.model.NoteLink
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Enhanced note card with modern design.
 * Features:
 * - Subtle color accents with left border
 * - Compact information density
 * - Visual indicators for checklist, links, pinned status
 * - Smooth animations for selection
 * - Smart content preview with truncation
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedNoteCard(
    note: Note,
    isSelected: Boolean = false,
    hasLinks: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val backgroundColor = remember(note.color, isDark) {
        getNoteBackground(note.color, isDark)
    }

    val accentColor = remember(note.color) {
        getAccentColor(note.color)
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    val formattedDate = remember(note.modifiedAt) {
        formatRelativeTime(note.modifiedAt)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.color != NoteColor.DEFAULT) {
                backgroundColor
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 1.dp
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Accent bar for colored notes
            if (note.color != NoteColor.DEFAULT) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(if (note.title.isNotBlank() && note.plainTextContent.isNotBlank()) 120.dp else 80.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    accentColor,
                                    accentColor.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header row: Title + indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Title
                    if (note.title.isNotBlank()) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 22.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Status indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        if (note.isPinned) {
                            PinIndicator()
                        }
                        if (note.isChecklist) {
                            ChecklistIndicator()
                        }
                        if (hasLinks) {
                            LinkIndicator()
                        }
                    }
                }

                // Content preview
                if (note.plainTextContent.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = note.preview,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Footer: Metadata
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    if (note.wordCount > 0 && !note.isChecklist) {
                        Text(
                            text = formatWordCount(note.wordCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Selection overlay
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun PinIndicator() {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.size(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PushPin,
            contentDescription = "Pinned",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(4.dp)
                .size(16.dp)
        )
    }
}

@Composable
private fun ChecklistIndicator() {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.size(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Checklist,
            contentDescription = "Checklist",
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier
                .padding(4.dp)
                .size(16.dp)
        )
    }
}

@Composable
private fun LinkIndicator() {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.size(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Link,
            contentDescription = "Has links",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .padding(4.dp)
                .size(16.dp)
        )
    }
}

/**
 * Compact card variant for list view
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactNoteCard(
    note: Note,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val backgroundColor = remember(note.color, isDark) {
        getNoteBackground(note.color, isDark)
    }

    val accentColor = remember(note.color) {
        getAccentColor(note.color)
    }

    val formattedDate = remember(note.modifiedAt) {
        formatRelativeTime(note.modifiedAt)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (note.color != NoteColor.DEFAULT) {
            backgroundColor
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator dot
            if (note.color != NoteColor.DEFAULT) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (note.title.isNotBlank()) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (note.plainTextContent.isNotBlank()) {
                    Text(
                        text = note.preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                if (note.wordCount > 0) {
                    Text(
                        text = formatWordCount(note.wordCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // Selection indicator
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp)
                )
            }
        }
    }
}

// Helper functions
private fun getNoteBackground(color: NoteColor, isDark: Boolean): Color {
    return when (color) {
        NoteColor.DEFAULT -> Color.Transparent
        NoteColor.YELLOW -> if (isDark) NoteYellowDark else NoteYellow
        NoteColor.GREEN -> if (isDark) NoteGreenDark else NoteGreen
        NoteColor.BLUE -> if (isDark) NoteBlueDark else NoteBlue
        NoteColor.PINK -> if (isDark) NotePinkDark else NotePink
        NoteColor.PURPLE -> if (isDark) NotePurpleDark else NotePurple
        NoteColor.ORANGE -> if (isDark) NoteOrangeDark else NoteOrange
        NoteColor.TEAL -> if (isDark) NoteTealDark else NoteTeal
        NoteColor.GRAY -> if (isDark) NoteGrayDark else NoteGray
    }
}

private fun getAccentColor(color: NoteColor): Color {
    return when (color) {
        NoteColor.DEFAULT -> Color(0xFF6750A4)
        NoteColor.YELLOW -> Color(0xFFF9A825)
        NoteColor.GREEN -> Color(0xFF43A047)
        NoteColor.BLUE -> Color(0xFF1E88E5)
        NoteColor.PINK -> Color(0xFFD81B60)
        NoteColor.PURPLE -> Color(0xFF8E24AA)
        NoteColor.ORANGE -> Color(0xFFEF6C00)
        NoteColor.TEAL -> Color(0xFF00897B)
        NoteColor.GRAY -> Color(0xFF757575)
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> {
            val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
            "${mins}m"
        }
        diff < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            "${hours}h"
        }
        diff < TimeUnit.DAYS.toMillis(7) -> {
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            "${days}d"
        }
        else -> {
            SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
        }
    }
}

private fun formatWordCount(count: Int): String {
    return when {
        count < 1000 -> "$count words"
        else -> String.format(Locale.getDefault(), "%.1fk words", count / 1000f)
    }
}
