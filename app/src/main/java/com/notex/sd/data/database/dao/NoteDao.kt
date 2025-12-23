package com.notex.sd.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.notex.sd.data.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query(
        """
        SELECT * FROM notes
        WHERE is_trashed = 0 AND is_archived = 0
        ORDER BY
            is_pinned DESC,
            CASE WHEN :sortOrder = 0 THEN modified_at END DESC,
            CASE WHEN :sortOrder = 1 THEN modified_at END ASC,
            CASE WHEN :sortOrder = 2 THEN created_at END DESC,
            CASE WHEN :sortOrder = 3 THEN created_at END ASC,
            CASE WHEN :sortOrder = 4 THEN title END ASC,
            CASE WHEN :sortOrder = 5 THEN title END DESC
        """
    )
    fun getAllNotes(sortOrder: Int): Flow<List<NoteEntity>>

    @Query(
        """
        SELECT * FROM notes
        WHERE is_trashed = 0 AND is_archived = 0 AND folder_id = :folderId
        ORDER BY
            is_pinned DESC,
            CASE WHEN :sortOrder = 0 THEN modified_at END DESC,
            CASE WHEN :sortOrder = 1 THEN modified_at END ASC,
            CASE WHEN :sortOrder = 2 THEN created_at END DESC,
            CASE WHEN :sortOrder = 3 THEN created_at END ASC,
            CASE WHEN :sortOrder = 4 THEN title END ASC,
            CASE WHEN :sortOrder = 5 THEN title END DESC
        """
    )
    fun getNotesByFolder(folderId: String, sortOrder: Int): Flow<List<NoteEntity>>

    @Query(
        """
        SELECT * FROM notes
        WHERE is_trashed = 0 AND is_archived = 0 AND folder_id IS NULL
        ORDER BY
            is_pinned DESC,
            CASE WHEN :sortOrder = 0 THEN modified_at END DESC,
            CASE WHEN :sortOrder = 1 THEN modified_at END ASC,
            CASE WHEN :sortOrder = 2 THEN created_at END DESC,
            CASE WHEN :sortOrder = 3 THEN created_at END ASC,
            CASE WHEN :sortOrder = 4 THEN title END ASC,
            CASE WHEN :sortOrder = 5 THEN title END DESC
        """
    )
    fun getUncategorizedNotes(sortOrder: Int): Flow<List<NoteEntity>>

    @Query(
        """
        SELECT * FROM notes
        WHERE is_archived = 1 AND is_trashed = 0
        ORDER BY modified_at DESC
        """
    )
    fun getArchivedNotes(): Flow<List<NoteEntity>>

    @Query(
        """
        SELECT * FROM notes
        WHERE is_trashed = 1
        ORDER BY trashed_at DESC
        """
    )
    fun getTrashedNotes(): Flow<List<NoteEntity>>

    @Query(
        """
        SELECT * FROM notes
        WHERE is_trashed = 0 AND is_archived = 0
        AND (
            title LIKE '%' || :query || '%'
            OR plain_text_content LIKE '%' || :query || '%'
        )
        ORDER BY
            CASE
                WHEN title LIKE :query || '%' THEN 1
                WHEN title LIKE '%' || :query || '%' THEN 2
                ELSE 3
            END,
            modified_at DESC
        """
    )
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE id = :id")
    fun observeNoteById(id: String): Flow<NoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("UPDATE notes SET is_pinned = :isPinned, modified_at = :modifiedAt WHERE id = :id")
    suspend fun updatePinStatus(id: String, isPinned: Boolean, modifiedAt: Long)

    @Query("UPDATE notes SET is_archived = :isArchived, modified_at = :modifiedAt WHERE id = :id")
    suspend fun updateArchiveStatus(id: String, isArchived: Boolean, modifiedAt: Long)

    @Query(
        """
        UPDATE notes
        SET is_trashed = :isTrashed,
            trashed_at = :trashedAt,
            modified_at = :modifiedAt
        WHERE id = :id
        """
    )
    suspend fun updateTrashStatus(id: String, isTrashed: Boolean, trashedAt: Long?, modifiedAt: Long)

    @Query("UPDATE notes SET folder_id = :folderId, modified_at = :modifiedAt WHERE id = :id")
    suspend fun updateNoteFolder(id: String, folderId: String?, modifiedAt: Long)

    @Query("UPDATE notes SET color = :color, modified_at = :modifiedAt WHERE id = :id")
    suspend fun updateNoteColor(id: String, color: Int, modifiedAt: Long)

    @Query("DELETE FROM notes WHERE is_trashed = 1")
    suspend fun emptyTrash()

    @Query("DELETE FROM notes WHERE is_trashed = 1 AND trashed_at < :threshold")
    suspend fun deleteOldTrashedNotes(threshold: Long)

    @Query("SELECT COUNT(*) FROM notes WHERE is_trashed = 0 AND is_archived = 0")
    fun getActiveNotesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE is_archived = 1 AND is_trashed = 0")
    fun getArchivedNotesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE is_trashed = 1")
    fun getTrashedNotesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE folder_id = :folderId AND is_trashed = 0 AND is_archived = 0")
    fun getNotesCountByFolder(folderId: String): Flow<Int>

    @Transaction
    suspend fun moveToTrash(id: String) {
        val now = System.currentTimeMillis()
        updateTrashStatus(id, isTrashed = true, trashedAt = now, modifiedAt = now)
    }

    @Transaction
    suspend fun restoreFromTrash(id: String) {
        val now = System.currentTimeMillis()
        updateTrashStatus(id, isTrashed = false, trashedAt = null, modifiedAt = now)
    }
}
