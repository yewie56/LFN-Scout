package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class WaterfallStatsPolicyTest {
    @Test
    fun normal_mode_is_always_2_to_25_hz() {
        val range = WaterfallStatsPolicy.effectiveRange(false, 8, 80)
        assertEquals(2.0, range.minHz, 0.0)
        assertEquals(25.0, range.maxHz, 0.0)
    }

    @Test
    fun developer_mode_uses_valid_persisted_range() {
        val range = WaterfallStatsPolicy.effectiveRange(true, 3, 40)
        assertEquals(3.0, range.minHz, 0.0)
        assertEquals(40.0, range.maxHz, 0.0)
    }

    @Test
    fun invalid_developer_range_falls_back_to_2_to_25_hz() {
        val range = WaterfallStatsPolicy.effectiveRange(true, 40, 3)
        assertEquals(2.0, range.minHz, 0.0)
        assertEquals(25.0, range.maxHz, 0.0)
    }
}
