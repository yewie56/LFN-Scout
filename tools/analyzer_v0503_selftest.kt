import de.brummbeobachter.app.*
import kotlin.math.*

private fun checkTest(label: String, ok: Boolean) {
    check(ok) { "FAIL: $label" }
    println("PASS: $label")
}

private fun sine(hz: Double, durationSeconds: Double = 2.0): ShortArray =
    ShortArray((8000 * durationSeconds).toInt()) { i ->
        (10000 * sin(2.0 * PI * hz * i / 8000.0)).toInt().toShort()
    }

private fun rms(d: DoubleArray): Double = sqrt(d.drop(d.size / 2).map { it * it }.average())

fun main() {
    var assertions = 0
    for (factor in AnalyzerTiming.allowedDecimationFactors) {
        val rate = AnalyzerTiming.lfRateHz(factor)
        val filter = RawLfDecimator(outputRateHz = rate)
        val signal = sine(50.0)
        val full = filter.accept(signal)
        val chunked = RawLfDecimator(outputRateHz = rate)
        val a = chunked.accept(signal.copyOfRange(0, 4777))
        val b = chunked.accept(signal.copyOfRange(4777, signal.size))
        checkTest("$factor:1 chunk continuity", full.contentEquals(a + b));assertions++
        val wanted = rms(full)
        checkTest("$factor:1 passband 50 Hz $wanted", wanted in 0.195..0.24);assertions++
        val near200 = rms(RawLfDecimator(outputRateHz=rate).accept(sine(195.0)))
        checkTest("$factor:1 195 Hz passband $near200", near200 in 0.16..0.24);assertions++
        val alias = rms(RawLfDecimator(outputRateHz=rate).accept(sine(rate + 100.0)))
        checkTest("$factor:1 alias +100 Hz suppressed $alias", alias < wanted / 50.0);assertions++
        val hopMs = AnalyzerTiming.frameIntervalMs(5, rate)
        checkTest("$factor:1 dynamic hop time", hopMs == ceil(5000.0 / rate).toInt());assertions++
    }
    checkTest("statistics valid custom range/window", AnalyzerTiming.validStatistics(7, 173, 237, 5.5));assertions++
    checkTest("statistics invalid reversed range", !AnalyzerTiming.validStatistics(100, 50, 120, 5.0));assertions++
    checkTest("statistics average cannot exceed window", !AnalyzerTiming.validStatistics(2, 25, 10, 12.0));assertions++
    checkTest("statistic frames at 500 Hz", AnalyzerTiming.statisticFrames(5, 120, 500) == 12000);assertions++
    val t = AnalyzerTiming.advanceWaterfall(0, 5, 250, 500)
    checkTest("waterfall timebase units", t.first == 5000 && !t.second);assertions++
    println("ANALYZER v0.5.03 HOST SELFTEST PASS $assertions/$assertions")
}
