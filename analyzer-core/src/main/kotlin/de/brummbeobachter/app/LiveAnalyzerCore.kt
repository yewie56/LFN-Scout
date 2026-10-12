package de.brummbeobachter.app

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt

/** FFT window selection, coherent gain is recalculated for each selected window. */
enum class AnalyzerWindow { HANN, HAMMING, BLACKMAN, RECTANGULAR, COSINE, BLACKMAN_HARRIS, FLAT_TOP, TRIANGULAR, WELCH }

/**
 * Standalone-testable DSP core. No Android, storage, microphone ownership or network API.
 * The caller must provide an actual PCM stream; this class NEVER opens a microphone.
 * Samples must come from one documented source/rate and in chronological order.
 * The incoming samples are analysed as supplied: amplified recorder PCM is not raw SPL.
 */
class LiveAnalyzerCore(
    val sampleRateHz: Int,
    val fftSize: Int = 4096,
    val hopSize: Int = 512,
    val maxFrequencyHz: Double = 100.0,
    val averageTimeSeconds: Double = 5.0,
    val windowType: AnalyzerWindow = AnalyzerWindow.HANN,
    val smoothingAlpha: Double? = null,
    val subtractDc: Boolean = true
) {
    init {
        require(sampleRateHz > 0)
        require(fftSize in 64..32768 && (fftSize and (fftSize - 1)) == 0)
        require(hopSize > 0)
        require(maxFrequencyHz > 0.0 && maxFrequencyHz <= sampleRateHz / 2.0)
        require(averageTimeSeconds >= 0.0 && averageTimeSeconds.isFinite())
        require(smoothingAlpha == null || (smoothingAlpha.isFinite() && smoothingAlpha in 0.0..1.0))
    }

    data class Spectrum(
        val sequence: Long,
        val inputSampleIndex: Long,
        val sampleRateHz: Int,
        val fftSize: Int,
        val frequenciesHz: DoubleArray,
        val liveDbFs: DoubleArray,
        val averageDbFs: DoubleArray,
        val maxHoldDbFs: DoubleArray,
        val inputClipped: Boolean
    )

    private val ring = DoubleArray(fftSize)
    private val window = DoubleArray(fftSize) { i ->
        val phase = 2.0 * PI * i / (fftSize - 1)
        when (windowType) {
            AnalyzerWindow.HANN -> 0.5 - 0.5 * cos(phase)
            AnalyzerWindow.HAMMING -> 0.54 - 0.46 * cos(phase)
            AnalyzerWindow.BLACKMAN -> 0.42 - 0.5 * cos(phase) + 0.08 * cos(2.0 * phase)
            AnalyzerWindow.RECTANGULAR -> 1.0
            AnalyzerWindow.COSINE -> kotlin.math.sin(PI * i / (fftSize-1))
            AnalyzerWindow.BLACKMAN_HARRIS -> 0.35875 - 0.48829*cos(phase) + 0.14128*cos(2*phase) - 0.01168*cos(3*phase)
            AnalyzerWindow.FLAT_TOP -> 1.0 - 1.93*cos(phase) + 1.29*cos(2*phase) - 0.388*cos(3*phase) + 0.0322*cos(4*phase)
            AnalyzerWindow.TRIANGULAR -> 1.0 - kotlin.math.abs((i - (fftSize-1)/2.0) / ((fftSize+1)/2.0))
            AnalyzerWindow.WELCH -> 1.0 - ((i - (fftSize-1)/2.0) / ((fftSize-1)/2.0)).let { it*it }
        }
    }
    private val coherentGain = window.sum() / fftSize
    private val bins = (maxFrequencyHz * fftSize / sampleRateHz).toInt().coerceAtMost(fftSize / 2) + 1
    private val frequencyAxis = DoubleArray(bins) { it.toDouble() * sampleRateHz / fftSize }
    private val averagePower = DoubleArray(bins)
    private val maxHoldPower = DoubleArray(bins)
    private var writeIndex = 0
    private var sampleCount = 0L
    private var sequence = 0L
    private var sinceLastFrame = 0
    private var initialized = false
    private var clippedSinceFrame = false
    private val alpha = smoothingAlpha ?: (if (averageTimeSeconds == 0.0) 1.0 else 1.0 - kotlin.math.exp(-hopSize.toDouble() / sampleRateHz / averageTimeSeconds))

    val frequencyResolutionHz: Double get() = sampleRateHz.toDouble() / fftSize

    /** Legacy PCM16 input, retained for existing recorder and regression fixtures. */
    @Synchronized
    fun accept(samples: ShortArray, offset: Int = 0, length: Int = samples.size - offset): List<Spectrum> {
        require(offset >= 0 && length >= 0 && offset <= samples.size - length)
        val out = ArrayList<Spectrum>()
        for (i in offset until offset + length) feedSample(samples[i] / 32768.0, out)
        return out
    }

    /** Full-precision normalized PCM, avoids an additional 16-bit quantization after LF FIR. */
    @Synchronized
    fun acceptNormalized(samples: DoubleArray): List<Spectrum> {
        val out = ArrayList<Spectrum>()
        for (sample in samples) {
            require(sample.isFinite()) { "Non-finite PCM sample" }
            feedSample(sample, out)
        }
        return out
    }

    private fun feedSample(sample: Double, out: MutableList<Spectrum>) {
        ring[writeIndex] = sample
        writeIndex = (writeIndex + 1) % fftSize
        sampleCount++
        sinceLastFrame++
        if (kotlin.math.abs(sample) >= 1.0) clippedSinceFrame = true
        if (sampleCount >= fftSize && sinceLastFrame >= hopSize) {
            sinceLastFrame = 0
            out.add(calculateSpectrum())
            clippedSinceFrame = false
        }
    }

    /** Clears just the exponential average; preserves ring, FFT cadence and source continuity. */
    @Synchronized
    fun resetAveraging() {
        averagePower.fill(0.0)
        initialized = false
    }

    @Synchronized
    fun resetMaxHold() {
        maxHoldPower.fill(0.0)
    }

    @Synchronized
    fun reset() {
        ring.fill(0.0)
        averagePower.fill(0.0)
        maxHoldPower.fill(0.0)
        writeIndex = 0
        sampleCount = 0
        sequence = 0
        sinceLastFrame = 0
        clippedSinceFrame = false
        initialized = false
    }

    private fun calculateSpectrum(): Spectrum {
        val re = DoubleArray(fftSize)
        val im = DoubleArray(fftSize)
        var dc = 0.0
        for (i in 0 until fftSize) dc += ring[i]
        dc /= fftSize
        for (i in 0 until fftSize) {
            re[i] = (ring[(writeIndex + i) % fftSize] - if (subtractDc) dc else 0.0) * window[i]
        }
        fft(re, im)
        val live = DoubleArray(bins)
        val avg = DoubleArray(bins)
        val max = DoubleArray(bins)
        for (bin in 0 until bins) {
            // The DC/Nyquist bins have no mirror, all other bins have an amplitude factor of two.
            val singleSided = if (bin == 0 || bin == fftSize / 2) 1.0 else 2.0
            val amplitude = hypot(re[bin], im[bin]) * singleSided / (fftSize * coherentGain)
            val power = amplitude * amplitude
            averagePower[bin] = if (!initialized) power else averagePower[bin] + alpha * (power - averagePower[bin])
            maxHoldPower[bin] = maxOf(maxHoldPower[bin], power)
            live[bin] = powerDb(power)
            avg[bin] = powerDb(averagePower[bin])
            max[bin] = powerDb(maxHoldPower[bin])
        }
        initialized = true
        return Spectrum(++sequence, sampleCount, sampleRateHz, fftSize,
            frequencyAxis.copyOf(), live, avg, max, clippedSinceFrame)
    }

    private fun powerDb(power: Double): Double = 10.0 * log10(power.coerceAtLeast(1.0e-24))

    /** In-place radix-2 FFT, without Android or third-party dependencies. */
    private fun fft(real: DoubleArray, imaginary: DoubleArray) {
        val n = real.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
            j = j xor bit
            if (i < j) {
                val r = real[i]; real[i] = real[j]; real[j] = r
                val im = imaginary[i]; imaginary[i] = imaginary[j]; imaginary[j] = im
            }
        }
        var len = 2
        while (len <= n) {
            val angle = -2.0 * PI / len
            val stepRe = cos(angle)
            val stepIm = sin(angle)
            var start = 0
            while (start < n) {
                var wr = 1.0
                var wi = 0.0
                for (k in 0 until len / 2) {
                    val even = start + k
                    val odd = even + len / 2
                    val tr = wr * real[odd] - wi * imaginary[odd]
                    val ti = wr * imaginary[odd] + wi * real[odd]
                    real[odd] = real[even] - tr
                    imaginary[odd] = imaginary[even] - ti
                    real[even] += tr
                    imaginary[even] += ti
                    val nextWr = wr * stepRe - wi * stepIm
                    wi = wr * stepIm + wi * stepRe
                    wr = nextWr
                }
                start += len
            }
            len = len shl 1
        }
    }
}
