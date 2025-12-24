package com.notex.sd.ui.components.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notex.sd.domain.model.FolderWithChildren

@Composable
fun NoteXDrawer(
    currentRoute: String,
    selectedFolderId: String?,
    folders: List<FolderWithChildren>,
    allNotesCount: Int,
    archivedCount: Int,
    trashedCount: Int,
    onNavigateToAllNotes: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onFolderSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Drawer Header
            DrawerHeader()

            Spacer(modifier = Modifier.height(8.dp))

            // All Notes
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Notes,
                        contentDescription = "All Notes"
                    )
                },
                label = { Text("All Notes") },
                badge = {
                    if (allNotesCount > 0) {
                        Badge {
                            Text(text = allNotesCount.toString())
                        }
                    }
                },
                selected = currentRoute == "all_notes" && selectedFolderId == null,
                onClick = onNavigateToAllNotes,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // Archive
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = "Archive"
                    )
                },
                label = { Text("Archive") },
                badge = {
                    if (archivedCount > 0) {
                        Badge {
                            Text(text = archivedCount.toString())
                        }
                    }
                },
                selected = currentRoute == "archive",
                onClick = onNavigateToArchive,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // Trash
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Trash"
                    )
                },
                badge = {
                    if (trashedCount > 0) {
                        Badge {
                            Text(text = trashedCount.toString())
                        }
                    }
                },
                label = { Text("Trash") },
                selected = currentRoute == "trash",
                onClick = onNavigateToTrash,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // Folders Section
            if (folders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Folders",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
                )

                folders.forEach { folderWithChildren ->
                    FolderTreeItem(
                        folderWithChildren = folderWithChildren,
                        selectedFolderId = selectedFolderId,
                        onFolderSelected = onFolderSelected,
                        level = 0
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Settings
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                },
                label = { Text("Settings") },
                selected = currentRoute == "settings",
                onClick = onNavigateToSettings,
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DrawerHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        Column {
            Text(
                text = "NoteX",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Your personal note-taking app",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FolderTreeItem(
    folderWithChildren: FolderWithChildren,
    selectedFolderId: String?,
    onFolderSelected: (String) -> Unit,
    level: Int,
    modifier: Modifier = Modifier
) {
    var expanded by remember(folderWithChildren.folder.id) {
        mutableStateOf(folderWithChildren.folder.isExpanded)
    }

    Column(modifier = modifier) {
        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = folderWithChildren.folder.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (folderWithChildren.notesCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = folderWithChildren.notesCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            badge = {
                if (folderWithChildren.children.isNotEmpty()) {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            selected = selectedFolderId == folderWithChildren.folder.id,
            onClick = { onFolderSelected(folderWithChildren.folder.id) },
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .padding(start = (level * 16).dp)
        )

        // Render children with animation
        AnimatedVisibility(
            visible = expanded && folderWithChildren.children.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                folderWithChildren.children.forEach { child ->
                    FolderTreeItem(
                        folderWithChildren = child,
                        selectedFolderId = selectedFolderId,
                        onFolderSelected = onFolderSelected,
                        level = level + 1
                    )
                }
            }
        }
    }
}
