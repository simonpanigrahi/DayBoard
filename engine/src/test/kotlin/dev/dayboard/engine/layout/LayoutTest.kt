package dev.dayboard.engine.layout

import dev.dayboard.engine.fixed
import dev.dayboard.engine.flow
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.engine.model.OverflowPolicy
import dev.dayboard.engine.t
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LayoutTest {

    private fun LayoutResult.at(title: String): ResolvedBlock =
        blocks.single { it.block.title == title }

    @Test
    fun `empty day lays out to nothing`() {
        val result = layout(emptyList(), t("08:00"))

        assertEquals(emptyList(), result.blocks)
        assertEquals(emptyList(), result.conflicts)
    }

    @Test
    fun `single flow block starts at day start`() {
        val result = layout(listOf(flow("Read", 45, order = 0)), t("08:00"))

        assertEquals(t("08:00"), result.at("Read").start)
        assertEquals(t("08:45"), result.at("Read").end)
        assertEquals(emptyList(), result.conflicts)
    }

    @Test
    fun `two anchors back to back do not conflict`() {
        val blocks = listOf(
            fixed("Class", at = "09:00", minutes = 60, order = 0),
            fixed("Lab", at = "10:00", minutes = 30, order = 1)
        )

        val result = layout(blocks, t("08:00"))

        assertEquals(t("09:00"), result.at("Class").start)
        assertEquals(t("10:00"), result.at("Lab").start)
        assertEquals(t("10:30"), result.at("Lab").end)
        assertEquals(emptyList(), result.conflicts)
    }

    @Test
    fun `day with no anchors chains every flow block from day start`() {
        val blocks = listOf(
            flow("Email", 30, order = 0),
            flow("Read", 45, order = 1),
            flow("Revise", 60, order = 2)
        )

        val result = layout(blocks, t("08:00"))

        assertEquals(listOf(t("08:00"), t("08:30"), t("09:15")), result.blocks.map { it.start })
        assertEquals(t("10:15"), result.at("Revise").end)
        assertEquals(emptyList(), result.conflicts)
    }

    @Test
    fun `flow run is laid out in order even when orderIndex is shuffled`() {
        val blocks = listOf(flow("Second", 30, order = 1), flow("First", 30, order = 0))

        val result = layout(blocks, t("08:00"))

        assertEquals(listOf("First", "Second"), result.blocks.map { it.block.title })
    }

    // A run that fits leaves the gap before the anchor untouched: the anchor is
    // pinned to the clock, not pulled earlier.
    @Test
    fun `flow run that fits leaves a gap before the anchor`() {
        val blocks = listOf(
            flow("Email", 30, order = 0),
            fixed("Gym", at = "09:30", minutes = 60, order = 1)
        )

        val result = layout(blocks, t("08:00"))

        assertEquals(t("08:30"), result.at("Email").end)
        assertEquals(t("09:30"), result.at("Gym").start)
        assertEquals(emptyList(), result.conflicts)
    }

    @Test
    fun `PUSH keeps durations and cascades past the anchor`() {
        val blocks = listOf(
            flow("Deep work", 60, order = 0, overflow = OverflowPolicy.PUSH),
            flow("Write up", 60, order = 1, overflow = OverflowPolicy.PUSH),
            fixed("Gym", at = "09:30", minutes = 60, order = 2)
        )

        val result = layout(blocks, t("08:00"))

        assertEquals(60, result.at("Deep work").minutes)
        assertEquals(60, result.at("Write up").minutes)
        assertEquals(t("10:00"), result.at("Write up").end)
        assertEquals(t("09:30"), result.at("Gym").start)

        val conflict = result.conflicts.single()
        assertEquals(ConflictKind.OVERFLOW_INTO_ANCHOR, conflict.kind)
        assertEquals(30, conflict.minutes)
        assertEquals(3L, conflict.anchorBlockId)
    }

    @Test
    fun `COMPRESS shrinks the run proportionally to fit the anchor`() {
        val blocks = listOf(
            flow("Deep work", 60, order = 0, overflow = OverflowPolicy.COMPRESS, minMinutes = 30),
            flow("Write up", 60, order = 1, overflow = OverflowPolicy.COMPRESS, minMinutes = 30),
            fixed("Gym", at = "09:30", minutes = 60, order = 2)
        )

        val result = layout(blocks, t("08:00"))

        assertEquals(45, result.at("Deep work").minutes)
        assertEquals(45, result.at("Write up").minutes)
        assertEquals(t("09:30"), result.at("Write up").end)
        assertEquals(Adjustment.COMPRESSED, result.at("Write up").adjustment)
        assertEquals(emptyList(), result.conflicts)
    }

    @Test
    fun `COMPRESS stops at the minMinutes floor and reports the residue`() {
        val blocks = listOf(
            flow("Deep work", 60, order = 0, overflow = OverflowPolicy.COMPRESS, minMinutes = 50),
            flow("Write up", 60, order = 1, overflow = OverflowPolicy.COMPRESS, minMinutes = 50),
            fixed("Gym", at = "09:30", minutes = 60, order = 2)
        )

        val result = layout(blocks, t("08:00"))

        assertEquals(50, result.at("Deep work").minutes)
        assertEquals(50, result.at("Write up").minutes)
        assertEquals(t("09:40"), result.at("Write up").end)

        assertTrue(result.conflicts.any { it.kind == ConflictKind.MIN_MINUTES_FLOOR })
        val overflow = result.conflicts.single { it.kind == ConflictKind.OVERFLOW_INTO_ANCHOR }
        assertEquals(10, overflow.minutes)
    }

    @Test
    fun `TRUNCATE cuts the last block at the anchor`() {
        val blocks = listOf(
            flow("Deep work", 60, order = 0, overflow = OverflowPolicy.TRUNCATE),
            flow("Browse", 60, order = 1, overflow = OverflowPolicy.TRUNCATE),
            fixed("Gym", at = "09:30", minutes = 60, order = 2)
        )

        val result = layout(blocks, t("08:00"))

        assertEquals(60, result.at("Deep work").minutes)
        assertEquals(30, result.at("Browse").minutes)
        assertEquals(t("09:30"), result.at("Browse").end)
        assertEquals(Adjustment.TRUNCATED, result.at("Browse").adjustment)

        val conflict = result.conflicts.single { it.kind == ConflictKind.BLOCK_TRUNCATED }
        assertEquals(30, conflict.minutes)
        assertEquals(2L, conflict.blockId)
    }

    @Test
    fun `SPILL eats the buffer before touching the work`() {
        val blocks = listOf(
            flow("Deep work", 60, order = 0, overflow = OverflowPolicy.SPILL),
            flow("Write up", 30, order = 1, overflow = OverflowPolicy.SPILL),
            flow("Buffer", 30, order = 2, overflow = OverflowPolicy.SPILL, kind = BlockKind.BUFFER),
            fixed("Gym", at = "09:30", minutes = 60, order = 3)
        )

        val result = layout(blocks, t("08:00"))

        assertEquals(60, result.at("Deep work").minutes)
        assertEquals(30, result.at("Write up").minutes)
        assertEquals(0, result.at("Buffer").minutes)
        assertEquals(Adjustment.SPILLED, result.at("Buffer").adjustment)
        assertEquals(t("09:30"), result.at("Gym").start)
        assertEquals(emptyList(), result.conflicts)
    }

    @Test
    fun `SPILL falls back to COMPRESS once the buffer is gone`() {
        val blocks = listOf(
            flow("Deep work", 60, order = 0, overflow = OverflowPolicy.SPILL, minMinutes = 30),
            flow("Buffer", 15, order = 1, overflow = OverflowPolicy.SPILL, kind = BlockKind.BUFFER),
            fixed("Gym", at = "08:45", minutes = 60, order = 2)
        )

        val result = layout(blocks, t("08:00"))

        assertEquals(0, result.at("Buffer").minutes)
        assertEquals(45, result.at("Deep work").minutes)
        assertEquals(Adjustment.COMPRESSED, result.at("Deep work").adjustment)
        assertEquals(emptyList(), result.conflicts)
    }

    @Test
    fun `an anchor that starts inside the previous anchor is reported`() {
        val blocks = listOf(
            fixed("Class", at = "09:00", minutes = 60, order = 0),
            fixed("Call", at = "09:30", minutes = 15, order = 1)
        )

        val result = layout(blocks, t("08:00"))

        assertEquals(t("09:30"), result.at("Call").start)
        val conflict = result.conflicts.single()
        assertEquals(ConflictKind.ANCHOR_OVERLAP, conflict.kind)
        assertEquals(30, conflict.minutes)
    }

    @Test
    fun `an anchor before day start is treated as the next day`() {
        val blocks = listOf(
            flow("Wind down", 30, order = 0),
            fixed("Sleep", at = "00:30", minutes = 60, order = 1)
        )

        val result = layout(blocks, t("22:00"))

        assertEquals(22 * 60 + 30, result.at("Wind down").endMinute)
        assertEquals(24 * 60 + 30, result.at("Sleep").startMinute)
        assertEquals(t("00:30"), result.at("Sleep").start)
        assertEquals(emptyList(), result.conflicts)
    }
}
