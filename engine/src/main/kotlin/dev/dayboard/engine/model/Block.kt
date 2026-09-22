package dev.dayboard.engine.model

import java.time.LocalTime

enum class BlockKind { FOCUS, BREAK, MEAL, FIXED_EVENT, BUFFER }

/** FIXED pins to the wall clock. FLOW starts when the previous block ends. */
enum class Anchor { FIXED, FLOW }

enum class OverflowPolicy { PUSH, COMPRESS, TRUNCATE, SPILL }

enum class ColorRole { NEUTRAL, DEEP, ADMIN, REST, MEAL, APPOINTMENT, BUFFER, ACCENT_A, ACCENT_B, ACCENT_C }

data class Block(
    val id: Long = 0,
    val planDayId: Long = 0,
    val title: String,
    val notes: String? = null,
    val kind: BlockKind,
    val anchor: Anchor,
    val startLocal: LocalTime? = null,
    val plannedMinutes: Int,
    val minMinutes: Int? = null,
    val overflow: OverflowPolicy = OverflowPolicy.SPILL,
    val colorRole: ColorRole = ColorRole.NEUTRAL,
    val goalId: Long? = null,
    val orderIndex: Int
) {
    init {
        require(plannedMinutes >= 0) { "plannedMinutes must be >= 0, was $plannedMinutes" }
        require((anchor == Anchor.FIXED) == (startLocal != null)) {
            "startLocal is non-null exactly when anchor == FIXED (anchor=$anchor, startLocal=$startLocal)"
        }
        require(minMinutes == null || minMinutes in 0..plannedMinutes) {
            "minMinutes must be within 0..plannedMinutes, was $minMinutes of $plannedMinutes"
        }
    }
}

data class ChecklistItem(
    val id: Long = 0,
    val blockId: Long,
    val text: String,
    val orderIndex: Int
    // No doneAt: done-ness lives in the event log.
)
