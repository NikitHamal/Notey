package com.notex.sd.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.notex.sd.data.database.entity.ChecklistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistItemDao {

    @Query("SELECT * FROM checklist_items WHERE note_id = :noteId ORDER BY position ASC")
    fun getChecklistItemsByNoteId(noteId: String): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items WHERE note_id = :noteId ORDER BY position ASC")
    suspend fun getChecklistItemsByNoteIdSync(noteId: String): List<ChecklistItemEntity>

    @Query("SELECT * FROM checklist_items WHERE id = :id")
    suspend fun getChecklistItemById(id: String): ChecklistItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItem(item: ChecklistItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<ChecklistItemEntity>)

    @Update
    suspend fun updateChecklistItem(item: ChecklistItemEntity)

    @Delete
    suspend fun deleteChecklistItem(item: ChecklistItemEntity)

    @Query("DELETE FROM checklist_items WHERE id = :id")
    suspend fun deleteChecklistItemById(id: String)

    @Query("DELETE FROM checklist_items WHERE note_id = :noteId")
    suspend fun deleteChecklistItemsByNoteId(noteId: String)

    @Query("UPDATE checklist_items SET is_checked = :isChecked WHERE id = :id")
    suspend fun updateCheckStatus(id: String, isChecked: Boolean)

    @Query("UPDATE checklist_items SET text = :text WHERE id = :id")
    suspend fun updateItemText(id: String, text: String)

    @Query("UPDATE checklist_items SET position = :position WHERE id = :id")
    suspend fun updateItemPosition(id: String, position: Int)

    @Query("SELECT COUNT(*) FROM checklist_items WHERE note_id = :noteId")
    suspend fun getItemsCount(noteId: String): Int

    @Query("SELECT COUNT(*) FROM checklist_items WHERE note_id = :noteId AND is_checked = 1")
    suspend fun getCheckedItemsCount(noteId: String): Int
}
