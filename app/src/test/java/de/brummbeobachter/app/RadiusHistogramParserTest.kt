package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RadiusHistogramParserTest {

    @Test
    fun parses_four_blocks_and_separates_no_response() {
        val body =
            """{
              "ok":true,
              "center_source":"gps",
              "radius_km":600,
              "observer_count":7,
              "observation_count":12,
              "blocks":[
                {"start_time":"2026-09-02T20:30:00Z","end_time":"2026-09-02T21:00:00Z","counts":[1,2,0,0,0,0],"no_response":1},
                {"start_time":"2026-09-02T21:00:00Z","end_time":"2026-09-02T21:30:00Z","counts":[0,0,1,0,0,0],"no_response":0},
                {"start_time":"2026-09-02T21:30:00Z","end_time":"2026-09-02T22:00:00Z","counts":[0,0,0,2,0,0],"no_response":2},
                {"start_time":"2026-09-02T22:00:00Z","end_time":"2026-09-02T22:30:00Z","counts":[0,0,0,0,1,1],"no_response":0}
              ]
            }"""

        val result =
            RadiusHistogramParser.parse(
                200,
                body
            )

        assertTrue(result.success)
        assertEquals("gps", result.centerSource)
        assertEquals(600, result.radiusKm)
        assertEquals(7, result.observerCount)
        assertEquals(12, result.observationCount)
        assertEquals(4, result.blocks.size)
        assertEquals(listOf(1, 2, 0, 0, 0, 0), result.blocks[0].ratingCounts)
        assertEquals(1, result.blocks[0].noResponseCount)
    }
}
