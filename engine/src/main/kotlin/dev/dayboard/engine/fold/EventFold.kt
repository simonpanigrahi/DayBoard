package dev.dayboard.engine.fold

import dev.dayboard.engine.model.BlockActuals
import dev.dayboard.engine.model.Confidence
import dev.dayboard.engine.model.EventType
import dev.dayboard.engine.model.SessionEvent

/**
 * Folds one block's slice of the append-only log into its actuals.
 *
 * [now] is `SystemClock.elapsedRealtime()` taken in the same boot as the newest
 * event: every duration here is a monotonic delta, never a wall-clock subtraction,
 * except across a reboot where the wall clock is the only ruler left.
 *
 * Untracked is a residual, never a measurement: whatever was not focus and not an
 * explicit break lands there, so elapsed = focused + break + untracked by construction.
 */
fun foldEvents(
    events: List<SessionEvent>,
    plannedMs: Long,
    now: Long,
    itemsTotal: Int = 0
): BlockActuals {
    val folded = events
        .sortedWith(compareBy({ it.wallAt }, { it.id }))
        .fold(FoldState.INITIAL) { state, event -> state.advance(event) }
        .closeAt(now)

    val elapsed = folded.focusedMs + folded.breakMs + folded.untrackedMs
    return BlockActuals(
        plannedMs = plannedMs,
        focusedMs = folded.focusedMs,
        breakMs = folded.breakMs,
        untrackedMs = folded.untrackedMs,
        overrunMs = (elapsed - plannedMs).coerceAtLeast(0),
        itemsDone = folded.checked.size,
        itemsTotal = itemsTotal,
        confidence = if (folded.trusted) Confidence.FULL else Confidence.PARTIAL
    )
}

private enum class Mode { IDLE, FOCUS, PAUSED, BREAK, AWAY, ENDED }

private data class FoldState(
    val mode: Mode,
    val focusedMs: Long,
    val breakMs: Long,
    val untrackedMs: Long,
    val checked: Set<Long>,
    val trusted: Boolean,
    val last: SessionEvent?
) {
    fun advance(event: SessionEvent): FoldState {
        val spanned = last?.let { accumulate(spanBetween(it, event)) } ?: this
        return spanned.copy(
            mode = mode.after(event.type),
            checked = checked.after(event),
            last = event
        )
    }

    fun closeAt(nowElapsed: Long): FoldState =
        last?.let { accumulate(spanToNow(it, nowElapsed)) } ?: this

    private fun accumulate(span: Span): FoldState = when (mode) {
        Mode.FOCUS -> copy(focusedMs = focusedMs + span.millis, trusted = trusted && span.trusted)
        Mode.BREAK -> copy(breakMs = breakMs + span.millis, trusted = trusted && span.trusted)
        Mode.PAUSED, Mode.AWAY -> copy(untrackedMs = untrackedMs + span.millis, trusted = trusted && span.trusted)
        // Before the block started and after it closed, nothing belongs to this block,
        // so an untrusted span there is not a reason to doubt what was counted.
        Mode.IDLE, Mode.ENDED -> this
    }

    companion object {
        val INITIAL = FoldState(Mode.IDLE, 0, 0, 0, emptySet(), trusted = true, last = null)
    }
}

private fun Mode.after(type: EventType): Mode = when (type) {
    EventType.BLOCK_START -> if (this == Mode.IDLE) Mode.FOCUS else this
    EventType.BLOCK_END, EventType.BLOCK_SKIP -> Mode.ENDED
    EventType.PAUSE -> if (this == Mode.FOCUS || this == Mode.AWAY) Mode.PAUSED else this
    EventType.RESUME -> if (this == Mode.PAUSED || this == Mode.AWAY) Mode.FOCUS else this
    EventType.BREAK_START -> if (this == Mode.IDLE || this == Mode.ENDED) this else Mode.BREAK
    EventType.BREAK_END -> if (this == Mode.BREAK) Mode.FOCUS else this
    // Stepping away during a break is still break time, not a second bucket.
    EventType.AWAY_START -> if (this == Mode.FOCUS || this == Mode.PAUSED) Mode.AWAY else this
    EventType.AWAY_END -> if (this == Mode.AWAY) Mode.FOCUS else this
    EventType.BLOCK_EXTEND, EventType.ITEM_CHECK, EventType.ITEM_UNCHECK,
    EventType.DAY_START, EventType.DAY_END -> this
}

private fun Set<Long>.after(event: SessionEvent): Set<Long> {
    val itemId = event.refId ?: return this
    return when (event.type) {
        EventType.ITEM_CHECK -> this + itemId
        EventType.ITEM_UNCHECK -> this - itemId
        else -> this
    }
}

/** The net set of ticked checklist items, for the board to render the boxes. */
internal fun checkedItems(events: List<SessionEvent>): Set<Long> =
    events.sortedWith(compareBy({ it.wallAt }, { it.id }))
        .fold(emptySet<Long>()) { checked, event -> checked.after(event) }
