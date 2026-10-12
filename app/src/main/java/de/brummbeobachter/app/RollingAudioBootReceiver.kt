package de.brummbeobachter.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RollingAudioBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val enabled = ReminderSettingsStore(context.applicationContext).settingsFlow.first().rollingBufferEnabled
                if (enabled) {
                    val started = RollingAudioService.requestStart(context.applicationContext)
                    if (!started) {
                        RollingAudioService.showRestartNeededNotification(context.applicationContext)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
