package dev.dayboard.engine.model

import java.time.LocalDate
import java.time.LocalTime

data class ParseError(val line: Int?, val span: IntRange?, val message: String)

data class Warning(val message: String, val line: Int? = null)

/** A draft is never live: every importer produces this, Review commits it. */
data class PlanDraft(
    val date: LocalDate?,
    val source: PlanSource,
    val blocks: List<DraftBlock>,
    val warnings: List<Warning> = emptyList()
)

data class DraftBlock(
    val title: String,
    val plannedMinutes: Int,
    val kind: BlockKind = BlockKind.FOCUS,
    val anchor: Anchor = Anchor.FLOW,
    val startLocal: LocalTime? = null,
    val minMinutes: Int? = null,
    val overflow: OverflowPolicy = OverflowPolicy.SPILL,
    val tag: String? = null,
    val checklist: List<String> = emptyList()
)
