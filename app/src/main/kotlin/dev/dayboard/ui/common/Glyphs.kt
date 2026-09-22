package dev.dayboard.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Drawn rather than typed. Emoji carry the system's own colour and shape and read as
 * somebody else's icons; these inherit the tint of the control they sit in.
 */
@Composable
fun PlayGlyph(tint: Color, size: Dp = 24.dp, modifier: Modifier = Modifier) = Glyph(size, modifier) {
    val inset = this.size.width * 0.08f
    val path = Path().apply {
        moveTo(inset, inset * 0.6f)
        lineTo(this@Glyph.size.width - inset, this@Glyph.size.height / 2f)
        lineTo(inset, this@Glyph.size.height - inset * 0.6f)
        close()
    }
    drawPath(path, tint)
    drawPath(path, tint, style = Stroke(width = inset * 1.6f, join = StrokeJoin.Round, cap = StrokeCap.Round))
}

@Composable
fun PauseGlyph(tint: Color, size: Dp = 24.dp, modifier: Modifier = Modifier) = Glyph(size, modifier) {
    val barWidth = this.size.width * 0.26f
    val radius = androidx.compose.ui.geometry.CornerRadius(barWidth * 0.35f)
    drawRoundRect(tint, Offset(this.size.width * 0.10f, 0f), Size(barWidth, this.size.height), radius)
    drawRoundRect(tint, Offset(this.size.width * 0.64f, 0f), Size(barWidth, this.size.height), radius)
}

@Composable
fun CheckGlyph(tint: Color, size: Dp = 24.dp, modifier: Modifier = Modifier) = Glyph(size, modifier) {
    val stroke = this.size.width * 0.16f
    val path = Path().apply {
        moveTo(this@Glyph.size.width * 0.08f, this@Glyph.size.height * 0.55f)
        lineTo(this@Glyph.size.width * 0.38f, this@Glyph.size.height * 0.84f)
        lineTo(this@Glyph.size.width * 0.94f, this@Glyph.size.height * 0.18f)
    }
    drawPath(path, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

@Composable
fun ForwardGlyph(tint: Color, size: Dp = 24.dp, modifier: Modifier = Modifier) = Glyph(size, modifier) {
    val bar = this.size.width * 0.16f
    val triangle = Path().apply {
        moveTo(0f, this@Glyph.size.height * 0.08f)
        lineTo(this@Glyph.size.width - bar * 1.6f, this@Glyph.size.height / 2f)
        lineTo(0f, this@Glyph.size.height * 0.92f)
        close()
    }
    drawPath(triangle, tint)
    drawRoundRect(
        tint,
        Offset(this.size.width - bar, this.size.height * 0.08f),
        Size(bar, this.size.height * 0.84f),
        androidx.compose.ui.geometry.CornerRadius(bar * 0.4f)
    )
}

@Composable
fun CupGlyph(tint: Color, size: Dp = 24.dp, modifier: Modifier = Modifier) = Glyph(size, modifier) {
    val stroke = this.size.width * 0.11f
    val bodyWidth = this.size.width * 0.62f
    val bodyHeight = this.size.height * 0.56f
    val top = this.size.height * 0.16f
    val body = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                Rect(Offset(0f, top), Size(bodyWidth, bodyHeight)),
                bottomLeft = androidx.compose.ui.geometry.CornerRadius(bodyWidth * 0.38f),
                bottomRight = androidx.compose.ui.geometry.CornerRadius(bodyWidth * 0.38f),
                topLeft = androidx.compose.ui.geometry.CornerRadius(bodyWidth * 0.08f),
                topRight = androidx.compose.ui.geometry.CornerRadius(bodyWidth * 0.08f)
            )
        )
    }
    drawPath(body, tint, style = Stroke(width = stroke, join = StrokeJoin.Round))
    drawArc(
        color = tint,
        startAngle = -70f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(bodyWidth - stroke, top + bodyHeight * 0.12f),
        size = Size(this.size.width * 0.34f, bodyHeight * 0.55f),
        style = Stroke(width = stroke, cap = StrokeCap.Round)
    )
    drawLine(
        color = tint,
        start = Offset(0f, this.size.height * 0.92f),
        end = Offset(bodyWidth, this.size.height * 0.92f),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
}

/** A rounded square outline, filled with a tick once the item is done. */
@Composable
fun CheckboxGlyph(tint: Color, checked: Boolean, size: Dp = 26.dp, modifier: Modifier = Modifier) =
    Glyph(size, modifier) {
        val stroke = this.size.width * 0.10f
        val radius = androidx.compose.ui.geometry.CornerRadius(this.size.width * 0.3f)
        if (checked) {
            drawRoundRect(tint, size = this.size, cornerRadius = radius)
            val tick = Path().apply {
                moveTo(this@Glyph.size.width * 0.24f, this@Glyph.size.height * 0.52f)
                lineTo(this@Glyph.size.width * 0.44f, this@Glyph.size.height * 0.72f)
                lineTo(this@Glyph.size.width * 0.78f, this@Glyph.size.height * 0.30f)
            }
            drawPath(tick, Color.Black, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        } else {
            drawRoundRect(
                tint,
                topLeft = Offset(stroke / 2f, stroke / 2f),
                size = Size(this.size.width - stroke, this.size.height - stroke),
                cornerRadius = radius,
                style = Stroke(width = stroke)
            )
        }
    }

@Composable
private fun Glyph(size: Dp, modifier: Modifier, draw: DrawScope.() -> Unit) {
    Canvas(modifier.size(size)) { draw() }
}

@Suppress("unused")
private val pathOperationKeepsImportHonest = PathOperation.Union

/** A chevron, for stepping through days. */
@Composable
fun ChevronGlyph(tint: Color, pointsLeft: Boolean, size: Dp = 22.dp, modifier: Modifier = Modifier) =
    GlyphBox(size, modifier) {
        val stroke = this.size.width * 0.14f
        val near = this.size.width * 0.34f
        val far = this.size.width * 0.72f
        val path = Path().apply {
            moveTo(if (pointsLeft) far else near, this@GlyphBox.size.height * 0.12f)
            lineTo(if (pointsLeft) near else far, this@GlyphBox.size.height * 0.5f)
            lineTo(if (pointsLeft) far else near, this@GlyphBox.size.height * 0.88f)
        }
        drawPath(path, tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }

/** Drag handle: three stacked rules, the way a reorderable list row is grabbed. */
@Composable
fun HandleGlyph(tint: Color, size: Dp = 26.dp, modifier: Modifier = Modifier) = GlyphBox(size, modifier) {
    val thickness = this.size.height * 0.09f
    val gap = this.size.height * 0.26f
    repeat(3) { index ->
        drawRoundRect(
            color = tint,
            topLeft = Offset(0f, this.size.height * 0.22f + gap * index),
            size = Size(this.size.width, thickness),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(thickness / 2f)
        )
    }
}

@Composable
private fun GlyphBox(size: Dp, modifier: Modifier, draw: DrawScope.() -> Unit) {
    Canvas(modifier.size(size)) { draw() }
}
