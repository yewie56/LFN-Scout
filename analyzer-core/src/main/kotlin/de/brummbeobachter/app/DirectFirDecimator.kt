package de.brummbeobachter.app

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Analyzer-only normalized PCM16 -> Double FIR; independently stateful per audio session.
 * Integer factors 2..12, full-precision output and DC-normalized coefficients.
 */
class DirectFirDecimator(val inputRateHz: Int, val factor: Int) {
    init { require(DirectAnalyzerConfig.valid(inputRateHz, factor) && factor in 2..12) }
    val outputRateHz: Int = inputRateHz / factor
    private val taps = factor * 64 + 1
    private val cutoff = 0.42 / factor // fraction of input sample rate, below output Nyquist 0.5/factor
    private val kernel = DoubleArray(taps) { k ->
        val n = k - (taps-1)/2
        val ideal = if (n == 0) 2 * cutoff else sin(2 * PI * cutoff * n) / (PI * n)
        ideal * (0.42 - 0.5 * cos(2*PI*k/(taps-1)) + 0.08*cos(4*PI*k/(taps-1)))
    }.also { array ->
        val sum = array.sum()
        for (i in array.indices) array[i] /= sum
    }
    private val ring = DoubleArray(taps)
    private var cursor = 0
    private var samples = 0L
    fun reset() { ring.fill(0.0); cursor=0; samples=0 }
    fun accept(input: ShortArray): DoubleArray {
        val out=DoubleArray(input.size/factor+2)
        var produced=0
        for (s in input) {
            ring[cursor]=s/32768.0
            cursor=(cursor+1)%taps
            samples++
            if(samples >= taps && samples % factor == 0L) {
                var y=0.0
                for(k in kernel.indices) y+=kernel[k]*ring[(cursor-1-k+taps)%taps]
                out[produced++]=y
            }
        }
        return out.copyOf(produced)
    }
}
