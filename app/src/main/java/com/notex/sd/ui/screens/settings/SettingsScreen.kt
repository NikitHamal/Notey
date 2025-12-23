package com.notex.sd.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notex.sd.core.preferences.ThemeMode
import com.notex.sd.core.preferences.ViewMode
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showViewModeDialog by rememberSaveable { mutableStateOf(false) }
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }

    // Handle backup status
    LaunchedEffect(uiState.backupStatus) {
        when (val status = uiState.backupStatus) {
            is BackupStatus.Success -> {
                snackbarHostState.showSnackbar(
                    message = if (status.filePath.contains("Restored")) {
                        status.filePath
                    } else {
                        "Backup saved to: ${status.filePath}"
                    }
                )
                viewModel.clearBackupStatus()
            }
            is BackupStatus.Error -> {
                snackbarHostState.showSnackbar("Error: ${status.message}")
                viewModel.clearBackupStatus()
            }
            else -> {}
        }
    }

    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Appearance Section
            item {
                SettingsSectionHeader(title = "Appearance")
            }

            item {
                SettingsListItem(
                    icon = Icons.Default.Brightness6,
                    title = "Theme",
                    subtitle = when (uiState.themeMode) {
                        ThemeMode.SYSTEM -> "System default"
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                    },
                    onClick = { showThemeDialog = true }
                )
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.ColorLens,
                    title = "Dynamic colors",
                    subtitle = "Use colors from your wallpaper",
                    checked = uiState.dynamicColors,
                    onCheckedChange = viewModel::setDynamicColors
                )
            }

            item {
                SettingsListItem(
                    icon = Icons.Default.ViewModule,
                    title = "Default view mode",
                    subtitle = when (uiState.viewMode) {
                        ViewMode.GRID -> "Grid"
                        ViewMode.LIST -> "List"
                    },
                    onClick = { showViewModeDialog = true }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Editor Section
            item {
                SettingsSectionHeader(title = "Editor")
            }

            item {
                SettingsSliderItem(
                    icon = Icons.Default.TextFields,
                    title = "Font size",
                    subtitle = "${uiState.editorFontSize}sp",
                    value = uiState.editorFontSize.toFloat(),
                    valueRange = 12f..24f,
                    onValueChange = { viewModel.setFontSize(it.roundToInt()) }
                )
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Save,
                    title = "Auto-save",
                    subtitle = "Automatically save notes while typing",
                    checked = uiState.autoSave,
                    onCheckedChange = viewModel::setAutoSave
                )
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.FontDownload,
                    title = "Show word count",
                    subtitle = "Display word count in editor",
                    checked = uiState.showWordCount,
                    onCheckedChange = viewModel::setShowWordCount
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Data Section
            item {
                SettingsSectionHeader(title = "Data")
            }

            item {
                SettingsListItem(
                    icon = Icons.Default.Backup,
                    title = "Backup notes",
                    subtitle = "Export all notes to JSON file",
                    onClick = { viewModel.backupNotes() },
                    showProgress = uiState.backupStatus is BackupStatus.InProgress
                )
            }

            item {
                SettingsListItem(
                    icon = Icons.Default.Restore,
                    title = "Restore notes",
                    subtitle = "Import notes from backup file",
                    onClick = {
                        // TODO: Implement file picker for restore
                        scope.launch {
                            snackbarHostState.showSnackbar("File picker coming soon")
                        }
                    }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // About Section
            item {
                SettingsSectionHeader(title = "About")
            }

            item {
                SettingsListItem(
                    icon = Icons.Default.Info,
                    title = "App version",
                    subtitle = "1.0.0 (Beta)",
                    onClick = { showAboutDialog = true }
                )
            }

            item {
                SettingsListItem(
                    icon = Icons.Default.Code,
                    title = "Developer",
                    subtitle = "Built with Jetpack Compose",
                    onClick = { showAboutDialog = true }
                )
            }

            item {
                SettingsListItem(
                    icon = Icons.Default.Star,
                    title = "Rate app",
                    subtitle = "Share your feedback",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Thank you for your interest!")
                        }
                    }
                )
            }

            item {
                SettingsListItem(
                    icon = Icons.Default.Email,
                    title = "Send feedback",
                    subtitle = "Report bugs or suggest features",
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Feedback feature coming soon")
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialogs
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.themeMode,
            onThemeSelected = { theme ->
                viewModel.setThemeMode(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showViewModeDialog) {
        ViewModeSelectionDialog(
            currentViewMode = uiState.viewMode,
            onViewModeSelected = { mode ->
                viewModel.setViewMode(mode)
                showViewModeDialog = false
            },
            onDismiss = { showViewModeDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showProgress: Boolean = false,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = if (showProgress) {
            { CircularProgressIndicator(modifier = Modifier.padding(8.dp)) }
        } else null,
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        modifier = modifier.clickable { onCheckedChange(!checked) }
    )
}

@Composable
private fun SettingsSliderItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember(value) { mutableFloatStateOf(value) }

    ListItem(
        headlineContent = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = sliderValue,
                    onValueChange = {
                        sliderValue = it
                        onValueChange(it)
                    },
                    valueRange = valueRange,
                    steps = (valueRange.endInclusive - valueRange.start - 1).toInt(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = modifier
    )
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTheme by remember { mutableStateOf(currentTheme) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Brightness6,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Choose theme",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                ThemeOption(
                    title = "System default",
                    subtitle = "Follow system theme",
                    selected = selectedTheme == ThemeMode.SYSTEM,
                    onClick = { selectedTheme = ThemeMode.SYSTEM }
                )
                ThemeOption(
                    title = "Light",
                    subtitle = "Light theme",
                    selected = selectedTheme == ThemeMode.LIGHT,
                    onClick = { selectedTheme = ThemeMode.LIGHT }
                )
                ThemeOption(
                    title = "Dark",
                    subtitle = "Dark theme",
                    selected = selectedTheme == ThemeMode.DARK,
                    onClick = { selectedTheme = ThemeMode.DARK }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onThemeSelected(selectedTheme)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ThemeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ViewModeSelectionDialog(
    currentViewMode: ViewMode,
    onViewModeSelected: (ViewMode) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentViewMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ViewModule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Default view mode",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                ViewModeOption(
                    title = "Grid",
                    subtitle = "Show notes in a grid layout",
                    selected = selectedMode == ViewMode.GRID,
                    onClick = { selectedMode = ViewMode.GRID }
                )
                ViewModeOption(
                    title = "List",
                    subtitle = "Show notes in a list layout",
                    selected = selectedMode == ViewMode.LIST,
                    onClick = { selectedMode = ViewMode.LIST }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onViewModeSelected(selectedMode)
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ViewModeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "About NoteX",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NoteX",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Version 1.0.0 (Beta)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A modern, minimalist note-taking app built with Jetpack Compose and Material Design 3.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Features:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• Rich text editing\n• Checklist support\n• Folders and organization\n• Color coding\n• Dark mode support\n• Material You theming",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
