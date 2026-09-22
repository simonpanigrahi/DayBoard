package dev.dayboard.engine.parse

import dev.dayboard.engine.model.Anchor
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.engine.model.OverflowPolicy
import dev.dayboard.engine.model.PlanDraft
import dev.dayboard.engine.t
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonPlanParserTest {

    private fun ok(text: String): PlanDraft {
        val result = JsonPlanParser.parse(text)
        assertTrue(result is ImportResult.Ok, "expected Ok but got $result")
        return result.draft
    }

    @Test
    fun `the schema from the appendix parses`() {
        val draft = ok(
            """
            {
              "schemaVersion": 1,
              "date": "2026-09-21",
              "blocks": [
                {
                  "title": "Email professors",
                  "start": "09:00",
                  "durationMinutes": 30,
                  "kind": "FOCUS",
                  "fixed": true,
                  "tag": "outreach",
                  "overflow": "COMPRESS",
                  "minMinutes": 20,
                  "checklist": ["Mail Dr. Puhan", "Reply to lab thread"]
                },
                {
                  "title": "Read Facial-R1 paper",
                  "durationMinutes": 45,
                  "kind": "FOCUS",
                  "fixed": false,
                  "checklist": []
                }
              ]
            }
            """.trimIndent()
        )

        assertEquals(LocalDate.of(2026, 9, 21), draft.date)
        assertEquals(2, draft.blocks.size)
        val first = draft.blocks.first()
        assertEquals("Email professors", first.title)
        assertEquals(Anchor.FIXED, first.anchor)
        assertEquals(t("09:00"), first.startLocal)
        assertEquals(30, first.plannedMinutes)
        assertEquals(OverflowPolicy.COMPRESS, first.overflow)
        assertEquals(20, first.minMinutes)
        assertEquals(listOf("Mail Dr. Puhan", "Reply to lab thread"), first.checklist)
        assertEquals(Anchor.FLOW, draft.blocks[1].anchor)
    }

    @Test
    fun `markdown fences and chatter around the object are stripped`() {
        val draft = ok(
            """
            Sure! Here is your day:

            ```json
            { "blocks": [ { "title": "Email", "durationMinutes": 30 } ] }
            ```
            """.trimIndent()
        )

        assertEquals("Email", draft.blocks.single().title)
    }

    @Test
    fun `trailing commas are tolerated`() {
        val draft = ok("""{ "blocks": [ { "title": "Email", "durationMinutes": 30, }, ], }""")

        assertEquals(1, draft.blocks.size)
    }

    @Test
    fun `durations and times are read leniently`() {
        val draft = ok(
            """
            { "blocks": [
              { "title": "A", "durationMinutes": "30m", "start": "9am", "fixed": true },
              { "title": "B", "durationMinutes": "1h 15 min" },
              { "title": "C", "duration": 20 }
            ] }
            """.trimIndent()
        )

        assertEquals(30, draft.blocks[0].plannedMinutes)
        assertEquals(t("09:00"), draft.blocks[0].startLocal)
        assertEquals(75, draft.blocks[1].plannedMinutes)
        assertEquals(20, draft.blocks[2].plannedMinutes)
    }

    @Test
    fun `a start time alone makes the block fixed`() {
        val draft = ok("""{ "blocks": [ { "title": "Class", "start": "10:00", "durationMinutes": 60 } ] }""")

        assertEquals(Anchor.FIXED, draft.blocks.single().anchor)
    }

    @Test
    fun `unknown kinds and policies fall back instead of failing`() {
        val draft = ok(
            """{ "blocks": [ { "title": "A", "durationMinutes": 30, "kind": "DEEP_WORK", "overflow": "SQUISH" } ] }"""
        )

        assertEquals(BlockKind.FOCUS, draft.blocks.single().kind)
        assertEquals(OverflowPolicy.SPILL, draft.blocks.single().overflow)
        assertTrue(draft.warnings.isNotEmpty())
    }

    @Test
    fun `a block with no title or no duration is reported with its index`() {
        val result = JsonPlanParser.parse("""{ "blocks": [ { "durationMinutes": 30 } ] }""")

        assertTrue(result is ImportResult.Failed)
        assertTrue(result.errors.single().message.contains("title", ignoreCase = true))
    }

    @Test
    fun `text that is not JSON at all fails cleanly`() {
        val result = JsonPlanParser.parse("I would like to read a paper this morning")

        assertTrue(result is ImportResult.Failed)
        assertTrue(result.errors.isNotEmpty())
    }

    @Test
    fun `one input box routes JSON and DSL to the right parser`() {
        assertEquals(
            "Email",
            (importPlan("""{ "blocks": [ { "title": "Email", "durationMinutes": 30 } ] }""") as ImportResult.Ok)
                .draft.blocks.single().title
        )
        assertEquals(
            "Email",
            (importPlan("30m Email") as ImportResult.Ok).draft.blocks.single().title
        )
    }
}
