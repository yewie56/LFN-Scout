package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MonAgcPolicyTest {
    @Test fun frequencyWeightingPenalizesHigherComponents() {
        val rate = 48000.0
        fun rmsOfTone(hz: Double): Double {
            val meter = MonAgcPolicy.WeightedMeter(rate)
            val wave = DoubleArray(48000) { i -> 0.02 * kotlin.math.sin(2 * kotlin.math.PI * hz * i / rate) }
            return meter.measure(wave, 100.0, 15.0)
        }
        assertTrue(rmsOfTone(500.0) > rmsOfTone(25.0) * 2.0)
        assertEquals(1.0, MonAgcPolicy.gain(0.1, 0.001), 1e-12)
    }

    @Test fun neverBoosts() {
        assertEquals(1.0, MonAgcPolicy.gain(0.1, 0.01), 1e-12)
        assertEquals(1.0, MonAgcPolicy.gain(0.1, 0.1), 1e-12)
        assertEquals(0.25, MonAgcPolicy.gain(0.1, 0.4), 1e-12)
        assertEquals(1.0, MonAgcPolicy.gain(0.0, 0.4), 1e-12)
    }

    @Test fun attenuatesImmediatelyAndReleasesGradually() {
        val reduced = MonAgcPolicy.nextGain(1.0, 0.25, 0.1, 600.0)
        assertEquals(0.25, reduced, 1e-12)
        val released = MonAgcPolicy.nextGain(reduced, 1.0, 0.1, 600.0)
        assertTrue(released > reduced)
        assertTrue(released < 1.0)
    }
}
