package com.notex.sd.domain.usecase

import com.notex.sd.domain.model.Folder
import com.notex.sd.domain.model.Note
import com.notex.sd.domain.model.NoteColor
import com.notex.sd.domain.repository.FolderRepository
import com.notex.sd.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * Exportable note data structure.
 * Serializable for JSON export/import.
 */
@Serializable
data class ExportableNote(
    val id: String,
    val title: String,
    val content: String,
    val plainTextContent: String,
    val folderName: String? = null,
    val color: String,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val isChecklist: Boolean,
    val createdAt: Long,
    val modifiedAt: Long,
    val wordCount: Int,
    val characterCount: Int
)

/**
 * Exportable folder data structure.
 */
@Serializable
data class ExportableFolder(
    val id: String,
    val name: String,
    val color: String?,
    val parentFolderName: String?,
    val createdAt: Long
)

/**
 * Complete export data structure containing all notes and folders.
 */
@Serializable
data class NoteyExportData(
    val version: Int = EXPORT_VERSION,
    val exportedAt: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0",
    val notes: List<ExportableNote>,
    val folders: List<ExportableFolder>
) {
    companion object {
        const val EXPORT_VERSION = 1
    }
}

/**
 * Export format options.
 */
enum class ExportFormat {
    JSON,
    MARKDOWN,
    PLAIN_TEXT
}

/**
 * Export result containing the data and metadata.
 */
data class ExportResult(
    val success: Boolean,
    val format: ExportFormat,
    val noteCount: Int,
    val folderCount: Int,
    val errorMessage: String? = null
)

/**
 * Import result with details about what was imported.
 */
data class ImportResult(
    val success: Boolean,
    val notesImported: Int,
    val foldersImported: Int,
    val notesSkipped: Int,
    val errorMessage: String? = null
)

/**
 * Use case for exporting notes to various formats.
 */
class ExportNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository
) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    /**
     * Export all notes to JSON format.
     */
    suspend fun exportToJson(outputStream: OutputStream): ExportResult {
        return try {
            val notes = noteRepository.getAllNotes().first()
            val folders = folderRepository.getAllFolders().first()

            val folderMap = folders.associateBy { it.id }

            val exportableNotes = notes.map { note ->
                ExportableNote(
                    id = note.id,
                    title = note.title,
                    content = note.content,
                    plainTextContent = note.plainTextContent,
                    folderName = note.folderId?.let { folderMap[it]?.name },
                    color = note.color.name,
                    isPinned = note.isPinned,
                    isArchived = note.isArchived,
                    isChecklist = note.isChecklist,
                    createdAt = note.createdAt,
                    modifiedAt = note.modifiedAt,
                    wordCount = note.wordCount,
                    characterCount = note.characterCount
                )
            }

            val exportableFolders = folders.map { folder ->
                ExportableFolder(
                    id = folder.id,
                    name = folder.name,
                    color = folder.color,
                    parentFolderName = folder.parentId?.let { folderMap[it]?.name },
                    createdAt = folder.createdAt
                )
            }

            val exportData = NoteyExportData(
                notes = exportableNotes,
                folders = exportableFolders
            )

            val jsonString = json.encodeToString(exportData)
            outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
            outputStream.flush()

            ExportResult(
                success = true,
                format = ExportFormat.JSON,
                noteCount = notes.size,
                folderCount = folders.size
            )
        } catch (e: Exception) {
            ExportResult(
                success = false,
                format = ExportFormat.JSON,
                noteCount = 0,
                folderCount = 0,
                errorMessage = e.message ?: "Unknown error during export"
            )
        }
    }

    /**
     * Export all notes to Markdown files (combined into a single file).
     */
    suspend fun exportToMarkdown(outputStream: OutputStream): ExportResult {
        return try {
            val notes = noteRepository.getAllNotes().first()
            val folders = folderRepository.getAllFolders().first()
            val folderMap = folders.associateBy { it.id }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val markdown = buildString {
                appendLine("# Notey Export")
                appendLine()
                appendLine("Exported on: ${dateFormat.format(Date())}")
                appendLine("Total notes: ${notes.size}")
                appendLine()
                appendLine("---")
                appendLine()

                // Group notes by folder
                val notesByFolder = notes.groupBy { note ->
                    note.folderId?.let { folderMap[it]?.name } ?: "Uncategorized"
                }

                notesByFolder.forEach { (folderName, folderNotes) ->
                    appendLine("## $folderName")
                    appendLine()

                    folderNotes.forEach { note ->
                        appendLine("### ${note.title.ifEmpty { "Untitled" }}")
                        appendLine()

                        // Metadata
                        appendLine("> **Created:** ${dateFormat.format(Date(note.createdAt))}")
                        appendLine("> **Modified:** ${dateFormat.format(Date(note.modifiedAt))}")
                        if (note.isPinned) appendLine("> **📌 Pinned**")
                        if (note.isArchived) appendLine("> **📦 Archived**")
                        appendLine()

                        // Content
                        appendLine(note.content)
                        appendLine()
                        appendLine("---")
                        appendLine()
                    }
                }
            }

            outputStream.write(markdown.toByteArray(Charsets.UTF_8))
            outputStream.flush()

            ExportResult(
                success = true,
                format = ExportFormat.MARKDOWN,
                noteCount = notes.size,
                folderCount = folders.size
            )
        } catch (e: Exception) {
            ExportResult(
                success = false,
                format = ExportFormat.MARKDOWN,
                noteCount = 0,
                folderCount = 0,
                errorMessage = e.message ?: "Unknown error during export"
            )
        }
    }

    /**
     * Export notes to plain text format.
     */
    suspend fun exportToPlainText(outputStream: OutputStream): ExportResult {
        return try {
            val notes = noteRepository.getAllNotes().first()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val plainText = buildString {
                appendLine("NOTEY EXPORT")
                appendLine("=" .repeat(50))
                appendLine("Exported: ${dateFormat.format(Date())}")
                appendLine("Total notes: ${notes.size}")
                appendLine()

                notes.forEachIndexed { index, note ->
                    appendLine("=" .repeat(50))
                    appendLine("NOTE ${index + 1}")
                    appendLine("-" .repeat(50))
                    appendLine("Title: ${note.title.ifEmpty { "Untitled" }}")
                    appendLine("Created: ${dateFormat.format(Date(note.createdAt))}")
                    appendLine("Modified: ${dateFormat.format(Date(note.modifiedAt))}")
                    if (note.isPinned) appendLine("Status: Pinned")
                    if (note.isArchived) appendLine("Status: Archived")
                    appendLine("-" .repeat(50))
                    appendLine()
                    appendLine(note.plainTextContent)
                    appendLine()
                }
            }

            outputStream.write(plainText.toByteArray(Charsets.UTF_8))
            outputStream.flush()

            ExportResult(
                success = true,
                format = ExportFormat.PLAIN_TEXT,
                noteCount = notes.size,
                folderCount = 0
            )
        } catch (e: Exception) {
            ExportResult(
                success = false,
                format = ExportFormat.PLAIN_TEXT,
                noteCount = 0,
                folderCount = 0,
                errorMessage = e.message ?: "Unknown error during export"
            )
        }
    }

    /**
     * Generate a suggested filename for the export.
     */
    fun generateExportFilename(format: ExportFormat): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        return when (format) {
            ExportFormat.JSON -> "notey_backup_$timestamp.json"
            ExportFormat.MARKDOWN -> "notey_export_$timestamp.md"
            ExportFormat.PLAIN_TEXT -> "notey_export_$timestamp.txt"
        }
    }
}

/**
 * Use case for importing notes from exported data.
 */
class ImportNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Import notes from JSON backup file.
     */
    suspend fun importFromJson(jsonString: String): ImportResult {
        return try {
            val exportData = json.decodeFromString<NoteyExportData>(jsonString)

            // Validate version compatibility
            if (exportData.version > NoteyExportData.EXPORT_VERSION) {
                return ImportResult(
                    success = false,
                    notesImported = 0,
                    foldersImported = 0,
                    notesSkipped = 0,
                    errorMessage = "Export file version ${exportData.version} is not supported. Please update the app."
                )
            }

            var foldersImported = 0
            var notesImported = 0
            var notesSkipped = 0

            // Create folder name to ID mapping
            val existingFolders = folderRepository.getAllFolders().first()
            val folderNameToId = existingFolders.associateBy { it.name }.toMutableMap()

            // Import folders first
            exportData.folders.forEach { exportFolder ->
                if (!folderNameToId.containsKey(exportFolder.name)) {
                    val newFolder = Folder(
                        id = UUID.randomUUID().toString(),
                        name = exportFolder.name,
                        color = exportFolder.color,
                        parentId = exportFolder.parentFolderName?.let { folderNameToId[it]?.id },
                        createdAt = exportFolder.createdAt
                    )
                    folderRepository.insertFolder(newFolder)
                    folderNameToId[newFolder.name] = newFolder
                    foldersImported++
                }
            }

            // Import notes
            val existingNoteIds = noteRepository.getAllNotes().first().map { it.id }.toSet()

            exportData.notes.forEach { exportNote ->
                // Skip if note with same ID already exists
                if (existingNoteIds.contains(exportNote.id)) {
                    notesSkipped++
                    return@forEach
                }

                val folderId = exportNote.folderName?.let { folderNameToId[it]?.id }

                val note = Note(
                    id = UUID.randomUUID().toString(), // Generate new ID to avoid conflicts
                    title = exportNote.title,
                    content = exportNote.content,
                    plainTextContent = exportNote.plainTextContent,
                    folderId = folderId,
                    color = try {
                        NoteColor.valueOf(exportNote.color)
                    } catch (e: Exception) {
                        NoteColor.DEFAULT
                    },
                    isPinned = exportNote.isPinned,
                    isArchived = exportNote.isArchived,
                    isTrashed = false,
                    isChecklist = exportNote.isChecklist,
                    createdAt = exportNote.createdAt,
                    modifiedAt = exportNote.modifiedAt,
                    wordCount = exportNote.wordCount,
                    characterCount = exportNote.characterCount
                )

                noteRepository.insertNote(note)
                notesImported++
            }

            ImportResult(
                success = true,
                notesImported = notesImported,
                foldersImported = foldersImported,
                notesSkipped = notesSkipped
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                notesImported = 0,
                foldersImported = 0,
                notesSkipped = 0,
                errorMessage = e.message ?: "Failed to parse import file"
            )
        }
    }

    /**
     * Import notes from a Markdown file.
     * Parses the markdown structure to extract notes.
     */
    suspend fun importFromMarkdown(markdownContent: String): ImportResult {
        return try {
            var notesImported = 0
            val notes = mutableListOf<Note>()

            // Simple markdown parsing - split by ### headers
            val notePattern = Regex("""###\s+(.+?)(?=\n###|\n##|\Z)""", RegexOption.DOT_MATCHES_ALL)
            val matches = notePattern.findAll(markdownContent)

            matches.forEach { match ->
                val fullContent = match.value
                val lines = fullContent.lines()

                if (lines.isNotEmpty()) {
                    val title = lines.first().removePrefix("###").trim()
                    val content = lines.drop(1)
                        .dropWhile { it.startsWith(">") || it.isBlank() }
                        .joinToString("\n")
                        .trim()
                        .removeSuffix("---")
                        .trim()

                    if (title.isNotBlank() || content.isNotBlank()) {
                        notes.add(
                            Note.create(
                                title = title,
                                content = content
                            )
                        )
                    }
                }
            }

            notes.forEach { note ->
                noteRepository.insertNote(note)
                notesImported++
            }

            ImportResult(
                success = true,
                notesImported = notesImported,
                foldersImported = 0,
                notesSkipped = 0
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                notesImported = 0,
                foldersImported = 0,
                notesSkipped = 0,
                errorMessage = e.message ?: "Failed to parse markdown file"
            )
        }
    }
}

/**
 * Get the appropriate MIME type for export format.
 */
fun ExportFormat.getMimeType(): String {
    return when (this) {
        ExportFormat.JSON -> "application/json"
        ExportFormat.MARKDOWN -> "text/markdown"
        ExportFormat.PLAIN_TEXT -> "text/plain"
    }
}

/**
 * Get the file extension for export format.
 */
fun ExportFormat.getFileExtension(): String {
    return when (this) {
        ExportFormat.JSON -> "json"
        ExportFormat.MARKDOWN -> "md"
        ExportFormat.PLAIN_TEXT -> "txt"
    }
}
