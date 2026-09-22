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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.ui.board.formatClock
import dev.dayboard.ui.board.formatDuration
import dev.dayboard.ui.common.ActionButton
import dev.dayboard.ui.common.ChevronGlyph
import dev.dayboard.ui.common.GhostButton
import dev.dayboard.ui.common.GlyphButton
import dev.dayboard.ui.common.Tone
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelSecondary
import dev.dayboard.ui.theme.LabelTertiary
import dev.dayboard.ui.theme.SystemBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DAY = DateTimeFormatter.ofPattern("EEEE d MMMM")

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
    val onDayStartNow: () -> Unit,
    val onDay: (Long) -> Unit,
    val onToday: () -> Unit,
    val onCopyDay: (LocalDate) -> Unit
)

@Composable
fun PlanEditorScreen(state: EditorUiState, actions: EditorActions, modifier: Modifier = Modifier) {
    val reorder = rememberReorderState(actions.onMove)
    val resolved = state.preview

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = 34.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DayHeader(state, actions)

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("DAY STARTS", style = MaterialTheme.typography.labelMedium, color = LabelTertiary)
            ActionButton("−", Modifier.size(60.dp), height = 60.dp, corner = 14.dp) { actions.onShiftDayStart(-15) }
            Text(
                text = formatClock(state.dayStart),
                style = MaterialTheme.typography.titleLarge,
                color = LabelPrimary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            ActionButton("+", Modifier.size(60.dp), height = 60.dp, corner = 14.dp) { actions.onShiftDayStart(15) }
            if (state.isToday) GhostButton("Start now", accent = SystemBlue, onClick = actions.onDayStartNow)
            Text(
                text = "  ${state.blocks.size} blocks · " +
                    formatDuration(state.blocks.sumOf { it.minutes } * 60_000L),
                style = MaterialTheme.typography.labelSmall,
                color = LabelTertiary
            )
        }

        if (state.blocks.isEmpty()) {
            EmptyDay(state.date, state.loaded, actions)
            return@Column
        }

        LazyColumn(
            state = reorder.listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(state.blocks, key = { _, draft -> draft.key }) { index, draft ->
                val placed = resolved.blocks.getOrNull(index)
                BlockRow(
                    draft = draft,
                    index = index,
                    dragging = reorder.draggingIndex == index,
                    window = placed?.let { "${formatClock(it.start)} – ${formatClock(it.end)}" },
                    reorder = reorder,
                    actions = actions
                )
            }
        }
    }
}

@Composable
private fun DayHeader(state: EditorUiState, actions: EditorActions) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        GlyphButton(glyph = { ChevronGlyph(it, pointsLeft = true) }) { actions.onDay(-1) }
        Column(Modifier.widthIn(min = 360.dp).padding(horizontal = 18.dp)) {
            Text(
                text = state.date.format(DAY),
                style = MaterialTheme.typography.titleLarge,
                color = LabelPrimary
            )
            Text(
                text = relativeDay(state.date),
                style = MaterialTheme.typography.labelMedium,
                color = if (state.isToday) SystemBlue else LabelTertiary
            )
        }
        GlyphButton(glyph = { ChevronGlyph(it, pointsLeft = false) }) { actions.onDay(1) }
        if (!state.isToday) {
            GhostButton("Today", accent = SystemBlue, modifier = Modifier.padding(start = 6.dp), onClick = actions.onToday)
        }

        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            GhostButton("Board", accent = SystemBlue, onClick = actions.onBack)
            GhostButton("Paste a day", accent = SystemBlue, onClick = actions.onImport)
            ActionButton("ADD BLOCK", Modifier.padding(start = 10.dp), height = 72.dp, corner = 14.dp, onClick = actions.onAdd)
            ActionButton(
                label = "REVIEW",
                modifier = Modifier.padding(start = 10.dp),
                tone = Tone.Filled,
                accent = SystemBlue,
                enabled = state.blocks.isNotEmpty(),
                height = 72.dp,
                corner = 14.dp,
                onClick = actions.onReview
            )
        }
    }
}

@Composable
private fun EmptyDay(date: LocalDate, loaded: Boolean, actions: EditorActions) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!loaded) return
        Text(
            text = "Nothing planned for ${relativeDay(date).lowercase()}",
            style = MaterialTheme.typography.headlineMedium,
            color = LabelSecondary,
            textAlign = TextAlign.Center
        )
        Row(Modifier.padding(top = 30.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ActionButton("ADD BLOCK", height = 76.dp, corner = 14.dp, onClick = actions.onAdd)
            ActionButton("PASTE A DAY", height = 76.dp, corner = 14.dp, onClick = actions.onImport)
            ActionButton(
                label = "COPY ${relativeDay(date.minusDays(1)).uppercase()}",
                tone = Tone.Tinted,
                accent = SystemBlue,
                height = 76.dp,
                corner = 14.dp
            ) { actions.onCopyDay(date.minusDays(1)) }
        }
    }
}

private fun relativeDay(date: LocalDate): String = when (date) {
    LocalDate.now() -> "Today"
    LocalDate.now().plusDays(1) -> "Tomorrow"
    LocalDate.now().minusDays(1) -> "Yesterday"
    else -> date.format(DateTimeFormatter.ofPattern("EEEE"))
}
