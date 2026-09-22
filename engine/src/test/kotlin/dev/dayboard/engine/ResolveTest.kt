package dev.dayboard.engine

import dev.dayboard.engine.model.BoardState
import dev.dayboard.engine.model.ChecklistItem
import dev.dayboard.engine.model.Confidence
import dev.dayboard.engine.model.EventType
import dev.dayboard.engine.model.NudgeKind
import dev.dayboard.engine.model.SegmentState
import dev.dayboard.engine.model.SessionEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResolveTest {

    private val zone: ZoneId = ZoneId.of("Asia/Kolkata")
    private val date: LocalDate = LocalDate.of(2026, 9, 21)

    private val blocks = listOf(
        flow("Email", 30, order = 0),
        flow("Read paper", 45, order = 1),
        fixed("Class", at = "10:00", minutes = 60, order = 2),
        flow("Revise", 120, order = 3),
        fixed("Gym", at = "17:30", minutes = 60, order = 4),
        flow("Wrap up", 30, order = 5)
    )

    private val plan = ResolvedPlan.from(
        date = date,
        dayStart = t("08:00"),
        blocks = blocks,
        checklists = mapOf(
            1L to listOf(
                ChecklistItem(id = 100, blockId = 1, text = "Mail Dr. Puhan", orderIndex = 0),
                ChecklistItem(id = 101, blockId = 1, text = "Reply to lab thread", orderIndex = 1)
            )
        )
    )

    private fun at(hhmm: String): Instant = date.atTime(t(hhmm)).atZone(zone).toInstant()

    private fun board(hhmm: String, events: List<SessionEvent> = emptyList(), nowElapsed: Long? = null): BoardState =
        resolve(plan, events, at(hhmm), zone, nowElapsed)

    private fun event(
        type: EventType,
        blockId: Long,
        hhmm: String,
        elapsedMinutes: Long,
        refId: Long? = null,
        id: Long = elapsedMinutes,
        meta: String? = null
    ) = SessionEvent(
        id = id,
        blockId = blockId,
        type = type,
        wallAt = at(hhmm),
        elapsedRealtime = ELAPSED0 + min(elapsedMinutes),
        bootId = BOOT_A,
        refId = refId,
        meta = meta
    )

    /** The plan restated independently of the layout sweep, as a schedule the test owns. */
    private fun expectedAt(minuteOfDay: Int): Pair<String?, String?> = when (minuteOfDay) {
        in 480 until 510 -> "Email" to "Read paper"
        in 510 until 555 -> "Read paper" to "Class"
        in 555 until 600 -> null to "Class"
        in 600 until 660 -> "Class" to "Revise"
        in 660 until 780 -> "Revise" to "Gym"
        in 780 until 1050 -> null to "Gym"
        in 1050 until 1110 -> "Gym" to "Wrap up"
        in 1110 until 1140 -> "Wrap up" to null
        else -> null to null
    }

    @Test
    fun `the plan lays out without conflicts`() {
        assertEquals(emptyList(), plan.conflicts)
        assertEquals(t("08:00"), plan.blocks.first().start)
        assertEquals(t("19:00"), plan.blocks.last().end)
    }

    @Test
    fun `current and next are right at every minute from 08-00 to 20-00`() {
        (480..1200).forEach { minuteOfDay ->
            val now = date.atTime(minuteOfDay / 60, minuteOfDay % 60).atZone(zone).toInstant()
            val state = resolve(plan, emptyList(), now, zone)
            val (expectedCurrent, expectedNext) = expectedAt(minuteOfDay)

            assertEquals(
                expectedCurrent,
                state.current?.resolved?.block?.title,
                "current at minute $minuteOfDay"
            )
            assertEquals(
                expectedNext,
                state.next?.block?.title,
                "next at minute $minuteOfDay"
            )
        }
    }

    @Test
    fun `before the day starts there is no current block`() {
        val state = board("07:00")

        assertNull(state.current)
        assertEquals("Email", state.next?.block?.title)
        assertEquals(listOf("Read paper", "Class", "Revise"), state.later.map { it.block.title })
        assertEquals(emptyList(), state.completed)
    }

    @Test
    fun `later holds the blocks after next and runs out at the end of the day`() {
        assertEquals(listOf("Revise", "Gym", "Wrap up"), board("08:35").later.map { it.block.title })
        assertEquals(emptyList(), board("18:45").later.map { it.block.title })
    }

    @Test
    fun `scheduled blocks in the past are completed`() {
        val state = board("10:30")

        assertEquals(listOf("Email", "Read paper"), state.completed.map { it.resolved.block.title })
        assertEquals("Class", state.current?.resolved?.block?.title)
    }

    @Test
    fun `a block still running past its planned end stays current`() {
        val events = listOf(event(EventType.BLOCK_START, blockId = 1, hhmm = "08:00", elapsedMinutes = 0))

        val state = board("08:45", events, nowElapsed = ELAPSED0 + min(45))

        val current = assertNotNull(state.current)
        assertEquals("Email", current.resolved.block.title)
        assertEquals("Read paper", state.next?.block?.title)
        assertEquals(min(45), current.actuals.focusedMs)
        assertEquals(min(15), current.overrunMs)
        assertEquals(0, current.remainingMs)
        assertEquals(1.0f, current.progress)
        assertEquals(NudgeKind.OVERRUN, state.nudge?.kind)
        assertEquals(15, state.nudge?.minutes)
    }

    @Test
    fun `an untracked block counts down against the schedule`() {
        val state = board("08:10")

        val current = assertNotNull(state.current)
        assertEquals(min(20), current.remainingMs)
        assertEquals(0, current.overrunMs)
        assertNull(state.nudge)
    }

    @Test
    fun `ending a block early moves it to completed and advances the board`() {
        val events = listOf(
            event(EventType.BLOCK_START, blockId = 1, hhmm = "08:00", elapsedMinutes = 0),
            event(EventType.ITEM_CHECK, blockId = 1, hhmm = "08:05", elapsedMinutes = 5, refId = 100),
            event(EventType.BLOCK_END, blockId = 1, hhmm = "08:20", elapsedMinutes = 20),
            event(EventType.BLOCK_START, blockId = 2, hhmm = "08:20", elapsedMinutes = 20, id = 21)
        )

        val state = board("08:25", events, nowElapsed = ELAPSED0 + min(25))

        assertEquals("Read paper", state.current?.resolved?.block?.title)
        assertEquals("Class", state.next?.block?.title)

        val email = state.completed.single { it.resolved.block.title == "Email" }
        assertEquals(min(20), email.actuals.focusedMs)
        assertEquals(1, email.actuals.itemsDone)
        assertEquals(2, email.actuals.itemsTotal)
        assertEquals(at("08:20"), email.endedAt)
    }

    @Test
    fun `a break shows on the active block and lands in the day totals`() {
        val events = listOf(
            event(EventType.BLOCK_START, blockId = 1, hhmm = "08:00", elapsedMinutes = 0),
            event(EventType.BREAK_START, blockId = 1, hhmm = "08:10", elapsedMinutes = 10)
        )

        val state = board("08:20", events, nowElapsed = ELAPSED0 + min(20))

        val current = assertNotNull(state.current)
        assertTrue(current.onBreak)
        assertEquals(min(10), current.actuals.breakMs)
        assertEquals(min(10), state.dayTotals.breakMs)
        assertEquals(min(10), state.dayTotals.focusedMs)
        assertEquals(min(20), state.dayTotals.elapsedMs)
    }

    @Test
    fun `day totals sum the plan and carry the worst confidence`() {
        val events = listOf(
            event(EventType.BLOCK_START, blockId = 1, hhmm = "08:00", elapsedMinutes = 0),
            SessionEvent(
                id = 9, blockId = 1, type = EventType.BLOCK_END, wallAt = at("08:25"),
                elapsedRealtime = 4_000, bootId = "boot-after-reboot"
            )
        )

        val state = board("08:30", events, nowElapsed = 4_000 + min(5))

        assertEquals(min(345), state.dayTotals.plannedMs)
        assertEquals(Confidence.PARTIAL, state.dayTotals.confidence)
    }

    @Test
    fun `the rest nudge appears after a long unbroken stretch of focus`() {
        val events = listOf(event(EventType.BLOCK_START, blockId = 4, hhmm = "11:00", elapsedMinutes = 0))

        val state = board("11:55", events, nowElapsed = ELAPSED0 + min(55))

        assertEquals(NudgeKind.REST_SUGGESTION, state.nudge?.kind)
        assertEquals(55, state.nudge?.minutes)
    }

    @Test
    fun `the ribbon covers the waking day in order with a marked current segment`() {
        val state = board("10:30")

        assertEquals(blocks.size, state.ribbon.size)
        assertEquals(blocks.map { it.id }, state.ribbon.map { it.blockId })
        assertTrue(state.ribbon.all { it.startFraction in 0f..1f && it.endFraction in 0f..1f })
        assertTrue(state.ribbon.zipWithNext().all { (a, b) -> a.startFraction <= b.startFraction })
        assertEquals(
            listOf(SegmentState.PAST, SegmentState.PAST, SegmentState.CURRENT, SegmentState.FUTURE),
            state.ribbon.take(4).map { it.state }
        )
    }

    @Test
    fun `layout conflicts are carried through to the board`() {
        val conflicted = ResolvedPlan.from(
            date = date,
            dayStart = t("08:00"),
            blocks = listOf(
                flow("Deep work", 120, order = 0, overflow = dev.dayboard.engine.model.OverflowPolicy.PUSH),
                fixed("Class", at = "09:00", minutes = 60, order = 1)
            )
        )

        val state = resolve(conflicted, emptyList(), at("08:30"), zone)

        assertEquals(conflicted.conflicts, state.conflicts)
        assertTrue(state.conflicts.isNotEmpty())
    }

    @Test
    fun `the clock is reported in the plan's zone`() {
        val state = board("14:32")

        assertEquals(zone, state.clock.zone)
        assertEquals(14, state.clock.hour)
        assertEquals(32, state.clock.minute)
    }

    @Test
    fun `the now needle sits proportionally across the ribbon window`() {
        // 06:00 to 23:00 is the window, so 10:30 is 270 of its 1020 minutes.
        assertEquals(270f / 1020f, board("10:30").nowFraction)
        assertEquals(0f, board("05:00").nowFraction)
        assertEquals(1f, board("23:30").nowFraction)
    }

    @Test
    fun `extending the running block moves the tail and clears the overrun`() {
        val events = listOf(
            event(EventType.BLOCK_START, blockId = 1, hhmm = "08:00", elapsedMinutes = 0),
            event(EventType.BLOCK_EXTEND, blockId = 1, hhmm = "08:30", elapsedMinutes = 30, id = 31, meta = "5")
        )

        val state = board("08:33", events, nowElapsed = ELAPSED0 + min(33))

        val current = assertNotNull(state.current)
        assertEquals("Email", current.resolved.block.title)
        assertEquals(35, current.resolved.minutes)
        assertEquals(min(2), current.remainingMs)
        assertEquals(0, current.overrunMs)
        assertNull(state.nudge)
        assertEquals(t("08:35"), state.next?.start)
    }

    @Test
    fun `an extension leaves a later anchor pinned where it was`() {
        val events = listOf(
            event(EventType.BLOCK_START, blockId = 2, hhmm = "08:30", elapsedMinutes = 0),
            event(EventType.BLOCK_EXTEND, blockId = 2, hhmm = "09:10", elapsedMinutes = 40, id = 41, meta = "15")
        )

        val state = board("09:15", events, nowElapsed = ELAPSED0 + min(45))

        assertEquals("Read paper", state.current?.resolved?.block?.title)
        assertEquals("Class", state.next?.block?.title)
        assertEquals(t("10:00"), state.next?.start)
    }

    @Test
    fun `START targets the current block until it is running`() {
        assertEquals("Email", board("08:10").startable?.block?.title)

        val started = listOf(event(EventType.BLOCK_START, blockId = 1, hhmm = "08:00", elapsedMinutes = 0))
        assertNull(board("08:10", started, nowElapsed = ELAPSED0 + min(10)).startable)
    }

    @Test
    fun `in a gap START targets the next block`() {
        assertEquals("Class", board("09:30").startable?.block?.title)
    }

    // Running late is the normal case, not an error: the whole plan being in the past
    // must not leave the board with nothing to start.
    @Test
    fun `after the whole plan has gone by START still targets the first unstarted block`() {
        val state = board("21:00")

        assertNull(state.current)
        assertNull(state.next)
        assertEquals("Email", state.startable?.block?.title)
    }

    @Test
    fun `a block already closed is never startable again`() {
        val events = listOf(
            event(EventType.BLOCK_START, blockId = 1, hhmm = "08:00", elapsedMinutes = 0),
            event(EventType.BLOCK_END, blockId = 1, hhmm = "08:20", elapsedMinutes = 20)
        )

        assertEquals("Read paper", board("21:00", events, nowElapsed = ELAPSED0 + min(20)).startable?.block?.title)
    }
}
