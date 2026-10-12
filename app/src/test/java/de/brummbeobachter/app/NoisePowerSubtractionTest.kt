package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class NoisePowerSubtractionTest {
    @Test fun equalPowersProduceExactZeroResidual() {
        assertEquals(0.0, NoisePowerSubtraction.residual(0.01, 0.01), 0.0)
        assertEquals(-180.0, NoisePowerSubtraction.toDisplayDb(0.0), 0.0)
    }
    @Test fun weakerThanReferenceReturnsZero() {
        assertEquals(0.0, NoisePowerSubtraction.residual(0.005, 0.01), 0.0)
    }
    @Test fun signalAboveReferenceSurvives() {
        assertEquals(0.03, NoisePowerSubtraction.residual(0.04, 0.01), 1e-12)
        assertEquals(10.0 * kotlin.math.log10(0.03),
            NoisePowerSubtraction.toDisplayDb(0.03), 1e-10)
    }
    @Test fun zeroReferenceLeavesPowerUnchanged() {
        assertEquals(0.02, NoisePowerSubtraction.residual(0.02, 0.0), 0.0)
    }
    @Test(expected=IllegalArgumentException::class)
    fun invalidPowerRejected() { NoisePowerSubtraction.residual(Double.NaN, 0.0) }
}
