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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notex.sd.domain.model.Note
import com.notex.sd.ui.components.common.EmptyState

/**
 * Modern note list component supporting grid (masonry) and list layouts.
 * Features enhanced cards with visual indicators and smooth animations.
 */
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
    contentPadding: PaddingValues = PaddingValues(0.dp),
    linkedNoteIds: Set<String> = emptySet()
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
            icon = Icons.AutoMirrored.Outlined.NoteAdd,
            title = emptyStateTitle,
            subtitle = emptyStateSubtitle,
            modifier = modifier.fillMaxSize()
        )
    } else {
        when (layout) {
            NoteCardLayout.GRID -> {
                NotesMasonryGrid(
                    pinnedNotes = pinnedNotes,
                    otherNotes = otherNotes,
                    selectedNoteIds = selectedNoteIds,
                    linkedNoteIds = linkedNoteIds,
                    onNoteClick = onNoteClick,
                    onNoteLongClick = onNoteLongClick,
                    contentPadding = contentPadding,
                    modifier = modifier
                )
            }
            NoteCardLayout.LIST -> {
                NotesCompactList(
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

/**
 * Masonry grid layout for notes - Pinterest-style staggered grid.
 * Uses enhanced cards with visual indicators for links, pins, and checklists.
 */
@Composable
private fun NotesMasonryGrid(
    pinnedNotes: List<Note>,
    otherNotes: List<Note>,
    selectedNoteIds: Set<String>,
    linkedNoteIds: Set<String>,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyMasonryGrid(
        items = otherNotes,
        columns = 2,
        contentPadding = PaddingValues(
            start = contentPadding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + 12.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            end = contentPadding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 100.dp
        ),
        horizontalSpacing = 10.dp,
        verticalSpacing = 10.dp,
        modifier = modifier.fillMaxSize(),
        state = listState,
        key = { it.id },
        sectionHeader = { title ->
            SectionHeader(text = title)
        },
        pinnedItems = pinnedNotes
    ) { note ->
        EnhancedNoteCard(
            note = note,
            isSelected = selectedNoteIds.contains(note.id),
            hasLinks = linkedNoteIds.contains(note.id),
            onClick = { onNoteClick(note) },
            onLongClick = { onNoteLongClick(note) }
        )
    }
}

/**
 * Compact list layout for notes - clean, dense vertical list.
 * Uses compact cards optimized for scanning and quick access.
 */
@Composable
private fun NotesCompactList(
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
            bottom = contentPadding.calculateBottomPadding() + 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Pinned section
        if (pinnedNotes.isNotEmpty()) {
            item(key = "pinned_header") {
                SectionHeader(text = "Pinned")
            }

            items(
                items = pinnedNotes,
                key = { "pinned_${it.id}" }
            ) { note ->
                CompactNoteCard(
                    note = note,
                    isSelected = selectedNoteIds.contains(note.id),
                    onClick = { onNoteClick(note) },
                    onLongClick = { onNoteLongClick(note) }
                )
            }

            item(key = "pinned_spacer") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Others section
        if (otherNotes.isNotEmpty()) {
            if (pinnedNotes.isNotEmpty()) {
                item(key = "others_header") {
                    SectionHeader(text = "Others")
                }
            }

            items(
                items = otherNotes,
                key = { "other_${it.id}" }
            ) { note ->
                CompactNoteCard(
                    note = note,
                    isSelected = selectedNoteIds.contains(note.id),
                    onClick = { onNoteClick(note) },
                    onLongClick = { onNoteLongClick(note) }
                )
            }
        }
    }
}

/**
 * Section header for note groups (Pinned, Others).
 * Clean typography with subtle visual hierarchy.
 */
@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
    }
}
