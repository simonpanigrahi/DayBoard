package dev.dayboard.ui.theme

import androidx.compose.material3.MaterialTheme
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

/** No dynamic colour: the board's background has to stay true black. */
@Composable
fun DayBoardTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = BoardColors, typography = BoardTypography, content = content)
}
