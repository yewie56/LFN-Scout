package de.brummbeobachter.app

/** Phase 3O/13. In-process coordination between MON-WAV start and a future
 * setup transition. This does NOT guard rolling/direct service lifecycles and
 * MUST NOT be treated as permission to activate a setup.
 */
internal object AnalyzerSetupTransitionCoordinator {
    private val mutex = Any()
    private var setupReserved = false

    /** Serializes MON start against acquisition of a setup reservation. */
    fun withMonStart(start: () -> Boolean): Boolean = synchronized(mutex) {
        if (setupReserved) false else start()
    }

    /** Exclusive reservation across an arbitrary synchronous block. A null result
     * indicates that another setup transaction holds the reservation. Release is
     * guaranteed even when the block throws. Must not be used for suspend code.
     */
    fun <T> withSetupReservation(block: () -> T): T? {
        synchronized(mutex) {
            if (setupReserved || AnalyzerMonitorRecording.isActive()) return null
            setupReserved = true
        }
        try { return block() }
        finally { synchronized(mutex) { setupReserved = false } }
    }
}
