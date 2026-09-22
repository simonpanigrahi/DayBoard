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
