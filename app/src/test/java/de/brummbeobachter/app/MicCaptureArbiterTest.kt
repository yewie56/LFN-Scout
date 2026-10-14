package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class MicCaptureArbiterTest {
    @Test fun failedHardwareReleaseKeepsMicrophoneReserved() {
        val lease = MicCaptureArbiter.tryAcquire("release_failure")!!
        try {
            lease.markHardwareReleaseUncertain()
            lease.close()
            lease.close()
            assertEquals("release_failure", MicCaptureArbiter.owner)
            assertEquals("release_failure", MicCaptureArbiter.uncertainReleaseOwner)
            assertNull(MicCaptureArbiter.tryAcquire("rolling"))
        } finally {
            // Test-only cleanup via reflection: no production API may clear an unsafe lease.
            val active = MicCaptureArbiter::class.java.getDeclaredField("active")
            active.isAccessible = true
            active.set(null, null)
        }
        assertNull(MicCaptureArbiter.owner)
    }

    @Test fun ordinaryReleaseHasNoUncertainMarker() {
        val lease = MicCaptureArbiter.tryAcquire("normal")!!
        assertNull(MicCaptureArbiter.uncertainReleaseOwner)
        lease.close()
        assertNull(MicCaptureArbiter.uncertainReleaseOwner)
    }

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
