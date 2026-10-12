package de.brummbeobachter.app

import java.time.Instant

object SeismicRegionalHelicorderSelfTest {
    @JvmStatic
    fun main(args: Array<String>) {
        check(SeismicRegionalPolicy.isBuerstadtSpecialZone(49.5430, 8.6012)) {
            "Viernheim must be inside the WBBU special zone"
        }
        check(!SeismicRegionalPolicy.isBuerstadtSpecialZone(39.4699, -0.3763)) {
            "Valencia must use the general nearest-station principle"
        }

        val requestStart = Instant.parse("2026-09-16T22:00:00Z").toEpochMilli()
        val samples = IntArray(24 * 60 * 60) { i -> ((i % 120) - 60) * 10 }
        val rows = SeismicHelicorderPolicy.buildRows(
            samples = samples,
            sampleRateHz = 1.0,
            traceStartEpochMs = requestStart,
            requestedStartEpochMs = requestStart,
            requestedEndEpochMs = requestStart + 24L * 60L * 60L * 1000L,
            binsPerRow = 60
        )
        check(rows.size == 48) { "classic 24h plot must contain 48 half-hour rows" }
        check(rows.all { it.bins.size == 60 }) { "full day rows must all contain data" }
        check(rows.first().startMinuteOfDay == 0)
        check(rows[1].startMinuteOfDay == 30)
        check(rows.last().startMinuteOfDay == 23 * 60 + 30)
        check(rows.first().usePrimaryColor)
        check(!rows[1].usePrimaryColor)

        val partialRows = SeismicHelicorderPolicy.buildRows(
            samples = IntArray(90 * 60) { it % 100 },
            sampleRateHz = 1.0,
            traceStartEpochMs = requestStart,
            requestedStartEpochMs = requestStart,
            requestedEndEpochMs = requestStart + 90L * 60L * 1000L,
            binsPerRow = 30
        )
        check(partialRows.size == 48)
        check(partialRows.take(3).all { it.bins.isNotEmpty() })
        check(partialRows.drop(3).all { it.bins.isEmpty() }) { "future rows of today must stay empty" }

        println("SeismicRegionalHelicorderSelfTest PASS")
    }
}
