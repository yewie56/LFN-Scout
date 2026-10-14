package de.brummbeobachter.app

/** Phase 3O/13. In-process coordination between MON-WAV start and a future
 * setup transition. This guards rolling/direct START requests but NOT complete service lifecycles and
 * MUST NOT be treated as permission to activate a setup.
 */
internal object AnalyzerSetupTransitionCoordinator {
    private val mutex = Any()
    private var setupReserved = false
    // A successful startForegroundService() submission is asynchronous.
    // Keep the setup gate closed until the corresponding Service.onDestroy().
    // Fail closed if Android never delivers a successful request.
    private val inFlightServices = mutableSetOf<String>()
    const val ROLLING = "rolling"
    const val DIRECT = "direct"

    fun withTrackedServiceStart(service: String, start: () -> Boolean): Boolean = synchronized(mutex) {
        if (setupReserved) return@synchronized false
        // Mark BEFORE invoking the asynchronous Android start request.
        inFlightServices.add(service)
        try {
            val accepted = start()
            if (!accepted) inFlightServices.remove(service)
            accepted
        } catch (error: Throwable) {
            inFlightServices.remove(service)
            throw error
        }
    }

    /** Called only when the matching service finishes its lifecycle. */
    fun serviceDestroyed(service: String) = synchronized(mutex) {
        inFlightServices.remove(service)
    }

    internal fun hasInFlightServiceStarts(): Boolean = synchronized(mutex) { inFlightServices.isNotEmpty() }

    /** Serializes MON start against acquisition of a setup reservation. */
    fun withMonStart(start: () -> Boolean): Boolean = synchronized(mutex) {
        if (setupReserved) false else start()
    }

    /** Serialize service start submission against a future setup reservation.
     * The service may start asynchronously AFTER the request returns. Thus this
     * gate is necessary but NOT sufficient for a safe setup switch.
     */
    fun withServiceStart(start: () -> Boolean): Boolean = synchronized(mutex) {
        if (setupReserved) false else start()
    }

    /** Exclusive reservation across an arbitrary synchronous block. A null result
     * indicates that another setup transaction holds the reservation. Release is
     * guaranteed even when the block throws. Must not be used for suspend code.
     */
    fun <T> withSetupReservation(block: () -> T): T? {
        synchronized(mutex) {
            if (setupReserved || inFlightServices.isNotEmpty() || AnalyzerMonitorRecording.isActive()) return null
            setupReserved = true
        }
        try { return block() }
        finally { synchronized(mutex) { setupReserved = false } }
    }
}
