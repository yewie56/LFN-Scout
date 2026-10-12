package de.brummbeobachter.app

object CommentAutoSendPolicy {
    const val MIN_TIMEOUT_SECONDS = 1
    const val MAX_TIMEOUT_SECONDS = 300

    const val DEFAULT_RATING_NO_COMMENT_TIMEOUT_SECONDS = 5
    const val DEFAULT_RATING_COMMENT_TIMEOUT_SECONDS = 5
    const val DEFAULT_AUDIO_NO_COMMENT_TIMEOUT_SECONDS = 60
    const val DEFAULT_AUDIO_COMMENT_TIMEOUT_SECONDS = 10

    fun normalizeSeconds(value: Int): Int =
        value.coerceIn(MIN_TIMEOUT_SECONDS, MAX_TIMEOUT_SECONDS)

    fun toMillis(seconds: Int): Long =
        normalizeSeconds(seconds) * 1_000L
}
