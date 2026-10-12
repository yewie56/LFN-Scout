package de.brummbeobachter.app

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeismicDayPolicyTest {
    private val berlin = ZoneId.of("Europe/Berlin")

    @Test
    fun today_runs_from_local_midnight_until_now() {
        val now = Instant.parse("2026-09-17T10:34:00Z")
        val window = SeismicDayPolicy.windowFor(LocalDate.of(2026, 9, 17), berlin, now)

        assertEquals(Instant.parse("2026-09-16T22:00:00Z"), window.start)
        assertEquals(now, window.end)
        assertTrue(window.isToday)
    }

    @Test
    fun past_day_runs_between_two_local_midnights() {
        val now = Instant.parse("2026-09-17T10:34:00Z")
        val window = SeismicDayPolicy.windowFor(LocalDate.of(2026, 9, 16), berlin, now)

        assertEquals(Instant.parse("2026-09-15T22:00:00Z"), window.start)
        assertEquals(Instant.parse("2026-09-16T22:00:00Z"), window.end)
        assertFalse(window.isToday)
    }

    @Test
    fun dst_fall_back_day_has_25_hours() {
        val window = SeismicDayPolicy.windowFor(
            LocalDate.of(2026, 10, 25),
            berlin,
            Instant.parse("2026-10-26T12:00:00Z")
        )

        assertEquals(25L * 3600L, window.end.epochSecond - window.start.epochSecond)
    }

    @Test(expected = IllegalArgumentException::class)
    fun future_date_is_rejected() {
        SeismicDayPolicy.windowFor(
            LocalDate.of(2026, 9, 18),
            berlin,
            Instant.parse("2026-09-17T10:34:00Z")
        )
    }
}
