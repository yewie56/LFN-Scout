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
}
