package de.brummbeobachter.app

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val UNIQUE_WORK_NAME = "brummbeobachter_next_reminder"

    fun scheduleNext(
        context: Context,
        delayMillis: Long,
        reason: String
    ): Long {
        val safeDelay = delayMillis.coerceAtLeast(0L)
        val plannedEpochMs = System.currentTimeMillis() + safeDelay

        val request =
            OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(safeDelay, TimeUnit.MILLISECONDS)
                .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )

        DebugLogger.log(
            context,
            "REMINDER_SCHEDULED",
            "for=${DebugLogger.formatEpoch(plannedEpochMs)} reason=$reason"
        )

        return plannedEpochMs
    }

    fun scheduleAfterInterval(
        context: Context,
        intervalMinutes: Int,
        reason: String
    ): Long {
        val normalized = ReminderPolicy.normalizeIntervalMinutes(intervalMinutes)

        return scheduleNext(
            context = context,
            delayMillis = normalized * 60L * 1000L,
            reason = reason
        )
    }

    fun cancel(
        context: Context,
        reason: String
    ) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        DebugLogger.log(
            context,
            "REMINDER_CANCELLED",
            "reason=$reason"
        )
    }
}
