package de.brummbeobachter.app

import android.content.Context

/** A missing pin, failed read or mismatched effective values are never reported as saved. */
internal object AnalyzerSetupLiveStatus {
    fun read(context: Context): Pair<String, Boolean> = try {
        val repo = AnalyzerJsonSetupRepository(context.applicationContext)
        val direct = LiveAnalyzerController.snapshot().direct48k
        val id = repo.selectedForMode(direct)
            ?: return "Setup: NICHT ZUGEORDNET – Änderungen nicht gesichert" to true
        val doc = repo.load(id)
        val name = doc.getString("name")
        val groups = doc.getJSONObject("parameters")
        val diskAnalyzer = groups.getJSONObject("legacy")
        val diskMonitor = groups.getJSONObject("monitor")
        val liveAnalyzer = AnalyzerRuntimeSetupSnapshot.capture(LiveAnalyzerController.snapshot())
        val liveMonitor = AnalyzerMonitor.snapshotSetupParameters()
        val matches = AnalyzerSetupExactComparison.sameComplete(liveAnalyzer, diskAnalyzer) &&
            AnalyzerSetupExactComparison.sameComplete(liveMonitor, diskMonitor)
        if (matches) "Setup: $name · AKTIV / GESPEICHERT" to false
        else "Setup: $name · GEÄNDERT / NICHT GESPEICHERT" to true
    } catch (error: Exception) {
        "Setup: PRÜFFEHLER – ${error.message ?: "unbekannt"}" to true
    }
}
