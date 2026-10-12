package de.brummbeobachter.app

import kotlin.math.log10

/** Statistical subtraction of calibrated electrical noise power; never subtract dB directly.
 * Zero residual is maintained exactly in linear-power processing.
 * Legacy display arrays are finite-valued: -180 dBFS is ONLY a graphic placeholder for
 * an unresolved zero residual, not a physical measurement at -180 dBFS.
 */
internal object NoisePowerSubtraction {
    const val DISPLAY_FLOOR_DBFS = -180.0
    fun residual(measuredPower: Double, referencePower: Double): Double {
        require(measuredPower.isFinite() && measuredPower >= 0.0)
        require(referencePower.isFinite() && referencePower >= 0.0)
        return (measuredPower - referencePower).coerceAtLeast(0.0)
    }
    fun toDisplayDb(residualPower: Double): Double {
        require(residualPower.isFinite() && residualPower >= 0.0)
        return if (residualPower == 0.0) DISPLAY_FLOOR_DBFS
        else 10.0 * log10(residualPower).coerceAtLeast(DISPLAY_FLOOR_DBFS / 10.0)
    }
}
