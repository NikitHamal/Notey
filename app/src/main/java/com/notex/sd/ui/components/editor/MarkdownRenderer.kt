package com.notex.sd.ui.components.editor

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Markdown AST node types for rendering.
 */
sealed class MarkdownNode {
    data class Paragraph(val content: List<InlineNode>) : MarkdownNode()
    data class Heading(val level: Int, val content: List<InlineNode>) : MarkdownNode()
    data class CodeBlock(val code: String, val language: String?) : MarkdownNode()
    data class Quote(val children: List<MarkdownNode>) : MarkdownNode()
    data class BulletList(val items: List<ListItem>) : MarkdownNode()
    data class NumberedList(val items: List<ListItem>) : MarkdownNode()
    data class ListItem(val content: List<InlineNode>, val isChecked: Boolean? = null) : MarkdownNode()
    data object HorizontalRule : MarkdownNode()
    data object Empty : MarkdownNode()
}

/**
 * Inline markdown elements.
 */
sealed class InlineNode {
    data class Text(val content: String) : InlineNode()
    data class Bold(val content: List<InlineNode>) : InlineNode()
    data class Italic(val content: List<InlineNode>) : InlineNode()
    data class Strikethrough(val content: List<InlineNode>) : InlineNode()
    data class Code(val content: String) : InlineNode()
    data class Highlight(val content: List<InlineNode>) : InlineNode()
    data class Link(val text: String, val url: String) : InlineNode()
    data class NoteLink(val noteTitle: String) : InlineNode()
}

/**
 * Parses markdown text into an AST for rendering.
 */
object MarkdownParser {

    private val headingRegex = Regex("""^(#{1,6})\s+(.+)$""")
    private val bulletListRegex = Regex("""^[-*+]\s+(.+)$""")
    private val numberedListRegex = Regex("""^\d+\.\s+(.+)$""")
    private val checklistRegex = Regex("""^[-*+]\s+\[([ xX])]\s+(.+)$""")
    private val quoteRegex = Regex("""^>\s*(.*)$""")
    private val codeBlockStartRegex = Regex("""^```(\w*)$""")
    private val codeBlockEndRegex = Regex("""^```$""")
    private val horizontalRuleRegex = Regex("""^[-*_]{3,}$""")

    fun parse(markdown: String): List<MarkdownNode> {
        val lines = markdown.lines()
        val nodes = mutableListOf<MarkdownNode>()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]

            when {
                line.isBlank() -> {
                    i++
                }

                horizontalRuleRegex.matches(line) -> {
                    nodes.add(MarkdownNode.HorizontalRule)
                    i++
                }

                headingRegex.matches(line) -> {
                    val match = headingRegex.find(line)!!
                    val level = match.groupValues[1].length
                    val content = parseInline(match.groupValues[2])
                    nodes.add(MarkdownNode.Heading(level, content))
                    i++
                }

                codeBlockStartRegex.matches(line) -> {
                    val startMatch = codeBlockStartRegex.find(line)!!
                    val language = startMatch.groupValues[1].takeIf { it.isNotEmpty() }
                    val codeLines = mutableListOf<String>()
                    i++

                    while (i < lines.size && !codeBlockEndRegex.matches(lines[i])) {
                        codeLines.add(lines[i])
                        i++
                    }

                    if (i < lines.size) i++ // Skip closing ```
                    nodes.add(MarkdownNode.CodeBlock(codeLines.joinToString("\n"), language))
                }

                quoteRegex.matches(line) -> {
                    val quoteLines = mutableListOf<String>()
                    while (i < lines.size && quoteRegex.matches(lines[i])) {
                        val match = quoteRegex.find(lines[i])!!
                        quoteLines.add(match.groupValues[1])
                        i++
                    }
                    val quoteContent = parse(quoteLines.joinToString("\n"))
                    nodes.add(MarkdownNode.Quote(quoteContent))
                }

                checklistRegex.matches(line) -> {
                    val items = mutableListOf<MarkdownNode.ListItem>()
                    while (i < lines.size && checklistRegex.matches(lines[i])) {
                        val match = checklistRegex.find(lines[i])!!
                        val isChecked = match.groupValues[1].lowercase() == "x"
                        val content = parseInline(match.groupValues[2])
                        items.add(MarkdownNode.ListItem(content, isChecked))
                        i++
                    }
                    nodes.add(MarkdownNode.BulletList(items))
                }

                bulletListRegex.matches(line) -> {
                    val items = mutableListOf<MarkdownNode.ListItem>()
                    while (i < lines.size && bulletListRegex.matches(lines[i])) {
                        val match = bulletListRegex.find(lines[i])!!
                        val content = parseInline(match.groupValues[1])
                        items.add(MarkdownNode.ListItem(content))
                        i++
                    }
                    nodes.add(MarkdownNode.BulletList(items))
                }

                numberedListRegex.matches(line) -> {
                    val items = mutableListOf<MarkdownNode.ListItem>()
                    while (i < lines.size && numberedListRegex.matches(lines[i])) {
                        val match = numberedListRegex.find(lines[i])!!
                        val content = parseInline(match.groupValues[1])
                        items.add(MarkdownNode.ListItem(content))
                        i++
                    }
                    nodes.add(MarkdownNode.NumberedList(items))
                }

                else -> {
                    val paragraphLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].isNotBlank() &&
                        !headingRegex.matches(lines[i]) &&
                        !bulletListRegex.matches(lines[i]) &&
                        !numberedListRegex.matches(lines[i]) &&
                        !checklistRegex.matches(lines[i]) &&
                        !quoteRegex.matches(lines[i]) &&
                        !codeBlockStartRegex.matches(lines[i]) &&
                        !horizontalRuleRegex.matches(lines[i])
                    ) {
                        paragraphLines.add(lines[i])
                        i++
                    }
                    if (paragraphLines.isNotEmpty()) {
                        val content = parseInline(paragraphLines.joinToString(" "))
                        nodes.add(MarkdownNode.Paragraph(content))
                    }
                }
            }
        }

        return nodes
    }

    private fun parseInline(text: String): List<InlineNode> {
        val nodes = mutableListOf<InlineNode>()
        var remaining = text
        var lastIndex = 0

        // Combined regex for all inline patterns
        val patterns = listOf(
            Triple("""\[\[([^\]]+)]]""".toRegex(), "notelink", 1),
            Triple("""\*\*(.+?)\*\*""".toRegex(), "bold", 1),
            Triple("""__(.+?)__""".toRegex(), "bold", 1),
            Triple("""\*(.+?)\*""".toRegex(), "italic", 1),
            Triple("""_(.+?)_""".toRegex(), "italic", 1),
            Triple("""~~(.+?)~~""".toRegex(), "strikethrough", 1),
            Triple("""==(.+?)==""".toRegex(), "highlight", 1),
            Triple("""`(.+?)`""".toRegex(), "code", 1),
            Triple("""\[(.+?)]\((.+?)\)""".toRegex(), "link", 2)
        )

        while (remaining.isNotEmpty()) {
            var earliestMatch: Pair<MatchResult, Triple<Regex, String, Int>>? = null
            var earliestIndex = Int.MAX_VALUE

            for (pattern in patterns) {
                val match = pattern.first.find(remaining)
                if (match != null && match.range.first < earliestIndex) {
                    earliestIndex = match.range.first
                    earliestMatch = match to pattern
                }
            }

            if (earliestMatch == null) {
                if (remaining.isNotEmpty()) {
                    nodes.add(InlineNode.Text(remaining))
                }
                break
            }

            val (match, pattern) = earliestMatch
            val (_, type, _) = pattern

            // Add text before the match
            if (match.range.first > 0) {
                nodes.add(InlineNode.Text(remaining.substring(0, match.range.first)))
            }

            // Process the match
            when (type) {
                "notelink" -> nodes.add(InlineNode.NoteLink(match.groupValues[1]))
                "bold" -> nodes.add(InlineNode.Bold(parseInline(match.groupValues[1])))
                "italic" -> nodes.add(InlineNode.Italic(parseInline(match.groupValues[1])))
                "strikethrough" -> nodes.add(InlineNode.Strikethrough(parseInline(match.groupValues[1])))
                "highlight" -> nodes.add(InlineNode.Highlight(parseInline(match.groupValues[1])))
                "code" -> nodes.add(InlineNode.Code(match.groupValues[1]))
                "link" -> nodes.add(InlineNode.Link(match.groupValues[1], match.groupValues[2]))
            }

            remaining = remaining.substring(match.range.last + 1)
        }

        return if (nodes.isEmpty()) listOf(InlineNode.Text(text)) else nodes
    }
}

/**
 * Renders parsed markdown nodes as Composable UI.
 */
@Composable
fun MarkdownContent(
    markdown: String,
    onNoteLinkClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val nodes = remember(markdown) {
        MarkdownParser.parse(markdown)
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        nodes.forEach { node ->
            RenderNode(node, onNoteLinkClick)
        }
    }
}

@Composable
private fun RenderNode(
    node: MarkdownNode,
    onNoteLinkClick: (String) -> Unit
) {
    when (node) {
        is MarkdownNode.Paragraph -> {
            Text(
                text = renderInline(node.content, onNoteLinkClick),
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp
                )
            )
        }

        is MarkdownNode.Heading -> {
            val style = when (node.level) {
                1 -> MaterialTheme.typography.headlineLarge
                2 -> MaterialTheme.typography.headlineMedium
                3 -> MaterialTheme.typography.headlineSmall
                4 -> MaterialTheme.typography.titleLarge
                5 -> MaterialTheme.typography.titleMedium
                else -> MaterialTheme.typography.titleSmall
            }

            Text(
                text = renderInline(node.content, onNoteLinkClick),
                style = style.copy(fontWeight = FontWeight.Bold)
            )
        }

        is MarkdownNode.CodeBlock -> {
            CodeBlockView(
                code = node.code,
                language = node.language
            )
        }

        is MarkdownNode.Quote -> {
            QuoteView(children = node.children, onNoteLinkClick = onNoteLinkClick)
        }

        is MarkdownNode.BulletList -> {
            BulletListView(items = node.items, onNoteLinkClick = onNoteLinkClick)
        }

        is MarkdownNode.NumberedList -> {
            NumberedListView(items = node.items, onNoteLinkClick = onNoteLinkClick)
        }

        MarkdownNode.HorizontalRule -> {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        is MarkdownNode.ListItem -> {
            // Handled by list views
        }

        MarkdownNode.Empty -> {}
    }
}

@Composable
private fun renderInline(
    nodes: List<InlineNode>,
    onNoteLinkClick: (String) -> Unit
): AnnotatedString {
    return buildAnnotatedString {
        nodes.forEach { node ->
            when (node) {
                is InlineNode.Text -> append(node.content)

                is InlineNode.Bold -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(renderInline(node.content, onNoteLinkClick))
                    }
                }

                is InlineNode.Italic -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(renderInline(node.content, onNoteLinkClick))
                    }
                }

                is InlineNode.Strikethrough -> {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append(renderInline(node.content, onNoteLinkClick))
                    }
                }

                is InlineNode.Code -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0x20888888)
                        )
                    ) {
                        append(" ${node.content} ")
                    }
                }

                is InlineNode.Highlight -> {
                    withStyle(
                        SpanStyle(
                            background = Color(0x40FFEB3B)
                        )
                    ) {
                        append(renderInline(node.content, onNoteLinkClick))
                    }
                }

                is InlineNode.Link -> {
                    pushStringAnnotation("url", node.url)
                    withStyle(
                        SpanStyle(
                            color = Color(0xFF1E88E5),
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(node.text)
                    }
                    pop()
                }

                is InlineNode.NoteLink -> {
                    pushStringAnnotation("notelink", node.noteTitle)
                    withStyle(
                        SpanStyle(
                            color = Color(0xFF7C4DFF),
                            fontWeight = FontWeight.Medium,
                            background = Color(0x15673AB7)
                        )
                    ) {
                        append(" ${node.noteTitle} ")
                    }
                    pop()
                }
            }
        }
    }
}

@Composable
private fun CodeBlockView(
    code: String,
    language: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Column {
            if (!language.isNullOrEmpty()) {
                Text(
                    text = language,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Text(
                text = code,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 20.sp
                ),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun QuoteView(
    children: List<MarkdownNode>,
    onNoteLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            children.forEach { node ->
                RenderNode(node, onNoteLinkClick)
            }
        }
    }
}

@Composable
private fun BulletListView(
    items: List<MarkdownNode.ListItem>,
    onNoteLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (item.isChecked != null) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(2.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Text(
                    text = renderInline(item.content, onNoteLinkClick),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (item.isChecked == true) TextDecoration.LineThrough else null,
                        color = if (item.isChecked == true) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NumberedListView(
    items: List<MarkdownNode.ListItem>,
    onNoteLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${index + 1}.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(24.dp)
                )

                Text(
                    text = renderInline(item.content, onNoteLinkClick),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Clickable note link component for use in the editor.
 */
@Composable
fun ClickableNoteLink(
    noteTitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "[[",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Text(
                text = noteTitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "]]",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
    }
}
