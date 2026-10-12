package de.brummbeobachter.app

import kotlin.math.log10
import kotlin.math.pow

/** One waterfall row from the actual FFT frames collected since the previous row.
 * This class never changes microphone, FFT cadence or the 120-second row timebase.
 */
enum class WaterfallMode(val label: String) {
    CURRENT("Aktuell"), AVERAGE("Mittelwert"), MAXIMUM("Maximum")
}

class WaterfallAggregator {
    private var count = 0
    private var latest = doubleArrayOf()
    private var powerSum = doubleArrayOf()
    private var maximum = doubleArrayOf()

    val pendingFrames: Int get() = count

    fun clear() {
        count = 0
        latest = doubleArrayOf()
        powerSum = doubleArrayOf()
        maximum = doubleArrayOf()
    }

    fun accept(dbfs: DoubleArray) {
        if (dbfs.isEmpty()) return
        if (count != 0 && dbfs.size != latest.size) clear()
        if (count == 0) {
            powerSum = DoubleArray(dbfs.size)
            maximum = DoubleArray(dbfs.size) { Double.NEGATIVE_INFINITY }
        }
        latest = dbfs.copyOf()
        for (i in dbfs.indices) {
            val db = dbfs[i].let { if (it.isFinite()) it.coerceIn(-240.0, 100.0) else -240.0 }
            powerSum[i] += 10.0.pow(db / 10.0)
            if (db > maximum[i]) maximum[i] = db
        }
        count++
    }

    /** Flush exactly once per waterfall interval; no dB arithmetic averaging. */
    fun take(mode: WaterfallMode): DoubleArray? {
        if (count == 0) return null
        val row = when (mode) {
            WaterfallMode.CURRENT -> latest.copyOf()
            WaterfallMode.AVERAGE -> DoubleArray(powerSum.size) { i ->
                10.0 * log10((powerSum[i] / count).coerceAtLeast(1.0e-24))
            }
            WaterfallMode.MAXIMUM -> maximum.copyOf()
        }
        clear()
        return row
    }
}
