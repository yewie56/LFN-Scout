package de.brummbeobachter.app

import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderFullscreenControllerPolicyTest {

    @Test
    fun direct_close_controller_exists() {
        val className =
            ReminderFullscreenController::class.java.simpleName

        assertTrue(
            className.isNotBlank()
        )
    }
}
