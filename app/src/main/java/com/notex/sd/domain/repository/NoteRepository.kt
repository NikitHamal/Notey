package com.notex.sd.domain.repository

import com.notex.sd.domain.model.ChecklistItem
import com.notex.sd.domain.model.Note
import com.notex.sd.domain.model.NoteColor
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(sortOrder: Int): Flow<List<Note>>
    fun getNotesByFolder(folderId: String, sortOrder: Int): Flow<List<Note>>
    fun getUncategorizedNotes(sortOrder: Int): Flow<List<Note>>
    fun getArchivedNotes(): Flow<List<Note>>
    fun getTrashedNotes(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    fun observeNoteById(id: String): Flow<Note?>
    suspend fun insertNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun deleteNoteById(id: String)
    suspend fun updatePinStatus(id: String, isPinned: Boolean)
    suspend fun updateArchiveStatus(id: String, isArchived: Boolean)
    suspend fun moveToTrash(id: String)
    suspend fun restoreFromTrash(id: String)
    suspend fun updateNoteFolder(id: String, folderId: String?)
    suspend fun updateNoteColor(id: String, color: NoteColor)
    suspend fun emptyTrash()
    suspend fun deleteOldTrashedNotes(daysOld: Int)
    fun getActiveNotesCount(): Flow<Int>
    fun getArchivedNotesCount(): Flow<Int>
    fun getTrashedNotesCount(): Flow<Int>
    fun getNotesCountByFolder(folderId: String): Flow<Int>

    fun getChecklistItems(noteId: String): Flow<List<ChecklistItem>>
    suspend fun getChecklistItemsSync(noteId: String): List<ChecklistItem>
    suspend fun insertChecklistItem(item: ChecklistItem)
    suspend fun insertChecklistItems(items: List<ChecklistItem>)
    suspend fun updateChecklistItem(item: ChecklistItem)
    suspend fun deleteChecklistItem(item: ChecklistItem)
    suspend fun deleteChecklistItemsByNoteId(noteId: String)
    suspend fun updateCheckStatus(id: String, isChecked: Boolean)
}
