package de.brummbeobachter.app

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object SeismicDayRoutingSelfTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val berlin = ZoneId.of("Europe/Berlin")
        val now = Instant.parse("2026-09-17T10:34:00Z") // 12:34 local CEST

        val today = SeismicDayPolicy.windowFor(LocalDate.of(2026, 9, 17), berlin, now)
        check(today.start == Instant.parse("2026-09-16T22:00:00Z")) { "today start must be local midnight" }
        check(today.end == now) { "today end must be now" }
        check(today.isToday) { "today flag" }

        val past = SeismicDayPolicy.windowFor(LocalDate.of(2026, 9, 16), berlin, now)
        check(past.start == Instant.parse("2026-09-15T22:00:00Z")) { "past start" }
        check(past.end == Instant.parse("2026-09-16T22:00:00Z")) { "past end must be next local midnight" }
        check(!past.isToday) { "past flag" }

        val dstDay = SeismicDayPolicy.windowFor(
            LocalDate.of(2026, 10, 25),
            berlin,
            Instant.parse("2026-10-26T12:00:00Z")
        )
        check(dstDay.end.epochSecond - dstDay.start.epochSecond == 25L * 3600L) { "DST fall-back day must be 25 h" }

        var futureRejected = false
        try {
            SeismicDayPolicy.windowFor(LocalDate.of(2026, 9, 18), berlin, now)
        } catch (_: IllegalArgumentException) {
            futureRejected = true
        }
        check(futureRejected) { "future date must be rejected" }

        val routed = EidaRoutingPolicy.parseGetResponse(
            """
            https://geofon.gfz.de/fdsnws/dataselect/1/query?net=GE&sta=STU&loc=--&cha=HHZ&starttime=2026-09-16T22:00:00Z&endtime=2026-09-17T10:34:00Z

            """.trimIndent()
        )
        check(routed.size == 1) { "one route expected" }
        check(routed.first().startsWith("https://geofon.gfz.de/fdsnws/dataselect/1/query")) { "route URL parse" }

        val mixed = EidaRoutingPolicy.parseGetResponse("# comment\nnot-a-url\nhttp://example.org/fdsnws/dataselect/1/query?net=XX")
        check(mixed == listOf("http://example.org/fdsnws/dataselect/1/query?net=XX")) { "invalid lines must be ignored" }

        println("PASS: SeismicDayRoutingSelfTest")
    }
}
