package de.brummbeobachter.app

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.max

/** Local, deterministic pre-gain-reference statistics for the exact saved 1-kHz PCM window.
 * This does NOT claim identity with the independent 8-kHz experimental FAST envelope.
 * Unavailable impulse/modulation and FAST-only values must be represented as null by the caller.
 */
object StatisticsWindowEngine {
    const val RATE_HZ = 1000
    const val SCHEMA = 1
    data class Configuration(val minHz: Double = 2.0, val maxHz: Double = 25.0,
                             val filterMinHz: Double = 0.0, val filterMaxHz: Double = 100.0,
                             val filterBands: Int = 10) {
        init {
            require(minHz >= 0.5 && minHz < maxHz && maxHz <= 200.0)
            require(filterMinHz >= 0.0 && filterMaxHz <= 200.0 && filterMaxHz > filterMinHz)
            require(filterBands in 2..40)
        }
    }
    data class Levels(val leq: Double, val min: Double, val max: Double, val l1: Double,
                      val l10: Double, val l50: Double, val l90: Double, val l95: Double,
                      val l99: Double, val mean: Double, val stddev: Double,
                      val crestDb: Double, val excessKurtosis: Double, val epochs: Int)
    data class Band(val index: Int, val minHz: Double, val maxHz: Double, val levels: Levels)
    data class Result(val overall: Levels, val bands: List<Band>, val clippedSamples: Int,
                      val sampleCount: Int, val gainDb: Double, val config: Configuration,
                      val frequenciesHz: DoubleArray, val meanPsd: DoubleArray,
                      val medianPsd: DoubleArray, val maxPsd: DoubleArray,
                      val dominantFrequencyHz: Double?)

    private class Biquad(val freq: Double, rate: Double = RATE_HZ.toDouble(),
                         val high: Boolean) {
        private val w = 2.0 * PI * freq / rate
        private val c = cos(w)
        private val a = sin(w) / (2.0 * 0.7071067811865476)
        private val den = 1.0 + a
        private val b0 = (1.0 + if (high) c else -c) / 2.0 / den
        private val b1 = (if (high) -(1.0 + c) else (1.0 - c)) / den
        private val b2 = b0
        private val a1 = -2.0 * c / den
        private val a2 = (1.0 - a) / den
        private var z1 = 0.0
        private var z2 = 0.0
        fun sample(x: Double): Double {
            val y = b0 * x + z1
            z1 = b1 * x - a1 * y + z2
            z2 = b2 * x - a2 * y
            return y
        }
    }
    private class Bandpass(low: Double, high: Double) {
        private val hp = Biquad(max(0.5, low), high = true)
        private val lp = Biquad(high.coerceAtMost(200.0), high = false)
        fun sample(x: Double) = lp.sample(hp.sample(x))
    }
    private class Accumulator {
        val epoches = ArrayList<Double>()
        var energy = 0.0
        var peak = 0.0
        var second = 0.0
        var fourth = 0.0
        var epochPower = 0.0
        var epochCount = 0
        var count = 0
        fun accept(sample: Double) {
            val sq = sample * sample
            energy += sq
            second += sq
            fourth += sq * sq
            peak = max(peak, kotlin.math.abs(sample))
            epochPower += sq
            epochCount++
            count++
            if (epochCount == RATE_HZ) {
                epoches.add(10.0 * log10((epochPower / RATE_HZ).coerceAtLeast(1e-24)))
                epochCount = 0
                epochPower = 0.0
            }
        }
        fun result(): Levels {
            if (epochCount > 0) epoches.add(10.0 * log10((epochPower / epochCount).coerceAtLeast(1e-24)))
            require(count > 0)
            val sorted = epoches.sorted()
            // Lx: exceedance x% -> (100-x)th percentile, linearly interpolated.
            fun percentile(x: Double): Double {
                val position = (1.0 - x / 100.0) * (sorted.size - 1)
                val a = position.toInt()
                val fraction = position - a
                return sorted[a] * (1.0 - fraction) + sorted[minOf(a + 1, sorted.lastIndex)] * fraction
            }
            val mean = epoches.average()
            val spread = sqrt(epoches.sumOf { (it - mean).pow(2) } / epoches.size)
            val rmsPower = energy / count
            val kurtosis = if (second > 1e-24) (fourth / count) / (second / count).pow(2) - 3.0 else 0.0
            return Levels(10.0 * log10(rmsPower.coerceAtLeast(1e-24)), sorted.first(), sorted.last(),
                percentile(1.0), percentile(10.0), percentile(50.0), percentile(90.0),
                percentile(95.0), percentile(99.0), mean, spread,
                20.0 * log10((peak / sqrt(rmsPower.coerceAtLeast(1e-24))).coerceAtLeast(1e-12)),
                kurtosis, epoches.size)
        }
    }
    private data class CorrectedPcm(val samples: DoubleArray, val clippedSamples: Int)

    private fun correctedPcm(pcm: ShortArray, gainDb: Double): CorrectedPcm {
        require(pcm.size >= RATE_HZ && pcm.size % RATE_HZ == 0) { "Vollständige ganze Aufnahme-Sekunden erforderlich" }
        require(gainDb.isFinite() && gainDb in -100.0..100.0)
        val correction = GainReferencePolicy.amplitudeFactor(gainDb) / 32768.0
        var clipped = 0
        val normalized = DoubleArray(pcm.size)
        pcm.forEachIndexed { i, v ->
            if (v == Short.MAX_VALUE || v == Short.MIN_VALUE) clipped++
            normalized[i] = v.toDouble() * correction
        }
        return CorrectedPcm(normalized, clipped)
    }

    private fun levelsForBand(samples: DoubleArray, minHz: Double, maxHz: Double): Levels {
        require(minHz >= 0.0 && minHz < maxHz && maxHz <= 200.0)
        val filter = Bandpass(minHz, maxHz)
        val accumulator = Accumulator()
        samples.forEach { accumulator.accept(filter.sample(it)) }
        return accumulator.result()
    }

    /** Shared overall level calculation used by local statistics JSON and WAV-derived displays.
     * gainDb is subtracted exactly once by referring the PCM back to the pre-gain input.
     */
    fun analyzeOverall(pcm: ShortArray, gainDb: Double, minHz: Double, maxHz: Double): Levels {
        val corrected = correctedPcm(pcm, gainDb)
        return levelsForBand(corrected.samples, minHz, maxHz)
    }

    fun analyze(pcm: ShortArray, gainDb: Double, config: Configuration): Result {
        val corrected = correctedPcm(pcm, gainDb)
        val normalized = corrected.samples
        val overall = levelsForBand(normalized, config.minHz, config.maxHz)
        val width = (config.filterMaxHz - config.filterMinHz) / config.filterBands
        val bands = List(config.filterBands) { idx ->
            val low = config.filterMinHz + idx * width
            val high = config.filterMinHz + (idx + 1) * width
            Band(idx, low, high, levelsForBand(normalized, low, high))
        }
        // 2048-sample Hann PSD with 50% overlap; power/Hz of FS-normalized PCM.
        val fft = 2048
        val hop = fft / 2
        val bins = (config.minHz * fft / RATE_HZ).let { kotlin.math.ceil(it).toInt() }..(config.maxHz * fft / RATE_HZ).toInt()
        val frequencies = bins.map { it * RATE_HZ.toDouble() / fft }.toDoubleArray()
        val spectral = Array(frequencies.size) { ArrayList<Double>() }
        val window = DoubleArray(fft) { i -> 0.5 - 0.5 * cos(2.0 * PI * i / (fft-1)) }
        val norm = RATE_HZ * window.sumOf { it * it }
        val tables = frequencies.indices.map { idx ->
            val k = bins.first + idx
            Pair(DoubleArray(fft) { i -> cos(2.0 * PI * k * i / fft) },
                 DoubleArray(fft) { i -> sin(2.0 * PI * k * i / fft) })
        }
        var pos = 0
        while (pos + fft <= normalized.size) {
            for (b in spectral.indices) {
                var real = 0.0
                var imaginary = 0.0
                val cosTable = tables[b].first
                val sinTable = tables[b].second
                for (i in 0 until fft) {
                    val x = normalized[pos+i] * window[i]
                    real += x * cosTable[i]
                    imaginary -= x * sinTable[i]
                }
                spectral[b].add(2.0 * (real*real + imaginary*imaginary) / norm)
            }
            pos += hop
        }
        fun sortedMedian(values: List<Double>): Double {
            if (values.isEmpty()) return 0.0
            val a = values.sorted()
            return if (a.size % 2 == 0) (a[a.size/2-1] + a[a.size/2]) / 2.0 else a[a.size/2]
        }
        val mean = spectral.map { it.averageOrZero() }.toDoubleArray()
        val median = spectral.map { sortedMedian(it) }.toDoubleArray()
        val max = spectral.map { it.maxOrNull() ?: 0.0 }.toDoubleArray()
        val peakIndex = mean.indices.maxByOrNull { mean[it] }
        return Result(overall, bands, corrected.clippedSamples, pcm.size, gainDb, config, frequencies, mean, median, max, peakIndex?.let { frequencies[it] })
    }
    private fun List<Double>.averageOrZero() = if (isEmpty()) 0.0 else average()
}
