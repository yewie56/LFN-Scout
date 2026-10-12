package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PauseControlsPolicyTest {
    @Test
    fun pause_controls_fit_in_exactly_two_rows_and_include_twelve_hours_and_disable() {
        assertEquals(2, PauseControlsPolicy.rows.size)
        assertEquals(
            listOf(1, 2, 4, 8),
            PauseControlsPolicy.rows[0]
        )
        assertEquals(
            listOf(12, 24, 72, PauseControlsPolicy.DISABLE),
            PauseControlsPolicy.rows[1]
        )
    }
}
