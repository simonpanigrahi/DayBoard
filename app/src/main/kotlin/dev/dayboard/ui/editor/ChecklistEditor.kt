package dev.dayboard.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import dev.dayboard.ui.theme.SystemRed

@Composable
fun ChecklistEditor(
    items: List<ItemDraft>,
    onText: (Long, String) -> Unit,
    onRemove: (Long) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CheckboxGlyph(tint = LabelTertiary, checked = false, size = 22.dp)
                OutlinedTextField(
                    value = item.text,
                    onValueChange = { onText(item.key, it) },
                    singleLine = true,
                    placeholder = { Text("Checklist item", color = LabelTertiary) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = RoundedCornerShape(16.dp),
                    colors = boardFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                GhostButton("Remove", accent = SystemRed) { onRemove(item.key) }
            }
        }
        GhostButton("+ Checklist item", accent = SystemBlue, onClick = onAdd, modifier = Modifier.padding(top = 2.dp))
    }
}
