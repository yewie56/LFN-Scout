package de.brummbeobachter.app

import android.content.Context
import org.json.JSONObject

/**
 * Phase 3J: an explicitly requested, reversible runtime-only DISPLAY preview.
 * No JSON or SharedPreferences writes. The preview is process-local, and must
 * NEVER be described as a complete Analyzer setup activation.
 *
 * Restoration returns to the captured display state at preview start; user edits
 * made after preview start will also be replaced on explicit restoration.
 */
internal object AnalyzerDisplayPreviewSession {
    private val displayKeys = listOf(
        "display_min", "display_max", "autorange_duration", "autorange_margin",
        "current", "average", "max_hold", "floor", "filter_display_min",
        "filter_display_max", "filter_auto_duration", "filter_auto_margin"
    )

    private var baseline: JSONObject? = null
    private var baselineDirect48k: Boolean? = null
    var activeSetupId: String? = null
        private set

    private fun displayFromLive(): JSONObject {
        val state = LiveAnalyzerController.snapshot()
        val current = AnalyzerRuntimeSetupSnapshot.capture(state)
        return JSONObject().apply {
            displayKeys.forEach { key ->
                if ((!key.startsWith("filter_") || !state.direct48k) &&
                    current.has(key) && !current.isNull(key)) put(key, current.get(key))
            }
        }
    }

    @Synchronized fun begin(context: Context, setupId: String, parameters: JSONObject): AnalyzerDisplayDraftApplier.Result {
        check(activeSetupId == null) { "Anzeige-Vorschau zuerst zurücksetzen" }
        check(!AnalyzerMonitorRecording.isActive()) { "MON-Aufnahme zuerst beenden" }
        check(!AnalyzerMonitor.enabled) { "MON vor der Anzeige-Vorschau stoppen" }
        val mode = LiveAnalyzerController.snapshot().direct48k
        val snapshot = displayFromLive()
        val result = AnalyzerDisplayDraftApplier.apply(context, parameters)
        baselineDirect48k = mode
        baseline = snapshot
        activeSetupId = setupId
        return result
    }

    @Synchronized fun restore(context: Context) {
        val previous = baseline ?: error("Keine aktive Anzeige-Vorschau")
        check(!AnalyzerMonitorRecording.isActive()) { "MON-Aufnahme zuerst beenden" }
        check(!AnalyzerMonitor.enabled) { "MON vor dem Zurücksetzen stoppen" }
        check(LiveAnalyzerController.snapshot().direct48k == baselineDirect48k) {
            "Analyzer-Modus seit Vorschau geändert; erst ursprünglichen Modus wiederherstellen"
        }
        // Keep baseline and active ID intact if restoration fails, so retry is possible.
        AnalyzerDisplayDraftApplier.apply(context, JSONObject(previous.toString()))
        baseline = null
        baselineDirect48k = null
        activeSetupId = null
    }
}
