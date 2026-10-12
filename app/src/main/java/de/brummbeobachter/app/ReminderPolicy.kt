package de.brummbeobachter.app

import kotlin.math.roundToInt

object ReminderPolicy {
    const val DEFAULT_INTERVAL_MINUTES = 30
    const val MIN_INTERVAL_MINUTES = 1
    const val MAX_INTERVAL_MINUTES = 24 * 60

    fun normalizeIntervalMinutes(value: Int): Int =
        value.coerceIn(MIN_INTERVAL_MINUTES, MAX_INTERVAL_MINUTES)

    fun responseWindowMinutes(intervalMinutes: Int): Int {
        val normalized = normalizeIntervalMinutes(intervalMinutes)
        return (normalized * 0.30)
            .roundToInt()
            .coerceIn(5, 15)
    }

    /**
     * Drei Tonpakete gleichmäßig innerhalb des Antwortfensters:
     * sofort, nach 1/3 und nach 2/3. Das letzte Drittel bleibt als
     * Reaktionszeit nach dem dritten Tonpaket frei.
     */
    fun tonePackageOffsetsMillis(responseWindowMinutes: Int): List<Long> {
        val windowMs = responseWindowMinutes.coerceAtLeast(1) * 60_000L
        return listOf(
            0L,
            windowMs / 3L,
            (windowMs * 2L) / 3L
        )
    }
}

enum class ReminderPauseOption(val hours: Int?) {
    ONE_HOUR(1),
    TWO_HOURS(2),
    FOUR_HOURS(4),
    EIGHT_HOURS(8),
    TWENTY_FOUR_HOURS(24),
    SEVENTY_TWO_HOURS(72),
    OFF(null)
}
