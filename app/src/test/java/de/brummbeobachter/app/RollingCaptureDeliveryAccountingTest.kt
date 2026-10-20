package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class RollingCaptureDeliveryAccountingTest {
    @Test fun completedBlocksCountSourceAndConsumerAtomically() {
        val accounting = RollingCaptureDeliveryAccounting()
        accounting.begin()
        val initial = accounting.snapshot()
        accounting.completedRead(1024, 128)
        accounting.completedRead(1024, 128)
        val final = accounting.snapshot()
        assertEquals(initial.epoch, final.epoch)
        assertEquals(2L, final.blocks)
        assertEquals(2048L, final.sourceSamples)
        assertEquals(256L, final.emittedSamples)
        assertEquals(0L, final.sourceReadErrors)
    }

    @Test fun epochChangesAreDetectableAndReadErrorsNeverBecomeSamples() {
        val accounting = RollingCaptureDeliveryAccounting()
        accounting.begin()
        val previous = accounting.snapshot()
        accounting.completedRead(-3, 0)
        assertEquals(1L, accounting.snapshot().sourceReadErrors)
        assertEquals(0L, accounting.snapshot().sourceSamples)
        accounting.begin()
        val restarted = accounting.snapshot()
        assertEquals(previous.epoch + 1L, restarted.epoch)
        assertEquals(0L, restarted.sourceReadErrors)
    }

    @Test fun partialDecimatorBlocksHaveBoundedResidual() {
        val accounting = RollingCaptureDeliveryAccounting()
        accounting.begin()
        accounting.completedRead(15, 1)
        val a = accounting.snapshot()
        accounting.completedRead(10, 2)
        val b = accounting.snapshot()
        val residual = (b.sourceSamples - a.sourceSamples) -
            8L * (b.emittedSamples - a.emittedSamples)
        assertEquals(-6L, residual)
    }
}
