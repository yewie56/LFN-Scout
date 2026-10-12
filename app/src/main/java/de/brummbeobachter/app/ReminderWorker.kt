package de.brummbeobachter.app

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.util.UUID

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        OfflineObservationRetry.flush(
            applicationContext,
            "reminder_worker"
        )

        val store =
            ReminderSettingsStore(applicationContext)

        val settings =
            store.settingsFlow.first()

        val now =
            System.currentTimeMillis()

        val delaySec =
            if (settings.nextReminderEpochMs > 0L) {
                (
                    now -
                        settings.nextReminderEpochMs
                    ) / 1000L
            } else {
                0L
            }

        DebugLogger.log(
            applicationContext,
            "REMINDER_WORKER_STARTED",
            "planned=${DebugLogger.formatEpoch(settings.nextReminderEpochMs)} " +
                "delay_sec=$delaySec mode=${settings.mode.name}"
        )

        if (!settings.enabled) {
            store.setNextReminderEpochMs(0L)
            return Result.success()
        }

        if (settings.pauseUntilEpochMs > now) {
            ReminderEngine.scheduleAt(
                applicationContext,
                store,
                settings,
                settings.pauseUntilEpochMs,
                "worker_pause_until"
            )
            return Result.success()
        }

        ReminderNotifier.show(
            applicationContext,
            requestFullScreen = false
        )

        ReminderEngine.scheduleAfterInterval(
            applicationContext,
            store,
            settings,
            "worker_next_cycle"
        )

        val responseWindowMinutes =
            ReminderPolicy.responseWindowMinutes(
                settings.intervalMinutes
            )

        if (settings.mode == ReminderMode.ALARM) {
            ReminderAlertController.playAlarmTonePackage(
                applicationContext,
                packageIndex = 1,
                packageCount = 3,
                offsetMs = 0L,
                responseWindowMinutes = responseWindowMinutes
            )

            ReminderTonePackageScheduler.scheduleRemaining(
                applicationContext,
                responseWindowMinutes
            )
        }

        ReminderResponseTimeoutScheduler.schedule(
            applicationContext,
            reminderEpochMs = now,
            responseWindowMinutes = responseWindowMinutes,
            eventId = UUID.randomUUID().toString()
        )

        return Result.success()
    }

    companion object {
        const val NOTIFICATION_ID = 4001
    }
}
