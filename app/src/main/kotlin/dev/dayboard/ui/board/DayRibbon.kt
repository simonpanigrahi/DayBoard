package dev.dayboard.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.dayboard.engine.model.RibbonSegment
import dev.dayboard.engine.model.RibbonWindow
import dev.dayboard.engine.model.SegmentState
import dev.dayboard.ui.theme.Inter
import dev.dayboard.ui.theme.LabelTertiary
import dev.dayboard.ui.theme.colorFor

/**
 * The whole waking day in one draw node: capsule segments, hour marks and a needle in a
 * single Canvas, so redrawing it every second costs one repaint and no recomposition.
 */
@Composable
fun DayRibbon(
    segments: List<RibbonSegment>,
    window: RibbonWindow,
    nowFraction: Float,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = LabelTertiary,
        fontSize = 13.sp,
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.04.em
    )

    Canvas(modifier.fillMaxWidth().height(62.dp)) {
        val barHeight = size.height * 0.46f
        val radius = CornerRadius(barHeight / 2f)

        drawRoundRect(
            color = Color.White.copy(alpha = 0.05f),
            size = Size(size.width, barHeight),
            cornerRadius = radius
        )

        segments.forEach { segment ->
            val left = segment.startFraction * size.width
            val width = ((segment.endFraction - segment.startFraction) * size.width).coerceAtLeast(barHeight)
            drawRoundRect(
                color = colorFor(segment.colorRole).copy(
                    alpha = when (segment.state) {
                        SegmentState.PAST -> 0.42f
                        SegmentState.CURRENT -> 1f
                        SegmentState.FUTURE -> 0.24f
                    }
                ),
                topLeft = Offset(left, 0f),
                size = Size(width, barHeight),
                cornerRadius = radius
            )
        }

        if (window.minutes > 0) {
            val firstHour = (window.startMinute + 59) / 60
            val lastHour = window.endMinute / 60
            (firstHour..lastHour).forEach { hour ->
                val x = (hour * 60 - window.startMinute).toFloat() / window.minutes * size.width
                val major = hour % 2 == 0
                drawRoundRect(
                    color = Color.White.copy(alpha = if (major) 0.22f else 0.10f),
                    topLeft = Offset(x, barHeight + 8f),
                    size = Size(2f, if (major) 10f else 6f),
                    cornerRadius = CornerRadius(1f)
                )
                if (major) {
                    val label = measurer.measure("%02d".format(hour % 24), labelStyle)
                    drawText(label, topLeft = Offset(x - label.size.width / 2f, barHeight + 22f))
                }
            }
        }

        val needle = nowFraction * size.width
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(needle - 2f, -5f),
            size = Size(4f, barHeight + 10f),
            cornerRadius = CornerRadius(2f)
        )
        drawCircle(color = Color.White, radius = 6f, center = Offset(needle, -5f))
    }
}
