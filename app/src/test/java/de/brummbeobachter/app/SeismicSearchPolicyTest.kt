package de.brummbeobachter.app

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeismicSearchPolicyTest {
    @Test
    fun direct_metadata_sources_prefer_bgr_and_do_not_use_federator() {
        val sources = SeismicSearchPolicy.directMetadataSources()
        assertEquals("BGR", sources.first().name)
        assertTrue(sources.first().url.contains("eida.bgr.de/fdsnws/station/1/query"))
        assertTrue(sources.any { it.url.contains("geofon.gfz.de/fdsnws/station/1/query") })
        assertTrue(sources.none { it.url.contains("federator.orfeus-eu.org") })
    }

    @Test
    fun probe_is_short_and_avoids_freshest_five_minutes_on_long_window() {
        val start = Instant.parse("2026-09-16T22:00:00Z")
        val end = Instant.parse("2026-09-17T17:00:00Z")
        val probe = SeismicSearchPolicy.probeWindow(start, end)
        assertEquals(120L, probe.end.epochSecond - probe.start.epochSecond)
        assertEquals(end.minusSeconds(300), probe.end)
    }

    @Test
    fun merge_channels_deduplicates_and_keeps_distance_order() {
        fun channel(network: String, station: String, distance: Double, cha: String) = SeismicChannel(
            station = SeismicStation(network, station, 0.0, 0.0, null, "", distance),
            location = "",
            channel = cha,
            sampleRateHz = 100.0
        )
        val far = channel("GR", "FAR", 100.0, "HHZ")
        val near = channel("GE", "NEAR", 10.0, "BHZ")
        val merged = SeismicSearchPolicy.mergeChannels(listOf(far, near, near))
        assertEquals(2, merged.size)
        assertEquals("NEAR", merged.first().station.station)
    }
}
