package com.notex.sd.ui.screens.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notex.sd.domain.model.Note
import com.notex.sd.domain.usecase.DeleteNoteUseCase
import com.notex.sd.domain.usecase.EmptyTrashUseCase
import com.notex.sd.domain.usecase.GetTrashedNotesUseCase
import com.notex.sd.domain.usecase.RestoreFromTrashUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrashUiState(
    val trashedNotes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEmptyingTrash: Boolean = false
) {
    val hasNotes: Boolean
        get() = trashedNotes.isNotEmpty()

    val notesCount: Int
        get() = trashedNotes.size

    fun getAutoDeleteInfo(note: Note): String? {
        if (note.trashedAt == null) return null

        val daysSinceTrashed = (System.currentTimeMillis() - note.trashedAt) / (1000 * 60 * 60 * 24)
        val daysRemaining = 30 - daysSinceTrashed

        return when {
            daysRemaining <= 0 -> "Will be deleted soon"
            daysRemaining == 1L -> "Will be deleted in 1 day"
            daysRemaining <= 7 -> "Will be deleted in $daysRemaining days"
            else -> null
        }
    }
}

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val getTrashedNotesUseCase: GetTrashedNotesUseCase,
    private val restoreFromTrashUseCase: RestoreFromTrashUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val emptyTrashUseCase: EmptyTrashUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _isEmptyingTrash = MutableStateFlow(false)

    val uiState: StateFlow<TrashUiState> = combine(
        getTrashedNotesUseCase(),
        _isLoading,
        _error,
        _isEmptyingTrash
    ) { notes, isLoading, error, isEmptying ->
        TrashUiState(
            trashedNotes = notes,
            isLoading = isLoading,
            error = error,
            isEmptyingTrash = isEmptying
        )
    }.catch { throwable ->
        emit(
            TrashUiState(
                isLoading = false,
                error = throwable.message ?: "An error occurred"
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrashUiState(isLoading = true)
    )

    fun restoreNote(noteId: String) {
        viewModelScope.launch {
            try {
                restoreFromTrashUseCase(noteId)
            } catch (e: Exception) {
                _error.value = "Failed to restore note: ${e.message}"
            }
        }
    }

    fun deleteNotePermanently(noteId: String) {
        viewModelScope.launch {
            try {
                deleteNoteUseCase(noteId)
            } catch (e: Exception) {
                _error.value = "Failed to delete note permanently: ${e.message}"
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            try {
                _isEmptyingTrash.value = true
                emptyTrashUseCase()
                _isEmptyingTrash.value = false
            } catch (e: Exception) {
                _error.value = "Failed to empty trash: ${e.message}"
                _isEmptyingTrash.value = false
            }
        }
    }

    fun restoreMultipleNotes(noteIds: List<String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                noteIds.forEach { noteId ->
                    restoreFromTrashUseCase(noteId)
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to restore notes: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteMultipleNotesPermanently(noteIds: List<String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                noteIds.forEach { noteId ->
                    deleteNoteUseCase(noteId)
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to delete notes permanently: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun loadNotes() {
        // Notes are loaded automatically via Flow
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // The Flow will automatically update
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to load trashed notes: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}
