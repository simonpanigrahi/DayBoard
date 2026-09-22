package dev.dayboard.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.ui.common.ActionButton
import dev.dayboard.ui.common.Tone
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelSecondary
import dev.dayboard.ui.theme.LabelTertiary
import dev.dayboard.ui.theme.SystemBlue

data class BoardActions(
    val onStart: () -> Unit,
    val onPause: () -> Unit,
    val onResume: () -> Unit,
    val onBreak: (BreakKind) -> Unit,
    val onEndBreak: () -> Unit,
    val onExtend: () -> Unit,
    val onDone: () -> Unit,
    val onSkip: () -> Unit,
    val onToggleItem: (Long) -> Unit,
    val onEdit: () -> Unit,
    val onImport: () -> Unit
)

@Composable
fun BoardScreen(state: BoardUiState, actions: BoardActions, modifier: Modifier = Modifier) {
    val board = state.board
    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // The tablet's status bar and taskbar overlap the window, and the ribbon is
            // the first thing to disappear under them.
            .safeDrawingPadding()
            .padding(horizontal = 34.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        if (board == null) {
            EmptyBoard(state.loading, actions)
            return@Column
        }

        ClockHeader(board.clock, actions.onEdit, actions.onImport)

        val active = board.current
        if (active != null && active.onBreak) {
            BreakFace(active, actions.onEndBreak, Modifier.weight(1f))
        } else {
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                CurrentBlockCard(
                    active = active,
                    startable = board.startable,
                    checklist = state.checklist,
                    onToggleItem = actions.onToggleItem,
                    modifier = Modifier.weight(1.7f).fillMaxHeight()
                )
                // Scrolls rather than squeezes: a Column that runs out of height gives
                // its last child nothing, which silently emptied the totals panel.
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    UpNextPane(board.next, board.later, Modifier.fillMaxWidth())
                    DayTotalsPane(board.dayTotals, Modifier.fillMaxWidth())
                }
            }

            ActionBar(
                running = active?.startedAt != null,
                paused = active?.paused == true,
                canStart = board.startable != null,
                // Nothing to pause, extend or finish until something is actually running.
                canAct = active?.startedAt != null,
                onStart = actions.onStart,
                onPause = actions.onPause,
                onResume = actions.onResume,
                onBreak = actions.onBreak,
                onExtend = actions.onExtend,
                onDone = actions.onDone,
                onSkip = actions.onSkip
            )
        }

        DayRibbon(board.ribbon, board.ribbonWindow, board.nowFraction)
    }
}

@Composable
private fun EmptyBoard(loading: Boolean, actions: BoardActions) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loading) {
            Text("\u2026", style = MaterialTheme.typography.displayMedium, color = LabelTertiary)
            return@Column
        }
        Text(
            text = "No plan for today",
            style = MaterialTheme.typography.displaySmall,
            color = LabelPrimary
        )
        Text(
            text = "Paste a day in one go, or lay it out block by block.",
            style = MaterialTheme.typography.bodyLarge,
            color = LabelSecondary,
            modifier = Modifier.padding(top = 14.dp)
        )
        Row(
            Modifier.padding(top = 44.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionButton(
                label = "FILL THE DAY",
                tone = Tone.Filled,
                accent = SystemBlue,
                height = 88.dp,
                corner = 24.dp,
                onClick = actions.onImport
            )
            ActionButton(
                label = "BUILD BY HAND",
                height = 88.dp,
                corner = 24.dp,
                onClick = actions.onEdit
            )
        }
    }
}
