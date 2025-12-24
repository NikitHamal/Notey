package com.notex.sd.ui.components.note

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notex.sd.domain.model.Note
import com.notex.sd.ui.components.common.EmptyState

@Composable
fun NotesList(
    notes: List<Note>,
    selectedNoteIds: Set<String>,
    layout: NoteCardLayout,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit,
    emptyStateTitle: String = "No notes yet",
    emptyStateSubtitle: String = "Tap the + button to create your first note",
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val pinnedNotes by remember(notes) {
        derivedStateOf {
            notes.filter { it.isPinned && !it.isArchived && !it.isTrashed }
        }
    }

    val otherNotes by remember(notes) {
        derivedStateOf {
            notes.filter { !it.isPinned && !it.isArchived && !it.isTrashed }
        }
    }

    if (notes.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.NoteAdd,
            title = emptyStateTitle,
            subtitle = emptyStateSubtitle,
            modifier = modifier.fillMaxSize()
        )
    } else {
        when (layout) {
            NoteCardLayout.GRID -> {
                NotesGrid(
                    pinnedNotes = pinnedNotes,
                    otherNotes = otherNotes,
                    selectedNoteIds = selectedNoteIds,
                    onNoteClick = onNoteClick,
                    onNoteLongClick = onNoteLongClick,
                    contentPadding = contentPadding,
                    modifier = modifier
                )
            }
            NoteCardLayout.LIST -> {
                NotesListView(
                    pinnedNotes = pinnedNotes,
                    otherNotes = otherNotes,
                    selectedNoteIds = selectedNoteIds,
                    onNoteClick = onNoteClick,
                    onNoteLongClick = onNoteLongClick,
                    contentPadding = contentPadding,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun NotesGrid(
    pinnedNotes: List<Note>,
    otherNotes: List<Note>,
    selectedNoteIds: Set<String>,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = contentPadding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + 8.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            end = contentPadding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 8.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Pinned section
        if (pinnedNotes.isNotEmpty()) {
            item {
                SectionHeader(
                    text = "Pinned",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(
                items = pinnedNotes,
                key = { it.id }
            ) { note ->
                NoteCard(
                    note = note,
                    isSelected = selectedNoteIds.contains(note.id),
                    layout = NoteCardLayout.GRID,
                    onClick = { onNoteClick(note) },
                    onLongClick = { onNoteLongClick(note) }
                )
            }
        }

        // Others section
        if (otherNotes.isNotEmpty()) {
            if (pinnedNotes.isNotEmpty()) {
                item {
                    SectionHeader(
                        text = "Others",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            items(
                items = otherNotes,
                key = { it.id }
            ) { note ->
                NoteCard(
                    note = note,
                    isSelected = selectedNoteIds.contains(note.id),
                    layout = NoteCardLayout.GRID,
                    onClick = { onNoteClick(note) },
                    onLongClick = { onNoteLongClick(note) }
                )
            }
        }
    }
}

@Composable
private fun NotesListView(
    pinnedNotes: List<Note>,
    otherNotes: List<Note>,
    selectedNoteIds: Set<String>,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = contentPadding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            end = contentPadding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Pinned section
        if (pinnedNotes.isNotEmpty()) {
            item {
                SectionHeader(text = "Pinned")
            }

            items(
                items = pinnedNotes,
                key = { it.id }
            ) { note ->
                NoteCard(
                    note = note,
                    isSelected = selectedNoteIds.contains(note.id),
                    layout = NoteCardLayout.LIST,
                    onClick = { onNoteClick(note) },
                    onLongClick = { onNoteLongClick(note) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Others section
        if (otherNotes.isNotEmpty()) {
            if (pinnedNotes.isNotEmpty()) {
                item {
                    SectionHeader(text = "Others")
                }
            }

            items(
                items = otherNotes,
                key = { it.id }
            ) { note ->
                NoteCard(
                    note = note,
                    isSelected = selectedNoteIds.contains(note.id),
                    layout = NoteCardLayout.LIST,
                    onClick = { onNoteClick(note) },
                    onLongClick = { onNoteLongClick(note) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
