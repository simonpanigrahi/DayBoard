package dev.dayboard.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.ui.board.formatClock
import dev.dayboard.ui.theme.BoardDim
import dev.dayboard.ui.theme.BoardSurface
import dev.dayboard.ui.theme.colorFor

@Composable
fun BlockRow(
    draft: BlockDraft,
    index: Int,
    dragging: Boolean,
    reorder: ReorderState,
    actions: EditorActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(if (dragging) colorFor(draft.kind.colorRole()).copy(alpha = 0.25f) else BoardSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "☰",
                style = MaterialTheme.typography.headlineMedium,
                color = BoardDim,
                modifier = Modifier
                    .size(72.dp)
                    .padding(20.dp)
                    .pointerInput(index) {
                        detectDragGestures(
                            onDragStart = { reorder.start(index) },
                            onDrag = { change, amount ->
                                change.consume()
                                reorder.drag(amount.y)
                            },
                            onDragEnd = { reorder.end() },
                            onDragCancel = { reorder.end() }
                        )
                    }
            )
            OutlinedTextField(
                value = draft.title,
                onValueChange = { actions.onTitle(draft.key, it) },
                singleLine = true,
                placeholder = { Text("Block title", color = BoardDim) },
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            )
            TextButton(onClick = { actions.onRemove(draft.key) }, modifier = Modifier.heightIn(min = 72.dp)) {
                Text("DELETE", style = MaterialTheme.typography.labelMedium, color = BoardDim)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Stepper("−", Modifier) { actions.onMinutes(draft.key, -5) }
            Text("${draft.minutes}m", style = MaterialTheme.typography.titleMedium)
            Stepper("+", Modifier) { actions.onMinutes(draft.key, 5) }

            TextButton(onClick = { actions.onKind(draft.key, draft.kind.next()) }, modifier = Modifier.heightIn(min = 72.dp)) {
                Text(draft.kind.name, style = MaterialTheme.typography.labelMedium, color = colorFor(draft.kind.colorRole()))
            }
            TextButton(onClick = { actions.onToggleFixed(draft.key) }, modifier = Modifier.heightIn(min = 72.dp)) {
                Text(if (draft.fixed) "FIXED" else "FLOW", style = MaterialTheme.typography.labelMedium)
            }
            if (draft.fixed) {
                Stepper("−", Modifier) { actions.onShiftStart(draft.key, -15) }
                Text(formatClock(draft.startLocal ?: java.time.LocalTime.of(9, 0)), style = MaterialTheme.typography.titleMedium)
                Stepper("+", Modifier) { actions.onShiftStart(draft.key, 15) }
            }
        }

        ChecklistEditor(
            items = draft.items,
            onText = { itemKey, text -> actions.onItemText(draft.key, itemKey, text) },
            onRemove = { itemKey -> actions.onRemoveItem(draft.key, itemKey) },
            onAdd = { actions.onAddItem(draft.key) }
        )
    }
}

@Composable
private fun Stepper(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = modifier.size(72.dp)) {
        Text(label, style = MaterialTheme.typography.titleLarge)
    }
}

private fun BlockKind.next(): BlockKind = BlockKind.entries[(ordinal + 1) % BlockKind.entries.size]
