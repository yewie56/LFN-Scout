package de.brummbeobachter.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MonPcmWriteAccountingTest {
    @Test fun requiresEveryAcceptedBlockWrittenAndExactFileLength() {
        val meter = MonPcmWriteAccounting()
        assertFalse(meter.snapshot().complete(44))
        meter.accepted(128)
        meter.accepted(256)
        meter.written(128)
        assertFalse(meter.snapshot().complete(428))
        meter.written(256)
        assertTrue(meter.snapshot().complete(428))
        assertFalse(meter.snapshot().complete(427))
    }

    @Test fun failedQueueOfferDisqualifiesOutput() {
        val meter = MonPcmWriteAccounting()
        meter.accepted(512)
        meter.written(512)
        meter.rejected()
        assertFalse(meter.snapshot().complete(556))
    }
}
