package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class AnalyzerSetupTransitionCoordinatorTest {
    @Test fun setupReservationBlocksConcurrentMonStart() {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val t = Thread {
            AnalyzerSetupTransitionCoordinator.withSetupReservation {
                entered.countDown()
                release.await(3, TimeUnit.SECONDS)
            }
        }
        t.start()
        try {
            assertTrue(entered.await(3, TimeUnit.SECONDS))
            val called = AtomicBoolean(false)
            assertFalse(AnalyzerSetupTransitionCoordinator.withMonStart { called.set(true); true })
            assertFalse(called.get())
        } finally { release.countDown(); t.join(3000) }
        assertTrue(AnalyzerSetupTransitionCoordinator.withMonStart { true })
    }

    @Test fun failingReservationReleasesGate() {
        try {
            AnalyzerSetupTransitionCoordinator.withSetupReservation<Unit> { throw IllegalStateException("test") }
            fail("exception expected")
        } catch (_: IllegalStateException) { }
        assertTrue(AnalyzerSetupTransitionCoordinator.withMonStart { true })
    }

    @Test fun secondSetupReservationRejected() {
        val result = AnalyzerSetupTransitionCoordinator.withSetupReservation {
            AnalyzerSetupTransitionCoordinator.withSetupReservation { "not allowed" }
        }
        assertNull(result)
    }
    @Test fun serviceStartRejectedWhileSetupReserved() {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val thread = Thread {
            AnalyzerSetupTransitionCoordinator.withSetupReservation {
                entered.countDown()
                release.await(3, TimeUnit.SECONDS)
            }
        }
        thread.start()
        try {
            assertTrue(entered.await(3, TimeUnit.SECONDS))
            val invoked = AtomicBoolean(false)
            assertFalse(AnalyzerSetupTransitionCoordinator.withServiceStart {
                invoked.set(true)
                true
            })
            assertFalse(invoked.get())
        } finally {
            release.countDown()
            thread.join(3000)
        }
        assertTrue(AnalyzerSetupTransitionCoordinator.withServiceStart { true })
    }

    @Test fun serviceStartExceptionDoesNotPoisonCoordinator() {
        try {
            AnalyzerSetupTransitionCoordinator.withServiceStart { throw IllegalStateException("boom") }
            fail("expected")
        } catch (_: IllegalStateException) { }
        assertTrue(AnalyzerSetupTransitionCoordinator.withServiceStart { true })
    }

    @Test fun acceptedAsyncStartBlocksSetupUntilDestroy() {
        assertTrue(AnalyzerSetupTransitionCoordinator.withTrackedServiceStart(AnalyzerSetupTransitionCoordinator.DIRECT) { true })
        try {
            assertTrue(AnalyzerSetupTransitionCoordinator.hasInFlightServiceStarts())
            assertNull(AnalyzerSetupTransitionCoordinator.withSetupReservation { "unsafe" })
        } finally { AnalyzerSetupTransitionCoordinator.serviceDestroyed(AnalyzerSetupTransitionCoordinator.DIRECT) }
        assertFalse(AnalyzerSetupTransitionCoordinator.hasInFlightServiceStarts())
    }

    @Test fun failedAsyncStartDoesNotPoisonSetupGate() {
        assertFalse(AnalyzerSetupTransitionCoordinator.withTrackedServiceStart(AnalyzerSetupTransitionCoordinator.ROLLING) { false })
        assertFalse(AnalyzerSetupTransitionCoordinator.hasInFlightServiceStarts())
    }

    @Test fun exceptionReleasesAsyncStartMarker() {
        try {
            AnalyzerSetupTransitionCoordinator.withTrackedServiceStart(AnalyzerSetupTransitionCoordinator.DIRECT) { throw IllegalStateException("failed") }
            fail("expected")
        } catch (_: IllegalStateException) { }
        assertFalse(AnalyzerSetupTransitionCoordinator.hasInFlightServiceStarts())
    }

    @Test fun destroyCannotReleaseWhileCaptureCleanupPending() {
        val coordinator = AnalyzerSetupTransitionCoordinator
        assertTrue(coordinator.withTrackedServiceStart(coordinator.ROLLING) { true })
        coordinator.captureJobPending(coordinator.ROLLING)
        coordinator.serviceDestroyed(coordinator.ROLLING)
        try {
            assertTrue(coordinator.hasInFlightServiceStarts())
            assertTrue(coordinator.hasPendingCaptureCleanup())
            assertNull(coordinator.withSetupReservation { "must stay blocked" })
        } finally { coordinator.captureJobFinished(coordinator.ROLLING) }
        assertFalse(coordinator.hasInFlightServiceStarts())
        assertFalse(coordinator.hasPendingCaptureCleanup())
    }

    @Test fun cleanupBeforeDestroyAlsoWaitsForLifecycle() {
        val coordinator = AnalyzerSetupTransitionCoordinator
        assertTrue(coordinator.withTrackedServiceStart(coordinator.DIRECT) { true })
        coordinator.captureJobPending(coordinator.DIRECT)
        coordinator.captureJobFinished(coordinator.DIRECT)
        assertTrue(coordinator.hasInFlightServiceStarts())
        coordinator.serviceDestroyed(coordinator.DIRECT)
        assertFalse(coordinator.hasInFlightServiceStarts())
    }

    @Test fun noCaptureJobDestroyReleasesMarker() {
        val coordinator = AnalyzerSetupTransitionCoordinator
        assertTrue(coordinator.withTrackedServiceStart(coordinator.DIRECT) { true })
        coordinator.serviceDestroyed(coordinator.DIRECT)
        assertFalse(coordinator.hasInFlightServiceStarts())
    }

    @Test fun duplicateAcceptedStartIsRejectedWithoutCallingSecondSubmission() {
        val coordinator = AnalyzerSetupTransitionCoordinator
        var secondInvoked = false
        assertTrue(coordinator.withTrackedServiceStart(coordinator.DIRECT) { true })
        try {
            assertFalse(coordinator.withTrackedServiceStart(coordinator.DIRECT) {
                secondInvoked = true
                true
            })
            assertFalse(secondInvoked)
            assertTrue(coordinator.hasInFlightServiceStarts())
            assertNull(coordinator.withSetupReservation { "must stay blocked" })
        } finally { coordinator.serviceDestroyed(coordinator.DIRECT) }
        assertFalse(coordinator.hasInFlightServiceStarts())
    }

    @Test fun duplicateStartDuringCaptureCleanupIsRejected() {
        val coordinator = AnalyzerSetupTransitionCoordinator
        assertTrue(coordinator.withTrackedServiceStart(coordinator.ROLLING) { true })
        coordinator.captureJobPending(coordinator.ROLLING)
        coordinator.serviceDestroyed(coordinator.ROLLING)
        try {
            assertFalse(coordinator.withTrackedServiceStart(coordinator.ROLLING) { true })
            assertTrue(coordinator.hasPendingCaptureCleanup())
        } finally { coordinator.captureJobFinished(coordinator.ROLLING) }
        assertFalse(coordinator.hasInFlightServiceStarts())
    }

    @Test fun successfulCleanupAllowsNewServiceStart() {
        val coordinator = AnalyzerSetupTransitionCoordinator
        assertTrue(coordinator.withTrackedServiceStart(coordinator.DIRECT) { true })
        coordinator.serviceDestroyed(coordinator.DIRECT)
        assertTrue(coordinator.withTrackedServiceStart(coordinator.DIRECT) { true })
        coordinator.serviceDestroyed(coordinator.DIRECT)
        assertFalse(coordinator.hasInFlightServiceStarts())
    }

    @Test fun lateDuplicateDestroyDoesNotPoisonNextLifecycle() {
        val c = AnalyzerSetupTransitionCoordinator
        assertTrue(c.withTrackedServiceStart(c.DIRECT) { true })
        c.serviceDestroyed(c.DIRECT)
        c.serviceDestroyed(c.DIRECT) // repeated callback after release
        assertTrue(c.withTrackedServiceStart(c.DIRECT) { true })
        c.captureJobPending(c.DIRECT)
        c.captureJobFinished(c.DIRECT) // must NOT release before actual destroy
        assertTrue(c.hasInFlightServiceStarts())
        c.serviceDestroyed(c.DIRECT)
        assertFalse(c.hasInFlightServiceStarts())
    }

    @Test fun duplicateCaptureCompletionCannotReleaseActiveService() {
        val c = AnalyzerSetupTransitionCoordinator
        assertTrue(c.withTrackedServiceStart(c.ROLLING) { true })
        c.captureJobPending(c.ROLLING)
        c.captureJobFinished(c.ROLLING)
        c.captureJobFinished(c.ROLLING) // duplicate callback
        assertTrue(c.hasInFlightServiceStarts())
        c.serviceDestroyed(c.ROLLING)
        assertFalse(c.hasInFlightServiceStarts())
    }

}
