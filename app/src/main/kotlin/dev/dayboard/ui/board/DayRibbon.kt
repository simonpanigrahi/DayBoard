package dev.dayboard.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
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
import dev.dayboard.ui.theme.BoardSurfaceHigh
import dev.dayboard.ui.theme.Inter
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelTertiary
import dev.dayboard.ui.theme.colorFor
import kotlinx.coroutines.delay

private const val NO_POINTER = -1f
private const val LINGER_MS = 1800L

/**
 * The whole waking day as one draw node, and the only place you can ask what a colour is.
 *
 * Hovering with a pen or a mouse, or pressing and sliding a finger along it, names the
 * block under the pointer in the band the hour labels usually occupy — so the answer
 * costs no extra height and the layout never jumps.
 */
@Composable
fun DayRibbon(
    segments: List<RibbonSegment>,
    window: RibbonWindow,
    nowFraction: Float,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    // Written by the pointer handler, read inside the draw lambda: a draw-phase read, so
    // scrubbing repaints without recomposing anything.
    val pointerX = remember { mutableFloatStateOf(NO_POINTER) }
    // A tap should answer the question too, so the name lingers after the finger lifts.
    var lifted by remember { mutableIntStateOf(0) }
    LaunchedEffect(lifted) {
        if (lifted > 0) {
            delay(LINGER_MS)
            pointerX.floatValue = NO_POINTER
        }
    }

    val hourStyle = TextStyle(
        color = LabelTertiary, fontSize = 13.sp, fontFamily = Inter,
        fontWeight = FontWeight.Medium, letterSpacing = 0.04.em
    )
    val calloutStyle = TextStyle(
        color = LabelPrimary, fontSize = 15.sp, fontFamily = Inter,
        fontWeight = FontWeight.Medium, letterSpacing = (-0.005).em
    )

    Canvas(
        modifier
            .fillMaxWidth()
            .height(72.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            // A pen or mouse leaving the ribbon means the question is over.
                            PointerEventType.Exit -> pointerX.floatValue = NO_POINTER
                            PointerEventType.Release -> lifted++
                            else -> pointerX.floatValue =
                                event.changes.lastOrNull()?.position?.x ?: NO_POINTER
                        }
                    }
                }
            }
    ) {
        val barHeight = 30.dp.toPx()
        val radius = CornerRadius(barHeight / 2f)
        val pointer = pointerX.floatValue
        val hovered = segments.firstOrNull {
            pointer >= 0f && pointer >= it.startFraction * size.width && pointer <= it.endFraction * size.width
        }

        drawRoundRect(Color.White.copy(alpha = 0.05f), size = Size(size.width, barHeight), cornerRadius = radius)

        segments.forEach { segment ->
            val left = segment.startFraction * size.width
            val width = ((segment.endFraction - segment.startFraction) * size.width).coerceAtLeast(barHeight)
            val color = colorFor(segment.colorRole)
            drawRoundRect(
                color = color.copy(
                    alpha = when {
                        segment === hovered -> 1f
                        segment.state == SegmentState.CURRENT -> 1f
                        segment.state == SegmentState.PAST -> 0.42f
                        else -> 0.24f
                    }
                ),
                topLeft = Offset(left, 0f),
                size = Size(width, barHeight),
                cornerRadius = radius
            )
            if (segment === hovered) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(left, 0f),
                    size = Size(width, barHeight),
                    cornerRadius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        drawHourMarks(window, barHeight)

        if (hovered == null) {
            drawHourLabels(measurer, window, barHeight, hourStyle)
        } else {
            drawCallout(measurer, hovered, pointer, barHeight, calloutStyle)
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

private fun DrawScope.drawHourMarks(window: RibbonWindow, barHeight: Float) {
    if (window.minutes <= 0) return
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
    }
}

private fun DrawScope.drawHourLabels(
    measurer: TextMeasurer,
    window: RibbonWindow,
    barHeight: Float,
    style: TextStyle
) {
    if (window.minutes <= 0) return
    val firstHour = (window.startMinute + 59) / 60
    val lastHour = window.endMinute / 60
    (firstHour..lastHour).filter { it % 2 == 0 }.forEach { hour ->
        val x = (hour * 60 - window.startMinute).toFloat() / window.minutes * size.width
        val label = measurer.measure("%02d".format(hour % 24), style)
        drawText(label, topLeft = Offset(x - label.size.width / 2f, barHeight + 22f))
    }
}

/** A pill under the pointer naming the block, clamped so it never runs off either end. */
private fun DrawScope.drawCallout(
    measurer: TextMeasurer,
    segment: RibbonSegment,
    pointer: Float,
    barHeight: Float,
    style: TextStyle
) {
    val text = "${segment.title}   ${formatClock(segment.start)} – ${formatClock(segment.end)}   ${segment.minutes}m"
    val measured = measurer.measure(text, style, maxLines = 1)
    val padding = 14.dp.toPx()
    val pillWidth = measured.size.width + padding * 2
    val pillHeight = measured.size.height + 10.dp.toPx()
    val left = (pointer - pillWidth / 2f).coerceIn(0f, (size.width - pillWidth).coerceAtLeast(0f))
    val top = barHeight + 10f

    drawRoundRect(
        color = BoardSurfaceHigh,
        topLeft = Offset(left, top),
        size = Size(pillWidth, pillHeight),
        cornerRadius = CornerRadius(pillHeight / 2f)
    )
    drawRoundRect(
        color = colorFor(segment.colorRole).copy(alpha = 0.55f),
        topLeft = Offset(left, top),
        size = Size(pillWidth, pillHeight),
        cornerRadius = CornerRadius(pillHeight / 2f),
        style = Stroke(width = 1.dp.toPx())
    )
    drawText(measured, topLeft = Offset(left + padding, top + 5.dp.toPx()))
}
