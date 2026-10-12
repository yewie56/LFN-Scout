package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RadiusHistogramRawObservationsParserTest {

    @Test
    fun parses_raw_observations_with_stable_observer_id_and_coordinates() {
        val body =
            """{
              "ok":true,
              "center_source":"gps",
              "radius_km":600,
              "observer_count":2,
              "observation_count":2,
              "blocks":[],
              "observations":[
                {
                  "observer_id":"BB017",
                  "rating":4,
                  "scheduled_time":"2026-09-02T20:15:00Z",
                  "response_time":"2026-09-02T20:16:12Z",
                  "latitude":49.5401,
                  "longitude":8.5783
                },
                {
                  "observer_id":"BB017",
                  "rating":5,
                  "scheduled_time":"2026-09-02T20:45:00Z",
                  "response_time":"2026-09-02T20:46:02Z",
                  "latitude":49.5402,
                  "longitude":8.5784
                }
              ]
            }"""

        val result =
            RadiusHistogramParser.parse(
                200,
                body
            )

        assertTrue(result.success)
        assertEquals(2, result.observations.size)
        assertEquals("BB017", result.observations[0].observerId)
        assertEquals("BB017", result.observations[1].observerId)
        assertEquals(4, result.observations[0].rating)
        assertEquals("2026-09-02T20:15:00Z", result.observations[0].scheduledTime)
        assertEquals("2026-09-02T20:16:12Z", result.observations[0].responseTime)
        assertEquals(49.5401, result.observations[0].latitude, 0.000001)
        assertEquals(8.5783, result.observations[0].longitude, 0.000001)
    }
}
