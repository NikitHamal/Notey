package com.notex.sd.ui.components.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.InsertLink
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Formatting actions available in the toolbar.
 */
sealed class FormattingAction {
    data object Bold : FormattingAction()
    data object Italic : FormattingAction()
    data object Underline : FormattingAction()
    data object Strikethrough : FormattingAction()
    data object Code : FormattingAction()
    data object CodeBlock : FormattingAction()
    data object Highlight : FormattingAction()
    data class Heading(val level: Int) : FormattingAction()
    data object BulletList : FormattingAction()
    data object NumberedList : FormattingAction()
    data object Checklist : FormattingAction()
    data object Quote : FormattingAction()
    data object Link : FormattingAction()
    data object NoteLink : FormattingAction()
    data object HorizontalRule : FormattingAction()
}

/**
 * Markdown formatting helper object.
 * Handles text transformations for markdown syntax.
 */
object MarkdownFormatter {

    /**
     * Apply formatting to the selected text or cursor position.
     * Returns the new text field value with formatting applied.
     */
    fun applyFormatting(
        textFieldValue: TextFieldValue,
        action: FormattingAction
    ): TextFieldValue {
        val text = textFieldValue.text
        val selection = textFieldValue.selection
        val selectedText = if (selection.collapsed) "" else text.substring(selection.start, selection.end)

        return when (action) {
            FormattingAction.Bold -> wrapSelection(textFieldValue, "**", "**")
            FormattingAction.Italic -> wrapSelection(textFieldValue, "*", "*")
            FormattingAction.Underline -> wrapSelection(textFieldValue, "<u>", "</u>")
            FormattingAction.Strikethrough -> wrapSelection(textFieldValue, "~~", "~~")
            FormattingAction.Code -> wrapSelection(textFieldValue, "`", "`")
            FormattingAction.CodeBlock -> wrapSelectionBlock(textFieldValue, "```\n", "\n```")
            FormattingAction.Highlight -> wrapSelection(textFieldValue, "==", "==")
            is FormattingAction.Heading -> {
                val prefix = "#".repeat(action.level) + " "
                insertAtLineStart(textFieldValue, prefix)
            }
            FormattingAction.BulletList -> insertAtLineStart(textFieldValue, "- ")
            FormattingAction.NumberedList -> insertAtLineStart(textFieldValue, "1. ")
            FormattingAction.Checklist -> insertAtLineStart(textFieldValue, "- [ ] ")
            FormattingAction.Quote -> insertAtLineStart(textFieldValue, "> ")
            FormattingAction.Link -> insertLink(textFieldValue)
            FormattingAction.NoteLink -> wrapSelection(textFieldValue, "[[", "]]")
            FormattingAction.HorizontalRule -> insertHorizontalRule(textFieldValue)
        }
    }

    private fun wrapSelection(
        textFieldValue: TextFieldValue,
        prefix: String,
        suffix: String
    ): TextFieldValue {
        val text = textFieldValue.text
        val selection = textFieldValue.selection

        val newText = buildString {
            append(text.substring(0, selection.start))
            append(prefix)
            if (!selection.collapsed) {
                append(text.substring(selection.start, selection.end))
            }
            append(suffix)
            append(text.substring(selection.end))
        }

        // Position cursor after the prefix if no selection, or after the wrapped text
        val newCursorPos = if (selection.collapsed) {
            selection.start + prefix.length
        } else {
            selection.end + prefix.length + suffix.length
        }

        return TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPos)
        )
    }

    private fun wrapSelectionBlock(
        textFieldValue: TextFieldValue,
        prefix: String,
        suffix: String
    ): TextFieldValue {
        val text = textFieldValue.text
        val selection = textFieldValue.selection

        // Ensure we're on a new line
        val needsNewLineBefore = selection.start > 0 && text[selection.start - 1] != '\n'
        val actualPrefix = if (needsNewLineBefore) "\n$prefix" else prefix

        val newText = buildString {
            append(text.substring(0, selection.start))
            append(actualPrefix)
            if (!selection.collapsed) {
                append(text.substring(selection.start, selection.end))
            }
            append(suffix)
            append(text.substring(selection.end))
        }

        val newCursorPos = selection.start + actualPrefix.length

        return TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPos)
        )
    }

    private fun insertAtLineStart(
        textFieldValue: TextFieldValue,
        prefix: String
    ): TextFieldValue {
        val text = textFieldValue.text
        val selection = textFieldValue.selection

        // Find the start of the current line
        val lineStart = text.lastIndexOf('\n', selection.start - 1) + 1

        val newText = buildString {
            append(text.substring(0, lineStart))
            append(prefix)
            append(text.substring(lineStart))
        }

        return TextFieldValue(
            text = newText,
            selection = TextRange(selection.start + prefix.length)
        )
    }

    private fun insertLink(textFieldValue: TextFieldValue): TextFieldValue {
        val text = textFieldValue.text
        val selection = textFieldValue.selection

        val selectedText = if (selection.collapsed) "link text" else text.substring(selection.start, selection.end)
        val linkTemplate = "[$selectedText](url)"

        val newText = buildString {
            append(text.substring(0, selection.start))
            append(linkTemplate)
            append(text.substring(selection.end))
        }

        // Position cursor at "url" for easy replacement
        val urlStart = selection.start + selectedText.length + 3
        val urlEnd = urlStart + 3

        return TextFieldValue(
            text = newText,
            selection = TextRange(urlStart, urlEnd)
        )
    }

    private fun insertHorizontalRule(textFieldValue: TextFieldValue): TextFieldValue {
        val text = textFieldValue.text
        val selection = textFieldValue.selection

        val rule = "\n---\n"

        val newText = buildString {
            append(text.substring(0, selection.start))
            append(rule)
            append(text.substring(selection.end))
        }

        return TextFieldValue(
            text = newText,
            selection = TextRange(selection.start + rule.length)
        )
    }
}

/**
 * Rich text formatting toolbar with markdown actions.
 * Appears above the keyboard when editing.
 */
@Composable
fun FormattingToolbar(
    visible: Boolean,
    onAction: (FormattingAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 4.dp
        ) {
            Column {
                HorizontalDivider(thickness = 0.5.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Text formatting
                    FormatButton(
                        icon = Icons.Default.FormatBold,
                        contentDescription = "Bold",
                        onClick = { onAction(FormattingAction.Bold) }
                    )

                    FormatButton(
                        icon = Icons.Default.FormatItalic,
                        contentDescription = "Italic",
                        onClick = { onAction(FormattingAction.Italic) }
                    )

                    FormatButton(
                        icon = Icons.Default.FormatStrikethrough,
                        contentDescription = "Strikethrough",
                        onClick = { onAction(FormattingAction.Strikethrough) }
                    )

                    FormatButton(
                        icon = Icons.Default.Highlight,
                        contentDescription = "Highlight",
                        onClick = { onAction(FormattingAction.Highlight) }
                    )

                    ToolbarDivider()

                    // Headings dropdown
                    HeadingDropdown(onAction = onAction)

                    ToolbarDivider()

                    // Lists
                    FormatButton(
                        icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                        contentDescription = "Bullet list",
                        onClick = { onAction(FormattingAction.BulletList) }
                    )

                    FormatButton(
                        icon = Icons.Default.FormatListNumbered,
                        contentDescription = "Numbered list",
                        onClick = { onAction(FormattingAction.NumberedList) }
                    )

                    FormatButton(
                        icon = Icons.Default.CheckBox,
                        contentDescription = "Checklist",
                        onClick = { onAction(FormattingAction.Checklist) }
                    )

                    ToolbarDivider()

                    // Code and quote
                    FormatButton(
                        icon = Icons.Default.Code,
                        contentDescription = "Code",
                        onClick = { onAction(FormattingAction.Code) }
                    )

                    FormatButton(
                        icon = Icons.Default.FormatQuote,
                        contentDescription = "Quote",
                        onClick = { onAction(FormattingAction.Quote) }
                    )

                    ToolbarDivider()

                    // Links
                    FormatButton(
                        icon = Icons.Default.InsertLink,
                        contentDescription = "Link",
                        onClick = { onAction(FormattingAction.Link) }
                    )

                    NoteLinkButton(
                        onClick = { onAction(FormattingAction.NoteLink) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 0.9f else 1f,
        label = "buttonScale"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.Transparent
            },
            contentColor = if (isActive) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun HeadingDropdown(
    onAction: (FormattingAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Title,
                contentDescription = "Heading",
                modifier = Modifier.size(22.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (1..3).forEach { level ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Heading $level",
                            fontSize = when (level) {
                                1 -> 20.sp
                                2 -> 18.sp
                                else -> 16.sp
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    onClick = {
                        onAction(FormattingAction.Heading(level))
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun NoteLinkButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        modifier = modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "[[",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "]]",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ToolbarDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .width(1.dp)
            .height(24.dp)
            .background(
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
    )
}

/**
 * Compact formatting bar that shows essential actions only
 */
@Composable
fun CompactFormattingBar(
    onAction: (FormattingAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompactFormatChip(
            label = "B",
            isBold = true,
            onClick = { onAction(FormattingAction.Bold) }
        )

        CompactFormatChip(
            label = "I",
            isItalic = true,
            onClick = { onAction(FormattingAction.Italic) }
        )

        CompactFormatChip(
            label = "H",
            onClick = { onAction(FormattingAction.Heading(2)) }
        )

        CompactFormatChip(
            label = "•",
            onClick = { onAction(FormattingAction.BulletList) }
        )

        CompactFormatChip(
            label = "</>",
            onClick = { onAction(FormattingAction.Code) }
        )

        Spacer(modifier = Modifier.weight(1f))

        Surface(
            onClick = { onAction(FormattingAction.NoteLink) },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "[[ ]]",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun CompactFormatChip(
    label: String,
    isBold: Boolean = false,
    isItalic: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Box(
            modifier = Modifier
                .size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                    fontStyle = if (isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
