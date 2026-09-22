package dev.dayboard.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.ui.theme.BoardDim

@Composable
fun ChecklistEditor(
    items: List<ItemDraft>,
    onText: (Long, String) -> Unit,
    onRemove: (Long) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth().padding(start = 24.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("☐", color = BoardDim, style = MaterialTheme.typography.bodyLarge)
                OutlinedTextField(
                    value = item.text,
                    onValueChange = { onText(item.key, it) },
                    singleLine = true,
                    placeholder = { Text("Checklist item", color = BoardDim) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                )
                TextButton(onClick = { onRemove(item.key) }, modifier = Modifier.heightIn(min = 72.dp)) {
                    Text("×", style = MaterialTheme.typography.titleLarge, color = BoardDim)
                }
            }
        }
        TextButton(onClick = onAdd, modifier = Modifier.heightIn(min = 72.dp)) {
            Text("+ CHECKLIST ITEM", style = MaterialTheme.typography.labelMedium, color = BoardDim)
        }
    }
}
