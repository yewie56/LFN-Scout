package de.brummbeobachter.app

import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

/** Immutable, JVM/standalone-reusable diagnostic settings. Only alters the detector. */
data class ImpulseSensitivity(
    val gainDb: Int = 0,
    val minPeakPcm: Int = 1400,
    val noiseMultiplier: Double = 6.0
) {
    init {
        require(gainDb in 0..50)
        require(minPeakPcm in 1..32767)
        require(noiseMultiplier.isFinite() && noiseMultiplier in 1.5..12.0)
    }
    fun fixedThresholdRawPcm(): Int =
        max(1, (minPeakPcm / 10.0.pow(gainDb / 20.0)).roundToInt())
}

/** Profiles do not silently modify the independent gain setting. */
enum class ImpulseSensitivityPreset(val label: String, val minPeakPcm: Int, val noiseMultiplier: Double) {
    ROBUST("Robust", 1400, 6.0),
    NORMAL("Mittel", 700, 4.0),
    SENSITIVE("Empfindlich", 250, 2.5);

    fun withGain(gainDb: Int): ImpulseSensitivity = ImpulseSensitivity(gainDb, minPeakPcm, noiseMultiplier)
}
