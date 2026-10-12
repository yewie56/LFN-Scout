package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderTonePackagePolicyTest {

    @Test
    fun three_tone_packages_are_evenly_distributed_over_nine_minute_window() {
        assertEquals(
            listOf(0L, 180_000L, 360_000L),
            ReminderPolicy.tonePackageOffsetsMillis(9)
        )
    }

    @Test
    fun three_tone_packages_are_evenly_distributed_over_five_minute_window() {
        assertEquals(
            listOf(0L, 100_000L, 200_000L),
            ReminderPolicy.tonePackageOffsetsMillis(5)
        )
    }
}
