package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderInProcessToneSchedulerTest {

    @Test
    fun offsets_are_one_third_and_two_thirds() {
        assertEquals(
            100_000L,
            ReminderInProcessToneScheduler
                .offsetForPackage(
                    responseWindowMinutes = 5,
                    packageIndex = 2
                )
        )

        assertEquals(
            200_000L,
            ReminderInProcessToneScheduler
                .offsetForPackage(
                    responseWindowMinutes = 5,
                    packageIndex = 3
                )
        )
    }
}
