package com.notex.sd.domain.model

import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val plainTextContent: String = "",
    val folderId: String? = null,
    val color: NoteColor = NoteColor.DEFAULT,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isTrashed: Boolean = false,
    val isChecklist: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val trashedAt: Long? = null,
    val wordCount: Int = 0,
    val characterCount: Int = 0
) {
    val isNotEmpty: Boolean
        get() = title.isNotBlank() || content.isNotBlank() || plainTextContent.isNotBlank()

    val preview: String
        get() = plainTextContent.take(200).replace("\n", " ")

    companion object {
        fun create(
            title: String = "",
            content: String = "",
            folderId: String? = null,
            color: NoteColor = NoteColor.DEFAULT,
            isChecklist: Boolean = false
        ): Note {
            val now = System.currentTimeMillis()
            val plainText = extractPlainText(content)
            return Note(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                plainTextContent = plainText,
                folderId = folderId,
                color = color,
                isChecklist = isChecklist,
                createdAt = now,
                modifiedAt = now,
                wordCount = countWords(plainText),
                characterCount = plainText.length
            )
        }

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

    fun updateContent(newTitle: String, newContent: String): Note {
        val plainText = extractPlainText(newContent)
        return copy(
            title = newTitle,
            content = newContent,
            plainTextContent = plainText,
            modifiedAt = System.currentTimeMillis(),
            wordCount = countWords(plainText),
            characterCount = plainText.length
        )
    }

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

enum class NoteColor {
    DEFAULT,
    YELLOW,
    GREEN,
    BLUE,
    PINK,
    PURPLE,
    ORANGE,
    TEAL,
    GRAY
}
