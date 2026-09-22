package dev.dayboard.ui.editor

import dev.dayboard.engine.model.Anchor
import dev.dayboard.engine.model.Block
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.engine.model.ChecklistItem
import dev.dayboard.engine.model.ColorRole
import dev.dayboard.engine.model.DraftBlock
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
    val items: List<ItemDraft>,
    val tag: String? = null,
    val colorRole: ColorRole = defaultColorFor(kind, tag)
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
        colorRole = colorRole,
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

        /** Straight from an importer: the draft has no ids yet. */
        fun of(imported: DraftBlock) = BlockDraft(
            key = keys.getAndDecrement(),
            id = 0,
            title = imported.title,
            minutes = imported.plannedMinutes,
            kind = imported.kind,
            fixed = imported.anchor == Anchor.FIXED,
            startLocal = imported.startLocal,
            items = imported.checklist.map { ItemDraft.new().copy(text = it) },
            tag = imported.tag,
            colorRole = defaultColorFor(imported.kind, imported.tag)
        )

        /** Another day's block, taken as a fresh row: no ids, so committing cannot move it. */
        fun copyFrom(block: Block, items: List<ChecklistItem>) = BlockDraft(
            key = keys.getAndDecrement(),
            id = 0,
            title = block.title,
            minutes = block.plannedMinutes,
            kind = block.kind,
            fixed = block.anchor == Anchor.FIXED,
            startLocal = block.startLocal,
            items = items.map { ItemDraft.new().copy(text = it.text) },
            colorRole = block.colorRole
        )

        fun of(block: Block, items: List<ChecklistItem>) = BlockDraft(
            key = block.id,
            id = block.id,
            title = block.title,
            minutes = block.plannedMinutes,
            kind = block.kind,
            fixed = block.anchor == Anchor.FIXED,
            startLocal = block.startLocal,
            items = items.map { ItemDraft(key = it.id, id = it.id, text = it.text) },
            colorRole = block.colorRole
        )
    }
}

data class ItemDraft(val key: Long, val id: Long, val text: String) {
    companion object {
        private val keys = AtomicLong(-1)

        fun new() = ItemDraft(key = keys.getAndDecrement(), id = 0, text = "")
    }
}

/** Colours cycle in this order when the user taps a block's swatch. */
val PALETTE = listOf(
    ColorRole.DEEP, ColorRole.ADMIN, ColorRole.ACCENT_A, ColorRole.ACCENT_B, ColorRole.ACCENT_C,
    ColorRole.REST, ColorRole.MEAL, ColorRole.APPOINTMENT, ColorRole.BUFFER, ColorRole.NEUTRAL
)

fun ColorRole.nextColor(): ColorRole = PALETTE[(PALETTE.indexOf(this).coerceAtLeast(0) + 1) % PALETTE.size]

/** A tag colours its blocks consistently, so #deep looks the same every day. */
fun defaultColorFor(kind: BlockKind, tag: String?): ColorRole = when {
    kind != BlockKind.FOCUS -> kind.colorRole()
    tag != null -> TAG_COLORS[Math.floorMod(tag.lowercase().hashCode(), TAG_COLORS.size)]
    else -> ColorRole.DEEP
}

private val TAG_COLORS = listOf(
    ColorRole.DEEP, ColorRole.ADMIN, ColorRole.ACCENT_A, ColorRole.ACCENT_B, ColorRole.ACCENT_C
)

fun BlockKind.colorRole(): ColorRole = when (this) {
    BlockKind.FOCUS -> ColorRole.DEEP
    BlockKind.BREAK -> ColorRole.REST
    BlockKind.MEAL -> ColorRole.MEAL
    BlockKind.FIXED_EVENT -> ColorRole.APPOINTMENT
    BlockKind.BUFFER -> ColorRole.BUFFER
}
