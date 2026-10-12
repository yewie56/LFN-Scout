package de.brummbeobachter.app

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs

object ReminderScheduleGridPolicy {

    private const val GRID_THRESHOLD_MINUTES = 30
    private const val HALF_HOUR_MINUTES = 30
    private const val GRID_OFFSET_MINUTES = 15

    fun nextReminderEpochMs(
        baseEpochMs: Long,
        intervalMinutes: Int,
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long {
        val normalizedInterval =
            ReminderPolicy.normalizeIntervalMinutes(
                intervalMinutes
            )

        val targetEpochMs =
            baseEpochMs +
                normalizedInterval * 60L * 1000L

        if (normalizedInterval < GRID_THRESHOLD_MINUTES) {
            return targetEpochMs
        }

        val target =
            Calendar.getInstance(timeZone).apply {
                timeInMillis = targetEpochMs
            }

        val lower =
            nearestLowerGridSlot(
                target,
                timeZone
            )

        val upper =
            Calendar.getInstance(timeZone).apply {
                timeInMillis = lower.timeInMillis
                add(
                    Calendar.MINUTE,
                    HALF_HOUR_MINUTES
                )
            }

        val lowerDistance =
            abs(
                targetEpochMs -
                    lower.timeInMillis
            )
        val upperDistance =
            abs(
                upper.timeInMillis -
                    targetEpochMs
            )

        return if (lowerDistance < upperDistance) {
            lower.timeInMillis
        } else {
            // Bei genau gleicher Entfernung den späteren Slot verwenden.
            upper.timeInMillis
        }
    }

    private fun nearestLowerGridSlot(
        target: Calendar,
        timeZone: TimeZone
    ): Calendar {
        val result =
            Calendar.getInstance(timeZone).apply {
                timeInMillis = target.timeInMillis
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

        val minute =
            result.get(Calendar.MINUTE)

        when {
            minute < GRID_OFFSET_MINUTES -> {
                result.add(
                    Calendar.HOUR_OF_DAY,
                    -1
                )
                result.set(
                    Calendar.MINUTE,
                    GRID_OFFSET_MINUTES +
                        HALF_HOUR_MINUTES
                )
            }

            minute <
                GRID_OFFSET_MINUTES +
                    HALF_HOUR_MINUTES -> {
                result.set(
                    Calendar.MINUTE,
                    GRID_OFFSET_MINUTES
                )
            }

            else -> {
                result.set(
                    Calendar.MINUTE,
                    GRID_OFFSET_MINUTES +
                        HALF_HOUR_MINUTES
                )
            }
        }

        return result
    }
}
