package com.notex.sd.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notex.sd.domain.model.Note
import com.notex.sd.domain.usecase.SearchNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Note> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val hasQuery: Boolean
        get() = query.isNotBlank()

    val hasResults: Boolean
        get() = results.isNotEmpty()

    val isEmpty: Boolean
        get() = query.isBlank() && results.isEmpty()

    val resultCount: Int
        get() = results.size

    fun getResultSummary(): String {
        return when (resultCount) {
            0 -> if (hasQuery) "No results found" else ""
            1 -> "1 result"
            else -> "$resultCount results"
        }
    }
}

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchNotesUseCase: SearchNotesUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())

    private val maxRecentSearches = 10

    val uiState: StateFlow<SearchUiState> = _query
        .debounce(300) // Debounce for 300ms
        .distinctUntilChanged()
        .onEach { query ->
            if (query.isNotBlank()) {
                _isLoading.value = true
            }
        }
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                searchNotesUseCase(query)
                    .catch { throwable ->
                        _error.value = throwable.message ?: "Search failed"
                        emit(emptyList())
                    }
                    .onEach {
                        _isLoading.value = false
                    }
            }
        }
        .map { results ->
            SearchUiState(
                query = _query.value,
                results = results,
                recentSearches = _recentSearches.value,
                isLoading = _isLoading.value,
                error = _error.value
            )
        }
        .catch { throwable ->
            emit(
                SearchUiState(
                    query = _query.value,
                    isLoading = false,
                    error = throwable.message ?: "An error occurred"
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchUiState()
        )

    fun search(query: String) {
        _query.value = query

        // Add to recent searches if it's a valid query and has results
        if (query.isNotBlank() && query.length >= 2) {
            viewModelScope.launch {
                // Wait a bit to see if we have results
                kotlinx.coroutines.delay(500)
                if (uiState.value.hasResults) {
                    addToRecentSearches(query)
                }
            }
        }
    }

    fun clearSearch() {
        _query.value = ""
        _error.value = null
        _isLoading.value = false
    }

    fun selectRecentSearch(query: String) {
        search(query)
    }

    fun removeRecentSearch(query: String) {
        _recentSearches.value = _recentSearches.value.filter { it != query }
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    private fun addToRecentSearches(query: String) {
        val currentSearches = _recentSearches.value.toMutableList()

        // Remove if already exists
        currentSearches.remove(query)

        // Add to the beginning
        currentSearches.add(0, query)

        // Keep only the most recent searches
        if (currentSearches.size > maxRecentSearches) {
            currentSearches.removeAt(currentSearches.lastIndex)
        }

        _recentSearches.value = currentSearches
    }

    fun clearError() {
        _error.value = null
    }

    // Helper function to highlight search terms in results
    fun highlightQuery(text: String): List<Pair<String, Boolean>> {
        if (_query.value.isBlank()) {
            return listOf(text to false)
        }

        val query = _query.value.lowercase()
        val lowerText = text.lowercase()
        val result = mutableListOf<Pair<String, Boolean>>()

        var currentIndex = 0
        var matchIndex = lowerText.indexOf(query, currentIndex)

        while (matchIndex != -1) {
            // Add non-highlighted text before match
            if (matchIndex > currentIndex) {
                result.add(text.substring(currentIndex, matchIndex) to false)
            }

            // Add highlighted match
            result.add(text.substring(matchIndex, matchIndex + query.length) to true)

            currentIndex = matchIndex + query.length
            matchIndex = lowerText.indexOf(query, currentIndex)
        }

        // Add remaining text
        if (currentIndex < text.length) {
            result.add(text.substring(currentIndex) to false)
        }

        return result
    }
}
