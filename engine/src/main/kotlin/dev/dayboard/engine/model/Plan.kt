package dev.dayboard.engine.model

import java.time.Instant
import java.time.LocalDate

enum class PlanSource { MANUAL, DSL, LLM_JSON, CALENDAR, OCR, VOICE }

data class PlanDay(
    val id: Long = 0,
    val date: LocalDate,
    val zoneId: String,
    val source: PlanSource,
    val createdAt: Instant
)

data class Goal(
    val id: Long = 0,
    val title: String,
    val targetMinutesPerWeek: Int? = null,
    val colorRole: ColorRole = ColorRole.NEUTRAL,
    val archived: Boolean = false
)
