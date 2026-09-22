package dev.dayboard.engine.parse

import dev.dayboard.engine.model.ParseError
import dev.dayboard.engine.model.PlanDraft
import dev.dayboard.engine.model.Warning

/** Every importer produces the same shape, and nothing here is live until it is committed. */
interface PlanImporter<in Input> {
    fun parse(input: Input): ImportResult
}

sealed interface ImportResult {
    data class Ok(val draft: PlanDraft, val warnings: List<Warning> = draft.warnings) : ImportResult
    data class Failed(val errors: List<ParseError>) : ImportResult
}
