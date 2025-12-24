package com.notex.sd.domain.usecase

import com.notex.sd.domain.model.Folder
import com.notex.sd.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllFoldersUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    operator fun invoke(): Flow<List<Folder>> {
        return repository.getAllFolders()
    }
}

class GetRootFoldersUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    operator fun invoke(): Flow<List<Folder>> {
        return repository.getRootFolders()
    }
}

class GetChildFoldersUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    operator fun invoke(parentId: String): Flow<List<Folder>> {
        return repository.getChildFolders(parentId)
    }
}

class GetFolderByIdUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    suspend operator fun invoke(id: String): Folder? {
        return repository.getFolderById(id)
    }

    fun observe(id: String): Flow<Folder?> {
        return repository.observeFolderById(id)
    }
}

class CreateFolderUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    suspend operator fun invoke(
        name: String,
        parentId: String? = null,
        color: Int = 0,
        icon: String? = null
    ): Folder {
        val position = if (parentId == null) {
            repository.getMaxRootPosition() + 1
        } else {
            repository.getMaxChildPosition(parentId) + 1
        }

        val folder = Folder.create(
            name = name,
            parentId = parentId,
            color = color,
            icon = icon,
            position = position
        )

        repository.insertFolder(folder)
        return folder
    }
}

class UpdateFolderUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    suspend operator fun invoke(folder: Folder) {
        repository.updateFolder(folder)
    }

    suspend fun updateName(id: String, name: String) {
        repository.updateFolderName(id, name)
    }

    suspend fun updateColor(id: String, color: Int) {
        repository.updateFolderColor(id, color)
    }
}

class DeleteFolderUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteFolderById(id)
    }

    suspend fun delete(folder: Folder) {
        repository.deleteFolder(folder)
    }
}

class UpdateFolderExpandedUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    suspend operator fun invoke(id: String, isExpanded: Boolean) {
        repository.updateFolderExpanded(id, isExpanded)
    }
}

class GetFoldersCountUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    operator fun invoke(): Flow<Int> {
        return repository.getFoldersCount()
    }
}
