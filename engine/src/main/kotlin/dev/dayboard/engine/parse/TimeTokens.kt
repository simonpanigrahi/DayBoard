package dev.dayboard.engine.parse

import java.time.LocalTime

private val TIME = Regex("""(\d{1,2})(?::(\d{2}))?\s*([ap])\.?m?\.?|(\d{1,2}):(\d{2})""", RegexOption.IGNORE_CASE)

private val DURATION = Regex("""(?:(\d+(?:\.\d+)?)\s*h(?:ou)?r?s?)?\s*(?:(\d+)\s*m(?:in(?:ute)?s?)?)?""")

/** 09:00, 9:00, 9am, 9 AM, 9:30 PM, 12:30am. Anything else is not a time. */
fun parseTime(text: String): LocalTime? {
    val match = TIME.matchEntire(text.trim().lowercase()) ?: return null
    return if (match.groupValues[3].isNotEmpty()) {
        val hour = match.groupValues[1].toInt()
        val minute = match.groupValues[2].ifEmpty { "0" }.toInt()
        if (hour !in 1..12 || minute !in 0..59) return null
        val meridiemHour = when {
            match.groupValues[3] == "a" -> if (hour == 12) 0 else hour
            else -> if (hour == 12) 12 else hour + 12
        }
        LocalTime.of(meridiemHour, minute)
    } else {
        val hour = match.groupValues[4].toInt()
        val minute = match.groupValues[5].toInt()
        if (hour !in 0..23 || minute !in 0..59) return null
        LocalTime.of(hour, minute)
    }
}

/** 45m, 45 min, 45 minutes, 1h, 1h30m, 1.5h, or a bare number of minutes. */
fun parseDuration(text: String): Int? {
    val cleaned = text.trim().lowercase()
    if (cleaned.isEmpty()) return null
    cleaned.toIntOrNull()?.let { return it.takeIf { minutes -> minutes > 0 } }
    val match = DURATION.matchEntire(cleaned) ?: return null
    val hours = match.groupValues[1].toDoubleOrNull() ?: 0.0
    val minutes = match.groupValues[2].toIntOrNull() ?: 0
    return ((hours * 60).toInt() + minutes).takeIf { it > 0 }
}

/** Minutes from [start] to [end], wrapping past midnight so a late block stays positive. */
internal fun minutesBetween(start: LocalTime, end: LocalTime): Int {
    val startMinute = start.hour * 60 + start.minute
    val endMinute = end.hour * 60 + end.minute
    return Math.floorMod(endMinute - startMinute, 24 * 60)
}
