package dev.dayboard.engine.parse

import dev.dayboard.engine.model.Anchor
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.engine.model.DraftBlock
import dev.dayboard.engine.model.ParseError
import dev.dayboard.engine.model.PlanDraft
import dev.dayboard.engine.model.PlanSource
import dev.dayboard.engine.model.Warning
import java.time.LocalTime

private val TAG = Regex("""#(\w+)""")
private val CHECKLIST_LINE = Regex("""^\s*-\s*(.+)$""")

private const val LONG_DAY_MINUTES = 16 * 60

/**
 * The line grammar from the appendix. Deterministic, no model in the loop, and every
 * error carries the line it came from so the editor can point at it.
 */
object DslParser : PlanImporter<String> {

    override fun parse(input: String): ImportResult {
        val parsed = input.lines().foldIndexed(Draft()) { index, draft, line -> draft.read(line, index + 1) }
        if (parsed.errors.isNotEmpty()) return ImportResult.Failed(parsed.errors)

        val plan = PlanDraft(
            date = null,
            source = PlanSource.DSL,
            blocks = parsed.blocks,
            warnings = warningsFor(parsed.blocks)
        )
        return ImportResult.Ok(plan)
    }

    private data class Draft(
        val blocks: List<DraftBlock> = emptyList(),
        val errors: List<ParseError> = emptyList()
    ) {
        fun read(line: String, number: Int): Draft {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("//")) return this

            val item = CHECKLIST_LINE.matchEntire(line)?.groupValues?.get(1)?.trim()
            if (item != null) return attach(item, number)

            return when (val block = blockLine(trimmed, number)) {
                is Parsed.Block -> copy(blocks = blocks + block.block)
                is Parsed.Bad -> copy(errors = errors + block.error)
            }
        }

        private fun attach(item: String, number: Int): Draft {
            val last = blocks.lastOrNull()
                ?: return copy(
                    errors = errors + ParseError(number, null, "Checklist item with no block above it")
                )
            return copy(blocks = blocks.dropLast(1) + last.copy(checklist = last.checklist + item))
        }
    }

    private sealed interface Parsed {
        data class Block(val block: DraftBlock) : Parsed
        data class Bad(val error: ParseError) : Parsed
    }

    private fun blockLine(line: String, number: Int): Parsed {
        val marker = line.first().takeIf { it in "!=~" }
        val body = if (marker == null) line else line.drop(1).trim()
        val tokens = body.split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return bad(number, "Nothing on this line but a marker")

        val timing = timingOf(tokens, number) ?: return bad(
            number,
            "Line needs a time or a duration, like '09:00-09:30 Title' or '45m Title'"
        )
        if (timing is Timing.Bad) return Parsed.Bad(timing.error.copy(line = number))

        val spec = timing as Timing.Ok
        if (marker == '!' && spec.start == null) return bad(number, "'!' needs a time, like '! 17:30 60m Gym'")

        val rest = tokens.drop(spec.consumed).joinToString(" ")
        val tag = TAG.find(rest)?.groupValues?.get(1)
        val title = TAG.replace(rest, "").trim()
        if (title.isEmpty()) return bad(number, "Block needs a title")

        val fixed = spec.start != null
        return Parsed.Block(
            DraftBlock(
                title = title,
                plannedMinutes = spec.minutes,
                kind = kindFor(marker, fixed),
                anchor = if (fixed) Anchor.FIXED else Anchor.FLOW,
                startLocal = spec.start,
                tag = tag
            )
        )
    }

    private sealed interface Timing {
        data class Ok(val start: LocalTime?, val minutes: Int, val consumed: Int) : Timing
        data class Bad(val error: ParseError) : Timing
    }

    private fun timingOf(tokens: List<String>, number: Int): Timing? {
        val first = tokens.first()

        val range = first.split("-").takeIf { it.size == 2 }
            ?.let { (from, to) -> parseTime(from)?.let { start -> parseTime(to)?.let { start to it } } }
        if (range != null) {
            val minutes = minutesBetween(range.first, range.second)
            return if (minutes == 0) Timing.Bad(ParseError(number, null, "That range is zero minutes long"))
            else Timing.Ok(range.first, minutes, consumed = 1)
        }

        val start = parseTime(first)
        if (start != null) {
            val minutes = tokens.getOrNull(1)?.let(::parseDuration)
                ?: return Timing.Bad(ParseError(number, null, "Expected a duration after $first, like '45m'"))
            return Timing.Ok(start, minutes, consumed = 2)
        }

        return parseDuration(first)?.let { Timing.Ok(null, it, consumed = 1) }
    }

    private fun kindFor(marker: Char?, fixed: Boolean): BlockKind = when {
        marker == '=' -> BlockKind.BUFFER
        marker == '~' -> BlockKind.BREAK
        marker == '!' -> BlockKind.FIXED_EVENT
        else -> BlockKind.FOCUS
    }

    private fun bad(line: Int, message: String) = Parsed.Bad(ParseError(line, null, message))

    private fun warningsFor(blocks: List<DraftBlock>): List<Warning> {
        if (blocks.isEmpty()) return emptyList()
        val total = blocks.sumOf { it.plannedMinutes }
        return listOfNotNull(
            Warning("No buffer in the day, so anything that runs long eats the next block")
                .takeIf { blocks.none { block -> block.kind == BlockKind.BUFFER } },
            Warning("That is ${total / 60} hours of blocks").takeIf { total > LONG_DAY_MINUTES }
        )
    }
}
