package de.brummbeobachter.app

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.first
import kotlin.math.absoluteValue

object ReminderNotifier {

    suspend fun show(
        context: Context,
        requestFullScreen: Boolean
    ): Boolean {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            DebugLogger.log(
                context,
                "NOTIFICATION_NOT_SHOWN",
                "reason=permission_missing"
            )
            return false
        }

        val settings =
            ReminderSettingsStore(context)
                .settingsFlow
                .first()

        val sound =
            ReminderSoundStore(context)
                .soundFlow
                .first()

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val fullScreenAllowed =
            requestFullScreen &&
                (
                    Build.VERSION.SDK_INT < 34 ||
                        notificationManager.canUseFullScreenIntent()
                    )

        val alarmPackagesSilent =
            settings.mode == ReminderMode.ALARM

        val channelId =
            ensureUrgentChannel(
                context = context,
                soundUri = sound.uri,
                soundTitle = sound.title,
                alarmUsage = settings.mode == ReminderMode.ALARM,
                alarmPackagesSilent = alarmPackagesSilent
            )

        val openIntent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(
                    ReminderActionReceiver.EXTRA_FROM_REMINDER,
                    true
                )
            }

        val openPendingIntent =
            PendingIntent.getActivity(
                context,
                1001,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        val pauseIntent =
            Intent(
                context,
                ReminderActionReceiver::class.java
            ).apply {
                action =
                    ReminderActionReceiver.ACTION_PAUSE_ONE_HOUR
            }

        val pausePendingIntent =
            PendingIntent.getBroadcast(
                context,
                1002,
                pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        val fullScreenIntent =
            Intent(
                context,
                ReminderAlarmActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val fullScreenPendingIntent =
            PendingIntent.getActivity(
                context,
                1003,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        val timeout =
            ReminderPolicy.responseWindowMinutes(
                settings.intervalMinutes
            )

        val builder =
            NotificationCompat.Builder(
                context,
                channelId
            )
                .setSmallIcon(
                    android.R.drawable.ic_lock_idle_alarm
                )
                .setContentTitle(
                    "LFN-SCOUT – JETZT BEWERTEN"
                )
                .setContentText(
                    "Bitte jetzt Brummton bewerten."
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "Bitte jetzt Brummton bewerten. " +
                                "Antwortfenster ca. $timeout Minuten. " +
                                "Tippe auf BEWERTEN oder pausiere 1 Stunde."
                        )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_MAX
                )
                .setCategory(
                    if (settings.mode == ReminderMode.ALARM) {
                        NotificationCompat.CATEGORY_ALARM
                    } else {
                        NotificationCompat.CATEGORY_REMINDER
                    }
                )
                .setVisibility(
                    NotificationCompat.VISIBILITY_PUBLIC
                )
                .setDefaults(0)
                .setLights(
                    Color.RED,
                    1000,
                    400
                )
                .setAutoCancel(true)
                .setContentIntent(openPendingIntent)
                .addAction(
                    android.R.drawable.ic_menu_edit,
                    "BEWERTEN",
                    openPendingIntent
                )
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "1 h pausieren",
                    pausePendingIntent
                )

        if (!alarmPackagesSilent) {
            builder
                .setSound(
                    sound.uri
                        .takeIf { it.isNotBlank() }
                        ?.let(Uri::parse)
                )
                .setVibrate(
                    longArrayOf(
                        0L, 700L, 250L, 700L,
                        250L, 1200L
                    )
                )
        }

        if (fullScreenAllowed) {
            builder.setFullScreenIntent(
                fullScreenPendingIntent,
                true
            )
        }

        NotificationManagerCompat
            .from(context)
            .notify(
                ReminderWorker.NOTIFICATION_ID,
                builder.build()
            )

        DebugLogger.log(
            context,
            "REMINDER_RATE_PENDING_INTENT",
            "target=MainActivity direct=true"
        )

        DebugLogger.log(
            context,
            "NOTIFICATION_SHOWN",
            "mode=${settings.mode.name} " +
                "sound=${sound.title} " +
                "importance=HIGH " +
                "full_screen_requested=$requestFullScreen " +
                "full_screen_allowed=$fullScreenAllowed " +
                "heads_up_expected=true"
        )

        return true
    }

    private fun ensureUrgentChannel(
        context: Context,
        soundUri: String,
        soundTitle: String,
        alarmUsage: Boolean,
        alarmPackagesSilent: Boolean
    ): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return "bb_urgent_legacy"
        }

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val soundHash =
            soundUri.hashCode().absoluteValue

        val channelId =
            if (alarmPackagesSilent) {
                "bb_urgent_alarm_packages_v1"
            } else {
                "bb_urgent_notice_$soundHash"
            }

        if (manager.getNotificationChannel(channelId) == null) {
            val channel =
                NotificationChannel(
                    channelId,
                    if (alarmUsage) {
                        "LFN-Scout Wecker"
                    } else {
                        "LFN-Scout sehr auffällig"
                    },
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description =
                        "Auffällige LFN-Scout-Erinnerung"
                    enableVibration(!alarmPackagesSilent)
                    if (!alarmPackagesSilent) {
                        vibrationPattern =
                            longArrayOf(
                                0L, 700L, 250L, 700L,
                                250L, 1200L
                            )
                    }
                    enableLights(true)
                    lightColor = Color.RED
                    lockscreenVisibility =
                        Notification.VISIBILITY_PUBLIC

                    val uri =
                        runCatching {
                            Uri.parse(soundUri)
                        }.getOrNull()

                    if (alarmPackagesSilent) {
                        setSound(null, null)
                    } else if (uri != null) {
                        val attributes =
                            AudioAttributes.Builder()
                                .setUsage(
                                    AudioAttributes.USAGE_NOTIFICATION_EVENT
                                )
                                .setContentType(
                                    AudioAttributes.CONTENT_TYPE_SONIFICATION
                                )
                                .build()

                        setSound(uri, attributes)
                    }

                    if (
                        manager.isNotificationPolicyAccessGranted
                    ) {
                        setBypassDnd(true)
                    }
                }

            manager.createNotificationChannel(channel)

            DebugLogger.log(
                context,
                "NOTIFICATION_CHANNEL_CREATED",
                "id=$channelId sound=$soundTitle " +
                    "importance=HIGH bypass_dnd=" +
                    manager.isNotificationPolicyAccessGranted
            )
        }

        return channelId
    }
}
