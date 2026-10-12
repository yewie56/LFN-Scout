package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundSelfTestResultTest {

    @Test
    fun runtime_result_has_visible_pass_and_fail_states() {
        assertTrue(
            SoundSelfTestResult.STARTED.displayText
                .contains("PASS")
        )
        assertTrue(
            SoundSelfTestResult.FAILED.displayText
                .contains("FAIL")
        )
    }

    @Test
    fun persistent_log_event_names_are_stable() {
        assertEquals(
            "SOUND_SELFTEST_STARTED",
            SoundSelfTestEvents.STARTED
        )
        assertEquals(
            "SOUND_SELFTEST_STOPPED",
            SoundSelfTestEvents.STOPPED
        )
        assertEquals(
            "SOUND_SELFTEST_FAILED",
            SoundSelfTestEvents.FAILED
        )
    }
}
