package de.brummbeobachter.app

import kotlin.math.ceil
import kotlin.math.floor

/**
 * Robust, display-only dBFS auto-ranging. Never changes audio samples, gain or calibration.
 *
 * Step 1 (frequency): a separate 5th/95th percentile is computed for each valid
 * live spectrum in the selected frequency range. A single narrow dip or spike
 * cannot dominate the dynamic range.
 * Step 2 (time): the 10th percentile of frame lows and 90th percentile of
 * frame highs suppress short-lived outlier frames during the capture interval.
 * Both stages are deterministic and bounded by the current frequency selection.
 */
class AnalyzerDisplayRange(private val marginDb: Int = 5) {
    init { require(marginDb in 0..30) }
    private val frameLows = ArrayList<Double>()
    private val frameHighs = ArrayList<Double>()
    val acceptedFrames: Int get() = frameLows.size

    /** DC is excluded because it is removed by the FFT; reject the numeric noise sentinel. */
    fun accept(spectrum: LiveAnalyzerCore.Spectrum, maxHz: Int) {
        require(maxHz > 0)
        val values = ArrayList<Double>()
        for (i in spectrum.liveDbFs.indices) {
            val hz = spectrum.frequenciesHz.getOrNull(i) ?: continue
            val db = spectrum.liveDbFs[i]
            if (hz > 0.0 && hz <= maxHz && db.isFinite() && db > -180.0) values.add(db)
        }
        if (values.isEmpty()) return
        values.sort()
        frameLows.add(if (values.size >= 20) percentile(values, 0.05) else values.first())
        frameHighs.add(if (values.size >= 20) percentile(values, 0.95) else values.last())
    }

    fun result(): Pair<Int, Int>? {
        if (acceptedFrames == 0) return null
        val low = percentile(frameLows.sorted(), 0.10)
        val high = percentile(frameHighs.sorted(), 0.90)
        if (!low.isFinite() || !high.isFinite() || high < low) return null
        val lower = floor(low - marginDb).toInt().coerceIn(-240, 50)
        val upper = ceil(high + marginDb).toInt().coerceIn(-240, 50)
        // At least ten dB of readable height even for a nearly constant spectrum.
        val min = minOf(lower, upper - 10).coerceAtLeast(-240)
        val max = maxOf(upper, min + 10).coerceAtMost(50)
        return min to max
    }

    private fun percentile(sorted: List<Double>, fraction: Double): Double {
        require(sorted.isNotEmpty())
        val pos = (sorted.size - 1) * fraction
        val lo = floor(pos).toInt()
        val hi = ceil(pos).toInt()
        return sorted[lo] + (sorted[hi] - sorted[lo]) * (pos - lo)
    }
}

/** Shared axis tick positions for Compose and bitmap export. Always includes exact end values. */
object AnalyzerDisplayTicks {
    fun db(minDb: Int, maxDb: Int): List<Int> {
        require(minDb < maxDb)
        val span = maxDb - minDb
        val step = when { span <= 25 -> 5; span < 60 -> 10; span <= 120 -> 20; else -> 40 }
        val first = Math.floorDiv(minDb, step) * step
        return (listOf(minDb, maxDb) + (first..maxDb step step).filter { it > minDb && it < maxDb }).distinct().sorted()
    }
}
