package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class AnalyzerSetupTransitionProtocolTest {
    private class Fake : AnalyzerSetupTransitionProtocol.ControlledPort {
        val log = mutableListOf<String>()
        var failedOperation: String? = null
        var failOnlyOnce = true
        var restored = false
        var verifyNew = true
        var verifyOld = true
        var safe = true
        private fun enter(action: String) {
            log.add(action)
            if (failedOperation == action) {
                if (failOnlyOnce) failedOperation = null
                throw IllegalStateException("fault: $action")
            }
        }
        override fun checkpoint(): Any { enter("checkpoint"); return "previous-state" }
        override fun stopAndRelease() = enter("stop")
        override fun applyDraft() = enter("apply")
        override fun startSession() = enter("start")
        override fun verifyNewSession(): Boolean { enter("verify-new"); return verifyNew }
        override fun restoreCheckpoint(checkpoint: Any) { assertEquals("previous-state", checkpoint); restored = true; enter("restore") }
        override fun verifyRestoredSession(): Boolean { enter("verify-old"); return verifyOld }
        override fun verifySafelyStopped(): Boolean { enter("verify-safe"); return safe }
    }

    @Test fun successfulSequence() {
        val p = Fake()
        val result = AnalyzerSetupTransitionProtocol.exerciseWithControlledPort(p)
        assertTrue(result.succeeded)
        assertEquals(AnalyzerSetupTransitionProtocol.Stage.COMPLETE, result.stage)
        assertEquals(listOf("checkpoint", "stop", "apply", "start", "verify-new"), p.log)
    }

    @Test fun failedCheckpointNeverStopsOldSession() {
        val p = Fake().apply { failedOperation = "checkpoint" }
        val result = AnalyzerSetupTransitionProtocol.exerciseWithControlledPort(p)
        assertFalse(result.succeeded)
        assertTrue(result.rollbackSucceeded)
        assertEquals(listOf("checkpoint"), p.log)
    }

    @Test fun failureAtEveryTransitionRestoresOldSession() {
        for (action in listOf("stop", "apply", "start", "verify-new")) {
            val p = Fake().apply { failedOperation = action }
            val result = AnalyzerSetupTransitionProtocol.exerciseWithControlledPort(p)
            assertFalse(action, result.succeeded)
            assertTrue(action, result.rollbackSucceeded)
            assertNotNull(result.originalError)
            assertTrue(action, p.restored)
            assertEquals("verify-old", p.log.last())
        }
    }

    @Test fun falseSessionVerificationTriggersRollback() {
        val p = Fake().apply { verifyNew = false }
        val result = AnalyzerSetupTransitionProtocol.exerciseWithControlledPort(p)
        assertFalse(result.succeeded)
        assertTrue(result.rollbackSucceeded)
        assertTrue(p.restored)
    }

    @Test fun rollbackRestoreFailurePreservesPrimaryErrorAndStopsSafely() {
        val p = Fake().apply { failedOperation = "restore"; verifyNew = false }
        val result = AnalyzerSetupTransitionProtocol.exerciseWithControlledPort(p)
        assertFalse(result.succeeded)
        assertFalse(result.rollbackSucceeded)
        assertEquals(AnalyzerSetupTransitionProtocol.Stage.SAFE_STOPPED, result.stage)
        assertNotNull(result.originalError)
        assertEquals(1, result.recoveryErrors.size)
        assertEquals("verify-safe", p.log.last())
    }

    @Test fun uncertainFinalStopNeverClaimsSafeState() {
        val p = Fake().apply { verifyNew = false; verifyOld = false; safe = false }
        val result = AnalyzerSetupTransitionProtocol.exerciseWithControlledPort(p)
        assertFalse(result.succeeded)
        assertFalse(result.rollbackSucceeded)
        assertNotEquals(AnalyzerSetupTransitionProtocol.Stage.SAFE_STOPPED, result.stage)
        assertTrue(result.recoveryErrors.isNotEmpty())
    }
    private class Boundary : AnalyzerSetupTransitionProtocol.ExclusiveBoundary {
        var available = true
        var valid = true
        var held = false
        var entries = 0
        override fun <T> underExclusiveOwnership(operation: () -> T): T? {
            if (!available || held) return null
            held = true
            entries++
            try { return operation() } finally { held = false }
        }
        override fun ownershipStillExclusive(): Boolean = held && valid
    }

    @Test fun unavailableLeaseNeverTouchesController() {
        val p = Fake()
        val boundary = Boundary().apply { available = false }
        val result = AnalyzerSetupTransitionProtocol.exerciseWithBoundary(p, boundary)
        assertFalse(result.succeeded)
        assertFalse(result.rollbackSucceeded)
        assertTrue(p.log.isEmpty())
        assertEquals(0, boundary.entries)
    }

    @Test fun leaseHeldThroughSuccessAndReleased() {
        val p = Fake()
        val boundary = Boundary()
        val result = AnalyzerSetupTransitionProtocol.exerciseWithBoundary(p, boundary)
        assertTrue(result.succeeded)
        assertFalse(boundary.held)
        assertEquals(1, boundary.entries)
    }

    @Test fun leaseHeldThroughRollbackAndReleased() {
        val p = Fake().apply { verifyNew = false }
        val boundary = Boundary()
        val result = AnalyzerSetupTransitionProtocol.exerciseWithBoundary(p, boundary)
        assertFalse(result.succeeded)
        assertTrue(result.rollbackSucceeded)
        assertEquals("verify-old", p.log.last())
        assertFalse(boundary.held)
    }

    @Test fun unavailableValidityNeverClaimsRollbackSucceeded() {
        val p = Fake()
        val boundary = Boundary().apply { valid = false }
        val result = AnalyzerSetupTransitionProtocol.exerciseWithBoundary(p, boundary)
        assertFalse(result.succeeded)
        assertFalse(result.rollbackSucceeded)
        assertEquals(AnalyzerSetupTransitionProtocol.Stage.CHECKPOINT, result.stage)
        assertNotNull(result.originalError)
        assertTrue(result.originalError!!.message!!.contains("ownership lost"))
        assertTrue(p.log.isEmpty())
        assertFalse(boundary.held)
    }

    @Test fun lostLeaseDoesNotClaimSafeStopped() {
        val p = Fake()
        val boundary = Boundary().apply { valid = false }
        val result = AnalyzerSetupTransitionProtocol.exerciseWithBoundary(p, boundary)
        assertFalse(result.succeeded)
        assertFalse(result.rollbackSucceeded)
        assertNotEquals(AnalyzerSetupTransitionProtocol.Stage.SAFE_STOPPED, result.stage)
        assertTrue(p.log.isEmpty())
        assertFalse(boundary.held)
    }

    @Test fun leaseLostAfterStopDoesNotRestartOrClaimRollback() {
        val p = Fake()
        val boundary = object : AnalyzerSetupTransitionProtocol.ExclusiveBoundary {
            var held = false
            var checks = 0
            override fun <T> underExclusiveOwnership(operation: () -> T): T? {
                held = true
                try { return operation() } finally { held = false }
            }
            override fun ownershipStillExclusive(): Boolean {
                checks++
                // Preflight and checkpoint are allowed, but fail before RECONFIGURE.
                return held && checks <= 3
            }
        }
        val result = AnalyzerSetupTransitionProtocol.exerciseWithBoundary(p, boundary)
        assertFalse(result.succeeded)
        assertFalse(result.rollbackSucceeded)
        assertNotEquals(AnalyzerSetupTransitionProtocol.Stage.SAFE_STOPPED, result.stage)
        assertTrue(p.log.contains("stop"))
        assertFalse(p.log.contains("apply"))
        assertFalse(p.log.contains("start"))
        assertNotNull(result.originalError)
        assertFalse(boundary.held)
    }

    @Test fun rollbackStartFailureRetainsPrimaryErrorAndVerifiesSafeStop() {
        val p = Fake().apply { verifyNew = false; failedOperation = "start"; failOnlyOnce = false }
        val result = AnalyzerSetupTransitionProtocol.exerciseWithControlledPort(p)
        assertFalse(result.succeeded)
        assertFalse(result.rollbackSucceeded)
        assertEquals(AnalyzerSetupTransitionProtocol.Stage.SAFE_STOPPED, result.stage)
        assertTrue(result.originalError?.message?.contains("fault: start") == true)
        assertTrue(result.recoveryErrors.isNotEmpty())
        assertEquals("verify-safe", p.log.last())
    }

}
