package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class FilterDisplayRangeTest {
    @Test fun noDataAndSilenceCannotInventRange() {
        val range=FilterDisplayRange()
        assertNull(range.result())
        range.accept(doubleArrayOf(-240.0, Double.NaN))
        range.accept(doubleArrayOf(Double.POSITIVE_INFINITY,-240.0))
        assertNull(range.result())
    }
    @Test fun validShortPeakIncludedWhileFloorRemainsRobust() {
        val range=FilterDisplayRange(5)
        repeat(100) { range.accept(doubleArrayOf(-110.0,-98.0,-93.0)) }
        range.accept(doubleArrayOf(-112.0,-94.0,-45.0))
        val (min,max)=requireNotNull(range.result())
        assertTrue(min<=-110)
        assertTrue(max>=-40)
    }
    @Test fun constantSignalHasReadableWidthAndBounds() {
        val range=FilterDisplayRange(0)
        range.accept(doubleArrayOf(-65.0));range.accept(doubleArrayOf(-65.0))
        val (min,max)=requireNotNull(range.result())
        assertTrue(max-min>=10)
        assertTrue(min>=-240 && max<=50)
    }
    @Test fun invalidMarginRejected() {
        assertTrue(runCatching { FilterDisplayRange(31) }.isFailure)
    }
}
