package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class NoiseModelReferenceCacheTest {
    @Test fun reusesIdenticalReferenceAndParameters() {
        val cache = NoiseModelReferenceCache()
        val input = doubleArrayOf(1.0, 2.0)
        var fits = 0
        fun lookup() = cache.get(input, 1000, 1024) { fits++; doubleArrayOf(3.0) }
        val first = lookup()
        val second = lookup()
        assertFalse(first.second)
        assertTrue(second.second)
        assertSame(first.first, second.first)
        assertEquals(1, fits)
    }

    @Test fun invalidatesForNewProfileOrFrequencyGrid() {
        val cache = NoiseModelReferenceCache()
        val old = doubleArrayOf(1.0)
        var fits = 0
        fun lookup(source: DoubleArray, fs: Int, fft: Int) =
            cache.get(source, fs, fft) { fits++; source.copyOf() }
        lookup(old, 1000, 1024)
        assertFalse(lookup(old, 2000, 1024).second)
        assertFalse(lookup(old, 2000, 2048).second)
        assertFalse(lookup(doubleArrayOf(1.0), 2000, 2048).second)
        cache.clear()
        assertFalse(lookup(old, 1000, 1024).second)
        assertEquals(5, fits)
    }

    @Test fun unavailableModelIsCachedUntilReset() {
        val cache = NoiseModelReferenceCache()
        val source = doubleArrayOf(1.0)
        var calls = 0
        fun lookup() = cache.get(source, 1000, 1024) { calls++; null }
        assertNull(lookup().first)
        assertTrue(lookup().second)
        assertEquals(1, calls)
        cache.clear()
        assertFalse(lookup().second)
        assertEquals(2, calls)
    }
}
