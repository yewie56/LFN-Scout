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

    @Test fun independentRollingSessionsMayResetSampleCounter() {
        val report = PersistentSystemTest.evaluate(listOf(
            "AUDIO_DIAG_ROLLING_RECORDING_STARTED sid=a",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT sid=a samples=120832",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT sid=a samples=241664",
            "AUDIO_DIAG_ROLLING_STOP_REQUEST sid=a",
            "AUDIO_DIAG_ROLLING_CLEANUP_DONE sid=a",
            "AUDIO_DIAG_ROLLING_RECORDING_STARTED sid=b",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT sid=b samples=120832",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT sid=b samples=241664"
        ))
        assertEquals("PASS", report.results.first { it.id == "rolling_counter_monotonic" }.status)
        assertEquals("PASS", report.results.first { it.id == "rolling_lifecycle" }.status)
        assertTrue(report.results.first { it.id == "rolling_counter_monotonic" }.detail.contains("2 sessions in source log"))
    }
    @Test fun genuineCounterRollbackWithinLatestSessionFails() {
        val report = PersistentSystemTest.evaluate(listOf(
            "AUDIO_DIAG_ROLLING_RECORDING_STARTED sid=old",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT samples=50000",
            "AUDIO_DIAG_ROLLING_RECORDING_STARTED sid=new",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT samples=120832",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT samples=2000"
        ))
        assertEquals("FAIL", report.results.first { it.id == "rolling_counter_monotonic" }.status)
    }
    @Test fun historicalAnalyzerFailuresAreNotAttributedToNewSession() {
        val report = PersistentSystemTest.evaluate(listOf(
            "AUDIO_DIAG_ROLLING_RECORDING_STARTED sid=old",
            "ANALYZER_RESTART_FAILED",
            "AUDIO_DIAG_ROLLING_RECORDING_STARTED sid=new",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT samples=120832",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT samples=241664"
        ))
        assertEquals("SKIP", report.results.first { it.id == "unhandled_crash_observed" }.status)
        assertEquals("PASS", report.results.first { it.id == "rolling_counter_monotonic" }.status)
    }
    @Test fun noEventsNeverPretendToPass() {
        val report = PersistentSystemTest.evaluate(emptyList())
        assertEquals(0, report.passed)
        assertEquals(0, report.failed)
        assertEquals(report.results.size, report.skipped)
        assertTrue(report.json().contains("LATEST_ROLLING_SESSION_LOG_REPLAY"))
    }
    @Test fun threeAnalyzerCyclesAndHeartbeatsWithinSingleWindowPass() {
        val report = PersistentSystemTest.evaluate(listOf(
            "SYSTEMTEST_BEGIN id=z",
            "AUDIO_DIAG_RESTART_REQUEST",
            "AUDIO_DIAG_RESTART_DONE",
            "AUDIO_DIAG_RESTART_REQUEST",
            "AUDIO_DIAG_RESTART_DONE",
            "AUDIO_DIAG_RESTART_REQUEST",
            "AUDIO_DIAG_RESTART_DONE",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT samples=120832",
            "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT samples=241664"
        ))
        assertEquals("PASS", report.results.first { it.id == "rolling_restart_vs_analyzer" }.status)
        assertEquals("PASS", report.results.first { it.id == "rolling_counter_monotonic" }.status)
    }
    @Test fun missingCompletedRestartFails() {
        val report = PersistentSystemTest.evaluate(listOf(
            "AUDIO_DIAG_RESTART_REQUEST",
            "AUDIO_DIAG_RESTART_DONE",
            "AUDIO_DIAG_RESTART_REQUEST"
        ))
        assertEquals("FAIL", report.results.first { it.id == "rolling_restart_vs_analyzer" }.status)
    }
}
