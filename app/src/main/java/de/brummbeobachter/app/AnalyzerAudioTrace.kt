package de.brummbeobachter.app

import android.content.Context
import android.os.Process
import android.os.SystemClock
import android.util.Log

/** Diagnostic-only trace for transitions. No PCM samples or personal identifiers are logged. */
internal object AnalyzerAudioTrace {
    @Volatile private var sessionId: String = "no-session"
    fun newSession(): String = synchronized(this) {
        "${System.currentTimeMillis().toString(36)}-${SystemClock.elapsedRealtime()}".also { sessionId = it }
    }
    fun write(context: Context, event: String, details: String = "") {
        // Keep the ordinary user-accessible persistent debug log as the primary source.
        runCatching {
            val line = "sid=$sessionId pid=${Process.myPid()} thread=${Thread.currentThread().name} " + details
            DebugLogger.log(context.applicationContext, "AUDIO_DIAG_$event", line)
            Log.i("LFN-AUDIO", "$event $line")
        }
    }
}
