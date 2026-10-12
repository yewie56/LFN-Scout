package de.brummbeobachter.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val appContext =
            context.applicationContext

        when (intent.action) {
            ACTION_PAUSE_ONE_HOUR -> {
                // Critical ordering requirement:
                // stop audible/visual alert before any pause work.
                ReminderAlertController.stop(
                    appContext,
                    "PAUSE_1H_BUTTON"
                )

                ReminderResponseTimeoutScheduler.cancel(
                    appContext
                )

                val pendingResult = goAsync()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val store =
                            ReminderSettingsStore(appContext)

                        val until =
                            store.pauseHours(1)

                        val settings =
                            store.settingsFlow.first()

                        ReminderEngine.scheduleAt(
                            appContext,
                            store,
                            settings,
                            until,
                            "notification_pause_1h"
                        )

                        DebugLogger.log(
                            appContext,
                            "PAUSE_1H_SELECTED",
                            "until=${DebugLogger.formatEpoch(until)}"
                        )
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            ACTION_STOP_ALERT -> {
                ReminderAlertController.stop(
                    appContext,
                    "STOP_ALERT_ACTION"
                )
            }
        }
    }

    private fun openMainApp(context: Context) {
        val intent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(
                    EXTRA_FROM_REMINDER,
                    true
                )
            }

        context.startActivity(intent)
    }

    companion object {
        const val ACTION_RATE =
            "de.brummbeobachter.app.action.RATE"

        const val ACTION_PAUSE_ONE_HOUR =
            "de.brummbeobachter.app.action.PAUSE_ONE_HOUR"

        const val ACTION_STOP_ALERT =
            "de.brummbeobachter.app.action.STOP_ALERT"

        const val EXTRA_FROM_REMINDER =
            "from_reminder"
    }
}
