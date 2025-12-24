package com.notex.sd.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notex.sd.R
import com.notex.sd.core.preferences.SortOrder
import com.notex.sd.core.preferences.ViewMode
import com.notex.sd.domain.model.Note
import com.notex.sd.ui.components.common.EmptyState
import com.notex.sd.ui.components.common.LoadingIndicator
import com.notex.sd.ui.components.dialog.ColorPickerDialog
import com.notex.sd.ui.components.drawer.NoteXDrawer
import com.notex.sd.ui.components.note.NoteCardLayout
import com.notex.sd.ui.components.note.NotesList
import com.notex.sd.ui.components.quickaction.QuickActionsFab
import com.notex.sd.ui.components.quickaction.QuickActionsChipRow
import com.notex.sd.ui.components.template.TemplatePickerSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToEditor: (noteId: String?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFolder: (folderId: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val createdNoteId by viewModel.createdNoteId.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    // Selected note for actions - store only ID to avoid Bundle serialization issues
    var selectedNoteId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedNote = selectedNoteId?.let { id -> uiState.notes.find { it.id == id } }
    var showActionSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Color picker dialog
    var showColorPicker by rememberSaveable { mutableStateOf(false) }

    // Sort order dropdown
    var showSortMenu by remember { mutableStateOf(false) }

    // Template picker
    var showTemplatePicker by rememberSaveable { mutableStateOf(false) }

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // Navigate to editor when note is created from template
    LaunchedEffect(createdNoteId) {
        createdNoteId?.let { noteId ->
            viewModel.clearCreatedNoteId()
            onNavigateToEditor(noteId)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NoteXDrawer(
                currentRoute = "all_notes",
                selectedFolderId = null,
                folders = emptyList(),
                allNotesCount = uiState.notes.size,
                archivedCount = 0,
                trashedCount = 0,
                onNavigateToAllNotes = {
                    scope.launch { drawerState.close() }
                },
                onNavigateToArchive = {
                    scope.launch { drawerState.close() }
                    onNavigateToArchive()
                },
                onNavigateToTrash = {
                    scope.launch { drawerState.close() }
                    onNavigateToTrash()
                },
                onNavigateToSettings = {
                    scope.launch { drawerState.close() }
                    onNavigateToSettings()
                },
                onFolderSelected = { folderId ->
                    scope.launch { drawerState.close() }
                    onNavigateToFolder(folderId)
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.home_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    actions = {
                        // Search button
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.nav_search)
                            )
                        }

                        // View mode toggle
                        IconButton(onClick = { viewModel.toggleViewMode() }) {
                            Icon(
                                imageVector = when (uiState.viewMode) {
                                    ViewMode.GRID -> Icons.Default.ViewAgenda
                                    ViewMode.LIST -> Icons.Default.GridView
                                },
                                contentDescription = "Toggle view mode"
                            )
                        }

                        // Sort order menu
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort"
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Modified (Newest)") },
                                    onClick = {
                                        viewModel.updateSortOrder(SortOrder.MODIFIED_DESC)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Modified (Oldest)") },
                                    onClick = {
                                        viewModel.updateSortOrder(SortOrder.MODIFIED_ASC)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Created (Newest)") },
                                    onClick = {
                                        viewModel.updateSortOrder(SortOrder.CREATED_DESC)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Created (Oldest)") },
                                    onClick = {
                                        viewModel.updateSortOrder(SortOrder.CREATED_ASC)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Title (A-Z)") },
                                    onClick = {
                                        viewModel.updateSortOrder(SortOrder.TITLE_ASC)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Title (Z-A)") },
                                    onClick = {
                                        viewModel.updateSortOrder(SortOrder.TITLE_DESC)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = !uiState.isLoading,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    QuickActionsFab(
                        onNewNote = { onNavigateToEditor(null) },
                        onNewChecklist = {
                            // Navigate to editor with checklist mode
                            // The editor will handle creating a checklist note
                            onNavigateToEditor(null)
                        },
                        onOpenTemplates = { showTemplatePicker = true }
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
                when {
                    uiState.isLoading -> {
                        LoadingIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    !uiState.hasNotes -> {
                        EmptyState(
                            icon = Icons.AutoMirrored.Outlined.NoteAdd,
                            title = stringResource(R.string.home_empty_title),
                            subtitle = stringResource(R.string.home_empty_subtitle),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        NotesList(
                            notes = uiState.notes,
                            selectedNoteIds = emptySet(),
                            layout = when (uiState.viewMode) {
                                ViewMode.GRID -> NoteCardLayout.GRID
                                ViewMode.LIST -> NoteCardLayout.LIST
                            },
                            onNoteClick = { note ->
                                onNavigateToEditor(note.id)
                            },
                            onNoteLongClick = { note ->
                                selectedNoteId = note.id
                                showActionSheet = true
                            },
                            emptyStateTitle = stringResource(R.string.home_empty_title),
                            emptyStateSubtitle = stringResource(R.string.home_empty_subtitle),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Note actions bottom sheet
    if (showActionSheet && selectedNote != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showActionSheet = false
                selectedNoteId = null
            },
            sheetState = sheetState
        ) {
            NoteActionsBottomSheet(
                note = selectedNote!!,
                onPin = {
                    selectedNote?.let { note ->
                        viewModel.togglePin(note.id, note.isPinned)
                    }
                    scope.launch {
                        sheetState.hide()
                        showActionSheet = false
                        selectedNoteId = null
                    }
                },
                onArchive = {
                    selectedNote?.let { note ->
                        viewModel.archiveNote(note.id, note.isArchived)
                    }
                    scope.launch {
                        sheetState.hide()
                        showActionSheet = false
                        selectedNoteId = null
                    }
                },
                onDelete = {
                    selectedNote?.let { note ->
                        viewModel.moveToTrash(note.id)
                    }
                    scope.launch {
                        sheetState.hide()
                        showActionSheet = false
                        selectedNoteId = null
                    }
                },
                onChangeColor = {
                    showColorPicker = true
                },
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        showActionSheet = false
                        selectedNoteId = null
                    }
                }
            )
        }
    }

    // Color picker dialog
    if (showColorPicker && selectedNote != null) {
        ColorPickerDialog(
            currentColor = selectedNote.color,
            onColorSelected = { color ->
                selectedNote?.let { note ->
                    viewModel.updateNoteColor(note.id, color)
                }
                showColorPicker = false
                scope.launch {
                    sheetState.hide()
                    showActionSheet = false
                    selectedNoteId = null
                }
            },
            onDismiss = {
                showColorPicker = false
            }
        )
    }

    // Template picker sheet
    if (showTemplatePicker) {
        TemplatePickerSheet(
            onTemplateSelected = { note ->
                showTemplatePicker = false
                // Save the note from template and navigate to editor
                viewModel.createNoteFromTemplate(note)
            },
            onDismiss = { showTemplatePicker = false }
        )
    }
}

@Composable
private fun NoteActionsBottomSheet(
    note: Note,
    onPin: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onChangeColor: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        // Pin/Unpin action
        BottomSheetAction(
            icon = Icons.Default.PushPin,
            text = stringResource(if (note.isPinned) R.string.action_unpin else R.string.action_pin),
            onClick = {
                onPin()
                onDismiss()
            }
        )

        // Archive action
        BottomSheetAction(
            icon = Icons.Default.Archive,
            text = stringResource(R.string.action_archive),
            onClick = {
                onArchive()
                onDismiss()
            }
        )

        // Change color action
        BottomSheetAction(
            icon = Icons.Default.Palette,
            text = "Change color",
            onClick = {
                onChangeColor()
            }
        )

        // Delete action
        BottomSheetAction(
            icon = Icons.Default.Delete,
            text = stringResource(R.string.action_delete),
            onClick = {
                onDelete()
                onDismiss()
            },
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun BottomSheetAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = tint
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = tint,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
