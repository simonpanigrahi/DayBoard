package dev.dayboard.engine.layout

import dev.dayboard.engine.model.Block
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.engine.model.OverflowPolicy
import kotlin.math.floor

internal data class AdjustedBlock(val block: Block, val minutes: Int, val adjustment: Adjustment)

internal data class OverflowOutcome(val blocks: List<AdjustedBlock>, val conflicts: List<Conflict>)

/**
 * Fits a run of flow blocks into [availableMinutes] before [anchor].
 *
 * The four policies are applied as a pipeline rather than per block, because a run
 * can mix them: buffers are eaten first (SPILL), then the soft edges shrink
 * (COMPRESS, and SPILL once its buffer is gone), then cuts happen from the end
 * (TRUNCATE), and whatever is still over cascades past the anchor (PUSH).
 */
internal fun applyOverflow(run: List<Block>, availableMinutes: Int, anchor: Block): OverflowOutcome {
    val initial = run.map { AdjustedBlock(it, it.plannedMinutes, Adjustment.NONE) }
    val overshoot = initial.sumOf { it.minutes } - availableMinutes
    if (overshoot <= 0) return OverflowOutcome(initial, emptyList())

    val (afterSpill, leftAfterSpill) =
        if (run.any { it.overflow == OverflowPolicy.SPILL }) consumeBuffers(initial, overshoot)
        else initial to overshoot

    val (afterCompress, leftAfterCompress) = compress(afterSpill, leftAfterSpill)
    val (afterTruncate, residual, cuts) = truncate(afterCompress, leftAfterCompress, anchor)

    val flooredConflicts = if (residual > 0) flooredBy(afterTruncate, anchor) else emptyList()
    val overflowConflict = if (residual > 0) {
        listOf(
            Conflict(
                kind = ConflictKind.OVERFLOW_INTO_ANCHOR,
                blockId = afterTruncate.lastOrNull()?.block?.id,
                anchorBlockId = anchor.id,
                minutes = residual,
                message = "Runs ${residual}m into '${anchor.title}'"
            )
        )
    } else emptyList()

    return OverflowOutcome(
        blocks = if (residual > 0) markPushed(afterTruncate, availableMinutes) else afterTruncate,
        conflicts = cuts + flooredConflicts + overflowConflict
    )
}

/** Buffers nearest the anchor go first: slack closest to the appointment is the cheapest. */
private fun consumeBuffers(blocks: List<AdjustedBlock>, overshoot: Int): Pair<List<AdjustedBlock>, Int> {
    val (reversed, left) = blocks.reversed()
        .fold(emptyList<AdjustedBlock>() to overshoot) { (acc, left), adjusted ->
            if (left > 0 && adjusted.block.kind == BlockKind.BUFFER && adjusted.minutes > 0) {
                val cut = minOf(left, adjusted.minutes)
                (acc + adjusted.copy(minutes = adjusted.minutes - cut, adjustment = Adjustment.SPILLED)) to (left - cut)
            } else {
                (acc + adjusted) to left
            }
        }
    return reversed.reversed() to left
}

private fun compress(blocks: List<AdjustedBlock>, overshoot: Int): Pair<List<AdjustedBlock>, Int> {
    val slacks = blocks.map { if (it.compressible) (it.minutes - it.floorMinutes).coerceAtLeast(0) else 0 }
    val totalSlack = slacks.sum()
    if (overshoot <= 0 || totalSlack == 0) return blocks to overshoot

    val target = minOf(overshoot, totalSlack)
    val exact = slacks.map { it.toDouble() * target / totalSlack }
    val base = exact.map { floor(it).toInt() }
    // Largest-remainder: the integer cuts must sum to `target` exactly, or the run
    // silently ends up a minute long or short of the anchor.
    val bumped = base.indices
        .sortedByDescending { exact[it] - base[it] }
        .fold(base to (target - base.sum())) { (cuts, left), index ->
            if (left > 0 && cuts[index] < slacks[index]) {
                cuts.replaceAt(index, cuts[index] + 1) to (left - 1)
            } else {
                cuts to left
            }
        }.first

    val compressed = blocks.mapIndexed { index, adjusted ->
        val cut = bumped[index]
        if (cut > 0) adjusted.copy(minutes = adjusted.minutes - cut, adjustment = Adjustment.COMPRESSED) else adjusted
    }
    return compressed to (overshoot - bumped.sum())
}

private fun truncate(
    blocks: List<AdjustedBlock>,
    overshoot: Int,
    anchor: Block
): Triple<List<AdjustedBlock>, Int, List<Conflict>> {
    if (overshoot <= 0 || blocks.none { it.block.overflow == OverflowPolicy.TRUNCATE }) {
        return Triple(blocks, overshoot, emptyList())
    }
    val (reversed, left, conflicts) = blocks.reversed().fold(
        Triple(emptyList<AdjustedBlock>(), overshoot, emptyList<Conflict>())
    ) { (acc, left, conflicts), adjusted ->
        if (left > 0 && adjusted.block.overflow == OverflowPolicy.TRUNCATE && adjusted.minutes > 0) {
            val cut = minOf(left, adjusted.minutes)
            Triple(
                acc + adjusted.copy(minutes = adjusted.minutes - cut, adjustment = Adjustment.TRUNCATED),
                left - cut,
                conflicts + Conflict(
                    kind = ConflictKind.BLOCK_TRUNCATED,
                    blockId = adjusted.block.id,
                    anchorBlockId = anchor.id,
                    minutes = cut,
                    message = "'${adjusted.block.title}' cut by ${cut}m at '${anchor.title}'"
                )
            )
        } else {
            Triple(acc + adjusted, left, conflicts)
        }
    }
    return Triple(reversed.reversed(), left, conflicts.reversed())
}

private fun flooredBy(blocks: List<AdjustedBlock>, anchor: Block): List<Conflict> =
    blocks.filter { it.compressible && it.minutes <= it.floorMinutes && it.block.minMinutes != null }
        .map {
            Conflict(
                kind = ConflictKind.MIN_MINUTES_FLOOR,
                blockId = it.block.id,
                anchorBlockId = anchor.id,
                minutes = it.floorMinutes,
                message = "'${it.block.title}' will not compress below ${it.floorMinutes}m"
            )
        }

/** Marks the blocks that end up on the far side of the anchor once nothing else absorbed the overshoot. */
private fun markPushed(blocks: List<AdjustedBlock>, availableMinutes: Int): List<AdjustedBlock> =
    blocks.fold(0 to emptyList<AdjustedBlock>()) { (cursor, acc), adjusted ->
        val end = cursor + adjusted.minutes
        val pushed = end > availableMinutes && adjusted.adjustment == Adjustment.NONE
        end to (acc + if (pushed) adjusted.copy(adjustment = Adjustment.PUSHED) else adjusted)
    }.second

private val AdjustedBlock.compressible: Boolean
    get() = block.overflow == OverflowPolicy.COMPRESS || block.overflow == OverflowPolicy.SPILL

private val AdjustedBlock.floorMinutes: Int get() = block.minMinutes ?: 0

private fun List<Int>.replaceAt(index: Int, value: Int): List<Int> =
    mapIndexed { i, existing -> if (i == index) value else existing }
