package de.brummbeobachter.app

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class SeismicScheliPolicyTest {
    @Test
    fun scheli_geometry_matches_48_half_hour_rows() {
        assertEquals(48, SeismicPlotUiPolicy.SCHELI_ROWS)
        assertEquals(1800, SeismicPlotUiPolicy.SCHELI_ROW_SECONDS)
    }

    @Test
    fun ordered_trace_is_continuous_and_peak_preserving() {
        val bins = listOf(
            SeismicEnvelopeBin(-2f, 5f, first = 1f, last = 2f, minBeforeMax = true),
            SeismicEnvelopeBin(3f, 7f, first = 4f, last = 6f, minBeforeMax = false)
        )
        val trace = SeismicScheliPolicy.orderedTraceValues(bins)
        assertArrayEquals(
            floatArrayOf(1f, -2f, 5f, 2f, 4f, 7f, 3f, 6f),
            trace,
            0f
        )
    }
}
