package com.notex.sd.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["folder_id"]),
        Index(value = ["is_pinned"]),
        Index(value = ["is_archived"]),
        Index(value = ["is_trashed"]),
        Index(value = ["modified_at"]),
        Index(value = ["created_at"])
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "plain_text_content")
    val plainTextContent: String,

    @ColumnInfo(name = "folder_id")
    val folderId: String?,

    @ColumnInfo(name = "color")
    val color: Int,

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean,

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean,

    @ColumnInfo(name = "is_trashed")
    val isTrashed: Boolean,

    @ColumnInfo(name = "is_checklist")
    val isChecklist: Boolean,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "modified_at")
    val modifiedAt: Long,

    @ColumnInfo(name = "trashed_at")
    val trashedAt: Long?,

    @ColumnInfo(name = "word_count")
    val wordCount: Int,

    @ColumnInfo(name = "character_count")
    val characterCount: Int
)
