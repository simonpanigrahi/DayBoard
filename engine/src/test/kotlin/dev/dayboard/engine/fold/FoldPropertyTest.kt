package dev.dayboard.engine.fold

import dev.dayboard.engine.ELAPSED0
import dev.dayboard.engine.T0
import dev.dayboard.engine.min
import dev.dayboard.engine.model.Confidence
import dev.dayboard.engine.model.EventType
import dev.dayboard.engine.model.SessionEvent
import java.time.Duration
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The invariant that catches almost every fold bug: whatever the log says,
 * focused + break + untracked is exactly the elapsed span and no bucket is negative.
 *
 * Hand-rolled generation rather than a property library: the engine takes no
 * dependencies, and a fixed seed range makes a failure reproducible by number.
 */
class FoldPropertyTest {

    private data class Generated(
        val events: List<SessionEvent>,
        val now: Long,
        val expectedElapsedMs: Long,
        val rebootInsideBlock: Boolean
    )

    @Test
    fun `the ledger identity holds for every generated day`() {
        (0 until 500).forEach { seed ->
            val generated = generate(Random(seed))
            val actuals = foldEvents(generated.events, plannedMs = min(30), now = generated.now)

            assertTrue(actuals.focusedMs >= 0, "seed $seed: focused ${actuals.focusedMs}")
            assertTrue(actuals.breakMs >= 0, "seed $seed: break ${actuals.breakMs}")
            assertTrue(actuals.untrackedMs >= 0, "seed $seed: untracked ${actuals.untrackedMs}")
            assertTrue(actuals.overrunMs >= 0, "seed $seed: overrun ${actuals.overrunMs}")
            assertEquals(
                actuals.elapsedMs,
                actuals.focusedMs + actuals.breakMs + actuals.untrackedMs,
                "seed $seed: buckets do not sum to elapsed"
            )
            assertEquals(
                generated.expectedElapsedMs,
                actuals.elapsedMs,
                "seed $seed: elapsed span lost or double counted"
            )
            assertEquals(
                (actuals.elapsedMs - min(30)).coerceAtLeast(0),
                actuals.overrunMs,
                "seed $seed: overrun"
            )
            if (generated.rebootInsideBlock) {
                assertEquals(Confidence.PARTIAL, actuals.confidence, "seed $seed: reboot must lower confidence")
            }
        }
    }

    @Test
    fun `folding is independent of the order events arrive in`() {
        (0 until 200).forEach { seed ->
            val random = Random(seed)
            val generated = generate(random)
            val shuffled = generated.events.shuffled(random)

            assertEquals(
                foldEvents(generated.events, plannedMs = min(30), now = generated.now),
                foldEvents(shuffled, plannedMs = min(30), now = generated.now),
                "seed $seed"
            )
        }
    }

    /** Walks a legal state machine so the sequences are ones the app could really write. */
    private fun generate(random: Random): Generated {
        val steps = random.nextInt(1, 12)
        var mode = "IDLE"
        var wallMs = 0L
        var elapsed = ELAPSED0
        var boot = "boot-0"
        var boots = 0
        var rebootInsideBlock = false
        val events = mutableListOf<SessionEvent>()

        repeat(steps) { step ->
            if (mode == "ENDED") return@repeat
            val gap = min(random.nextLong(1, 16))
            wallMs += gap
            if (mode == "IDLE") {
                elapsed += gap
            } else if (random.nextInt(12) == 0) {
                // Reboot: elapsedRealtime restarts, the wall clock carries on.
                boot = "boot-${++boots}"
                elapsed = random.nextLong(1_000, 60_000)
                rebootInsideBlock = true
            } else {
                elapsed += gap
            }

            val type = when (mode) {
                "IDLE" -> EventType.BLOCK_START
                "FOCUS" -> listOf(
                    EventType.PAUSE, EventType.BREAK_START, EventType.AWAY_START,
                    EventType.ITEM_CHECK, EventType.BLOCK_EXTEND, EventType.BLOCK_END
                ).random(random)
                "PAUSED" -> listOf(EventType.RESUME, EventType.BLOCK_END).random(random)
                "BREAK" -> listOf(EventType.BREAK_END, EventType.BLOCK_END).random(random)
                else -> listOf(EventType.AWAY_END, EventType.PAUSE, EventType.BLOCK_END).random(random)
            }

            events += SessionEvent(
                id = step.toLong(),
                blockId = 1L,
                type = type,
                wallAt = T0.plusMillis(wallMs),
                elapsedRealtime = elapsed,
                bootId = boot,
                refId = if (type == EventType.ITEM_CHECK) random.nextLong(1, 4) else null
            )

            mode = when (type) {
                EventType.BLOCK_START -> "FOCUS"
                EventType.PAUSE -> "PAUSED"
                EventType.RESUME, EventType.BREAK_END, EventType.AWAY_END -> "FOCUS"
                EventType.BREAK_START -> "BREAK"
                EventType.AWAY_START -> "AWAY"
                EventType.BLOCK_END -> "ENDED"
                else -> mode
            }
        }

        val tail = min(random.nextLong(0, 16))
        val now = elapsed + if (mode == "ENDED" || mode == "IDLE") 0 else tail
        return Generated(events, now, expectedElapsed(events, now), rebootInsideBlock && events.size > 1)
    }

    /** Recomputed straight from the log, without the bucketing under test. */
    private fun expectedElapsed(events: List<SessionEvent>, now: Long): Long {
        val started = events.indexOfFirst { it.type == EventType.BLOCK_START }
        if (started < 0) return 0
        val window = events.drop(started)
        val terminal = window.indexOfFirst { it.type == EventType.BLOCK_END || it.type == EventType.BLOCK_SKIP }
        val counted = if (terminal < 0) window else window.take(terminal + 1)

        val betweenEvents = counted.zipWithNext().sumOf { (previous, next) ->
            if (previous.bootId != next.bootId) {
                Duration.between(previous.wallAt, next.wallAt).toMillis().coerceAtLeast(0)
            } else {
                (next.elapsedRealtime - previous.elapsedRealtime).coerceAtLeast(0)
            }
        }
        val tail = if (terminal < 0) (now - counted.last().elapsedRealtime).coerceAtLeast(0) else 0
        return betweenEvents + tail
    }
}
