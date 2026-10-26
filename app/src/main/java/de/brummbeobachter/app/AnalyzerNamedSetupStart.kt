package de.brummbeobachter.app

import android.content.Context

/**
 * Start a verified named JSON setup without rereading older analyzer preferences.
 * Only analyzer DSP/timing and MON settings are affected; the shared rolling recorder
 * and its backward-looking audio buffer are neither stopped nor recreated here.
 */
internal object AnalyzerNamedSetupStart {
    fun prepare(context: Context, direct48k: Boolean) {
        val repository = AnalyzerJsonSetupRepository(context.applicationContext)
        val activeId = repository.selectedForMode(direct48k)
        if (activeId == null) {
            return
        }
        check(!LiveAnalyzerController.snapshot().running) { "Analyzer muss vor Setup-Laden STOP sein" }
        val parameters = repository.load(activeId).getJSONObject("parameters")
        val legacy = parameters.getJSONObject("legacy")
        if (!AnalyzerModeSetupGuard.compatible(legacy, direct48k)) {
            // Preserve the incompatible JSON; use the controller's validated 8-kHz
            // preferences instead of aborting the complete audio start.
            return
        }
        val monitor = parameters.getJSONObject("monitor")
        val result = AnalyzerStoppedJsonControllerPort.applyCombinedDraft(legacy, monitor)
        when (result) {
            AnalyzerSetupSwitchTransaction.Outcome.Applied,
            AnalyzerSetupSwitchTransaction.Outcome.Unchanged -> {
                // Apply verified stopped draft only; the coordinator starts after audio is ready.
            }
            is AnalyzerSetupSwitchTransaction.Outcome.Rejected ->
                error("Benanntes Analyzer-Setup konnte nicht geladen werden: ${result.reason}")
            is AnalyzerSetupSwitchTransaction.Outcome.Failed ->
                error("Benanntes Analyzer-Setup fehlgeschlagen: ${result.reason}; Rücknahme: ${result.rollbackSucceeded}")
        }
    }
}
