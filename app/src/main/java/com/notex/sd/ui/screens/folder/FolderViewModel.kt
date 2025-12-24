package com.notex.sd.ui.screens.folder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notex.sd.core.preferences.AppPreferences
import com.notex.sd.core.preferences.SortOrder
import com.notex.sd.core.preferences.ViewMode
import com.notex.sd.domain.model.Folder
import com.notex.sd.domain.model.Note
import com.notex.sd.domain.model.NoteColor
import com.notex.sd.domain.usecase.DeleteFolderUseCase
import com.notex.sd.domain.usecase.GetFolderByIdUseCase
import com.notex.sd.domain.usecase.GetNotesByFolderUseCase
import com.notex.sd.domain.usecase.GetNotesCountUseCase
import com.notex.sd.domain.usecase.MoveToTrashUseCase
import com.notex.sd.domain.usecase.ToggleArchiveUseCase
import com.notex.sd.domain.usecase.TogglePinUseCase
import com.notex.sd.domain.usecase.UpdateFolderUseCase
import com.notex.sd.domain.usecase.UpdateNoteColorUseCase
import com.notex.sd.domain.usecase.UpdateNoteFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FolderUiState(
    val folder: Folder? = null,
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

    val folderName: String
        get() = folder?.name ?: "Folder"

    val folderColor: Int
        get() = folder?.color ?: 0

    companion object {
        fun fromData(
            folder: Folder?,
            notes: List<Note>,
            viewMode: ViewMode,
            sortOrder: SortOrder,
            isLoading: Boolean = false,
            error: String? = null
        ): FolderUiState {
            val pinned = notes.filter { it.isPinned }
            val other = notes.filter { !it.isPinned }

            return FolderUiState(
                folder = folder,
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

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val getNotesByFolderUseCase: GetNotesByFolderUseCase,
    private val getFolderByIdUseCase: GetFolderByIdUseCase,
    private val getNotesCountUseCase: GetNotesCountUseCase,
    private val togglePinUseCase: TogglePinUseCase,
    private val moveToTrashUseCase: MoveToTrashUseCase,
    private val toggleArchiveUseCase: ToggleArchiveUseCase,
    private val updateNoteColorUseCase: UpdateNoteColorUseCase,
    private val updateNoteFolderUseCase: UpdateNoteFolderUseCase,
    private val updateFolderUseCase: UpdateFolderUseCase,
    private val deleteFolderUseCase: DeleteFolderUseCase,
    private val appPreferences: AppPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val folderId: String? = savedStateHandle.get<String>("folderId")

    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _folder = MutableStateFlow<Folder?>(null)

    init {
        loadFolder()
    }

    val uiState: StateFlow<FolderUiState> = combine(
        appPreferences.viewMode,
        appPreferences.sortOrder,
        _folder,
        _isLoading,
        _error
    ) { viewMode, sortOrder, folder, isLoading, error ->
        FolderData(viewMode, sortOrder, folder, isLoading, error)
    }.combine(
        if (folderId != null) {
            getNotesByFolderUseCase(folderId, SortOrder.MODIFIED_DESC)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    ) { folderData, notes ->
        FolderUiState.fromData(
            folder = folderData.folder,
            notes = notes,
            viewMode = folderData.viewMode,
            sortOrder = folderData.sortOrder,
            isLoading = folderData.isLoading,
            error = folderData.error
        )
    }.catch { throwable ->
        emit(
            FolderUiState(
                isLoading = false,
                error = throwable.message ?: "An error occurred"
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FolderUiState(isLoading = true)
    )

    private data class FolderData(
        val viewMode: ViewMode,
        val sortOrder: SortOrder,
        val folder: Folder?,
        val isLoading: Boolean,
        val error: String?
    )

    val folderNotesCount: StateFlow<Int> = if (folderId != null) {
        getNotesCountUseCase.getCountByFolder(folderId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )
    } else {
        MutableStateFlow(0)
    }

    private fun loadFolder() {
        if (folderId == null) {
            _error.value = "Folder ID is required"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                getFolderByIdUseCase.observe(folderId).collect { folder ->
                    _folder.value = folder
                    _isLoading.value = false

                    if (folder == null) {
                        _error.value = "Folder not found"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load folder: ${e.message}"
                _isLoading.value = false
            }
        }
    }

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

    fun moveNoteToFolder(noteId: String, targetFolderId: String?) {
        viewModelScope.launch {
            try {
                updateNoteFolderUseCase(noteId, targetFolderId)
            } catch (e: Exception) {
                _error.value = "Failed to move note: ${e.message}"
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

    fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            try {
                updateFolderUseCase.updateName(folderId, newName)
            } catch (e: Exception) {
                _error.value = "Failed to rename folder: ${e.message}"
            }
        }
    }

    fun changeFolderColor(folderId: String, color: Int) {
        viewModelScope.launch {
            try {
                updateFolderUseCase.updateColor(folderId, color)
            } catch (e: Exception) {
                _error.value = "Failed to change folder color: ${e.message}"
            }
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            try {
                deleteFolderUseCase(folderId)
            } catch (e: Exception) {
                _error.value = "Failed to delete folder: ${e.message}"
            }
        }
    }
}
