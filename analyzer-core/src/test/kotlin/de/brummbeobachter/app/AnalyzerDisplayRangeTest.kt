package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class AnalyzerDisplayRangeTest {
    private fun spectrum(db: DoubleArray, hz: DoubleArray = doubleArrayOf(0.0,5.0,10.0,20.0,150.0))
        = LiveAnalyzerCore.Spectrum(1,4096,1000,4096,hz,db,db.copyOf(),db.copyOf(),false)

    @Test fun boundsUseValidFrequencyBinsAndFixedDbNotPercent() {
        val collector = AnalyzerDisplayRange(5)
        collector.accept(spectrum(doubleArrayOf(-240.0,-105.0,-70.0,-80.0,-15.0)),100)
        assertEquals(-110 to -65,collector.result())
        assertEquals(1,collector.acceptedFrames)
    }

    @Test fun invalidSamplesAreNeverSavedAsDisplayRange() {
        val collector = AnalyzerDisplayRange(5)
        collector.accept(spectrum(DoubleArray(5) {-240.0}),100)
        assertNull(collector.result())
    }

    @Test fun selectedFrequencyAndConfigurableMarginStillApply() {
        val collector = AnalyzerDisplayRange(3)
        collector.accept(spectrum(doubleArrayOf(-240.0,-100.2,-90.1,-85.1,-50.0)),10)
        assertEquals(-104 to -87,collector.result())
    }

    @Test fun isolatedSpectralDipsAndPeaksAreNotHardBounds() {
        val hz = DoubleArray(101) { it.toDouble() }
        val db = DoubleArray(101) { -88.0 }
        db[0] = -240.0 // DC is never used
        db[13] = -170.0; db[31] = -150.0 // two narrow, deep notches
        db[48] = -35.0 // one narrow spike
        val collector = AnalyzerDisplayRange(5)
        collector.accept(spectrum(db,hz),100)
        assertEquals(-93 to -83,collector.result())
    }

    @Test fun shortOutlierFramesDoNotDominateTwoSecondCapture() {
        val hz = DoubleArray(101) { it.toDouble() }
        val collector = AnalyzerDisplayRange(5)
        repeat(20) { frame ->
            val db = DoubleArray(101) { if (frame == 0) -150.0 else -80.0 }
            db[0] = -240.0
            collector.accept(spectrum(db,hz),100)
        }
        assertEquals(20,collector.acceptedFrames)
        assertEquals(-85 to -75,collector.result())
    }

    @Test fun ticksContainNonRoundAutorangeEndpoints() {
        assertEquals(listOf(-109,-100,-90,-80,-70,-61),AnalyzerDisplayTicks.db(-109,-61))
        assertEquals(listOf(-120,-100,-80,-60),AnalyzerDisplayTicks.db(-120,-60))
    }

    @Test fun resettingAverageRetainsFftWindow() {
        val core=LiveAnalyzerCore(1000,4096,100,200.0)
        core.acceptNormalized(DoubleArray(4100) { 0.01 })
        core.resetAveraging()
        val next=core.acceptNormalized(DoubleArray(100) { 0.01 })
        assertTrue("FFT must not warm up again", next.isNotEmpty())
        assertArrayEquals(next.last().liveDbFs,next.last().averageDbFs,1e-8)
    }
}
