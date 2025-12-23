package com.notex.sd.ui.screens.folder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notex.sd.core.preferences.SortOrder
import com.notex.sd.core.preferences.ViewMode
import com.notex.sd.domain.model.Note
import com.notex.sd.domain.model.NoteColor
import com.notex.sd.ui.components.common.EmptyState
import com.notex.sd.ui.components.common.LoadingIndicator
import com.notex.sd.ui.components.dialog.ColorPickerDialog
import com.notex.sd.ui.components.dialog.ConfirmationDialog
import com.notex.sd.ui.components.dialog.FolderDialog
import com.notex.sd.ui.components.dialog.FolderDialogMode
import com.notex.sd.ui.components.note.NoteCardLayout
import com.notex.sd.ui.components.note.NotesList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    onNavigateBack: () -> Unit,
    onSearchClick: () -> Unit,
    onNoteClick: (String) -> Unit,
    onCreateNoteClick: (String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FolderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showMoreMenu by rememberSaveable { mutableStateOf(false) }
    var showSortMenu by rememberSaveable { mutableStateOf(false) }
    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showColorDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showNoteActionsSheet by rememberSaveable { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var selectedNoteIds by rememberSaveable { mutableStateOf(emptySet<String>()) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Show snackbar on error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = uiState.folderName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                    // Search button
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }

                    // View mode toggle
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = when (uiState.viewMode) {
                                ViewMode.GRID -> Icons.Default.ViewList
                                ViewMode.LIST -> Icons.Default.GridView
                            },
                            contentDescription = "Toggle view mode"
                        )
                    }

                    // Sort menu button
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort"
                            )
                        }
                        SortDropdownMenu(
                            expanded = showSortMenu,
                            currentSortOrder = uiState.sortOrder,
                            onDismiss = { showSortMenu = false },
                            onSortOrderSelected = { sortOrder ->
                                viewModel.updateSortOrder(sortOrder)
                                showSortMenu = false
                            }
                        )
                    }

                    // More options menu
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        MoreOptionsMenu(
                            expanded = showMoreMenu,
                            onDismiss = { showMoreMenu = false },
                            onRenameClick = {
                                showRenameDialog = true
                                showMoreMenu = false
                            },
                            onColorClick = {
                                showColorDialog = true
                                showMoreMenu = false
                            },
                            onDeleteClick = {
                                showDeleteDialog = true
                                showMoreMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onCreateNoteClick(uiState.folder?.id)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create note"
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        FolderContent(
            uiState = uiState,
            selectedNoteIds = selectedNoteIds,
            paddingValues = paddingValues,
            onNoteClick = { note ->
                if (selectedNoteIds.isEmpty()) {
                    onNoteClick(note.id)
                } else {
                    selectedNoteIds = if (selectedNoteIds.contains(note.id)) {
                        selectedNoteIds - note.id
                    } else {
                        selectedNoteIds + note.id
                    }
                }
            },
            onNoteLongClick = { note ->
                selectedNote = note
                showNoteActionsSheet = true
            }
        )
    }

    // Rename folder dialog
    if (showRenameDialog) {
        FolderDialog(
            mode = FolderDialogMode.RENAME,
            currentName = uiState.folderName,
            onConfirm = { newName ->
                uiState.folder?.let { folder ->
                    viewModel.renameFolder(folder.id, newName)
                    scope.launch {
                        snackbarHostState.showSnackbar("Folder renamed")
                    }
                }
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    // Change color dialog
    if (showColorDialog) {
        ColorPickerDialog(
            currentColor = NoteColor.DEFAULT, // Using note color for folder for now
            onColorSelected = { color ->
                uiState.folder?.let { folder ->
                    // Note: You may need to add a method to convert NoteColor to Int
                    // or handle folder colors differently
                    viewModel.changeFolderColor(folder.id, color.ordinal)
                    scope.launch {
                        snackbarHostState.showSnackbar("Folder color changed")
                    }
                }
            },
            onDismiss = { showColorDialog = false }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Folder",
            message = "Are you sure you want to delete this folder? All notes in this folder will be moved to trash.",
            confirmButtonText = "Delete",
            dismissButtonText = "Cancel",
            icon = Icons.Default.Delete,
            onConfirm = {
                uiState.folder?.let { folder ->
                    viewModel.deleteFolder(folder.id)
                    scope.launch {
                        snackbarHostState.showSnackbar("Folder deleted")
                    }
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Note actions bottom sheet
    if (showNoteActionsSheet && selectedNote != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showNoteActionsSheet = false
                selectedNote = null
            },
            sheetState = bottomSheetState
        ) {
            NoteActionsBottomSheet(
                note = selectedNote!!,
                onPinClick = {
                    viewModel.togglePin(selectedNote!!.id, selectedNote!!.isPinned)
                    showNoteActionsSheet = false
                    selectedNote = null
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (selectedNote!!.isPinned) "Note unpinned" else "Note pinned"
                        )
                    }
                },
                onArchiveClick = {
                    viewModel.archiveNote(selectedNote!!.id, selectedNote!!.isArchived)
                    showNoteActionsSheet = false
                    selectedNote = null
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (selectedNote!!.isArchived) "Note unarchived" else "Note archived"
                        )
                    }
                },
                onColorClick = {
                    // You can implement color picker for individual note here
                    showNoteActionsSheet = false
                },
                onMoveClick = {
                    // You can implement move to folder picker here
                    showNoteActionsSheet = false
                },
                onDeleteClick = {
                    viewModel.moveToTrash(selectedNote!!.id)
                    showNoteActionsSheet = false
                    selectedNote = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Note moved to trash")
                    }
                },
                onDismiss = {
                    showNoteActionsSheet = false
                    selectedNote = null
                }
            )
        }
    }
}

@Composable
private fun FolderContent(
    uiState: FolderUiState,
    selectedNoteIds: Set<String>,
    paddingValues: PaddingValues,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when {
            uiState.isLoading -> {
                LoadingIndicator()
            }
            uiState.folder == null -> {
                EmptyState(
                    icon = Icons.Outlined.FolderOpen,
                    title = "Folder not found",
                    subtitle = "This folder may have been deleted"
                )
            }
            !uiState.hasNotes -> {
                EmptyState(
                    icon = Icons.Outlined.FolderOpen,
                    title = "No notes in this folder",
                    subtitle = "Tap the + button to create a note in this folder"
                )
            }
            else -> {
                NotesList(
                    notes = uiState.notes,
                    selectedNoteIds = selectedNoteIds,
                    layout = when (uiState.viewMode) {
                        ViewMode.GRID -> NoteCardLayout.GRID
                        ViewMode.LIST -> NoteCardLayout.LIST
                    },
                    onNoteClick = onNoteClick,
                    onNoteLongClick = onNoteLongClick,
                    emptyStateTitle = "No notes in this folder",
                    emptyStateSubtitle = "Tap the + button to create a note in this folder"
                )
            }
        }
    }
}

@Composable
private fun MoreOptionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onRenameClick: () -> Unit,
    onColorClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = { Text("Rename folder") },
            onClick = onRenameClick,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            text = { Text("Change color") },
            onClick = onColorClick,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            text = { Text("Delete folder") },
            onClick = onDeleteClick,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}

@Composable
private fun SortDropdownMenu(
    expanded: Boolean,
    currentSortOrder: SortOrder,
    onDismiss: () -> Unit,
    onSortOrderSelected: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        val sortOptions = listOf(
            SortOrder.MODIFIED_DESC to "Last modified",
            SortOrder.MODIFIED_ASC to "First modified",
            SortOrder.CREATED_DESC to "Newest first",
            SortOrder.CREATED_ASC to "Oldest first",
            SortOrder.TITLE_ASC to "Title (A-Z)",
            SortOrder.TITLE_DESC to "Title (Z-A)"
        )

        sortOptions.forEach { (sortOrder, label) ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = label,
                        style = if (currentSortOrder == sortOrder) {
                            MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            MaterialTheme.typography.bodyMedium
                        }
                    )
                },
                onClick = { onSortOrderSelected(sortOrder) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteActionsBottomSheet(
    note: Note,
    onPinClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onColorClick: () -> Unit,
    onMoveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Note Actions",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        NoteActionItem(
            icon = Icons.Default.Edit,
            label = if (note.isPinned) "Unpin" else "Pin",
            onClick = {
                onPinClick()
                onDismiss()
            }
        )

        NoteActionItem(
            icon = Icons.Default.Edit,
            label = if (note.isArchived) "Unarchive" else "Archive",
            onClick = {
                onArchiveClick()
                onDismiss()
            }
        )

        NoteActionItem(
            icon = Icons.Default.Palette,
            label = "Change color",
            onClick = {
                onColorClick()
                onDismiss()
            }
        )

        NoteActionItem(
            icon = Icons.Outlined.FolderOpen,
            label = "Move to folder",
            onClick = {
                onMoveClick()
                onDismiss()
            }
        )

        NoteActionItem(
            icon = Icons.Default.Delete,
            label = "Delete",
            iconTint = MaterialTheme.colorScheme.error,
            onClick = {
                onDeleteClick()
                onDismiss()
            }
        )
    }
}

@Composable
private fun NoteActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        modifier = modifier.fillMaxSize()
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
