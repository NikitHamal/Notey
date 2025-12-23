package com.notex.sd.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.notex.sd.data.database.dao.ChecklistItemDao
import com.notex.sd.data.database.dao.FolderDao
import com.notex.sd.data.database.dao.NoteDao
import com.notex.sd.data.database.entity.ChecklistItemEntity
import com.notex.sd.data.database.entity.FolderEntity
import com.notex.sd.data.database.entity.NoteEntity

@Database(
    entities = [
        NoteEntity::class,
        FolderEntity::class,
        ChecklistItemEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class NoteXDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun checklistItemDao(): ChecklistItemDao

    companion object {
        const val DATABASE_NAME = "notex_database"
    }
}
