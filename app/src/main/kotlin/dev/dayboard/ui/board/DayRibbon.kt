package dev.dayboard.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.RibbonSegment
import dev.dayboard.engine.model.SegmentState
import dev.dayboard.ui.theme.colorFor

/**
 * The whole waking day in one draw node: a handful of drawRect calls in a single
 * Canvas, so redrawing it once a second costs effectively nothing.
 */
@Composable
fun DayRibbon(segments: List<RibbonSegment>, nowFraction: Float, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(30.dp)) {
        segments.forEach { segment ->
            val left = segment.startFraction * size.width
            val width = ((segment.endFraction - segment.startFraction) * size.width).coerceAtLeast(1f)
            drawRect(
                color = colorFor(segment.colorRole).copy(
                    alpha = when (segment.state) {
                        SegmentState.PAST -> 1f
                        SegmentState.CURRENT -> 1f
                        SegmentState.FUTURE -> 0.35f
                    }
                ),
                topLeft = Offset(left, 0f),
                size = Size(width, size.height)
            )
        }
        val needle = nowFraction * size.width
        drawRect(
            color = Color.White,
            topLeft = Offset(needle - 1.5f, 0f),
            size = Size(3f, size.height)
        )
    }
}
