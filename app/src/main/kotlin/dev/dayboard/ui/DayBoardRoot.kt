package dev.dayboard.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.dayboard.AppContainer
import dev.dayboard.ui.board.BoardActions
import dev.dayboard.ui.board.BoardScreen
import dev.dayboard.ui.board.BoardViewModel
import dev.dayboard.ui.editor.EditorActions
import dev.dayboard.ui.editor.ImportScreen
import dev.dayboard.ui.editor.PlanEditorScreen
import dev.dayboard.ui.editor.PlanEditorViewModel
import dev.dayboard.ui.editor.ReviewScreen

private enum class Screen { BOARD, IMPORT, EDITOR, REVIEW }

/** Board, one input box, editor and review, held in plain state. */
@Composable
fun DayBoardRoot(container: AppContainer) {
    var screen by rememberSaveable { mutableStateOf(Screen.BOARD) }
    // Bumped on every entry so the plan screens reload the committed day rather than
    // resuming a draft the user walked away from.
    var session by rememberSaveable { mutableIntStateOf(0) }

    if (screen == Screen.BOARD) {
        BoardRoute(
            container = container,
            onEdit = {
                session++
                screen = Screen.EDITOR
            },
            onImport = {
                session++
                screen = Screen.IMPORT
            }
        )
    } else {
        PlanRoute(container, session, screen) { screen = it }
    }
}

@Composable
private fun BoardRoute(container: AppContainer, onEdit: () -> Unit, onImport: () -> Unit) {
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
            onSkip = board::skip,
            onToggleItem = board::toggleItem,
            onEdit = onEdit,
            onImport = onImport
        )
    )
}

@Composable
private fun PlanRoute(
    container: AppContainer,
    session: Int,
    screen: Screen,
    onScreen: (Screen) -> Unit
) {
    val editor: PlanEditorViewModel =
        viewModel(key = "editor-$session", factory = PlanEditorViewModel.factory(container))
    val state by editor.state.collectAsStateWithLifecycle()

    when (screen) {
        Screen.IMPORT -> ImportScreen(
            errors = state.importErrors,
            onParse = { text -> editor.importText(text) { onScreen(Screen.REVIEW) } },
            onBack = { onScreen(Screen.BOARD) }
        )

        Screen.REVIEW -> ReviewScreen(
            plan = state.preview,
            warnings = state.importWarnings,
            onCommit = { editor.commit { onScreen(Screen.BOARD) } },
            onBack = { onScreen(Screen.EDITOR) }
        )

        else -> PlanEditorScreen(
            state = state,
            actions = EditorActions(
                onAdd = editor::addBlock,
                onRemove = editor::removeBlock,
                onMove = editor::move,
                onTitle = editor::setTitle,
                onMinutes = editor::changeMinutes,
                onKind = editor::setKind,
                onColor = editor::cycleColor,
                onToggleFixed = editor::toggleFixed,
                onShiftStart = editor::shiftStart,
                onAddItem = editor::addItem,
                onItemText = editor::setItemText,
                onRemoveItem = editor::removeItem,
                onReview = { onScreen(Screen.REVIEW) },
                onImport = { onScreen(Screen.IMPORT) },
                onBack = { onScreen(Screen.BOARD) },
                onShiftDayStart = editor::shiftDayStart,
                onDayStartNow = editor::startDayNow
            )
        )
    }
}

/**
 * The board sits on a stand and has to stay readable, so the window keeps the screen on
 * while it shows. A WAKE_LOCK would need a permission and would outlive the screen;
 * this flag is scoped to the view and releases itself.
 */
@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}
