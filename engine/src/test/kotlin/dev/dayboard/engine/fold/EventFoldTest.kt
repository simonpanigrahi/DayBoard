package dev.dayboard.engine.fold

import dev.dayboard.engine.BOOT_A
import dev.dayboard.engine.BOOT_B
import dev.dayboard.engine.ELAPSED0
import dev.dayboard.engine.ev
import dev.dayboard.engine.min
import dev.dayboard.engine.model.BlockActuals
import dev.dayboard.engine.model.Confidence
import dev.dayboard.engine.model.EventType.AWAY_END
import dev.dayboard.engine.model.EventType.AWAY_START
import dev.dayboard.engine.model.EventType.BLOCK_END
import dev.dayboard.engine.model.EventType.BLOCK_EXTEND
import dev.dayboard.engine.model.EventType.BLOCK_SKIP
import dev.dayboard.engine.model.EventType.BLOCK_START
import dev.dayboard.engine.model.EventType.BREAK_END
import dev.dayboard.engine.model.EventType.BREAK_START
import dev.dayboard.engine.model.EventType.DAY_START
import dev.dayboard.engine.model.EventType.ITEM_CHECK
import dev.dayboard.engine.model.EventType.ITEM_UNCHECK
import dev.dayboard.engine.model.EventType.PAUSE
import dev.dayboard.engine.model.EventType.RESUME
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventFoldTest {

    private fun assertLedgerHolds(actuals: BlockActuals) {
        assertEquals(
            actuals.focusedMs + actuals.breakMs + actuals.untrackedMs,
            actuals.elapsedMs,
            "elapsed must equal focused + break + untracked"
        )
        assertTrue(actuals.focusedMs >= 0, "focusedMs was ${actuals.focusedMs}")
        assertTrue(actuals.breakMs >= 0, "breakMs was ${actuals.breakMs}")
        assertTrue(actuals.untrackedMs >= 0, "untrackedMs was ${actuals.untrackedMs}")
        assertTrue(actuals.overrunMs >= 0, "overrunMs was ${actuals.overrunMs}")
    }

    @Test
    fun `nothing logged is an empty ledger`() {
        val actuals = foldEvents(emptyList(), plannedMs = min(30), now = ELAPSED0)

        assertEquals(0, actuals.elapsedMs)
        assertEquals(Confidence.FULL, actuals.confidence)
        assertLedgerHolds(actuals)
    }

    @Test
    fun `time before BLOCK_START is not counted`() {
        val events = listOf(
            ev(DAY_START, atMs = 0, blockId = null),
            ev(BLOCK_START, atMs = min(10)),
            ev(BLOCK_END, atMs = min(35))
        )

        val actuals = foldEvents(events, plannedMs = min(30), now = ELAPSED0 + min(40))

        assertEquals(min(25), actuals.focusedMs)
        assertEquals(min(25), actuals.elapsedMs)
        assertLedgerHolds(actuals)
    }

    @Test
    fun `an open block counts up to now`() {
        val events = listOf(ev(BLOCK_START, atMs = 0))

        val actuals = foldEvents(events, plannedMs = min(30), now = ELAPSED0 + min(12))

        assertEquals(min(12), actuals.focusedMs)
        assertEquals(0, actuals.overrunMs)
        assertLedgerHolds(actuals)
    }

    @Test
    fun `overrun is elapsed past planned, and never negative`() {
        val events = listOf(ev(BLOCK_START, atMs = 0), ev(BLOCK_END, atMs = min(38)))

        val over = foldEvents(events, plannedMs = min(30), now = ELAPSED0 + min(38))
        assertEquals(min(8), over.overrunMs)

        val under = foldEvents(events, plannedMs = min(45), now = ELAPSED0 + min(38))
        assertEquals(0, under.overrunMs)
        assertLedgerHolds(over)
        assertLedgerHolds(under)
    }

    @Test
    fun `a break is its own bucket and suspends focus`() {
        val events = listOf(
            ev(BLOCK_START, atMs = 0),
            ev(BREAK_START, atMs = min(20)),
            ev(BREAK_END, atMs = min(30)),
            ev(BLOCK_END, atMs = min(40))
        )

        val actuals = foldEvents(events, plannedMs = min(30), now = ELAPSED0 + min(40))

        assertEquals(min(30), actuals.focusedMs)
        assertEquals(min(10), actuals.breakMs)
        assertEquals(0, actuals.untrackedMs)
        assertLedgerHolds(actuals)
    }

    @Test
    fun `pause without resume leaves the rest untracked`() {
        val events = listOf(ev(BLOCK_START, atMs = 0), ev(PAUSE, atMs = min(10)))

        val actuals = foldEvents(events, plannedMs = min(30), now = ELAPSED0 + min(30))

        assertEquals(min(10), actuals.focusedMs)
        assertEquals(min(20), actuals.untrackedMs)
        assertEquals(min(30), actuals.elapsedMs)
        assertLedgerHolds(actuals)
    }

    @Test
    fun `break without end keeps counting as break`() {
        val events = listOf(ev(BLOCK_START, atMs = 0), ev(BREAK_START, atMs = min(10)))

        val actuals = foldEvents(events, plannedMs = min(30), now = ELAPSED0 + min(30))

        assertEquals(min(10), actuals.focusedMs)
        assertEquals(min(20), actuals.breakMs)
        assertLedgerHolds(actuals)
    }

    @Test
    fun `away without return is untracked, and pause_resume round trips`() {
        val events = listOf(
            ev(BLOCK_START, atMs = 0),
            ev(PAUSE, atMs = min(5)),
            ev(RESUME, atMs = min(10)),
            ev(AWAY_START, atMs = min(20))
        )

        val actuals = foldEvents(events, plannedMs = min(30), now = ELAPSED0 + min(30))

        assertEquals(min(15), actuals.focusedMs)
        assertEquals(min(15), actuals.untrackedMs)
        assertLedgerHolds(actuals)
    }

    @Test
    fun `going away during a break is still break time`() {
        val events = listOf(
            ev(BLOCK_START, atMs = 0),
            ev(BREAK_START, atMs = min(10)),
            ev(AWAY_START, atMs = min(12)),
            ev(AWAY_END, atMs = min(18)),
            ev(BREAK_END, atMs = min(20))
        )

        val actuals = foldEvents(events, plannedMs = min(30), now = ELAPSED0 + min(20))

        assertEquals(min(10), actuals.focusedMs)
        assertEquals(min(10), actuals.breakMs)
        assertEquals(0, actuals.untrackedMs)
        assertLedgerHolds(actuals)
    }

    @Test
    fun `BLOCK_SKIP closes the block like BLOCK_END`() {
        val events = listOf(ev(BLOCK_START, atMs = 0), ev(BLOCK_SKIP, atMs = min(5)))

        val actuals = foldEvents(events, plannedMs = min(30), now = ELAPSED0 + min(60))

        assertEquals(min(5), actuals.elapsedMs)
        assertLedgerHolds(actuals)
    }

    @Test
    fun `BLOCK_EXTEND does not move any bucket`() {
        val events = listOf(
            ev(BLOCK_START, atMs = 0),
            ev(BLOCK_EXTEND, atMs = min(30)),
            ev(BLOCK_END, atMs = min(35))
        )

        val actuals = foldEvents(events, plannedMs = min(30), now = ELAPSED0 + min(35))

        assertEquals(min(35), actuals.focusedMs)
        assertLedgerHolds(actuals)
    }

    @Test
    fun `out of order events fold the same as ordered ones`() {
        val ordered = listOf(
            ev(BLOCK_START, atMs = 0),
            ev(BREAK_START, atMs = min(20)),
            ev(BREAK_END, atMs = min(30)),
            ev(BLOCK_END, atMs = min(40))
        )

        val expected = foldEvents(ordered, plannedMs = min(30), now = ELAPSED0 + min(40))
        val shuffled = foldEvents(ordered.reversed(), plannedMs = min(30), now = ELAPSED0 + min(40))

        assertEquals(expected, shuffled)
        assertLedgerHolds(shuffled)
    }

    // elapsedRealtime restarts at reboot, so the span across the boundary is taken
    // from the wall clock instead and the whole block drops to PARTIAL.
    @Test
    fun `a reboot mid block falls back to wall time and marks PARTIAL`() {
        val events = listOf(
            ev(BLOCK_START, atMs = 0, boot = BOOT_A),
            ev(BLOCK_END, atMs = min(30), boot = BOOT_B, elapsed = 12_000, id = 99)
        )

        val actuals = foldEvents(events, plannedMs = min(30), now = 12_000)

        assertEquals(min(30), actuals.focusedMs)
        assertEquals(Confidence.PARTIAL, actuals.confidence)
        assertLedgerHolds(actuals)
    }

    @Test
    fun `a wall clock jump inside one boot marks PARTIAL without corrupting durations`() {
        val events = listOf(
            ev(BLOCK_START, atMs = 0),
            // The user moved the clock forward an hour; elapsedRealtime disagrees.
            ev(BLOCK_END, atMs = min(70), elapsed = ELAPSED0 + min(10), id = 7)
        )

        val actuals = foldEvents(events, plannedMs = min(30), now = ELAPSED0 + min(10))

        assertEquals(min(10), actuals.focusedMs)
        assertEquals(Confidence.PARTIAL, actuals.confidence)
        assertLedgerHolds(actuals)
    }

    @Test
    fun `checklist ticks net out`() {
        val events = listOf(
            ev(BLOCK_START, atMs = 0),
            ev(ITEM_CHECK, atMs = min(1), refId = 10),
            ev(ITEM_CHECK, atMs = min(2), refId = 11),
            ev(ITEM_UNCHECK, atMs = min(3), refId = 10),
            ev(ITEM_CHECK, atMs = min(4), refId = 12)
        )

        val actuals = foldEvents(events, plannedMs = min(30), now = ELAPSED0 + min(5), itemsTotal = 5)

        assertEquals(2, actuals.itemsDone)
        assertEquals(5, actuals.itemsTotal)
        assertLedgerHolds(actuals)
    }
}
