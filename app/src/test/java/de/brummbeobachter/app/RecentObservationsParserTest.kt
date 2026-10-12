package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentObservationsParserTest {

    @Test
    fun parses_recent_observations() {
        val body = """
            {
              "ok": true,
              "observer_id": "BB001",
              "observations": [
                {
                  "rating": 3,
                  "response_time": "2026-09-01T23:34:12+02:00",
                  "scheduled_time": "2026-09-01T23:30:00+02:00"
                },
                {
                  "rating": 1,
                  "response_time": null,
                  "scheduled_time": "2026-09-01T22:30:00+02:00"
                }
              ]
            }
        """.trimIndent()

        val result = RecentObservationsParser.parse(200, body)

        assertTrue(result.success)
        assertEquals(2, result.items.size)
        assertEquals(3, result.items[0].rating)
        assertEquals("2026-09-01T23:34:12+02:00", result.items[0].displayTime)
        assertEquals("2026-09-01T22:30:00+02:00", result.items[1].displayTime)
    }

    @Test
    fun handles_empty_list() {
        val result = RecentObservationsParser.parse(
            200,
            """{"ok":true,"observer_id":"BB001","observations":[]}"""
        )

        assertTrue(result.success)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun converts_http_error_to_failure() {
        val result = RecentObservationsParser.parse(
            401,
            """{"error":"invalid_or_disabled_key"}"""
        )

        assertFalse(result.success)
        assertTrue(result.message.contains("401"))
        assertTrue(result.message.contains("invalid_or_disabled_key"))
    }
}
