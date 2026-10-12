package de.brummbeobachter.app

/** Arithmetic mean of nonnegative linear FFT-bin powers across valid windows.
 * One invalid input window is discarded entirely, so no bin receives a different weight.
 */
internal class NoisePowerAverage {
    private var sums = DoubleArray(0)
    var acceptedWindows: Long = 0L
        private set
    var rejectedWindows: Long = 0L
        private set

    fun reset() { sums = DoubleArray(0); acceptedWindows = 0; rejectedWindows = 0 }
    fun add(powers: DoubleArray): Boolean {
        if (powers.isEmpty() || powers.any { !it.isFinite() || it < 0.0 } ||
            (sums.isNotEmpty() && sums.size != powers.size)) {
            rejectedWindows++
            return false
        }
        if (sums.isEmpty()) sums = DoubleArray(powers.size)
        for (i in powers.indices) sums[i] += powers[i]
        acceptedWindows++
        return true
    }
    fun mean(): DoubleArray? = if (acceptedWindows == 0L) null else
        DoubleArray(sums.size) { sums[it] / acceptedWindows.toDouble() }
}
