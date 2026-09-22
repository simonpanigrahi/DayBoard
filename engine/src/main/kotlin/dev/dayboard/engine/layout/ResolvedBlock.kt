package dev.dayboard.engine.layout

import dev.dayboard.engine.model.Block
import java.time.LocalTime

/** What an overflow policy did to a block, for the Review screen to explain. */
enum class Adjustment { NONE, COMPRESSED, TRUNCATED, SPILLED, PUSHED }

/**
 * Times are minutes from midnight of the plan's date and may exceed 1440: a plan
 * that runs past midnight has to stay monotonic, which LocalTime alone cannot do.
 */
data class ResolvedBlock(
    val block: Block,
    val startMinute: Int,
    val minutes: Int,
    val adjustment: Adjustment = Adjustment.NONE
) {
    val endMinute: Int get() = startMinute + minutes
    val start: LocalTime get() = minuteOfDay(startMinute)
    val end: LocalTime get() = minuteOfDay(endMinute)
}

data class LayoutResult(
    val blocks: List<ResolvedBlock>,
    val conflicts: List<Conflict> = emptyList()
)

internal fun minuteOfDay(minute: Int): LocalTime =
    LocalTime.MIDNIGHT.plusMinutes(Math.floorMod(minute, MINUTES_PER_DAY).toLong())

internal fun LocalTime.minutesFromMidnight(): Int = hour * 60 + minute

internal const val MINUTES_PER_DAY = 24 * 60
