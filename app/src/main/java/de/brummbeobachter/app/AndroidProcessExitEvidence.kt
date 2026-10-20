package de.brummbeobachter.app

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build

/** Independent OS-level diagnostic. Absence of an exit record is not a crash-free proof. */
internal object AndroidProcessExitEvidence {
    data class Snapshot(val supported: Boolean, val anomalousCount: Int, val detail: String)

    fun inspect(context: Context, sinceMs: Long): Snapshot {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return Snapshot(false, 0, "Android API < 30; process-exit history unavailable")
        }
        return try {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val records = manager.getHistoricalProcessExitReasons(context.packageName, 0, 64)
            val relevant = records.filter { it.timestamp >= sinceMs }
            val bad = relevant.filter {
                it.reason == ApplicationExitInfo.REASON_CRASH ||
                    it.reason == ApplicationExitInfo.REASON_CRASH_NATIVE ||
                    it.reason == ApplicationExitInfo.REASON_ANR
            }
            Snapshot(true, bad.size,
                "OS process-exit records since=$sinceMs: total=${relevant.size}, crash_or_anr=${bad.size}; " +
                "bounded history, no negative crash-free proof")
        } catch (t: Exception) {
            Snapshot(false, 0, "OS process-exit history unavailable: ${t.javaClass.simpleName}: ${t.message}")
        }
    }
}
