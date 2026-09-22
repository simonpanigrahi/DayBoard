package dev.dayboard.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** A block plus the checklist it owns, as one commit-time unit. */
data class BlockWithItems(val block: BlockEntity, val items: List<ChecklistItemEntity>)

@Dao
interface PlanDao {

    @Query("SELECT * FROM plan_days WHERE date = :date LIMIT 1")
    fun observeDay(date: LocalDate): Flow<PlanDayEntity?>

    @Query("SELECT * FROM blocks WHERE planDayId = :planDayId ORDER BY orderIndex")
    fun observeBlocks(planDayId: Long): Flow<List<BlockEntity>>

    @Query(
        """
        SELECT items.* FROM checklist_items AS items
        JOIN blocks ON items.blockId = blocks.id
        WHERE blocks.planDayId = :planDayId
        ORDER BY items.blockId, items.orderIndex
        """
    )
    fun observeItems(planDayId: Long): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM plan_days WHERE date = :date LIMIT 1")
    suspend fun findDay(date: LocalDate): PlanDayEntity?

    @Query("SELECT * FROM blocks WHERE planDayId = :planDayId ORDER BY orderIndex")
    suspend fun blocksOf(planDayId: Long): List<BlockEntity>

    @Query(
        """
        SELECT items.* FROM checklist_items AS items
        JOIN blocks ON items.blockId = blocks.id
        WHERE blocks.planDayId = :planDayId
        ORDER BY items.blockId, items.orderIndex
        """
    )
    suspend fun itemsOf(planDayId: Long): List<ChecklistItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: PlanDayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: BlockEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ChecklistItemEntity>)

    /**
     * The single write path for the plan tables.
     *
     * Replacing the day cascades its blocks and checklist items away, and they are
     * written back carrying the ids the editor loaded. Ids therefore survive an edit,
     * which is what keeps already-logged events pointing at the right block.
     */
    @Transaction
    suspend fun commit(day: PlanDayEntity, blocks: List<BlockWithItems>): Long {
        val dayId = insertDay(day)
        blocks.forEach { (block, items) ->
            val blockId = insertBlock(block.copy(planDayId = dayId))
            if (items.isNotEmpty()) {
                insertItems(items.map { it.copy(blockId = blockId) })
            }
        }
        return dayId
    }
}
