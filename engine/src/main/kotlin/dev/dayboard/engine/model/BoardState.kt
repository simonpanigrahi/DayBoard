package dev.dayboard.engine.model

import dev.dayboard.engine.layout.Conflict
import dev.dayboard.engine.layout.ResolvedBlock
import java.time.Instant
import java.time.LocalTime
import java.time.ZonedDateTime

data class BoardState(
    val clock: ZonedDateTime,
    val current: ActiveBlock?,
    val next: ResolvedBlock?,
    /**
     * What a START button should act on: the current block if it has not been started,
     * otherwise the next one, otherwise the first block never started. Running late is
     * normal, and the day should still be startable when the clock has moved past it.
     */
    val startable: ResolvedBlock?,
    val later: List<ResolvedBlock>,
    val completed: List<CompletedBlock>,
    val dayTotals: DayTotals,
    val ribbon: List<RibbonSegment>,
    /** The span the ribbon's fractions are measured across, so it can be labelled. */
    val ribbonWindow: RibbonWindow,
    /** Where the "now" needle sits across the ribbon window, 0..1. */
    val nowFraction: Float,
    val nudge: Nudge?,
    val conflicts: List<Conflict>
)

data class ActiveBlock(
    val resolved: ResolvedBlock,
    val startedAt: Instant?,
    val actuals: BlockActuals,
    val remainingMs: Long,
    val overrunMs: Long,
    val progress: Float,
    val checkedItemIds: Set<Long> = emptySet(),
    val onBreak: Boolean = false,
    val paused: Boolean = false
)

data class CompletedBlock(
    val resolved: ResolvedBlock,
    val actuals: BlockActuals,
    val endedAt: Instant?,
    val skipped: Boolean = false
)

data class DayTotals(
    val plannedMs: Long,
    val focusedMs: Long,
    val breakMs: Long,
    val untrackedMs: Long,
    val overrunMs: Long,
    val itemsDone: Int,
    val itemsTotal: Int,
    val confidence: Confidence
) {
    val elapsedMs: Long get() = focusedMs + breakMs + untrackedMs
}

enum class SegmentState { PAST, CURRENT, FUTURE }

/**
 * Fractions are 0..1 across the ribbon window, ready for one Canvas drawRect each. The
 * segment carries its own title and times so the ribbon can answer "what is that block?"
 * without the UI holding a second copy of the plan.
 */
data class RibbonSegment(
    val blockId: Long,
    val title: String,
    val colorRole: ColorRole,
    val start: LocalTime,
    val end: LocalTime,
    val minutes: Int,
    val startFraction: Float,
    val endFraction: Float,
    val state: SegmentState
)

/** Minutes from midnight; the end may pass 1440 for a day that runs late. */
data class RibbonWindow(val startMinute: Int, val endMinute: Int) {
    val minutes: Int get() = endMinute - startMinute
}

enum class NudgeKind { REST_SUGGESTION, OVERRUN }

data class Nudge(val kind: NudgeKind, val minutes: Int, val message: String)
