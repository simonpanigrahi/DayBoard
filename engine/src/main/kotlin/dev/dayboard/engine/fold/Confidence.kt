package dev.dayboard.engine.fold

import dev.dayboard.engine.model.SessionEvent
import java.time.Duration
import kotlin.math.abs

/** A gap between two log points: how long it lasted, and whether the clocks agreed. */
internal data class Span(val millis: Long, val trusted: Boolean)

/**
 * Anything larger than this between the monotonic and wall deltas of one span is a
 * clock change, not scheduler jitter or NTP slew.
 */
private const val CLOCK_DISAGREEMENT_TOLERANCE_MS = 5_000L

internal fun spanBetween(previous: SessionEvent, next: SessionEvent): Span {
    if (previous.bootId != next.bootId) {
        // elapsedRealtime restarted at the reboot, so subtracting across the boundary
        // would produce garbage. The wall clock is the only usable ruler here.
        return Span(wallMillis(previous, next), trusted = false)
    }
    val monotonic = next.elapsedRealtime - previous.elapsedRealtime
    val agrees = abs(wallMillis(previous, next) - monotonic) <= CLOCK_DISAGREEMENT_TOLERANCE_MS
    return Span(monotonic.coerceAtLeast(0), trusted = monotonic >= 0 && agrees)
}

/** [nowElapsed] is elapsedRealtime from the same boot as the newest event. */
internal fun spanToNow(last: SessionEvent, nowElapsed: Long): Span {
    val monotonic = nowElapsed - last.elapsedRealtime
    return Span(monotonic.coerceAtLeast(0), trusted = monotonic >= 0)
}

private fun wallMillis(previous: SessionEvent, next: SessionEvent): Long =
    Duration.between(previous.wallAt, next.wallAt).toMillis().coerceAtLeast(0)
