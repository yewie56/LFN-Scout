package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class RollingFilterHalfHourStatisticsTest {
    @Test fun tenBandsAndBucketBoundary() {
        val completed = mutableListOf<List<RollingFilterHalfHourStatistics.BandResult>>()
        val start = 1_800_000L * 100
        val bank = RollingFilterHalfHourStatistics(onCompleted = { completed.add(it) })
        for (i in 0 until 2000) bank.accept(1000, start + i)
        bank.accept(1000, start + 1_800_000L)
        assertEquals(1, completed.size)
        assertEquals(10, completed.single().size)
        assertEquals(0.0, completed.single().first().minimumHz, 0.00001)
        assertEquals(100.0, completed.single().last().maximumHz, 0.00001)
        assertEquals(2, completed.single().first().validEpochs)
    }
    @Test fun twentyBandsFiveHzEach() {
        val completed = mutableListOf<List<RollingFilterHalfHourStatistics.BandResult>>()
        val bank = RollingFilterHalfHourStatistics(bandCount=20, onCompleted={completed.add(it)})
        bank.accept(0, 1_800_000L)
        bank.stop()
        assertEquals(20, completed.single().size)
        assertEquals(5.0, completed.single().first().maximumHz, 0.00001)
        assertEquals("NO_DATA", completed.single().first().qualityStatus)
    }
    @Test(expected=IllegalArgumentException::class)
    fun invalidCountRejected() { RollingFilterHalfHourStatistics(bandCount=40) }
    @Test fun performanceWindowIsResetAndMethodIsReported() {
        for (m in RollingFilterHalfHourStatistics.Method.entries) {
            val bank = RollingFilterHalfHourStatistics(method=m, bandCount=20)
            repeat(1100) { i -> bank.accept(1000, 1_800_000L + i) }
            val first=bank.consumePerformanceWindow()
            assertEquals(m.name, first.methodName)
            assertEquals(20, first.bandCount)
            assertEquals(1100L, first.samples)
            assertEquals(1L, first.epochCount)
            assertTrue(first.meanMicrosPerSample >= 0)
            assertTrue(first.peakEpochMicros >= 0)
            assertEquals(0L, bank.performance().samples)
            bank.accept(1000, 1_800_000L + 1100)
            assertEquals(1L, bank.performance().samples)
        }
    }
    @Test fun performanceTenBandsAllMethods() {
        for (m in RollingFilterHalfHourStatistics.Method.entries) {
            val bank = RollingFilterHalfHourStatistics(method=m, bandCount=10)
            repeat(1200) { i -> bank.accept(1000, 3_600_000L + i) }
            assertEquals(10, bank.performance().bandCount)
            assertEquals(1200L, bank.performance().samples)
            assertTrue(bank.performance().realtimePercent >= 0.0)
        }
    }
}
