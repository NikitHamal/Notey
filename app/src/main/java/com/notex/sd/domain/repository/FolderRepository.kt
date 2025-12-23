package com.notex.sd.domain.repository

import com.notex.sd.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getAllFolders(): Flow<List<Folder>>
    fun getRootFolders(): Flow<List<Folder>>
    fun getChildFolders(parentId: String): Flow<List<Folder>>
    suspend fun getFolderById(id: String): Folder?
    fun observeFolderById(id: String): Flow<Folder?>
    suspend fun insertFolder(folder: Folder)
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(folder: Folder)
    suspend fun deleteFolderById(id: String)
    suspend fun updateFolderName(id: String, name: String)
    suspend fun updateFolderColor(id: String, color: Int)
    suspend fun updateFolderExpanded(id: String, isExpanded: Boolean)
    suspend fun updateFolderPosition(id: String, position: Int)
    suspend fun getMaxRootPosition(): Int
    suspend fun getMaxChildPosition(parentId: String): Int
    fun getFoldersCount(): Flow<Int>
    suspend fun folderNameExists(name: String, excludeId: String): Boolean
}
