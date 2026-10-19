package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistentSystemTestTest {
    @Test fun detectsCompletedRollingAnalyzerCycles() {
        val log = listOf(
            "AUDIO_DIAG_ROLLING_RECORDING_STARTED",
            "AUDIO_DIAG_BEGIN_SUCCESS",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT samples=8000",
            "AUDIO_DIAG_RESTART_REQUEST",
            "AUDIO_DIAG_RESTART_DONE",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT samples=16000"
        )
        val report = PersistentSystemTest.evaluate(log)
        assertEquals("PASS", report.results.first { it.id == "rolling_restart_vs_analyzer" }.status)
        assertEquals("PASS", report.results.first { it.id == "rolling_counter_monotonic" }.status)
        assertEquals("SKIP", report.results.first { it.id == "sample_perfect_continuity" }.status)
    }
    @Test fun detectsCounterResetAndRestartFailure() {
        val report = PersistentSystemTest.evaluate(listOf(
            "AUDIO_DIAG_RESTART_REQUEST",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT samples=12000",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT samples=20"
        ))
        assertEquals("FAIL", report.results.first { it.id == "rolling_restart_vs_analyzer" }.status)
        assertEquals("FAIL", report.results.first { it.id == "rolling_counter_monotonic" }.status)
    }
    @Test fun noEventsNeverPretendToPass() {
        val report = PersistentSystemTest.evaluate(emptyList())
        assertEquals(0, report.passed)
        assertEquals(0, report.failed)
        assertEquals(report.results.size, report.skipped)
        assertTrue(report.json().contains("NON_DESTRUCTIVE_LOG_REPLAY"))
    }
}
