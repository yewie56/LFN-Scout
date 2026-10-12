package de.brummbeobachter.app

import kotlin.math.pow

object AudioRecordingPolicy {
    const val SOURCE_SAMPLE_RATE_HZ = 8000
    const val TARGET_SAMPLE_RATE_HZ = 1000
    const val DURATION_SECONDS = 120
    const val COUNTDOWN_SECONDS = 15
    const val DEFAULT_GAIN_DB = 0
    const val MAX_GAIN_DB = 50
    const val DEFAULT_DAILY_LIMIT = 10
    const val MAX_DAILY_LIMIT = 100
    const val DEFAULT_ALARM_VOLUME_PERCENT = 70

    fun normalizeGainDb(value: Int): Int =
        value.coerceIn(
            0,
            MAX_GAIN_DB
        )

    fun normalizeDailyLimit(value: Int): Int =
        value.coerceIn(
            1,
            MAX_DAILY_LIMIT
        )

    fun normalizeAlarmVolumePercent(value: Int): Int =
        value.coerceIn(
            0,
            100
        )

    fun canRecordToday(
        countToday: Int,
        dailyLimit: Int
    ): Boolean =
        countToday <
            normalizeDailyLimit(dailyLimit)

    fun gainFactor(gainDb: Int): Double =
        10.0.pow(
            normalizeGainDb(gainDb) / 20.0
        )
}

object DeveloperModePolicy {

    fun expectedPassword(key: String): String? {
        val fourCharacterGroups =
            key
                .trim()
                .split("-")
                .map(String::trim)
                .filter { it.length == 4 }

        return fourCharacterGroups
            .getOrNull(2)
            ?.reversed()
    }

    fun passwordMatches(
        key: String,
        enteredPassword: String
    ): Boolean =
        expectedPassword(key)
            ?.let {
                enteredPassword.trim().uppercase() == it.uppercase()
            }
            ?: false
}


data class AudioClipState(
    val clipped: Boolean = false,
    val clippedSamples: Int = 0
) {
    fun withClippedSamples(
        currentClippedSamples: Int
    ): AudioClipState {
        val normalized =
            currentClippedSamples.coerceAtLeast(0)

        return if (clipped) {
            this
        } else if (normalized > 0) {
            AudioClipState(
                clipped = true,
                clippedSamples = normalized
            )
        } else {
            this
        }
    }
}
