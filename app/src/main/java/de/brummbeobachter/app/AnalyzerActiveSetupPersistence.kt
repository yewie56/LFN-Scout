package de.brummbeobachter.app

import android.content.Context
import org.json.JSONObject

/** Persist the currently selected named setup when the analyzer screen is exited.
 * Does not create a setup implicitly or change the running audio source. A failed
 * snapshot leaves the previous atomic JSON document untouched.
 */
internal object AnalyzerActiveSetupPersistence {
    fun saveOnAnalyzerExit(context: Context): Result<Boolean> = runCatching {
        val repository = AnalyzerJsonSetupRepository(context.applicationContext)
        val direct = LiveAnalyzerController.snapshot().direct48k
        val id = repository.selectedForMode(direct)
            ?: error("Kein aktives benanntes JSON-Setup für ${if (direct) "Direktanalyzer" else "8-kHz-Analyzer"} zugeordnet")
        val oldLegacy = repository.load(id).getJSONObject("parameters").getJSONObject("legacy")
        check(AnalyzerModeSetupGuard.compatible(oldLegacy, direct)) {
            "Automatisches Speichern verhindert: Inkompatibles Setup " +
                AnalyzerModeSetupGuard.reason(oldLegacy, direct)
        }
        val doc = repository.load(id)
        val analyzer = AnalyzerRuntimeSetupSnapshot.capture(LiveAnalyzerController.snapshot())
        val monitor = AnalyzerMonitor.snapshotSetupParameters()
        val groups = JSONObject(doc.getJSONObject("parameters").toString())
        val previousAnalyzer = groups.optJSONObject("legacy")
        val previousMonitor = groups.optJSONObject("monitor")
        if (previousAnalyzer != null && previousMonitor != null &&
            previousAnalyzer.toString() == analyzer.toString() &&
            previousMonitor.toString() == monitor.toString()) {
            AnalyzerAudioTrace.write(context, "SETUP_SAVE_UNCHANGED", "mode=${if (direct) "DIRECT" else "8K"} id=$id")
            return@runCatching true // Verified unchanged is a successful persisted state.
        }
        groups.put("legacy", analyzer)
        groups.put("monitor", monitor)
        doc.put("parameters", groups)
        repository.save(doc) // AtomicFile write; never truncate the old copy on failure.
        // Read back from disk; comparing with the live MON state catches silent loss.
        val saved = repository.load(id).getJSONObject("parameters")
        check(AnalyzerSetupExactComparison.sameComplete(analyzer, saved.getJSONObject("legacy"))) {
            "Analyzer-Werte nach Speichern nicht identisch"
        }
        check(AnalyzerSetupExactComparison.sameComplete(monitor, saved.getJSONObject("monitor"))) {
            "MON-Werte nach Speichern nicht identisch"
        }
        AnalyzerAudioTrace.write(context, "SETUP_SAVE_VERIFIED",
            "mode=${if (direct) "DIRECT" else "8K"} id=$id max_gain=${monitor.optDouble("max_gain")} level=${monitor.optDouble("level")}")
        true
    }
}
