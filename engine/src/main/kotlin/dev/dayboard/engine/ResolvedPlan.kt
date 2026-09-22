package dev.dayboard.engine

import dev.dayboard.engine.layout.Conflict
import dev.dayboard.engine.layout.ResolvedBlock
import dev.dayboard.engine.layout.layout
import dev.dayboard.engine.model.Block
import dev.dayboard.engine.model.ChecklistItem
import java.time.LocalDate
import java.time.LocalTime

/** A plan that has been through the sweep once, ready to be resolved against the log. */
data class ResolvedPlan(
    val date: LocalDate,
    val dayStart: LocalTime,
    val blocks: List<ResolvedBlock>,
    val conflicts: List<Conflict> = emptyList(),
    val checklists: Map<Long, List<ChecklistItem>> = emptyMap()
) {
    /**
     * Re-runs the sweep with extra minutes granted to some blocks. Only blocks after an
     * extended one move, because the cursor ahead of it is untouched, which is what
     * keeps an extension from re-laying-out the part of the day already lived.
     */
    fun withExtensions(extraMinutes: Map<Long, Int>): ResolvedPlan {
        if (extraMinutes.isEmpty()) return this
        val extended = blocks.map { it.block }.map { block ->
            val extra = extraMinutes[block.id] ?: 0
            if (extra > 0) block.copy(plannedMinutes = block.plannedMinutes + extra) else block
        }
        return from(date, dayStart, extended, checklists)
    }

    companion object {
        fun from(
            date: LocalDate,
            dayStart: LocalTime,
            blocks: List<Block>,
            checklists: Map<Long, List<ChecklistItem>> = emptyMap()
        ): ResolvedPlan {
            val laid = layout(blocks, dayStart)
            return ResolvedPlan(date, dayStart, laid.blocks, laid.conflicts, checklists)
        }
    }
}

data class ResolveSettings(
    val restNudgeAfterMs: Long = 50 * 60_000L,
    val laterCount: Int = 3,
    val ribbonStart: LocalTime = LocalTime.of(6, 0),
    val ribbonEnd: LocalTime = LocalTime.of(23, 0)
)
