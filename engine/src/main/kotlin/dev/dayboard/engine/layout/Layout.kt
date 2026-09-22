package dev.dayboard.engine.layout

import dev.dayboard.engine.model.Anchor
import dev.dayboard.engine.model.Block
import java.time.LocalTime

/**
 * The single sweep from the spec: sort by orderIndex (order is what the user edited,
 * not time), walk once with a cursor, pin FIXED blocks to the clock and chain FLOW
 * blocks behind them.
 */
fun layout(blocks: List<Block>, dayStart: LocalTime): LayoutResult {
    val dayStartMinute = dayStart.minutesFromMidnight()
    return sweep(
        remaining = blocks.sortedBy { it.orderIndex },
        cursor = dayStartMinute,
        dayStartMinute = dayStartMinute,
        pendingRun = emptyList(),
        placed = emptyList(),
        conflicts = emptyList()
    )
}

private tailrec fun sweep(
    remaining: List<Block>,
    cursor: Int,
    dayStartMinute: Int,
    pendingRun: List<Block>,
    placed: List<ResolvedBlock>,
    conflicts: List<Conflict>
): LayoutResult {
    val block = remaining.firstOrNull()
        ?: return LayoutResult(placed + place(cursor, pendingRun.map { it.unadjusted() }), conflicts)

    if (block.anchor == Anchor.FLOW) {
        return sweep(remaining.drop(1), cursor, dayStartMinute, pendingRun + block, placed, conflicts)
    }

    val anchorMinute = anchorMinuteOf(block, dayStartMinute)
    val flushed = flushRun(pendingRun, cursor, anchorMinute, block, placed.lastOrNull())
    return sweep(
        remaining = remaining.drop(1),
        cursor = anchorMinute + block.plannedMinutes,
        dayStartMinute = dayStartMinute,
        pendingRun = emptyList(),
        placed = placed + flushed.first + ResolvedBlock(block, anchorMinute, block.plannedMinutes),
        conflicts = conflicts + flushed.second
    )
}

/**
 * An anchor whose clock time falls before the day started belongs to the small hours
 * of the next morning, so it is normalised forward rather than laid out in the past.
 */
private fun anchorMinuteOf(block: Block, dayStartMinute: Int): Int {
    val minute = requireNotNull(block.startLocal) { "FIXED block '${block.title}' has no startLocal" }
        .minutesFromMidnight()
    return if (minute < dayStartMinute) minute + MINUTES_PER_DAY else minute
}

private fun flushRun(
    run: List<Block>,
    cursor: Int,
    anchorMinute: Int,
    anchor: Block,
    previous: ResolvedBlock?
): Pair<List<ResolvedBlock>, List<Conflict>> {
    val available = anchorMinute - cursor
    val overlap = if (available < 0) {
        listOf(
            Conflict(
                kind = ConflictKind.ANCHOR_OVERLAP,
                blockId = previous?.block?.id,
                anchorBlockId = anchor.id,
                minutes = -available,
                message = "'${anchor.title}' starts ${-available}m before the previous block ends"
            )
        )
    } else emptyList()

    if (run.isEmpty()) return emptyList<ResolvedBlock>() to overlap

    val outcome = applyOverflow(run, available.coerceAtLeast(0), anchor)
    return place(cursor, outcome.blocks) to (outcome.conflicts + overlap)
}

private fun place(startMinute: Int, blocks: List<AdjustedBlock>): List<ResolvedBlock> =
    blocks.fold(startMinute to emptyList<ResolvedBlock>()) { (cursor, acc), adjusted ->
        (cursor + adjusted.minutes) to (acc + ResolvedBlock(adjusted.block, cursor, adjusted.minutes, adjusted.adjustment))
    }.second

private fun Block.unadjusted() = AdjustedBlock(this, plannedMinutes, Adjustment.NONE)
