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
    @Test fun sameLengthDifferentPayloadFails() {
        val meter = MonPcmWriteAccounting()
        meter.acceptedBlock(byteArrayOf(1, 2, 3, 4))
        meter.writtenBlock(byteArrayOf(1, 2, 3, 5))
        assertFalse(meter.snapshot().complete(48))
    }

    @Test fun identicalBlocksRequireExactOrderAndLength() {
        val meter = MonPcmWriteAccounting()
        val a = byteArrayOf(1, 2)
        val b = byteArrayOf(3, 4)
        meter.acceptedBlock(a)
        meter.acceptedBlock(b)
        meter.writtenBlock(a)
        meter.writtenBlock(b)
        assertTrue(meter.snapshot().complete(48))
        assertFalse(meter.snapshot().complete(49))
        val reordered = MonPcmWriteAccounting()
        reordered.acceptedBlock(a)
        reordered.acceptedBlock(b)
        reordered.writtenBlock(b)
        reordered.writtenBlock(a)
        assertFalse(reordered.snapshot().complete(48))
    }
}
