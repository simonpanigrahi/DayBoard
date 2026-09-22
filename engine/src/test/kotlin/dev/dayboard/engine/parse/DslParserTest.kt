package dev.dayboard.engine.parse

import dev.dayboard.engine.model.Anchor
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.engine.model.PlanDraft
import dev.dayboard.engine.t
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DslParserTest {

    private fun ok(text: String): PlanDraft {
        val result = DslParser.parse(text)
        assertTrue(result is ImportResult.Ok, "expected Ok but got $result")
        return result.draft
    }

    private fun failed(text: String): List<ParseErrorAt> {
        val result = DslParser.parse(text)
        assertTrue(result is ImportResult.Failed, "expected Failed but got $result")
        return result.errors.map { ParseErrorAt(it.line, it.message) }
    }

    private data class ParseErrorAt(val line: Int?, val message: String)

    @Test
    fun `a range is a fixed block with an explicit end`() {
        val block = ok("09:00-09:30 Email professors #deep").blocks.single()

        assertEquals("Email professors", block.title)
        assertEquals(Anchor.FIXED, block.anchor)
        assertEquals(t("09:00"), block.startLocal)
        assertEquals(30, block.plannedMinutes)
        assertEquals("deep", block.tag)
    }

    @Test
    fun `a time plus a duration is a fixed block`() {
        val block = ok("09:30 45m Read Facial-R1 paper #research").blocks.single()

        assertEquals("Read Facial-R1 paper", block.title)
        assertEquals(Anchor.FIXED, block.anchor)
        assertEquals(t("09:30"), block.startLocal)
        assertEquals(45, block.plannedMinutes)
    }

    @Test
    fun `a bare duration is a flow block`() {
        val block = ok("45m Revise electromagnetics").blocks.single()

        assertEquals(Anchor.FLOW, block.anchor)
        assertEquals(null, block.startLocal)
        assertEquals(45, block.plannedMinutes)
        assertEquals("Revise electromagnetics", block.title)
    }

    @Test
    fun `markers set buffer, break and forced fixed`() {
        val draft = ok(
            """
            = 15m buffer
            ~ 10m tea
            ! 17:30 60m Gym
            """.trimIndent()
        )

        assertEquals(BlockKind.BUFFER, draft.blocks[0].kind)
        assertEquals(BlockKind.BREAK, draft.blocks[1].kind)
        assertEquals(BlockKind.FIXED_EVENT, draft.blocks[2].kind)
        assertEquals(Anchor.FIXED, draft.blocks[2].anchor)
        assertEquals(t("17:30"), draft.blocks[2].startLocal)
    }

    @Test
    fun `indented dashes attach to the block above`() {
        val draft = ok(
            """
            09:00-09:30 Email professors
              - Mail Dr. Puhan
              - Reply to lab thread
            45m Read
              - Section 3
            """.trimIndent()
        )

        assertEquals(listOf("Mail Dr. Puhan", "Reply to lab thread"), draft.blocks[0].checklist)
        assertEquals(listOf("Section 3"), draft.blocks[1].checklist)
    }

    @Test
    fun `comments and blank lines are ignored`() {
        val draft = ok(
            """
            // morning
            30m Email

            // afternoon
            45m Read
            """.trimIndent()
        )

        assertEquals(2, draft.blocks.size)
    }

    @Test
    fun `a whole realistic day parses`() {
        val draft = ok(
            """
            09:00-09:30 Email professors #deep
              - Mail Dr. Puhan
              - Reply to lab thread
            09:30 45m Read Facial-R1 paper #research
            = 15m buffer
            ! 17:30 60m Gym
            ~ 10m break
            90m Revise #exam
            """.trimIndent()
        )

        assertEquals(6, draft.blocks.size)
        assertEquals(listOf(30, 45, 15, 60, 10, 90), draft.blocks.map { it.plannedMinutes })
        assertEquals(listOf("deep", "research", null, null, null, "exam"), draft.blocks.map { it.tag })
    }

    @Test
    fun `a range that crosses midnight still has a positive duration`() {
        assertEquals(60, ok("23:30-00:30 Wind down").blocks.single().plannedMinutes)
    }

    @Test
    fun `am and pm ranges work`() {
        val block = ok("9am-10:30am Lab").blocks.single()

        assertEquals(t("09:00"), block.startLocal)
        assertEquals(90, block.plannedMinutes)
    }

    @Test
    fun `a line with no timing is an error that names its line`() {
        val errors = failed("30m Email\nRead the paper\n45m Revise")

        assertEquals(1, errors.size)
        assertEquals(2, errors.single().line)
        assertTrue(errors.single().message.contains("duration", ignoreCase = true))
    }

    @Test
    fun `a checklist item with no block above it is an error`() {
        assertEquals(1, failed("  - orphan item").single().line)
    }

    @Test
    fun `a forced fixed block without a time is an error`() {
        assertTrue(failed("! Gym").single().message.contains("time", ignoreCase = true))
    }

    @Test
    fun `an empty document is an empty draft, not an error`() {
        assertEquals(0, ok("   \n\n// nothing here\n").blocks.size)
    }

    @Test
    fun `a title is required`() {
        assertTrue(failed("45m").single().message.contains("title", ignoreCase = true))
    }

    @Test
    fun `a day with no buffer is warned about, not rejected`() {
        val draft = ok("30m Email\n45m Read")

        assertTrue(draft.warnings.any { it.message.contains("buffer", ignoreCase = true) })
    }

    @Test
    fun `an implausibly long day is warned about`() {
        val draft = ok((1..20).joinToString("\n") { "60m Block $it" })

        assertTrue(draft.warnings.any { it.message.contains("hours", ignoreCase = true) })
    }
}
