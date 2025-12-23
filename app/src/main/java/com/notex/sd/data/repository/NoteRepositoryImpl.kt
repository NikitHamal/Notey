package com.notex.sd.data.repository

import com.notex.sd.data.database.dao.ChecklistItemDao
import com.notex.sd.data.database.dao.NoteDao
import com.notex.sd.data.database.entity.ChecklistItemEntity
import com.notex.sd.data.database.entity.NoteEntity
import com.notex.sd.domain.model.ChecklistItem
import com.notex.sd.domain.model.Note
import com.notex.sd.domain.model.NoteColor
import com.notex.sd.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val checklistItemDao: ChecklistItemDao
) : NoteRepository {

    override fun getAllNotes(sortOrder: Int): Flow<List<Note>> {
        return noteDao.getAllNotes(sortOrder).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getNotesByFolder(folderId: String, sortOrder: Int): Flow<List<Note>> {
        return noteDao.getNotesByFolder(folderId, sortOrder).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUncategorizedNotes(sortOrder: Int): Flow<List<Note>> {
        return noteDao.getUncategorizedNotes(sortOrder).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivedNotes(): Flow<List<Note>> {
        return noteDao.getArchivedNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTrashedNotes(): Flow<List<Note>> {
        return noteDao.getTrashedNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getNoteById(id: String): Note? {
        return noteDao.getNoteById(id)?.toDomain()
    }

    override fun observeNoteById(id: String): Flow<Note?> {
        return noteDao.observeNoteById(id).map { it?.toDomain() }
    }

    override suspend fun insertNote(note: Note) {
        noteDao.insertNote(note.toEntity())
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.toEntity())
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note.toEntity())
    }

    override suspend fun deleteNoteById(id: String) {
        noteDao.deleteNoteById(id)
    }

    override suspend fun updatePinStatus(id: String, isPinned: Boolean) {
        noteDao.updatePinStatus(id, isPinned, System.currentTimeMillis())
    }

    override suspend fun updateArchiveStatus(id: String, isArchived: Boolean) {
        noteDao.updateArchiveStatus(id, isArchived, System.currentTimeMillis())
    }

    override suspend fun moveToTrash(id: String) {
        noteDao.moveToTrash(id)
    }

    override suspend fun restoreFromTrash(id: String) {
        noteDao.restoreFromTrash(id)
    }

    override suspend fun updateNoteFolder(id: String, folderId: String?) {
        noteDao.updateNoteFolder(id, folderId, System.currentTimeMillis())
    }

    override suspend fun updateNoteColor(id: String, color: NoteColor) {
        noteDao.updateNoteColor(id, color.ordinal, System.currentTimeMillis())
    }

    override suspend fun emptyTrash() {
        noteDao.emptyTrash()
    }

    override suspend fun deleteOldTrashedNotes(daysOld: Int) {
        val threshold = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        noteDao.deleteOldTrashedNotes(threshold)
    }

    override fun getActiveNotesCount(): Flow<Int> {
        return noteDao.getActiveNotesCount()
    }

    override fun getArchivedNotesCount(): Flow<Int> {
        return noteDao.getArchivedNotesCount()
    }

    override fun getTrashedNotesCount(): Flow<Int> {
        return noteDao.getTrashedNotesCount()
    }

    override fun getNotesCountByFolder(folderId: String): Flow<Int> {
        return noteDao.getNotesCountByFolder(folderId)
    }

    override fun getChecklistItems(noteId: String): Flow<List<ChecklistItem>> {
        return checklistItemDao.getChecklistItemsByNoteId(noteId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getChecklistItemsSync(noteId: String): List<ChecklistItem> {
        return checklistItemDao.getChecklistItemsByNoteIdSync(noteId).map { it.toDomain() }
    }

    override suspend fun insertChecklistItem(item: ChecklistItem) {
        checklistItemDao.insertChecklistItem(item.toEntity())
    }

    override suspend fun insertChecklistItems(items: List<ChecklistItem>) {
        checklistItemDao.insertChecklistItems(items.map { it.toEntity() })
    }

    override suspend fun updateChecklistItem(item: ChecklistItem) {
        checklistItemDao.updateChecklistItem(item.toEntity())
    }

    override suspend fun deleteChecklistItem(item: ChecklistItem) {
        checklistItemDao.deleteChecklistItem(item.toEntity())
    }

    override suspend fun deleteChecklistItemsByNoteId(noteId: String) {
        checklistItemDao.deleteChecklistItemsByNoteId(noteId)
    }

    override suspend fun updateCheckStatus(id: String, isChecked: Boolean) {
        checklistItemDao.updateCheckStatus(id, isChecked)
    }

    private fun NoteEntity.toDomain(): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            plainTextContent = plainTextContent,
            folderId = folderId,
            color = NoteColor.entries.getOrNull(color) ?: NoteColor.DEFAULT,
            isPinned = isPinned,
            isArchived = isArchived,
            isTrashed = isTrashed,
            isChecklist = isChecklist,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            trashedAt = trashedAt,
            wordCount = wordCount,
            characterCount = characterCount
        )
    }

    private fun Note.toEntity(): NoteEntity {
        return NoteEntity(
            id = id,
            title = title,
            content = content,
            plainTextContent = plainTextContent,
            folderId = folderId,
            color = color.ordinal,
            isPinned = isPinned,
            isArchived = isArchived,
            isTrashed = isTrashed,
            isChecklist = isChecklist,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            trashedAt = trashedAt,
            wordCount = wordCount,
            characterCount = characterCount
        )
    }

    private fun ChecklistItemEntity.toDomain(): ChecklistItem {
        return ChecklistItem(
            id = id,
            noteId = noteId,
            text = text,
            isChecked = isChecked,
            position = position,
            indentation = indentation
        )
    }

    private fun ChecklistItem.toEntity(): ChecklistItemEntity {
        return ChecklistItemEntity(
            id = id,
            noteId = noteId,
            text = text,
            isChecked = isChecked,
            position = position,
            indentation = indentation
        )
    }
}
