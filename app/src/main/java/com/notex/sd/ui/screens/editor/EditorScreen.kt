package com.notex.sd.ui.screens.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notex.sd.core.theme.NoteBlue
import com.notex.sd.core.theme.NoteBlueDark
import com.notex.sd.core.theme.NoteGray
import com.notex.sd.core.theme.NoteGrayDark
import com.notex.sd.core.theme.NoteGreen
import com.notex.sd.core.theme.NoteGreenDark
import com.notex.sd.core.theme.NoteOrange
import com.notex.sd.core.theme.NoteOrangeDark
import com.notex.sd.core.theme.NotePink
import com.notex.sd.core.theme.NotePinkDark
import com.notex.sd.core.theme.NotePurple
import com.notex.sd.core.theme.NotePurpleDark
import com.notex.sd.core.theme.NoteTeal
import com.notex.sd.core.theme.NoteTealDark
import com.notex.sd.core.theme.NoteYellow
import com.notex.sd.core.theme.NoteYellowDark
import com.notex.sd.domain.model.ChecklistItem
import com.notex.sd.domain.model.NoteColor
import com.notex.sd.ui.components.dialog.ColorPickerDialog
import com.notex.sd.ui.components.editor.FocusModeButton
import com.notex.sd.ui.components.editor.FocusModeEditor
import com.notex.sd.ui.components.editor.FocusModeState
import com.notex.sd.ui.components.editor.FormattingAction
import com.notex.sd.ui.components.editor.FormattingToolbar
import com.notex.sd.ui.components.editor.MarkdownFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onNavigateBack: () -> Unit,
    onMoveToFolder: (String?) -> Unit = {},
    onDeleteNote: () -> Unit = {},
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showUnsavedChangesDialog by rememberSaveable { mutableStateOf(false) }
    var showColorPickerDialog by rememberSaveable { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Focus Mode state
    var focusModeState by remember { mutableStateOf(FocusModeState()) }

    // Keyboard visibility for formatting toolbar
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    // TextFieldValue for content (needed for cursor-aware formatting)
    var contentTextFieldValue by remember(uiState.content) {
        mutableStateOf(TextFieldValue(text = uiState.content, selection = TextRange(uiState.content.length)))
    }

    // Handle back press with unsaved changes
    BackHandler(enabled = uiState.hasUnsavedChanges) {
        if (uiState.hasUnsavedChanges) {
            showUnsavedChangesDialog = true
        } else {
            onNavigateBack()
        }
    }

    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    val isDark = isSystemInDarkTheme()
    val backgroundColor = remember(uiState.color, isDark) {
        getNoteBackgroundColor(uiState.color, isDark)
    }

    // Focus Mode full screen
    if (focusModeState.isActive) {
        FocusModeEditor(
            title = uiState.title,
            content = uiState.content,
            onTitleChange = viewModel::updateTitle,
            onContentChange = viewModel::updateContent,
            onExitFocusMode = {
                focusModeState = focusModeState.copy(isActive = false)
            },
            state = focusModeState,
            onStateChange = { focusModeState = it },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    Scaffold(
        topBar = {
            EditorTopBar(
                hasUnsavedChanges = uiState.hasUnsavedChanges,
                isSaving = uiState.isSaving,
                showMoreMenu = showMoreMenu,
                onShowMoreMenuChange = { showMoreMenu = it },
                onNavigateBack = {
                    if (uiState.hasUnsavedChanges) {
                        showUnsavedChangesDialog = true
                    } else {
                        onNavigateBack()
                    }
                },
                onPinNote = { /* TODO: Implement pin */ },
                onArchiveNote = { /* TODO: Implement archive */ },
                onChangeColor = { showColorPickerDialog = true },
                onMoveToFolder = { onMoveToFolder(null) },
                onDeleteNote = { showDeleteConfirmDialog = true },
                backgroundColor = backgroundColor
            )
        },
        bottomBar = {
            Column {
                // Formatting toolbar - appears above keyboard when editing
                if (!uiState.isChecklist) {
                    FormattingToolbar(
                        visible = imeVisible,
                        onAction = { action ->
                            val newValue = MarkdownFormatter.applyFormatting(contentTextFieldValue, action)
                            contentTextFieldValue = newValue
                            viewModel.updateContent(newValue.text)
                        }
                    )
                }

                EditorBottomBar(
                    uiState = uiState,
                    onToggleChecklist = {
                        // Toggle checklist mode
                    },
                    onColorPickerClick = { showColorPickerDialog = true },
                    onFocusModeClick = {
                        focusModeState = focusModeState.copy(isActive = true)
                    },
                    backgroundColor = backgroundColor
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                EditorContent(
                    uiState = uiState,
                    onTitleChange = viewModel::updateTitle,
                    onContentChange = viewModel::updateContent,
                    onChecklistItemCheckedChange = viewModel::toggleChecklistItem,
                    onChecklistItemTextChange = { itemId, newText ->
                        val updatedItems = uiState.checklistItems.map { item ->
                            if (item.id == itemId) item.copy(text = newText) else item
                        }
                        viewModel.updateChecklistItems(updatedItems)
                    },
                    onChecklistItemRemove = viewModel::removeChecklistItem,
                    onChecklistItemAdd = { viewModel.addChecklistItem("") },
                    backgroundColor = backgroundColor,
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                )
            }
        }
    }

    // Dialogs
    if (showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            onSaveAndExit = {
                viewModel.saveNote()
                showUnsavedChangesDialog = false
                onNavigateBack()
            },
            onDiscardAndExit = {
                showUnsavedChangesDialog = false
                onNavigateBack()
            },
            onDismiss = { showUnsavedChangesDialog = false }
        )
    }

    if (showColorPickerDialog) {
        ColorPickerDialog(
            currentColor = uiState.color,
            onColorSelected = { color ->
                viewModel.updateNoteColor(color)
                showColorPickerDialog = false
            },
            onDismiss = { showColorPickerDialog = false }
        )
    }

    if (showDeleteConfirmDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                showDeleteConfirmDialog = false
                onDeleteNote()
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTopBar(
    hasUnsavedChanges: Boolean,
    isSaving: Boolean,
    showMoreMenu: Boolean,
    onShowMoreMenuChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onPinNote: () -> Unit,
    onArchiveNote: () -> Unit,
    onChangeColor: () -> Unit,
    onMoveToFolder: () -> Unit,
    onDeleteNote: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Saving...",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else if (hasUnsavedChanges) {
                    Text(
                        text = "Unsaved changes",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { onShowMoreMenuChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }

                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { onShowMoreMenuChange(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text("Pin note") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            onPinNote()
                            onShowMoreMenuChange(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Archive") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            onArchiveNote()
                            onShowMoreMenuChange(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Change color") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            onChangeColor()
                            onShowMoreMenuChange(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Move to folder") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            onMoveToFolder()
                            onShowMoreMenuChange(false)
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            onDeleteNote()
                            onShowMoreMenuChange(false)
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}

@Composable
private fun EditorContent(
    uiState: EditorUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onChecklistItemCheckedChange: (String) -> Unit,
    onChecklistItemTextChange: (String, String) -> Unit,
    onChecklistItemRemove: (String) -> Unit,
    onChecklistItemAdd: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Title TextField
            BasicTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box {
                        if (uiState.title.isEmpty()) {
                            Text(
                                text = "Title",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.isChecklist) {
            // Checklist items
            itemsIndexed(
                items = uiState.checklistItems,
                key = { _, item -> item.id }
            ) { index, item ->
                ChecklistItemRow(
                    item = item,
                    onCheckedChange = { onChecklistItemCheckedChange(item.id) },
                    onTextChange = { newText -> onChecklistItemTextChange(item.id, newText) },
                    onRemove = { onChecklistItemRemove(item.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            item {
                // Add new item button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onChecklistItemAdd)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add item",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Add item",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            // Content TextField
            item {
                BasicTextField(
                    value = uiState.content,
                    onValueChange = onContentChange,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (uiState.content.isEmpty()) {
                                Text(
                                    text = "Note",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 100.dp)
                )
            }
        }
    }
}

@Composable
private fun ChecklistItemRow(
    item: ChecklistItem,
    onCheckedChange: () -> Unit,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onCheckedChange() }
        )

        BasicTextField(
            value = item.text,
            onValueChange = onTextChange,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.weight(1f)) {
                    if (item.text.isEmpty()) {
                        Text(
                            text = "List item",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove item",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditorBottomBar(
    uiState: EditorUiState,
    onToggleChecklist: () -> Unit,
    onColorPickerClick: () -> Unit,
    onFocusModeClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = backgroundColor,
        modifier = modifier
    ) {
        Column {
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onColorPickerClick) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Change color",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onToggleChecklist) {
                        Icon(
                            imageVector = if (uiState.isChecklist) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Outlined.CheckCircle
                            },
                            contentDescription = "Toggle checklist",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Focus Mode button - only for regular notes
                    if (!uiState.isChecklist) {
                        Spacer(modifier = Modifier.width(8.dp))
                        FocusModeButton(
                            onClick = onFocusModeClick
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (uiState.wordCount > 0 && !uiState.isChecklist) {
                        Text(
                            text = uiState.formattedWordCount,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    uiState.modifiedAt?.let { timestamp ->
                        Text(
                            text = "Edited ${formatTimestamp(timestamp)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnsavedChangesDialog(
    onSaveAndExit: () -> Unit,
    onDiscardAndExit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unsaved changes") },
        text = { Text("Do you want to save your changes before leaving?") },
        confirmButton = {
            TextButton(onClick = onSaveAndExit) {
                Text("Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDiscardAndExit) {
                    Text("Discard")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Delete note?") },
        text = { Text("This note will be moved to trash. You can restore it within 30 days.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getNoteBackgroundColor(color: NoteColor, isDark: Boolean): Color {
    return when (color) {
        NoteColor.DEFAULT -> if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
        NoteColor.YELLOW -> if (isDark) NoteYellowDark else NoteYellow
        NoteColor.GREEN -> if (isDark) NoteGreenDark else NoteGreen
        NoteColor.BLUE -> if (isDark) NoteBlueDark else NoteBlue
        NoteColor.PINK -> if (isDark) NotePinkDark else NotePink
        NoteColor.PURPLE -> if (isDark) NotePurpleDark else NotePurple
        NoteColor.ORANGE -> if (isDark) NoteOrangeDark else NoteOrange
        NoteColor.TEAL -> if (isDark) NoteTealDark else NoteTeal
        NoteColor.GRAY -> if (isDark) NoteGrayDark else NoteGray
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> {
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}
