package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class MonInteractionPolicyTest {
    @Test fun agcIsFullyBypassedBelowOneHundredHz() {
        assertFalse(MonInteractionPolicy.agcApplicable(99.999))
        assertFalse(MonInteractionPolicy.agcApplicable(Double.NaN))
        assertTrue(MonInteractionPolicy.agcApplicable(100.0))
        assertTrue(MonInteractionPolicy.agcApplicable(300.0))
    }
    @Test fun pinchKeepsCenterWithinBothFrequencyEdges() {
        assertEquals(20.0, MonInteractionPolicy.pinchedWidth(10.0, 8.0, 110.0, 100.0, 500.0), 1e-9)
        assertEquals(2.5, MonInteractionPolicy.pinchedWidth(10.0, 0.25, 110.0, 100.0, 500.0), 1e-9)
        assertEquals(0.5, MonInteractionPolicy.pinchedWidth(10.0, 0.001, 110.0, 100.0, 500.0), 1e-9)
        assertEquals(10.0, MonInteractionPolicy.pinchedWidth(10.0, Double.NaN, 110.0, 100.0, 500.0), 1e-9)
    }
}
