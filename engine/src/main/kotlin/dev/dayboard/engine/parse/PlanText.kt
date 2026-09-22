package dev.dayboard.engine.parse

private val BLOCKS_KEY = Regex(""""blocks"\s*:""")

/**
 * One input box for both paths: whatever the user pastes, JSON from a model or lines
 * typed by hand, goes to the parser that understands it.
 */
fun importPlan(text: String): ImportResult {
    val trimmed = text.trim().removePrefix("```json").removePrefix("```").trimStart()
    val looksLikeJson = trimmed.startsWith("{") || BLOCKS_KEY.containsMatchIn(text)
    return if (looksLikeJson) JsonPlanParser.parse(text) else DslParser.parse(text)
}
