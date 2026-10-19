package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class RollingHalfHourStatisticsTest {
    @Test fun boundariesAndPartialCoverage() {
        val results = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
        val start = 1_800_000L * 100L
        val stats = RollingHalfHourStatistics(onCompleted = { results.add(it) })
        for (i in 0 until 2_000) stats.accept(1000, start + i)
        stats.accept(1000, start + 1_800_000L)
        assertEquals(1, results.size)
        assertEquals(start, results.first().startEpochMs)
        assertEquals(2, results.first().validEpochs)
        assertEquals("PARTIAL", results.first().qualityStatus)
        assertNotNull(results.first().leqDbfs)
    }
    @Test fun noDataWhenLessThanValidEpoch() {
        val results = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
        val stats = RollingHalfHourStatistics(onCompleted = { results.add(it) })
        stats.accept(1000, 1_800_000L)
        stats.accept(1000, 3_600_000L)
        assertEquals("NO_DATA", results.single().qualityStatus)
        assertNull(results.single().leqDbfs)
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidBand() { RollingHalfHourStatistics(minimumHz = 20, maximumHz = 2) }
    @Test fun invalidFutureEpochCannotFlushActiveBucket() {
        val results = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
        val start = 1_800_000L * 100L
        val stats = RollingHalfHourStatistics(onCompleted = { results.add(it) })
        stats.acceptFftEpoch(start, -30.0)
        stats.acceptFftEpoch(start + 1_800_000L, Double.NaN)
        stats.acceptFftEpoch(start + 1_800_001L, Double.POSITIVE_INFINITY)
        assertTrue("Invalid FFT values must not close the current bucket", results.isEmpty())
        stats.acceptFftEpoch(start + 1_000L, -31.0)
        stats.acceptFftEpoch(start + 1_800_000L, -32.0)
        assertEquals(1, results.size)
        assertEquals(2, results.single().validEpochs)
        assertEquals(2, results.single().fftSecondsObserved)
        assertEquals("PARTIAL", results.single().qualityStatus)
    }
}
