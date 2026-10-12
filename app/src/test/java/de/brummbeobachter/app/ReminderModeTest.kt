package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderModeTest {

    @Test
    fun reminder_modes_are_available() {
        assertEquals(
            "VERY_NOTICEABLE",
            ReminderMode.VERY_NOTICEABLE.name
        )
        assertEquals(
            "ALARM",
            ReminderMode.ALARM.name
        )
    }

    @Test
    fun unknown_stored_mode_falls_back_safely() {
        assertEquals(
            ReminderMode.VERY_NOTICEABLE,
            ReminderMode.fromStored("UNKNOWN")
        )
    }
}
