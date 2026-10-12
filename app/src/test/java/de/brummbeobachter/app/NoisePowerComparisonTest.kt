package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class NoisePowerComparisonTest {
    @Test fun ratioIsZeroForIdenticalPowers() {
        assertEquals("+0.0", NoisePowerComparison.ratioDb(1e-8, 1e-8))
    }
    @Test fun referenceTenTimesPowerGivesPlusTenDb() {
        assertEquals("+10.0", NoisePowerComparison.ratioDb(1e-7, 1e-8))
    }
    @Test fun zeroResidualIsNotDisplayedAsMinus180Measurement() {
        assertEquals("-Inf", NoisePowerComparison.db(0.0))
    }
    @Test fun invalidAndZeroInputsAreDistinguished() {
        assertEquals("ungueltig", NoisePowerComparison.db(Double.NaN))
        assertEquals("unbestimmt", NoisePowerComparison.ratioDb(0.0, 0.0))
    }
}
