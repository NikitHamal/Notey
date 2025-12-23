package com.notex.sd.data.repository

import com.notex.sd.data.database.dao.FolderDao
import com.notex.sd.data.database.entity.FolderEntity
import com.notex.sd.domain.model.Folder
import com.notex.sd.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao
) : FolderRepository {

    override fun getAllFolders(): Flow<List<Folder>> {
        return folderDao.getAllFolders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRootFolders(): Flow<List<Folder>> {
        return folderDao.getRootFolders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getChildFolders(parentId: String): Flow<List<Folder>> {
        return folderDao.getChildFolders(parentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getFolderById(id: String): Folder? {
        return folderDao.getFolderById(id)?.toDomain()
    }

    override fun observeFolderById(id: String): Flow<Folder?> {
        return folderDao.observeFolderById(id).map { it?.toDomain() }
    }

    override suspend fun insertFolder(folder: Folder) {
        folderDao.insertFolder(folder.toEntity())
    }

    override suspend fun updateFolder(folder: Folder) {
        folderDao.updateFolder(folder.toEntity())
    }

    override suspend fun deleteFolder(folder: Folder) {
        folderDao.deleteFolder(folder.toEntity())
    }

    override suspend fun deleteFolderById(id: String) {
        folderDao.deleteFolderById(id)
    }

    override suspend fun updateFolderName(id: String, name: String) {
        folderDao.updateFolderName(id, name, System.currentTimeMillis())
    }

    override suspend fun updateFolderColor(id: String, color: Int) {
        folderDao.updateFolderColor(id, color, System.currentTimeMillis())
    }

    override suspend fun updateFolderExpanded(id: String, isExpanded: Boolean) {
        folderDao.updateFolderExpanded(id, isExpanded)
    }

    override suspend fun updateFolderPosition(id: String, position: Int) {
        folderDao.updateFolderPosition(id, position)
    }

    override suspend fun getMaxRootPosition(): Int {
        return folderDao.getMaxRootPosition() ?: -1
    }

    override suspend fun getMaxChildPosition(parentId: String): Int {
        return folderDao.getMaxChildPosition(parentId) ?: -1
    }

    override fun getFoldersCount(): Flow<Int> {
        return folderDao.getFoldersCount()
    }

    override suspend fun folderNameExists(name: String, excludeId: String): Boolean {
        return folderDao.folderNameExists(name, excludeId)
    }

    private fun FolderEntity.toDomain(): Folder {
        return Folder(
            id = id,
            name = name,
            parentId = parentId,
            color = color,
            icon = icon,
            position = position,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            isExpanded = isExpanded
        )
    }

    private fun Folder.toEntity(): FolderEntity {
        return FolderEntity(
            id = id,
            name = name,
            parentId = parentId,
            color = color,
            icon = icon,
            position = position,
            createdAt = createdAt,
            modifiedAt = modifiedAt,
            isExpanded = isExpanded
        )
    }
}
