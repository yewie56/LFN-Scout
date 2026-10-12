package de.brummbeobachter.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object ReminderTonePackageScheduler {

    private const val REQUEST_CODE_BASE = 4200

    fun scheduleRemaining(
        context: Context,
        responseWindowMinutes: Int
    ) {
        val appContext = context.applicationContext
        val alarmManager =
            appContext.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        cancel(appContext, log = false)

        val now = System.currentTimeMillis()
        val offsets =
            ReminderPolicy.tonePackageOffsetsMillis(
                responseWindowMinutes
            )

        offsets.forEachIndexed { index, offsetMs ->
            if (index == 0) {
                return@forEachIndexed
            }

            val triggerAt = now + offsetMs
            val pendingIntent =
                packagePendingIntent(
                    appContext,
                    index = index,
                    packageCount = offsets.size,
                    offsetMs = offsetMs,
                    responseWindowMinutes = responseWindowMinutes
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
                "ALARM_TONE_PACKAGE_SCHEDULED",
                "package=${index + 1}/${offsets.size} offset_ms=$offsetMs " +
                    "response_window_min=$responseWindowMinutes"
            )
        }
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

        repeat(3) { index ->
            alarmManager.cancel(
                packagePendingIntent(
                    appContext,
                    index = index,
                    packageCount = 3,
                    offsetMs = 0L,
                    responseWindowMinutes = 0
                )
            )
        }

        if (log) {
            DebugLogger.log(
                appContext,
                "ALARM_TONE_PACKAGES_CANCELLED",
                "reason=explicit_cancel"
            )
        }
    }

    private fun packagePendingIntent(
        context: Context,
        index: Int,
        packageCount: Int,
        offsetMs: Long,
        responseWindowMinutes: Int
    ): PendingIntent {
        val intent =
            Intent(
                context,
                ReminderAlarmReceiver::class.java
            ).apply {
                action =
                    ReminderAlarmReceiver.ACTION_ALARM_TONE_PACKAGE
                putExtra(
                    ReminderAlarmReceiver.EXTRA_PACKAGE_INDEX,
                    index + 1
                )
                putExtra(
                    ReminderAlarmReceiver.EXTRA_PACKAGE_COUNT,
                    packageCount
                )
                putExtra(
                    ReminderAlarmReceiver.EXTRA_PACKAGE_OFFSET_MS,
                    offsetMs
                )
                putExtra(
                    ReminderAlarmReceiver.EXTRA_RESPONSE_WINDOW_MINUTES,
                    responseWindowMinutes
                )
            }

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BASE + index,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )
    }
}
