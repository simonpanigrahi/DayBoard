package dev.dayboard.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * One card shape for the whole app: the raised step of the elevation ramp, a hairline
 * to separate it from true black, and the block's colour as a wash rather than a stripe,
 * so the accent reads without drawing a border around itself.
 */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    corner: Dp = 30.dp,
    padding: Dp = 30.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(corner)
    Column(
        modifier
            .clip(shape)
            .background(BoardSurface)
            .then(
                if (accent == null) Modifier
                else Modifier.background(
                    Brush.linearGradient(
                        0f to accent.copy(alpha = 0.16f),
                        0.7f to Color.Transparent
                    )
                )
            )
            .border(Dp.Hairline, BoardHairline, shape)
            .padding(padding),
        content = content
    )
}
