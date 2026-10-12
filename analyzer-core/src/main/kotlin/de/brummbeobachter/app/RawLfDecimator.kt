package de.brummbeobachter.app

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Independent full-precision 8-kHz to 2-kHz/1-kHz/500-Hz low-pass/decimation.
 * Does NOT replace or touch the legacy 8:1 averaged 1-kHz WAV recording.
 * A 129-tap windowed-sinc FIR has ~8 ms group delay, state kept across blocks.
 */
class RawLfDecimator(
    val inputRateHz: Int = 8000,
    val outputRateHz: Int = 1000,
    val cutoffHz: Double = if (outputRateHz == 500) 215.0 else 280.0,
    val taps: Int = if (outputRateHz == 500) 513 else 129
) {
    init {
        require(inputRateHz == 8000 && outputRateHz in intArrayOf(2000, 1000, 500))
        require(outputRateHz > 0 && cutoffHz > 0 && cutoffHz < outputRateHz / 2.0)
        require(taps >= 17 && taps % 2 == 1)
    }
    private val factor = inputRateHz / outputRateHz
    private val kernel = DoubleArray(taps) { k ->
        val n = k - (taps - 1) / 2
        val ideal = if (n == 0) 2.0 * cutoffHz / inputRateHz else
            sin(2.0 * PI * cutoffHz / inputRateHz * n) / (PI * n)
        val window = 0.54 - 0.46 * cos(2.0 * PI * k / (taps - 1))
        ideal * window
    }.also { a ->
        val sum = a.sum()
        for (i in a.indices) a[i] /= sum
    }
    private val ring = DoubleArray(taps)
    private var cursor = 0
    private var samples = 0L

    fun reset() { ring.fill(0.0); cursor = 0; samples = 0 }

    /** Returned samples are normalized [-1,+1) DOUBLE values, NOT PCM16 shorts. */
    fun accept(input: ShortArray): DoubleArray {
        val out = DoubleArray((input.size / factor) + 2)
        var nout = 0
        for (sample in input) {
            ring[cursor] = sample / 32768.0
            cursor = (cursor + 1) % taps
            samples++
            if (samples >= taps && samples % factor == 0L) {
                var y = 0.0
                for (k in kernel.indices) {
                    val pos = (cursor - 1 - k + taps) % taps
                    y += kernel[k] * ring[pos]
                }
                out[nout++] = y
            }
        }
        return out.copyOf(nout)
    }
}
