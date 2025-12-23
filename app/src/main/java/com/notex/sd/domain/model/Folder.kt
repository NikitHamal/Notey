package com.notex.sd.domain.model

import java.util.UUID

data class Folder(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val parentId: String? = null,
    val color: Int = 0,
    val icon: String? = null,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isExpanded: Boolean = true
) {
    companion object {
        fun create(
            name: String,
            parentId: String? = null,
            color: Int = 0,
            icon: String? = null,
            position: Int = 0
        ): Folder {
            val now = System.currentTimeMillis()
            return Folder(
                id = UUID.randomUUID().toString(),
                name = name,
                parentId = parentId,
                color = color,
                icon = icon,
                position = position,
                createdAt = now,
                modifiedAt = now,
                isExpanded = true
            )
        }
    }
}

data class FolderWithChildren(
    val folder: Folder,
    val children: List<FolderWithChildren> = emptyList(),
    val notesCount: Int = 0
)
