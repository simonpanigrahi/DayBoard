package dev.dayboard.engine.model

import dev.dayboard.engine.layout.Conflict
import dev.dayboard.engine.layout.ResolvedBlock
import java.time.Instant
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

/** Fractions are 0..1 across the ribbon window, ready for one Canvas drawRect each. */
data class RibbonSegment(
    val blockId: Long,
    val colorRole: ColorRole,
    val startFraction: Float,
    val endFraction: Float,
    val state: SegmentState
)

enum class NudgeKind { REST_SUGGESTION, OVERRUN }

data class Nudge(val kind: NudgeKind, val minutes: Int, val message: String)
