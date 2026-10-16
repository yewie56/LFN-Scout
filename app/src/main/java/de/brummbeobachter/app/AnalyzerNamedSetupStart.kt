package de.brummbeobachter.app

import android.content.Context

/**
 * Start a verified named JSON setup without rereading older analyzer preferences.
 * Only analyzer DSP/timing and MON settings are affected; the shared rolling recorder
 * and its backward-looking audio buffer are neither stopped nor recreated here.
 */
internal object AnalyzerNamedSetupStart {
    fun start(context: Context) {
        val repository = AnalyzerJsonSetupRepository(context.applicationContext)
        val activeId = repository.listing().activeId
        if (activeId == null) {
            LiveAnalyzerController.start(context)
            return
        }
        check(!LiveAnalyzerController.snapshot().running) { "Analyzer muss vor Setup-Laden STOP sein" }
        val parameters = repository.load(activeId).getJSONObject("parameters")
        val legacy = parameters.getJSONObject("legacy")
        val monitor = parameters.getJSONObject("monitor")
        val result = AnalyzerStoppedJsonControllerPort.applyCombinedDraft(legacy, monitor)
        when (result) {
            AnalyzerSetupSwitchTransaction.Outcome.Applied,
            AnalyzerSetupSwitchTransaction.Outcome.Unchanged -> {
                val checkpoint = LiveAnalyzerController.captureStoppedAnalyzerConfigCheckpoint()
                LiveAnalyzerController.startFromStoppedConfiguration(context, checkpoint)
            }
            is AnalyzerSetupSwitchTransaction.Outcome.Rejected ->
                error("Benanntes Analyzer-Setup konnte nicht geladen werden: ${result.reason}")
            is AnalyzerSetupSwitchTransaction.Outcome.Failed ->
                error("Benanntes Analyzer-Setup fehlgeschlagen: ${result.reason}; Rücknahme: ${result.rollbackSucceeded}")
        }
    }
}
