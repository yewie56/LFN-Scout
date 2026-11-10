package de.brummbeobachter.app

import kotlin.math.exp

/** Direct-Analyzer MON output policy. Never returns a gain above unity. */
internal object MonAgcPolicy {
    fun gain(referenceRms: Double, observedRms: Double): Double {
        if (!referenceRms.isFinite() || !observedRms.isFinite() || referenceRms <= 0.0) return 1.0
        return (referenceRms / observedRms.coerceAtLeast(1e-12)).coerceIn(0.0, 1.0)
    }

    /** Immediate attenuation on a rise; smoothly release when the source gets quieter. */
    fun nextGain(previous: Double, target: Double, blockSeconds: Double, releaseMs: Double): Double {
        val safeTarget = target.coerceIn(0.0, 1.0)
        if (safeTarget <= previous) return safeTarget
        val coeff = exp(-blockSeconds.coerceAtLeast(0.0) * 1000.0 / releaseMs)
        return (coeff * previous + (1.0 - coeff) * safeTarget).coerceIn(0.0, 1.0)
    }
}
