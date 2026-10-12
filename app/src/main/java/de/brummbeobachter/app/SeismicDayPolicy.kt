package de.brummbeobachter.app

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

data class SeismicDayWindow(
    val date: LocalDate,
    val zoneId: ZoneId,
    val localStart: ZonedDateTime,
    val localEnd: ZonedDateTime,
    val start: Instant,
    val end: Instant,
    val isToday: Boolean
)

object SeismicDayPolicy {
    fun windowFor(
        date: LocalDate,
        zoneId: ZoneId,
        now: Instant = Instant.now()
    ): SeismicDayWindow {
        val today = now.atZone(zoneId).toLocalDate()
        require(!date.isAfter(today)) { "Future seismic dates are not allowed" }

        val localStart = date.atStartOfDay(zoneId)
        val isToday = date == today
        val localEnd = if (isToday) now.atZone(zoneId) else date.plusDays(1).atStartOfDay(zoneId)

        return SeismicDayWindow(
            date = date,
            zoneId = zoneId,
            localStart = localStart,
            localEnd = localEnd,
            start = localStart.toInstant(),
            end = localEnd.toInstant(),
            isToday = isToday
        )
    }
}
