import de.brummbeobachter.app.*
import kotlin.math.abs
import kotlin.math.log10

fun main() {
    fun checkClose(actual: Double, expected: Double) = check(abs(actual - expected) < 1e-7) { "$actual != $expected" }
    var passed = 0
    run { // Default semantics: latest FFT, not a time average.
        val a=WaterfallAggregator(); a.accept(doubleArrayOf(-90.0)); a.accept(doubleArrayOf(-65.0))
        checkClose(a.take(WaterfallMode.CURRENT)!![0], -65.0); passed++
    }
    run { // Mean linear power, then logarithm.
        val a=WaterfallAggregator(); a.accept(doubleArrayOf(-60.0)); a.accept(doubleArrayOf(-80.0))
        checkClose(a.take(WaterfallMode.AVERAGE)!![0], 10*log10((1e-6+1e-8)/2)); passed++
    }
    run { // Binwise maximum, even when maxima occur in different FFTs.
        val a=WaterfallAggregator(); a.accept(doubleArrayOf(-60.0,-90.0)); a.accept(doubleArrayOf(-80.0,-70.0))
        val row=a.take(WaterfallMode.MAXIMUM)!!; checkClose(row[0],-60.0);checkClose(row[1],-70.0);passed++
    }
    run { // No samples carried across new time interval.
        val a=WaterfallAggregator(); a.accept(doubleArrayOf(-20.0)); a.take(WaterfallMode.MAXIMUM)
        a.accept(doubleArrayOf(-100.0));checkClose(a.take(WaterfallMode.MAXIMUM)!![0],-100.0);passed++
    }
    run { // Can clear on mode change or source discontinuity.
        val a=WaterfallAggregator();a.accept(doubleArrayOf(-20.0));a.clear();a.accept(doubleArrayOf(-80.0))
        checkClose(a.take(WaterfallMode.AVERAGE)!![0],-80.0);passed++
    }
    run { // Shared frame timings are unchanged by the new aggregator.
        var phase=0;var rows=0
        repeat(1000) { val (next,due)=AnalyzerTiming.advanceWaterfall(phase,10,250); phase=next;if(due)rows++ }
        check(rows == 40); passed++
    }
    println("WATERFALL v0.4.98 SELFTEST PASS $passed/6")
}
