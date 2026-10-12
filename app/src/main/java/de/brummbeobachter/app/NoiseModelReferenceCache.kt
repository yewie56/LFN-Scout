package de.brummbeobachter.app

/** Identity-keyed cache for an immutable measured noise profile.
 * The owner must clear the cache if it changes the contents of the source array in place.
 */
internal class NoiseModelReferenceCache {
    private var source: DoubleArray? = null
    private var rate = 0
    private var fft = 0
    private var cached: DoubleArray? = null
    private var valid = false

    fun clear() {
        source = null
        cached = null
        valid = false
    }

    fun get(reference: DoubleArray, sampleRate: Int, fftSize: Int,
            calculate: () -> DoubleArray?): Pair<DoubleArray?, Boolean> {
        if (valid && source === reference && rate == sampleRate && fft == fftSize)
            return cached to true
        val value = calculate()
        source = reference
        rate = sampleRate
        fft = fftSize
        cached = value
        valid = true
        return value to false
    }
}
