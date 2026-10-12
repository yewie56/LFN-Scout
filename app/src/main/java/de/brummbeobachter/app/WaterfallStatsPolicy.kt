package de.brummbeobachter.app

data class WaterfallStatsRange(
    val minHz: Double,
    val maxHz: Double
)

object WaterfallStatsPolicy {
    const val DEFAULT_MIN_HZ = 2.0
    const val DEFAULT_MAX_HZ = 25.0
    const val ABSOLUTE_MIN_HZ = 0.0
    const val ABSOLUTE_MAX_HZ = 140.0

    fun effectiveRange(
        developerModeEnabled: Boolean,
        storedMinHz: Int,
        storedMaxHz: Int
    ): WaterfallStatsRange {
        if (!developerModeEnabled) {
            return WaterfallStatsRange(
                DEFAULT_MIN_HZ,
                DEFAULT_MAX_HZ
            )
        }

        val min = storedMinHz
            .coerceIn(
                ABSOLUTE_MIN_HZ.toInt(),
                (ABSOLUTE_MAX_HZ - 1.0).toInt()
            )
        val max = storedMaxHz
            .coerceIn(
                (ABSOLUTE_MIN_HZ + 1.0).toInt(),
                ABSOLUTE_MAX_HZ.toInt()
            )

        return if (min < max) {
            WaterfallStatsRange(min.toDouble(), max.toDouble())
        } else {
            WaterfallStatsRange(DEFAULT_MIN_HZ, DEFAULT_MAX_HZ)
        }
    }
}
