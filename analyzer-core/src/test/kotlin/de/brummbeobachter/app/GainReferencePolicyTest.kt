package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class GainReferencePolicyTest {
    @Test fun plus50DbIsSubtractedExactlyOnce() {
        assertEquals(-71.66, GainReferencePolicy.compensateDb(-21.66, 50.0), 1.0e-9)
        assertEquals(0.0031622776601683794, GainReferencePolicy.amplitudeFactor(50.0), 1.0e-15)
    }
}
