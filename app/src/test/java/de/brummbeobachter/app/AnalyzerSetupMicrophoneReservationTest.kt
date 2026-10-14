package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class AnalyzerSetupMicrophoneReservationTest {
    @Test fun preventsCompetingMicrophoneAcquisitionAndReleasesAfterSuccess() {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val result = AtomicReference<String?>()
        val thread = Thread {
            result.set(AnalyzerSetupMicrophoneReservation.withReservation {
                entered.countDown()
                check(release.await(5, TimeUnit.SECONDS))
                "ok"
            })
        }
        thread.start()
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            assertEquals("analyzer_setup_preflight", MicCaptureArbiter.owner)
            assertNull(MicCaptureArbiter.tryAcquire("rolling"))
            assertNull(MicCaptureArbiter.tryAcquire("direct_recording"))
        } finally {
            release.countDown()
            thread.join(5000)
        }
        assertFalse(thread.isAlive)
        assertEquals("ok", result.get())
        assertNull(MicCaptureArbiter.owner)
    }

    @Test fun rejectsWhenRecorderOwnsMicrophoneWithoutExecutingCallback() {
        MicCaptureArbiter.tryAcquire("rolling")!!.use {
            var called = false
            val result = AnalyzerSetupMicrophoneReservation.withReservation { called = true; "unexpected" }
            assertNull(result)
            assertFalse(called)
            assertEquals("rolling", MicCaptureArbiter.owner)
        }
        assertNull(MicCaptureArbiter.owner)
    }

    @Test fun releasesReservationAfterException() {
        try {
            AnalyzerSetupMicrophoneReservation.withReservation<Unit> { throw IllegalStateException("injected") }
            fail("expected injected failure")
        } catch (expected: IllegalStateException) {
            assertEquals("injected", expected.message)
        }
        assertNull(MicCaptureArbiter.owner)
    }
}
