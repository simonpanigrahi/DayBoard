package dev.dayboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BoardColors = darkColorScheme(
    primary = BoardAmber,
    onPrimary = BoardBlack,
    secondary = RoleDeep,
    onSecondary = BoardBlack,
    background = BoardBlack,
    onBackground = BoardText,
    surface = BoardSurface,
    onSurface = BoardText,
    surfaceVariant = BoardSurface,
    onSurfaceVariant = BoardDim,
    outline = RoleBuffer,
    error = RoleAppointment
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
