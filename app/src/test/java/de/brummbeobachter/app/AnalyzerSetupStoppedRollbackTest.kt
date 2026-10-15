package de.brummbeobachter.app

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/** Exercises the transaction mechanics without starting Android audio services. */
class AnalyzerSetupStoppedRollbackTest {
    private class FakePort : AnalyzerSetupSwitchTransaction.Port {
        override val transitionLock = Any()
        override val supportsAtomicStoppedApply = true
        var current = 1
        var applyThrows = false
        var verifyOk = true
        var restoreThrows = false
        var restoreCalls = 0
        var applyCalls = 0
        var checkpointCalls = 0
        override fun status() = AnalyzerSetupSwitchTransaction.Status(false, false, false, false, false)
        override fun saved() = JSONObject().put("gain", 5)
        override fun live() = JSONObject().put("gain", current)
        override fun checkpoint(): Any { checkpointCalls++; return current }
        override fun applyStoppedDraft(saved: JSONObject) {
            applyCalls++
            current = saved.getInt("gain")
            if (applyThrows) throw IllegalStateException("apply failure")
        }
        override fun restoreStopped(checkpoint: Any) {
            restoreCalls++
            if (restoreThrows) throw IllegalStateException("restore failure")
            current = checkpoint as Int
        }
        override fun verifyRestored(checkpoint: Any) = current == checkpoint as Int
        override fun verifyApplied(saved: JSONObject) = verifyOk && current == saved.getInt("gain")
    }

    @Test fun successAppliesAndVerifiesWithoutRestore() {
        val p = FakePort()
        assertEquals(AnalyzerSetupSwitchTransaction.Outcome.Applied,
            AnalyzerSetupSwitchTransaction.applyWithRollback(p, p.saved()) { true })
        assertEquals(5, p.current)
        assertEquals(0, p.restoreCalls)
    }

    @Test fun changedStateNeverCallsApply() {
        val p = FakePort()
        assertTrue(AnalyzerSetupSwitchTransaction.applyWithRollback(p, p.saved()) { false }
            is AnalyzerSetupSwitchTransaction.Outcome.Rejected)
        assertEquals(0, p.applyCalls)
    }

    @Test fun applyErrorRestoresCheckpoint() {
        val p = FakePort().apply { applyThrows = true }
        val result = AnalyzerSetupSwitchTransaction.applyWithRollback(p, p.saved()) { true }
        assertTrue(result is AnalyzerSetupSwitchTransaction.Outcome.Failed)
        assertTrue((result as AnalyzerSetupSwitchTransaction.Outcome.Failed).rollbackSucceeded)
        assertEquals(1, p.current)
    }

    @Test fun verificationErrorRestoresCheckpoint() {
        val p = FakePort().apply { verifyOk = false }
        val result = AnalyzerSetupSwitchTransaction.applyWithRollback(p, p.saved()) { true }
        assertTrue(result is AnalyzerSetupSwitchTransaction.Outcome.Failed)
        assertEquals(1, p.current)
    }

    @Test fun restoreFailureIsReportedNotHidden() {
        val p = FakePort().apply { applyThrows = true; restoreThrows = true }
        val result = AnalyzerSetupSwitchTransaction.applyWithRollback(p, p.saved()) { true }
        assertTrue(result is AnalyzerSetupSwitchTransaction.Outcome.Failed)
        assertFalse((result as AnalyzerSetupSwitchTransaction.Outcome.Failed).rollbackSucceeded)
    }

    @Test fun checkpointErrorDoesNotAttemptApply() {
        val p = object : AnalyzerSetupSwitchTransaction.Port by FakePort() {
            override fun checkpoint(): Any = error("checkpoint failure")
        }
        val result = AnalyzerSetupSwitchTransaction.applyWithRollback(p, p.saved()) { true }
        assertTrue(result is AnalyzerSetupSwitchTransaction.Outcome.Rejected)
    }
}
