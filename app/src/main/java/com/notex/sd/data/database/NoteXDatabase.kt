package com.notex.sd.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.notex.sd.data.database.dao.ChecklistItemDao
import com.notex.sd.data.database.dao.FolderDao
import com.notex.sd.data.database.dao.NoteDao
import com.notex.sd.data.database.dao.NoteLinkDao
import com.notex.sd.data.database.entity.ChecklistItemEntity
import com.notex.sd.data.database.entity.FolderEntity
import com.notex.sd.data.database.entity.NoteEntity
import com.notex.sd.data.database.entity.NoteLinkEntity

/**
 * Main database class for the Notey app.
 *
 * Uses manual migrations for production reliability. Schema is exported
 * for migration testing and validation.
 *
 * Version History:
 * - v1: Initial schema (notes, folders, checklist_items)
 * - v2: Added note_links table for wiki-style linking
 */
@Database(
    entities = [
        NoteEntity::class,
        FolderEntity::class,
        ChecklistItemEntity::class,
        NoteLinkEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class NoteXDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun checklistItemDao(): ChecklistItemDao
    abstract fun noteLinkDao(): NoteLinkDao

    companion object {
        const val DATABASE_NAME = "notex_database"
    }
}
