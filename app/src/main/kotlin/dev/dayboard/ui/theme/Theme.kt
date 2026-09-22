package dev.dayboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BoardColors = darkColorScheme(
    primary = SystemBlue,
    onPrimary = BoardBlack,
    secondary = SystemTeal,
    onSecondary = BoardBlack,
    background = BoardBlack,
    onBackground = LabelPrimary,
    surface = BoardSurface,
    onSurface = LabelPrimary,
    surfaceVariant = BoardSurfaceHigh,
    onSurfaceVariant = LabelSecondary,
    outline = BoardHairline,
    error = SystemRed,
    onError = BoardBlack
)

/**
 * No dynamic colour: the board's background has to stay true black.
 *
 * The Surface matters as much as the scheme. Without one, LocalContentColor is black,
 * so any Text that does not name a colour renders invisibly on this background.
 */
@Composable
fun DayBoardTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = BoardColors, typography = BoardTypography) {
        Surface(color = BoardColors.background, contentColor = BoardColors.onBackground, content = content)
    }
}
