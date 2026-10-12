package de.brummbeobachter.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoBackgroundSettingsTest {

    @Test
    fun auto_background_is_off_by_default() {
        assertFalse(
            ReminderSettings().autoBackgroundAfterAction
        )
    }

    @Test
    fun auto_background_can_be_enabled_in_model() {
        assertTrue(
            ReminderSettings(
                autoBackgroundAfterAction = true
            ).autoBackgroundAfterAction
        )
    }
}
