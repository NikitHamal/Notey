package com.notex.sd.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notex.sd.R
import com.notex.sd.domain.model.Folder
import com.notex.sd.domain.model.Note
import com.notex.sd.ui.components.common.EmptyState
import com.notex.sd.ui.components.note.NoteCard
import com.notex.sd.ui.components.note.NoteCardLayout
import com.notex.sd.ui.components.search.DateRange
import com.notex.sd.ui.components.search.ExpandedFilterPanel
import com.notex.sd.ui.components.search.SearchFilterChips
import com.notex.sd.ui.components.search.SearchFilters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (String) -> Unit,
    folders: List<Folder> = emptyList(),
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Filter state
    var searchFilters by rememberSaveable { mutableStateOf(SearchFilters()) }
    var showFilterPanel by rememberSaveable { mutableStateOf(false) }
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Auto-focus search field
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Apply filters to results
    val filteredResults = remember(uiState.results, searchFilters) {
        applyFilters(uiState.results, searchFilters)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            SearchTopAppBar(
                query = uiState.query,
                onQueryChange = { viewModel.search(it) },
                onNavigateBack = onNavigateBack,
                onClearQuery = { viewModel.clearSearch() },
                focusRequester = focusRequester
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips bar
            SearchFilterChips(
                filters = searchFilters,
                onFiltersChange = { searchFilters = it },
                onShowAllFilters = { showFilterPanel = true }
            )

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.hasQuery) {
                    // Show search results
                    if (filteredResults.isNotEmpty()) {
                        SearchResults(
                            notes = filteredResults,
                            query = uiState.query,
                            onNoteClick = { note ->
                                keyboardController?.hide()
                                onNavigateToEditor(note.id)
                            },
                            viewModel = viewModel
                        )
                    } else if (!uiState.isLoading) {
                        // No results found
                        EmptyState(
                            icon = Icons.Outlined.Search,
                            title = if (searchFilters.hasActiveFilters) {
                                "No matching notes"
                            } else {
                                stringResource(R.string.search_no_results)
                            },
                            subtitle = if (searchFilters.hasActiveFilters) {
                                "Try adjusting your filters"
                            } else {
                                "Try different keywords"
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // Show recent searches
                    RecentSearches(
                        recentSearches = uiState.recentSearches,
                        onSearchClick = { query ->
                            viewModel.selectRecentSearch(query)
                        },
                        onRemoveSearch = { query ->
                            viewModel.removeRecentSearch(query)
                        },
                        onClearAll = {
                            viewModel.clearRecentSearches()
                        }
                    )
                }
            }
        }
    }

    // Filter bottom sheet
    if (showFilterPanel) {
        ModalBottomSheet(
            onDismissRequest = { showFilterPanel = false },
            sheetState = filterSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ExpandedFilterPanel(
                filters = searchFilters,
                folders = folders,
                onFiltersChange = { searchFilters = it },
                onDismiss = { showFilterPanel = false }
            )
        }
    }
}

/**
 * Applies search filters to the list of notes.
 */
private fun applyFilters(notes: List<Note>, filters: SearchFilters): List<Note> {
    if (!filters.hasActiveFilters) return notes

    return notes.filter { note ->
        // Date range filter
        val passesDateFilter = when (filters.dateRange) {
            DateRange.ALL -> true
            else -> {
                filters.dateRange.daysAgo?.let { daysAgo ->
                    val cutoffTime = System.currentTimeMillis() - (daysAgo * 24 * 60 * 60 * 1000L)
                    note.modifiedAt >= cutoffTime
                } ?: true
            }
        }

        // Color filter
        val passesColorFilter = filters.colors.isEmpty() || filters.colors.contains(note.color)

        // Folder filter
        val passesFolderFilter = filters.folders.isEmpty() ||
                (note.folderId != null && filters.folders.contains(note.folderId))

        // Archived filter
        val passesArchivedFilter = filters.includeArchived || !note.isArchived

        // Links filter
        val passesLinksFilter = when (filters.hasLinks) {
            true -> note.content.contains("[[") && note.content.contains("]]")
            false -> !note.content.contains("[[")
            null -> true
        }

        passesDateFilter && passesColorFilter && passesFolderFilter &&
                passesArchivedFilter && passesLinksFilter
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onClearQuery: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_hint),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = query.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = onClearQuery) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        // Search is already happening via real-time update
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}

@Composable
private fun SearchResults(
    notes: List<Note>,
    query: String,
    onNoteClick: (Note) -> Unit,
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Results count
        item {
            Text(
                text = "${notes.size} result${if (notes.size == 1) "" else "s"}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Search results
        items(
            items = notes,
            key = { it.id }
        ) { note ->
            SearchResultCard(
                note = note,
                query = query,
                onClick = { onNoteClick(note) },
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun SearchResultCard(
    note: Note,
    query: String,
    onClick: () -> Unit,
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title with highlighting
            if (note.title.isNotBlank()) {
                val titleParts = viewModel.highlightQuery(note.title)
                Text(
                    text = buildAnnotatedString {
                        titleParts.forEach { (text, isHighlight) ->
                            if (isHighlight) {
                                withStyle(
                                    style = SpanStyle(
                                        background = MaterialTheme.colorScheme.primaryContainer,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(text)
                                }
                            } else {
                                append(text)
                            }
                        }
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Content with highlighting
            if (note.plainTextContent.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                val contentParts = viewModel.highlightQuery(note.preview)
                Text(
                    text = buildAnnotatedString {
                        contentParts.forEach { (text, isHighlight) ->
                            if (isHighlight) {
                                withStyle(
                                    style = SpanStyle(
                                        background = MaterialTheme.colorScheme.primaryContainer,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append(text)
                                }
                            } else {
                                append(text)
                            }
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RecentSearches(
    recentSearches: List<String>,
    onSearchClick: (String) -> Unit,
    onRemoveSearch: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (recentSearches.isEmpty()) {
        // Empty state for no recent searches
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Search your notes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Find notes by title, content, or tags",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.search_recent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Clear all",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onClearAll)
                    )
                }
            }

            // Recent searches list
            items(
                items = recentSearches,
                key = { it }
            ) { search ->
                RecentSearchItem(
                    query = search,
                    onClick = { onSearchClick(search) },
                    onRemove = { onRemoveSearch(search) }
                )
            }
        }
    }
}

@Composable
private fun RecentSearchItem(
    query: String,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = query,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
