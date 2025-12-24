package com.notex.sd.ui.screens.archive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notex.sd.R
import com.notex.sd.domain.model.Note
import com.notex.sd.ui.components.common.EmptyState
import com.notex.sd.ui.components.note.NoteCard
import com.notex.sd.ui.components.note.NoteCardLayout
import com.notex.sd.ui.components.note.NotesList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedNoteIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedNoteIds.isNotEmpty()

    // Clear selection when notes change
    LaunchedEffect(uiState.archivedNotes) {
        if (selectedNoteIds.isNotEmpty()) {
            selectedNoteIds = selectedNoteIds.filter { id ->
                uiState.archivedNotes.any { note -> note.id == id }
            }.toSet()
        }
    }

    // Show error message
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (isSelectionMode) {
                SelectionTopAppBar(
                    selectedCount = selectedNoteIds.size,
                    onClearSelection = { selectedNoteIds = emptySet() },
                    onUnarchiveAll = {
                        viewModel.unarchiveMultipleNotes(selectedNoteIds.toList())
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "${selectedNoteIds.size} note(s) unarchived",
                                actionLabel = "Undo"
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                // Re-archive the notes
                                selectedNoteIds.forEach { noteId ->
                                    // Note: Would need toggleArchive to re-archive
                                }
                            }
                        }
                        selectedNoteIds = emptySet()
                    },
                    onDeleteAll = {
                        viewModel.deleteMultipleNotes(selectedNoteIds.toList())
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "${selectedNoteIds.size} note(s) moved to trash",
                                actionLabel = "Undo"
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                // Restore notes from trash
                                // Note: Would need restore functionality
                            }
                        }
                        selectedNoteIds = emptySet()
                    },
                    scrollBehavior = scrollBehavior
                )
            } else {
                ArchiveTopAppBar(
                    scrollBehavior = scrollBehavior,
                    onNavigateBack = onNavigateBack
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.hasNotes) {
                NotesList(
                    notes = uiState.archivedNotes,
                    selectedNoteIds = selectedNoteIds,
                    layout = NoteCardLayout.GRID,
                    onNoteClick = { note ->
                        if (isSelectionMode) {
                            selectedNoteIds = if (selectedNoteIds.contains(note.id)) {
                                selectedNoteIds - note.id
                            } else {
                                selectedNoteIds + note.id
                            }
                        } else {
                            onNavigateToEditor(note.id)
                        }
                    },
                    onNoteLongClick = { note ->
                        selectedNoteIds = if (selectedNoteIds.contains(note.id)) {
                            selectedNoteIds - note.id
                        } else {
                            selectedNoteIds + note.id
                        }
                    },
                    emptyStateTitle = stringResource(R.string.archive_empty_title),
                    emptyStateSubtitle = stringResource(R.string.archive_empty_subtitle),
                    modifier = Modifier.fillMaxSize()
                )
            } else if (!uiState.isLoading) {
                EmptyState(
                    icon = Icons.Outlined.Archive,
                    title = stringResource(R.string.archive_empty_title),
                    subtitle = stringResource(R.string.archive_empty_subtitle),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LargeTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.archive_title),
                style = MaterialTheme.typography.headlineMedium
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back"
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopAppBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onUnarchiveAll: () -> Unit,
    onDeleteAll: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {
    LargeTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.selected, selectedCount),
                style = MaterialTheme.typography.headlineMedium
            )
        },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear selection"
                )
            }
        },
        actions = {
            IconButton(onClick = onUnarchiveAll) {
                Icon(
                    imageVector = Icons.Default.Unarchive,
                    contentDescription = "Unarchive all"
                )
            }
            IconButton(onClick = onDeleteAll) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete all"
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}
