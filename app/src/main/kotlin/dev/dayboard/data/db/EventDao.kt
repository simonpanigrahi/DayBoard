package dev.dayboard.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Append and read only. There is deliberately no @Update and no @Delete here: the
 * event log is the app's record of what actually happened, and nothing may rewrite it.
 */
@Dao
interface EventDao {

    @Insert
    suspend fun insert(event: SessionEventEntity): Long

    @Query("SELECT * FROM events WHERE wallAt >= :from AND wallAt < :to ORDER BY wallAt, id")
    fun observeBetween(from: Instant, to: Instant): Flow<List<SessionEventEntity>>

    @Query("SELECT * FROM events WHERE blockId = :blockId ORDER BY wallAt, id")
    fun observeForBlock(blockId: Long): Flow<List<SessionEventEntity>>

    @Query("SELECT * FROM events ORDER BY wallAt DESC, id DESC LIMIT 1")
    suspend fun latest(): SessionEventEntity?
}
