package de.brummbeobachter.app

/** Separate display algorithm for the 8-kHz filterbank only. The existing RMS values
 * and original FAST/SLOW DSP states are untouched. Signal gain remains 0 dB.
 * The input excess is supplied by FastFilterBank; it is a relative dB quantity,
 * never an acoustic dB SPL measurement.
 */
internal object FilterImpulseExcessPolicy {
    fun displayExcess(excessDb: Double, thresholdDb: Int): Double {
        require(thresholdDb in 0..20)
        return if (excessDb.isFinite()) (excessDb - thresholdDb).coerceIn(0.0,20.0) else 0.0
    }
}
