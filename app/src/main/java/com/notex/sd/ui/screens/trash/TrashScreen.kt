package com.notex.sd.ui.screens.trash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notex.sd.R
import com.notex.sd.ui.components.common.EmptyState
import com.notex.sd.ui.components.dialog.ConfirmationDialog
import com.notex.sd.ui.components.note.NoteCardLayout
import com.notex.sd.ui.components.note.NotesList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedNoteIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedNoteIds.isNotEmpty()

    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var showDeletePermanentlyDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<String?>(null) }

    // Clear selection when notes change
    LaunchedEffect(uiState.trashedNotes) {
        if (selectedNoteIds.isNotEmpty()) {
            selectedNoteIds = selectedNoteIds.filter { id ->
                uiState.trashedNotes.any { note -> note.id == id }
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

    // Empty trash confirmation dialog
    if (showEmptyTrashDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.trash_empty_confirm_title),
            message = stringResource(R.string.trash_empty_confirm_message),
            confirmButtonText = stringResource(R.string.delete),
            dismissButtonText = stringResource(R.string.cancel),
            onConfirm = {
                viewModel.emptyTrash()
                scope.launch {
                    snackbarHostState.showSnackbar("Trash emptied")
                }
            },
            onDismiss = { showEmptyTrashDialog = false }
        )
    }

    // Delete permanently confirmation dialog
    if (showDeletePermanentlyDialog) {
        val count = if (noteToDelete != null) 1 else selectedNoteIds.size
        ConfirmationDialog(
            title = "Delete permanently?",
            message = if (count == 1) {
                "This note will be permanently deleted. This action cannot be undone."
            } else {
                "$count notes will be permanently deleted. This action cannot be undone."
            },
            confirmButtonText = stringResource(R.string.action_delete_forever),
            dismissButtonText = stringResource(R.string.cancel),
            onConfirm = {
                if (noteToDelete != null) {
                    viewModel.deleteNotePermanently(noteToDelete!!)
                    noteToDelete = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Note permanently deleted")
                    }
                } else {
                    viewModel.deleteMultipleNotesPermanently(selectedNoteIds.toList())
                    scope.launch {
                        snackbarHostState.showSnackbar("$count note(s) permanently deleted")
                    }
                    selectedNoteIds = emptySet()
                }
            },
            onDismiss = {
                showDeletePermanentlyDialog = false
                noteToDelete = null
            }
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (isSelectionMode) {
                SelectionTopAppBar(
                    selectedCount = selectedNoteIds.size,
                    onClearSelection = { selectedNoteIds = emptySet() },
                    onRestoreAll = {
                        viewModel.restoreMultipleNotes(selectedNoteIds.toList())
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "${selectedNoteIds.size} note(s) restored",
                                actionLabel = "Undo"
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                // Move notes back to trash
                                // Note: Would need moveToTrash functionality
                            }
                        }
                        selectedNoteIds = emptySet()
                    },
                    onDeleteAll = {
                        showDeletePermanentlyDialog = true
                    },
                    scrollBehavior = scrollBehavior
                )
            } else {
                TrashTopAppBar(
                    scrollBehavior = scrollBehavior,
                    onNavigateBack = onNavigateBack,
                    onEmptyTrash = { showEmptyTrashDialog = true },
                    hasNotes = uiState.hasNotes
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Auto-delete info banner
            if (uiState.hasNotes) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    tonalElevation = 0.dp
                ) {
                    Text(
                        text = stringResource(R.string.trash_auto_delete),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }

            // Notes list or empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (uiState.hasNotes) {
                    NotesList(
                        notes = uiState.trashedNotes,
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
                                // In trash, clicking doesn't open editor
                                // Start selection mode instead
                                selectedNoteIds = setOf(note.id)
                            }
                        },
                        onNoteLongClick = { note ->
                            selectedNoteIds = if (selectedNoteIds.contains(note.id)) {
                                selectedNoteIds - note.id
                            } else {
                                selectedNoteIds + note.id
                            }
                        },
                        emptyStateTitle = stringResource(R.string.trash_empty_title),
                        emptyStateSubtitle = stringResource(R.string.trash_empty_subtitle),
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!uiState.isLoading) {
                    EmptyState(
                        icon = Icons.Outlined.Delete,
                        title = stringResource(R.string.trash_empty_title),
                        subtitle = stringResource(R.string.trash_empty_subtitle),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrashTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateBack: () -> Unit,
    onEmptyTrash: () -> Unit,
    hasNotes: Boolean,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    LargeTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.trash_title),
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
        actions = {
            if (hasNotes) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.trash_empty_all)) },
                            onClick = {
                                showMenu = false
                                onEmptyTrash()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
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
    onRestoreAll: () -> Unit,
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
            IconButton(onClick = onRestoreAll) {
                Icon(
                    imageVector = Icons.Default.RestoreFromTrash,
                    contentDescription = "Restore all"
                )
            }
            IconButton(onClick = onDeleteAll) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete permanently"
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
