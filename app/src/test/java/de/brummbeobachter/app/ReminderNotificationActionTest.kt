package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderNotificationActionTest {

    @Test
    fun pause_action_name_is_stable() {
        assertEquals(
            "de.brummbeobachter.app.action.PAUSE_ONE_HOUR",
            ReminderActionReceiver.ACTION_PAUSE_ONE_HOUR
        )
    }
}
