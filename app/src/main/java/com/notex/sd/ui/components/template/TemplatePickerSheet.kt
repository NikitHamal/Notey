package com.notex.sd.ui.components.template

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notex.sd.domain.model.BuiltInTemplates
import com.notex.sd.domain.model.Note
import com.notex.sd.domain.model.Template
import com.notex.sd.domain.model.TemplateCategory
import com.notex.sd.domain.model.TemplateIcon

/**
 * Bottom sheet for selecting a template to create a new note.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePickerSheet(
    onTemplateSelected: (Note) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by remember { mutableStateOf<TemplateCategory?>(null) }

    val templates = remember(selectedCategory) {
        if (selectedCategory == null) {
            BuiltInTemplates.allTemplates
        } else {
            BuiltInTemplates.getByCategory(selectedCategory!!)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            Text(
                text = "Choose a Template",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Start with a structured format for your notes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category filter chips
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Templates list
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = templates,
                    key = { it.id }
                ) { template ->
                    TemplateCard(
                        template = template,
                        onClick = {
                            val note = template.createNote()
                            onTemplateSelected(note)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: TemplateCategory?,
    onCategorySelected: (TemplateCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(null) + TemplateCategory.entries.filter { category ->
        BuiltInTemplates.allTemplates.any { it.category == category }
    }

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category?.displayName ?: "All",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun TemplateCard(
    template: Template,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = getTemplateIconColor(template.icon).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getTemplateIcon(template.icon),
                    contentDescription = null,
                    tint = getTemplateIconColor(template.icon),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Category badge
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = template.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun getTemplateIcon(icon: TemplateIcon): ImageVector {
    return when (icon) {
        TemplateIcon.NOTE -> Icons.Outlined.Description
        TemplateIcon.MEETING -> Icons.Default.Groups
        TemplateIcon.TODO -> Icons.Default.CheckCircle
        TemplateIcon.JOURNAL -> Icons.Default.MenuBook
        TemplateIcon.IDEA -> Icons.Default.Lightbulb
        TemplateIcon.PROJECT -> Icons.Default.Flag
        TemplateIcon.RECIPE -> Icons.Default.Description
        TemplateIcon.TRAVEL -> Icons.Default.Description
        TemplateIcon.BOOK -> Icons.Default.Book
        TemplateIcon.CODE -> Icons.Default.Code
        TemplateIcon.WORKOUT -> Icons.Default.Description
        TemplateIcon.SHOPPING -> Icons.Default.Description
        TemplateIcon.GOAL -> Icons.Default.Flag
        TemplateIcon.HABIT -> Icons.Default.Event
        TemplateIcon.CUSTOM -> Icons.Default.Description
    }
}

@Composable
private fun getTemplateIconColor(icon: TemplateIcon): Color {
    return when (icon) {
        TemplateIcon.NOTE -> MaterialTheme.colorScheme.primary
        TemplateIcon.MEETING -> Color(0xFF2196F3)
        TemplateIcon.TODO -> Color(0xFF4CAF50)
        TemplateIcon.JOURNAL -> Color(0xFFFFC107)
        TemplateIcon.IDEA -> Color(0xFFFF9800)
        TemplateIcon.PROJECT -> Color(0xFF9C27B0)
        TemplateIcon.RECIPE -> Color(0xFFE91E63)
        TemplateIcon.TRAVEL -> Color(0xFF00BCD4)
        TemplateIcon.BOOK -> Color(0xFF009688)
        TemplateIcon.CODE -> Color(0xFF607D8B)
        TemplateIcon.WORKOUT -> Color(0xFFF44336)
        TemplateIcon.SHOPPING -> Color(0xFF795548)
        TemplateIcon.GOAL -> Color(0xFF3F51B5)
        TemplateIcon.HABIT -> Color(0xFF8BC34A)
        TemplateIcon.CUSTOM -> MaterialTheme.colorScheme.tertiary
    }
}
