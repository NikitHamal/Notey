package com.notex.sd.domain.model

import java.util.UUID

/**
 * Represents a link between two notes (wiki-style [[note]] linking).
 * This is a unique selling point feature that enables knowledge graph navigation.
 */
data class NoteLink(
    val id: String = UUID.randomUUID().toString(),
    val sourceNoteId: String,
    val targetNoteId: String,
    val linkText: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Extracts wiki-style links from note content.
         * Supports [[Note Title]] syntax.
         */
        fun extractLinksFromContent(content: String): List<String> {
            val linkPattern = Regex("""\[\[([^\]]+)\]\]""")
            return linkPattern.findAll(content)
                .map { it.groupValues[1].trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .toList()
        }

        /**
         * Replaces wiki-style links with clickable spans.
         * Returns content with link markers for processing.
         */
        fun processLinksInContent(content: String): ProcessedContent {
            val linkPattern = Regex("""\[\[([^\]]+)\]\]""")
            val links = mutableListOf<LinkSpan>()
            var offset = 0

            linkPattern.findAll(content).forEach { match ->
                val linkText = match.groupValues[1].trim()
                val adjustedStart = match.range.first - offset
                val adjustedEnd = adjustedStart + linkText.length
                links.add(LinkSpan(adjustedStart, adjustedEnd, linkText))
                offset += match.value.length - linkText.length
            }

            val processedText = content.replace(linkPattern) { it.groupValues[1] }
            return ProcessedContent(processedText, links)
        }
    }
}

/**
 * Represents a span of text that is a link to another note.
 */
data class LinkSpan(
    val start: Int,
    val end: Int,
    val linkText: String
)

/**
 * Content with extracted link information.
 */
data class ProcessedContent(
    val text: String,
    val links: List<LinkSpan>
)

/**
 * Represents a backlink - a note that links to the current note.
 */
data class Backlink(
    val sourceNoteId: String,
    val sourceNoteTitle: String,
    val linkText: String,
    val contextPreview: String,
    val modifiedAt: Long
)
