package de.brummbeobachter.app

import kotlin.math.pow

/** One canonical conversion from post-digital-gain values back to the pre-gain reference.
 * Apply exactly once and only when the analyzed samples/levels actually include that gain.
 */
object GainReferencePolicy {
    fun amplitudeFactor(gainDb: Double): Double {
        require(gainDb.isFinite() && gainDb in -100.0..100.0)
        return 10.0.pow(-gainDb / 20.0)
    }

    fun compensateDb(measuredDb: Double, gainDb: Double): Double {
        require(measuredDb.isFinite())
        require(gainDb.isFinite() && gainDb in -100.0..100.0)
        return measuredDb - gainDb
    }
}
