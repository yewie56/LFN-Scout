package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

class LiveAnalyzerCoreTest {
    private fun tone(hz: Double, samples: Int, rate: Int = 1000, amplitude: Double = 0.5): ShortArray =
        ShortArray(samples) { i -> (32767.0 * amplitude * sin(2.0 * PI * hz * i / rate))
            .roundToInt().coerceIn(-32768, 32767).toShort() }

    @Test fun toneAt10HzPeaksAt10HzAndCorrectAmplitude() {
        val core = LiveAnalyzerCore(1000, 1024, 256)
        val spectra = core.accept(tone(10.0, 8192)).last()
        val peakIndex = spectra.liveDbFs.indices.maxByOrNull { spectra.liveDbFs[it] }!!
        assertEquals(10.0, spectra.frequenciesHz[peakIndex], core.frequencyResolutionHz)
        assertEquals(-6.02, spectra.liveDbFs[peakIndex], 1.3)
        assertEquals(100.0, spectra.frequenciesHz.last(), 0.99)
    }

    @Test fun lfRange200Shows180HzWithoutTouchingLegacyRecorder() {
        val core = LiveAnalyzerCore(1000, 4096, 512, 200.0)
        val s = core.accept(tone(180.0, 8192)).last()
        val peak = s.liveDbFs.indices.maxByOrNull { s.liveDbFs[it] }!!
        assertEquals(180.0, s.frequenciesHz[peak], core.frequencyResolutionHz)
        assertEquals(200.0, s.frequenciesHz.last(), 0.25)
        assertTrue(s.liveDbFs.all { it.isFinite() })
    }

    @Test fun chunkingPreservesFinalSpectrumAndSequence() {
        val pcm = tone(32.0, 8192)
        val a = LiveAnalyzerCore(1000, 1024, 256)
        val b = LiveAnalyzerCore(1000, 1024, 256)
        val all = a.accept(pcm).last()
        for (offset in pcm.indices step 37) {
            val size = minOf(37, pcm.size - offset)
            b.accept(pcm, offset, size)
        }
        // Single-element tail may not emit; both cores have identical internal position and sequence.
        val aTail = a.accept(tone(32.0, 256)).last()
        val bTail = b.accept(tone(32.0, 256)).last()
        assertEquals(aTail.sequence, bTail.sequence)
        assertArrayEquals(aTail.liveDbFs, bTail.liveDbFs, 1e-10)
        assertTrue(all.sequence > 0)
    }

    @Test fun silenceNoiseFloorFiniteAndClippingReported() {
        val core = LiveAnalyzerCore(1000, 1024, 256)
        assertTrue(core.accept(ShortArray(1023)).isEmpty())
        assertTrue(core.accept(shortArrayOf(Short.MAX_VALUE)).isNotEmpty())
        val result = core.accept(ShortArray(256)).last()
        assertTrue(result.liveDbFs.all { it.isFinite() })
        assertFalse(result.inputClipped)
    }

    @Test fun invalidConfigurationAndRangeRejected() {
        assertThrows(IllegalArgumentException::class.java) { LiveAnalyzerCore(1000, 1000) }
        assertThrows(IllegalArgumentException::class.java) { LiveAnalyzerCore(1000, 1024, 0) }
        assertThrows(IllegalArgumentException::class.java) { LiveAnalyzerCore(1000, 1024, 256, 600.0) }
    }

    @Test fun resetMaxHoldAndResetAll() {
        val core = LiveAnalyzerCore(1000, 1024, 256)
        val before = core.accept(tone(20.0, 2048)).last()
        val idx = before.frequenciesHz.indices.minByOrNull { kotlin.math.abs(before.frequenciesHz[it] - 20.0) }!!
        core.resetMaxHold()
        val after = core.accept(ShortArray(1024)).last()
        assertTrue(after.maxHoldDbFs[idx] < before.maxHoldDbFs[idx])
        core.reset()
        assertTrue(core.accept(ShortArray(100)).isEmpty())
    }
}
