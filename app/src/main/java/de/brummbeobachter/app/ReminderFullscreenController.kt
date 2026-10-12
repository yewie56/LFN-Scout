package de.brummbeobachter.app

import android.os.Handler
import android.os.Looper
import java.lang.ref.WeakReference

object ReminderFullscreenController {

    private val mainHandler =
        Handler(
            Looper.getMainLooper()
        )

    @Volatile
    private var currentActivity =
        WeakReference<ReminderAlarmActivity>(
            null
        )

    fun register(
        activity: ReminderAlarmActivity
    ) {
        currentActivity =
            WeakReference(activity)
    }

    fun unregister(
        activity: ReminderAlarmActivity
    ) {
        val current =
            currentActivity.get()

        if (current === activity) {
            currentActivity.clear()
            currentActivity =
                WeakReference(null)
        }
    }

    fun close(
        reason: String
    ) {
        mainHandler.post {
            val activity =
                currentActivity.get()

            if (activity == null) {
                return@post
            }

            DebugLogger.log(
                activity.applicationContext,
                "FULL_SCREEN_DIRECT_CLOSE",
                "reason=$reason"
            )

            if (
                !activity.isFinishing &&
                !activity.isDestroyed
            ) {
                activity.finishAndRemoveTask()
            }
        }
    }
}
