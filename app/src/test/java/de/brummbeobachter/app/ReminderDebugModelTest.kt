package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderDebugModelTest {

    @Test
    fun settings_can_store_next_reminder_time() {
        val settings = ReminderSettings(
            enabled = true,
            intervalMinutes = 30,
            pauseUntilEpochMs = 0L,
            nextReminderEpochMs = 1_800_000L
        )

        assertEquals(1_800_000L, settings.nextReminderEpochMs)
    }

    @Test
    fun logger_formats_disabled_time_as_dash() {
        assertEquals("-", DebugLogger.formatEpoch(0L))
    }

    @Test
    fun response_window_for_default_interval_is_nine_minutes() {
        assertEquals(9, ReminderPolicy.responseWindowMinutes(30))
    }
}
