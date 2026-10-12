package de.brummbeobachter.app

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** Direct Analyzer v0.5.09: standalone host DSP/configuration regression tests. */
fun main() {
    var passed=0
    fun checkThat(name:String, predicate:()->Boolean) {
        check(predicate()) { "FAIL: $name" }
        ++passed
        println("PASS: $name")
    }
    fun signal(rate:Int, freq:Double, count:Int, amp:Double=.3):ShortArray =
        ShortArray(count) { i -> (32767*amp*sin(2*PI*freq*i/rate)).roundToInt().coerceIn(-32768,32767).toShort() }
    checkThat("rates and FFT list") { DirectAnalyzerConfig.inputRates.contentEquals(intArrayOf(8000,11025,16000,22050,44100,48000,96000)) &&
        DirectAnalyzerConfig.fftSizes.contentEquals(intArrayOf(64,128,256,512,1024,2048,4096,8192,16384)) }
    checkThat("decimation 0 and 1 both retain sample rate") {
        DirectAnalyzerConfig.outputRate(48000,0)==48000 && DirectAnalyzerConfig.outputRate(48000,1)==48000
    }
    checkThat("direct factor 4:1") { DirectAnalyzerConfig.outputRate(48000,4)==12000 }
    checkThat("direct factor 12:1") { DirectAnalyzerConfig.outputRate(96000,12)==8000 }
    checkThat("nonintegral rate/factor refused") { !DirectAnalyzerConfig.valid(11025,2) && !DirectAnalyzerConfig.valid(44100,8) }
    checkThat("hop interval at 8k") { DirectAnalyzerConfig.hop(8000,0,10)==80 && DirectAnalyzerConfig.hop(8000,0,100)==800 }
    checkThat("hop interval at 96k") { DirectAnalyzerConfig.hop(96000,0,10)==960 && DirectAnalyzerConfig.hop(96000,0,100)==9600 }
    checkThat("timing functions allow direct hops without LF changes") {
        AnalyzerTiming.validHop(9600,96000) && AnalyzerTiming.validHop(10) && AnalyzerTiming.lfRateHz(16)==500
    }
    for (size in DirectAnalyzerConfig.fftSizes) {
        val core=LiveAnalyzerCore(8000,size,800,4000.0,windowType=AnalyzerWindow.HANN)
        val frames=core.accept(signal(8000,500.0,size+1600))
        checkThat("FFT $size; hop may exceed FFT") { frames.isNotEmpty() && frames.last().fftSize==size &&
            frames.last().frequenciesHz.size==size/2+1 }
    }
    for (window in AnalyzerWindow.values()) {
        val core=LiveAnalyzerCore(8000,1024,256,4000.0,windowType=window)
        val v=core.accept(signal(8000,500.0,4096)).last()
        val bin=64
        checkThat("window ${window.name} coherent amplitude") { abs(v.liveDbFs[bin] - 20*kotlin.math.log10(.3)) < .7 }
    }
    val noDc=LiveAnalyzerCore(8000,1024,256,4000.0,subtractDc=false)
        .accept(ShortArray(2048){4000}).last()
    val withDc=LiveAnalyzerCore(8000,1024,256,4000.0,subtractDc=true)
        .accept(ShortArray(2048){4000}).last()
    checkThat("subtract DC suppresses DC bin") { noDc.liveDbFs[0] > -25 && withDc.liveDbFs[0] < -180 }
    val smoothHold=LiveAnalyzerCore(8000,1024,256,4000.0,smoothingAlpha=0.0)
    val strong=smoothHold.accept(signal(8000,500.0,2048,.45)).last()
    val weak=smoothHold.accept(signal(8000,500.0,2048,.04)).last()
    checkThat("alpha 0 keeps previous average") { abs(strong.averageDbFs[64]-weak.averageDbFs[64])<.0001 }
    val smoothNow=LiveAnalyzerCore(8000,1024,256,4000.0,smoothingAlpha=1.0)
    smoothNow.accept(signal(8000,500.0,2048,.45))
    val newSignal=smoothNow.accept(signal(8000,500.0,2048,.04)).last()
    checkThat("alpha 1 follows current signal") { abs(newSignal.averageDbFs[64]-newSignal.liveDbFs[64])<.0001 }
    val fir=DirectFirDecimator(96000,12)
    val low=fir.accept(signal(96000,50.0,96000,.3))
    val lowRms=sqrt(low.map{it*it}.average())
    checkThat("FIR factor12 preserves 50 Hz normalized level") { low.size in 7800..8000 && abs(lowRms - (.3/sqrt(2.0))) < .015 }
    fir.reset()
    val high=fir.accept(signal(96000,6000.0,96000,.3))
    val highRms=sqrt(high.map{it*it}.average())
    checkThat("FIR factor12 attenuates >Nyquist 6 kHz") { highRms < lowRms/15.0 }
    val peaksHz=DoubleArray(128){ it*10.0 }
    val dB=DoubleArray(128){ -100.0 }.also { it[10]=-20.0;it[11]=-32.0;it[30]=-40.0 }
    val peaks=AnalyzerPeakMarkers.find(peaksHz,dB,0,1000,2)
    checkThat("two peak markers, distinct local maxima") { peaks.size==2 && peaks[0].bin==10 && peaks[1].bin==30 }
    checkThat("peak markers disabled") { AnalyzerPeakMarkers.find(peaksHz,dB,0,1000,0).isEmpty() }
    println("ANALYZER v0.5.09 DIRECT HOST SELFTEST PASS $passed/$passed")
}
