package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineRetrySchedulePolicyTest {

    @Test
    fun retry_interval_is_exactly_two_hours() {
        assertEquals(
            2L * 60L * 60L * 1000L,
            OfflineRetrySchedulePolicy.RETRY_INTERVAL_MS
        )
    }
}
