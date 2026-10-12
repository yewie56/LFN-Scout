package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class LocalHalfHourObservationsTest {
    private fun e(id: String, second: Long, rating: Int) = LocalHalfHourObservations.Event(id, second * 1000, rating)

    @Test fun inclusiveStartExclusiveEndAndDeduplication() {
        val events = listOf(e("a", 1800, 0), e("b", 1900, 5), e("b", 1900, 5),
            e("c", 2000, -1), e("d", 3599, 3), e("e", 3600, 2), e("f", 1799, 4))
        val s = LocalHalfHourObservations.summarize(events, 1800_000, 3600_000)
        assertEquals(3, s.count)
        assertEquals(8.0 / 3, s.mean!!, 1e-12)
        assertEquals(0, s.minimum)
        assertEquals(5, s.maximum)
        assertEquals(1, s.unavailable)
    }

    @Test fun onlyUnavailableIsNotZeroRating() {
        val s = LocalHalfHourObservations.summarize(listOf(e("a", 1800, -1)), 1800_000, 3600_000)
        assertNull(s.mean)
        assertNull(s.minimum)
        assertNull(s.maximum)
        assertEquals(0, s.count)
        assertEquals(1, s.unavailable)
    }

    @Test fun emptyBucketHasEmptyAverage() {
        val s = LocalHalfHourObservations.summarize(emptyList(), 0, 1800_000)
        assertEquals(0, s.count)
        assertNull(s.mean)
        assertEquals(0, s.unavailable)
    }

    private fun payload(id: String, rating: Int, scheduled: String, response: String?) = ObservationPayload(
        key = "test", eventId = id, scheduledTime = scheduled, responseTime = response,
        rating = rating, latitude = 0.0, longitude = 0.0, appVersion = "test"
    )

    @Test fun actualResponseTimeWinsForRatingsIncludingZero() {
        val scheduled = "2026-10-11T10:29:59+02:00"
        val response = "2026-10-11T10:30:00+02:00"
        val event = LocalHalfHourObservations.fromPayload(payload("answer", 0, scheduled, response))!!
        assertEquals(java.time.OffsetDateTime.parse(response).toInstant().toEpochMilli(), event.timeMs)
        val boundary = event.timeMs
        assertEquals(0, LocalHalfHourObservations.summarize(listOf(event), boundary - 1800000, boundary).count)
        assertEquals(1, LocalHalfHourObservations.summarize(listOf(event), boundary, boundary + 1800000).count)
    }

    @Test fun unavailableUsesScheduledNotResponseTime() {
        val scheduled = "2026-10-11T11:29:59+02:00"
        val response = "2026-10-11T11:45:00+02:00"
        val event = LocalHalfHourObservations.fromPayload(payload("timeout", -1, scheduled, response))!!
        assertEquals(java.time.OffsetDateTime.parse(scheduled).toInstant().toEpochMilli(), event.timeMs)
        val next = java.time.OffsetDateTime.parse("2026-10-11T11:30:00+02:00").toInstant().toEpochMilli()
        assertEquals(1, LocalHalfHourObservations.summarize(listOf(event), next-1800000, next).unavailable)
        assertEquals(0, LocalHalfHourObservations.summarize(listOf(event), next, next+1800000).unavailable)
    }

    @Test fun sameInstantAcrossOffsetAndDuplicateIdCountsOnce() {
        val a = LocalHalfHourObservations.fromPayload(payload("same", 4, "2026-10-11T10:00:00+02:00", "2026-10-11T10:30:00+02:00"))!!
        val b = LocalHalfHourObservations.fromPayload(payload("same", 4, "2026-10-11T08:00:00Z", "2026-10-11T08:30:00Z"))!!
        assertEquals(a.timeMs, b.timeMs)
        val s = LocalHalfHourObservations.summarize(listOf(a,b), a.timeMs, a.timeMs+1800000)
        assertEquals(1, s.count)
        assertEquals(4.0, s.mean!!, 0.0)
    }

    @Test fun invalidTimestampsRatingsAndMissingResponseRejected() {
        assertNull(LocalHalfHourObservations.fromPayload(payload("a", 2, "2026-10-11T10:00:00Z", null)))
        assertNull(LocalHalfHourObservations.fromPayload(payload("a", -1, "invalid", null)))
        assertNull(LocalHalfHourObservations.fromPayload(payload("", 2, "2026-10-11T10:00:00Z", "2026-10-11T10:00:00Z")))
        assertNull(LocalHalfHourObservations.fromPayload(payload("b", 6, "2026-10-11T10:00:00Z", "2026-10-11T10:00:00Z")))
        assertNull(LocalHalfHourObservations.fromPayload(payload("b", -2, "2026-10-11T10:00:00Z", null)))
    }

    @Test fun onlyZeroIsValidMeanZeroButNoAnswerIsEmpty() {
        val s = LocalHalfHourObservations.summarize(listOf(e("z", 100, 0),e("u",110,-1)), 0, 1800000)
        assertEquals(1, s.count)
        assertEquals(0.0, s.mean!!, 0.0)
        assertEquals(1, s.unavailable)
    }
}
