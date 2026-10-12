package de.brummbeobachter.app

import java.util.Random
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

/** Diagnostic only: synthetic PCM runs through the identical production FFT core. */
internal object NoiseDiagnostic {
    @Volatile var result: String = "Noch kein Weißrausch-Selbsttest"
        private set
    @Volatile private var busy = false

    /** Fitted dB-versus-log10(f) slope; positive means increasing toward high frequencies. */
    fun slope(frequenciesHz: DoubleArray, powers: DoubleArray, lowHz: Double = 1.0,
              highHz: Double = 100.0): Double? {
        if (frequenciesHz.size != powers.size) return null
        var n=0; var sx=0.0; var sy=0.0; var sxx=0.0; var sxy=0.0
        for (i in powers.indices) {
            val f=frequenciesHz[i]; val p=powers[i]
            if (f < lowHz || f > highHz || !f.isFinite() || !p.isFinite() || p <= 0.0) continue
            val x=log10(f); val y=10.0*log10(p)
            n++;sx+=x;sy+=y;sxx+=x*x;sxy+=x*y
        }
        val den=n*sxx-sx*sx
        return if (n < 5 || den <= 0.0) null else (n*sxy-sx*sy)/den
    }
    fun reportSlope(frequenciesHz: DoubleArray, powers: DoubleArray): String =
        slope(frequenciesHz,powers)?.let { "Steigung 1–100 Hz: %.2f dB/Dekade".format(java.util.Locale.US,it) }
            ?: "Steigung nicht bestimmbar (zu wenige gültige Bins)"

    fun startWhiteNoiseTest() {
        synchronized(this) {
            if (busy) return
            busy=true; result="Weißrausch-FFT-Selbsttest läuft …"
        }
        Thread({
            try {
                val rate=1000; val fft=4096
                val core=LiveAnalyzerCore(sampleRateHz=rate,fftSize=fft,hopSize=512,
                    maxFrequencyHz=100.0,averageTimeSeconds=0.0,windowType=AnalyzerWindow.HANN)
                val rng=Random(4545L)
                val sums=DoubleArray(fft*100/rate+1);var windows=0
                repeat(40) {
                    val samples=DoubleArray(4096) { rng.nextGaussian()*0.015 }
                    val out=core.acceptNormalized(samples)
                    for (s in out) {
                        if (s.liveDbFs.size != sums.size) continue
                        for(i in sums.indices) sums[i]+=10.0.pow(s.liveDbFs[i]/10.0)
                        windows++
                    }
                }
                check(windows > 100)
                val freq=DoubleArray(sums.size) { it*rate.toDouble()/fft }
                val mean=DoubleArray(sums.size) { sums[it]/windows }
                val slope=slope(freq,mean)
                val status=if(slope != null && kotlin.math.abs(slope)<=2.5) "PASS" else "FAIL"
                result="Weißrausch-FFT: $status | ${windows} Fenster | " +
                    (slope?.let { "%.2f dB/Dekade (1–100 Hz)".format(java.util.Locale.US,it) } ?: "Steigung ungültig")
            } catch(t: Throwable) { result="Weißrausch-FFT: FEHLER ${t.message}" }
            finally { synchronized(this) { busy=false } }
        },"LFN-Noise-Diagnostic").apply { isDaemon=true; start() }
    }
}
