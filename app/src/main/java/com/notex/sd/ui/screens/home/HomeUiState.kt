package com.notex.sd.ui.screens.home

import com.notex.sd.core.preferences.SortOrder
import com.notex.sd.core.preferences.ViewMode
import com.notex.sd.domain.model.Note

data class HomeUiState(
    val notes: List<Note> = emptyList(),
    val pinnedNotes: List<Note> = emptyList(),
    val otherNotes: List<Note> = emptyList(),
    val viewMode: ViewMode = ViewMode.GRID,
    val sortOrder: SortOrder = SortOrder.MODIFIED_DESC,
    val isLoading: Boolean = false,
    val error: String? = null,
    val notesCount: Int = 0
) {
    val hasNotes: Boolean
        get() = notes.isNotEmpty()

    val hasPinnedNotes: Boolean
        get() = pinnedNotes.isNotEmpty()

    companion object {
        fun fromNotes(
            notes: List<Note>,
            viewMode: ViewMode,
            sortOrder: SortOrder,
            isLoading: Boolean = false,
            error: String? = null
        ): HomeUiState {
            val pinned = notes.filter { it.isPinned }
            val other = notes.filter { !it.isPinned }

            return HomeUiState(
                notes = notes,
                pinnedNotes = pinned,
                otherNotes = other,
                viewMode = viewMode,
                sortOrder = sortOrder,
                isLoading = isLoading,
                error = error,
                notesCount = notes.size
            )
        }
    }
}
