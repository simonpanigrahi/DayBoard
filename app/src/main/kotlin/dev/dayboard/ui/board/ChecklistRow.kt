package dev.dayboard.ui.board

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.ChecklistItem
import dev.dayboard.ui.theme.BoardDim

/** One tap at a 72dp target ticks an item; the tick is an event, not a field. */
@Composable
fun ChecklistRow(
    items: List<ChecklistItem>,
    checkedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val checked = item.id in checkedIds
            Text(
                text = if (checked) "☑ ${item.text}" else "☐ ${item.text}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (checked) BoardDim else MaterialTheme.colorScheme.onBackground,
                textDecoration = if (checked) TextDecoration.LineThrough else null,
                modifier = Modifier
                    .heightIn(min = 72.dp)
                    .clickable { onToggle(item.id) }
                    .padding(vertical = 20.dp)
            )
        }
    }
}
