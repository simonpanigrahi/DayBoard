package dev.dayboard.engine.parse

import dev.dayboard.engine.model.Anchor
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.engine.model.DraftBlock
import dev.dayboard.engine.model.OverflowPolicy
import dev.dayboard.engine.model.ParseError
import dev.dayboard.engine.model.PlanDraft
import dev.dayboard.engine.model.PlanSource
import dev.dayboard.engine.model.Warning
import java.time.LocalDate

/**
 * Lenient on the way in, strict on what Review then shows. Model output is not clean
 * and the user should not be punished for that.
 */
object JsonPlanParser : PlanImporter<String> {

    override fun parse(input: String): ImportResult {
        val root = parseJson(input) as? JsonValue.Obj
            ?: return failed("That does not look like a JSON plan")
        val blocks = root.fields["blocks"] as? JsonValue.Arr
            ?: return failed("No \"blocks\" array in that JSON")

        val read = blocks.items.mapIndexed(::readBlock)
        val errors = read.filterIsInstance<Read.Bad>().map { it.error }
        if (errors.isNotEmpty()) return ImportResult.Failed(errors)

        val good = read.filterIsInstance<Read.Good>()
        val dateText = (root.fields["date"] as? JsonValue.Str)?.value
        val date = dateText?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        return ImportResult.Ok(
            PlanDraft(
                date = date,
                source = PlanSource.LLM_JSON,
                blocks = good.map { it.block },
                warnings = good.flatMap { it.warnings } +
                    listOfNotNull(Warning("Ignored an unreadable date: $dateText").takeIf { dateText != null && date == null })
            )
        )
    }

    private sealed interface Read {
        data class Good(val block: DraftBlock, val warnings: List<Warning>) : Read
        data class Bad(val error: ParseError) : Read
    }

    private fun readBlock(index: Int, value: JsonValue): Read {
        val position = index + 1
        val obj = value as? JsonValue.Obj
            ?: return Read.Bad(ParseError(null, null, "Block $position is not an object"))
        val title = obj.text("title")?.takeIf { it.isNotBlank() }
            ?: return Read.Bad(ParseError(null, null, "Block $position has no title"))
        val minutes = obj.duration("durationMinutes", "duration", "minutes", "durationMins")
            ?: return Read.Bad(ParseError(null, null, "Block '$title' has no usable duration"))

        val start = obj.text("start")?.let(::parseTime)
        val fixed = start != null && (obj.flag("fixed") != false)
        val kindText = obj.text("kind")
        val kind = kindText?.let { text -> BlockKind.entries.firstOrNull { it.name.equals(text, ignoreCase = true) } }
        val overflowText = obj.text("overflow")
        val overflow = overflowText?.let { text ->
            OverflowPolicy.entries.firstOrNull { it.name.equals(text, ignoreCase = true) }
        }

        return Read.Good(
            block = DraftBlock(
                title = title,
                plannedMinutes = minutes,
                kind = kind ?: BlockKind.FOCUS,
                anchor = if (fixed) Anchor.FIXED else Anchor.FLOW,
                startLocal = if (fixed) start else null,
                minMinutes = obj.int("minMinutes")?.coerceIn(0, minutes),
                overflow = overflow ?: OverflowPolicy.SPILL,
                tag = obj.text("tag"),
                checklist = obj.strings("checklist")
            ),
            warnings = listOfNotNull(
                Warning("'$kindText' is not a block kind, read '$title' as FOCUS").takeIf { kindText != null && kind == null },
                Warning("'$overflowText' is not an overflow policy, read '$title' as SPILL")
                    .takeIf { overflowText != null && overflow == null },
                Warning("'$title' says fixed but carries no start time, so it flows")
                    .takeIf { obj.flag("fixed") == true && start == null }
            )
        )
    }

    private fun failed(message: String) = ImportResult.Failed(listOf(ParseError(null, null, message)))
}

private fun JsonValue.Obj.text(key: String): String? = when (val value = fields[key]) {
    is JsonValue.Str -> value.value
    is JsonValue.Num -> value.value.toInt().toString()
    else -> null
}

private fun JsonValue.Obj.flag(key: String): Boolean? = when (val value = fields[key]) {
    is JsonValue.Bool -> value.value
    is JsonValue.Str -> value.value.toBooleanStrictOrNull()
    else -> null
}

private fun JsonValue.Obj.int(key: String): Int? = (fields[key] as? JsonValue.Num)?.value?.toInt()

private fun JsonValue.Obj.duration(vararg keys: String): Int? = keys.firstNotNullOfOrNull { key ->
    when (val value = fields[key]) {
        is JsonValue.Num -> value.value.toInt().takeIf { it > 0 }
        is JsonValue.Str -> parseDuration(value.value)
        else -> null
    }
}

private fun JsonValue.Obj.strings(key: String): List<String> =
    (fields[key] as? JsonValue.Arr)?.items.orEmpty().filterIsInstance<JsonValue.Str>().map { it.value }
