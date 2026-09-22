package dev.dayboard.data.repo

import android.os.SystemClock
import dev.dayboard.data.db.EventDao
import dev.dayboard.data.db.SessionEventEntity
import dev.dayboard.data.db.toModel
import dev.dayboard.engine.model.EventType
import dev.dayboard.engine.model.SessionEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Append and read. There is no update and no delete on this type by design: correcting
 * the record means appending another event, never rewriting one.
 */
class EventRepository(
    private val dao: EventDao,
    private val bootId: suspend () -> String,
    private val now: () -> Instant = Instant::now,
    private val elapsedRealtime: () -> Long = SystemClock::elapsedRealtime
) {

    suspend fun append(
        type: EventType,
        blockId: Long? = null,
        refId: Long? = null,
        meta: String? = null
    ) {
        dao.insert(
            SessionEventEntity(
                blockId = blockId,
                type = type,
                // Both clocks on every event: wallAt orders and displays, elapsedRealtime
                // is the only thing durations are ever computed from.
                wallAt = now(),
                elapsedRealtime = elapsedRealtime(),
                bootId = bootId(),
                refId = refId,
                meta = meta
            )
        )
    }

    /**
     * A day's events, widened past midnight so a plan that runs into the small hours
     * still folds. Events from blocks outside the plan are ignored by resolve().
     */
    fun observeDay(date: LocalDate, zone: ZoneId): Flow<List<SessionEvent>> {
        val from = date.atStartOfDay(zone).toInstant()
        val to = date.plusDays(1).atStartOfDay(zone).plusHours(6).toInstant()
        return dao.observeBetween(from, to).map { events -> events.map { it.toModel() } }
    }
}
