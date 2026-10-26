package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

/** Regression for LFN-FILTER-01: 8-kHz PCM frame must not yield only one filter callback. */
class FilterTimingRegressionTest {
    @Test fun fortyBandsProduceSeveralResultsPer256msFrame() {
        for (bands in intArrayOf(10, 20, 40)) {
            val bank = FastFilterBank(FastFilterBank.Config(0.0, 200.0, bands))
            val samples = ShortArray(2048) { 100.toShort() }
            var callbacks = 0
            for (start in samples.indices step 400) {
                val levels = bank.accept(samples.copyOfRange(start, minOf(start + 400, samples.size)))
                if (levels != null) {
                    callbacks++
                    assertEquals(bands, levels.absoluteDbFs.size)
                    assertTrue(levels.absoluteDbFs.all { it.isFinite() })
                }
            }
            assertEquals("50-ms callbacks for 2048 PCM samples", 6, callbacks)
        }
    }

    @Test fun transientPeakPersistsInSegmentedFilterOutputs() {
        val bank = FastFilterBank(FastFilterBank.Config(0.0, 200.0, 40))
        val samples = ShortArray(2400)
        for (i in 810 until 850) samples[i] = 24000.toShort()
        val peak = ArrayList<Double>()
        for (start in samples.indices step 400) {
            val levels = requireNotNull(bank.accept(samples.copyOfRange(start, start + 400)))
            peak.add(levels.absoluteDbFs.maxOrNull()!!)
        }
        assertTrue("Impulse must change filter energy", peak.maxOrNull()!! > peak.first() + 20.0)
    }
}
