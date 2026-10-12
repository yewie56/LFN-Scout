package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderPolicyTest {

    @Test
    fun default_interval_is_30_minutes() {
        assertEquals(30, ReminderPolicy.DEFAULT_INTERVAL_MINUTES)
    }

    @Test
    fun response_window_is_30_percent_for_30_minutes() {
        assertEquals(9, ReminderPolicy.responseWindowMinutes(30))
    }

    @Test
    fun response_window_has_minimum_5_minutes() {
        assertEquals(5, ReminderPolicy.responseWindowMinutes(10))
    }

    @Test
    fun response_window_has_maximum_15_minutes() {
        assertEquals(15, ReminderPolicy.responseWindowMinutes(120))
    }

    @Test
    fun pause_options_include_requested_values() {
        assertEquals(1, ReminderPauseOption.ONE_HOUR.hours)
        assertEquals(2, ReminderPauseOption.TWO_HOURS.hours)
        assertEquals(4, ReminderPauseOption.FOUR_HOURS.hours)
        assertEquals(8, ReminderPauseOption.EIGHT_HOURS.hours)
        assertEquals(24, ReminderPauseOption.TWENTY_FOUR_HOURS.hours)
        assertEquals(72, ReminderPauseOption.SEVENTY_TWO_HOURS.hours)
        assertEquals(null, ReminderPauseOption.OFF.hours)
    }
}
