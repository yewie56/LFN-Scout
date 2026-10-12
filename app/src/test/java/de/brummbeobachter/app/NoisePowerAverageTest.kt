package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class NoisePowerAverageTest {
    @Test fun arithmeticLinearPowerAverage() {
        val average = NoisePowerAverage()
        assertTrue(average.add(doubleArrayOf(1.0, 9.0)))
        assertTrue(average.add(doubleArrayOf(3.0, 1.0)))
        assertArrayEquals(doubleArrayOf(2.0, 5.0), average.mean()!!, 1e-12)
        assertEquals(2L, average.acceptedWindows)
    }
    @Test fun invalidWindowDoesNotBiasAnyBin() {
        val average = NoisePowerAverage()
        average.add(doubleArrayOf(2.0, 4.0))
        assertFalse(average.add(doubleArrayOf(Double.NaN, 9.0)))
        assertFalse(average.add(doubleArrayOf(2.0)))
        assertFalse(average.add(doubleArrayOf(-1.0, 1.0)))
        assertArrayEquals(doubleArrayOf(2.0, 4.0), average.mean()!!, 0.0)
        assertEquals(3L, average.rejectedWindows)
    }
    @Test fun emptyAndReset() {
        val average = NoisePowerAverage()
        assertNull(average.mean())
        average.add(doubleArrayOf(5.0))
        average.reset()
        assertNull(average.mean())
        assertEquals(0L, average.acceptedWindows)
    }
}
