package de.brummbeobachter.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object ExactAlarmScheduler {
    private const val REQUEST_CODE = 6101

    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        return alarmManager.canScheduleExactAlarms()
    }

    fun scheduleAt(
        context: Context,
        triggerAtEpochMs: Long
    ): Boolean {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingIntent = alarmPendingIntent(context)

        return try {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                DebugLogger.log(
                    context,
                    "EXACT_ALARM_NOT_ALLOWED"
                )
                false
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtEpochMs,
                    pendingIntent
                )

                DebugLogger.log(
                    context,
                    "EXACT_ALARM_SCHEDULED",
                    "for=${DebugLogger.formatEpoch(triggerAtEpochMs)}"
                )
                true
            }
        } catch (e: SecurityException) {
            DebugLogger.log(
                context,
                "EXACT_ALARM_SECURITY_EXCEPTION",
                "message=${e.message ?: ""}"
            )
            false
        }
    }

    fun cancel(context: Context) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(alarmPendingIntent(context))

        DebugLogger.log(
            context,
            "EXACT_ALARM_CANCELLED"
        )
    }

    private fun alarmPendingIntent(context: Context): PendingIntent {
        val intent = Intent(
            context,
            ReminderAlarmReceiver::class.java
        ).apply {
            action = ReminderAlarmReceiver.ACTION_EXACT_REMINDER
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
