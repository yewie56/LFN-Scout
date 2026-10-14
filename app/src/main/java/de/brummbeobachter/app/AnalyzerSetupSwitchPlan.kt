package de.brummbeobachter.app

import org.json.JSONObject
import org.json.JSONArray
import java.security.MessageDigest

/** Phase 3N: immutable, read-only preparation for a future transactional switch.
 * No STOP/START, setter, file write or implicit default is performed here.
 */
internal object AnalyzerSetupSwitchPlan {
    data class Plan(
        val expectedSavedFingerprint: String,
        val expectedLiveFingerprint: String,
        val steps: List<String>,
        val blockers: List<String>,
        val requiresStop: Boolean,
        val changedKeys: List<String>
    ) {
        val executable: Boolean get() = false // Executor deliberately not implemented.
    }

    private fun fingerprint(obj: JSONObject): String {
        // Deterministic for a flat legacy parameter group; keys must not depend on JSON insertion order.
        val canonical = obj.keys().asSequence().toList().sorted().joinToString("\n") { key ->
            val v = obj.get(key)
            JSONObject.quote(key) + ":" + (if (v === JSONObject.NULL) "null" else JSONArray().put(v).toString())
        }
        val bytes = MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun prepare(
        saved: JSONObject, live: JSONObject, running: Boolean, recording: Boolean,
        monEnabled: Boolean, displayPreviewActive: Boolean
    ): Plan {
        val report = AnalyzerSetupSwitchPreflight.assess(saved, live, running, recording)
        val blockers = mutableListOf<String>()
        if (recording) blockers += "Aufnahme aktiv: Setupwechsel verboten"
        if (monEnabled) blockers += "MON vor der Umschaltung beenden"
        if (displayPreviewActive) blockers += "Anzeige-Vorschau zuerst abschließen"
        if (report.missingOrNull.isNotEmpty()) blockers += "Fehlende/null-Werte: ${report.missingOrNull.joinToString(", ")}"
        if (report.unsupported.isNotEmpty()) blockers += "Nicht unterstützte Werte: ${report.unsupported.joinToString(", ")}"
        if (running && report.requiresStopAndRestart) blockers += "Laufende Messung muss vor Audio/FFT-Änderungen gestoppt werden"
        val steps = mutableListOf<String>()
        steps += "Aktuellen JSON-/Laufzeitzustand unmittelbar vor jeder Aktion erneut prüfen"
        steps += "Aufnahme und MON erneut prüfen; bei Änderung abbrechen"
        steps += "Ungespeicherte Anzeige-/MON-Entwürfe zuerst abschließen"
        if (report.requiresStopAndRestart) {
            steps += "Messung geordnet stoppen, ohne laufende Aufnahme abzubrechen"
            steps += "Audioquelle, Abtastrate, FFT und Filterbank nach vollständiger Validierung übernehmen"
            steps += "Messung nur auf ausdrückliche Freigabe erneut starten"
        }
        if (report.displayPreviewable.isNotEmpty()) steps += "Anzeigewerte speicherfrei anwenden"
        steps += "Bei Fehlern vorherigen Laufzeitzustand wiederherstellen und Fehler melden"
        steps += "Nur auf ausdrückliches Speichern hin JSON-Datei aktualisieren"
        return Plan(fingerprint(saved), fingerprint(live), steps, blockers,
            report.requiresStopAndRestart, report.changed)
    }

    /** Detect a stale plan; this is not a permission to execute it. */
    fun stillCurrent(plan: Plan, saved: JSONObject, live: JSONObject,
                     running: Boolean, recording: Boolean, monEnabled: Boolean,
                     displayPreviewActive: Boolean): Boolean {
        if (recording || monEnabled || displayPreviewActive || running && plan.requiresStop) return false
        return fingerprint(saved) == plan.expectedSavedFingerprint &&
            fingerprint(live) == plan.expectedLiveFingerprint
    }
}
