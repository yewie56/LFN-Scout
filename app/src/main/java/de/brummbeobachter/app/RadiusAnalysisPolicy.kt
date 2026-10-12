package de.brummbeobachter.app

object RadiusAnalysisPolicy {

    const val DEFAULT_RADIUS_KM = 600
    const val MIN_RADIUS_KM = 1
    const val MAX_RADIUS_KM = 20_000

    fun normalizeRadiusKm(value: Int): Int =
        value.coerceIn(
            MIN_RADIUS_KM,
            MAX_RADIUS_KM
        )
}
