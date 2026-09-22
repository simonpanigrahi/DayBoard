package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reads [progress] inside drawBehind, so a changing fraction repaints one draw node
 * rather than recomposing an indicator once a second, and the fill keeps round caps at
 * both ends the way a capsule does.
 */
@Composable
fun ProgressBar(
    progress: State<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .drawBehind {
                val radius = CornerRadius(size.height / 2f)
                drawRoundRect(color = Color.White.copy(alpha = 0.10f), size = size, cornerRadius = radius)
                val filled = size.width * progress.value.coerceIn(0f, 1f)
                if (filled > 0f) {
                    drawRoundRect(
                        color = color,
                        size = Size(filled.coerceAtLeast(size.height), size.height),
                        cornerRadius = radius
                    )
                }
            }
    )
}
