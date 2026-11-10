package de.brummbeobachter.app

import kotlin.math.*

/** Direct-Analyzer MON output policy. Never returns a gain above unity. */
internal object MonAgcPolicy {
    /** Stateful first-order frequency weighting: low frequency ~ unity, high frequency
     * ~ boost. This is an adjustable approximation, NOT an ISO loudness standard.
     * It is a measurement branch only, never changes the sound path.
     */
    class WeightedMeter(private val sampleRate: Double) {
        private var previousInput = 0.0
        private var previousHigh = 0.0
        fun reset() { previousInput = 0.0; previousHigh = 0.0 }
        fun measure(samples: DoubleArray, cutoffHz: Double, boostDb: Double): Double {
            if (samples.isEmpty()) return 0.0
            val corner = cutoffHz.coerceIn(1.0, sampleRate * 0.45)
            val rc = 1.0 / (2.0 * PI * corner)
            val alpha = rc / (rc + 1.0 / sampleRate)
            val highBoost = 10.0.pow(boostDb.coerceIn(0.0, 30.0) / 20.0) - 1.0
            var sum = 0.0
            for (x in samples) {
                val high = alpha * (previousHigh + x - previousInput)
                previousInput = x
                previousHigh = high
                val weighted = x + highBoost * high
                sum += weighted * weighted
            }
            return sqrt(sum / samples.size)
        }
    }

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
