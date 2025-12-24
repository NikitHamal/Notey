package com.notex.sd.ui.screens.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notex.sd.domain.model.Note
import com.notex.sd.domain.usecase.GetArchivedNotesUseCase
import com.notex.sd.domain.usecase.MoveToTrashUseCase
import com.notex.sd.domain.usecase.ToggleArchiveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArchiveUiState(
    val archivedNotes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val hasNotes: Boolean
        get() = archivedNotes.isNotEmpty()

    val notesCount: Int
        get() = archivedNotes.size
}

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val getArchivedNotesUseCase: GetArchivedNotesUseCase,
    private val toggleArchiveUseCase: ToggleArchiveUseCase,
    private val moveToTrashUseCase: MoveToTrashUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ArchiveUiState> = combine(
        getArchivedNotesUseCase(),
        _isLoading,
        _error
    ) { notes, isLoading, error ->
        ArchiveUiState(
            archivedNotes = notes,
            isLoading = isLoading,
            error = error
        )
    }.catch { throwable ->
        emit(
            ArchiveUiState(
                isLoading = false,
                error = throwable.message ?: "An error occurred"
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ArchiveUiState(isLoading = true)
    )

    fun unarchiveNote(noteId: String) {
        viewModelScope.launch {
            try {
                toggleArchiveUseCase(noteId, false)
            } catch (e: Exception) {
                _error.value = "Failed to unarchive note: ${e.message}"
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            try {
                moveToTrashUseCase(noteId)
            } catch (e: Exception) {
                _error.value = "Failed to delete note: ${e.message}"
            }
        }
    }

    fun unarchiveMultipleNotes(noteIds: List<String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                noteIds.forEach { noteId ->
                    toggleArchiveUseCase(noteId, false)
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to unarchive notes: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteMultipleNotes(noteIds: List<String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                noteIds.forEach { noteId ->
                    moveToTrashUseCase(noteId)
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to delete notes: ${e.message}"
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
                _error.value = "Failed to load archived notes: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}
