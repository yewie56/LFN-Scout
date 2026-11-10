package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MonAgcPolicyTest {
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
