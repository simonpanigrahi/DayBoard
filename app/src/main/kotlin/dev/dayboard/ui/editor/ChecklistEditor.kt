package dev.dayboard.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.ui.common.CheckboxGlyph
import dev.dayboard.ui.common.GhostButton
import dev.dayboard.ui.common.boardFieldColors
import dev.dayboard.ui.theme.LabelTertiary
import dev.dayboard.ui.theme.SystemBlue

/**
 * Checklist fields line up with the block title above them, with the box sitting in the
 * same gutter as the drag handle rather than pushing every item across.
 */
@Composable
fun ChecklistEditor(
    items: List<ItemDraft>,
    onText: (Long, String) -> Unit,
    onRemove: (Long) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach { item ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(GUTTER), contentAlignment = Alignment.Center) {
                    CheckboxGlyph(tint = LabelTertiary, checked = false, size = 20.dp)
                }
                OutlinedTextField(
                    value = item.text,
                    onValueChange = { onText(item.key, it) },
                    singleLine = true,
                    placeholder = { Text("Checklist item", color = LabelTertiary) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(12.dp),
                    colors = boardFieldColors(),
                    modifier = Modifier.weight(1f).height(56.dp)
                )
                GhostButton("Remove", accent = LabelTertiary, height = 56.dp) { onRemove(item.key) }
            }
        }
        Box(Modifier.padding(start = GUTTER)) {
            GhostButton("+ Checklist item", accent = SystemBlue, height = 56.dp, onClick = onAdd)
        }
    }
}
