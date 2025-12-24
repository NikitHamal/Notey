package com.notex.sd.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notex.sd.core.preferences.AppPreferences
import com.notex.sd.core.preferences.SortOrder
import com.notex.sd.core.preferences.ViewMode
import com.notex.sd.domain.model.Note
import com.notex.sd.domain.model.NoteColor
import com.notex.sd.domain.usecase.GetAllNotesUseCase
import com.notex.sd.domain.usecase.GetNotesCountUseCase
import com.notex.sd.domain.usecase.InsertNoteUseCase
import com.notex.sd.domain.usecase.MoveToTrashUseCase
import com.notex.sd.domain.usecase.ToggleArchiveUseCase
import com.notex.sd.domain.usecase.TogglePinUseCase
import com.notex.sd.domain.usecase.UpdateNoteColorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val getNotesCountUseCase: GetNotesCountUseCase,
    private val insertNoteUseCase: InsertNoteUseCase,
    private val togglePinUseCase: TogglePinUseCase,
    private val moveToTrashUseCase: MoveToTrashUseCase,
    private val toggleArchiveUseCase: ToggleArchiveUseCase,
    private val updateNoteColorUseCase: UpdateNoteColorUseCase,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val _createdNoteId = MutableStateFlow<String?>(null)
    val createdNoteId: StateFlow<String?> = _createdNoteId.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        appPreferences.viewMode,
        appPreferences.sortOrder,
        _isLoading,
        _error
    ) { viewMode, sortOrder, isLoading, error ->
        Triple(viewMode, sortOrder, isLoading to error)
    }.combine(
        getAllNotesUseCase.invoke(SortOrder.MODIFIED_DESC)
    ) { (viewMode, sortOrder, loadingError), notes ->
        val (isLoading, error) = loadingError
        HomeUiState.fromNotes(
            notes = notes,
            viewMode = viewMode,
            sortOrder = sortOrder,
            isLoading = isLoading,
            error = error
        )
    }.catch { throwable ->
        emit(
            HomeUiState(
                isLoading = false,
                error = throwable.message ?: "An error occurred"
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    val activeNotesCount: StateFlow<Int> = getNotesCountUseCase.getActiveCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val archivedNotesCount: StateFlow<Int> = getNotesCountUseCase.getArchivedCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val trashedNotesCount: StateFlow<Int> = getNotesCountUseCase.getTrashedCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun togglePin(noteId: String, isPinned: Boolean) {
        viewModelScope.launch {
            try {
                togglePinUseCase(noteId, !isPinned)
            } catch (e: Exception) {
                _error.value = "Failed to ${if (isPinned) "unpin" else "pin"} note: ${e.message}"
            }
        }
    }

    fun moveToTrash(noteId: String) {
        viewModelScope.launch {
            try {
                moveToTrashUseCase(noteId)
            } catch (e: Exception) {
                _error.value = "Failed to move note to trash: ${e.message}"
            }
        }
    }

    fun archiveNote(noteId: String, isArchived: Boolean) {
        viewModelScope.launch {
            try {
                toggleArchiveUseCase(noteId, !isArchived)
            } catch (e: Exception) {
                _error.value = "Failed to ${if (isArchived) "unarchive" else "archive"} note: ${e.message}"
            }
        }
    }

    fun updateNoteColor(noteId: String, color: NoteColor) {
        viewModelScope.launch {
            try {
                updateNoteColorUseCase(noteId, color)
            } catch (e: Exception) {
                _error.value = "Failed to update note color: ${e.message}"
            }
        }
    }

    fun toggleViewMode() {
        viewModelScope.launch {
            try {
                val currentMode = appPreferences.viewMode.first()
                val newMode = when (currentMode) {
                    ViewMode.GRID -> ViewMode.LIST
                    ViewMode.LIST -> ViewMode.GRID
                }
                appPreferences.setViewMode(newMode)
            } catch (e: Exception) {
                _error.value = "Failed to toggle view mode: ${e.message}"
            }
        }
    }

    fun updateSortOrder(sortOrder: SortOrder) {
        viewModelScope.launch {
            try {
                appPreferences.setSortOrder(sortOrder)
            } catch (e: Exception) {
                _error.value = "Failed to update sort order: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun loadNotes() {
        // Notes are loaded automatically via Flow
        // This method can be used to trigger a refresh if needed
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // The Flow will automatically update
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to load notes: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun createNoteFromTemplate(note: Note) {
        viewModelScope.launch {
            try {
                val noteId = insertNoteUseCase(note)
                _createdNoteId.value = noteId
            } catch (e: Exception) {
                _error.value = "Failed to create note from template: ${e.message}"
            }
        }
    }

    fun clearCreatedNoteId() {
        _createdNoteId.value = null
    }
}
