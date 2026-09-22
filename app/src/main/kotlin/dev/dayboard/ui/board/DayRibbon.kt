package dev.dayboard.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dayboard.engine.model.RibbonSegment
import dev.dayboard.engine.model.RibbonWindow
import dev.dayboard.engine.model.SegmentState
import dev.dayboard.ui.theme.BoardDim
import dev.dayboard.ui.theme.colorFor

/**
 * The whole waking day as one draw node: a handful of drawRects, hour ticks and a now
 * needle in a single Canvas, so redrawing it every second is effectively free.
 */
@Composable
fun DayRibbon(
    segments: List<RibbonSegment>,
    window: RibbonWindow,
    nowFraction: Float,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = BoardDim, fontSize = 15.sp)

    Canvas(modifier.fillMaxWidth().height(58.dp)) {
        val barHeight = size.height - 24f - 14f

        segments.forEach { segment ->
            val left = segment.startFraction * size.width
            val width = ((segment.endFraction - segment.startFraction) * size.width).coerceAtLeast(2f)
            drawRect(
                color = colorFor(segment.colorRole).copy(
                    alpha = when (segment.state) {
                        SegmentState.PAST -> 0.55f
                        SegmentState.CURRENT -> 1f
                        SegmentState.FUTURE -> 0.3f
                    }
                ),
                topLeft = Offset(left, 0f),
                size = Size(width, barHeight)
            )
        }

        if (window.minutes > 0) {
            val firstHour = (window.startMinute + 59) / 60
            val lastHour = window.endMinute / 60
            (firstHour..lastHour).forEach { hour ->
                val fraction = (hour * 60 - window.startMinute).toFloat() / window.minutes
                val x = fraction * size.width
                drawRect(
                    color = Color.White.copy(alpha = 0.16f),
                    topLeft = Offset(x, 0f),
                    size = Size(1f, barHeight)
                )
                if (hour % 2 == 0) {
                    val label = "%02d".format(hour % 24)
                    val measured = measurer.measure(label, labelStyle)
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(x - measured.size.width / 2f, barHeight + 8f)
                    )
                }
            }
        }

        val needle = nowFraction * size.width
        drawRect(color = Color.White, topLeft = Offset(needle - 2f, -4f), size = Size(4f, barHeight + 8f))
    }
}
