package de.brummbeobachter.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object ReminderResponseTimeoutScheduler {

    private const val REQUEST_CODE = 4300

    fun schedule(
        context: Context,
        reminderEpochMs: Long,
        responseWindowMinutes: Int,
        eventId: String
    ) {
        val appContext = context.applicationContext
        val alarmManager =
            appContext.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        cancel(appContext, log = false)

        val triggerAt =
            ReminderResponseTimeoutPolicy.timeoutTriggerEpochMs(
                reminderEpochMs,
                responseWindowMinutes
            )

        val pendingIntent =
            timeoutPendingIntent(
                appContext,
                reminderEpochMs,
                responseWindowMinutes,
                eventId
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
            ) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }

        DebugLogger.log(
            appContext,
            "RESPONSE_TIMEOUT_SCHEDULED",
            "for=${DebugLogger.formatEpoch(triggerAt)} " +
                "response_window_min=$responseWindowMinutes"
        )
    }

    fun cancel(
        context: Context,
        log: Boolean = true
    ) {
        val appContext = context.applicationContext
        val alarmManager =
            appContext.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        alarmManager.cancel(
            timeoutPendingIntent(
                appContext,
                0L,
                0,
                ""
            )
        )

        if (log) {
            DebugLogger.log(
                appContext,
                "RESPONSE_TIMEOUT_CANCELLED",
                "reason=answered_or_rescheduled"
            )
        }
    }

    private fun timeoutPendingIntent(
        context: Context,
        reminderEpochMs: Long,
        responseWindowMinutes: Int,
        eventId: String
    ): PendingIntent {
        val intent =
            Intent(
                context,
                ReminderAlarmReceiver::class.java
            ).apply {
                action =
                    ReminderAlarmReceiver.ACTION_RESPONSE_WINDOW_TIMEOUT
                putExtra(
                    ReminderAlarmReceiver.EXTRA_REMINDER_EPOCH_MS,
                    reminderEpochMs
                )
                putExtra(
                    ReminderAlarmReceiver.EXTRA_RESPONSE_WINDOW_MINUTES,
                    responseWindowMinutes
                )
                putExtra(
                    ReminderAlarmReceiver.EXTRA_TIMEOUT_EVENT_ID,
                    eventId
                )
            }

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )
    }
}
