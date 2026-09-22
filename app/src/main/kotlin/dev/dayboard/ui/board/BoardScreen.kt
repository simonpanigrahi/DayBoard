package dev.dayboard.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.ui.theme.BoardAmber
import dev.dayboard.ui.theme.BoardBlack
import dev.dayboard.ui.theme.BoardDim

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
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
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
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                CurrentBlockCard(
                    active = active,
                    startable = board.startable,
                    checklist = state.checklist,
                    onToggleItem = actions.onToggleItem,
                    modifier = Modifier.weight(1.7f).fillMaxHeight()
                )
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Both panels size to their content and the spare height goes to the
                    // spacer, so neither can ever clip its last row.
                    UpNextPane(board.next, board.later, Modifier.fillMaxWidth())
                    DayTotalsPane(board.dayTotals, Modifier.fillMaxWidth())
                    Spacer(Modifier.weight(1f))
                }
            }

            ActionBar(
                running = active?.startedAt != null,
                paused = active?.paused == true,
                canStart = board.startable != null,
                canAct = active != null,
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
        Text(
            text = if (loading) "…" else "No plan for today",
            style = MaterialTheme.typography.displayMedium,
            color = BoardDim
        )
        if (!loading) {
            Text(
                text = "Paste a day in one go, or lay it out block by block.",
                style = MaterialTheme.typography.bodyLarge,
                color = BoardDim,
                modifier = Modifier.padding(top = 16.dp)
            )
            Row(
                Modifier.padding(top = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Button(
                    onClick = actions.onImport,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.height(84.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BoardAmber, contentColor = BoardBlack)
                ) {
                    Text("FILL THE DAY", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 20.dp))
                }
                OutlinedButton(
                    onClick = actions.onEdit,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.height(84.dp)
                ) {
                    Text("BUILD BY HAND", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 20.dp))
                }
            }
        }
    }
}
