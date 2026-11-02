package de.brummbeobachter.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.KeyEvent

/** Fallback for ACTION_MEDIA_BUTTON delivered by Android, not an S18 HID handler.
 * The OS still decides which app receives a media button; this receiver alone
 * cannot force LFN-Scout to become the preferred media session.
 */
class BoomMediaButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MEDIA_BUTTON) return
        val key = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
        } ?: return
        DebugLogger.log(context, "BOOM_MEDIA_RECEIVER",
            "action=${key.action} key=${KeyEvent.keyCodeToString(key.keyCode)} repeat=${key.repeatCount}")
        BluetoothRemoteDiagnostic.onMediaSessionKey(key)
        // While diagnosing, log input but never execute an analyzer action.
        if (!BluetoothRemoteDiagnostic.listening) {
            BluetoothRemoteControl.onMediaSessionButton(context, key)
        }
    }
}
