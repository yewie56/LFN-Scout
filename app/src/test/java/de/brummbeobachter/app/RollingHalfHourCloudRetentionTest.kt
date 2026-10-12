package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class RollingHalfHourCloudRetentionTest {
    private val day = 24L * 60L * 60L * 1000L

    @Test fun retainsUntilFull24HoursAfterCloudConfirmation() {
        val t = 1_791_750_003_414L
        assertEquals(t + day, RollingHalfHourCloudOutbox.deleteAfterMs(t))
        assertFalse(RollingHalfHourCloudOutbox.isExpired(t, t))
        assertFalse(RollingHalfHourCloudOutbox.isExpired(t, t + day - 1L))
        assertTrue(RollingHalfHourCloudOutbox.isExpired(t, t + day))
    }

    @Test fun rejectsMissingFutureOrOverflowedConfirmation() {
        assertNull(RollingHalfHourCloudOutbox.deleteAfterMs(0L))
        assertNull(RollingHalfHourCloudOutbox.deleteAfterMs(Long.MAX_VALUE))
        assertFalse(RollingHalfHourCloudOutbox.isExpired(0L, Long.MAX_VALUE))
        assertFalse(RollingHalfHourCloudOutbox.isExpired(2000L, 1000L))
        assertFalse(RollingHalfHourCloudOutbox.isExpired(Long.MAX_VALUE, Long.MAX_VALUE))
    }
}
