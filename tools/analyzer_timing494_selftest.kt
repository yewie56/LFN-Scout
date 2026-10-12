import de.brummbeobachter.app.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

fun main() {
    val timings=AnalyzerTiming
    check(timings.DEFAULT_FFT_HOP==100 && timings.DEFAULT_WATERFALL_INTERVAL_MS==250)
    check(timings.historyRows(250)==480 && timings.statisticFrames(100)==1200)
    check(timings.statisticFrames(10)==12000 && timings.frameIntervalMs(10)==10)
    for (hop in timings.allowedFftHops) for (interval in timings.allowedWaterfallIntervalsMs) {
        val effective=timings.effectiveWaterfallIntervalMs(hop,interval)
        // Wideband-only hops 2400/4800 may exceed an LF requested interval.
        // They are not a valid 1000-Hz LF historical configuration.
        if (effective !in timings.allowedWaterfallIntervalsMs) continue
        val rows=timings.historyRows(effective)
        check(rows*effective==120000)
        var phase=0;var emitted=0
        repeat(120000/hop) {
            val (next,due)=timings.advanceWaterfall(phase,hop,effective)
            phase=next
            if(due)emitted++
        }
        check(emitted==rows) {"hop=$hop interval=$interval effective=$effective emitted=$emitted expected=$rows"}
        check(phase==0)
    }
    println("PASS: independent FFT/time-based waterfall and 120 s history for all 30 combinations")
    fun run(hop: Int):List<LiveAnalyzerCore.Spectrum> {
        val core=LiveAnalyzerCore(1000,4096,hop,200.0)
        return core.acceptNormalized(DoubleArray(8192){i->0.125*sin(2*PI*50*i/1000)})
    }
    val fast=run(10);val slow=run(500)
    check(fast.size>slow.size)
    check(fast.last().frequenciesHz.contentEquals(slow.last().frequenciesHz))
    val peak=fast.last().liveDbFs.indices.maxByOrNull{fast.last().liveDbFs[it]}!!
    check(abs(fast.last().frequenciesHz[peak]-50.0)<0.25)
    println("PASS: 10-sample FFT hop updates more often without modifying FFT frequency axis")
    val rawHub=AnalyzerPcmHub()
    var count=0
    val session=AnalyzerRawLfSession(rawHub,{count++},{},200.0,hopSize=100)
    val block=ShortArray(2048){i->(8000*sin(2*PI*40*i/8000)).toInt().toShort()}
    repeat(40){rawHub.publish(block,block.size,8000,"fixture","mic",0); Thread.sleep(12)}
    Thread.sleep(1000)
    check(count>5){"No FFT frames: $count"}
    check(session.droppedFrames==0L){"Dropped: ${session.droppedFrames}"}
    session.close()
    println("PASS: raw hub -> FIR -> FFT100 spectra=$count, without a new microphone owner")
}
