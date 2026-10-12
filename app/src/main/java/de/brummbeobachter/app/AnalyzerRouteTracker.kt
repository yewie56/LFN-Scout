package de.brummbeobachter.app

import java.io.Closeable

/** Actual recording route read from the live PCM producer, never from a preference. */
internal object AnalyzerRouteTracker {
    @Volatile var deviceLabel: String = ""
        private set
    @Volatile var lastFrameMs: Long = 0L
        private set
    private var subscription: Closeable? = null
    @Synchronized fun start() {
        subscription?.close()
        deviceLabel="";lastFrameMs=0
        subscription=AnalyzerSessionGateway.rawPcm.subscribe(queueCapacity=4) { frame ->
            deviceLabel=frame.deviceLabel
            lastFrameMs=android.os.SystemClock.elapsedRealtime()
        }
    }
    @Synchronized fun stop() {subscription?.close();subscription=null;deviceLabel="";lastFrameMs=0}
    fun liveDevice():String = if(android.os.SystemClock.elapsedRealtime()-lastFrameMs<=1500) deviceLabel else ""
}
