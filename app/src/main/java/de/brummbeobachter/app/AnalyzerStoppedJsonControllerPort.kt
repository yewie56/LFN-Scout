package de.brummbeobachter.app

import org.json.JSONObject

/** Strict, STOP-only controller adapter. It does not persist settings and does not
 * start AudioRecord; a later normal START can reload legacy preferences. This port
 * therefore represents a temporary stopped draft, NOT a full saved setup activation.
 */
internal class AnalyzerStoppedJsonControllerPort(private val requested: JSONObject) : AnalyzerSetupSwitchTransaction.Port {
    override val transitionLock: Any get() = LiveAnalyzerController.setupTransitionLock
    override val supportsAtomicStoppedApply: Boolean get() = true
    override fun status(): AnalyzerSetupSwitchTransaction.Status {
        val state = LiveAnalyzerController.snapshot()
        return AnalyzerSetupSwitchTransaction.Status(
            running = state.running,
            scoutRecording = AnalyzerSetupAudioOwnerGate.observe().directCapturing,
            monRecording = AnalyzerMonitorRecording.isActive(),
            monEnabled = AnalyzerMonitor.enabled,
            displayPreviewActive = AnalyzerDisplayPreviewSession.activeSetupId != null
        )
    }
    override fun saved(): JSONObject = JSONObject(requested.toString())
    override fun live(): JSONObject = AnalyzerRuntimeSetupSnapshot.capture(LiveAnalyzerController.snapshot())
    override fun checkpoint(): Any = JSONObject(live().toString())
    override fun applyStoppedDraft(saved: JSONObject) = LiveAnalyzerController.applyStoppedJsonDraft(saved)
    override fun restoreStopped(checkpoint: Any) =
        LiveAnalyzerController.applyStoppedJsonDraft(checkpoint as JSONObject)
    override fun verifyRestored(checkpoint: Any): Boolean =
        AnalyzerSetupExactComparison.sameComplete(checkpoint as JSONObject, live())
    override fun verifyApplied(saved: JSONObject): Boolean =
        AnalyzerSetupExactComparison.sameComplete(saved, live())

    companion object {
        fun apply(draft: JSONObject): AnalyzerSetupSwitchTransaction.Outcome {
            val state = LiveAnalyzerController.snapshot()
            val current = AnalyzerRuntimeSetupSnapshot.capture(state)
            val plan = AnalyzerSetupSwitchPlan.prepare(
                draft, current, state.running, AnalyzerMonitorRecording.isActive(),
                AnalyzerMonitor.enabled, AnalyzerDisplayPreviewSession.activeSetupId != null
            )
            return AnalyzerSetupSwitchTransaction.execute(plan, AnalyzerStoppedJsonControllerPort(draft))
        }

    /** Combined STOP draft: neither legacy preferences nor JSON files are written.
     * A MON failure restores both parameter snapshots. A completed draft is not
     * advertised as a durable installation or startable controller configuration.
     */
    fun applyCombinedDraft(legacy: JSONObject, monitor: JSONObject): AnalyzerSetupSwitchTransaction.Outcome {
        synchronized(LiveAnalyzerController.setupTransitionLock) {
            val safety = AnalyzerSetupAudioOwnerGate.observe()
            // A running shared rolling recorder is expected here. A foreign owner is not.
            val expectedOwner = if (safety.rollingRunning) "rolling" else null
            if (safety.microphoneOwner != expectedOwner) {
                return AnalyzerSetupSwitchTransaction.Outcome.Rejected(
                    "Audio-Ressourcenkonflikt: Mikrofon belegt durch ${safety.microphoneOwner}"
                )
            }
            if (LiveAnalyzerController.snapshot().running || AnalyzerMonitor.enabled ||
                AnalyzerMonitorRecording.isActive() || safety.directCapturing ||
                AnalyzerDisplayPreviewSession.activeSetupId != null) {
                return AnalyzerSetupSwitchTransaction.Outcome.Rejected("STOP erforderlich; Aufnahme/MON/Vorschau beenden")
            }
            val oldAnalyzer = runCatching {
                AnalyzerRuntimeSetupSnapshot.capture(LiveAnalyzerController.snapshot())
            }.getOrElse { return AnalyzerSetupSwitchTransaction.Outcome.Rejected("Analyzer-Checkpoint: ${it.message}") }
            val oldMonitor = AnalyzerMonitor.snapshotSetupParameters()
            // The first half is already strict and transactional.
            val analyzerResult = apply(legacy)
            if (analyzerResult !is AnalyzerSetupSwitchTransaction.Outcome.Applied &&
                analyzerResult !is AnalyzerSetupSwitchTransaction.Outcome.Unchanged) return analyzerResult
            return try {
                AnalyzerMonitor.applySetupDraft(monitor)
                check(AnalyzerSetupExactComparison.sameComplete(monitor, AnalyzerMonitor.snapshotSetupParameters())) {
                    "MON-Parameter nach Übernahme unterschiedlich"
                }
                analyzerResult
            } catch (failure: Throwable) {
                val rollback = runCatching {
                    LiveAnalyzerController.applyStoppedJsonDraft(oldAnalyzer)
                    AnalyzerMonitor.applySetupDraft(oldMonitor)
                    AnalyzerSetupExactComparison.sameComplete(oldAnalyzer,
                        AnalyzerRuntimeSetupSnapshot.capture(LiveAnalyzerController.snapshot())) &&
                        AnalyzerSetupExactComparison.sameComplete(oldMonitor, AnalyzerMonitor.snapshotSetupParameters())
                }.getOrDefault(false)
                AnalyzerSetupSwitchTransaction.Outcome.Failed(
                    "MON-Übernahme: ${failure.message}", rollback)
            }
        }
    }
    }
}
