package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RollingHalfHourEpochIntegrityTest {
    private val base = 1_800_000L

    @Test fun duplicateSecondsCannotFakeCompleteHalfHour() {
        val done = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
        val stats = RollingHalfHourStatistics(onCompleted = { done.add(it) })
        repeat(1800) { second ->
            val ts = base + second * 1000L
            stats.acceptFftEpoch(ts, -50.0)
            stats.acceptFftEpoch(ts, -10.0)
        }
        stats.stop()
        assertEquals(1800, done.single().validEpochs)
        assertEquals("COMPLETE", done.single().qualityStatus)
        assertTrue(done.single().leqDbfs!! < -49.9)
    }

    @Test fun missingAndLateSecondsArePartial() {
        val done = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
        val stats = RollingHalfHourStatistics(onCompleted = { done.add(it) })
        repeat(1800) { second ->
            if (second != 27) stats.acceptFftEpoch(base + second * 1000L, -30.0)
        }
        stats.acceptFftEpoch(base + 27_000L, -30.0)
        stats.stop()
        assertEquals(1799, done.single().validEpochs)
        assertEquals("PARTIAL", done.single().qualityStatus)
    }

    @Test fun stalePreviousBucketCallbackDoesNotCorruptNewBucket() {
        val done = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
        val stats = RollingHalfHourStatistics(onCompleted = { done.add(it) })
        stats.acceptFftEpoch(base + 1000L, -30.0)
        stats.acceptFftEpoch(base + 1_800_000L, -30.0)
        stats.acceptFftEpoch(base + 2000L, -30.0)
        stats.stop()
        assertEquals(2, done.size)
        assertEquals(1, done[0].validEpochs)
        assertEquals(1, done[1].validEpochs)
    }
}
