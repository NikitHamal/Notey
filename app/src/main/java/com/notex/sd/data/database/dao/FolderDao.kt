package com.notex.sd.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.notex.sd.data.database.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders ORDER BY position ASC, name ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parent_id IS NULL ORDER BY position ASC, name ASC")
    fun getRootFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parent_id = :parentId ORDER BY position ASC, name ASC")
    fun getChildFolders(parentId: String): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE id = :id")
    fun observeFolderById(id: String): Flow<FolderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolderById(id: String)

    @Query("UPDATE folders SET name = :name, modified_at = :modifiedAt WHERE id = :id")
    suspend fun updateFolderName(id: String, name: String, modifiedAt: Long)

    @Query("UPDATE folders SET color = :color, modified_at = :modifiedAt WHERE id = :id")
    suspend fun updateFolderColor(id: String, color: Int, modifiedAt: Long)

    @Query("UPDATE folders SET is_expanded = :isExpanded WHERE id = :id")
    suspend fun updateFolderExpanded(id: String, isExpanded: Boolean)

    @Query("UPDATE folders SET position = :position WHERE id = :id")
    suspend fun updateFolderPosition(id: String, position: Int)

    @Query("SELECT MAX(position) FROM folders WHERE parent_id IS NULL")
    suspend fun getMaxRootPosition(): Int?

    @Query("SELECT MAX(position) FROM folders WHERE parent_id = :parentId")
    suspend fun getMaxChildPosition(parentId: String): Int?

    @Query("SELECT COUNT(*) FROM folders")
    fun getFoldersCount(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM folders WHERE name = :name AND id != :excludeId)")
    suspend fun folderNameExists(name: String, excludeId: String): Boolean
}
