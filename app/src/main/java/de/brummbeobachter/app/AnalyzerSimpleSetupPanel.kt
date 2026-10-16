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
    var selectedId by remember { mutableStateOf(listing?.activeId) }
    var menuOpen by remember { mutableStateOf(false) }
    var createName by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    var failedSwitchId by remember { mutableStateOf<String?>(null) }
    var failedSwitchReason by remember { mutableStateOf("") }
    val ink = Color(0xFF25384C)
    val sky = Color(0xFFF0F5FA)
    fun refresh() { listing = runCatching { repo.listing() }.getOrNull(); selectedId = listing?.activeId }
    fun saveCurrent(): Boolean {
        val result = AnalyzerActiveSetupPersistence.saveOnAnalyzerExit(context)
        if (result.isFailure)
            notice = "Vorheriges Setup konnte nicht gespeichert werden: ${result.exceptionOrNull()?.message ?: "unbekannter Fehler"}"
        return result.isSuccess
    }
    fun selectDraft(id: String, discardUnsaved: Boolean = false) {
        if (id == repo.listing().activeId) {
            selectedId = id
            notice = "Dieses Setup ist bereits zugeordnet."
            return
        }
        // No implicit interruption of measurement/recording or MON.
        if (LiveAnalyzerController.snapshot().running || AnalyzerMonitor.enabled || AnalyzerMonitorRecording.isActive()) {
            notice = "Setupwechsel nur bei STOP und ausgeschaltetem MON möglich"
            return
        }
        if (!discardUnsaved && !saveCurrent()) {
            failedSwitchId = id
            failedSwitchReason = notice
            return
        }
        runCatching {
            val groups = repo.load(id).getJSONObject("parameters")
            val analyzer = groups.optJSONObject("legacy") ?: error("Analyzer-Daten fehlen")
            val monitor = groups.optJSONObject("monitor") ?: error("MON-Daten fehlen")
            val outcome = AnalyzerStoppedJsonControllerPort.applyCombinedDraft(analyzer, monitor)
            when (outcome) {
                AnalyzerSetupSwitchTransaction.Outcome.Applied,
                AnalyzerSetupSwitchTransaction.Outcome.Unchanged -> {
                    // Commit active ID / MRU only after the complete STOP draft verifies.
                    repo.select(id)
                    refresh()
                    notice = "Setup im STOP-Zustand übernommen. Beim nächsten RUN wird das benannte JSON-Setup geprüft geladen."
                }
                is AnalyzerSetupSwitchTransaction.Outcome.Rejected -> error(outcome.reason)
                is AnalyzerSetupSwitchTransaction.Outcome.Failed -> error("${outcome.reason} (Rücknahme: ${outcome.rollbackSucceeded})")
            }
        }.onFailure { notice = "Setup nicht übernommen: ${it.message}" }
    }
    if (failedSwitchId != null) {
        AlertDialog(
            onDismissRequest = { failedSwitchId = null },
            title = { Text("Speichern fehlgeschlagen") },
            text = { Text("$failedSwitchReason\n\nWenn du das Setup trotzdem wechselst, gehen die nicht gespeicherten Änderungen verloren.") },
            confirmButton = {
                TextButton(onClick = {
                    val id = failedSwitchId ?: return@TextButton
                    failedSwitchId = null
                    if (!saveCurrent()) {
                        failedSwitchId = id
                        failedSwitchReason = notice
                    } else selectDraft(id, discardUnsaved = true)
                }) { Text("Erneut speichern") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        val id = failedSwitchId ?: return@TextButton
                        failedSwitchId = null
                        selectDraft(id, discardUnsaved = true)
                    }) { Text("Trotzdem wechseln") }
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
                if (repo.listing().activeId != null) {
                    require(saveCurrent()) { "Vorheriges Setup nicht gespeichert" }
                }
                val groups = JSONObject()
                    .put("legacy", AnalyzerRuntimeSetupSnapshot.capture(LiveAnalyzerController.snapshot()))
                    .put("monitor", AnalyzerMonitor.snapshotSetupParameters())
                val entry = repo.create(createName.trim(), groups)
                repo.select(entry.id)
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
