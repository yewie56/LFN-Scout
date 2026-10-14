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
    // onDestroy may run before cancelled coroutine has released AudioRecord.
    private val destroyedServices = mutableSetOf<String>()
    private val captureJobsPending = mutableSetOf<String>()
    const val ROLLING = "rolling"
    const val DIRECT = "direct"

    /** Phase 3O/19: non-mutating diagnostic view of all setup blocking markers.
     * Never clears an in-flight marker automatically: a missing Android lifecycle
     * callback must fail closed rather than permit a potentially unsafe switch.
     */
    internal data class GateSnapshot(
        val setupReserved: Boolean,
        val inFlightServices: Set<String>,
        val destroyedServices: Set<String>,
        val captureJobsPending: Set<String>
    ) {
        val serviceLifecycleBlocked: Boolean get() = inFlightServices.isNotEmpty() || captureJobsPending.isNotEmpty()
    }

    internal fun snapshotGate(): GateSnapshot = synchronized(mutex) {
        GateSnapshot(
            setupReserved = setupReserved,
            inFlightServices = inFlightServices.toSet(),
            destroyedServices = destroyedServices.toSet(),
            captureJobsPending = captureJobsPending.toSet()
        )
    }

    fun withTrackedServiceStart(service: String, start: () -> Boolean): Boolean = synchronized(mutex) {
        if (setupReserved || inFlightServices.contains(service) || captureJobsPending.contains(service)) return@synchronized false
        // Reject duplicate starts while a previous lifecycle/cleanup is unresolved.
        // Otherwise a repeated request could share the same Set marker and be released early.
        // Mark BEFORE invoking the asynchronous Android start request.
        inFlightServices.add(service)
        destroyedServices.remove(service)
        try {
            val accepted = start()
            if (!accepted) inFlightServices.remove(service)
            accepted
        } catch (error: Throwable) {
            inFlightServices.remove(service)
            throw error
        }
    }

    /** Register before launching a capture job; completion must follow its finally cleanup. */
    fun captureJobPending(service: String) = synchronized(mutex) {
        captureJobsPending.add(service)
    }

    /** Invoked by Job.invokeOnCompletion, AFTER the coroutine completes its finally. */
    fun captureJobFinished(service: String) = synchronized(mutex) {
        // A duplicate completion must not finalize an unrelated lifecycle.
        if (!captureJobsPending.remove(service)) return@synchronized
        if (destroyedServices.contains(service)) clearCompletedService(service)
    }

    /** Android's lifecycle alone is insufficient: wait for any pending capture cleanup. */
    fun serviceDestroyed(service: String) = synchronized(mutex) {
        // Ignore duplicate or late callbacks after an already completed lifecycle.
        // In particular, do not leave a stale destroyed marker for a subsequent start.
        if (!inFlightServices.contains(service) && !captureJobsPending.contains(service)) return@synchronized
        destroyedServices.add(service)
        if (!captureJobsPending.contains(service)) clearCompletedService(service)
    }

    private fun clearCompletedService(service: String) {
        inFlightServices.remove(service)
        destroyedServices.remove(service)
    }

    internal fun hasPendingCaptureCleanup(): Boolean = synchronized(mutex) { captureJobsPending.isNotEmpty() }

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
            if (setupReserved || inFlightServices.isNotEmpty() || captureJobsPending.isNotEmpty() || AnalyzerMonitorRecording.isActive()) return null
            setupReserved = true
        }
        try { return block() }
        finally { synchronized(mutex) { setupReserved = false } }
    }
}
