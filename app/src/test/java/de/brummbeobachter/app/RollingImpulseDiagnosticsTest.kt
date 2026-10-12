package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class RollingImpulseDiagnosticsTest {
    @Test fun constantSignalHasNoLevelGap() {
        val d = RollingImpulseDiagnostics()
        val start = 1_800_000L
        repeat(100) { d.accept(start + it * 1000, -70.0) }
        val result = d.finish(start)
        assertEquals(100, result.count)
        assertEquals(-70.0, result.peakSecondDbfs!!, 1e-8)
        assertEquals(1.0, result.topOneEnergyPercent!!, 1e-8)
        assertEquals(10.0, result.topTenEnergyPercent!!, 1e-8)
        assertEquals(0.0, result.leqMinusL50Db!!, 1e-8)
    }
    @Test fun oneSpikeDominatesEnergy() {
        val d = RollingImpulseDiagnostics()
        val start = 3_600_000L
        repeat(99) { d.accept(start + it * 1000, -80.0) }
        d.accept(start + 99_000L, -20.0)
        val result = d.finish(start)
        assertTrue(result.topOneEnergyPercent!! > 99.9)
        assertTrue(result.leqMinusL50Db!! > 35.0)
    }
    @Test fun bucketMismatchCannotReuseOldValues() {
        val d = RollingImpulseDiagnostics()
        d.accept(1_800_000L, -30.0)
        assertEquals(0, d.finish(3_600_000L).count)
    }
}
