package de.brummbeobachter.app

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/** Host-only regression. Does not claim Android/Compose build or on-device performance. */
fun main() {
    var tests = 0
    fun verify(value: Boolean, name: String) {
        check(value) { "FAIL: $name" }; println("PASS: $name"); tests++
    }
    verify(5 in AnalyzerTiming.allowedFftHops && AnalyzerTiming.frameIntervalMs(5)==5,
        "FFT-Hop 5 -> 5 ms")
    for (high in intArrayOf(20,50,100,200)) {
        val ticks=AnalyzerTiming.frequencyTicks(0,high)
        val unit=AnalyzerTiming.unitFrequencyHz(0,high)
        verify(ticks[1].toDouble()<unit && unit<ticks[2].toDouble() && ticks.last()==high,
            "Hz precisely between consecutive ticks at $high Hz")
    }
    verify(AnalyzerTiming.validRange(7,173) && !AnalyzerTiming.validRange(170,171) &&
        !AnalyzerTiming.validRange(0,201), "free range validation")
    verify(AnalyzerTiming.historyRows(250,123)==492 &&
        AnalyzerTiming.validHistory(600,250) && !AnalyzerTiming.validHistory(600,100) &&
        AnalyzerTiming.validHistory(601,1000) && !AnalyzerTiming.validHistory(14401,20000), "free history extended from 600 s; 2400-row and 14400-s guard")
    for (size in AnalyzerTiming.allowedFftSizes) {
        val core=LiveAnalyzerCore(1000,size,100,200.0)
        verify(abs(core.frequencyResolutionHz - 1000.0/size)<1e-10, "FFT resolution size=$size")
    }
    val size=2048
    val pcm=DoubleArray(size + 5*3) { i -> 0.5*sin(2*PI*(1000.0*20/size)*i/1000.0) }
    for (window in AnalyzerWindow.values()) {
        val frames=LiveAnalyzerCore(1000,size,5,200.0,5.0,window).acceptNormalized(pcm)
        val last=frames.last()
        val peak=last.liveDbFs.indices.maxByOrNull { last.liveDbFs[it] }!!
        verify(frames.size==4 && abs(last.frequenciesHz[peak]-1000.0*20/size)<1e-8 &&
            abs(last.liveDbFs[peak] + 6.0206)<0.1, "$window coherent amplitude; 5-sample hop")
    }
    val disabled=LiveAnalyzerCore(1000,2048,100,200.0,0.0,AnalyzerWindow.HANN)
        .acceptNormalized(pcm).last()
    verify(disabled.liveDbFs.contentEquals(disabled.averageDbFs), "smoothing OFF: average equals current")
    println("ANALYZER v0.5.02 HOST SELFTEST PASS $tests/$tests")
}
