package de.brummbeobachter.app

import org.junit.Assert.assertTrue
import org.junit.Test

class ObservationCommentPayloadTest {

    @Test
    fun optional_comment_is_serialized() {
        val payload =
            ObservationPayload(
                key = "KEY",
                eventId = "event-1",
                scheduledTime = "2026-09-03T08:00:00Z",
                responseTime = "2026-09-03T08:01:00Z",
                rating = 4,
                latitude = 49.5,
                longitude = 8.5,
                appVersion = "0.4.22",
                comment = "tiefer Ton, Fenster offen"
            )

        assertTrue(
            payload.toJson().contains(
                "\"comment\":\"tiefer Ton, Fenster offen\""
            )
        )
    }
}
