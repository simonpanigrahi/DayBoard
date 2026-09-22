package dev.dayboard.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.dayboard.AppContainer
import dev.dayboard.ui.board.BoardActions
import dev.dayboard.ui.board.BoardScreen
import dev.dayboard.ui.board.BoardViewModel
import dev.dayboard.ui.editor.EditorActions
import dev.dayboard.ui.editor.PlanEditorScreen
import dev.dayboard.ui.editor.PlanEditorViewModel
import dev.dayboard.ui.editor.ReviewScreen

private enum class Screen { BOARD, EDITOR, REVIEW }

/** Two destinations and a review step, held in plain state. A nav library would be overkill. */
@Composable
fun DayBoardRoot(container: AppContainer) {
    var screen by rememberSaveable { mutableStateOf(Screen.BOARD) }
    // Bumped on every entry so the editor reloads the committed plan rather than
    // resuming a draft the user walked away from.
    var session by rememberSaveable { mutableIntStateOf(0) }

    when (screen) {
        Screen.BOARD -> BoardRoute(container) {
            session++
            screen = Screen.EDITOR
        }

        Screen.EDITOR, Screen.REVIEW -> EditorRoute(
            container = container,
            session = session,
            reviewing = screen == Screen.REVIEW,
            onScreen = { reviewing -> screen = if (reviewing) Screen.REVIEW else Screen.EDITOR },
            onLeave = { screen = Screen.BOARD }
        )
    }
}

@Composable
private fun BoardRoute(container: AppContainer, onEdit: () -> Unit) {
    KeepScreenOn()
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
            onEdit = onEdit
        )
    )
}

@Composable
private fun EditorRoute(
    container: AppContainer,
    session: Int,
    reviewing: Boolean,
    onScreen: (Boolean) -> Unit,
    onLeave: () -> Unit
) {
    val editor: PlanEditorViewModel =
        viewModel(key = "editor-$session", factory = PlanEditorViewModel.factory(container))
    val state by editor.state.collectAsStateWithLifecycle()

    if (reviewing) {
        ReviewScreen(
            plan = state.preview,
            onCommit = { editor.commit(onLeave) },
            onBack = { onScreen(false) }
        )
    } else {
        PlanEditorScreen(
            state = state,
            actions = EditorActions(
                onAdd = editor::addBlock,
                onRemove = editor::removeBlock,
                onMove = editor::move,
                onTitle = editor::setTitle,
                onMinutes = editor::changeMinutes,
                onKind = editor::setKind,
                onToggleFixed = editor::toggleFixed,
                onShiftStart = editor::shiftStart,
                onAddItem = editor::addItem,
                onItemText = editor::setItemText,
                onRemoveItem = editor::removeItem,
                onReview = { onScreen(true) },
                onBack = onLeave
            )
        )
    }
}

/**
 * The board is meant to sit on a stand and stay readable, so the window keeps the
 * screen on while it is showing. A WAKE_LOCK would need a permission and would outlive
 * the screen; this flag is scoped to the view and releases itself.
 */
@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}
