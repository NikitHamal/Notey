package com.notex.sd.domain.model

import java.util.UUID

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val noteId: String,
    val text: String,
    val isChecked: Boolean = false,
    val position: Int = 0,
    val indentation: Int = 0
) {
    companion object {
        fun create(
            noteId: String,
            text: String,
            position: Int = 0,
            indentation: Int = 0
        ): ChecklistItem {
            return ChecklistItem(
                id = UUID.randomUUID().toString(),
                noteId = noteId,
                text = text,
                isChecked = false,
                position = position,
                indentation = indentation
            )
        }
    }
}
