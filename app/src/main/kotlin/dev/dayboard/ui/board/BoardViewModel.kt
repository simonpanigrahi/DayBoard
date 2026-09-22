package dev.dayboard.ui.board

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.dayboard.AppContainer
import dev.dayboard.clock.MINUTE_MS
import dev.dayboard.clock.SECOND_MS
import dev.dayboard.clock.tickFlow
import dev.dayboard.data.repo.EventRepository
import dev.dayboard.data.repo.PlanRepository
import dev.dayboard.data.repo.SettingsRepository
import dev.dayboard.engine.model.BoardState
import dev.dayboard.engine.model.ChecklistItem
import dev.dayboard.engine.model.EventType
import dev.dayboard.engine.resolve
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId

data class BoardUiState(
    val loading: Boolean = true,
    val hasPlan: Boolean = false,
    val board: BoardState? = null,
    val checklist: List<ChecklistItem> = emptyList()
)

enum class BreakKind(val meta: String, val minutes: Int) {
    WALK("walk", 10),
    TOILET("bio", 5),
    MEAL("meal", 30),
    CUSTOM("custom", 15)
}

/**
 * Holds no clock logic of its own: it collects the tick, the plan and the log, and
 * hands all three to the engine's resolve(). Every button appends one event.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BoardViewModel(
    private val plans: PlanRepository,
    private val events: EventRepository,
    settings: SettingsRepository,
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val elapsedRealtime: () -> Long = SystemClock::elapsedRealtime
) : ViewModel() {

    // Re-derived once a minute so the board rolls over at midnight on its own.
    private val date = tickFlow(MINUTE_MS).map { it.atZone(zone).toLocalDate() }.distinctUntilChanged()

    private val plan = date.flatMapLatest { plans.observe(it) }
    private val log = date.flatMapLatest { events.observeDay(it, zone) }

    val state: StateFlow<BoardUiState> =
        combine(tickFlow(SECOND_MS), plan, log, settings.dayStart) { now, dayPlan, log, dayStart ->
            if (dayPlan == null) {
                BoardUiState(loading = false, hasPlan = false)
            } else {
                val board = resolve(dayPlan.layout(dayStart), log, now, zone, elapsedRealtime())
                BoardUiState(
                    loading = false,
                    hasPlan = true,
                    board = board,
                    checklist = dayPlan.checklists[board.current?.resolved?.block?.id].orEmpty()
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BoardUiState())

    private val currentBlockId: Long? get() = state.value.board?.current?.resolved?.block?.id

    /** What START acts on: the engine decides, including when the day has run late. */
    private val startableBlockId: Long? get() = state.value.board?.startable?.block?.id

    fun startCurrent() {
        append(EventType.BLOCK_START, startableBlockId ?: return)
    }

    /** Not doing this one. It closes without pretending any of it was worked. */
    fun skip() {
        append(EventType.BLOCK_SKIP, currentBlockId ?: startableBlockId ?: return)
    }

    fun pause() = onCurrentBlock(EventType.PAUSE)

    fun resume() = onCurrentBlock(EventType.RESUME)

    fun startBreak(kind: BreakKind) = onCurrentBlock(EventType.BREAK_START, meta = kind.meta)

    fun endBreak() = onCurrentBlock(EventType.BREAK_END)

    fun extend(minutes: Int = 5) = onCurrentBlock(EventType.BLOCK_EXTEND, meta = minutes.toString())

    /** Closes the running block and opens the next one, as two appended facts. */
    fun doneAndAdvance() {
        val board = state.value.board ?: return
        val finished = board.current?.resolved?.block?.id
        val next = board.next?.block?.id
        viewModelScope.launch {
            if (finished != null) events.append(EventType.BLOCK_END, finished)
            if (next != null) events.append(EventType.BLOCK_START, next)
        }
    }

    fun toggleItem(itemId: Long) {
        val checked = state.value.board?.current?.checkedItemIds.orEmpty().contains(itemId)
        onCurrentBlock(
            type = if (checked) EventType.ITEM_UNCHECK else EventType.ITEM_CHECK,
            refId = itemId
        )
    }

    /**
     * Block-level events need a block. With nothing running, the honest record is no
     * record: an event with a null blockId is a day-level fact and would be an orphan
     * that no fold ever reads.
     */
    private fun onCurrentBlock(type: EventType, refId: Long? = null, meta: String? = null) {
        val blockId = currentBlockId ?: return
        append(type, blockId, refId, meta)
    }

    private fun append(type: EventType, blockId: Long, refId: Long? = null, meta: String? = null) {
        viewModelScope.launch { events.append(type, blockId, refId, meta) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { BoardViewModel(container.plans, container.events, container.settings) }
        }
    }
}
