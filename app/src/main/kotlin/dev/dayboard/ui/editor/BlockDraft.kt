package dev.dayboard.ui.editor

import dev.dayboard.engine.model.Anchor
import dev.dayboard.engine.model.Block
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.engine.model.ChecklistItem
import dev.dayboard.engine.model.ColorRole
import dev.dayboard.engine.model.OverflowPolicy
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicLong

/** Editable shape of a block. [key] is stable for the list; [id] is 0 until committed. */
data class BlockDraft(
    val key: Long,
    val id: Long,
    val title: String,
    val minutes: Int,
    val kind: BlockKind,
    val fixed: Boolean,
    val startLocal: LocalTime?,
    val items: List<ItemDraft>
) {
    fun toBlock(index: Int) = Block(
        id = id,
        title = title.ifBlank { "Untitled" },
        kind = kind,
        anchor = if (fixed) Anchor.FIXED else Anchor.FLOW,
        startLocal = if (fixed) startLocal ?: LocalTime.of(9, 0) else null,
        plannedMinutes = minutes,
        minMinutes = if (fixed) null else (minutes / 2),
        // An appointment is hard, so it pushes; work has soft edges and spills first.
        overflow = if (fixed) OverflowPolicy.PUSH else OverflowPolicy.SPILL,
        colorRole = kind.colorRole(),
        orderIndex = index
    )

    companion object {
        private val keys = AtomicLong(-1)

        fun new() = BlockDraft(
            key = keys.getAndDecrement(),
            id = 0,
            title = "",
            minutes = 30,
            kind = BlockKind.FOCUS,
            fixed = false,
            startLocal = null,
            items = emptyList()
        )

        fun of(block: Block, items: List<ChecklistItem>) = BlockDraft(
            key = block.id,
            id = block.id,
            title = block.title,
            minutes = block.plannedMinutes,
            kind = block.kind,
            fixed = block.anchor == Anchor.FIXED,
            startLocal = block.startLocal,
            items = items.map { ItemDraft(key = it.id, id = it.id, text = it.text) }
        )
    }
}

data class ItemDraft(val key: Long, val id: Long, val text: String) {
    companion object {
        private val keys = AtomicLong(-1)

        fun new() = ItemDraft(key = keys.getAndDecrement(), id = 0, text = "")
    }
}

fun BlockKind.colorRole(): ColorRole = when (this) {
    BlockKind.FOCUS -> ColorRole.DEEP
    BlockKind.BREAK -> ColorRole.REST
    BlockKind.MEAL -> ColorRole.MEAL
    BlockKind.FIXED_EVENT -> ColorRole.APPOINTMENT
    BlockKind.BUFFER -> ColorRole.BUFFER
}
