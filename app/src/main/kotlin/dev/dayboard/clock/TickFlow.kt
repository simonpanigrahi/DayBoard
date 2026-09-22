package dev.dayboard.clock

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.time.Instant

const val SECOND_MS = 1_000L
const val MINUTE_MS = 60_000L

/**
 * Emits on wall-clock boundaries rather than every [periodMs] from whenever it started.
 *
 * `delay(1000)` in a loop drifts: each iteration pays the emit plus scheduling slop, so
 * the displayed clock slowly slides off the real second and visibly skips one every
 * minute or so. Sleeping to the next boundary instead stays phase-locked indefinitely
 * and self-heals after any hiccup.
 */
fun tickFlow(periodMs: Long): Flow<Instant> = flow {
    while (currentCoroutineContext().isActive) {
        val now = System.currentTimeMillis()
        emit(Instant.ofEpochMilli(now))
        val next = ((now / periodMs) + 1) * periodMs
        delay((next - System.currentTimeMillis()).coerceAtLeast(1))
    }
}
