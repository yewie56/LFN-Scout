package de.brummbeobachter.app

import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.*

class RawLfDecimatorTest {
    @Test fun lowLevelSignalSurvivesWithoutSecond16BitRounding() {
        val dec = RawLfDecimator()
        val core = LiveAnalyzerCore(1000,4096,512,200.0)
        var last:LiveAnalyzerCore.Spectrum?=null
        for (start in 0 until 64000 step 257) {
            val samples = ShortArray(min(257,64000-start)) {i ->
                (2.0*sin(2*PI*47.0*(i+start)/8000.0)).toInt().toShort()
            }
            core.acceptNormalized(dec.accept(samples)).lastOrNull()?.let{last=it}
        }
        val s=last!!
        val bin=s.frequenciesHz.indices.minByOrNull {abs(s.frequenciesHz[it]-47.0)}!!
        assertTrue(s.liveDbFs[bin]>-105.0)
    }
    @Test fun highFrequencyAliasingIsSuppressed() {
        val dec=RawLfDecimator()
        val samples=ShortArray(80000) {i-> (8000*sin(2*PI*2500*i/8000.0)).toInt().toShort()}
        val filtered=dec.accept(samples)
        val rms=sqrt(filtered.map {it*it}.average())*32768.0
        assertTrue("Alias $rms PCM",rms<4.0)
    }
    @Test fun zeroInputProducesZeroAndNotPlausibleLevels() {
        val dec=RawLfDecimator()
        val core=LiveAnalyzerCore(1000,4096,512,200.0)
        // 8200 input samples (~1 s after 8:1 decimation) are not yet enough
        // to fill the 4096-sample FFT window: no spectrum is expected.
        val startup=dec.accept(ShortArray(8200))
        assertTrue(startup.all {it==0.0})
        assertTrue(core.acceptNormalized(startup).isEmpty())
        // Continue the same stream until at least one complete FFT window exists.
        val ready=dec.accept(ShortArray(32800))
        assertTrue(ready.all {it==0.0})
        val spectra=core.acceptNormalized(ready)
        assertTrue("FFT must emit after 4096 LF samples",spectra.isNotEmpty())
        assertTrue(spectra.last().liveDbFs.all {it<=-180.0})
    }
    @Test fun oldRecorderPcmApiStillWorks() {
        val core=LiveAnalyzerCore(1000)
        assertTrue(core.accept(ShortArray(5000) { i-> (4000*sin(2*PI*35*i/1000.0)).toInt().toShort() }).isNotEmpty())
        assertEquals(1000,core.sampleRateHz)
    }
}
