package com.notex.sd.ui.components.quickaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Expandable Quick Actions FAB that shows multiple creation options.
 * A professional, modern implementation for rapid note creation.
 */
@Composable
fun QuickActionsFab(
    onNewNote: () -> Unit,
    onNewChecklist: () -> Unit,
    onOpenTemplates: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        // Expanded menu
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(spring(stiffness = Spring.StiffnessHigh)) +
                    scaleIn(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(spring(stiffness = Spring.StiffnessHigh)) +
                    scaleOut(spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            QuickActionsMenu(
                onNewNote = {
                    onNewNote()
                    isExpanded = false
                },
                onNewChecklist = {
                    onNewChecklist()
                    isExpanded = false
                },
                onOpenTemplates = {
                    onOpenTemplates()
                    isExpanded = false
                },
                modifier = Modifier.padding(bottom = 72.dp, end = 4.dp)
            )
        }

        // Main FAB
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = if (isExpanded) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = if (isExpanded) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = if (isExpanded) 2.dp else 6.dp
            )
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (isExpanded) "Close menu" else "Create note"
            )
        }
    }
}

@Composable
private fun QuickActionsMenu(
    onNewNote: () -> Unit,
    onNewChecklist: () -> Unit,
    onOpenTemplates: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            QuickActionItem(
                icon = Icons.Default.NoteAdd,
                label = "New Note",
                description = "Blank note",
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = onNewNote
            )

            QuickActionItem(
                icon = Icons.Default.CheckBox,
                label = "Checklist",
                description = "Task list",
                iconTint = MaterialTheme.colorScheme.tertiary,
                onClick = onNewChecklist
            )

            QuickActionItem(
                icon = Icons.Outlined.AutoAwesome,
                label = "Templates",
                description = "Start with template",
                iconTint = MaterialTheme.colorScheme.secondary,
                onClick = onOpenTemplates
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    description: String,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = iconTint.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Horizontal quick actions bar for home screen header.
 * Provides fast access to common actions without opening a menu.
 */
@Composable
fun QuickActionsChipRow(
    onNewNote: () -> Unit,
    onNewChecklist: () -> Unit,
    onOpenTemplates: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionChip(
            icon = Icons.Outlined.Description,
            label = "Note",
            onClick = onNewNote,
            modifier = Modifier.weight(1f)
        )

        QuickActionChip(
            icon = Icons.Default.CheckBox,
            label = "Checklist",
            onClick = onNewChecklist,
            modifier = Modifier.weight(1f)
        )

        QuickActionChip(
            icon = Icons.Outlined.AutoAwesome,
            label = "Template",
            onClick = onOpenTemplates,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
