package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AudioInputSelectionPolicyTest {

    @Test
    fun auto_prefers_unprocessed() {
        assertEquals(
            listOf(AudioSourceMode.UNPROCESSED, AudioSourceMode.MIC),
            AudioInputSelectionPolicy.sourceOrder(AudioSourceMode.AUTO)
        )
    }

    @Test
    fun forced_unprocessed_has_no_mic_fallback() {
        assertEquals(
            listOf(AudioSourceMode.UNPROCESSED),
            AudioInputSelectionPolicy.sourceOrder(AudioSourceMode.UNPROCESSED)
        )
    }

    @Test
    fun missing_saved_device_falls_back_to_auto() {
        assertEquals(
            AudioInputSelectionPolicy.AUTO_DEVICE_ID,
            AudioInputSelectionPolicy.normalizeDeviceId(
                selectedDeviceId = 17,
                availableDeviceIds = setOf(2, 3, 4)
            )
        )
    }

    @Test
    fun existing_saved_device_is_retained() {
        assertEquals(
            3,
            AudioInputSelectionPolicy.normalizeDeviceId(
                selectedDeviceId = 3,
                availableDeviceIds = setOf(2, 3, 4)
            )
        )
    }
}
