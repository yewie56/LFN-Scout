// Host-only test: compile with kotlinc together with analyzer-core main sources.
import de.brummbeobachter.app.*

fun main() {
    fun spectrum(db: DoubleArray, hz: DoubleArray = doubleArrayOf(0.0,5.0,10.0,20.0,150.0)) =
        LiveAnalyzerCore.Spectrum(1,4096,1000,4096,hz,db,db.copyOf(),db.copyOf(),false)
    val measured = AnalyzerDisplayRange(5)
    measured.accept(spectrum(doubleArrayOf(-240.0,-105.0,-70.0,-80.0,-15.0)),100)
    check(measured.result()==(-110 to -65)) { "fixed ±5 dB: ${measured.result()}" }
    println("PASS 01: fixed ±5-dB margin, DC/high frequencies excluded")
    val invalid = AnalyzerDisplayRange(5)
    invalid.accept(spectrum(DoubleArray(5) {-240.0}),100)
    check(invalid.result()==null)
    println("PASS 02: invalid bins do not persist display boundaries")
    val custom = AnalyzerDisplayRange(3)
    custom.accept(spectrum(doubleArrayOf(-240.0,-100.2,-90.1,-85.1,-50.0)),10)
    check(custom.result()==(-104 to -87))
    println("PASS 03: configurable margin and selected frequency range")
    val hz=DoubleArray(101){it.toDouble()}
    val notch=DoubleArray(101){-88.0}.apply{this[0]=-240.0;this[13]=-170.0;this[31]=-150.0;this[48]=-35.0}
    val robust=AnalyzerDisplayRange(5)
    robust.accept(spectrum(notch,hz),100)
    check(robust.result()==(-93 to -83)) { "isolated frequency outliers: ${robust.result()}" }
    println("PASS 04: isolated frequency dips and spikes do not determine limits")
    val temporal=AnalyzerDisplayRange(5)
    repeat(20){frame->temporal.accept(spectrum(DoubleArray(101){i->if(i==0)-240.0 else if(frame==0)-150.0 else -80.0},hz),100)}
    check(temporal.result()==(-85 to -75)) { "temporal outlier: ${temporal.result()}" }
    println("PASS 05: one anomalous frame does not determine limits")
    check(AnalyzerDisplayTicks.db(-109,-61)==listOf(-109,-100,-90,-80,-70,-61))
    println("PASS 06: variable dB-axis ticks")
    val core=LiveAnalyzerCore(1000,4096,100,200.0)
    core.acceptNormalized(DoubleArray(4100){0.01});core.resetAveraging()
    val next=core.acceptNormalized(DoubleArray(100){0.01})
    check(next.isNotEmpty() && next.last().averageDbFs.zip(next.last().liveDbFs).all{(a,b)->kotlin.math.abs(a-b)<1e-8})
    println("PASS 07: average reset retains FFT cadence")
}
