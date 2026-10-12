package de.brummbeobachter.app

import android.content.Context

object ReminderEngine {

    suspend fun scheduleAt(
        context: Context,
        settingsStore: ReminderSettingsStore,
        settings: ReminderSettings,
        triggerAtEpochMs: Long,
        reason: String
    ): Long {
        cancelAll(context, "replace:$reason")

        val planned =
            if (
                settings.mode == ReminderMode.ALARM &&
                ExactAlarmScheduler.scheduleAt(
                    context,
                    triggerAtEpochMs
                )
            ) {
                DebugLogger.log(
                    context,
                    "REMINDER_ENGINE",
                    "mode=ALARM exact=true reason=$reason"
                )
                triggerAtEpochMs
            } else {
                val result = ReminderScheduler.scheduleNext(
                    context = context,
                    delayMillis = (
                        triggerAtEpochMs -
                            System.currentTimeMillis()
                    ).coerceAtLeast(0L),
                    reason = if (settings.mode == ReminderMode.ALARM) {
                        "alarm_fallback:$reason"
                    } else {
                        reason
                    }
                )

                DebugLogger.log(
                    context,
                    "REMINDER_ENGINE",
                    "mode=${settings.mode.name} exact=false reason=$reason"
                )

                result
            }

        settingsStore.setNextReminderEpochMs(planned)
        return planned
    }

    suspend fun scheduleAfterInterval(
        context: Context,
        settingsStore: ReminderSettingsStore,
        settings: ReminderSettings,
        reason: String
    ): Long {
        val now =
            System.currentTimeMillis()

        val triggerAt =
            ReminderScheduleGridPolicy.nextReminderEpochMs(
                baseEpochMs = now,
                intervalMinutes =
                    settings.intervalMinutes
            )

        DebugLogger.log(
            context,
            "REMINDER_NEXT_TIME_POLICY",
            "interval_min=${settings.intervalMinutes} " +
                "grid=${settings.intervalMinutes > 30} " +
                "planned=${DebugLogger.formatEpoch(triggerAt)}"
        )

        return scheduleAt(
            context,
            settingsStore,
            settings,
            triggerAt,
            reason
        )
    }

    fun cancelAll(
        context: Context,
        reason: String
    ) {
        ReminderScheduler.cancel(
            context,
            reason
        )
        ExactAlarmScheduler.cancel(context)
        ReminderTonePackageScheduler.cancel(
            context,
            log = false
        )
    }
}
