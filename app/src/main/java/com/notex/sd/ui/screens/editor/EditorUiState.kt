package com.notex.sd.ui.screens.editor

import com.notex.sd.domain.model.ChecklistItem
import com.notex.sd.domain.model.NoteColor

data class EditorUiState(
    val noteId: String? = null,
    val title: String = "",
    val content: String = "",
    val checklistItems: List<ChecklistItem> = emptyList(),
    val color: NoteColor = NoteColor.DEFAULT,
    val folderId: String? = null,
    val isChecklist: Boolean = false,
    val isLoading: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val createdAt: Long? = null,
    val modifiedAt: Long? = null,
    val error: String? = null,
    val isSaving: Boolean = false,
    val lastSavedAt: Long? = null
) {
    val isNewNote: Boolean
        get() = noteId == null

    val isEmpty: Boolean
        get() = title.isBlank() && content.isBlank() && checklistItems.isEmpty()

    val hasContent: Boolean
        get() = !isEmpty

    val formattedWordCount: String
        get() = when {
            wordCount == 0 -> "No words"
            wordCount == 1 -> "1 word"
            else -> "$wordCount words"
        }

    val formattedCharacterCount: String
        get() = when {
            characterCount == 0 -> "No characters"
            characterCount == 1 -> "1 character"
            else -> "$characterCount characters"
        }

    fun withTitleAndContent(newTitle: String, newContent: String): EditorUiState {
        val plainText = extractPlainText(newContent)
        return copy(
            title = newTitle,
            content = newContent,
            wordCount = countWords(plainText),
            characterCount = plainText.length,
            hasUnsavedChanges = true
        )
    }

    fun withChecklistItems(items: List<ChecklistItem>): EditorUiState {
        return copy(
            checklistItems = items,
            hasUnsavedChanges = true
        )
    }

    fun withColor(newColor: NoteColor): EditorUiState {
        return copy(
            color = newColor,
            hasUnsavedChanges = true
        )
    }

    fun markAsSaved(): EditorUiState {
        return copy(
            hasUnsavedChanges = false,
            isSaving = false,
            lastSavedAt = System.currentTimeMillis()
        )
    }

    fun markAsSaving(): EditorUiState {
        return copy(isSaving = true)
    }

    companion object {
        private fun extractPlainText(content: String): String {
            return content
                .replace(Regex("<[^>]*>"), "")
                .replace(Regex("\\*\\*|__|\\*|_|~~|`|#|>|\\[|\\]|\\(|\\)"), "")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        private fun countWords(text: String): Int {
            if (text.isBlank()) return 0
            return text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        }
    }
}
