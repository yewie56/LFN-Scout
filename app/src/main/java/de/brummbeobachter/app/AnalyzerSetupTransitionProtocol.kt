package de.brummbeobachter.app

/** Phase 3O / package 4: isolated and NOT connected to production audio or UI.
 * The caller must eventually provide an actual shared exclusive microphone lease.
 * This protocol intentionally grants no such lease and is not an activation API.
 */
internal object AnalyzerSetupTransitionProtocol {
    enum class Stage { CHECKPOINT, STOP, RECONFIGURE, START, VERIFY, ROLLBACK_STOP, ROLLBACK_RESTORE, ROLLBACK_START, ROLLBACK_VERIFY, COMPLETE, SAFE_STOPPED }

    /** Each method must be synchronous and return only after its audio resources have settled.
     * Implementations must not write JSON files or SharedPreferences.
     */
    interface ControlledPort {
        fun checkpoint(): Any
        fun stopAndRelease()
        fun applyDraft()
        fun startSession()
        fun verifyNewSession(): Boolean
        fun restoreCheckpoint(checkpoint: Any)
        fun verifyRestoredSession(): Boolean
        fun verifySafelyStopped(): Boolean
    }

    /** A fake/test lease boundary. Production use requires a lease shared by ALL
     * audio owners and Service starts; MicCaptureArbiter alone is insufficient.
     * The lease must remain held through recovery and final verification.
     */
    interface ExclusiveBoundary {
        fun <T> underExclusiveOwnership(operation: () -> T): T?
        fun ownershipStillExclusive(): Boolean
    }

    /** Fail closed if exclusive ownership is missing or lost. Test-only until the
     * production coordinator, MON and recorder paths share one owner boundary.
     */
    internal fun exerciseWithBoundary(port: ControlledPort, boundary: ExclusiveBoundary): Result {
        var ran = false
        return try {
            val result = boundary.underExclusiveOwnership {
                ran = true
                exerciseWithControlledPort(object : ControlledPort by port {
                    private fun check() {
                        check(boundary.ownershipStillExclusive()) { "Exclusive audio ownership lost" }
                    }
                    override fun checkpoint(): Any { check(); return port.checkpoint() }
                    override fun stopAndRelease() { check(); port.stopAndRelease() }
                    override fun applyDraft() { check(); port.applyDraft() }
                    override fun startSession() { check(); port.startSession() }
                    override fun verifyNewSession(): Boolean { check(); return port.verifyNewSession() && boundary.ownershipStillExclusive() }
                    override fun restoreCheckpoint(checkpoint: Any) { check(); port.restoreCheckpoint(checkpoint) }
                    override fun verifyRestoredSession(): Boolean { check(); return port.verifyRestoredSession() && boundary.ownershipStillExclusive() }
                    override fun verifySafelyStopped(): Boolean { check(); return port.verifySafelyStopped() && boundary.ownershipStillExclusive() }
                })
            }
            result ?: Result(false, false, Stage.CHECKPOINT,
                IllegalStateException(if (ran) "Exclusive operation returned null" else "Exclusive audio ownership unavailable"), emptyList())
        } catch (t: Throwable) {
            Result(false, false, Stage.CHECKPOINT, t, emptyList())
        }
    }

    data class Result(
        val succeeded: Boolean,
        val rollbackSucceeded: Boolean,
        val stage: Stage,
        val originalError: Throwable?,
        val recoveryErrors: List<Throwable>
    )

    /** Exercise only with a controlled fake until ownership, MIC lease and controller
     * adapters have been verified. No production caller exists in package 4.
     */
    internal fun exerciseWithControlledPort(port: ControlledPort): Result {
        var stage = Stage.CHECKPOINT
        var checkpoint: Any? = null
        var stoppingAttempted = false
        try {
            checkpoint = port.checkpoint()
            stage = Stage.STOP
            stoppingAttempted = true
            port.stopAndRelease()
            stage = Stage.RECONFIGURE
            port.applyDraft()
            stage = Stage.START
            port.startSession()
            stage = Stage.VERIFY
            if (!port.verifyNewSession()) error("New analyzer session verification failed")
            return Result(true, false, Stage.COMPLETE, null, emptyList())
        } catch (primary: Throwable) {
            val recovery = mutableListOf<Throwable>()
            // No changes made before STOP: preserve the originally running session.
            if (!stoppingAttempted) return Result(false, true, stage, primary, emptyList())
            stage = Stage.ROLLBACK_STOP
            try { port.stopAndRelease() } catch (t: Throwable) { recovery.add(t) }
            // A failed STOP means ownership may remain ambiguous. Never restart on top
            // of a resource that may still be held by another session.
            if (recovery.isEmpty()) {
                stage = Stage.ROLLBACK_RESTORE
                try { port.restoreCheckpoint(checkpoint!!) } catch (t: Throwable) { recovery.add(t) }
            }
            if (recovery.isEmpty()) {
                stage = Stage.ROLLBACK_START
                try { port.startSession() } catch (t: Throwable) { recovery.add(t) }
            }
            if (recovery.isEmpty()) {
                stage = Stage.ROLLBACK_VERIFY
                try {
                    if (!port.verifyRestoredSession()) error("Restored analyzer session verification failed")
                } catch (t: Throwable) { recovery.add(t) }
            }
            if (recovery.isEmpty()) return Result(false, true, stage, primary, emptyList())
            // Best effort stop without ever masking original failure. Any uncertainty
            // is reported as SAFE_STOPPED only after affirmative verification.
            try { port.stopAndRelease() } catch (t: Throwable) { recovery.add(t) }
            val safe = try { port.verifySafelyStopped() } catch (t: Throwable) { recovery.add(t); false }
            return Result(false, false, if (safe) Stage.SAFE_STOPPED else stage, primary, recovery.toList())
        }
    }
}
