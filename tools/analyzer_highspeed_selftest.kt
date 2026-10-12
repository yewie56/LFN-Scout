import de.brummbeobachter.app.AnalyzerPcmHub
import de.brummbeobachter.app.HighspeedImpulseDetector
import de.brummbeobachter.app.HighspeedImpulseSession
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.abs

fun main() {
    val rate8 = 8_000
    val raw = ShortArray(400) { 0 }
    // 0.5-ms pulse starting between 8:1 averaged 1-kHz output windows.
    for (i in 17..20) raw[i] = 20_000
    val legacy1k = ShortArray(raw.size / 8) { k ->
        (0 until 8).sumOf { raw[k * 8 + it].toInt() }.div(8).toShort()
    }
    check(legacy1k.maxOf { abs(it.toInt()) } < raw.maxOf { abs(it.toInt()) })
    val detector = HighspeedImpulseDetector(rate8, initialMinPeakPcm = 1_400)
    val first = detector.accept(raw.copyOfRange(0, 19), startMonotonicNs = 1_000_000_000L)
    val second = detector.accept(raw.copyOfRange(19, raw.size), startMonotonicNs = 1_000_000_000L + 19L * 1_000_000_000L / rate8)
    check(first.isEmpty() && second.size == 1)
    val event = second.single()
    check(event.peakPcm == 20_000 && event.onsetSampleIndex == 17L)
    check(abs(event.durationMs - 0.5) < 1e-9)
    check(event.onsetMonotonicNs == 1_000_000_000L + 17L * 1_000_000_000L / rate8)
    println("PASS: actual 8-kHz 0.5-ms pulse detected across chunk boundary; 1-kHz averaging attenuates peak")

    val raw48k = ShortArray(480) { if (it in 50..73) (-30_000).toShort() else 0 }
    val events48 = HighspeedImpulseDetector(48_000).accept(raw48k)
    check(events48.size == 1 && abs(events48.single().durationMs - 0.5) < 1e-9)
    check(events48.single().peakPcm == 30_000)
    check(HighspeedImpulseDetector(8_000).accept(ShortArray(320)).isEmpty())
    println("PASS: detector processes real 48-kHz fixture, silence produces no events (hardware capture NOT tested)")

    val clip = ShortArray(80) { if (it == 10) Short.MIN_VALUE else 0 }
    check(HighspeedImpulseDetector(8_000).accept(clip).single().clippedSamples == 1L)
    println("PASS: signed -32768 clipping handled without Int overflow")

    val hub = AnalyzerPcmHub()
    val arrived = CountDownLatch(1)
    val collected = mutableListOf<HighspeedImpulseDetector.Event>()
    val session = HighspeedImpulseSession(hub, { synchronized(collected) { collected.add(it) }; arrived.countDown() })
    try {
        val source = ShortArray(400) { if (it in 70..73) 22_000 else 0 }
        val timestamp = 4_000_000_000L
        hub.publish(source, 150, 8_000, "UNPROCESSED", "fixture", 0, timestamp)
        hub.publish(source.copyOfRange(150, 400), 250, 8_000, "UNPROCESSED", "fixture", 0,
            timestamp + 150L * 1_000_000_000L / 8_000)
        check(arrived.await(2, TimeUnit.SECONDS))
        check(synchronized(collected) { collected.single().peakPcm == 22_000 })
        // Changing the microphone route invalidates old continuity.
        hub.publish(shortArrayOf(0, 0), 2, 8_000, "MIC", "fixture", 0)
        Thread.sleep(120)
        check(session.discontinuityCount >= 1)
        println("PASS: async pre-gain raw-PCM fan-out and route reset")
    } finally { session.close() }
    // The live session must use updated sensitivity without mutating the upstream raw PCM.
    val quietHub = AnalyzerPcmHub()
    val quietArrived = CountDownLatch(1)
    val quietSession = HighspeedImpulseSession(quietHub, { if (it.peakPcm == 300) quietArrived.countDown() })
    try {
        quietSession.sensitivity = de.brummbeobachter.app.ImpulseSensitivityPreset.SENSITIVE.withGain(0)
        val gentle = ShortArray(400) { if (it in 40..47) 300 else 0 }
        quietHub.publish(gentle, gentle.size, 8_000, "UNPROCESSED", "gentle", 0)
        check(quietArrived.await(2, TimeUnit.SECONDS))
        check(quietSession.fixedThresholdRawPcm == 250)
        check(quietSession.detectedEvents == 1L)
        check(gentle[42].toInt() == 300)
        println("PASS: configurable sensitive session detects quiet fixture with preserved upstream PCM")
    } finally { quietSession.close() }
    println("PASS: Highspeed phase-2 extension self-test complete")
}
