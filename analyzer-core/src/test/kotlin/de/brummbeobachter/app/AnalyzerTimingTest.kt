package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class AnalyzerTimingTest {
    @Test fun restoredIdleModeNeverCrashesOnOldWidebandFactor() {
        assertEquals(48000, AnalyzerTiming.displayRateHz(true, 1))
        assertEquals(1000, AnalyzerTiming.displayRateHz(false, 1))
        assertEquals(1000, AnalyzerTiming.displayRateHz(false, 0))
        assertEquals(1000, AnalyzerTiming.displayRateHz(false, -1))
        assertEquals(1000, AnalyzerTiming.displayRateHz(false, 8))
        assertEquals(500, AnalyzerTiming.displayRateHz(false, 16))
        assertEquals(2000, AnalyzerTiming.displayRateHz(false, 4))
        assertTrue(runCatching { AnalyzerTiming.lfRateHz(1) }.isFailure)
    }

    @Test fun independentDecimationAndFftHop() {
        assertEquals(1000,AnalyzerTiming.LF_RATE_HZ)
        assertEquals(4096,AnalyzerTiming.FFT_SIZE)
        assertEquals(100,AnalyzerTiming.DEFAULT_FFT_HOP)
        assertEquals(250,AnalyzerTiming.DEFAULT_WATERFALL_INTERVAL_MS)
        assertEquals(100,AnalyzerTiming.frameIntervalMs(100))
        assertEquals(480,AnalyzerTiming.historyRows(250))
        assertEquals(12000,AnalyzerTiming.statisticFrames(10))
        assertEquals(10,AnalyzerTiming.frameIntervalMs(10))
        assertEquals(1200,AnalyzerTiming.statisticFrames(100))
        // Legacy 1-kHz test: the global hop list also contains 48-kHz-only hops.
        // At 1 kHz, 480/960/2400/4800 can produce an interval that is not
        // one of the permitted waterfall intervals; test only LF-valid hops.
        for(hop in intArrayOf(5, 10, 50, 100, 200, 500, 1000)) {
            for(interval in AnalyzerTiming.allowedWaterfallIntervalsMs) {
                val effective=AnalyzerTiming.effectiveWaterfallIntervalMs(hop,interval)
                val capacity=AnalyzerTiming.historyRows(effective)
                assertEquals(120000,capacity*effective)
            }
        }
    }
    @Test fun smallerHopRecomputesWithoutChangingFrequencyBins() {
        fun run(hop:Int):List<LiveAnalyzerCore.Spectrum> {
            val core=LiveAnalyzerCore(1000,4096,hop,200.0)
            return core.acceptNormalized(DoubleArray(8192) { i ->
                0.125*sin(2*PI*50*i/1000.0)
            })
        }
        val slow=run(500)
        val fast=run(10)
        assertTrue(fast.size>slow.size)
        assertEquals(slow.first().frequenciesHz.size,fast.first().frequenciesHz.size)
        assertEquals(slow.first().frequenciesHz[205],fast.first().frequenciesHz[205],0.0)
        val peak=fast.last().liveDbFs.indices.maxByOrNull{fast.last().liveDbFs[it]}!!
        assertTrue(abs(fast.last().frequenciesHz[peak]-50.0)<0.25)
    }
    @Test fun waterfallTimebaseIndependentOfFftHop() {
        for(hop in intArrayOf(5, 10, 50, 100, 200, 500, 1000)) for(interval in AnalyzerTiming.allowedWaterfallIntervalsMs) {
            val effective=AnalyzerTiming.effectiveWaterfallIntervalMs(hop,interval)
            var phase=0
            var emitted=0
            repeat(120000/hop) {
                val (next,due)=AnalyzerTiming.advanceWaterfall(phase,hop,effective)
                phase=next
                if(due) emitted++
            }
            assertEquals("hop=$hop interval=$interval",AnalyzerTiming.historyRows(effective),emitted)
            assertEquals(0,phase)
        }
    }
    @Test fun widebandTimingUsesRealSampleRate() {
        // Wideband hops are specified in 48-kHz samples, not in 1-kHz LF samples.
        for (hop in AnalyzerTiming.allowedWidebandHops) {
            val interval = AnalyzerTiming.effectiveWaterfallIntervalMs(hop, 100, 48000)
            assertTrue(interval in AnalyzerTiming.allowedWaterfallIntervalsMs)
            assertEquals(120000, AnalyzerTiming.historyRows(interval) * interval)
            var phase = 0
            var rows = 0
            repeat(48000 * 120 / hop) {
                val (next, due) = AnalyzerTiming.advanceWaterfall(phase, hop, interval, 48000)
                phase = next
                if (due) rows++
            }
            assertEquals(AnalyzerTiming.historyRows(interval), rows)
            assertEquals(0, phase)
        }
    }

    @Test fun invalidHopsAreRejected() {
        assertTrue(runCatching{AnalyzerTiming.historyRows(33)}.isFailure)
        assertTrue(runCatching{AnalyzerTiming.advanceWaterfall(0,33,250)}.isFailure)
    }
}
