package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class WaterfallLevelPolicyTest {
    @Test fun gainCorrectedManualRangeDoesNotShiftWithRecordingGain() {
        val at0 = WaterfallLevelPolicy.effectiveManualRange(0, -100, -20)
        val at50 = WaterfallLevelPolicy.effectiveManualRange(50, -100, -20)
        assertEquals(-100.0, at0.minDb, 0.0)
        assertEquals(-20.0, at0.maxDb, 0.0)
        assertEquals(at0, at50)
    }
}
