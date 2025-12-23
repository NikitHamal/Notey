package com.notex.sd.di

import com.notex.sd.data.repository.FolderRepositoryImpl
import com.notex.sd.data.repository.NoteRepositoryImpl
import com.notex.sd.domain.repository.FolderRepository
import com.notex.sd.domain.repository.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository

    @Binds
    @Singleton
    abstract fun bindFolderRepository(
        impl: FolderRepositoryImpl
    ): FolderRepository
}
