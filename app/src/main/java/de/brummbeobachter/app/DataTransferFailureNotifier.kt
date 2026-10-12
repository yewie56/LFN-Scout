package de.brummbeobachter.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object DataTransferFailureNotifier {

    private const val CHANNEL_ID =
        "bb_data_transfer_failures_v1"
    private const val NOTIFICATION_ID =
        4601

    fun showPermanentFailure(
        context: Context,
        message: String
    ) {
        val appContext =
            context.applicationContext

        ensureChannel(
            appContext
        )

        val detail =
            message
                .take(220)
                .ifBlank {
                    "Bitte Zugangsschlüssel bzw. Serverkonfiguration prüfen."
                }

        val notification =
            NotificationCompat.Builder(
                appContext,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.stat_notify_error
                )
                .setContentTitle(
                    "Dauerhafte Störung der Datenübertragung"
                )
                .setContentText(
                    "Bitte Zugangsschlüssel bzw. Serverkonfiguration prüfen."
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "Die Daten konnten nicht übertragen werden. " +
                                "Bitte Zugangsschlüssel bzw. Serverkonfiguration prüfen.\n\n" +
                                detail
                        )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .build()

        runCatching {
            NotificationManagerCompat
                .from(appContext)
                .notify(
                    NOTIFICATION_ID,
                    notification
                )
        }.onFailure { error ->
            DebugLogger.log(
                appContext,
                "DATA_TRANSFER_PERMANENT_WARNING_FAIL",
                "message=${error.message ?: error.javaClass.simpleName}"
            )
        }

        DebugLogger.log(
            appContext,
            "DATA_TRANSFER_PERMANENT_FAILURE",
            "message=$detail"
        )
    }

    private fun ensureChannel(
        context: Context
    ) {
        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.O
        ) {
            return
        }

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "Datenübertragungsfehler",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description =
                    "Dauerhafte Störungen bei der Datenübertragung"
            }

        manager.createNotificationChannel(
            channel
        )
    }
}
