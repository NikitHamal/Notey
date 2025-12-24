package com.notex.sd.domain.usecase

import com.notex.sd.core.preferences.SortOrder
import com.notex.sd.domain.model.ChecklistItem
import com.notex.sd.domain.model.Note
import com.notex.sd.domain.model.NoteColor
import com.notex.sd.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(sortOrder: SortOrder): Flow<List<Note>> {
        return repository.getAllNotes(sortOrder.value)
    }
}

class GetNotesByFolderUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(folderId: String, sortOrder: SortOrder): Flow<List<Note>> {
        return repository.getNotesByFolder(folderId, sortOrder.value)
    }
}

class GetUncategorizedNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(sortOrder: SortOrder): Flow<List<Note>> {
        return repository.getUncategorizedNotes(sortOrder.value)
    }
}

class GetArchivedNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> {
        return repository.getArchivedNotes()
    }
}

class GetTrashedNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> {
        return repository.getTrashedNotes()
    }
}

class SearchNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(query: String): Flow<List<Note>> {
        return repository.searchNotes(query)
    }
}

class GetNoteByIdUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: String): Note? {
        return repository.getNoteById(id)
    }
}

class ObserveNoteByIdUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(id: String): Flow<Note?> {
        return repository.observeNoteById(id)
    }
}

class CreateNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(
        title: String = "",
        content: String = "",
        folderId: String? = null,
        color: NoteColor = NoteColor.DEFAULT,
        isChecklist: Boolean = false
    ): Note {
        val note = Note.create(
            title = title,
            content = content,
            folderId = folderId,
            color = color,
            isChecklist = isChecklist
        )
        repository.insertNote(note)
        return note
    }
}

class InsertNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note): String {
        repository.insertNote(note)
        return note.id
    }
}

class UpdateNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.updateNote(note)
    }
}

class DeleteNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteNoteById(id)
    }
}

class TogglePinUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: String, isPinned: Boolean) {
        repository.updatePinStatus(id, isPinned)
    }
}

class ToggleArchiveUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: String, isArchived: Boolean) {
        repository.updateArchiveStatus(id, isArchived)
    }
}

class MoveToTrashUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: String) {
        repository.moveToTrash(id)
    }
}

class RestoreFromTrashUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: String) {
        repository.restoreFromTrash(id)
    }
}

class UpdateNoteFolderUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(noteId: String, folderId: String?) {
        repository.updateNoteFolder(noteId, folderId)
    }
}

class UpdateNoteColorUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(noteId: String, color: NoteColor) {
        repository.updateNoteColor(noteId, color)
    }
}

class EmptyTrashUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke() {
        repository.emptyTrash()
    }
}

class CleanOldTrashUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(daysOld: Int = 30) {
        repository.deleteOldTrashedNotes(daysOld)
    }
}

class GetNotesCountUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    fun getActiveCount(): Flow<Int> = repository.getActiveNotesCount()
    fun getArchivedCount(): Flow<Int> = repository.getArchivedNotesCount()
    fun getTrashedCount(): Flow<Int> = repository.getTrashedNotesCount()
    fun getCountByFolder(folderId: String): Flow<Int> = repository.getNotesCountByFolder(folderId)
}

class GetChecklistItemsUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(noteId: String): Flow<List<ChecklistItem>> {
        return repository.getChecklistItems(noteId)
    }

    suspend fun sync(noteId: String): List<ChecklistItem> {
        return repository.getChecklistItemsSync(noteId)
    }
}

class SaveChecklistItemsUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(noteId: String, items: List<ChecklistItem>) {
        repository.deleteChecklistItemsByNoteId(noteId)
        if (items.isNotEmpty()) {
            repository.insertChecklistItems(items)
        }
    }
}

class ToggleChecklistItemUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(itemId: String, isChecked: Boolean) {
        repository.updateCheckStatus(itemId, isChecked)
    }
}
