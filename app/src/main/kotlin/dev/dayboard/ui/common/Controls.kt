package dev.dayboard.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.dayboard.ui.theme.BoardBlack
import dev.dayboard.ui.theme.BoardHairline
import dev.dayboard.ui.theme.BoardSurface
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelTertiary

enum class Tone {
    /** The one thing to do right now. Solid colour, dark text. */
    Filled,

    /** Available and coloured, but not shouting: the accent at low opacity. */
    Tinted,

    /** Everything else on the bar: a raised slab of the elevation ramp. */
    Glass,

    /** Navigation and dismissal: no fill at all. */
    Plain
}

/**
 * Presses answer immediately with a small spring, which is most of what makes a control
 * feel handled rather than merely tapped.
 */
@Composable
fun ActionButton(
    label: String,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Glass,
    accent: Color = LabelPrimary,
    enabled: Boolean = true,
    height: Dp = 84.dp,
    corner: Dp = 22.dp,
    glyph: (@Composable (Color) -> Unit)? = null,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.965f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow),
        label = "press"
    )

    val container = when {
        !enabled -> BoardSurface
        tone == Tone.Filled -> accent
        tone == Tone.Tinted -> accent.copy(alpha = 0.18f)
        tone == Tone.Glass -> BoardSurface
        else -> Color.Transparent
    }
    val content = when {
        !enabled -> LabelTertiary
        tone == Tone.Filled -> BoardBlack
        tone == Tone.Tinted || tone == Tone.Plain -> accent
        else -> LabelPrimary
    }

    Box(
        modifier
            .scale(scale)
            .height(height)
            .clip(RoundedCornerShape(corner))
            .background(container)
            .then(
                if (tone == Tone.Glass && enabled) Modifier.border(Dp.Hairline, BoardHairline, RoundedCornerShape(corner))
                else Modifier
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 22.dp)
        ) {
            glyph?.invoke(content)
            Text(label, style = MaterialTheme.typography.labelLarge, color = content)
        }
    }
}

/** A square button carrying a drawn glyph and nothing else. */
@Composable
fun GlyphButton(
    modifier: Modifier = Modifier,
    accent: Color = LabelPrimary,
    enabled: Boolean = true,
    size: Dp = 64.dp,
    corner: Dp = 16.dp,
    glyph: @Composable (Color) -> Unit,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow),
        label = "press"
    )
    Box(
        modifier
            .scale(scale)
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(BoardSurface)
            .border(Dp.Hairline, BoardHairline, RoundedCornerShape(corner))
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        glyph(if (enabled) accent else LabelTertiary)
    }
}

/** Header and navigation: text only, at a comfortable target size. */
@Composable
fun GhostButton(
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = LabelPrimary,
    enabled: Boolean = true,
    height: Dp = 72.dp,
    onClick: () -> Unit
) = ActionButton(
    label = label,
    modifier = modifier,
    tone = Tone.Plain,
    accent = accent,
    enabled = enabled,
    height = height,
    onClick = onClick
)

@Composable
fun BorderHairline() = BorderStroke(Dp.Hairline, BoardHairline)
