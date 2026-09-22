package dev.dayboard.engine.layout

enum class ConflictKind {
    /** A flow run still crosses the next anchor after its overflow policy ran. */
    OVERFLOW_INTO_ANCHOR,

    /** COMPRESS could not absorb the overshoot without breaching minMinutes. */
    MIN_MINUTES_FLOOR,

    /** TRUNCATE cut a block short at the anchor. */
    BLOCK_TRUNCATED,

    /** An anchor starts before the previous anchor's own planned end. */
    ANCHOR_OVERLAP
}

data class Conflict(
    val kind: ConflictKind,
    val blockId: Long?,
    val anchorBlockId: Long?,
    val minutes: Int,
    val message: String
)
