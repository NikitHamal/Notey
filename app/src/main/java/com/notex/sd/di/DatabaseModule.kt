package com.notex.sd.di

import android.content.Context
import androidx.room.Room
import com.notex.sd.data.database.NoteXDatabase
import com.notex.sd.data.database.dao.ChecklistItemDao
import com.notex.sd.data.database.dao.FolderDao
import com.notex.sd.data.database.dao.NoteDao
import com.notex.sd.data.database.dao.NoteLinkDao
import com.notex.sd.data.database.migration.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): NoteXDatabase {
        return Room.databaseBuilder(
            context,
            NoteXDatabase::class.java,
            NoteXDatabase.DATABASE_NAME
        )
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            .build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: NoteXDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideFolderDao(database: NoteXDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    @Singleton
    fun provideChecklistItemDao(database: NoteXDatabase): ChecklistItemDao {
        return database.checklistItemDao()
    }

    @Provides
    @Singleton
    fun provideNoteLinkDao(database: NoteXDatabase): NoteLinkDao {
        return database.noteLinkDao()
    }
}
