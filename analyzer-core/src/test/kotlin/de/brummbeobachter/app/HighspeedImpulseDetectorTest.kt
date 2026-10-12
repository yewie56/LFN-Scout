package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class HighspeedImpulseDetectorTest {
    @Test fun detectHalfMillisecondAcrossChunks() {
        val detector = HighspeedImpulseDetector(8_000)
        val pcm = ShortArray(400) { if (it in 17..20) 20_000 else 0 }
        assertTrue(detector.accept(pcm.copyOfRange(0, 19)).isEmpty())
        val event = detector.accept(pcm.copyOfRange(19, pcm.size)).single()
        assertEquals(20_000, event.peakPcm)
        assertEquals(0.5, event.durationMs, 1e-8)
        assertEquals(17L, event.onsetSampleIndex)
    }
    @Test fun negativeClip48kAndSilence() {
        val raw = ShortArray(480) { if (it in 50..73) (-30_000).toShort() else 0 }
        assertEquals(0.5, HighspeedImpulseDetector(48_000).accept(raw).single().durationMs, 1e-8)
        assertTrue(HighspeedImpulseDetector(8_000).accept(ShortArray(800)).isEmpty())
        val clip = ShortArray(80) { if (it == 10) Short.MIN_VALUE else 0 }
        assertEquals(1L, HighspeedImpulseDetector(8_000).accept(clip).single().clippedSamples)
    }
    @Test fun gainEquivalentRawThresholdDetectsPreviouslyQuietPulse() {
        val quietPulse = ShortArray(500) { if (it in 30..37) 200 else 0 }
        assertTrue(HighspeedImpulseDetector(8_000).accept(quietPulse).isEmpty())
        val gained = HighspeedImpulseDetector(8_000)
        gained.minPeakPcm = (1400 / 10.0).toInt() // +20 dB, fixed threshold in raw PCM
        val event = gained.accept(quietPulse).single()
        assertEquals(200, event.peakPcm) // raw magnitude preserved, no gain/clipping
        assertEquals(1.0, event.durationMs, 1e-8)
    }
    @Test fun gainDoesNotBypassAdaptiveNoiseFloor() {
        val noisy = ShortArray(8_000) { if (it % 2 == 0) 200 else -200 }
        val gained = HighspeedImpulseDetector(8_000)
        assertTrue(gained.accept(noisy).isEmpty()) // adapt baseline under old 1400 floor
        gained.minPeakPcm = 1  // +50 dB equivalent floor
        assertTrue(gained.accept(noisy).isEmpty())
        assertTrue(gained.lastEffectiveThresholdPcm > 200.0)
    }
    @Test fun thresholdReconfigurationIsValidated() {
        val detector = HighspeedImpulseDetector(8_000)
        detector.minPeakPcm = 4
        assertEquals(4, detector.minPeakPcm)
        try { detector.minPeakPcm = 0; fail("must reject nonpositive threshold") }
        catch (_: IllegalArgumentException) { }
    }
    @Test fun profilesHaveReproducibleThresholds() {
        assertEquals(1400, ImpulseSensitivityPreset.ROBUST.withGain(0).fixedThresholdRawPcm())
        assertEquals(700, ImpulseSensitivityPreset.NORMAL.withGain(0).fixedThresholdRawPcm())
        assertEquals(250, ImpulseSensitivityPreset.SENSITIVE.withGain(0).fixedThresholdRawPcm())
        assertEquals(25, ImpulseSensitivityPreset.SENSITIVE.withGain(20).fixedThresholdRawPcm())
    }
    @Test fun manualThresholdBelowPreviousFloorDetectsQuietPulse() {
        val raw = ShortArray(500) { if (it in 30..37) 300 else 0 }
        assertTrue(HighspeedImpulseDetector(8_000).accept(raw).isEmpty())
        val d = HighspeedImpulseDetector(8_000)
        d.minPeakPcm = 250
        d.thresholdOverNoise = 2.5
        assertEquals(300, d.accept(raw).single().peakPcm)
    }
    @Test fun quietSeriesFiveEventsThenSilence() {
        val d = HighspeedImpulseDetector(8_000)
        d.minPeakPcm = 100
        d.thresholdOverNoise = 2.5
        var total = 0
        repeat(5) {
            val pcm = ShortArray(2000) { i -> if (i in 100..107) 400 else 0 }
            total += d.accept(pcm).size
        }
        total += d.accept(ShortArray(8000)).size
        assertEquals(5, total)
    }
    @Test fun adjustableNoiseMultiplierAffectsEffectiveThreshold() {
        val d = HighspeedImpulseDetector(8_000)
        d.accept(ShortArray(8_000) { if (it % 2 == 0) 200 else -200 })
        d.minPeakPcm = 1
        d.thresholdOverNoise = 6.0
        d.accept(ShortArray(1) { 0 })
        val high = d.lastEffectiveThresholdPcm
        d.thresholdOverNoise = 2.0
        d.accept(ShortArray(1) { 0 })
        val low = d.lastEffectiveThresholdPcm
        assertTrue(high > low && low > 200.0)
        assertTrue(d.lastNoiseAbsPcm > 100.0)
    }
    @Test fun sensitivityParametersRejectInvalidInput() {
        try { ImpulseSensitivity(noiseMultiplier = 0.0); fail("invalid noise ratio") }
        catch (_: IllegalArgumentException) { }
        try { ImpulseSensitivity(gainDb = 60); fail("invalid gain") }
        catch (_: IllegalArgumentException) { }
        try { ImpulseSensitivity(minPeakPcm = 0); fail("invalid minimum") }
        catch (_: IllegalArgumentException) { }
        try { HighspeedImpulseDetector(8_000).thresholdOverNoise = Double.NaN; fail("NaN") }
        catch (_: IllegalArgumentException) { }
    }
}
