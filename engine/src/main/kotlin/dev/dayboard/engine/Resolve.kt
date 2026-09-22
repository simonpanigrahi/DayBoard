package dev.dayboard.engine

import dev.dayboard.engine.fold.checkedItems
import dev.dayboard.engine.fold.foldEvents
import dev.dayboard.engine.layout.MINUTES_PER_DAY
import dev.dayboard.engine.layout.ResolvedBlock
import dev.dayboard.engine.layout.minuteOfDay
import dev.dayboard.engine.layout.minutesFromMidnight
import dev.dayboard.engine.model.ActiveBlock
import dev.dayboard.engine.model.BlockActuals
import dev.dayboard.engine.model.BoardState
import dev.dayboard.engine.model.CompletedBlock
import dev.dayboard.engine.model.Confidence
import dev.dayboard.engine.model.DayTotals
import dev.dayboard.engine.model.EventType
import dev.dayboard.engine.model.Nudge
import dev.dayboard.engine.model.NudgeKind
import dev.dayboard.engine.model.RibbonSegment
import dev.dayboard.engine.model.SegmentState
import dev.dayboard.engine.model.SessionEvent
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * The whole board as one pure function of plan, log and time.
 *
 * [now] is the wall clock and drives scheduling only. [nowElapsed] is
 * `SystemClock.elapsedRealtime()` and drives durations only; when it is absent the
 * open segment of a running block simply is not counted yet, because inferring it
 * from the wall clock would mix the two clocks and silently corrupt history.
 */
fun resolve(
    plan: ResolvedPlan,
    events: List<SessionEvent>,
    now: Instant,
    zone: ZoneId,
    nowElapsed: Long? = null,
    settings: ResolveSettings = ResolveSettings()
): BoardState {
    val byBlock = events.filter { it.blockId != null }.groupBy { it.blockId }
    val slots = plan.blocks.map { resolved ->
        slotFor(resolved, byBlock[resolved.block.id].orEmpty(), plan, zone, nowElapsed)
    }

    val current = slots.firstOrNull { it.started && !it.closed }
        ?: slots.firstOrNull { !it.closed && it.resolved.minutes > 0 && !now.isBefore(it.startAt) && now.isBefore(it.endAt) }

    val upcoming = if (current == null) {
        slots.filter { !it.closed && it.startAt.isAfter(now) }
    } else {
        slots.drop(slots.indexOf(current) + 1).filter { !it.closed }
    }

    val active = current?.toActiveBlock(now)
    return BoardState(
        clock = ZonedDateTime.ofInstant(now, zone),
        current = active,
        next = upcoming.firstOrNull()?.resolved,
        later = upcoming.drop(1).take(settings.laterCount).map { it.resolved },
        completed = slots.filter { it != current && (it.closed || !it.endAt.isAfter(now)) }
            .map { CompletedBlock(it.resolved, it.actuals, it.endedAt, it.skipped) },
        dayTotals = totals(slots),
        ribbon = ribbon(slots, current, now, settings),
        nudge = active?.let { nudgeFor(it, settings) },
        conflicts = plan.conflicts
    )
}

private data class Slot(
    val resolved: ResolvedBlock,
    val startAt: Instant,
    val endAt: Instant,
    val events: List<SessionEvent>,
    val actuals: BlockActuals,
    val started: Boolean,
    val closed: Boolean,
    val endedAt: Instant?,
    val skipped: Boolean
)

private fun slotFor(
    resolved: ResolvedBlock,
    blockEvents: List<SessionEvent>,
    plan: ResolvedPlan,
    zone: ZoneId,
    nowElapsed: Long?
): Slot {
    val ordered = blockEvents.sortedWith(compareBy({ it.wallAt }, { it.id }))
    val closing = ordered.lastOrNull { it.type == EventType.BLOCK_END || it.type == EventType.BLOCK_SKIP }
    return Slot(
        resolved = resolved,
        startAt = resolved.startMinute.instantOn(plan.date, zone),
        endAt = resolved.endMinute.instantOn(plan.date, zone),
        events = ordered,
        actuals = foldEvents(
            events = ordered,
            plannedMs = resolved.minutes * 60_000L,
            // Without a monotonic reading of "now", the fold stops at the last logged
            // event rather than guessing how long the open segment has run.
            now = nowElapsed ?: ordered.lastOrNull()?.elapsedRealtime ?: 0L,
            itemsTotal = plan.checklists[resolved.block.id].orEmpty().size
        ),
        started = ordered.any { it.type == EventType.BLOCK_START },
        closed = closing != null,
        endedAt = closing?.wallAt,
        skipped = closing?.type == EventType.BLOCK_SKIP
    )
}

private fun Slot.toActiveBlock(now: Instant): ActiveBlock {
    val plannedMs = resolved.minutes * 60_000L
    // A tracked block is measured from the log; an untracked one can only be read off
    // the schedule, which is a wall-clock quantity and stays one.
    val elapsedMs = if (started) actuals.elapsedMs else Duration.between(startAt, now).toMillis().coerceAtLeast(0)
    val overrunMs = if (started) actuals.overrunMs else (elapsedMs - plannedMs).coerceAtLeast(0)
    return ActiveBlock(
        resolved = resolved,
        startedAt = events.firstOrNull { it.type == EventType.BLOCK_START }?.wallAt,
        actuals = actuals,
        remainingMs = (plannedMs - elapsedMs).coerceAtLeast(0),
        overrunMs = overrunMs,
        progress = if (plannedMs == 0L) 1f else (elapsedMs.toFloat() / plannedMs).coerceIn(0f, 1f),
        checkedItemIds = checkedItems(events),
        onBreak = lastOf(EventType.BREAK_START, EventType.BREAK_END) == EventType.BREAK_START,
        paused = lastOf(EventType.PAUSE, EventType.RESUME) == EventType.PAUSE
    )
}

private fun Slot.lastOf(vararg types: EventType): EventType? =
    events.lastOrNull { it.type in types }?.type

private fun totals(slots: List<Slot>) = DayTotals(
    plannedMs = slots.sumOf { it.resolved.minutes * 60_000L },
    focusedMs = slots.sumOf { it.actuals.focusedMs },
    breakMs = slots.sumOf { it.actuals.breakMs },
    untrackedMs = slots.sumOf { it.actuals.untrackedMs },
    overrunMs = slots.sumOf { it.actuals.overrunMs },
    itemsDone = slots.sumOf { it.actuals.itemsDone },
    itemsTotal = slots.sumOf { it.actuals.itemsTotal },
    confidence = if (slots.any { it.actuals.confidence == Confidence.PARTIAL }) Confidence.PARTIAL else Confidence.FULL
)

private fun ribbon(
    slots: List<Slot>,
    current: Slot?,
    now: Instant,
    settings: ResolveSettings
): List<RibbonSegment> {
    if (slots.isEmpty()) return emptyList()
    val windowStart = minOf(settings.ribbonStart.minutesFromMidnight(), slots.minOf { it.resolved.startMinute })
    val windowEnd = maxOf(settings.ribbonEnd.minutesFromMidnight(), slots.maxOf { it.resolved.endMinute })
    val span = (windowEnd - windowStart).toFloat()
    if (span <= 0f) return emptyList()

    return slots.map { slot ->
        RibbonSegment(
            blockId = slot.resolved.block.id,
            colorRole = slot.resolved.block.colorRole,
            startFraction = ((slot.resolved.startMinute - windowStart) / span).coerceIn(0f, 1f),
            endFraction = ((slot.resolved.endMinute - windowStart) / span).coerceIn(0f, 1f),
            state = when {
                slot === current -> SegmentState.CURRENT
                !slot.endAt.isAfter(now) -> SegmentState.PAST
                else -> SegmentState.FUTURE
            }
        )
    }
}

private fun nudgeFor(active: ActiveBlock, settings: ResolveSettings): Nudge? = when {
    active.overrunMs > 0 -> Nudge(
        kind = NudgeKind.OVERRUN,
        minutes = (active.overrunMs / 60_000L).toInt(),
        message = "${active.overrunMs / 60_000L}m over on '${active.resolved.block.title}'"
    )

    active.actuals.breakMs == 0L && active.actuals.focusedMs >= settings.restNudgeAfterMs -> Nudge(
        kind = NudgeKind.REST_SUGGESTION,
        minutes = (active.actuals.focusedMs / 60_000L).toInt(),
        message = "Been ${active.actuals.focusedMs / 60_000L}m. Stretch?"
    )

    else -> null
}

/**
 * Block times are local wall-clock times, so they are mapped through the plan's zone
 * rather than by adding minutes to an instant: on a DST day 09:00 still means 09:00.
 */
private fun Int.instantOn(date: LocalDate, zone: ZoneId): Instant =
    date.plusDays(Math.floorDiv(this, MINUTES_PER_DAY).toLong())
        .atTime(minuteOfDay(this))
        .atZone(zone)
        .toInstant()
