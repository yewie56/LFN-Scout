package de.brummbeobachter.app

import org.json.JSONObject

/** Phase 3O: fail-closed, opt-in transaction boundary. NOT connected to the audio controller.
 * A future controller adapter must provide a lock shared with every recorder state transition.
 * In particular this contract must NEVER be called with a lock covering only the UI.
 */
internal object AnalyzerSetupSwitchTransaction {
    data class Status(
        val running: Boolean,
        val scoutRecording: Boolean,
        val monRecording: Boolean,
        val monEnabled: Boolean,
        val displayPreviewActive: Boolean
    )
    interface Port {
        /** Must coordinate with microphone/recorder owner, not just a cached Compose state. */
        val transitionLock: Any
        fun status(): Status
        fun saved(): JSONObject
        fun live(): JSONObject
        /** Must return an independently owned deep snapshot. */
        fun checkpoint(): Any
        /** Only when STOPPED; must not write preferences or JSON. */
        fun applyStoppedDraft(saved: JSONObject)
        fun restoreStopped(checkpoint: Any)
        /** Must independently verify ALL checkpoint fields; default fails closed. */
        fun verifyRestored(checkpoint: Any): Boolean = false
        fun verifyApplied(saved: JSONObject): Boolean
        /** Only a verified implementation may opt in; legacy UI ports remain deny-only. */
        val supportsAtomicStoppedApply: Boolean get() = false
    }
    sealed class Outcome {
        data class Rejected(val reason: String): Outcome()
        object Applied: Outcome()
        object Unchanged: Outcome()
        data class Failed(val reason: String, val rollbackSucceeded: Boolean): Outcome()
    }

    /** A restricted stopped-only transaction. Deliberately does NOT stop or restart audio.
     * Only verified preference-free adapters with complete rollback may opt in.
     */
    fun execute(plan: AnalyzerSetupSwitchPlan.Plan, port: Port): Outcome = synchronized(port.transitionLock) {
        // Deny-only combined gate; no authorization even when blockers are absent.
        val safety = AnalyzerSetupTransitionCoordinator.diagnoseSafety {
            AnalyzerSetupAudioOwnerGate.observe()
        }
        // Shared 8-kHz rolling capture is not an Analyzer STOP violation.
        // Direct capture, MON, unrelated owners and unsafe lifecycle states remain blockers.
        val owner = AnalyzerSetupAudioOwnerGate.observe()
        val rollingOnly = owner.rollingRunning &&
            owner.microphoneOwner == "rolling" &&
            !owner.directCapturing && !owner.monRecording
        val blockers = if (rollingOnly) safety.blockers.filterNot {
            it == "Rolling-Audiostream aktiv" ||
                it == "Mikrofon belegt: rolling" ||
                it == "service_in_flight:rolling" ||
                it == "capture_cleanup_pending:rolling"
        } else safety.blockers
        if (blockers.isNotEmpty())
            return@synchronized Outcome.Rejected(blockers.joinToString("; "))
        val state = try { port.status() } catch (t: Exception) {
            return@synchronized Outcome.Rejected("Analyzer-Status nicht lesbar: ${t.message}")
        }
        if (state.scoutRecording || state.monRecording) return@synchronized Outcome.Rejected("Aufnahme aktiv")
        if (state.running) return@synchronized Outcome.Rejected("Analyzer muss gestoppt sein")
        if (state.monEnabled || state.displayPreviewActive) return@synchronized Outcome.Rejected("MON/Vorschau aktiv")
        val saved = try { port.saved() } catch (t: Exception) {
            return@synchronized Outcome.Rejected("Setup nicht lesbar: ${t.message}")
        }
        val live = try { port.live() } catch (t: Exception) {
            return@synchronized Outcome.Rejected("Laufzeitstatus nicht lesbar: ${t.message}")
        }
        if (!AnalyzerSetupSwitchPlan.stillCurrent(plan, saved, live, false, false, false, false))
            return@synchronized Outcome.Rejected("Plan veraltet")
        val current = try { AnalyzerSetupSwitchPreflight.assess(saved, live, false, false) }
            catch (t: Exception) { return@synchronized Outcome.Rejected("Validierung: ${t.message}") }
        if (current.missingOrNull.isNotEmpty() || current.unsupported.isNotEmpty())
            return@synchronized Outcome.Rejected("Unvollständige Parameter: ${current.missingOrNull.joinToString(", ")}; nicht unterstützt: ${current.unsupported.joinToString(", ")}")
        // No production controller port opts in until it can prove a complete,
        // preference-free STOP apply and restore under the same hardware lock.
        if (current.stopRequired.isNotEmpty() && !port.supportsAtomicStoppedApply)
            return@synchronized Outcome.Rejected("Audio-/FFT-Übernahme noch nicht freigegeben")
        // Only an exact, full keyset equality can be reported as a no-op.
        // A partial saved preset must not be interpreted as complete activation.
        if (current.changed.isEmpty()) {
            return@synchronized if (AnalyzerSetupExactComparison.sameComplete(saved, live))
                Outcome.Unchanged else Outcome.Rejected("Laufzeitstatus nicht vollständig vergleichbar")
        }
        if (!port.supportsAtomicStoppedApply)
            return@synchronized Outcome.Rejected("Kein überprüfter atomarer STOP-Adapter vorhanden")
        applyWithRollback(port, saved) {
            val now = port.status()
            now == state && AnalyzerSetupSwitchPlan.stillCurrent(
                plan, port.saved(), port.live(), now.running,
                now.scoutRecording || now.monRecording, now.monEnabled, now.displayPreviewActive)
        }

    }
    /** Pure rollback mechanism, testable using a fake Port without Android services.
     * Production callers MUST hold their exclusive controller/hardware transition lock
     * and pass a recheck that covers the entire stopped state.
     */
    internal fun applyWithRollback(port: Port, saved: JSONObject, recheck: () -> Boolean): Outcome {
        val before = try { port.checkpoint() } catch (t: Exception) {
            return Outcome.Rejected("Checkpoint fehlgeschlagen: ${t.message}")
        }
        try {
            if (!recheck()) return Outcome.Rejected("Setup/Analyzer während Checkpoint verändert")
            port.applyStoppedDraft(saved)
            check(port.verifyApplied(saved)) { "Angewendete Werte weichen vom JSON ab" }
            return Outcome.Applied
        } catch (failure: Throwable) {
            val restored = try {
                port.restoreStopped(before)
                port.verifyRestored(before)
            } catch (_: Throwable) { false }
            return Outcome.Failed(failure.message ?: failure.javaClass.simpleName, restored)
        }
    }
}
