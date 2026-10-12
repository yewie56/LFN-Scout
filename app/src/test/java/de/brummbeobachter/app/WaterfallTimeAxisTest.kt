package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class WaterfallTimeAxisTest {
    private val berlin = TimeZone.getTimeZone("Europe/Berlin")
    private val start = 1_789_984_800_000L

    @Test fun intervalsKeepAtMostFiveClockMarks() {
        listOf(120 to 1, 600 to 5, 1200 to 5, 3600 to 20,
            7200 to 30, 14400 to 60).forEach { (duration, step) ->
            assertEquals(step, WaterfallTimeAxis.intervalMinutes(duration))
        }
    }

    @Test fun realClockTicksFollowActualRowPositions() {
        val rows = (0..14400).map { start + it * 1000L }
        val ticks = WaterfallTimeAxis.ticks(rows,14400,14401,1000,berlin)
        assertTrue(ticks.isNotEmpty())
        assertTrue(ticks.size <= 5)
        assertTrue(ticks.zipWithNext().all { it.first.ageRows < it.second.ageRows })
        assertTrue(ticks.all { it.ageRows in 0f..14401f })
        assertEquals(rows.last(), ticks.first().epochMillis)
        assertTrue(ticks.drop(1).all { (it.epochMillis + berlin.getOffset(it.epochMillis)) % 3_600_000L == 0L })
    }

    @Test fun pausedCaptureDoesNotInventIntermediateTimeRows() {
        val rows = (0..60).map { start + it*1000L } +
            (0..60).map { start + 600_000L + it*1000L }
        val ticks = WaterfallTimeAxis.ticks(rows,3600,rows.size,1000,berlin)
        assertTrue(ticks.none { it.ageRows in 60f..61f })
        assertTrue(WaterfallTimeAxis.ticks(emptyList(),120,480,250,berlin).isEmpty())
    }
}
