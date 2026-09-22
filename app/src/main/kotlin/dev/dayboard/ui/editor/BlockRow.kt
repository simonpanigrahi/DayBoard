package dev.dayboard.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

/** The gutter the handle sits in. Everything below lines up against its right edge. */
internal val GUTTER = 66.dp

@Composable
fun BlockRow(
    draft: BlockDraft,
    index: Int,
    dragging: Boolean,
    window: String?,
    reorder: ReorderState,
    actions: EditorActions,
    modifier: Modifier = Modifier
) {
    val accent = colorFor(draft.colorRole)
    Panel(modifier.fillMaxWidth(), accent = if (dragging) accent else null, corner = 16.dp, padding = 12.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(GUTTER)
                    .size(GUTTER)
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
                shape = RoundedCornerShape(12.dp),
                colors = boardFieldColors(),
                modifier = Modifier.weight(1f).height(62.dp)
            )

            Box(
                Modifier.size(GUTTER).clickable { actions.onColor(draft.key) },
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(28.dp).clip(CircleShape).background(accent))
            }

            GhostButton("Delete", accent = SystemRed, height = 62.dp) { actions.onRemove(draft.key) }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp, start = GUTTER),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (window != null) {
                Text(
                    text = window,
                    style = MaterialTheme.typography.titleMedium,
                    color = LabelSecondary,
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
            Stepper(
                value = "${draft.minutes} min",
                onDown = { actions.onMinutes(draft.key, -5) },
                onUp = { actions.onMinutes(draft.key, 5) }
            )
            ActionButton(draft.kind.name, tone = Tone.Tinted, accent = accent, height = 56.dp, corner = 14.dp) {
                actions.onKind(draft.key, draft.kind.next())
            }
            ActionButton(if (draft.fixed) "FIXED" else "FLOWS", height = 56.dp, corner = 14.dp) {
                actions.onToggleFixed(draft.key)
            }
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
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun Stepper(value: String, onDown: () -> Unit, onUp: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        ActionButton("−", Modifier.size(60.dp), height = 56.dp, corner = 14.dp, onClick = onDown)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = LabelPrimary,
            modifier = Modifier.width(88.dp).padding(horizontal = 8.dp)
        )
        ActionButton("+", Modifier.size(60.dp), height = 56.dp, corner = 14.dp, onClick = onUp)
    }
}

private fun BlockKind.next(): BlockKind = BlockKind.entries[(ordinal + 1) % BlockKind.entries.size]
