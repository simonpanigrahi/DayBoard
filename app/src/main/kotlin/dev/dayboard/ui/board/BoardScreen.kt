package dev.dayboard.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.ui.theme.BoardDim

data class BoardActions(
    val onStart: () -> Unit,
    val onPause: () -> Unit,
    val onResume: () -> Unit,
    val onBreak: (BreakKind) -> Unit,
    val onEndBreak: () -> Unit,
    val onExtend: () -> Unit,
    val onDone: () -> Unit,
    val onToggleItem: (Long) -> Unit,
    val onEdit: () -> Unit
)

@Composable
fun BoardScreen(state: BoardUiState, actions: BoardActions, modifier: Modifier = Modifier) {
    val board = state.board
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        if (board == null) {
            EmptyBoard(state.loading, actions.onEdit)
            return@Column
        }

        ClockHeader(board.clock, actions.onEdit)

        val active = board.current
        if (active != null && active.onBreak) {
            BreakFace(active, actions.onEndBreak, Modifier.weight(1f))
        } else {
            CurrentBlockCard(active, board.next, state.checklist, actions.onToggleItem)

            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                UpNextPane(board.next, board.later, Modifier.weight(1f))
                DayTotalsPane(board.dayTotals, Modifier.weight(1f))
            }

            ActionBar(
                running = active != null && active.startedAt != null,
                paused = active?.paused == true,
                onStart = actions.onStart,
                onPause = actions.onPause,
                onResume = actions.onResume,
                onBreak = actions.onBreak,
                onExtend = actions.onExtend,
                onDone = actions.onDone
            )
        }

        DayRibbon(board.ribbon, board.nowFraction)
    }
}

@Composable
private fun EmptyBoard(loading: Boolean, onEdit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (loading) "…" else "No plan for today",
            style = MaterialTheme.typography.headlineLarge,
            color = BoardDim
        )
        if (!loading) {
            Button(onClick = onEdit, modifier = Modifier.padding(top = 32.dp)) {
                Text("BUILD THE DAY", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
