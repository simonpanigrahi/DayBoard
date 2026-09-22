package dev.dayboard.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.ui.board.formatClock
import dev.dayboard.ui.common.ActionButton
import dev.dayboard.ui.common.GhostButton
import dev.dayboard.ui.common.HandleGlyph
import dev.dayboard.ui.common.Tone
import dev.dayboard.ui.common.boardFieldColors
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelSecondary
import dev.dayboard.ui.theme.LabelTertiary
import dev.dayboard.ui.theme.Panel
import dev.dayboard.ui.theme.SystemRed
import dev.dayboard.ui.theme.colorFor
import java.time.LocalTime

@Composable
fun BlockRow(
    draft: BlockDraft,
    index: Int,
    dragging: Boolean,
    reorder: ReorderState,
    actions: EditorActions,
    modifier: Modifier = Modifier
) {
    val accent = colorFor(draft.colorRole)
    Panel(modifier.fillMaxWidth(), accent = if (dragging) accent else null, corner = 24.dp, padding = 18.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(72.dp)
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
                    },
                contentAlignment = Alignment.Center
            ) {
                HandleGlyph(if (dragging) accent else LabelTertiary)
            }

            OutlinedTextField(
                value = draft.title,
                onValueChange = { actions.onTitle(draft.key, it) },
                singleLine = true,
                placeholder = { Text("Block title", color = LabelTertiary) },
                textStyle = MaterialTheme.typography.titleMedium,
                shape = RoundedCornerShape(18.dp),
                colors = boardFieldColors(),
                modifier = Modifier.weight(1f).padding(horizontal = 14.dp)
            )

            Box(
                Modifier
                    .size(72.dp)
                    .clickable { actions.onColor(draft.key) },
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(32.dp).clip(CircleShape).background(accent))
            }

            GhostButton("Delete", accent = SystemRed) { actions.onRemove(draft.key) }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 10.dp, start = 72.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Stepper(
                value = "${draft.minutes} min",
                onDown = { actions.onMinutes(draft.key, -5) },
                onUp = { actions.onMinutes(draft.key, 5) }
            )
            ActionButton(
                label = draft.kind.name,
                tone = Tone.Tinted,
                accent = accent,
                height = 64.dp,
                corner = 18.dp
            ) { actions.onKind(draft.key, draft.kind.next()) }
            ActionButton(
                label = if (draft.fixed) "FIXED" else "FLOWS",
                height = 64.dp,
                corner = 18.dp
            ) { actions.onToggleFixed(draft.key) }
            if (draft.fixed) {
                Stepper(
                    value = formatClock(draft.startLocal ?: LocalTime.of(9, 0)),
                    onDown = { actions.onShiftStart(draft.key, -15) },
                    onUp = { actions.onShiftStart(draft.key, 15) }
                )
            }
        }

        ChecklistEditor(
            items = draft.items,
            onText = { itemKey, text -> actions.onItemText(draft.key, itemKey, text) },
            onRemove = { itemKey -> actions.onRemoveItem(draft.key, itemKey) },
            onAdd = { actions.onAddItem(draft.key) },
            modifier = Modifier.padding(start = 72.dp, top = 6.dp)
        )
    }
}

@Composable
private fun Stepper(value: String, onDown: () -> Unit, onUp: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        ActionButton("−", Modifier.size(64.dp), height = 64.dp, corner = 18.dp, onClick = onDown)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = LabelPrimary,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        ActionButton("+", Modifier.size(64.dp), height = 64.dp, corner = 18.dp, onClick = onUp)
    }
}

private fun BlockKind.next(): BlockKind = BlockKind.entries[(ordinal + 1) % BlockKind.entries.size]

@Suppress("unused")
private val labelSecondaryKeepsImportHonest = LabelSecondary
