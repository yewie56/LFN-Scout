package de.brummbeobachter.app

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

/** Host regression: MAX reset cannot modify FFT, average, or audio ownership. */
fun main() {
    var passes = 0
    fun test(name: String, block: () -> Boolean) {
        check(block()) { "FAIL: $name" }
        passes++
        println("PASS: $name")
    }
    fun tone(hz: Double, count: Int, amplitude: Double = .45): ShortArray =
        ShortArray(count) { i -> (32767.0 * amplitude * sin(2.0 * PI * hz * i / 1000))
            .roundToInt().coerceIn(-32768, 32767).toShort() }
    val core = LiveAnalyzerCore(1000, 1024, 256, 200.0)
    val first = core.accept(tone(50.0, 2048)).last()
    val idx = first.frequenciesHz.indices.minByOrNull { abs(first.frequenciesHz[it] - 50.0) }!!
    test("spectrum carries max hold") { first.maxHoldDbFs.size == first.liveDbFs.size }
    test("max hold matches initial strong peak") { abs(first.maxHoldDbFs[idx] - first.liveDbFs[idx]) < .1 }
    val lower = core.accept(tone(50.0, 2048, .02)).last()
    test("current decreases on quieter input") { lower.liveDbFs[idx] < first.liveDbFs[idx] - 15.0 }
    test("max hold retains earlier peak") { lower.maxHoldDbFs[idx] >= first.maxHoldDbFs[idx] - .01 }
    val seq = lower.sequence
    core.resetMaxHold()
    val after = core.accept(tone(50.0, 2048, .02)).last()
    test("MAX CLR clears previous peak") { after.maxHoldDbFs[idx] < first.maxHoldDbFs[idx] - 15.0 }
    test("MAX CLR retains FFT sequence") { after.sequence > seq }
    test("MAX CLR does not clear average") { after.averageDbFs[idx] > -240.0 && after.averageDbFs[idx] < first.liveDbFs[idx] }
    test("MAX CLR retains FFT configuration") { core.sampleRateHz == 1000 && core.fftSize == 1024 && core.hopSize == 256 }
    println("ANALYZER v0.5.06 MAX-HOLD HOST SELFTEST PASS $passes/$passes")
}
