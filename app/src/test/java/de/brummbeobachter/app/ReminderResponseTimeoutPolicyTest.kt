package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderResponseTimeoutPolicyTest {

    @Test
    fun timeout_is_at_end_of_five_minute_response_window() {
        assertEquals(
            1_300_000L,
            ReminderResponseTimeoutPolicy.timeoutTriggerEpochMs(
                1_000_000L,
                5
            )
        )
    }

    @Test
    fun timeout_is_at_end_of_nine_minute_response_window() {
        assertEquals(
            1_540_000L,
            ReminderResponseTimeoutPolicy.timeoutTriggerEpochMs(
                1_000_000L,
                9
            )
        )
    }
}
