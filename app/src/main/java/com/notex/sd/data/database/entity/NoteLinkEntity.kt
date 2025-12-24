package com.notex.sd.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Database entity for storing note-to-note links.
 * Enables wiki-style [[note]] linking feature.
 */
@Entity(
    tableName = "note_links",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["source_note_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["target_note_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["source_note_id"]),
        Index(value = ["target_note_id"]),
        Index(value = ["source_note_id", "target_note_id"], unique = true)
    ]
)
data class NoteLinkEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "source_note_id")
    val sourceNoteId: String,

    @ColumnInfo(name = "target_note_id")
    val targetNoteId: String,

    @ColumnInfo(name = "link_text")
    val linkText: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
