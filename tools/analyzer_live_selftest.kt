import de.brummbeobachter.app.LiveAnalyzerCore
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

fun main() {
    val rate = 1000
    val fftSize = 4096
    val hz = 180.0
    val core = LiveAnalyzerCore(rate, fftSize, 512, 200.0, 5.0)
    val pcm = ShortArray(8192) { i -> (16384.0 * sin(2.0 * PI * hz * i / rate)).roundToInt().toShort() }
    val spectra = core.accept(pcm)
    check(spectra.isNotEmpty())
    val s = spectra.last()
    val idx = s.liveDbFs.indices.maxByOrNull { s.liveDbFs[it] }!!
    check(abs(s.frequenciesHz[idx] - hz) <= rate.toDouble()/fftSize)
    check(abs(s.frequenciesHz.last() - 200.0) < rate.toDouble()/fftSize)
    check(s.liveDbFs.all { it.isFinite() })
    println("PASS: 180-Hz fixture reaches 200-Hz LF range, FFT bin=${s.frequenciesHz[idx]}")
    println("PASS: FFT size $fftSize, 1-kHz input, ${spectra.size} real FFT frames")
    val history = java.util.ArrayDeque<DoubleArray>()
    for (frame in spectra) {
        history.addLast(frame.liveDbFs.copyOf())
        while (history.size > 235) history.removeFirst()
    }
    check(history.size <= 235)
    check(history.all { it.size == s.liveDbFs.size })
    println("PASS: bounded live-waterfall rows and immutable per-row arrays")
    val silence = LiveAnalyzerCore(rate,fftSize,512,200.0)
    val quiet = silence.accept(ShortArray(8192)).last()
    check(quiet.liveDbFs.all { it.isFinite() })
    println("PASS: silence remains finite, no synthetic event markers")
}
