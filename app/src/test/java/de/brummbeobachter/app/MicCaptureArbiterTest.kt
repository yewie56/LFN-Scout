package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class MicCaptureArbiterTest {
    @Test fun mutuallyExclusiveAndIdempotentRelease() {
        val lease = MicCaptureArbiter.tryAcquire("direct_recording")!!
        try {
            assertEquals("direct_recording", MicCaptureArbiter.owner)
            assertNull(MicCaptureArbiter.tryAcquire("rolling"))
        } finally { lease.close() }
        lease.close()
        assertNull(MicCaptureArbiter.owner)
        MicCaptureArbiter.tryAcquire("rolling")!!.use {
            assertEquals("rolling", MicCaptureArbiter.owner)
            assertNull(MicCaptureArbiter.tryAcquire("direct_recording"))
        }
        assertNull(MicCaptureArbiter.owner)
    }
}
