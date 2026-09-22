package dev.dayboard.engine

import dev.dayboard.engine.model.Anchor
import dev.dayboard.engine.model.Block
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.engine.model.OverflowPolicy
import java.time.LocalTime

fun t(text: String): LocalTime = LocalTime.parse(text)

fun flow(
    title: String,
    minutes: Int,
    order: Int,
    overflow: OverflowPolicy = OverflowPolicy.SPILL,
    minMinutes: Int? = null,
    kind: BlockKind = BlockKind.FOCUS,
    id: Long = order.toLong() + 1
) = Block(
    id = id,
    title = title,
    kind = kind,
    anchor = Anchor.FLOW,
    plannedMinutes = minutes,
    minMinutes = minMinutes,
    overflow = overflow,
    orderIndex = order
)

fun fixed(
    title: String,
    at: String,
    minutes: Int,
    order: Int,
    kind: BlockKind = BlockKind.FIXED_EVENT,
    id: Long = order.toLong() + 1
) = Block(
    id = id,
    title = title,
    kind = kind,
    anchor = Anchor.FIXED,
    startLocal = t(at),
    plannedMinutes = minutes,
    overflow = OverflowPolicy.PUSH,
    orderIndex = order
)

// --- event fixtures -------------------------------------------------------

val T0: java.time.Instant = java.time.Instant.parse("2026-09-21T08:00:00Z")

/** An arbitrary non-zero elapsedRealtime base: the device has been up a while. */
const val ELAPSED0 = 5_000_000L

const val BOOT_A = "boot-a"
const val BOOT_B = "boot-b"

fun min(n: Long): Long = n * 60_000L

fun ev(
    type: dev.dayboard.engine.model.EventType,
    atMs: Long,
    boot: String = BOOT_A,
    elapsed: Long = ELAPSED0 + atMs,
    refId: Long? = null,
    blockId: Long? = 1L,
    id: Long = atMs
) = dev.dayboard.engine.model.SessionEvent(
    id = id,
    blockId = blockId,
    type = type,
    wallAt = T0.plusMillis(atMs),
    elapsedRealtime = elapsed,
    bootId = boot,
    refId = refId
)
