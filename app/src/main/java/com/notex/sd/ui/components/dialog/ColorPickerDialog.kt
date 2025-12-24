package com.notex.sd.ui.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.notex.sd.domain.model.NoteColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    currentColor: NoteColor,
    onColorSelected: (NoteColor) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedColor by remember { mutableStateOf(currentColor) }

    val colors = remember {
        listOf(
            NoteColor.DEFAULT to "Default",
            NoteColor.YELLOW to "Yellow",
            NoteColor.GREEN to "Green",
            NoteColor.BLUE to "Blue",
            NoteColor.PINK to "Pink",
            NoteColor.PURPLE to "Purple",
            NoteColor.ORANGE to "Orange",
            NoteColor.TEAL to "Teal",
            NoteColor.GRAY to "Gray"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Color",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    maxItemsInEachRow = 4
                ) {
                    colors.forEach { (color, label) ->
                        ColorOption(
                            color = color,
                            label = label,
                            isSelected = selectedColor == color,
                            onClick = { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onColorSelected(selectedColor)
                    onDismiss()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ColorOption(
    color: NoteColor,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val displayColor = remember(color, isDark) {
        when (color) {
            NoteColor.DEFAULT -> if (isDark) Color(0xFF2D2D2D) else Color(0xFFF5F5F5)
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(displayColor)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
