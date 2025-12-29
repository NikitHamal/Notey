package com.notex.sd.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notex.sd.core.preferences.AppPreferences
import com.notex.sd.core.preferences.SortOrder
import com.notex.sd.core.preferences.ThemeMode
import com.notex.sd.core.preferences.ViewMode
import com.notex.sd.domain.model.Note
import com.notex.sd.domain.usecase.CreateNoteUseCase
import com.notex.sd.domain.usecase.ExportFormat
import com.notex.sd.domain.usecase.ExportNotesUseCase
import com.notex.sd.domain.usecase.GetAllNotesUseCase
import com.notex.sd.domain.usecase.ImportNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColors: Boolean = true,
    val viewMode: ViewMode = ViewMode.GRID,
    val sortOrder: SortOrder = SortOrder.MODIFIED_DESC,
    val editorFontSize: Int = 16,
    val autoSave: Boolean = true,
    val showWordCount: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val backupStatus: BackupStatus? = null
)

sealed class BackupStatus {
    data class Success(val filePath: String) : BackupStatus()
    data class Error(val message: String) : BackupStatus()
    data object InProgress : BackupStatus()
}

@Serializable
data class NoteBackup(
    val id: String,
    val title: String,
    val content: String,
    val plainTextContent: String,
    val folderId: String? = null,
    val color: String,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val isTrashed: Boolean,
    val isChecklist: Boolean,
    val createdAt: Long,
    val modifiedAt: Long,
    val trashedAt: Long? = null,
    val wordCount: Int,
    val characterCount: Int
)

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long,
    val notes: List<NoteBackup>
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
    private val exportNotesUseCase: ExportNotesUseCase,
    private val importNotesUseCase: ImportNotesUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _backupStatus = MutableStateFlow<BackupStatus?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            appPreferences.themeMode,
            appPreferences.dynamicColors,
            appPreferences.viewMode,
            appPreferences.sortOrder,
            appPreferences.editorFontSize
        ) { themeMode, dynamicColors, viewMode, sortOrder, fontSize ->
            SettingsPartial1(themeMode, dynamicColors, viewMode, sortOrder, fontSize)
        },
        combine(
            appPreferences.autoSave,
            appPreferences.showWordCount,
            _isLoading,
            _error,
            _backupStatus
        ) { autoSave, showWordCount, isLoading, error, backupStatus ->
            SettingsPartial2(autoSave, showWordCount, isLoading, error, backupStatus)
        }
    ) { partial1, partial2 ->
        SettingsUiState(
            themeMode = partial1.themeMode,
            dynamicColors = partial1.dynamicColors,
            viewMode = partial1.viewMode,
            sortOrder = partial1.sortOrder,
            editorFontSize = partial1.editorFontSize,
            autoSave = partial2.autoSave,
            showWordCount = partial2.showWordCount,
            isLoading = partial2.isLoading,
            error = partial2.error,
            backupStatus = partial2.backupStatus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    private data class SettingsPartial1(
        val themeMode: ThemeMode,
        val dynamicColors: Boolean,
        val viewMode: ViewMode,
        val sortOrder: SortOrder,
        val editorFontSize: Int
    )

    private data class SettingsPartial2(
        val autoSave: Boolean,
        val showWordCount: Boolean,
        val isLoading: Boolean,
        val error: String?,
        val backupStatus: BackupStatus?
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                appPreferences.setThemeMode(mode)
            } catch (e: Exception) {
                _error.value = "Failed to update theme mode: ${e.message}"
            }
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            try {
                appPreferences.setDynamicColors(enabled)
            } catch (e: Exception) {
                _error.value = "Failed to update dynamic colors: ${e.message}"
            }
        }
    }

    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch {
            try {
                appPreferences.setViewMode(mode)
            } catch (e: Exception) {
                _error.value = "Failed to update view mode: ${e.message}"
            }
        }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            try {
                appPreferences.setSortOrder(order)
            } catch (e: Exception) {
                _error.value = "Failed to update sort order: ${e.message}"
            }
        }
    }

    fun setFontSize(size: Int) {
        viewModelScope.launch {
            try {
                appPreferences.setEditorFontSize(size)
            } catch (e: Exception) {
                _error.value = "Failed to update font size: ${e.message}"
            }
        }
    }

    fun setAutoSave(enabled: Boolean) {
        viewModelScope.launch {
            try {
                appPreferences.setAutoSave(enabled)
            } catch (e: Exception) {
                _error.value = "Failed to update auto-save: ${e.message}"
            }
        }
    }

    fun setShowWordCount(show: Boolean) {
        viewModelScope.launch {
            try {
                appPreferences.setShowWordCount(show)
            } catch (e: Exception) {
                _error.value = "Failed to update word count display: ${e.message}"
            }
        }
    }

    fun backupNotes() {
        viewModelScope.launch {
            try {
                _backupStatus.value = BackupStatus.InProgress
                _isLoading.value = true

                // Get all notes
                val notes = getAllNotesUseCase(SortOrder.MODIFIED_DESC).first()

                // Convert to backup format
                val backupNotes = notes.map { note ->
                    NoteBackup(
                        id = note.id,
                        title = note.title,
                        content = note.content,
                        plainTextContent = note.plainTextContent,
                        folderId = note.folderId,
                        color = note.color.name,
                        isPinned = note.isPinned,
                        isArchived = note.isArchived,
                        isTrashed = note.isTrashed,
                        isChecklist = note.isChecklist,
                        createdAt = note.createdAt,
                        modifiedAt = note.modifiedAt,
                        trashedAt = note.trashedAt,
                        wordCount = note.wordCount,
                        characterCount = note.characterCount
                    )
                }

                val backupData = BackupData(
                    exportedAt = System.currentTimeMillis(),
                    notes = backupNotes
                )

                // Serialize to JSON
                val json = Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                }
                val jsonString = json.encodeToString(backupData)

                // Save to file
                val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                val timestamp = dateFormat.format(Date())
                val fileName = "notex_backup_$timestamp.json"

                val downloadsDir = context.getExternalFilesDir(null)
                    ?: context.filesDir

                val backupFile = File(downloadsDir, fileName)
                FileOutputStream(backupFile).use { output ->
                    output.write(jsonString.toByteArray())
                }

                _backupStatus.value = BackupStatus.Success(backupFile.absolutePath)
                _isLoading.value = false
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.Error(e.message ?: "Failed to backup notes")
                _error.value = "Failed to backup notes: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun restoreNotes(jsonContent: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Parse JSON
                val json = Json {
                    ignoreUnknownKeys = true
                }
                val backupData = json.decodeFromString<BackupData>(jsonContent)

                // Restore notes
                var restoredCount = 0
                backupData.notes.forEach { backupNote ->
                    try {
                        // Convert color string to NoteColor enum
                        val color = try {
                            com.notex.sd.domain.model.NoteColor.valueOf(backupNote.color)
                        } catch (e: Exception) {
                            com.notex.sd.domain.model.NoteColor.DEFAULT
                        }

                        createNoteUseCase(
                            title = backupNote.title,
                            content = backupNote.content,
                            folderId = backupNote.folderId,
                            color = color,
                            isChecklist = backupNote.isChecklist
                        )
                        restoredCount++
                    } catch (e: Exception) {
                        // Continue with other notes even if one fails
                    }
                }

                _backupStatus.value = BackupStatus.Success("Restored $restoredCount notes")
                _isLoading.value = false
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.Error(e.message ?: "Failed to restore notes")
                _error.value = "Failed to restore notes: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun exportNotes(format: ExportFormat) {
        viewModelScope.launch {
            try {
                _backupStatus.value = BackupStatus.InProgress
                _isLoading.value = true

                val fileName = exportNotesUseCase.generateExportFilename(format)
                val downloadsDir = context.getExternalFilesDir(null) ?: context.filesDir
                val exportFile = File(downloadsDir, fileName)

                FileOutputStream(exportFile).use { outputStream ->
                    val result = when (format) {
                        ExportFormat.JSON -> exportNotesUseCase.exportToJson(outputStream)
                        ExportFormat.MARKDOWN -> exportNotesUseCase.exportToMarkdown(outputStream)
                        ExportFormat.PLAIN_TEXT -> exportNotesUseCase.exportToPlainText(outputStream)
                    }

                    if (result.success) {
                        _backupStatus.value = BackupStatus.Success(
                            "Exported ${result.noteCount} notes to ${exportFile.absolutePath}"
                        )
                    } else {
                        _backupStatus.value = BackupStatus.Error(
                            result.errorMessage ?: "Export failed"
                        )
                    }
                }

                _isLoading.value = false
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.Error(e.message ?: "Failed to export notes")
                _error.value = "Failed to export notes: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearBackupStatus() {
        _backupStatus.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
