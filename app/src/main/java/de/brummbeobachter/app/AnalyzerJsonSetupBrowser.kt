package de.brummbeobachter.app

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Process-local reference for the currently active MON draft.
 * Retains identity across Compose recreation without writing to preferences.
 */
internal object AnalyzerMonDraftSession {
    var setupId: String? = null
    var baseline: String? = null
}

/** Phase 3G: JSON listing and explicitly confirmed MON-only draft switching.
 * Full Analyzer activation is deliberately NOT claimed before legacy runtime integration.
 */
@Composable
internal fun AnalyzerJsonSetupBrowser(context: Context) {
    val repository = remember(context) { AnalyzerJsonSetupRepository(context.applicationContext) }
    var listing by remember { mutableStateOf(runCatching { repository.listing() }) }
    var inspectedId by remember { mutableStateOf<String?>(null) }
    var details by remember { mutableStateOf("") }
    var runtimeComparison by remember { mutableStateOf("") }
    var displayPreviewId by remember { mutableStateOf(AnalyzerDisplayPreviewSession.activeSetupId) }
    var requestedDisplayPreview by remember { mutableStateOf<String?>(null) }
    var requestedDisplayFinish by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var monDraftId by remember { mutableStateOf(AnalyzerMonDraftSession.setupId) }
    var monBaseline by remember { mutableStateOf(AnalyzerMonDraftSession.baseline) }
    var requestedId by remember { mutableStateOf<String?>(null) }
    var warning by remember { mutableStateOf("") }
    var confirmActivation by remember { mutableStateOf(false) }

    fun refresh() { listing = runCatching { repository.listing() } }
    fun inspect(id: String) {
        inspectedId = id
        runtimeComparison = ""
        details = runCatching {
            val d = repository.load(id)
            val groups = d.getJSONObject("parameters")
            "ID: $id\nSchema: ${d.getInt("schemaVersion")}\n" +
                "Gruppen: ${groups.keys().asSequence().toList().sorted().joinToString(", ")}\n" +
                "Beschreibung: ${d.optString("description")}"
        }.getOrElse { "JSON nicht lesbar: ${it.message ?: it.javaClass.simpleName}" }
    }
    fun isDirty(): Boolean = monDraftId != null && monBaseline != null &&
        AnalyzerMonitor.snapshotSetupParameters().toString() != monBaseline
    fun saveDraft(): Boolean = runCatching {
        val id = checkNotNull(monDraftId)
        check(!AnalyzerMonitorRecording.isActive()) { "Während MON-Aufnahme nicht speichern" }
        val doc = repository.load(id)
        val groups = doc.getJSONObject("parameters")
        groups.put("monitor", AnalyzerMonitor.snapshotSetupParameters())
        repository.save(doc)
        monBaseline = AnalyzerMonitor.snapshotSetupParameters().toString()
        AnalyzerMonDraftSession.baseline = monBaseline
        refresh()
    }.onFailure { warning = "Speichern fehlgeschlagen: ${it.message}" }.isSuccess
    fun activate(id: String) {
        runCatching {
            check(AnalyzerDisplayPreviewSession.activeSetupId == null) {
                "Anzeige-Vorschau zuerst speichern oder verwerfen"
            }
            check(!AnalyzerMonitorRecording.isActive()) { "MON-Aufnahme zuerst beenden" }
            // No switching while the MON DSP is operating; prevents unexpected live jumps.
            check(!AnalyzerMonitor.enabled) { "MON vor dem Setupwechsel stoppen" }
            val doc = repository.load(id)
            val mon = doc.getJSONObject("parameters").optJSONObject("monitor")
                ?: error("Dieses Setup enthält keine MON-Parameter")
            // Reject partial MON payload, nulls and out-of-range values BEFORE changing runtime.
            AnalyzerMonitor.applySetupDraft(mon)
            monDraftId = id
            monBaseline = AnalyzerMonitor.snapshotSetupParameters().toString()
            AnalyzerMonDraftSession.setupId = id
            AnalyzerMonDraftSession.baseline = monBaseline
            warning = "MON-Entwurf geladen (nur MON; übriger Analyzer unverändert)."
            // Track last selected MON draft only after successful application.
            repository.select(id)
            refresh()
        }.onFailure { warning = "MON-Entwurf nicht aktiviert: ${it.message}" }
    }
    fun requestActivate(id: String) {
        if (AnalyzerDisplayPreviewSession.activeSetupId != null) {
            warning = "Anzeige-Vorschau zuerst speichern oder verwerfen"
            return
        }
        requestedId = id
        if (monDraftId != null && isDirty()) {
            confirmActivation = false // Save / discard / cancel dialog
        } else {
            confirmActivation = true // Explicit confirmation before first activation
        }
    }
    // Explicit opt-in: preview only 12 DISPLAY fields, with an explicit restore path.
    if (requestedDisplayPreview != null) {
        val pending = requestedDisplayPreview!!
        AlertDialog(onDismissRequest = { requestedDisplayPreview = null },
            title = { Text("Nur Anzeigeparameter als Vorschau übernehmen?") },
            text = { Text("Nur 12 unterstützte Anzeigeparameter werden temporär angewendet. Audio, FFT, Filterbank-DSP und MON bleiben unverändert. Keine Speicherung in JSON oder alten Einstellungen. Mit 'Anzeige zurücksetzen' wird der vorherige Anzeigezustand wiederhergestellt.") },
            confirmButton = { OutlinedButton(onClick = {
                requestedDisplayPreview = null
                runCatching {
                    val doc = repository.load(pending)
                    val legacy = doc.getJSONObject("parameters").optJSONObject("legacy")
                        ?: error("Dieses Setup enthält keine Analyzer-Anzeigeparameter")
                    val result = AnalyzerDisplayPreviewSession.begin(context, pending, legacy)
                    displayPreviewId = AnalyzerDisplayPreviewSession.activeSetupId
                    warning = "Anzeige-Vorschau aktiv: ${result.appliedKeys.size} Felder. " +
                        "Nicht unterstützt: ${result.excludedKeys.size}. Nicht gespeichert."
                }.onFailure { warning = "Anzeige-Vorschau fehlgeschlagen: ${it.message}" }
            }) { Text("Vorschau starten") } },
            dismissButton = { OutlinedButton(onClick = { requestedDisplayPreview = null }) { Text("Abbrechen") } })
    }
    if (requestedDisplayFinish) {
        AlertDialog(onDismissRequest = { requestedDisplayFinish = false },
            title = { Text("Anzeige-Vorschau beenden") },
            text = { Text("Speichern übernimmt ausschließlich die 12 unterstützten bzw. tatsächlich geladenen Anzeigeparameter ins ausgewählte JSON-Setup. Verwerfen stellt den Zustand vor Vorschau-Beginn wieder her. Abbrechen behält die Vorschau bei. Andere Setup-Parameter und alte SharedPreferences bleiben unverändert.") },
            confirmButton = { OutlinedButton(onClick = {
                runCatching {
                    val count = AnalyzerDisplayPreviewSession.saveToSetup(repository)
                    displayPreviewId = AnalyzerDisplayPreviewSession.activeSetupId
                    requestedDisplayFinish = false
                    refresh()
                    warning = "Anzeige gespeichert ($count Parameter)."
                }.onFailure { warning = "Anzeige nicht gespeichert: ${it.message}" }
            }) { Text("Speichern") } },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(onClick = {
                        runCatching {
                            AnalyzerDisplayPreviewSession.restore(context)
                            displayPreviewId = AnalyzerDisplayPreviewSession.activeSetupId
                            requestedDisplayFinish = false
                            warning = "Anzeige-Vorschau verworfen; ursprüngliche Anzeige wiederhergestellt."
                        }.onFailure { warning = "Verwerfen fehlgeschlagen: ${it.message}" }
                    }) { Text("Verwerfen") }
                    OutlinedButton(onClick = { requestedDisplayFinish = false }) {
                        Text("Abbrechen")
                    }
                }
            })
    }
    if (requestedId != null) {
        val pending = requestedId!!
        if (!confirmActivation && isDirty()) {
            AlertDialog(onDismissRequest = { requestedId = null },
                title = { Text("Ungespeicherte MON-Änderungen") },
                text = { Text("Änderungen am bisherigen MON-Entwurf vor dem Wechsel speichern, verwerfen oder abbrechen?") },
                confirmButton = { OutlinedButton(onClick = {
                    if (saveDraft()) { requestedId = null; activate(pending) }
                }) { Text("Speichern") } },
                dismissButton = {
                    Row {
                        OutlinedButton(onClick = { requestedId = null; activate(pending) }) { Text("Verwerfen") }
                        OutlinedButton(onClick = { requestedId = null }) { Text("Abbrechen") }
                    }
                })
        } else {
            AlertDialog(onDismissRequest = { requestedId = null },
                title = { Text("MON-Entwurf anwenden?") },
                text = { Text("Nur die 16 MON-Werte werden übernommen. FFT, Audio und Anzeige bleiben unverändert. MON muss gestoppt sein.") },
                confirmButton = { OutlinedButton(onClick = { requestedId = null; activate(pending) }) { Text("MON anwenden") } },
                dismissButton = { OutlinedButton(onClick = { requestedId = null }) { Text("Abbrechen") } })
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("JSON-Setups · MON-Teilaktivierung", color = Color(0xFFFFB33B), fontSize = 15.sp)
        Text("Slots 1–4 bleiben für den restlichen Analyzer maßgeblich. MON wird ausschließlich nach Bestätigung als Entwurf geladen. Änderungen schreiben erst bei 'MON speichern' in JSON.",
            color = Color(0xFFABB8C0), fontSize = 12.sp)
        OutlinedButton(onClick = { refresh() }) { Text("JSON-Liste aktualisieren") }
        listing.onSuccess { data ->
            Text("${data.entries.size} JSON-Setups vorhanden", color = Color.White, fontSize = 12.sp)
            if (data.recent.isNotEmpty()) {
                Text("Zuletzt gewählte JSON-Setups:", color = Color(0xFFABB8C0), fontSize = 12.sp)
                data.recent.forEach { id ->
                    val entry = data.entries.firstOrNull { it.id == id }
                    if (entry != null) OutlinedButton(onClick = { inspect(id) }) { Text(entry.name.take(80)) }
                }
            }
            OutlinedButton(onClick = { expanded = !expanded }, enabled = data.entries.isNotEmpty()) {
                Text(if (expanded) "Liste schließen" else "Alle JSON-Setups anzeigen (${data.entries.size})")
            }
            if (expanded) Column(Modifier.fillMaxWidth().heightIn(max = 250.dp).verticalScroll(rememberScrollState())) {
                data.entries.forEach { entry ->
                    OutlinedButton(onClick = { inspect(entry.id) }, modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (inspectedId == entry.id) Color(0xFFFFB33B) else Color(0xFF34515B))) {
                        Text(entry.name, maxLines = 2)
                    }
                }
            }
            if (inspectedId != null) {
                Text(details, color = Color.White, fontSize = 12.sp)
                OutlinedButton(onClick = {
                    runtimeComparison = runCatching {
                        val doc = repository.load(checkNotNull(inspectedId))
                        val saved = doc.getJSONObject("parameters").optJSONObject("legacy")
                            ?: error("Setup enthält keine Analyzer-Parameter")
                        val current = AnalyzerRuntimeSetupSnapshot.capture(LiveAnalyzerController.snapshot())
                        val result = AnalyzerRuntimeSetupSnapshot.compare(saved, current)
                        "Laufzeitvergleich (nur lesend): ${result.changed.size} abweichend, " +
                            "${result.unchanged.size} gleich, ${result.notComparable.size} nicht vergleichbar.\n" +
                            "Abweichungen: ${result.changed.joinToString(", ").ifEmpty { "keine" }}\n" +
                            "Nicht vergleichbar: ${result.notComparable.joinToString(", ").ifEmpty { "keine" }}"
                    }.getOrElse { "Laufzeitvergleich fehlgeschlagen: ${it.message}" }
                }) { Text("Mit laufendem Analyzer vergleichen (nur lesen)") }
                if (runtimeComparison.isNotBlank()) Text(runtimeComparison, color = Color(0xFFABB8C0), fontSize = 12.sp)
                OutlinedButton(onClick = { requestedDisplayPreview = inspectedId },
                    enabled = displayPreviewId == null && !AnalyzerMonitor.enabled && !AnalyzerMonitorRecording.isActive()) {
                    Text("Anzeige-Vorschau (12 Werte, ohne Speichern)")
                }
                OutlinedButton(onClick = { requestActivate(inspectedId!!) }, enabled = !AnalyzerMonitor.enabled && !AnalyzerMonitorRecording.isActive()) {
                    Text("MON-Entwurf aus diesem Setup laden")
                }
            }
            if (displayPreviewId != null) {
                Text("Anzeige-Vorschau aktiv: ${data.entries.firstOrNull { it.id == displayPreviewId }?.name ?: displayPreviewId}",
                    color = Color(0xFFFFB33B), fontSize = 12.sp)
                Text("Zurücksetzen stellt den Zustand vor Vorschau-Beginn wieder her. Spätere manuelle Anzeigeänderungen gehen dabei ebenfalls verloren.",
                    color = Color(0xFFABB8C0), fontSize = 12.sp)
                Text("Abweichungen vom Zustand vor Vorschau: ${AnalyzerDisplayPreviewSession.changedKeys().size}",
                    color = Color(0xFFABB8C0), fontSize = 12.sp)
                OutlinedButton(onClick = { requestedDisplayFinish = true },
                    enabled = !AnalyzerMonitor.enabled && !AnalyzerMonitorRecording.isActive()) {
                    Text("Vorschau beenden: Speichern / Verwerfen / Abbrechen")
                }
                OutlinedButton(onClick = {
                    runCatching {
                        AnalyzerDisplayPreviewSession.restore(context)
                        displayPreviewId = AnalyzerDisplayPreviewSession.activeSetupId
                        warning = "Anzeigezustand vor der Vorschau wiederhergestellt."
                    }.onFailure { warning = "Anzeige nicht zurückgesetzt: ${it.message}" }
                }, enabled = !AnalyzerMonitor.enabled && !AnalyzerMonitorRecording.isActive()) {
                    Text("Anzeige zurücksetzen (nicht speichern)")
                }
            }
            if (monDraftId != null) {
                Text("MON-Entwurf aktiv: ${data.entries.firstOrNull { it.id == monDraftId }?.name ?: monDraftId}" +
                    if (isDirty()) " · geändert" else " · unverändert", color = Color(0xFFFFB33B), fontSize = 12.sp)
                OutlinedButton(onClick = { saveDraft() }, enabled = !AnalyzerMonitorRecording.isActive() && isDirty()) {
                    Text("MON speichern")
                }
            }
        }.onFailure { err ->
            Text("JSON-Index nicht lesbar: ${err.message ?: err.javaClass.simpleName}", color = Color(0xFFFFB33B), fontSize = 12.sp)
        }
        if (warning.isNotBlank()) Text(warning, color = Color(0xFFFFB33B), fontSize = 12.sp)
    }
}
