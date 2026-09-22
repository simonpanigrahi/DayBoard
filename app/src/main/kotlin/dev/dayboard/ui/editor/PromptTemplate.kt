package dev.dayboard.ui.editor

/**
 * Copied to the clipboard, pasted into any AI with the day described in plain words,
 * and the JSON that comes back is pasted into the same box it came from.
 *
 * The last two rules matter most: without them models reliably pad a day with
 * plausible-sounding blocks that were never asked for.
 */
const val PLAN_PROMPT = """Turn my day plan below into JSON matching this exact schema.
Return ONLY the JSON object, no explanation, no markdown fences.

Schema:
{
  "schemaVersion": 1,
  "date": "YYYY-MM-DD",
  "blocks": [{
    "title": string,
    "start": "HH:MM" | omit if it should start when the previous block ends,
    "durationMinutes": integer,
    "kind": "FOCUS" | "BREAK" | "MEAL" | "FIXED_EVENT" | "BUFFER",
    "fixed": boolean,
    "tag": string | omit,
    "overflow": "PUSH" | "COMPRESS" | "TRUNCATE" | "SPILL",
    "minMinutes": integer | omit,
    "checklist": [string]
  }]
}

Rules:
- Appointments with a real clock time (class, gym slot, call) get "fixed": true and a "start".
- Everything else gets "fixed": false and no "start".
- Insert a "BUFFER" block of 15 minutes before each fixed block.
- If I mention several sub-tasks for one block, put them in "checklist".
- Do not invent blocks I did not mention.
- Do not exceed the hours I described.

My plan:
<describe your day here in plain words>"""

const val DSL_HINT = """09:00-09:30 Email professors #deep
  - Mail Dr. Puhan
09:30 45m Read the paper #research
45m Revise            flows after the one above
= 15m buffer          ~ 10m break          ! 17:30 60m Gym"""
