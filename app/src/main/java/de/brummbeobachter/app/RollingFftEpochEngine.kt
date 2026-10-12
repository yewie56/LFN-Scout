package de.brummbeobachter.app

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.log10
import kotlin.math.pow

/** FFT one-second bins shared by rolling overall and band CSV outputs (not the screen render loop).
 * The raw 1-kHz rolling PCM stream is consumed only once. Legacy IIR/Biquad/FFT CSV bank is not called.
 * FFT window-power normalization uses 1-s Hann energy and a one-sided PSD integral.
 */
internal class RollingFftEpochEngine(private val sampleRate: Int = 1000,
    private val onFrame: (Long, DoubleArray, DoubleArray) -> Unit) {
    init { require(sampleRate == 1000) }
    private val size = 1024
    private val buffer = DoubleArray(sampleRate)
    private var count = 0
    private var epoch = -1L
    private val stepHz = sampleRate.toDouble() / size
    val frequenciesHz = DoubleArray(size / 2 + 1) { it * stepHz }
    fun accept(sample: Short, timeMs: Long) {
        val key = Math.floorDiv(timeMs, 1000L) * 1000L
        if (epoch != key) {
            if (count >= 900) finish()
            count = 0; epoch = key
        }
        if (count < sampleRate) buffer[count++] = sample.toDouble() / 32768.0
    }
    fun stop() { if (count >= 900) finish(); count = 0; epoch = -1 }
    private fun finish() {
        val re = DoubleArray(size); val im = DoubleArray(size)
        // No DC-bin power: matches the FFT analyzer DC-subtraction concept.
        val mean = buffer.take(count).sum() / count
        var windowEnergy = 0.0
        for (i in 0 until count) {
            val w = 0.5 - 0.5 * cos(2 * PI * i / (count - 1))
            re[i] = (buffer[i] - mean) * w
            windowEnergy += w * w
        }
        var j = 0
        for (i in 1 until size) {
            var bit = size shr 1
            while ((j and bit) != 0) { j = j xor bit; bit = bit shr 1 }
            j = j xor bit
            if (i < j) { val a = re[i]; re[i] = re[j]; re[j] = a }
        }
        var length = 2
        while (length <= size) {
            val a = -2.0 * PI / length
            val wr = cos(a); val wi = sin(a)
            var start = 0
            while (start < size) {
                var cr = 1.0; var ci = 0.0
                for (k in 0 until length / 2) {
                    val i = start + k; val n = i + length / 2
                    val tr = re[n] * cr - im[n] * ci
                    val ti = re[n] * ci + im[n] * cr
                    re[n] = re[i] - tr; im[n] = im[i] - ti
                    re[i] += tr; im[i] += ti
                    val next = cr * wr - ci * wi
                    ci = cr * wi + ci * wr; cr = next
                }
                start += length
            }
            length *= 2
        }
        val powers = DoubleArray(size / 2 + 1) { i ->
            val mirror = if (i == 0 || i == size / 2) 1.0 else 2.0
            mirror * (re[i] * re[i] + im[i] * im[i]) / (size * windowEnergy)
        }
        onFrame(epoch, frequenciesHz, powers)
    }
    companion object {
        fun bandDb(frequencies: DoubleArray, powers: DoubleArray, fromHz: Double, toHz: Double): Double {
            require(fromHz < toHz)
            var total = 0.0
            for (i in frequencies.indices) {
                if (frequencies[i] >= fromHz && frequencies[i] < toHz) total += powers[i]
            }
            return 10.0 * log10(total.coerceAtLeast(1e-20))
        }
    }
}
