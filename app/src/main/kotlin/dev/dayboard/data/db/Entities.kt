package dev.dayboard.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.dayboard.engine.model.Anchor
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.engine.model.ColorRole
import dev.dayboard.engine.model.EventType
import dev.dayboard.engine.model.OverflowPolicy
import dev.dayboard.engine.model.PlanSource
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "plan_days", indices = [Index(value = ["date"], unique = true)])
data class PlanDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val zoneId: String,
    val source: PlanSource,
    val createdAt: Instant
)

@Entity(
    tableName = "blocks",
    indices = [Index(value = ["planDayId", "orderIndex"])],
    foreignKeys = [
        ForeignKey(
            entity = PlanDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["planDayId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planDayId: Long,
    val title: String,
    val notes: String? = null,
    val kind: BlockKind,
    val anchor: Anchor,
    val startLocal: LocalTime? = null,
    val plannedMinutes: Int,
    val minMinutes: Int? = null,
    val overflow: OverflowPolicy,
    val colorRole: ColorRole,
    val goalId: Long? = null,
    val orderIndex: Int
)

@Entity(
    tableName = "checklist_items",
    indices = [Index(value = ["blockId", "orderIndex"])],
    foreignKeys = [
        ForeignKey(
            entity = BlockEntity::class,
            parentColumns = ["id"],
            childColumns = ["blockId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val blockId: Long,
    val text: String,
    val orderIndex: Int
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetMinutesPerWeek: Int? = null,
    val colorRole: ColorRole = ColorRole.NEUTRAL,
    val archived: Boolean = false
)

/**
 * No foreign key to blocks on purpose: the log outlives the plan it refers to, so
 * re-committing a day must never cascade a delete into recorded history.
 */
@Entity(tableName = "events", indices = [Index("blockId"), Index("wallAt")])
data class SessionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val blockId: Long?,
    val type: EventType,
    val wallAt: Instant,
    val elapsedRealtime: Long,
    val bootId: String,
    val refId: Long? = null,
    val meta: String? = null
)
