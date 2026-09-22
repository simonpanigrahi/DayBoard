package dev.dayboard.engine.model

import java.time.Instant

enum class EventType {
    BLOCK_START, BLOCK_END, BLOCK_SKIP, BLOCK_EXTEND,
    PAUSE, RESUME,
    BREAK_START, BREAK_END,
    AWAY_START, AWAY_END,
    ITEM_CHECK, ITEM_UNCHECK,
    DAY_START, DAY_END
}

data class SessionEvent(
    val id: Long = 0,
    val blockId: Long?,
    val type: EventType,
    val wallAt: Instant,
    val elapsedRealtime: Long,
    val bootId: String,
    val refId: Long? = null,
    val meta: String? = null
)
