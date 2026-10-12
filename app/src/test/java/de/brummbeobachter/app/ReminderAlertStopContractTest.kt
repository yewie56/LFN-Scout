package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderAlertStopContractTest {

    @Test
    fun reminder_action_names_are_stable() {
        assertEquals(
            "de.brummbeobachter.app.action.RATE",
            ReminderActionReceiver.ACTION_RATE
        )

        assertEquals(
            "de.brummbeobachter.app.action.PAUSE_ONE_HOUR",
            ReminderActionReceiver.ACTION_PAUSE_ONE_HOUR
        )

        assertEquals(
            "de.brummbeobachter.app.action.STOP_ALERT",
            ReminderActionReceiver.ACTION_STOP_ALERT
        )
    }
}
