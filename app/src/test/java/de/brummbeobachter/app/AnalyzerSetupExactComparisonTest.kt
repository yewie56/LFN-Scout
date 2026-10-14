package de.brummbeobachter.app

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AnalyzerSetupExactComparisonTest {
    @Test fun numericRepresentationDoesNotChangeValue() {
        assertTrue(AnalyzerSetupExactComparison.same(10, 10.0))
        assertFalse(AnalyzerSetupExactComparison.same(10, 11))
        assertFalse(AnalyzerSetupExactComparison.same(1, "1"))
    }

    @Test fun largeNumbersNeverRoundIntoEquality() {
        assertFalse(AnalyzerSetupExactComparison.same(9007199254740992L, 9007199254740993L))
    }

    @Test fun missingExtraOrNullKeysRejectFullEquality() {
        val a = JSONObject().put("fft_size", 4096)
        val b = JSONObject().put("fft_size", 4096).put("range", 100)
        assertFalse(AnalyzerSetupExactComparison.sameComplete(a, b))
        assertFalse(AnalyzerSetupExactComparison.sameComplete(b, a))
        assertFalse(AnalyzerSetupExactComparison.sameComplete(a, JSONObject().put("fft_size", JSONObject.NULL)))
        assertTrue(AnalyzerSetupExactComparison.sameComplete(a, JSONObject().put("fft_size", 4096.0)))
    }

    @Test fun preflightRejectsIncompleteSnapshotAndUnknownDifferences() {
        val saved = JSONObject().put("fft_size", 4096)
        val live = JSONObject().put("fft_size", 4096).put("range", 100)
        val report = AnalyzerSetupSwitchPreflight.assess(saved, live, false, false)
        assertTrue(report.missingOrNull.contains("range"))
        assertTrue(report.changed.isEmpty())
    }
}
