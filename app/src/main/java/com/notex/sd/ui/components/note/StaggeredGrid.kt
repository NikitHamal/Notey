package com.notex.sd.ui.components.note

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil

/**
 * A staggered grid layout (Pinterest/Masonry style) that displays items with varying heights.
 * This creates a more dynamic and visually interesting layout for note cards.
 *
 * Uses a custom Layout composable for precise control over item placement.
 */
@Composable
fun <T> StaggeredVerticalGrid(
    items: List<T>,
    columns: Int = 2,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    itemContent: @Composable (T) -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val startPadding = contentPadding.calculateStartPadding(layoutDirection)
    val endPadding = contentPadding.calculateEndPadding(layoutDirection)
    val topPadding = contentPadding.calculateTopPadding()
    val bottomPadding = contentPadding.calculateBottomPadding()

    // Split items into rows for the LazyColumn
    val rows by remember(items, columns) {
        derivedStateOf {
            val rowCount = ceil(items.size.toFloat() / columns).toInt()
            List(rowCount) { rowIndex ->
                val startIdx = rowIndex * columns
                val endIdx = minOf(startIdx + columns, items.size)
                items.subList(startIdx, endIdx)
            }
        }
    }

    LazyColumn(
        state = state,
        contentPadding = PaddingValues(
            start = startPadding,
            end = endPadding,
            top = topPadding,
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        modifier = modifier
    ) {
        items(
            count = rows.size,
            key = { rowIndex -> "row_$rowIndex" }
        ) { rowIndex ->
            StaggeredRow(
                items = rows[rowIndex],
                columns = columns,
                horizontalSpacing = horizontalSpacing,
                modifier = Modifier.fillMaxWidth(),
                itemContent = itemContent
            )
        }
    }
}

/**
 * A single row in the staggered grid that lays out items with equal width
 * but varying heights based on content.
 */
@Composable
private fun <T> StaggeredRow(
    items: List<T>,
    columns: Int,
    horizontalSpacing: Dp,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    Layout(
        content = {
            items.forEach { item ->
                Box(modifier = Modifier) {
                    itemContent(item)
                }
            }
        },
        modifier = modifier
    ) { measurables, constraints ->
        val spacing = horizontalSpacing.roundToPx()
        val totalSpacing = spacing * (columns - 1)
        val itemWidth = (constraints.maxWidth - totalSpacing) / columns

        val itemConstraints = constraints.copy(
            minWidth = itemWidth,
            maxWidth = itemWidth
        )

        val placeables = measurables.map { it.measure(itemConstraints) }
        val maxHeight = placeables.maxOfOrNull { it.height } ?: 0

        layout(constraints.maxWidth, maxHeight) {
            var xPosition = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(x = xPosition, y = 0)
                xPosition += itemWidth + spacing
            }
        }
    }
}

/**
 * Alternative masonry implementation using a true staggered approach where
 * items are placed in the column with the least height.
 * Better for variable height content but requires measuring all items upfront.
 */
@Composable
fun <T : Any> MasonryGrid(
    items: List<T>,
    columns: Int = 2,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalSpacing: Dp = 10.dp,
    verticalSpacing: Dp = 10.dp,
    modifier: Modifier = Modifier,
    key: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val startPadding = contentPadding.calculateStartPadding(layoutDirection)
    val endPadding = contentPadding.calculateEndPadding(layoutDirection)
    val topPadding = contentPadding.calculateTopPadding()
    val bottomPadding = contentPadding.calculateBottomPadding()

    Layout(
        content = {
            items.forEach { item ->
                Box(
                    modifier = Modifier.padding(bottom = verticalSpacing)
                ) {
                    itemContent(item)
                }
            }
        },
        modifier = modifier.padding(
            start = startPadding,
            end = endPadding,
            top = topPadding,
            bottom = bottomPadding
        )
    ) { measurables, constraints ->
        val spacing = horizontalSpacing.roundToPx()
        val totalSpacing = spacing * (columns - 1)
        val itemWidth = (constraints.maxWidth - totalSpacing) / columns

        val itemConstraints = constraints.copy(
            minWidth = itemWidth,
            maxWidth = itemWidth
        )

        val placeables = measurables.map { it.measure(itemConstraints) }

        // Track the height of each column
        val columnHeights = IntArray(columns) { 0 }

        // Assign each item to the shortest column
        val placements = mutableListOf<ItemPlacement>()

        placeables.forEachIndexed { index, placeable ->
            // Find the shortest column
            val column = columnHeights.withIndex().minByOrNull { it.value }?.index ?: 0

            val x = column * (itemWidth + spacing)
            val y = columnHeights[column]

            placements.add(ItemPlacement(x, y, placeable))
            columnHeights[column] += placeable.height
        }

        val totalHeight = columnHeights.maxOrNull() ?: 0

        layout(constraints.maxWidth, totalHeight) {
            placements.forEach { placement ->
                placement.placeable.placeRelative(
                    x = placement.x,
                    y = placement.y
                )
            }
        }
    }
}

private data class ItemPlacement(
    val x: Int,
    val y: Int,
    val placeable: Placeable
)

/**
 * Lazy masonry grid that chunks items and uses LazyColumn for virtualization.
 * More memory efficient for large lists.
 */
@Composable
fun <T : Any> LazyMasonryGrid(
    items: List<T>,
    columns: Int = 2,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalSpacing: Dp = 10.dp,
    verticalSpacing: Dp = 10.dp,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    key: ((T) -> Any)? = null,
    sectionHeader: (@Composable (String) -> Unit)? = null,
    pinnedItems: List<T> = emptyList(),
    itemContent: @Composable (T) -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val startPadding = contentPadding.calculateStartPadding(layoutDirection)
    val endPadding = contentPadding.calculateEndPadding(layoutDirection)
    val topPadding = contentPadding.calculateTopPadding()
    val bottomPadding = contentPadding.calculateBottomPadding()

    // Group items into chunks for lazy loading
    val chunkSize = columns * 4 // Load 4 rows at a time

    val pinnedChunks by remember(pinnedItems, columns) {
        derivedStateOf {
            pinnedItems.chunked(chunkSize)
        }
    }

    val regularChunks by remember(items, columns) {
        derivedStateOf {
            items.chunked(chunkSize)
        }
    }

    LazyColumn(
        state = state,
        contentPadding = PaddingValues(
            start = startPadding,
            end = endPadding,
            top = topPadding,
            bottom = bottomPadding
        ),
        modifier = modifier
    ) {
        // Pinned section
        if (pinnedItems.isNotEmpty() && sectionHeader != null) {
            item(key = "pinned_header") {
                sectionHeader("Pinned")
            }
        }

        pinnedChunks.forEachIndexed { chunkIndex, chunk ->
            item(key = "pinned_chunk_$chunkIndex") {
                MasonryChunk(
                    items = chunk,
                    columns = columns,
                    horizontalSpacing = horizontalSpacing,
                    verticalSpacing = verticalSpacing,
                    key = key,
                    itemContent = itemContent
                )
            }
        }

        // Regular items section
        if (regularChunks.isNotEmpty() && pinnedItems.isNotEmpty() && sectionHeader != null) {
            item(key = "others_header") {
                sectionHeader("Others")
            }
        }

        regularChunks.forEachIndexed { chunkIndex, chunk ->
            item(key = "chunk_$chunkIndex") {
                MasonryChunk(
                    items = chunk,
                    columns = columns,
                    horizontalSpacing = horizontalSpacing,
                    verticalSpacing = verticalSpacing,
                    key = key,
                    itemContent = itemContent
                )
            }
        }
    }
}

@Composable
private fun <T : Any> MasonryChunk(
    items: List<T>,
    columns: Int,
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    key: ((T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit
) {
    Layout(
        content = {
            items.forEach { item ->
                Box(modifier = Modifier.padding(bottom = verticalSpacing)) {
                    itemContent(item)
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) { measurables, constraints ->
        val spacing = horizontalSpacing.roundToPx()
        val totalSpacing = spacing * (columns - 1)
        val itemWidth = (constraints.maxWidth - totalSpacing) / columns

        val itemConstraints = constraints.copy(
            minWidth = itemWidth,
            maxWidth = itemWidth
        )

        val placeables = measurables.map { it.measure(itemConstraints) }
        val columnHeights = IntArray(columns) { 0 }
        val placements = mutableListOf<ItemPlacement>()

        placeables.forEach { placeable ->
            val column = columnHeights.withIndex().minByOrNull { it.value }?.index ?: 0
            val x = column * (itemWidth + spacing)
            val y = columnHeights[column]

            placements.add(ItemPlacement(x, y, placeable))
            columnHeights[column] += placeable.height
        }

        val totalHeight = columnHeights.maxOrNull() ?: 0

        layout(constraints.maxWidth, totalHeight) {
            placements.forEach { placement ->
                placement.placeable.placeRelative(placement.x, placement.y)
            }
        }
    }
}
