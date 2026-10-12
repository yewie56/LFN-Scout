package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.log10

class WaterfallAggregatorTest {
    @Test fun currentUsesLastFftAndFlushes() {
        val a = WaterfallAggregator()
        assertNull(a.take(WaterfallMode.CURRENT))
        a.accept(doubleArrayOf(-100.0, -80.0))
        a.accept(doubleArrayOf(-70.0, -90.0))
        assertArrayEquals(doubleArrayOf(-70.0, -90.0), a.take(WaterfallMode.CURRENT)!!, 0.0)
        assertEquals(0, a.pendingFrames)
        assertNull(a.take(WaterfallMode.CURRENT))
    }

    @Test fun averageUsesLinearPowerNotDbArithmetic() {
        val a = WaterfallAggregator()
        a.accept(doubleArrayOf(-60.0, -90.0))
        a.accept(doubleArrayOf(-80.0, -90.0))
        val row = a.take(WaterfallMode.AVERAGE)!!
        assertEquals(10 * log10((1e-6 + 1e-8) / 2), row[0], 1e-8)
        assertEquals(-90.0, row[1], 1e-8)
        assertTrue(row[0] > -70.0) // Average of dB values would incorrectly yield -70.
    }

    @Test fun maximumIsBinwiseForEntireRowInterval() {
        val a = WaterfallAggregator()
        a.accept(doubleArrayOf(-100.0, -70.0, -92.0))
        a.accept(doubleArrayOf(-60.0, -90.0, -85.0))
        a.accept(doubleArrayOf(-75.0, -71.0, -99.0))
        assertArrayEquals(doubleArrayOf(-60.0, -70.0, -85.0), a.take(WaterfallMode.MAXIMUM)!!, 0.0)
    }

    @Test fun intervalsDoNotLeakAcrossRowsOrModeChange() {
        val a = WaterfallAggregator()
        a.accept(doubleArrayOf(-30.0))
        a.clear()
        a.accept(doubleArrayOf(-93.0))
        assertArrayEquals(doubleArrayOf(-93.0), a.take(WaterfallMode.MAXIMUM)!!, 0.0)
        a.accept(doubleArrayOf(-82.0))
        assertArrayEquals(doubleArrayOf(-82.0), a.take(WaterfallMode.AVERAGE)!!, 1e-8)
    }

    @Test fun inputIsCopiedAndDifferentBinCountStartsNewInterval() {
        val a = WaterfallAggregator()
        val first = doubleArrayOf(-70.0)
        a.accept(first)
        first[0] = -10.0
        assertArrayEquals(doubleArrayOf(-70.0), a.take(WaterfallMode.CURRENT)!!, 0.0)
        a.accept(doubleArrayOf(-50.0))
        a.accept(doubleArrayOf(-60.0,-80.0))
        assertArrayEquals(doubleArrayOf(-60.0,-80.0), a.take(WaterfallMode.MAXIMUM)!!,0.0)
    }
}
