package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioRecordingPolicyTest {

    @Test
    fun developer_password_uses_third_four_character_group_reversed() {
        assertEquals(
            "XNRY",
            DeveloperModePolicy.expectedPassword(
                "BB-PP8J-MYEN-YRNX-JHWX"
            )
        )
    }

    @Test
    fun invalid_key_has_no_developer_password() {
        assertNull(
            DeveloperModePolicy.expectedPassword(
                "BB-1234-5678"
            )
        )
    }

    @Test
    fun gain_is_clamped_to_zero_through_fifty_db() {
        assertEquals(0, AudioRecordingPolicy.normalizeGainDb(-4))
        assertEquals(17, AudioRecordingPolicy.normalizeGainDb(17))
        assertEquals(50, AudioRecordingPolicy.normalizeGainDb(99))
    }

    @Test
    fun clip_latch_stays_set_after_first_clip() {
        var state = AudioClipState()
        state = state.withClippedSamples(0)
        assertEquals(false, state.clipped)

        state = state.withClippedSamples(3)
        assertEquals(true, state.clipped)
        assertEquals(3, state.clippedSamples)

        state = state.withClippedSamples(0)
        assertEquals(true, state.clipped)
        assertEquals(3, state.clippedSamples)
    }

    @Test
    fun daily_limit_defaults_to_ten_and_is_positive() {
        assertEquals(10, AudioRecordingPolicy.DEFAULT_DAILY_LIMIT)
        assertEquals(1, AudioRecordingPolicy.normalizeDailyLimit(0))
        assertTrue(AudioRecordingPolicy.normalizeDailyLimit(500) <= 100)
    }

    @Test
    fun target_audio_format_is_fixed() {
        assertEquals(1000, AudioRecordingPolicy.TARGET_SAMPLE_RATE_HZ)
        assertEquals(120, AudioRecordingPolicy.DURATION_SECONDS)
        assertEquals(15, AudioRecordingPolicy.COUNTDOWN_SECONDS)
    }

    @Test
    fun v0428_layout_is_tighter_than_v0427() {
        assertEquals(7, FaceliftLayoutPolicy.SCREEN_PADDING_DP)
        assertEquals(3, FaceliftLayoutPolicy.SECTION_GAP_DP)
        assertEquals(7, FaceliftLayoutPolicy.CARD_PADDING_DP)
        assertEquals(2, FaceliftLayoutPolicy.FIELD_GAP_DP)
    }
}
