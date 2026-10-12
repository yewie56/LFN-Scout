package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeveloperAudioSettingsContractTest {

    @Test
    fun daily_limit_blocks_when_count_reaches_limit() {
        assertTrue(AudioRecordingPolicy.canRecordToday(9, 10))
        assertFalse(AudioRecordingPolicy.canRecordToday(10, 10))
    }

    @Test
    fun alarm_volume_is_clamped_to_percent_range() {
        assertEquals(0, AudioRecordingPolicy.normalizeAlarmVolumePercent(-1))
        assertEquals(70, AudioRecordingPolicy.normalizeAlarmVolumePercent(70))
        assertEquals(100, AudioRecordingPolicy.normalizeAlarmVolumePercent(120))
    }
}
