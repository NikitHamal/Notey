package com.notex.sd.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Onboarding : Route

    @Serializable
    data object Home : Route

    @Serializable
    data class Editor(
        val noteId: String? = null,
        val folderId: String? = null,
        val isChecklist: Boolean = false
    ) : Route

    @Serializable
    data object Search : Route

    @Serializable
    data object Archive : Route

    @Serializable
    data object Trash : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data class Folder(val folderId: String) : Route
}
