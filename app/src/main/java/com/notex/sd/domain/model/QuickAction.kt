package com.notex.sd.domain.model

/**
 * Quick actions for rapid note creation and common operations.
 * These appear in the quick action bar for fast access.
 */
sealed class QuickAction(
    val id: String,
    val label: String,
    val description: String
) {
    /**
     * Create a new blank note immediately.
     */
    data object NewNote : QuickAction(
        id = "quick_new_note",
        label = "New Note",
        description = "Create a blank note"
    )

    /**
     * Create a new checklist note.
     */
    data object NewChecklist : QuickAction(
        id = "quick_new_checklist",
        label = "Checklist",
        description = "Create a new checklist"
    )

    /**
     * Voice note (future feature placeholder).
     */
    data object VoiceNote : QuickAction(
        id = "quick_voice",
        label = "Voice",
        description = "Record a voice note"
    )

    /**
     * Image note (future feature placeholder).
     */
    data object ImageNote : QuickAction(
        id = "quick_image",
        label = "Image",
        description = "Add an image note"
    )

    /**
     * Open templates selection.
     */
    data object Templates : QuickAction(
        id = "quick_templates",
        label = "Templates",
        description = "Start from a template"
    )

    /**
     * Open search.
     */
    data object Search : QuickAction(
        id = "quick_search",
        label = "Search",
        description = "Search all notes"
    )

    companion object {
        /**
         * Primary quick actions shown in the main quick action bar.
         */
        val primaryActions = listOf(
            NewNote,
            NewChecklist,
            Templates
        )

        /**
         * All available quick actions.
         */
        val allActions = listOf(
            NewNote,
            NewChecklist,
            VoiceNote,
            ImageNote,
            Templates,
            Search
        )
    }
}

/**
 * Recent action for quick access to recently used features.
 */
data class RecentAction(
    val actionId: String,
    val lastUsedAt: Long,
    val useCount: Int
)
