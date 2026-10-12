package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ReminderScheduleGridPolicyTest {

    private val zone =
        TimeZone.getTimeZone("Europe/Berlin")

    private fun epoch(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int = 0
    ): Long =
        Calendar.getInstance(zone).apply {
            clear()
            set(year, month - 1, day, hour, minute, second)
        }.timeInMillis

    @Test
    fun interval_15_minutes_remains_relative() {
        assertEquals(
            epoch(2026, 9, 2, 10, 38),
            ReminderScheduleGridPolicy.nextReminderEpochMs(
                baseEpochMs = epoch(2026, 9, 2, 10, 23),
                intervalMinutes = 15,
                timeZone = zone
            )
        )
    }

    @Test
    fun interval_60_minutes_uses_quarter_past_hour_grid() {
        assertEquals(
            epoch(2026, 9, 2, 11, 15),
            ReminderScheduleGridPolicy.nextReminderEpochMs(
                baseEpochMs = epoch(2026, 9, 2, 10, 23),
                intervalMinutes = 60,
                timeZone = zone
            )
        )
    }

    @Test
    fun interval_90_minutes_uses_shifted_half_hour_grid() {
        assertEquals(
            epoch(2026, 9, 2, 11, 45),
            ReminderScheduleGridPolicy.nextReminderEpochMs(
                baseEpochMs = epoch(2026, 9, 2, 10, 23),
                intervalMinutes = 90,
                timeZone = zone
            )
        )
    }

    @Test
    fun interval_120_minutes_uses_quarter_past_hour_grid() {
        assertEquals(
            epoch(2026, 9, 2, 12, 15),
            ReminderScheduleGridPolicy.nextReminderEpochMs(
                baseEpochMs = epoch(2026, 9, 2, 10, 23),
                intervalMinutes = 120,
                timeZone = zone
            )
        )
    }

    @Test
    fun exact_grid_time_is_kept_after_interval() {
        assertEquals(
            epoch(2026, 9, 2, 11, 15),
            ReminderScheduleGridPolicy.nextReminderEpochMs(
                baseEpochMs = epoch(2026, 9, 2, 10, 15),
                intervalMinutes = 60,
                timeZone = zone
            )
        )
    }

    @Test
    fun tie_between_slots_uses_later_slot() {
        assertEquals(
            epoch(2026, 9, 2, 11, 45),
            ReminderScheduleGridPolicy.nextReminderEpochMs(
                baseEpochMs = epoch(2026, 9, 2, 10, 30),
                intervalMinutes = 60,
                timeZone = zone
            )
        )
    }


    @Test
    fun interval_30_minutes_uses_grid() {
        assertEquals(
            epoch(2026, 9, 2, 10, 45),
            ReminderScheduleGridPolicy.nextReminderEpochMs(
                baseEpochMs = epoch(2026, 9, 2, 10, 23),
                intervalMinutes = 30,
                timeZone = zone
            )
        )
    }
}
