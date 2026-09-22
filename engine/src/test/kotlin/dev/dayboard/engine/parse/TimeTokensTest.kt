package dev.dayboard.engine.parse

import dev.dayboard.engine.t
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TimeTokensTest {

    @Test
    fun `times arrive in every shape a human or a model writes`() {
        assertEquals(t("09:00"), parseTime("09:00"))
        assertEquals(t("09:00"), parseTime("9:00"))
        assertEquals(t("09:00"), parseTime("9am"))
        assertEquals(t("09:00"), parseTime("9 AM"))
        assertEquals(t("21:00"), parseTime("9pm"))
        assertEquals(t("21:30"), parseTime("9:30 PM"))
        assertEquals(t("00:30"), parseTime("12:30am"))
        assertEquals(t("12:30"), parseTime("12:30pm"))
        assertEquals(t("17:30"), parseTime("17:30"))
    }

    @Test
    fun `nonsense is not a time`() {
        assertNull(parseTime("tomorrow"))
        assertNull(parseTime("25:00"))
        assertNull(parseTime("9:75"))
        assertNull(parseTime(""))
    }

    @Test
    fun `durations arrive in every shape too`() {
        assertEquals(45, parseDuration("45m"))
        assertEquals(45, parseDuration("45 min"))
        assertEquals(45, parseDuration("45 minutes"))
        assertEquals(60, parseDuration("1h"))
        assertEquals(90, parseDuration("1h30m"))
        assertEquals(90, parseDuration("1.5h"))
        assertEquals(30, parseDuration("30"))
    }

    @Test
    fun `a duration has to be a positive number of minutes`() {
        assertNull(parseDuration("soon"))
        assertNull(parseDuration("0m"))
        assertNull(parseDuration("-10m"))
        assertNull(parseDuration(""))
    }
}
