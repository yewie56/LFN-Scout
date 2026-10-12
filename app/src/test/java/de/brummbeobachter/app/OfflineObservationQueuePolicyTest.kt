package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineObservationQueuePolicyTest {

    private fun item(
        eventId: String,
        rating: Int,
        createdAt: Long
    ) =
        OfflineQueueItem(
            eventId = eventId,
            scheduledTime = "2026-09-02T22:06:36+02:00",
            responseTime = null,
            rating = rating,
            latitude = 0.0,
            longitude = 0.0,
            appVersion = "0.4.15",
            createdAtEpochMs = createdAt
        )

    @Test
    fun same_event_id_replaces_existing_entry_instead_of_duplicating() {
        val result =
            OfflineObservationQueuePolicy.upsert(
                listOf(item("same-id", 1, 1000L)),
                item("same-id", 2, 2000L)
            )

        assertEquals(1, result.size)
        assertEquals(2, result.single().rating)
        assertEquals(2000L, result.single().createdAtEpochMs)
    }

    @Test
    fun successful_event_is_removed_only_by_its_event_id() {
        val result =
            OfflineObservationQueuePolicy.removeByEventId(
                listOf(
                    item("a", 1, 1000L),
                    item("b", -1, 2000L)
                ),
                "a"
            )

        assertEquals(1, result.size)
        assertEquals("b", result.single().eventId)
    }

    @Test
    fun ratings_minus_one_through_five_are_queueable() {
        for (rating in -1..5) {
            assertTrue(
                OfflineObservationQueuePolicy.isQueueableRating(rating)
            )
        }
        assertFalse(
            OfflineObservationQueuePolicy.isQueueableRating(-2)
        )
        assertFalse(
            OfflineObservationQueuePolicy.isQueueableRating(6)
        )
    }
}
