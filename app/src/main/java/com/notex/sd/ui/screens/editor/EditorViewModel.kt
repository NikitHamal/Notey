package com.notex.sd.ui.screens.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notex.sd.core.preferences.AppPreferences
import com.notex.sd.domain.model.ChecklistItem
import com.notex.sd.domain.model.Note
import com.notex.sd.domain.model.NoteColor
import com.notex.sd.domain.usecase.CreateNoteUseCase
import com.notex.sd.domain.usecase.GetChecklistItemsUseCase
import com.notex.sd.domain.usecase.GetNoteByIdUseCase
import com.notex.sd.domain.usecase.ObserveNoteByIdUseCase
import com.notex.sd.domain.usecase.SaveChecklistItemsUseCase
import com.notex.sd.domain.usecase.UpdateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val observeNoteByIdUseCase: ObserveNoteByIdUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val getChecklistItemsUseCase: GetChecklistItemsUseCase,
    private val saveChecklistItemsUseCase: SaveChecklistItemsUseCase,
    private val appPreferences: AppPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: String? = savedStateHandle.get<String>("noteId")
    private val isChecklistMode: Boolean = savedStateHandle.get<Boolean>("isChecklist") ?: false
    private val initialFolderId: String? = savedStateHandle.get<String>("folderId")

    private val _uiState = MutableStateFlow(
        EditorUiState(
            noteId = noteId,
            isChecklist = isChecklistMode,
            folderId = initialFolderId,
            isLoading = noteId != null
        )
    )
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var currentNote: Note? = null
    private var autoSaveJob: Job? = null
    private var isAutoSaveEnabled: Boolean = true

    init {
        observeAutoSavePreference()
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    private fun observeAutoSavePreference() {
        viewModelScope.launch {
            appPreferences.autoSave.collect { enabled ->
                isAutoSaveEnabled = enabled
            }
        }
    }

    private fun loadNote(id: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Observe note changes
                observeNoteByIdUseCase(id).collect { note ->
                    if (note != null) {
                        currentNote = note

                        // Load checklist items if it's a checklist note
                        val items = if (note.isChecklist) {
                            getChecklistItemsUseCase.sync(note.id)
                        } else {
                            emptyList()
                        }

                        _uiState.update {
                            EditorUiState(
                                noteId = note.id,
                                title = note.title,
                                content = note.content,
                                checklistItems = items,
                                color = note.color,
                                folderId = note.folderId,
                                isChecklist = note.isChecklist,
                                isLoading = false,
                                hasUnsavedChanges = false,
                                wordCount = note.wordCount,
                                characterCount = note.characterCount,
                                createdAt = note.createdAt,
                                modifiedAt = note.modifiedAt
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Note not found"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load note: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update {
            it.withTitleAndContent(title, it.content)
        }
        scheduleAutoSave()
    }

    fun updateContent(content: String) {
        _uiState.update {
            it.withTitleAndContent(it.title, content)
        }
        scheduleAutoSave()
    }

    fun updateTitleAndContent(title: String, content: String) {
        _uiState.update {
            it.withTitleAndContent(title, content)
        }
        scheduleAutoSave()
    }

    fun updateChecklistItems(items: List<ChecklistItem>) {
        _uiState.update {
            it.withChecklistItems(items)
        }
        scheduleAutoSave()
    }

    fun addChecklistItem(text: String, position: Int? = null) {
        val currentItems = _uiState.value.checklistItems
        val newPosition = position ?: currentItems.size
        val newItem = ChecklistItem.create(
            noteId = _uiState.value.noteId ?: "",
            text = text,
            position = newPosition
        )
        val updatedItems = currentItems.toMutableList().apply {
            add(newPosition.coerceAtMost(size), newItem)
        }.mapIndexed { index, item ->
            item.copy(position = index)
        }
        updateChecklistItems(updatedItems)
    }

    fun removeChecklistItem(itemId: String) {
        val updatedItems = _uiState.value.checklistItems
            .filter { it.id != itemId }
            .mapIndexed { index, item ->
                item.copy(position = index)
            }
        updateChecklistItems(updatedItems)
    }

    fun toggleChecklistItem(itemId: String) {
        val updatedItems = _uiState.value.checklistItems.map { item ->
            if (item.id == itemId) {
                item.copy(isChecked = !item.isChecked)
            } else {
                item
            }
        }
        updateChecklistItems(updatedItems)
    }

    fun updateNoteColor(color: NoteColor) {
        _uiState.update { it.withColor(color) }
        scheduleAutoSave()
    }

    fun updateFolderId(folderId: String?) {
        _uiState.update {
            it.copy(
                folderId = folderId,
                hasUnsavedChanges = true
            )
        }
        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        if (!isAutoSaveEnabled) return

        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(2000) // Auto-save after 2 seconds of inactivity
            saveNote()
        }
    }

    fun saveNote() {
        val state = _uiState.value

        // Don't save if note is empty
        if (state.isEmpty) return

        viewModelScope.launch {
            try {
                _uiState.update { it.markAsSaving() }

                if (state.noteId == null) {
                    // Create new note
                    val note = createNoteUseCase(
                        title = state.title,
                        content = state.content,
                        folderId = state.folderId,
                        color = state.color,
                        isChecklist = state.isChecklist
                    )

                    currentNote = note

                    // Save checklist items if in checklist mode
                    if (state.isChecklist) {
                        val items = state.checklistItems.map { item ->
                            item.copy(noteId = note.id)
                        }
                        saveChecklistItemsUseCase(note.id, items)
                    }

                    _uiState.update {
                        it.copy(noteId = note.id).markAsSaved()
                    }
                } else {
                    // Update existing note
                    val updatedNote = currentNote?.copy(
                        title = state.title,
                        content = state.content,
                        folderId = state.folderId,
                        color = state.color,
                        modifiedAt = System.currentTimeMillis()
                    )?.updateContent(state.title, state.content)

                    if (updatedNote != null) {
                        updateNoteUseCase(updatedNote)
                        currentNote = updatedNote

                        // Save checklist items if in checklist mode
                        if (state.isChecklist) {
                            saveChecklistItemsUseCase(updatedNote.id, state.checklistItems)
                        }

                        _uiState.update { it.markAsSaved() }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save note: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()

        // Auto-save on exit if there are unsaved changes
        if (_uiState.value.hasUnsavedChanges && isAutoSaveEnabled) {
            viewModelScope.launch {
                saveNote()
            }
        }
    }
}
