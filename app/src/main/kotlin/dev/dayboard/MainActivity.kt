package dev.dayboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.dayboard.ui.board.BoardActions
import dev.dayboard.ui.board.BoardScreen
import dev.dayboard.ui.board.BoardViewModel
import dev.dayboard.ui.theme.DayBoardTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as DayBoardApp).container
        setContent {
            DayBoardTheme {
                DayBoard(container)
            }
        }
    }
}

@Composable
private fun DayBoard(container: AppContainer) {
    val board: BoardViewModel = viewModel(factory = BoardViewModel.factory(container))
    val state by board.state.collectAsStateWithLifecycle()

    BoardScreen(
        state = state,
        actions = BoardActions(
            onStart = board::startCurrent,
            onPause = board::pause,
            onResume = board::resume,
            onBreak = board::startBreak,
            onEndBreak = board::endBreak,
            onExtend = { board.extend(5) },
            onDone = board::doneAndAdvance,
            onToggleItem = board::toggleItem,
            onEdit = {}
        )
    )
}
