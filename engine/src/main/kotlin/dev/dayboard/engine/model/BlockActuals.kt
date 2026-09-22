package dev.dayboard.engine.model

/** FULL unless a reboot or a wall-clock jump was seen inside the folded span. */
enum class Confidence { FULL, PARTIAL }

data class BlockActuals(
    val plannedMs: Long,
    val focusedMs: Long,
    val breakMs: Long,
    val untrackedMs: Long,
    val overrunMs: Long,
    val itemsDone: Int,
    val itemsTotal: Int,
    val confidence: Confidence
) {
    /** The ledger identity from the spec: elapsed = focused + break + untracked. */
    val elapsedMs: Long get() = focusedMs + breakMs + untrackedMs

    companion object {
        fun empty(plannedMs: Long = 0, itemsTotal: Int = 0) = BlockActuals(
            plannedMs = plannedMs,
            focusedMs = 0,
            breakMs = 0,
            untrackedMs = 0,
            overrunMs = 0,
            itemsDone = 0,
            itemsTotal = itemsTotal,
            confidence = Confidence.FULL
        )
    }
}
