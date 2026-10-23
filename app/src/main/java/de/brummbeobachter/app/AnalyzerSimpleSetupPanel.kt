package de.brummbeobachter.app

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.json.JSONObject

/** A compact, named-JSON-only user interface. No second slot system.
 * The STOP draft is explicitly not advertised as an audio-persistent activation.
 */
@Composable
internal fun AnalyzerSimpleSetupPanel(context: Context) {
    val repo = remember(context) { AnalyzerJsonSetupRepository(context.applicationContext) }
    var listing by remember { mutableStateOf(runCatching { repo.listing() }.getOrNull()) }
    fun currentId() = repo.selectedForMode(LiveAnalyzerController.snapshot().direct48k)
    var selectedId by remember { mutableStateOf(currentId()) }
    var menuOpen by remember { mutableStateOf(false) }
    var createName by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    var runSwitchWarning by remember { mutableStateOf(false) }
    var failedSwitchId by remember { mutableStateOf<String?>(null) }
    var failedSwitchReason by remember { mutableStateOf("") }
    val ink = Color(0xFF25384C)
    val sky = Color(0xFFF0F5FA)
    fun refresh() { listing = runCatching { repo.listing() }.getOrNull(); selectedId = currentId() }
    fun saveCurrent(): Boolean {
        val result = AnalyzerActiveSetupPersistence.saveOnAnalyzerExit(context)
        if (result.isFailure)
            notice = "Vorheriges Setup konnte nicht gespeichert werden: ${result.exceptionOrNull()?.message ?: "unbekannter Fehler"}"
        return result.isSuccess
    }
    fun selectDraft(id: String, savedBeforeSelection: Boolean = false) {
        if (id == currentId()) {
            selectedId = id
            notice = "Dieses Setup ist bereits zugeordnet."
            return
        }
        if (LiveAnalyzerController.snapshot().running) {
            runSwitchWarning = true
            return
        }
        // A MON WAV recording cannot safely be paused by this synchronous setup UI.
        // Never close a recording or discard its data implicitly.
        if (AnalyzerMonitorRecording.isActive()) {
            notice = "MON-Aufnahme zuerst beenden; Rolling läuft weiter"
            return
        }
        // Save the old JSON BEFORE temporarily switching off live MON. The JSON
        // contains MON parameters; stopping playback must not rewrite them.
        if (!savedBeforeSelection && !saveCurrent()) {
            failedSwitchId = id
            failedSwitchReason = notice
            return
        }
        // A controller rollback must also restore the repository selection. A
        // failed commit must never leave the index pointing at another setup.
        val directMode = LiveAnalyzerController.snapshot().direct48k
        val previousModeId = repo.selectedForMode(directMode)
        val previousActiveId = repo.listing().activeId
        val monWasEnabled = AnalyzerMonitor.enabled
        val monWasShifted = AnalyzerMonitor.shifted
        val oldAnalyzer = AnalyzerRuntimeSetupSnapshot.capture(LiveAnalyzerController.snapshot())
        val oldMonitor = AnalyzerMonitor.snapshotSetupParameters()
        try {
            val groups = repo.load(id).getJSONObject("parameters")
            val analyzer = groups.optJSONObject("legacy") ?: error("Analyzer-Daten fehlen")
            val monitor = groups.optJSONObject("monitor") ?: error("MON-Daten fehlen")
            require(AnalyzerModeSetupGuard.compatible(analyzer, directMode)) {
                "Setup passt nicht zum Analyzer-Modus: " + AnalyzerModeSetupGuard.reason(analyzer, directMode)
            }
            // STOP only the MON playback subscription while the strict setup
            // transaction is in flight. The shared rolling PCM source is untouched.
            if (monWasEnabled) AnalyzerMonitor.stop()
            val outcome = AnalyzerStoppedJsonControllerPort.applyCombinedDraft(analyzer, monitor)
            when (outcome) {
                AnalyzerSetupSwitchTransaction.Outcome.Applied,
                AnalyzerSetupSwitchTransaction.Outcome.Unchanged -> {
                    repo.selectForMode(id, directMode)
                    check(repo.selectedForMode(directMode) == id) { "Setup-Index nach Auswahl nicht bestätigt" }
                    refresh()
                    notice = "Setup übernommen; MON und Rolling bleiben im bisherigen Betriebszustand."
                }
                is AnalyzerSetupSwitchTransaction.Outcome.Rejected -> error(outcome.reason)
                is AnalyzerSetupSwitchTransaction.Outcome.Failed -> error("${outcome.reason} (Rücknahme: ${outcome.rollbackSucceeded})")
            }
        } catch (failure: Exception) {
            // Restore controller state AND original repository selection if a
            // later step failed after the index commit. Do not hide rollback errors.
            val controllerRollback = runCatching {
                LiveAnalyzerController.applyStoppedJsonDraft(oldAnalyzer)
                AnalyzerMonitor.applySetupDraft(oldMonitor)
                check(AnalyzerSetupExactComparison.sameComplete(oldAnalyzer,
                    AnalyzerRuntimeSetupSnapshot.capture(LiveAnalyzerController.snapshot())))
                check(AnalyzerSetupExactComparison.sameComplete(oldMonitor,
                    AnalyzerMonitor.snapshotSetupParameters()))
            }
            val indexRollback = runCatching {
                if (previousActiveId != null && repo.listing().activeId != previousActiveId) {
                    repo.select(previousActiveId)
                }
                check(repo.listing().activeId == previousActiveId) {
                    "Alter Setup-Index konnte nicht wiederhergestellt werden"
                }
                repo.restoreModePin(directMode, previousModeId)
                refresh()
            }
            notice = "Setup nicht übernommen: ${failure.message}" +
                (if (controllerRollback.isFailure) "; Controller-Rücknahme fehlgeschlagen: ${controllerRollback.exceptionOrNull()?.message}" else "") +
                (if (indexRollback.isFailure) "; Index-Rücknahme fehlgeschlagen: ${indexRollback.exceptionOrNull()?.message}" else "")
        } finally {
            // Preserve the user's explicit MON EIN/AUS and shift selection.
            // New setup's MON parameter values apply on success; the previous
            // values apply on failure. No start/stop of Rolling occurs here.
            if (monWasEnabled) {
                val restored = runCatching {
                    check(AnalyzerMonitor.start(context)) { "MON-Neustart nicht möglich" }
                    if (monWasShifted) AnalyzerMonitor.toggleShift()
                }
                if (restored.isFailure) notice += "; MON-Wiederanlauf fehlgeschlagen: ${restored.exceptionOrNull()?.message}"
            }
        }
    }
    if (runSwitchWarning) {
        AlertDialog(
            onDismissRequest = { runSwitchWarning = false },
            title = { Text("Analyzer zuerst stoppen") },
            text = { Text("Der Setupwechsel ist nur bei Analyzer STOP möglich. Bitte zuerst den Analyzer stoppen. Rolling kann weiterlaufen.") },
            confirmButton = { TextButton(onClick = { runSwitchWarning = false }) { Text("OK") } }
        )
    }
    if (failedSwitchId != null) {
        AlertDialog(
            onDismissRequest = { failedSwitchId = null },
            title = { Text("Speichern fehlgeschlagen") },
            text = { Text("$failedSwitchReason\n\nEin Wechsel ist erst nach erfolgreichem Speichern möglich.") },
            confirmButton = {
                TextButton(onClick = {
                    val id = failedSwitchId ?: return@TextButton
                    failedSwitchId = null
                    if (!saveCurrent()) {
                        failedSwitchId = id
                        failedSwitchReason = notice
                    } else selectDraft(id, savedBeforeSelection = true)
                }) { Text("Erneut speichern") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { failedSwitchId = null }) { Text("Abbrechen") }
                }
            }
        )
    }
    val data = listing
    Column(
        modifier = Modifier.fillMaxWidth().background(sky, RoundedCornerShape(12.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Meine Analyzer-Setups", color = ink)
        val selectedName = data?.entries?.firstOrNull { it.id == data?.activeId }?.name ?: "Kein aktives JSON-Setup"
        Text("Zugeordnetes Setup: $selectedName", color = ink)
        Text("Einmal auf einen Namen tippen: Werte werden bei STOP übernommen.", color = ink)
        OutlinedButton(onClick = { menuOpen = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$selectedName  ▾", color = ink)
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            data?.entries?.forEach { entry ->
                DropdownMenuItem(text = { Text(entry.name) }, onClick = {
                    menuOpen = false
                    selectDraft(entry.id)
                })
            }
        }
        if (!data?.recent.isNullOrEmpty()) {
            Text("Zuletzt verwendet", color = ink)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                data?.recent?.forEach { id ->
                    val entry = data?.entries?.firstOrNull { it.id == id }
                    if (entry != null) OutlinedButton(
                        onClick = { selectDraft(id) }, modifier = Modifier.weight(1f)
                    ) { Text(entry.name.take(10), color = ink) }
                }
            }
        }
        OutlinedTextField(
            value = createName, onValueChange = { createName = it.take(120) },
            label = { Text("Aktuelle Einstellungen speichern unter …") }, modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Button(onClick = {
            runCatching {
                require(createName.isNotBlank()) { "Name fehlt" }
                // Creating and selecting a setup is also a setup transition.
                // Apply the same STOP/recording safeguards as selectDraft().
                require(!LiveAnalyzerController.snapshot().running) {
                    "Analyzer zuerst stoppen; Setup wurde nicht angelegt"
                }
                require(!AnalyzerMonitorRecording.isActive()) {
                    "MON-Aufnahme zuerst beenden; Setup wurde nicht angelegt"
                }
                if (repo.listing().activeId != null) {
                    require(saveCurrent()) { "Vorheriges Setup nicht gespeichert" }
                }
                val groups = JSONObject()
                    .put("legacy", AnalyzerRuntimeSetupSnapshot.capture(LiveAnalyzerController.snapshot()))
                    .put("monitor", AnalyzerMonitor.snapshotSetupParameters())
                val entry = repo.create(createName.trim(), groups)
                repo.selectForMode(entry.id, LiveAnalyzerController.snapshot().direct48k)
                selectedId = entry.id
                createName = ""
                refresh()
                notice = "Neues JSON-Setup angelegt; Speicherung beim Verlassen des Analyzers aktiv"
            }.onFailure { notice = "Anlegen fehlgeschlagen: ${it.message}" }
        }, enabled = createName.isNotBlank()) { Text("+ Neues Setup") }
        OutlinedButton(onClick = {
            if (selectedId == null) notice = "Bitte zuerst ein Setup anlegen"
            else if (saveCurrent()) notice = "Aktives JSON-Setup gespeichert"
        }) { Text("Jetzt sichern") }
        Text("Änderungen werden beim Verlassen des Analyzers im aktiven JSON-Setup gespeichert.", color = ink)
        Text("Rolling-Aufnahme und rückwirkender Puffer bleiben beim Analyzer-Setupwechsel aktiv.", color = Color(0xFF835623))
        if (notice.isNotBlank()) Text(notice, color = ink)
    }
}
