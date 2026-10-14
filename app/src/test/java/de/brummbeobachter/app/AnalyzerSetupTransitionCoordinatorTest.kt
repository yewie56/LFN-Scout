package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class AnalyzerSetupTransitionCoordinatorTest {
    @Test fun ownerBlockersRejectReservationWithoutRunningCallback() {
        val c = AnalyzerSetupTransitionCoordinator
        var invoked = false
        assertNull(c.withOwnerCheckedReservation({ listOf("microphone_busy") }) {
            invoked = true
            "must not run"
        })
        assertFalse(invoked)
        assertFalse(c.snapshotGate().setupReserved)
    }

    @Test fun ownerProbeExceptionFailsClosedWithoutPoisoningGate() {
        val c = AnalyzerSetupTransitionCoordinator
        var invoked = false
        assertNull(c.withOwnerCheckedReservation<String>({ throw IllegalStateException("probe failure") }) {
            invoked = true
            "must not run"
        })
        assertFalse(invoked)
        assertFalse(c.snapshotGate().setupReserved)
        assertEquals("ok", c.withOwnerCheckedReservation({ emptyList() }) { "ok" })
    }

    @Test fun ownerCheckedReservationRemainsHeldUntilOperationEnds() {
        val c = AnalyzerSetupTransitionCoordinator
        assertNull(c.withOwnerCheckedReservation({ emptyList() }) {
            c.withOwnerCheckedReservation({ emptyList() }) { "duplicate" }
        })
        assertFalse(c.snapshotGate().setupReserved)
    }

    @Test fun ownerCheckedReservationReleasesAfterCallbackFailure() {
        val c = AnalyzerSetupTransitionCoordinator
        try {
            c.withOwnerCheckedReservation<Unit>({ emptyList() }) {
                throw IllegalArgumentException("operation failure")
            }
            fail("exception expected")
        } catch (_: IllegalArgumentException) { }
        assertFalse(c.snapshotGate().setupReserved)
    }

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

    @Test fun snapshotReportsInFlightAndCaptureCleanupWithoutClearingThem() {
        val c = AnalyzerSetupTransitionCoordinator
        assertTrue(c.withTrackedServiceStart(c.ROLLING) { true })
        c.captureJobPending(c.ROLLING)
        c.serviceDestroyed(c.ROLLING)
        try {
            val snapshot = c.snapshotGate()
            assertTrue(snapshot.serviceLifecycleBlocked)
            assertTrue(snapshot.inFlightServices.contains(c.ROLLING))
            assertTrue(snapshot.destroyedServices.contains(c.ROLLING))
            assertTrue(snapshot.captureJobsPending.contains(c.ROLLING))
            // Snapshot is a copy: lifecycle remains blocked until actual completion.
            assertNull(c.withSetupReservation { "unsafe" })
        } finally { c.captureJobFinished(c.ROLLING) }
        assertFalse(c.snapshotGate().serviceLifecycleBlocked)
    }

    @Test fun snapshotCopiesCannotMutateFutureCoordinatorState() {
        val c = AnalyzerSetupTransitionCoordinator
        assertTrue(c.withTrackedServiceStart(c.DIRECT) { true })
        val oldSnapshot = c.snapshotGate()
        c.serviceDestroyed(c.DIRECT)
        assertTrue(oldSnapshot.inFlightServices.contains(c.DIRECT))
        assertFalse(c.snapshotGate().inFlightServices.contains(c.DIRECT))
    }

    @Test fun gateAssessmentReportsLifecycleAndCleanupReasonsWithoutChangingState() {
        val c = AnalyzerSetupTransitionCoordinator
        assertTrue(c.withTrackedServiceStart(c.ROLLING) { true })
        c.captureJobPending(c.ROLLING)
        c.serviceDestroyed(c.ROLLING)
        try {
            val result = c.assessGate()
            assertFalse(result.coordinatorMarkersClear)
            assertTrue(result.blockingReasons.contains("service_in_flight:rolling"))
            assertTrue(result.blockingReasons.contains("capture_cleanup_pending:rolling"))
            assertTrue(c.hasInFlightServiceStarts())
            assertNull(c.withSetupReservation { "still blocked" })
        } finally { c.captureJobFinished(c.ROLLING) }
    }

    @Test fun gateAssessmentClearNeverImpliesExecutableSetupSwitch() {
        val c = AnalyzerSetupTransitionCoordinator
        val assessment = c.assessGate()
        assertTrue(assessment.coordinatorMarkersClear)
        assertTrue(assessment.blockingReasons.isEmpty())
        // This merely checks internal markers; external audio owners still require checks.
        val inside = c.withSetupReservation { c.assessGate() }
        assertNotNull(inside)
        assertFalse(inside!!.coordinatorMarkersClear)
        assertTrue(inside.blockingReasons.contains("setup_reserved"))
    }

    @Test fun combinedDiagnosisNeverGrantsPermissionEvenIfOwnerAndMarkersClear() {
        val c = AnalyzerSetupTransitionCoordinator
        val result = c.diagnoseSafety {
            AnalyzerSetupAudioOwnerGate.Observation(null, false, false, false)
        }
        assertTrue(result.blockers.isEmpty())
        assertFalse(result.executionAuthorized)
    }

    @Test fun combinedDiagnosisPreservesBothOwnerAndCleanupBlockers() {
        val c = AnalyzerSetupTransitionCoordinator
        assertTrue(c.withTrackedServiceStart(c.DIRECT) { true })
        c.captureJobPending(c.DIRECT)
        c.serviceDestroyed(c.DIRECT)
        try {
            val result = c.diagnoseSafety {
                AnalyzerSetupAudioOwnerGate.Observation("scout", false, true, false)
            }
            assertTrue(result.blockers.contains("capture_cleanup_pending:direct"))
            assertTrue(result.blockers.any { it.contains("Mikrofon belegt") })
            assertTrue(result.blockers.any { it.contains("Direct-Audio-Service") })
            assertFalse(result.executionAuthorized)
        } finally { c.captureJobFinished(c.DIRECT) }
    }

    @Test fun combinedDiagnosisFailsClosedWhenOwnerProbeThrows() {
        val result = AnalyzerSetupTransitionCoordinator.diagnoseSafety {
            throw IllegalStateException("probe unavailable")
        }
        assertTrue(result.blockers.contains("audio_owner_probe_failed"))
        assertFalse(result.executionAuthorized)
    }

    @Test fun unknownTrackedServiceNeverRunsSubmission() {
        val c = AnalyzerSetupTransitionCoordinator
        var called = false
        assertFalse(c.withTrackedServiceStart("unknown-service") { called = true; true })
        assertFalse(called)
        assertTrue(c.assessGate().coordinatorMarkersClear)
    }

    @Test fun reservationRejectedDuringOutstandingServiceLifecycle() {
        val c = AnalyzerSetupTransitionCoordinator
        assertTrue(c.withTrackedServiceStart(c.DIRECT) { true })
        try {
            assertFalse(c.assessGate().coordinatorMarkersClear)
            assertNull(c.withSetupReservation { "not allowed" })
        } finally { c.serviceDestroyed(c.DIRECT) }
        assertTrue(c.assessGate().coordinatorMarkersClear)
    }

    @Test fun setupReservationBlocksOtherReservationAndTrackedStart() {
        val c = AnalyzerSetupTransitionCoordinator
        val result = c.withSetupReservation {
            var called = false
            assertNull(c.withSetupReservation { "nested" })
            assertFalse(c.withTrackedServiceStart(c.ROLLING) { called = true; true })
            assertFalse(called)
            assertTrue(c.assessGate().blockingReasons.contains("setup_reserved"))
            "held"
        }
        assertEquals("held", result)
        assertTrue(c.assessGate().coordinatorMarkersClear)
    }

    @Test fun overlappingCaptureGenerationsRemainBlockedUntilBothComplete() {
        val c = AnalyzerSetupTransitionCoordinator
        assertTrue(c.withTrackedServiceStart(c.ROLLING) { true })
        val first = c.captureJobRegistered(c.ROLLING)
        val second = c.captureJobRegistered(c.ROLLING)
        c.serviceDestroyed(c.ROLLING)
        c.captureJobCompleted(c.ROLLING, first)
        try {
            assertTrue(c.hasPendingCaptureCleanup())
            assertNull(c.withSetupReservation { "unsafe" })
            assertTrue(c.assessGate().blockingReasons.contains("capture_cleanup_pending:rolling"))
        } finally { c.captureJobCompleted(c.ROLLING, second) }
        assertFalse(c.hasPendingCaptureCleanup())
        assertFalse(c.hasInFlightServiceStarts())
    }

    @Test fun staleAndDuplicateCompletionCannotReleaseNewGeneration() {
        val c = AnalyzerSetupTransitionCoordinator
        assertTrue(c.withTrackedServiceStart(c.DIRECT) { true })
        val old = c.captureJobRegistered(c.DIRECT)
        c.captureJobCompleted(c.DIRECT, old)
        val current = c.captureJobRegistered(c.DIRECT)
        c.serviceDestroyed(c.DIRECT)
        c.captureJobCompleted(c.DIRECT, old)
        c.captureJobFinished(c.DIRECT) // legacy completion cannot release a token
        try {
            assertTrue(c.hasPendingCaptureCleanup())
            assertNull(c.withSetupReservation { "unsafe" })
        } finally { c.captureJobCompleted(c.DIRECT, current) }
        assertFalse(c.hasPendingCaptureCleanup())
    }

}
