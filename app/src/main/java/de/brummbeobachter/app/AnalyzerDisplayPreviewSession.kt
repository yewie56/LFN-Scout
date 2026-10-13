package de.brummbeobachter.app

import android.content.Context
import org.json.JSONObject

/**
 * Phase 3K: an explicitly requested DISPLAY draft. Preview and restore never
 * write to SharedPreferences; JSON is written only on explicit Save.
 * The preview is process-local and must
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
    private var appliedKeys: List<String> = emptyList()

    /** Only values actually previewed may be written back on explicit Save. */
    @Synchronized fun changedKeys(): List<String> {
        if (activeSetupId == null) return emptyList()
        val previous = baseline ?: return emptyList()
        val now = displayFromLive()
        return appliedKeys.filter { key ->
            previous.has(key) && now.has(key) && previous.get(key) != now.get(key)
        }
    }

    @Synchronized fun saveToSetup(repository: AnalyzerJsonSetupRepository): Int {
        val id = activeSetupId ?: error("Keine aktive Anzeige-Vorschau")
        check(!AnalyzerMonitorRecording.isActive() && !AnalyzerMonitor.enabled) { "MON zunächst stoppen" }
        check(LiveAnalyzerController.snapshot().direct48k == baselineDirect48k) { "Analyzer-Modus gewechselt" }
        val now = displayFromLive()
        val doc = repository.load(id)
        val legacy = doc.getJSONObject("parameters").optJSONObject("legacy")
            ?: error("Setup enthält keine Legacy-Parameter")
        // Preserve absent fields and every unrelated setting. Never infer missing values.
        for (key in appliedKeys) {
            check(now.has(key)) { "Anzeigeparameter nicht mehr verfügbar: $key" }
            legacy.put(key, now.get(key))
        }
        // Validate and write atomically before finishing the preview.
        val count = appliedKeys.size
        repository.save(doc)
        clear()
        return count
    }

    @Synchronized private fun clear() {
        baseline = null
        baselineDirect48k = null
        activeSetupId = null
        appliedKeys = emptyList()
    }

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
        appliedKeys = result.appliedKeys
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
        clear()
    }
}
