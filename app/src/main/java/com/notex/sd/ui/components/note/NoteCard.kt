package com.notex.sd.ui.components.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NoteCardLayout {
    GRID,
    LIST
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    isSelected: Boolean,
    layout: NoteCardLayout,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = remember(note.color, isDark) {
        getNoteColorBackgroundStatic(note.color, isDark)
    }

    val formattedDate = remember(note.modifiedAt) {
        formatTimestamp(note.modifiedAt)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (note.color != NoteColor.DEFAULT) {
                backgroundColor
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        NoteCardContent(
            note = note,
            isSelected = isSelected,
            layout = layout,
            formattedDate = formattedDate
        )
    }
}

@Composable
private fun NoteCardContent(
    note: Note,
    isSelected: Boolean,
    layout: NoteCardLayout,
    formattedDate: String
) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with pin indicator and color
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Color indicator
                if (note.color != NoteColor.DEFAULT) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(getColorIndicator(note.color))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Title
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = if (layout == NoteCardLayout.GRID) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Pin indicator
                if (note.isPinned) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Content preview
            if (note.plainTextContent.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (layout == NoteCardLayout.GRID) 6 else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Footer with timestamp
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Word count (for list layout)
                if (layout == NoteCardLayout.LIST && note.wordCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${note.wordCount} words",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Selection indicator
        androidx.compose.animation.AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
            )
        }
    }
}

private fun getNoteColorBackgroundStatic(color: NoteColor, isDark: Boolean): Color {
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

@Composable
private fun getColorIndicator(color: NoteColor): Color {
    return when (color) {
        NoteColor.DEFAULT -> MaterialTheme.colorScheme.primary
        NoteColor.YELLOW -> Color(0xFFFFC107)
        NoteColor.GREEN -> Color(0xFF4CAF50)
        NoteColor.BLUE -> Color(0xFF2196F3)
        NoteColor.PINK -> Color(0xFFE91E63)
        NoteColor.PURPLE -> Color(0xFF9C27B0)
        NoteColor.ORANGE -> Color(0xFFFF9800)
        NoteColor.TEAL -> Color(0xFF009688)
        NoteColor.GRAY -> Color(0xFF9E9E9E)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> {
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}
