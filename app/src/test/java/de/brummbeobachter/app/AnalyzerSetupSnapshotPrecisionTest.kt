package de.brummbeobachter.app

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AnalyzerSetupSnapshotPrecisionTest {
    @Test fun integralFilterBoundariesRemainExact() {
        assertEquals(0, AnalyzerRuntimeSetupSnapshot.exactLegacyFilterHz(0.0))
        assertEquals(100, AnalyzerRuntimeSetupSnapshot.exactLegacyFilterHz(100.0))
        assertEquals(-1, AnalyzerRuntimeSetupSnapshot.exactLegacyFilterHz(-1.0))
    }
    @Test fun fractionalFilterBoundaryIsNeverTruncated() {
        assertFails { AnalyzerRuntimeSetupSnapshot.exactLegacyFilterHz(0.75) }
        assertFails { AnalyzerRuntimeSetupSnapshot.exactLegacyFilterHz(99.999) }
    }
    @Test fun nonFiniteAndOutOfRangeBoundariesFailClosed() {
        assertFails { AnalyzerRuntimeSetupSnapshot.exactLegacyFilterHz(Double.NaN) }
        assertFails { AnalyzerRuntimeSetupSnapshot.exactLegacyFilterHz(Double.POSITIVE_INFINITY) }
        assertFails { AnalyzerRuntimeSetupSnapshot.exactLegacyFilterHz(2147483648.0) }
    }
    @Test fun legacyIntegerFieldsDoNotAcceptFractionalNumbers() {
        assertFails { AnalyzerSetupFormat.validate(JSONObject().put("legacy", JSONObject().put("filter_min_hz", 1.5))) }
        AnalyzerSetupFormat.validate(JSONObject().put("legacy", JSONObject().put("filter_min_hz", 1.0)))
    }
    private fun assertFails(block: () -> Unit) {
        try { block(); fail("Expected validation failure") } catch (_: IllegalArgumentException) { }
    }
}
