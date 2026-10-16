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
    val ink = Color(0xFF25384C)
    val sky = Color(0xFFF0F5FA)
    fun refresh() { listing = runCatching { repo.listing() }.getOrNull() }
    fun saveCurrent(): Boolean {
        val result = AnalyzerActiveSetupPersistence.saveOnAnalyzerExit(context)
        if (result.isFailure) notice = "Vorheriges Setup konnte nicht gespeichert werden: ${result.exceptionOrNull()?.message}"
        return result.isSuccess
    }
    fun selectDraft(id: String) {
        if (id == repo.listing().activeId) {
            selectedId = id
            notice = "Bereits ausgewählt"
            return
        }
        // No implicit interruption of measurement/recording or MON.
        if (LiveAnalyzerController.snapshot().running || AnalyzerMonitor.enabled || AnalyzerMonitorRecording.isActive()) {
            notice = "Setupwechsel nur bei STOP und ausgeschaltetem MON möglich"
            return
        }
        if (!saveCurrent()) return
        runCatching {
            val groups = repo.load(id).getJSONObject("parameters")
            val analyzer = groups.optJSONObject("legacy") ?: error("Analyzer-Daten fehlen")
            val monitor = groups.optJSONObject("monitor") ?: error("MON-Daten fehlen")
            val outcome = AnalyzerStoppedJsonControllerPort.applyCombinedDraft(analyzer, monitor)
            when (outcome) {
                AnalyzerSetupSwitchTransaction.Outcome.Applied,
                AnalyzerSetupSwitchTransaction.Outcome.Unchanged -> {
                    // Do not claim durable activation: START still uses legacy preferences.
                    repo.select(id)
                    selectedId = id
                    refresh()
                    notice = "STOP-Entwurf geladen. Vollständiger Messstart mit diesem Setup noch nicht freigegeben."
                }
                is AnalyzerSetupSwitchTransaction.Outcome.Rejected -> error(outcome.reason)
                is AnalyzerSetupSwitchTransaction.Outcome.Failed -> error("${outcome.reason} (Rücknahme: ${outcome.rollbackSucceeded})")
            }
        }.onFailure { notice = "Setup nicht übernommen: ${it.message}" }
    }
    val data = listing
    Column(
        modifier = Modifier.fillMaxWidth().background(sky, RoundedCornerShape(12.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Meine Analyzer-Setups", color = ink)
        val selectedName = data?.entries?.firstOrNull { it.id == selectedId }?.name ?: "Noch kein Setup gewählt"
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
            label = { Text("Neues Setup benennen") }, modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Button(onClick = {
            runCatching {
                require(createName.isNotBlank()) { "Name fehlt" }
                require(saveCurrent()) { "Vorheriges Setup nicht gespeichert" }
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
        Text("Hinweis: Ein vollständiger, dauerhaft wirksamer Analyzer-Setupwechsel einschließlich Messstart ist technisch noch nicht freigegeben.", color = Color(0xFF835623))
        if (notice.isNotBlank()) Text(notice, color = ink)
    }
}
