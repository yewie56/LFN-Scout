package de.brummbeobachter.app

/** Phase 3O/12: optional guard against competing AudioRecord owners.
 * This does NOT protect MON starts or unrelated service state transitions.
 * It is not wired to the production setup switch.
 */
internal object AnalyzerSetupMicrophoneReservation {
    fun <T> withReservation(operation: () -> T): T? {
        val lease = MicCaptureArbiter.tryAcquire("analyzer_setup_preflight") ?: return null
        return lease.use { operation() }
    }
}
