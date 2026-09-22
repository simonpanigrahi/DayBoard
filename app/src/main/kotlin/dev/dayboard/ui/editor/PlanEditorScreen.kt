package dev.dayboard.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.ui.board.formatClock
import dev.dayboard.ui.theme.BoardDim

data class EditorActions(
    val onAdd: () -> Unit,
    val onRemove: (Long) -> Unit,
    val onMove: (Int, Int) -> Unit,
    val onTitle: (Long, String) -> Unit,
    val onMinutes: (Long, Int) -> Unit,
    val onKind: (Long, BlockKind) -> Unit,
    val onColor: (Long) -> Unit,
    val onToggleFixed: (Long) -> Unit,
    val onShiftStart: (Long, Long) -> Unit,
    val onAddItem: (Long) -> Unit,
    val onItemText: (Long, Long, String) -> Unit,
    val onRemoveItem: (Long, Long) -> Unit,
    val onReview: () -> Unit,
    val onImport: () -> Unit,
    val onBack: () -> Unit,
    val onShiftDayStart: (Long) -> Unit,
    val onDayStartNow: () -> Unit
)

@Composable
fun PlanEditorScreen(state: EditorUiState, actions: EditorActions, modifier: Modifier = Modifier) {
    val reorder = rememberReorderState(actions.onMove)

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("TODAY'S PLAN", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = actions.onBack, modifier = Modifier.height(72.dp)) {
                Text("BOARD", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
                onClick = actions.onImport,
                modifier = Modifier.height(72.dp).padding(start = 12.dp)
            ) {
                Text("PASTE A DAY", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
                onClick = actions.onAdd,
                modifier = Modifier.height(72.dp).padding(start = 12.dp)
            ) {
                Text("+ BLOCK", style = MaterialTheme.typography.labelMedium)
            }
            Button(
                onClick = actions.onReview,
                modifier = Modifier.height(72.dp).padding(start = 12.dp)
            ) {
                Text("REVIEW", style = MaterialTheme.typography.labelLarge)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("DAY STARTS", style = MaterialTheme.typography.labelMedium, color = BoardDim)
            TextButton(onClick = { actions.onShiftDayStart(-15) }, modifier = Modifier.height(72.dp)) {
                Text("\u2212", style = MaterialTheme.typography.titleLarge)
            }
            Text(formatClock(state.dayStart), style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { actions.onShiftDayStart(15) }, modifier = Modifier.height(72.dp)) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
            TextButton(onClick = actions.onDayStartNow, modifier = Modifier.height(72.dp)) {
                Text("NOW", style = MaterialTheme.typography.labelMedium, color = BoardDim)
            }
        }

        LazyColumn(
            state = reorder.listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(state.blocks, key = { _, draft -> draft.key }) { index, draft ->
                BlockRow(
                    draft = draft,
                    index = index,
                    dragging = reorder.draggingIndex == index,
                    reorder = reorder,
                    actions = actions
                )
            }
        }
    }
}
