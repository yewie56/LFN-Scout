package de.brummbeobachter.app

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

fun main() {
    var passed = 0
    fun checkCase(name: String, okay: Boolean) {
        check(okay) { "FAIL $name" }
        println("PASS: $name")
        passed++
    }
    checkCase("48-kHz range 0–22000", AnalyzerTiming.validWidebandRange(0,22000) && !AnalyzerTiming.validRange(0,22000))
    checkCase("48-kHz FFT 8192 and 16384", 8192 in AnalyzerTiming.allowedFftSizes && 16384 in AnalyzerTiming.allowedFftSizes)
    checkCase("48-kHz default 4800-sample hop=100 ms", AnalyzerTiming.frameIntervalMs(4800,48000) == 100)
    checkCase("wideband Hz label between ticks", AnalyzerTiming.frequencyTicks(0,22000).let { t ->
        AnalyzerTiming.unitFrequencyHz(0,22000) > t[1] && AnalyzerTiming.unitFrequencyHz(0,22000) < t[2] })
    checkCase("3600-s history gets memory-bounded interval", AnalyzerTiming.intervalForDuration(3600,250,4800,48000).let { it != null &&
        AnalyzerTiming.historyRows(it, 3600) <= AnalyzerTiming.MAX_HISTORY_ROWS })
    checkCase("7200-s history gets memory-bounded interval", AnalyzerTiming.intervalForDuration(7200,250,4800,48000).let { it != null &&
        AnalyzerTiming.historyRows(it, 7200) <= AnalyzerTiming.MAX_HISTORY_ROWS })
    checkCase("14400-s upper limit enforced", AnalyzerTiming.intervalForDuration(14401,250,4800,48000) == null)
    checkCase("old 8-kHz LF range preserved", AnalyzerTiming.validRange(0,200) && !AnalyzerTiming.validRange(0,201))
    checkCase("8-kHz legacy FFT hop 5 preserved", 5 in AnalyzerTiming.allowedFftHops)

    val sr = 48000
    val n = sr * 2
    val samples = ShortArray(n) { i ->
        val v = 0.08 * sin(2.0 * PI * 50.0 * i / sr) +
            0.08 * sin(2.0 * PI * 195.0 * i / sr) +
            0.05 * sin(2.0 * PI * 10000.0 * i / sr)
        (32767 * v).roundToInt().toShort()
    }
    val core = LiveAnalyzerCore(sr,8192,4800,22000.0,5.0,AnalyzerWindow.HANN)
    val spectra = ArrayList<LiveAnalyzerCore.Spectrum>()
    for (offset in samples.indices step 4096) {
        val size = minOf(4096,samples.size-offset)
        spectra.addAll(core.accept(samples,offset,size))
    }
    checkCase("48-kHz core produces spectra", spectra.size >= 12)
    val s = spectra.last()
    checkCase("48-kHz spectrum includes 22k bins", s.frequenciesHz.last() >= 21990.0)
    fun peakAt(hz:Double):Double = s.liveDbFs.indices.filter{ abs(s.frequenciesHz[it]-hz) <= sr.toDouble()/8192 }
        .maxOf { s.liveDbFs[it] }
    checkCase("direct 50 Hz detected", peakAt(50.0) > -45.0)
    checkCase("direct 195 Hz detected", peakAt(195.0) > -45.0)
    checkCase("direct 10 kHz detected", peakAt(10000.0) > -48.0)
    val sumDb = 20.0 * log10(0.08)
    checkCase("48-kHz 50 Hz level plausible", abs(peakAt(50.0)-sumDb) < 9.0)
    val longer = LiveAnalyzerCore(48000,16384,4800,22000.0)
    val after = longer.accept(samples)
    checkCase("16384 FFT produces spectra", after.isNotEmpty() && abs(longer.frequencyResolutionHz-48000.0/16384.0)<1e-8)
    println("ANALYZER v0.5.04 HOST SELFTEST PASS $passed/$passed")
}
