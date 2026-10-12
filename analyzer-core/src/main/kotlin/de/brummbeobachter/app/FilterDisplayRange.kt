package de.brummbeobachter.app

import kotlin.math.ceil
import kotlin.math.floor

/** Display-only auto-range of actual digital filter-band RMS levels (dBFS).
 * No changes to PCM, filter coefficients, gain, calibration or transient thresholds.
 * Low side uses a time-robust quantile; high side retains valid short peaks.
 * -240 dBFS is the filter bank's numerical silence sentinel, NOT a measured level.
 */
class FilterDisplayRange(private val marginDb: Int = 5) {
    init { require(marginDb in 0..30) }
    private val frameLows = ArrayList<Double>()
    private val frameHighs = ArrayList<Double>()
    val acceptedFrames: Int get() = frameLows.size

    fun accept(bandDbFs: DoubleArray) {
        val valid = bandDbFs.filter { it.isFinite() && it > -200.0 && it <= 50.0 }.sorted()
        if (valid.isEmpty()) return
        // All bands count toward the floor; a very brief strong impulse survives in high.
        frameLows.add(quantile(valid, 0.10))
        frameHighs.add(valid.last())
    }

    fun result(): Pair<Int,Int>? {
        if (frameLows.size < 2) return null
        val low = quantile(frameLows.sorted(), 0.10)
        val high = frameHighs.maxOrNull() ?: return null
        if (!low.isFinite() || !high.isFinite() || high < low) return null
        val lower = floor(low - marginDb).toInt().coerceIn(-240,40)
        val upper = ceil(high + marginDb).toInt().coerceIn(-230,50)
        val min = minOf(lower, upper-10).coerceAtLeast(-240)
        val max = maxOf(upper, min+10).coerceAtMost(50)
        return min to max
    }

    private fun quantile(sorted: List<Double>, f: Double): Double {
        val pos=(sorted.size-1)*f
        val lo=floor(pos).toInt();val hi=ceil(pos).toInt()
        return sorted[lo]+(sorted[hi]-sorted[lo])*(pos-lo)
    }
}
