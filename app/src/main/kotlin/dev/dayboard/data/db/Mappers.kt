package dev.dayboard.data.db

import dev.dayboard.engine.model.Block
import dev.dayboard.engine.model.ChecklistItem
import dev.dayboard.engine.model.Goal
import dev.dayboard.engine.model.PlanDay
import dev.dayboard.engine.model.SessionEvent

fun PlanDayEntity.toModel() = PlanDay(id, date, zoneId, source, createdAt)

fun BlockEntity.toModel() = Block(
    id = id,
    planDayId = planDayId,
    title = title,
    notes = notes,
    kind = kind,
    anchor = anchor,
    startLocal = startLocal,
    plannedMinutes = plannedMinutes,
    minMinutes = minMinutes,
    overflow = overflow,
    colorRole = colorRole,
    goalId = goalId,
    orderIndex = orderIndex
)

fun Block.toEntity(planDayId: Long) = BlockEntity(
    id = id,
    planDayId = planDayId,
    title = title,
    notes = notes,
    kind = kind,
    anchor = anchor,
    startLocal = startLocal,
    plannedMinutes = plannedMinutes,
    minMinutes = minMinutes,
    overflow = overflow,
    colorRole = colorRole,
    goalId = goalId,
    orderIndex = orderIndex
)

fun ChecklistItemEntity.toModel() = ChecklistItem(id, blockId, text, orderIndex)

fun ChecklistItem.toEntity(blockId: Long) = ChecklistItemEntity(id, blockId, text, orderIndex)

fun GoalEntity.toModel() = Goal(id, title, targetMinutesPerWeek, colorRole, archived)

fun Goal.toEntity() = GoalEntity(id, title, targetMinutesPerWeek, colorRole, archived)

fun SessionEventEntity.toModel() = SessionEvent(id, blockId, type, wallAt, elapsedRealtime, bootId, refId, meta)

fun SessionEvent.toEntity() = SessionEventEntity(id, blockId, type, wallAt, elapsedRealtime, bootId, refId, meta)
