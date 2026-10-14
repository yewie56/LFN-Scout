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
        fun verifyApplied(saved: JSONObject): Boolean
    }
    sealed class Outcome {
        data class Rejected(val reason: String): Outcome()
        object Applied: Outcome()
        object Unchanged: Outcome()
        data class Failed(val reason: String, val rollbackSucceeded: Boolean): Outcome()
    }

    /** A restricted stopped-only transaction. Deliberately does NOT stop or restart audio.
     * Adapter installation is prohibited until a verified preference-free controller API exists.
     */
    fun execute(plan: AnalyzerSetupSwitchPlan.Plan, port: Port): Outcome = synchronized(port.transitionLock) {
        // Deny-only combined gate; no authorization even when blockers are absent.
        val safety = AnalyzerSetupTransitionCoordinator.diagnoseSafety {
            AnalyzerSetupAudioOwnerGate.observe()
        }
        if (safety.blockers.isNotEmpty())
            return@synchronized Outcome.Rejected(safety.blockers.joinToString("; "))
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
            return@synchronized Outcome.Rejected("Unvollständige oder nicht unterstützte Parameter")
        // Phase 3O does not authorize changing even one stop-required value until all
        // audio/FFT setter side effects and rollback paths have been reviewed.
        if (current.stopRequired.isNotEmpty())
            return@synchronized Outcome.Rejected("Audio-/FFT-Übernahme noch nicht freigegeben")
        // Only an exact, full keyset equality can be reported as a no-op.
        // A partial saved preset must not be interpreted as complete activation.
        if (current.changed.isEmpty()) {
            return@synchronized if (AnalyzerSetupExactComparison.sameComplete(saved, live))
                Outcome.Unchanged else Outcome.Rejected("Laufzeitstatus nicht vollständig vergleichbar")
        }
        // Display preview has a separate existing implementation, so this executor never
        // duplicates it or modifies the legacy controller behind its session management.
        Outcome.Rejected("Anzeigeänderungen ausschließlich über bestehenden Vorschaupfad")
    }
}
