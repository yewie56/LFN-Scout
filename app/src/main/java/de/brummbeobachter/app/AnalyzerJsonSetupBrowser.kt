package de.brummbeobachter.app

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedTextField
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
import org.json.JSONObject

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
    var newName by remember { mutableStateOf("") }
    var editName by remember { mutableStateOf("") }
    var askDelete by remember { mutableStateOf(false) }
    var pendingExport by remember { mutableStateOf<String?>(null) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
                // Bound imported JSON to 2 MiB; reject oversized documents before parsing.
                val out = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val count = stream.read(buffer)
                    if (count < 0) break
                    require(out.size() + count <= 2 * 1024 * 1024) { "JSON-Datei zu groß" }
                    out.write(buffer, 0, count)
                }
                out.toByteArray()
            } ?: error("Datei nicht lesbar")
            val entry = repository.importNew(bytes.toString(Charsets.UTF_8))
            listing = runCatching { repository.listing() }
            inspectedId = entry.id
            details = "Importiert: ${entry.name} (neue ID, keine vorhandene Datei überschrieben)"
            warning = "JSON-Setup importiert; noch nicht auf Analyzer angewendet."
        }.onFailure { warning = "Import fehlgeschlagen: ${it.message}" }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val id = pendingExport
        pendingExport = null
        if (uri != null && id != null) runCatching {
            context.contentResolver.openOutputStream(uri, "wt")?.use {
                it.write(repository.exportCopy(id).toByteArray(Charsets.UTF_8))
            } ?: error("Zieldatei nicht beschreibbar")
            warning = "JSON exportiert."
        }.onFailure { warning = "Export fehlgeschlagen: ${it.message}" }
    }

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
        // An isolated MON draft switch is transactional: a failed index commit
        // must not leave a different live MON draft active in memory.
        val previousMonitor = AnalyzerMonitor.snapshotSetupParameters()
        val previousDraftId = monDraftId
        val previousBaseline = monBaseline
        val previousSessionId = AnalyzerMonDraftSession.setupId
        val previousSessionBaseline = AnalyzerMonDraftSession.baseline
        val previousSelection = repository.listing()
        val previousActiveId = previousSelection.activeId
        runCatching {
            check(AnalyzerDisplayPreviewSession.activeSetupId == null) {
                "Anzeige-Vorschau zuerst speichern oder verwerfen"
            }
            check(!AnalyzerMonitorRecording.isActive()) { "MON-Aufnahme zuerst beenden" }
            check(!AnalyzerMonitor.enabled) { "MON vor dem Setupwechsel stoppen" }
            val doc = repository.load(id)
            val mon = doc.getJSONObject("parameters").optJSONObject("monitor")
                ?: error("Dieses Setup enthält keine MON-Parameter")
            // Validate and apply through the actual production MON draft path.
            AnalyzerMonitor.applySetupDraft(mon)
            // Commit selection before publishing draft identity and success status.
            repository.select(id)
            check(repository.listing().activeId == id) { "Setup-Index nicht bestätigt" }
            val newBaseline = AnalyzerMonitor.snapshotSetupParameters().toString()
            monDraftId = id
            monBaseline = newBaseline
            AnalyzerMonDraftSession.setupId = id
            AnalyzerMonDraftSession.baseline = newBaseline
            warning = "MON-Entwurf geladen (nur MON; übriger Analyzer unverändert)."
            refresh()
        }.onFailure { cause ->
            val monitorRollback = runCatching {
                AnalyzerMonitor.applySetupDraft(previousMonitor)
                check(AnalyzerMonitor.snapshotSetupParameters().toString() == previousMonitor.toString()) {
                    "MON-Rücknahme nicht bestätigt"
                }
            }
            val indexRollback = runCatching {
                if (repository.listing().activeId != previousActiveId ||
                    repository.listing().recent != previousSelection.recent) {
                    repository.restoreSelection(previousActiveId, previousSelection.recent)
                }
                check(repository.listing().activeId == previousActiveId &&
                    repository.listing().recent == previousSelection.recent) {
                    "Setup-Index-/MRU-Rücknahme fehlgeschlagen"
                }
            }
            monDraftId = previousDraftId
            monBaseline = previousBaseline
            AnalyzerMonDraftSession.setupId = previousSessionId
            AnalyzerMonDraftSession.baseline = previousSessionBaseline
            refresh()
            warning = "MON-Entwurf nicht aktiviert: ${cause.message}" +
                (if (monitorRollback.isFailure) "; MON-Rücknahme FEHLGESCHLAGEN: ${monitorRollback.exceptionOrNull()?.message}" else "") +
                (if (indexRollback.isFailure) "; Index-Rücknahme FEHLGESCHLAGEN: ${indexRollback.exceptionOrNull()?.message}" else "")
        }
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
    if (askDelete && inspectedId != null) {
        val target = inspectedId!!
        AlertDialog(onDismissRequest = { askDelete = false },
            title = { Text("JSON-Setup löschen?") },
            text = { Text("Das ausgewählte Setup wird zuvor automatisch im internen Backup-Verzeichnis gesichert. Alte Slot-Setups bleiben unverändert.") },
            confirmButton = { OutlinedButton(onClick = {
                askDelete = false
                runCatching {
                    repository.delete(target)
                    inspectedId = null
                    details = ""
                    refresh()
                    warning = "Setup gelöscht; Sicherheitskopie intern gespeichert."
                }.onFailure { warning = "Löschen fehlgeschlagen: ${it.message}" }
            }) { Text("Löschen") } },
            dismissButton = { OutlinedButton(onClick = { askDelete = false }) { Text("Abbrechen") } })
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
        Text("JSON-Setups · Verwaltung und MON-Teilaktivierung", color = Color(0xFFFFB33B), fontSize = 15.sp)
        Text("Slots 1–4 bleiben für den restlichen Analyzer maßgeblich. MON wird ausschließlich nach Bestätigung als Entwurf geladen. Änderungen schreiben erst bei 'MON speichern' in JSON.",
            color = Color(0xFFABB8C0), fontSize = 12.sp)
        Text("Eigene JSON-Setups verwalten (unabhängig von den alten Slots)", color = Color.White, fontSize = 13.sp)
        OutlinedTextField(value = newName, onValueChange = { newName = it.take(120) },
            label = { Text("Neues Setup: Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedButton(onClick = {
            runCatching {
                require(newName.isNotBlank()) { "Bitte einen Namen eingeben" }
                val snap = LiveAnalyzerController.snapshot()
                val parameters = JSONObject()
                    .put("legacy", AnalyzerRuntimeSetupSnapshot.capture(snap))
                    .put("monitor", AnalyzerMonitor.snapshotSetupParameters())
                val entry = repository.create(newName, parameters)
                newName = ""
                refresh()
                inspect(entry.id)
                warning = "Aktuellen Analyzer/MON-Zustand als eigenes JSON gespeichert. Ein späteres Laden ist derzeit nur teilweise möglich."
            }.onFailure { warning = "Anlegen fehlgeschlagen: ${it.message}" }
        }, enabled = newName.isNotBlank()) { Text("+ Aktuelle Einstellungen als JSON speichern") }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }) {
                Text("JSON importieren")
            }
            OutlinedButton(onClick = { refresh() }) { Text("Aktualisieren") }
        }

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
                OutlinedTextField(value = editName, onValueChange = { editName = it.take(120) },
                    label = { Text("Neuer Name (optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    OutlinedButton(onClick = {
                        runCatching {
                            require(editName.isNotBlank()) { "Name fehlt" }
                            val doc = repository.load(checkNotNull(inspectedId))
                            doc.put("name", editName.trim())
                            repository.save(doc)
                            editName = ""
                            refresh()
                            inspect(doc.getString("id"))
                        }.onFailure { warning = "Umbenennen fehlgeschlagen: ${it.message}" }
                    }, enabled = editName.isNotBlank()) { Text("Umbenennen") }
                    OutlinedButton(onClick = {
                        val id = checkNotNull(inspectedId)
                        pendingExport = id
                        exportLauncher.launch("LFN-Scout-Setup-${id.take(8)}.json")
                    }) { Text("Export") }
                    OutlinedButton(onClick = { askDelete = true }) { Text("Löschen") }
                }
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
                OutlinedButton(onClick = {
                    runtimeComparison = runCatching {
                        val saved = repository.load(checkNotNull(inspectedId))
                            .getJSONObject("parameters").optJSONObject("legacy")
                            ?: error("Setup enthält keine Analyzer-Parameter")
                        val currentState = LiveAnalyzerController.snapshot()
                        val live = AnalyzerRuntimeSetupSnapshot.capture(currentState)
                        val report = AnalyzerSetupSwitchPreflight.assess(
                            saved, live, currentState.running, AnalyzerMonitorRecording.isActive())
                        "Umschaltprüfung (nur lesen): " +
                            "Anzeige ${report.displayPreviewable.size}, " +
                            "STOP/Neustart ${report.stopRequired.size}, " +
                            "noch nicht unterstützt ${report.unsupported.size}, " +
                            "fehlend/null ${report.missingOrNull.size}.\n" +
                            "STOP/Neustart: ${report.stopRequired.joinToString(", ").ifEmpty { "keine" }}\n" +
                            "Nicht unterstützt: ${report.unsupported.joinToString(", ").ifEmpty { "keine" }}\n" +
                            "Aufnahme aktiv: ${report.recordingActive}; Messung läuft: ${report.measurementRunning}. " +
                            "Vollständiges Anwenden weiterhin gesperrt."
                    }.getOrElse { "Umschaltprüfung fehlgeschlagen: ${it.message}" }
                }) { Text("Audio/FFT-Umschaltprüfung (nur lesen)") }

                OutlinedButton(onClick = {
                    runtimeComparison = runCatching {
                        val saved = repository.load(checkNotNull(inspectedId))
                            .getJSONObject("parameters").optJSONObject("legacy")
                            ?: error("Setup enthält keine Analyzer-Parameter")
                        val state = LiveAnalyzerController.snapshot()
                        val live = AnalyzerRuntimeSetupSnapshot.capture(state)
                        val plan = AnalyzerSetupSwitchPlan.prepare(saved, live, state.running,
                            AnalyzerMonitorRecording.isActive(), AnalyzerMonitor.enabled,
                            AnalyzerDisplayPreviewSession.activeSetupId != null)
                        "Audio/FFT-Ablaufplan (nur lesen, nicht ausführbar):\n" +
                            plan.steps.mapIndexed { i, step -> "${i + 1}. $step" }.joinToString("\n") +
                            "\nSperren: " + plan.blockers.joinToString("; ").ifEmpty { "keine aktuell erkannt" } +
                            "\nFreigabe zur Ausführung: NEIN (Transaktions-Executor noch nicht implementiert)."
                    }.getOrElse { "Umschaltplan nicht verfügbar: ${it.message}" }
                }) { Text("Audio/FFT-Ablaufplan (nur lesen)") }

                OutlinedButton(onClick = {
                    warning = runCatching {
                        val saved = repository.load(checkNotNull(inspectedId))
                            .getJSONObject("parameters").optJSONObject("legacy")
                            ?: error("Setup enthält keine Analyzer-Parameter")
                        when (val outcome = AnalyzerStoppedJsonControllerPort.apply(saved)) {
                            AnalyzerSetupSwitchTransaction.Outcome.Applied ->
                                "STOP-Entwurf übernommen (nur im Speicher). Nicht dauerhaft aktiv: START kann Legacy-Werte laden."
                            AnalyzerSetupSwitchTransaction.Outcome.Unchanged ->
                                "Keine Änderung: JSON entspricht der Laufzeit."
                            is AnalyzerSetupSwitchTransaction.Outcome.Rejected ->
                                "STOP-Entwurf abgelehnt: ${outcome.reason}"
                            is AnalyzerSetupSwitchTransaction.Outcome.Failed ->
                                "STOP-Entwurf fehlgeschlagen: ${outcome.reason}; Rücknahme=${outcome.rollbackSucceeded}"
                        }
                    }.getOrElse { "STOP-Entwurf fehlgeschlagen: ${it.message}" }
                }, enabled = !LiveAnalyzerController.snapshot().running &&
                    !AnalyzerMonitor.enabled && !AnalyzerMonitorRecording.isActive()) {
                    Text("JSON als temporären STOP-Entwurf anwenden")
                }
                OutlinedButton(onClick = {
                    warning = runCatching {
                        val groups = repository.load(checkNotNull(inspectedId)).getJSONObject("parameters")
                        val legacy = groups.optJSONObject("legacy") ?: error("Analyzer-Parameter fehlen")
                        val monitor = groups.optJSONObject("monitor") ?: error("MON-Parameter fehlen")
                        when (val result = AnalyzerStoppedJsonControllerPort.applyCombinedDraft(legacy, monitor)) {
                            AnalyzerSetupSwitchTransaction.Outcome.Applied ->
                                "Analyzer und MON als STOP-Entwurf übernommen (nicht dauerhaft gespeichert)."
                            AnalyzerSetupSwitchTransaction.Outcome.Unchanged ->
                                "Analyzer unverändert; MON-Entwurf geprüft/übernommen (nicht dauerhaft gespeichert)."
                            is AnalyzerSetupSwitchTransaction.Outcome.Rejected ->
                                "Setup abgelehnt: ${result.reason}"
                            is AnalyzerSetupSwitchTransaction.Outcome.Failed ->
                                "Setup fehlgeschlagen: ${result.reason}; Rücknahme=${result.rollbackSucceeded}"
                        }
                    }.getOrElse { "Kombinierter STOP-Entwurf fehlgeschlagen: ${it.message}" }
                }, enabled = !LiveAnalyzerController.snapshot().running &&
                    !AnalyzerMonitor.enabled && !AnalyzerMonitorRecording.isActive()) {
                    Text("Analyzer + MON gemeinsam als STOP-Entwurf anwenden")
                }
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
