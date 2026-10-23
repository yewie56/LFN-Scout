package de.brummbeobachter.app

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/** Regression: Direct decimation 1 must never be applied to the rolling 8-kHz controller. */
class AnalyzerModeSetupGuardTest {
    private fun draft(factor: Int, size: Int, hop: Int, range: Int = 200): JSONObject =
        JSONObject().put("decimation_factor", factor).put("min_frequency", 0)
            .put("range", range).put("fft_size", size).put("fft_hop", hop)
            .put("direct_sample_rate", 48000)

    @Test fun directDraftIsRejectedForRolling() {
        val direct = draft(1, 8192, 4800)
        assertFalse(AnalyzerModeSetupGuard.compatible(direct, false))
        assertTrue(AnalyzerModeSetupGuard.reason(direct, false).contains("decimation_factor=1"))
    }

    @Test fun existingFftTestIsCompatibleWithRolling() {
        assertTrue(AnalyzerModeSetupGuard.compatible(draft(8, 4096, 200), false))
    }

    @Test fun outOfRangeRollingFrequencyIsRejected() {
        assertFalse(AnalyzerModeSetupGuard.compatible(draft(8, 4096, 200, 10000), false))
    }

    @Test fun directDraftIsValidForDirect() {
        assertTrue(AnalyzerModeSetupGuard.compatible(draft(1, 8192, 4800), true))
    }
}
