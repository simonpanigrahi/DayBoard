package dev.dayboard.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
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

/** One tap at a 72dp target ticks an item, and the tick is an event, not a field. */
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
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val checked = item.id in checkedIds
            Row(
                modifier = Modifier
                    .heightIn(min = 72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (checked) accent.copy(alpha = 0.14f) else BoardSurfaceHigh)
                    .clickable { onToggle(item.id) }
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CheckboxGlyph(tint = if (checked) accent else LabelTertiary, checked = checked)
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (checked) LabelTertiary else LabelPrimary
                )
            }
        }
    }
}
