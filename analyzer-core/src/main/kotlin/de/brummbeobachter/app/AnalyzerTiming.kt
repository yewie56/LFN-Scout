package de.brummbeobachter.app

/** Timing of the analyzer-only LF stream; independent of 8:1 FIR decimation. (Transfer revision R1: comment only.) */
object AnalyzerTiming {
    const val LF_RATE_HZ = 1000
    val allowedDecimationFactors = intArrayOf(4, 8, 16)
    fun lfRateHz(decimationFactor: Int): Int {
        require(decimationFactor in allowedDecimationFactors)
        return 8000 / decimationFactor
    }
    /** UI snapshots can be requested between STOP and mode restoration. Never crash
     * the Compose main thread on a stale/invalid decimation factor at that boundary.
     * The actual LF session still validates its requested factor separately.
     */
    fun displayRateHz(wideband: Boolean, decimationFactor: Int): Int =
        if (wideband) 48000 else 8000 / decimationFactor.takeIf { it in allowedDecimationFactors }.let { it ?: 8 }

    const val FFT_SIZE = 4096
    const val HISTORY_SECONDS = 120
    const val MIN_HISTORY_SECONDS = 10
    const val MAX_HISTORY_SECONDS = 14400
    const val MAX_HISTORY_ROWS = 2400
    val allowedFftSizes = intArrayOf(2048, 4096, 8192, 16384)

    /** Bound actual allocated rows, not just elapsed seconds, to control memory. */
    fun validHistory(seconds: Int, intervalMs: Int): Boolean =
        seconds in MIN_HISTORY_SECONDS..MAX_HISTORY_SECONDS &&
            intervalMs in allowedWaterfallIntervalsMs && seconds * 1000L / intervalMs <= MAX_HISTORY_ROWS

    fun validStatistics(minHz: Int, maxHz: Int, windowSec: Int, averageSec: Double): Boolean =
        minHz in 0..199 && maxHz in 1..200 && maxHz > minHz &&
        windowSec in 10..600 && averageSec.isFinite() && averageSec in 0.1..60.0 && averageSec <= windowSec

    /** Linear frequency view on the fixed 0–200 Hz core. */
    fun validRange(minHz: Int, maxHz: Int): Boolean =
        minHz in 0..195 && maxHz in 5..200 && maxHz - minHz >= 5
    fun validWidebandRange(minHz: Int, maxHz: Int): Boolean =
        minHz >= 0 && maxHz <= 48000 && maxHz - minHz >= 5

    /** Numeric ticks including both endpoints, with nonoverlapping unit position. */
    fun frequencyTicks(minHz: Int, maxHz: Int): IntArray {
        require(validRange(minHz, maxHz) || validWidebandRange(minHz, maxHz))
        val span = maxHz - minHz
        val step = intArrayOf(1, 2, 5, 10, 20, 25, 50, 100, 200, 500, 1000, 2000, 5000).firstOrNull { it >= span / 5.0 } ?: 5000
        val ticks = (minHz..maxHz step step).toMutableList()
        if (ticks.last() != maxHz) ticks.add(maxHz)
        return ticks.toIntArray()
    }

    fun unitFrequencyHz(minHz: Int, maxHz: Int): Double {
        val ticks = frequencyTicks(minHz, maxHz)
        return (ticks[1] + ticks[2]) / 2.0
    }
    val allowedFftHops = intArrayOf(5, 10, 50, 100, 200, 480, 500, 960, 1000, 2400, 4800)
    val allowedWidebandHops = intArrayOf(480, 960, 2400, 4800)
    fun validHop(hop: Int, rateHz: Int = LF_RATE_HZ): Boolean =
        hop in allowedFftHops || (rateHz > 0 && DirectAnalyzerConfig.transformIntervalsMs.any { ms ->
            (rateHz.toLong() * ms + 500L) / 1000L == hop.toLong()
        })
    /** Wall-clock LF sample intervals, not a number of FFTs. */
    val allowedWaterfallIntervalsMs = intArrayOf(100, 200, 250, 500, 1000, 2000, 5000, 10000, 20000)
    const val DEFAULT_FFT_HOP = 100
    const val DEFAULT_WATERFALL_INTERVAL_MS = 250

    /** A spectrum cannot provide more than one distinct row per FFT hop. */
    fun effectiveWaterfallIntervalMs(fftHop: Int, requestedMs: Int, sampleRateHz: Int = LF_RATE_HZ): Int {
        require(validHop(fftHop, sampleRateHz) && requestedMs in allowedWaterfallIntervalsMs)
        return maxOf((fftHop * 1000 + sampleRateHz - 1) / sampleRateHz, requestedMs)
    }

    /** Choose a permissible interval that bounds historical rows. No hidden rounding of duration. */
    fun intervalForDuration(seconds: Int, requestedMs: Int, hop: Int, rateHz: Int): Int? =
        allowedWaterfallIntervalsMs.firstOrNull { it >= requestedMs &&
            it >= frameIntervalMs(hop, rateHz) && validHistory(seconds, it) }

    fun historyRows(waterfallIntervalMs: Int, historySeconds: Int = HISTORY_SECONDS): Int {
        require(waterfallIntervalMs in allowedWaterfallIntervalsMs)
        require(validHistory(historySeconds, waterfallIntervalMs))
        return historySeconds * 1000 / waterfallIntervalMs
    }

    /** Advance the waterfall phase without drift, also when FFT hop is changed. */
    fun advanceWaterfall(phaseLfSamples: Int, fftHop: Int, intervalMs: Int, sampleRateHz: Int = LF_RATE_HZ): Pair<Int, Boolean> {
        require(validHop(fftHop, sampleRateHz) && intervalMs in allowedWaterfallIntervalsMs)
        val elapsedUnits = phaseLfSamples + fftHop * 1000
        val unitsPerRow = intervalMs * sampleRateHz
        return (elapsedUnits % unitsPerRow) to (elapsedUnits >= unitsPerRow)
    }

    fun statisticFrames(fftHop: Int, seconds: Int = HISTORY_SECONDS, sampleRateHz: Int = LF_RATE_HZ): Int {
        require(validHop(fftHop, sampleRateHz))
        return (seconds * sampleRateHz + fftHop - 1) / fftHop
    }

    fun frameIntervalMs(fftHop: Int, sampleRateHz: Int = LF_RATE_HZ): Int {
        require(validHop(fftHop, sampleRateHz))
        return (fftHop * 1000 + sampleRateHz - 1) / sampleRateHz
    }
}
