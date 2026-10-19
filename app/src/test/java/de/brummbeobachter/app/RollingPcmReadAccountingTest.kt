package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class RollingPcmReadAccountingTest {
    @Test fun countsActualReadResultsAndSequenceGaps() {
        val meter = RollingPcmReadAccounting()
        meter.begin(100L)
        meter.onRead(2048, 200L)
        meter.onRead(0, 300L)
        meter.onRead(-3, 400L)
        meter.onRead(512, 500L, 5L) // simulated missing callback sequence
        val s = meter.snapshot()
        assertEquals(2L, s.blocks)
        assertEquals(2560L, s.samples)
        assertEquals(1L, s.zeroReads)
        assertEquals(1L, s.readErrors)
        assertEquals(1L, s.sequenceErrors)
        assertEquals(0L, s.timestampRegressions)
        meter.onRead(8, 450L)
        assertEquals(1L, meter.snapshot().timestampRegressions)
        meter.begin(600L)
        assertEquals(s.epoch + 1, meter.snapshot().epoch)
        assertEquals(0L, meter.snapshot().samples)
        assertEquals(0L, meter.snapshot().timestampRegressions)
    }
}
