package dev.dayboard.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.ui.board.formatClock
import dev.dayboard.ui.common.ActionButton
import dev.dayboard.ui.common.GhostButton
import dev.dayboard.ui.common.Tone
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelSecondary
import dev.dayboard.ui.theme.LabelTertiary
import dev.dayboard.ui.theme.SystemBlue

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
            .padding(horizontal = 34.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Today's plan", style = MaterialTheme.typography.displaySmall, color = LabelPrimary)
                Text(
                    text = "Drag to reorder. Flow blocks chain from the day's start.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LabelSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            GhostButton("Board", accent = SystemBlue, onClick = actions.onBack)
            GhostButton("Paste a day", accent = SystemBlue, onClick = actions.onImport)
            ActionButton(
                label = "ADD BLOCK",
                modifier = Modifier.padding(start = 10.dp),
                height = 76.dp,
                onClick = actions.onAdd
            )
            ActionButton(
                label = "REVIEW",
                modifier = Modifier.padding(start = 10.dp),
                tone = Tone.Filled,
                accent = SystemBlue,
                height = 76.dp,
                onClick = actions.onReview
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("DAY STARTS", style = MaterialTheme.typography.labelMedium, color = LabelTertiary)
            ActionButton("−", Modifier.size(64.dp), height = 64.dp, corner = 18.dp) { actions.onShiftDayStart(-15) }
            Text(
                text = formatClock(state.dayStart),
                style = MaterialTheme.typography.titleLarge,
                color = LabelPrimary,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            ActionButton("+", Modifier.size(64.dp), height = 64.dp, corner = 18.dp) { actions.onShiftDayStart(15) }
            GhostButton("Start now", accent = SystemBlue, onClick = actions.onDayStartNow)
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
