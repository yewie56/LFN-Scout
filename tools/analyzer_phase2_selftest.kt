import de.brummbeobachter.app.AnalyzerLiveSession
import de.brummbeobachter.app.AnalyzerPcmHub
import de.brummbeobachter.app.LiveAnalyzerCore
import de.brummbeobachter.app.MicCaptureArbiter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

fun main() {
    val core = LiveAnalyzerCore(1000, 1024, 256)
    val samples = ShortArray(8192) { (16384.0 * sin(2.0 * PI * 10.0 * it / 1000)).roundToInt().toShort() }
    val spectrum = core.accept(samples).last()
    val peak = spectrum.liveDbFs.indices.maxByOrNull { spectrum.liveDbFs[it] }!!
    check(abs(spectrum.frequenciesHz[peak] - 10.0) < core.frequencyResolutionHz)
    check(abs(spectrum.liveDbFs[peak] + 6.02) < 1.3)
    println("PASS: FFT 10-Hz input and dBFS calibration, frequency ${spectrum.frequenciesHz[peak]}")

    val hub = AnalyzerPcmHub()
    val gate = CountDownLatch(1)
    val entered = CountDownLatch(1)
    val observed = CountDownLatch(2)
    val received = mutableListOf<Int>()
    val listener = hub.subscribe(1) {
        if (it.sequence == 1L) { entered.countDown(); gate.await(2, TimeUnit.SECONDS) }
        synchronized(received) { received.add(it.samples[0].toInt()) }
        observed.countDown()
    }
    try {
        val pcm = shortArrayOf(100)
        hub.publish(pcm, 1, 1000, "MIC", "fake", 0)
        check(entered.await(2, TimeUnit.SECONDS))
        pcm[0] = 101
        val start = System.nanoTime()
        for (i in 2..32) hub.publish(shortArrayOf(i.toShort()), 1, 1000, "MIC", "fake", 0)
        check(System.nanoTime() - start < 300_000_000L)
        check(listener.droppedFrames >= 30)
        gate.countDown()
        check(observed.await(2, TimeUnit.SECONDS))
        check(synchronized(received) { received == listOf(100, 32) })
        println("PASS: nonblocking PCM fan-out, defensive copy, backpressure drop=${listener.droppedFrames}")
    } finally { gate.countDown(); listener.close() }

    val fftHub = AnalyzerPcmHub()
    val results = CountDownLatch(5)
    val session = AnalyzerLiveSession(fftHub, { results.countDown() }, 1024, 256)
    try {
        repeat(8) { _ -> fftHub.publish(samples, 256, 1000, "MIC", "fake", 0) }
        check(results.await(2, TimeUnit.SECONDS))
        val previous = session.processedSpectra
        fftHub.publish(shortArrayOf(1, 2), 2, 1000, "UNPROCESSED", "fake", 0)
        Thread.sleep(150)
        
        check(session.discontinuityCount >= 1)
        check(session.processedSpectra == previous)
        println("PASS: session emits FFT, detects source change and resets continuity")
    } finally { session.close() }
    val lease = MicCaptureArbiter.tryAcquire("rolling")!!
    check(MicCaptureArbiter.tryAcquire("direct_recording") == null)
    check(MicCaptureArbiter.owner == "rolling")
    lease.close()
    val direct = MicCaptureArbiter.tryAcquire("direct_recording")!!
    try {
        check(MicCaptureArbiter.tryAcquire("rolling") == null)
        check(MicCaptureArbiter.owner == "direct_recording")
    } finally { direct.close() }
    check(MicCaptureArbiter.owner == null)
    println("PASS: capture arbiter prevents conflicting microphone ownership")
    println("PASS: Phase-2 isolated JVM host complete")
}
