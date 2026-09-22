package dev.dayboard.data.repo

import dev.dayboard.data.db.BlockWithItems
import dev.dayboard.data.db.PlanDao
import dev.dayboard.data.db.PlanDayEntity
import dev.dayboard.data.db.toEntity
import dev.dayboard.data.db.toModel
import dev.dayboard.engine.ResolvedPlan
import dev.dayboard.engine.model.Block
import dev.dayboard.engine.model.ChecklistItem
import dev.dayboard.engine.model.PlanDay
import dev.dayboard.engine.model.PlanSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** A block together with the checklist it owns, before either has an id. */
data class PlannedBlock(val block: Block, val items: List<ChecklistItem> = emptyList())

data class DayPlan(
    val day: PlanDay,
    val blocks: List<Block>,
    val checklists: Map<Long, List<ChecklistItem>>
) {
    fun layout(dayStart: LocalTime): ResolvedPlan =
        ResolvedPlan.from(date = day.date, dayStart = dayStart, blocks = blocks, checklists = checklists)
}

class PlanRepository(private val planDao: PlanDao) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(date: LocalDate): Flow<DayPlan?> =
        planDao.observeDay(date).flatMapLatest { day ->
            if (day == null) {
                flowOf(null)
            } else {
                combine(planDao.observeBlocks(day.id), planDao.observeItems(day.id)) { blocks, items ->
                    DayPlan(
                        day = day.toModel(),
                        blocks = blocks.map { it.toModel() },
                        checklists = items.groupBy({ it.blockId }, { it.toModel() })
                    )
                }
            }
        }

    suspend fun load(date: LocalDate): DayPlan? {
        val day = planDao.findDay(date) ?: return null
        return DayPlan(
            day = day.toModel(),
            blocks = planDao.blocksOf(day.id).map { it.toModel() },
            checklists = planDao.itemsOf(day.id).groupBy({ it.blockId }, { it.toModel() })
        )
    }

    /**
     * The only write path into the plan tables. Everything that happens afterwards is
     * an appended event, never an edit of what was committed here.
     */
    suspend fun commit(
        date: LocalDate,
        zone: ZoneId,
        blocks: List<PlannedBlock>,
        source: PlanSource = PlanSource.MANUAL,
        existingDayId: Long = 0
    ): Long = planDao.commit(
        day = PlanDayEntity(
            id = existingDayId,
            date = date,
            zoneId = zone.id,
            source = source,
            createdAt = Instant.now()
        ),
        blocks = blocks.mapIndexed { index, planned ->
            val block = planned.block.copy(orderIndex = index)
            BlockWithItems(
                block = block.toEntity(planDayId = existingDayId),
                items = planned.items.mapIndexed { itemIndex, item ->
                    item.copy(orderIndex = itemIndex).toEntity(blockId = block.id)
                }
            )
        }
    )
}
