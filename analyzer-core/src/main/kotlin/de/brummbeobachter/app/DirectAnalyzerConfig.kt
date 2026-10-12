package de.brummbeobachter.app

/** Direct-mode-only capabilities; legacy rolling decimation remains independent. */
object DirectAnalyzerConfig {
    val inputRates = intArrayOf(8000, 11025, 16000, 22050, 44100, 48000, 96000)
    val fftSizes = intArrayOf(64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384)
    val transformIntervalsMs = intArrayOf(5, 10, 15, 20, 25, 30, 50, 100)
    /** 0 disables decimation; 1 is mathematically equivalent to 1:1. */
    fun effectiveFactor(selected: Int): Int { require(selected in 0..12); return selected.coerceAtLeast(1) }
    fun valid(rate: Int, selected: Int): Boolean =
        rate in inputRates && selected in 0..12 && rate % effectiveFactor(selected) == 0
    fun outputRate(rate: Int, selected: Int): Int {
        require(valid(rate, selected)) { "Abtastrate $rate / Dezimierung $selected ist nicht ganzzahlig" }
        return rate / effectiveFactor(selected)
    }
    fun hop(rate: Int, factor: Int, intervalMs: Int): Int {
        require(intervalMs in transformIntervalsMs)
        // Round to the closest attainable sample count; long FFT hops are legitimate.
        return ((outputRate(rate, factor).toLong() * intervalMs + 500L) / 1000L).toInt().coerceAtLeast(1)
    }
    val permissibleHops: Set<Int> by lazy {
        inputRates.flatMap { rate -> (0..12).filter { valid(rate, it) }.flatMap { factor ->
            transformIntervalsMs.map { ms -> hop(rate, factor, ms) }
        } }.toSet()
    }
    fun validFrequency(maxHz: Int, rate: Int, factor: Int): Boolean =
        valid(rate, factor) && maxHz >= 5 && maxHz <= outputRate(rate, factor) / 2
}
