package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeismicStationPolicyTest {
    @Test
    fun station_text_is_sorted_by_distance_from_gps() {
        val text = """
            #Network|Station|Latitude|Longitude|Elevation|SiteName|StartTime|EndTime
            GE|FAR|50.0000|10.0000|300|Far station|1990-01-01T00:00:00|
            GE|NEAR|49.5500|8.5800|100|Near station|1990-01-01T00:00:00|
        """.trimIndent()

        val result = SeismicStationPolicy.parseFdsnStationText(
            text = text,
            userLatitude = 49.54,
            userLongitude = 8.58
        )

        assertEquals(0, result.skippedLines)
        assertEquals("NEAR", result.stations.first().station)
        assertTrue(result.stations.first().distanceKm < result.stations.last().distanceKm)
    }

    @Test
    fun malformed_station_lines_are_skipped() {
        val text = """
            GE|GOOD|49.55|8.58|100|Good
            broken|line
            GE|BAD|not-a-lat|8.5|100|Bad
        """.trimIndent()

        val result = SeismicStationPolicy.parseFdsnStationText(text, 49.54, 8.58)

        assertEquals(1, result.stations.size)
        assertEquals(2, result.skippedLines)
    }

    @Test
    fun zero_distance_is_zero() {
        assertEquals(
            0.0,
            SeismicStationPolicy.haversineKm(49.54, 8.58, 49.54, 8.58),
            0.000001
        )
    }

    @Test
    fun channel_text_parses_fdsn_sensitivity_for_velocity_scaling() {
        val text = "GE|STU||HHZ|48.77|9.19|700|0|0|-90|Sensor|2500000000|1.0|M/S|100|2020-01-01T00:00:00|"
        val channel = SeismicStationPolicy.parseFdsnChannelText(text, 49.54, 8.60).channels.single()
        assertEquals(2_500_000_000.0, channel.sensitivity ?: 0.0, 0.0)
        assertEquals(1.0, channel.sensitivityFrequencyHz ?: 0.0, 0.0)
        assertEquals("M/S", channel.sensitivityUnits)
        assertEquals(0.4, SeismicAmplitudePolicy.velocityNmPerSecPerCount(channel) ?: -1.0, 1e-12)
    }
}
