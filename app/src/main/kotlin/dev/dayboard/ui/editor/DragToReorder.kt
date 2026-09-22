package dev.dayboard.ui.editor

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Reorder by dragging a handle. The target is whichever row the pointer is currently
 * over, taken from the list's own layout info, so rows may be any height.
 */
class ReorderState(val listState: LazyListState, private val onMove: (Int, Int) -> Unit) {

    var draggingIndex by mutableStateOf<Int?>(null)
        private set

    private var pointerY = 0f

    fun start(index: Int) {
        draggingIndex = index
        pointerY = centerOf(index)
    }

    fun drag(deltaY: Float) {
        val from = draggingIndex ?: return
        pointerY += deltaY
        val target = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { pointerY >= it.offset && pointerY <= it.offset + it.size }
            ?.index
            ?: return
        if (target != from) {
            onMove(from, target)
            draggingIndex = target
        }
    }

    fun end() {
        draggingIndex = null
    }

    private fun centerOf(index: Int): Float =
        listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
            ?.let { it.offset + it.size / 2f }
            ?: 0f
}

@Composable
fun rememberReorderState(onMove: (Int, Int) -> Unit): ReorderState {
    val listState = rememberLazyListState()
    return remember(listState) { ReorderState(listState, onMove) }
}
