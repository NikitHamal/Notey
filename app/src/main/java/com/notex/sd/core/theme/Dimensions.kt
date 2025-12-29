package com.notex.sd.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design system dimensions for consistent spacing and sizing across the app.
 * Following Material 3 8dp grid system with modifications for compactness.
 */
data class Dimensions(
    // Base spacing units
    val spaceNone: Dp = 0.dp,
    val spaceXXSmall: Dp = 2.dp,
    val spaceXSmall: Dp = 4.dp,
    val spaceSmall: Dp = 8.dp,
    val spaceMedium: Dp = 12.dp,
    val spaceDefault: Dp = 16.dp,
    val spaceLarge: Dp = 20.dp,
    val spaceXLarge: Dp = 24.dp,
    val spaceXXLarge: Dp = 32.dp,
    val spaceHuge: Dp = 48.dp,
    val spaceGiant: Dp = 64.dp,

    // Card dimensions
    val cardMinWidth: Dp = 160.dp,
    val cardMaxWidth: Dp = 280.dp,
    val cardPadding: Dp = 14.dp,
    val cardPaddingCompact: Dp = 12.dp,
    val cardCornerRadius: Dp = 16.dp,
    val cardCornerRadiusSmall: Dp = 12.dp,
    val cardElevation: Dp = 0.dp,
    val cardElevationPressed: Dp = 2.dp,

    // Note card specific
    val noteCardGap: Dp = 10.dp,
    val noteCardContentGap: Dp = 8.dp,
    val noteCardTitleMaxLines: Int = 2,
    val noteCardPreviewMaxLinesGrid: Int = 8,
    val noteCardPreviewMaxLinesList: Int = 2,

    // Icon sizes
    val iconSizeXSmall: Dp = 12.dp,
    val iconSizeSmall: Dp = 16.dp,
    val iconSizeMedium: Dp = 20.dp,
    val iconSizeDefault: Dp = 24.dp,
    val iconSizeLarge: Dp = 32.dp,
    val iconSizeXLarge: Dp = 40.dp,
    val iconSizeHuge: Dp = 48.dp,

    // Color indicator
    val colorIndicatorSize: Dp = 8.dp,
    val colorIndicatorSizeLarge: Dp = 12.dp,
    val colorPickerItemSize: Dp = 40.dp,

    // Bottom sheet
    val bottomSheetCornerRadius: Dp = 24.dp,
    val bottomSheetDragHandle: Dp = 4.dp,
    val bottomSheetDragHandleWidth: Dp = 36.dp,

    // FAB
    val fabSize: Dp = 56.dp,
    val fabSizeSmall: Dp = 40.dp,
    val fabCornerRadius: Dp = 16.dp,

    // Top bar
    val topBarHeight: Dp = 64.dp,
    val topBarHeightLarge: Dp = 152.dp,

    // Search bar
    val searchBarHeight: Dp = 48.dp,
    val searchBarCornerRadius: Dp = 24.dp,

    // Toolbar
    val toolbarButtonSize: Dp = 40.dp,
    val toolbarIconSize: Dp = 22.dp,
    val toolbarDividerWidth: Dp = 1.dp,
    val toolbarHeight: Dp = 48.dp,

    // Editor
    val editorTitleFontSize: Int = 24,
    val editorContentFontSize: Int = 16,
    val editorLineHeight: Int = 24,
    val editorPaddingHorizontal: Dp = 16.dp,
    val editorPaddingVertical: Dp = 16.dp,

    // Grid layout
    val gridColumns: Int = 2,
    val gridSpacing: Dp = 10.dp,
    val masonryMinItemWidth: Dp = 160.dp,

    // Animations
    val animationDurationFast: Int = 150,
    val animationDurationMedium: Int = 250,
    val animationDurationSlow: Int = 400,

    // Touch targets
    val minTouchTarget: Dp = 48.dp,
    val minTouchTargetSmall: Dp = 40.dp,

    // Dividers
    val dividerThickness: Dp = 0.5.dp,
    val dividerThicknessMedium: Dp = 1.dp,

    // Chips
    val chipHeight: Dp = 32.dp,
    val chipHeightSmall: Dp = 28.dp,
    val chipCornerRadius: Dp = 8.dp,
    val chipPaddingHorizontal: Dp = 12.dp,
    val chipPaddingVertical: Dp = 6.dp,

    // Buttons
    val buttonHeight: Dp = 48.dp,
    val buttonHeightSmall: Dp = 36.dp,
    val buttonCornerRadius: Dp = 12.dp,

    // Lists
    val listItemHeight: Dp = 56.dp,
    val listItemHeightCompact: Dp = 48.dp,
    val listItemPadding: Dp = 16.dp,

    // Navigation
    val drawerWidth: Dp = 280.dp,
    val navigationRailWidth: Dp = 72.dp,

    // Backlinks panel
    val backlinksPreviewMaxLines: Int = 2,
    val backlinksItemPadding: Dp = 12.dp,

    // Progress indicators
    val progressIndicatorSize: Dp = 24.dp,
    val progressIndicatorStroke: Dp = 2.dp
)

val LocalDimensions = staticCompositionLocalOf { Dimensions() }

/**
 * Access app dimensions through this object
 */
object AppDimensions {
    val current: Dimensions
        @Composable
        get() = LocalDimensions.current
}
