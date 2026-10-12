package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderResponseTimeoutContractTest {

    @Test
    fun response_timeout_action_name_is_stable() {
        assertEquals(
            "de.brummbeobachter.app.action.RESPONSE_WINDOW_TIMEOUT",
            ReminderAlarmReceiver.ACTION_RESPONSE_WINDOW_TIMEOUT
        )
    }
}
