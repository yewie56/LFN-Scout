package de.brummbeobachter.app

import java.time.Instant

object SeismicSearchSelfTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val sources = SeismicSearchPolicy.directMetadataSources()
        check(sources.size >= 2) { "at least two direct metadata sources expected" }
        check(sources.first().url.contains("eida.bgr.de/fdsnws/station/1/query")) { "BGR must be first direct German source" }
        check(sources.any { it.url.contains("geofon.gfz.de/fdsnws/station/1/query") }) { "GEOFON direct source expected" }
        check(sources.none { it.url.contains("federator.orfeus-eu.org") }) { "federator must not be a primary direct source" }

        val start = Instant.parse("2026-09-16T22:00:00Z")
        val end = Instant.parse("2026-09-17T17:00:00Z")
        val probe = SeismicSearchPolicy.probeWindow(start, end)
        check(probe.end <= end) { "probe must stay inside requested window" }
        check(probe.start >= start) { "probe must stay inside requested window" }
        check(probe.end.epochSecond - probe.start.epochSecond <= 120L) { "probe must be short" }
        check(probe.end.epochSecond <= end.epochSecond - 300L) { "long windows should avoid the freshest five minutes" }

        println("PASS: SeismicSearchSelfTest")
    }
}
