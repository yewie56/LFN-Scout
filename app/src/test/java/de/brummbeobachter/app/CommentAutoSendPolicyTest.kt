package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class CommentAutoSendPolicyTest {
    @Test
    fun defaults_match_release_requirements() {
        assertEquals(5, CommentAutoSendPolicy.DEFAULT_RATING_NO_COMMENT_TIMEOUT_SECONDS)
        assertEquals(5, CommentAutoSendPolicy.DEFAULT_RATING_COMMENT_TIMEOUT_SECONDS)
        assertEquals(60, CommentAutoSendPolicy.DEFAULT_AUDIO_NO_COMMENT_TIMEOUT_SECONDS)
        assertEquals(10, CommentAutoSendPolicy.DEFAULT_AUDIO_COMMENT_TIMEOUT_SECONDS)
    }

    @Test
    fun seconds_are_normalized_to_supported_range() {
        assertEquals(1, CommentAutoSendPolicy.normalizeSeconds(0))
        assertEquals(42, CommentAutoSendPolicy.normalizeSeconds(42))
        assertEquals(300, CommentAutoSendPolicy.normalizeSeconds(999))
    }

    @Test
    fun seconds_are_converted_to_milliseconds() {
        assertEquals(5_000L, CommentAutoSendPolicy.toMillis(5))
        assertEquals(60_000L, CommentAutoSendPolicy.toMillis(60))
    }
}
