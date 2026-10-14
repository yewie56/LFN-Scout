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
    // A completion must identify its own job; otherwise an older completion can
    // release the marker of a newer, overlapping capture. Legacy marker 0 is
    // retained for existing synchronous callers/tests.
    private val captureTokens = mutableMapOf<String, MutableSet<Long>>()
    private var nextCaptureToken = 1L
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

    /** Phase 3O/20: diagnostic decision ONLY, NOT permission to execute a switch.
     * This in-process view cannot prove hardware, MIC arbiter or external recorder idleness.
     */
    internal data class GateAssessment(
        val coordinatorMarkersClear: Boolean,
        val blockingReasons: List<String>
    )

    // Single source of truth for diagnostic and reservation-side marker checks.
    // Must only be called with mutex held.
    private fun blockingReasonsLocked(): List<String> {
        val reasons = mutableListOf<String>()
        if (setupReserved) reasons.add("setup_reserved")
        inFlightServices.sorted().forEach { reasons.add("service_in_flight:$it") }
        captureJobsPending.sorted().forEach { reasons.add("capture_cleanup_pending:$it") }
        // A destroyed marker with no active lifecycle is an inconsistency worth diagnosing.
        destroyedServices.sorted().filter { it !in inFlightServices && it !in captureJobsPending }
            .forEach { reasons.add("orphan_destroy_marker:$it") }
        return reasons.toList()
    }

    internal fun assessGate(): GateAssessment = synchronized(mutex) {
        val reasons = blockingReasonsLocked()
        GateAssessment(reasons.isEmpty(), reasons)
    }

    /** Fail-closed combined diagnosis; NOT a permission token or transactional lock.
     * An external owner snapshot is necessarily acquired separately from our mutex.
     */
    internal data class SafetyDiagnosis(
        val blockers: List<String>,
        val executionAuthorized: Boolean = false
    )

    internal fun diagnoseSafety(
        ownerProbe: () -> AnalyzerSetupAudioOwnerGate.Observation
    ): SafetyDiagnosis {
        val local = assessGate()
        val reasons = local.blockingReasons.toMutableList()
        try {
            reasons.addAll(ownerProbe().blockers)
        } catch (error: Exception) {
            reasons.add("audio_owner_probe_failed")
        }
        // Even with no blockers the audio state may change after observation.
        return SafetyDiagnosis(reasons.toList())
    }

    /** Phase 3O/AP17: combined, deny-only local STOP / lifecycle / owner evidence.
     * Independent observations are not atomic and NEVER authorize hardware transition.
     */
    internal fun diagnoseStoppedBoundary(
        localStopProbe: () -> LiveAnalyzerController.LocalStopVerification,
        ownerProbe: () -> AnalyzerSetupAudioOwnerGate.Observation
    ): SafetyDiagnosis {
        val reasons = diagnoseSafety(ownerProbe).blockers.toMutableList()
        try {
            val local = localStopProbe()
            if (local.controllerRunning) reasons.add("analyzer_running")
            if (local.analyzerSessionPresent) reasons.add("analyzer_session_present")
            if (local.localBufferSubscribed) reasons.add("analyzer_pcm_subscribed")
        } catch (_: Throwable) {
            reasons.add("analyzer_local_stop_probe_failed")
        }
        // Local STOP does not attest AudioRecord release or Android service completion.
        return SafetyDiagnosis(reasons.distinct())
    }

    /** Read-only production diagnosis; no STOP, START, preferences or file writes. */
    internal fun diagnoseCurrentStoppedBoundary(): SafetyDiagnosis = diagnoseStoppedBoundary(
        { LiveAnalyzerController.verifyLocalStopForSetupTransition() },
        { AnalyzerSetupAudioOwnerGate.observe() }
    )

    fun withTrackedServiceStart(service: String, start: () -> Boolean): Boolean = synchronized(mutex) {
        if (service != ROLLING && service != DIRECT) return@synchronized false
        // Deny concurrent Rolling/Direct lifecycles and MON recording at submission time.
        // This is intentionally conservative, not a hardware-wide arbiter.
        if (setupReserved || inFlightServices.isNotEmpty() || captureJobsPending.isNotEmpty() || destroyedServices.isNotEmpty()) return@synchronized false
        if (AnalyzerMonitorRecording.isActive()) return@synchronized false
        // Package 9: reject a start submission while another AudioRecord owner
        // holds the shared in-process MIC token. The later asynchronous capture
        // must still acquire its own token; this observation is deny-only and
        // cannot grant an atomic future hardware reservation.
        if (MicCaptureArbiter.owner != null) return@synchronized false
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

    /** Register the exact capture generation BEFORE launching its coroutine. */
    fun captureJobRegistered(service: String): Long = synchronized(mutex) {
        require(service == ROLLING || service == DIRECT) { "Unknown capture service" }
        val token = nextCaptureToken++
        captureTokens.getOrPut(service) { mutableSetOf() }.add(token)
        captureJobsPending.add(service)
        token
    }

    /** Only this generation's completion may release its marker. */
    fun captureJobCompleted(service: String, token: Long) = synchronized(mutex) {
        val tokens = captureTokens[service] ?: return@synchronized
        if (!tokens.remove(token)) return@synchronized
        if (tokens.isEmpty()) {
            captureTokens.remove(service)
            captureJobsPending.remove(service)
            if (destroyedServices.contains(service)) clearCompletedService(service)
        }
    }

    /** Compatibility path for pre-existing un-tokenized callers. */
    fun captureJobPending(service: String) = synchronized(mutex) {
        captureTokens.getOrPut(service) { mutableSetOf() }.add(0L)
        captureJobsPending.add(service)
    }

    /** Compatibility completion cannot release a tokenized capture. */
    fun captureJobFinished(service: String) = captureJobCompleted(service, 0L)

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

    /** Legacy MON submission gate. This is a deny-only check, not a hardware lease.
     * Keep the start callback under the same coordinator mutex as setup reservation.
     */
    fun withMonStart(start: () -> Boolean): Boolean = synchronized(mutex) {
        if (blockingReasonsLocked().isNotEmpty() || AnalyzerMonitorRecording.isActive() ||
            MicCaptureArbiter.owner != null) return@synchronized false
        start()
    }

    /** Legacy synchronous service-submission helper (no production callers).
     * Apply the same conservative blockers as the tracked start path. Unlike
     * withTrackedServiceStart this helper cannot track asynchronous lifecycles;
     * it MUST NOT be used for a new asynchronous Android Service request.
     */
    fun withServiceStart(start: () -> Boolean): Boolean = synchronized(mutex) {
        if (blockingReasonsLocked().isNotEmpty() || AnalyzerMonitorRecording.isActive() ||
            MicCaptureArbiter.owner != null) return@synchronized false
        start()
    }

    /** Exclusive reservation across an arbitrary synchronous block. A null result
     * indicates that another setup transaction holds the reservation. Release is
     * guaranteed even when the block throws. Must not be used for suspend code.
     */
    fun <T> withSetupReservation(block: () -> T): T? =
        withOwnerCheckedReservation({ AnalyzerSetupAudioOwnerGate.observe().blockers }, block)

    /** Phase 3O / package 6: shared reservation decision with a conservative owner
     * probe under the coordinator mutex. Injection permits JVM tests without Android
     * services. A clean probe is only a deny-only precondition, never proof that the
     * real microphone or other start/stop paths are atomically locked.
     *
     * No exceptions from the probe can grant a reservation. The callback executes
     * without holding the coordinator mutex, but setupReserved stays true until it
     * completes, including on exceptional exits.
     */
    internal fun <T> withOwnerCheckedReservation(
        ownerBlockers: () -> List<String>,
        block: () -> T
    ): T? {
        // The coordinator gate and MIC token are now acquired in the same
        // serialized admission decision. A preflight that loses the MIC race
        // must NOT execute its caller even when the owner probe was clear.
        val lease: MicCaptureArbiter.Token
        synchronized(mutex) {
            if (blockingReasonsLocked().isNotEmpty()) return null
            val blockers = try { ownerBlockers() } catch (_: Throwable) { return null }
            if (blockers.isNotEmpty()) return null
            if (AnalyzerMonitorRecording.isActive()) return null
            lease = MicCaptureArbiter.tryAcquire("analyzer_setup_preflight") ?: return null
            setupReserved = true
        }
        try { return block() }
        finally {
            // Keep the coordinator gate closed until the MIC token is released.
            synchronized(mutex) {
                try { lease.close() }
                finally { setupReserved = false }
            }
        }
    }
}
