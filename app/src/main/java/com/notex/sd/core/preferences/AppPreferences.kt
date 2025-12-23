package com.notex.sd.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_THEME_MODE = intPreferencesKey("theme_mode")
        private val KEY_DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        private val KEY_VIEW_MODE = intPreferencesKey("view_mode")
        private val KEY_SORT_ORDER = intPreferencesKey("sort_order")
        private val KEY_LAST_SELECTED_FOLDER = stringPreferencesKey("last_selected_folder")
        private val KEY_EDITOR_FONT_SIZE = intPreferencesKey("editor_font_size")
        private val KEY_AUTO_SAVE = booleanPreferencesKey("auto_save")
        private val KEY_SHOW_WORD_COUNT = booleanPreferencesKey("show_word_count")
    }

    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        ThemeMode.fromValue(preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.value)
    }

    val dynamicColors: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_DYNAMIC_COLORS] ?: true
    }

    val viewMode: Flow<ViewMode> = dataStore.data.map { preferences ->
        ViewMode.fromValue(preferences[KEY_VIEW_MODE] ?: ViewMode.GRID.value)
    }

    val sortOrder: Flow<SortOrder> = dataStore.data.map { preferences ->
        SortOrder.fromValue(preferences[KEY_SORT_ORDER] ?: SortOrder.MODIFIED_DESC.value)
    }

    val lastSelectedFolder: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_LAST_SELECTED_FOLDER]
    }

    val editorFontSize: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_EDITOR_FONT_SIZE] ?: 16
    }

    val autoSave: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_AUTO_SAVE] ?: true
    }

    val showWordCount: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_SHOW_WORD_COUNT] ?: true
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.value
        }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DYNAMIC_COLORS] = enabled
        }
    }

    suspend fun setViewMode(mode: ViewMode) {
        dataStore.edit { preferences ->
            preferences[KEY_VIEW_MODE] = mode.value
        }
    }

    suspend fun setSortOrder(order: SortOrder) {
        dataStore.edit { preferences ->
            preferences[KEY_SORT_ORDER] = order.value
        }
    }

    suspend fun setLastSelectedFolder(folderId: String?) {
        dataStore.edit { preferences ->
            if (folderId != null) {
                preferences[KEY_LAST_SELECTED_FOLDER] = folderId
            } else {
                preferences.remove(KEY_LAST_SELECTED_FOLDER)
            }
        }
    }

    suspend fun setEditorFontSize(size: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_EDITOR_FONT_SIZE] = size.coerceIn(12, 24)
        }
    }

    suspend fun setAutoSave(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_SAVE] = enabled
        }
    }

    suspend fun setShowWordCount(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SHOW_WORD_COUNT] = show
        }
    }
}

enum class ThemeMode(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromValue(value: Int): ThemeMode = entries.find { it.value == value } ?: SYSTEM
    }
}

enum class ViewMode(val value: Int) {
    GRID(0),
    LIST(1);

    companion object {
        fun fromValue(value: Int): ViewMode = entries.find { it.value == value } ?: GRID
    }
}

enum class SortOrder(val value: Int) {
    MODIFIED_DESC(0),
    MODIFIED_ASC(1),
    CREATED_DESC(2),
    CREATED_ASC(3),
    TITLE_ASC(4),
    TITLE_DESC(5);

    companion object {
        fun fromValue(value: Int): SortOrder = entries.find { it.value == value } ?: MODIFIED_DESC
    }
}
