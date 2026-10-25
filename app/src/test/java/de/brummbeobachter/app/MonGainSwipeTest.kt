package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class MonGainSwipeTest {
    @Test fun upwardSwipeIncreasesGain() {
        assertEquals(0.0, MonGainSwipe.nextLevel(-30.0, -100.0, 600.0), 1e-9)
    }
    @Test fun downwardSwipeDecreasesGain() {
        assertEquals(-60.0, MonGainSwipe.nextLevel(-30.0, 100.0, 600.0), 1e-9)
    }
    @Test fun bothSurfacesUseIdenticalSensitivity() {
        val start = -24.0
        val delta = -25.0
        val height = 500.0
        // The spectrum bar and waterfall both normalize by the WATERFALL height.
        assertEquals(-15.0, MonGainSwipe.nextLevel(start, delta, height), 1e-9)
    }
    @Test fun limitsAreEnforced() {
        assertEquals(140.0, MonGainSwipe.nextLevel(130.0, -300.0, 600.0), 0.0)
        assertEquals(-60.0, MonGainSwipe.nextLevel(-55.0, 300.0, 600.0), 0.0)
    }
    @Test fun upperLimitIsReachable() {
        assertEquals(140.0, MonGainSwipe.nextLevel(120.0, -100.0, 600.0), 0.0)
    }
    @Test fun invalidHeightDoesNotChangeGain() {
        assertEquals(-24.0, MonGainSwipe.nextLevel(-24.0, 10.0, 0.0), 0.0)
    }
}
