package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.dayboard.ui.theme.RoleBuffer

/**
 * Reads [progress] inside drawBehind, so a changing fraction repaints one draw node
 * instead of recomposing a progress indicator once a second for the whole second.
 */
@Composable
fun ProgressBar(progress: State<Float>, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(20.dp)
            .drawBehind {
                drawRect(color = RoleBuffer, size = size)
                drawRect(color = color, size = Size(size.width * progress.value, size.height))
            }
    )
}
