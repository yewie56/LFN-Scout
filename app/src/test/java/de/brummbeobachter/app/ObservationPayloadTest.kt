package de.brummbeobachter.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservationPayloadTest {

    @Test
    fun payload_contains_required_fields() {
        val payload = ObservationPayload(
            key = "BB-TEST-KEY",
            eventId = "123e4567-e89b-12d3-a456-426614174000",
            scheduledTime = "2026-09-01T22:40:00+02:00",
            responseTime = "2026-09-01T22:40:03+02:00",
            rating = 4,
            latitude = 0.0,
            longitude = 0.0,
            appVersion = "0.2.0"
        )

        val json = payload.toJson()

        assertTrue(json.contains("\"key\":\"BB-TEST-KEY\""))
        assertTrue(json.contains("\"event_id\":\"123e4567-e89b-12d3-a456-426614174000\""))
        assertTrue(json.contains("\"rating\":4"))
        assertTrue(json.contains("\"latitude\":0.0"))
        assertTrue(json.contains("\"longitude\":0.0"))
        assertTrue(json.contains("\"app_version\":\"0.2.0\""))
    }

    @Test
    fun json_escapes_key_content() {
        val payload = ObservationPayload(
            key = "BB-\"TEST\\KEY",
            eventId = "event",
            scheduledTime = "time",
            responseTime = "time",
            rating = 1,
            latitude = 0.0,
            longitude = 0.0,
            appVersion = "0.2.0"
        )

        val json = payload.toJson()

        assertTrue(json.contains("BB-\\\"TEST\\\\KEY"))
        assertFalse(json.contains("BB-\"TEST\\KEY"))
    }


@Test
fun no_response_payload_uses_minus_one_null_response_and_no_wav() {
    val payload = ObservationPayload(
        key = "BB-TEST-KEY",
        eventId = "timeout-event",
        scheduledTime = "2026-09-02T22:06:36+02:00",
        responseTime = null,
        rating = -1,
        latitude = 0.0,
        longitude = 0.0,
        appVersion = "0.4.13"
    )

    val json = payload.toJson()

    assertTrue(json.contains("\"rating\":-1"))
    assertTrue(json.contains("\"response_time\":null"))
    assertTrue(json.contains("\"wav_present\":false"))
}

}