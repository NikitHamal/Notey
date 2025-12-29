package com.notex.sd.ui.components.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay

/**
 * Focus Mode theme options.
 */
enum class FocusTheme(val displayName: String) {
    DARK("Night"),
    LIGHT("Day"),
    SEPIA("Sepia"),
    DEEP_DARK("Deep Dark")
}

/**
 * Focus Mode state container.
 */
data class FocusModeState(
    val isActive: Boolean = false,
    val theme: FocusTheme = FocusTheme.DARK,
    val showTimer: Boolean = false,
    val timerMinutes: Int = 25,
    val ambientSoundEnabled: Boolean = false,
    val wordGoal: Int? = null,
    val sessionStartTime: Long = 0L,
    val wordsWritten: Int = 0
)

/**
 * Colors for different focus themes.
 */
private fun getFocusColors(theme: FocusTheme): FocusColors {
    return when (theme) {
        FocusTheme.DARK -> FocusColors(
            background = Color(0xFF1A1A1A),
            surface = Color(0xFF242424),
            text = Color(0xFFE0E0E0),
            textSecondary = Color(0xFF888888),
            accent = Color(0xFF7C4DFF)
        )
        FocusTheme.LIGHT -> FocusColors(
            background = Color(0xFFFAFAFA),
            surface = Color(0xFFFFFFFF),
            text = Color(0xFF1A1A1A),
            textSecondary = Color(0xFF666666),
            accent = Color(0xFF6750A4)
        )
        FocusTheme.SEPIA -> FocusColors(
            background = Color(0xFFF5E6D3),
            surface = Color(0xFFFFF8F0),
            text = Color(0xFF5D4037),
            textSecondary = Color(0xFF8D6E63),
            accent = Color(0xFF6D4C41)
        )
        FocusTheme.DEEP_DARK -> FocusColors(
            background = Color(0xFF000000),
            surface = Color(0xFF121212),
            text = Color(0xFFD0D0D0),
            textSecondary = Color(0xFF666666),
            accent = Color(0xFF00BCD4)
        )
    }
}

private data class FocusColors(
    val background: Color,
    val surface: Color,
    val text: Color,
    val textSecondary: Color,
    val accent: Color
)

/**
 * Full-screen distraction-free writing mode.
 * Key USP feature that provides a clean, immersive writing experience.
 *
 * Features:
 * - Multiple theme options (Dark, Light, Sepia, Deep Dark)
 * - Optional focus timer (Pomodoro-style)
 * - Word count progress tracking
 * - Minimal UI with fade-in controls
 * - System bar hiding for true immersion
 */
@Composable
fun FocusModeEditor(
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onExitFocusMode: () -> Unit,
    state: FocusModeState = FocusModeState(isActive = true),
    onStateChange: (FocusModeState) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = remember(state.theme) { getFocusColors(state.theme) }
    val focusRequester = remember { FocusRequester() }

    // Control visibility for UI elements on interaction
    var showControls by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Timer state
    var timerSeconds by remember { mutableLongStateOf(state.timerMinutes * 60L) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Word count animation
    val wordCount = remember(content) {
        if (content.isBlank()) 0 else content.split(Regex("\\s+")).size
    }

    // Auto-hide controls after inactivity
    LaunchedEffect(lastInteractionTime) {
        delay(3000)
        showControls = false
    }

    // Timer logic
    LaunchedEffect(isTimerRunning, timerSeconds) {
        if (isTimerRunning && timerSeconds > 0) {
            delay(1000)
            timerSeconds--
        } else if (timerSeconds <= 0 && isTimerRunning) {
            isTimerRunning = false
            // Timer complete - could trigger a notification
        }
    }

    // Auto-focus on content field
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Handle back press
    BackHandler(enabled = true) {
        onExitFocusMode()
    }

    val controlsAlpha by animateFloatAsState(
        targetValue = if (showControls) 1f else 0f,
        animationSpec = tween(300),
        label = "controlsAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
                lastInteractionTime = System.currentTimeMillis()
            }
    ) {
        // Main content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Title
            BasicTextField(
                value = title,
                onValueChange = {
                    onTitleChange(it)
                    lastInteractionTime = System.currentTimeMillis()
                    showControls = true
                },
                textStyle = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    lineHeight = 40.sp
                ),
                cursorBrush = SolidColor(colors.accent),
                decorationBox = { innerTextField ->
                    Box {
                        if (title.isEmpty()) {
                            Text(
                                text = "Title",
                                style = TextStyle(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary.copy(alpha = 0.4f)
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Content
            BasicTextField(
                value = content,
                onValueChange = {
                    onContentChange(it)
                    lastInteractionTime = System.currentTimeMillis()
                    showControls = true
                },
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.text,
                    lineHeight = 28.sp
                ),
                cursorBrush = SolidColor(colors.accent),
                decorationBox = { innerTextField ->
                    Box {
                        if (content.isEmpty()) {
                            Text(
                                text = "Start writing...",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    color = colors.textSecondary.copy(alpha = 0.4f)
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            Spacer(modifier = Modifier.height(200.dp))
        }

        // Top controls bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            FocusModeTopBar(
                onClose = onExitFocusMode,
                theme = state.theme,
                onThemeChange = { onStateChange(state.copy(theme = it)) },
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        // Bottom stats bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FocusModeBottomBar(
                wordCount = wordCount,
                timerSeconds = timerSeconds,
                isTimerRunning = isTimerRunning,
                onTimerToggle = { isTimerRunning = !isTimerRunning },
                onTimerReset = { timerSeconds = state.timerMinutes * 60L },
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        // Word goal progress (if set)
        state.wordGoal?.let { goal ->
            val progress = (wordCount.toFloat() / goal).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .alpha(controlsAlpha)
            ) {
                CircularWordProgress(
                    progress = progress,
                    wordCount = wordCount,
                    goal = goal,
                    accentColor = colors.accent,
                    textColor = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun FocusModeTopBar(
    onClose: () -> Unit,
    theme: FocusTheme,
    onThemeChange: (FocusTheme) -> Unit,
    colors: FocusColors,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface.copy(alpha = 0.9f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close button
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit focus mode",
                    tint = colors.text
                )
            }

            // Theme toggles
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeToggleButton(
                    isSelected = theme == FocusTheme.LIGHT,
                    icon = Icons.Default.LightMode,
                    onClick = { onThemeChange(FocusTheme.LIGHT) },
                    colors = colors
                )

                ThemeToggleButton(
                    isSelected = theme == FocusTheme.DARK,
                    icon = Icons.Default.NightsStay,
                    onClick = { onThemeChange(FocusTheme.DARK) },
                    colors = colors
                )
            }

            // Ambient sound toggle (placeholder)
            IconButton(onClick = { /* Toggle ambient sounds */ }) {
                Icon(
                    imageVector = Icons.Default.VolumeOff,
                    contentDescription = "Ambient sounds",
                    tint = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ThemeToggleButton(
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    colors: FocusColors
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) colors.accent.copy(alpha = 0.2f) else Color.Transparent
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .size(20.dp),
            tint = if (isSelected) colors.accent else colors.textSecondary
        )
    }
}

@Composable
private fun FocusModeBottomBar(
    wordCount: Int,
    timerSeconds: Long,
    isTimerRunning: Boolean,
    onTimerToggle: () -> Unit,
    onTimerReset: () -> Unit,
    colors: FocusColors,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface.copy(alpha = 0.9f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Word count
            Column {
                Text(
                    text = "$wordCount",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.text
                )
                Text(
                    text = "words",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }

            // Timer display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = onTimerToggle,
                    shape = RoundedCornerShape(12.dp),
                    color = if (isTimerRunning) colors.accent.copy(alpha = 0.2f) else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isTimerRunning) colors.accent else colors.textSecondary
                        )

                        Text(
                            text = formatTime(timerSeconds),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (isTimerRunning) colors.accent else colors.text
                        )
                    }
                }

                if (timerSeconds < (25 * 60)) {
                    Surface(
                        onClick = onTimerReset,
                        shape = CircleShape,
                        color = colors.surface
                    ) {
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Session indicator
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = colors.accent
                )
                Text(
                    text = "Focus",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun CircularWordProgress(
    progress: Float,
    wordCount: Int,
    goal: Int,
    accentColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progressAnimation"
    )

    Box(
        modifier = modifier.size(60.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawCircle(
                color = accentColor.copy(alpha = 0.1f),
                radius = size.minDimension / 2
            )

            // Progress arc
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = 360 * animatedProgress,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 4.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$wordCount",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = textColor
            )
            Text(
                text = "/$goal",
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.5f)
            )
        }
    }
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

/**
 * Compact focus mode indicator button for the editor toolbar.
 */
@Composable
fun FocusModeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        modifier = modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NightsStay,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = "Focus",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
