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
        val id = repository.selectedForMode(direct) ?: return@runCatching false
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
            return@runCatching false // No changes: avoid unnecessary flash writes.
        }
        groups.put("legacy", analyzer)
        groups.put("monitor", monitor)
        doc.put("parameters", groups)
        repository.save(doc) // AtomicFile write; never truncate the old copy on failure.
        true
    }
}
