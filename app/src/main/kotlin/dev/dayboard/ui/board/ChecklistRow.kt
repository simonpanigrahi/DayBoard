package dev.dayboard.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.ChecklistItem
import dev.dayboard.ui.common.CheckboxGlyph
import dev.dayboard.ui.theme.BoardSurfaceHigh
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelTertiary

/**
 * The pill is drawn small so it stays subordinate to the countdown, but the row around
 * it keeps the full 72dp target, so what you hit is bigger than what you see.
 */
@Composable
fun ChecklistRow(
    items: List<ChecklistItem>,
    checkedIds: Set<Long>,
    accent: Color,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.horizontalGradient(
                        0.92f to Color.Black,
                        1f to Color.Transparent,
                        startX = 0f,
                        endX = size.width
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val checked = item.id in checkedIds
            Box(
                modifier = Modifier.height(72.dp).clickable { onToggle(item.id) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(if (checked) accent.copy(alpha = 0.16f) else BoardSurfaceHigh)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    CheckboxGlyph(tint = if (checked) accent else LabelTertiary, checked = checked, size = 19.dp)
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (checked) LabelTertiary else LabelPrimary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
