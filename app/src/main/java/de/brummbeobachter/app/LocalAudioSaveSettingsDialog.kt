package de.brummbeobachter.app

import android.content.Context
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** The same local-only save duration / overall statistics range for both panels. */
@Composable
internal fun LocalAudioSaveSettingsDialog(context: Context, onClose: (LocalAudioSaveSettings.Values?) -> Unit) {
    val current = remember { LocalAudioSaveSettings.load(context) }
    var duration by remember { mutableStateOf(current.durationSeconds.toString()) }
    var min by remember { mutableStateOf(current.minHz.toString()) }
    var max by remember { mutableStateOf(current.maxHz.toString()) }
    var error by remember { mutableStateOf("") }
    var exportFilterCsv by remember { mutableStateOf(current.exportWavFilterCsv) }
    val rollingCurrent = remember { RollingStatisticsSettings.load(context) }
    var rollingEnabled by remember { mutableStateOf(rollingCurrent.enabled) }
    var rollingMin by remember { mutableStateOf(rollingCurrent.minimumHz.toString()) }
    var rollingMax by remember { mutableStateOf(rollingCurrent.maximumHz.toString()) }
    var overallCsv by remember { mutableStateOf(rollingCurrent.exportOverallCsv) }
    var filterEnabled by remember { mutableStateOf(rollingCurrent.filterEnabled) }
    var filterMin by remember { mutableStateOf(rollingCurrent.filterMinimumHz.toString()) }
    var filterMax by remember { mutableStateOf(rollingCurrent.filterMaximumHz.toString()) }
    var filterCount by remember { mutableStateOf(rollingCurrent.filterCount.toString()) }
    var csvFilterMethod by remember { mutableStateOf(rollingCurrent.csvFilterMethod) }
    var filterCsv by remember { mutableStateOf(rollingCurrent.exportFilterCsv) }
    var epochCsv by remember { mutableStateOf(rollingCurrent.exportEpochCsv) }
    AlertDialog(onDismissRequest = { onClose(null) }, title = { Text("Audio / Statistik lokal") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(duration, { duration = it }, label = { Text("WAV-Dauer (30–600 s)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(min, { min = it }, label = { Text("Statistik MIN (1–199 Hz)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(max, { max = it }, label = { Text("Statistik MAX (2–200 Hz)") }, modifier = Modifier.fillMaxWidth())
                Row {
                    Checkbox(checked = exportFilterCsv, onCheckedChange = { exportFilterCsv = it })
                    Text("WAV-Filterstatistik als CSV exportieren (je WAV)", modifier = Modifier.padding(top = 12.dp))
                }
                Text("Rollingbuffer – Halbstundenstatistik")
                Row {
                    Checkbox(checked = rollingEnabled, onCheckedChange = { rollingEnabled = it })
                    Text("30-Minuten-Statistik aktivieren", modifier = Modifier.padding(top = 12.dp))
                }
                OutlinedTextField(rollingMin, { rollingMin = it }, label = { Text("Rolling-Statistik MIN (Hz)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(rollingMax, { rollingMax = it }, label = { Text("Rolling-Statistik MAX (Hz)") }, modifier = Modifier.fillMaxWidth())
                Row {
                    Checkbox(checked = overallCsv, onCheckedChange = { overallCsv = it })
                    Text("Halbstunden-Gesamtwerte als CSV (Downloads)", modifier = Modifier.padding(top = 12.dp))
                }
                Row {
                    Checkbox(checked = filterEnabled, onCheckedChange = { filterEnabled = it }, enabled = rollingEnabled)
                    Text("FFT-Frequenzbandstatistik für CSV aktivieren", modifier = Modifier.padding(top = 12.dp))
                }
                OutlinedTextField(filterMin, { filterMin = it }, label = { Text("Rolling-FILTER MIN (Hz; Standard 0)") }, modifier = Modifier.fillMaxWidth(), enabled = false)
                OutlinedTextField(filterMax, { filterMax = it }, label = { Text("Rolling-FILTER MAX (Hz; Standard 100)") }, modifier = Modifier.fillMaxWidth(), enabled = false)
                OutlinedTextField(filterCount, { filterCount = it }, label = { Text("Filterbänder (10 oder 20)") }, modifier = Modifier.fillMaxWidth(), enabled = false)
                Text("Legacy-PCM-Filterbank stillgelegt: Einstellungen bleiben gespeichert und sind ausgegraut; aktuelle CSV-Bänder stammen aus FFT-Leistungsdaten.")
                RollingFilterHalfHourStatistics.Method.entries.forEach { method ->
                    Row {
                        RadioButton(selected = csvFilterMethod == method,
                            onClick = { csvFilterMethod = method }, enabled = false)
                        Text(method.label, modifier = Modifier.padding(top = 12.dp))
                    }
                }
                Row {
                    Checkbox(checked = filterCsv, onCheckedChange = { filterCsv = it }, enabled = false)
                    Text("Halbstunden-FFT-Bandwerte als CSV (Downloads)", modifier = Modifier.padding(top = 12.dp))
                }
                Row {
                    Checkbox(checked = epochCsv, onCheckedChange = { epochCsv = it }, enabled = rollingEnabled)
                    Text("Sekundenwerte-CSV (Gesamt + optionale Filter; Standard AUS)", modifier = Modifier.padding(top = 12.dp))
                }
                Text("CSV wird blockweise im Downloads-Ordner geschrieben. Neue Einstellungen gelten nach Neustart des Rollingbuffers.")
                Text("Rolling-Filter sind vorläufige Diagnosefilter; kein Cloud-Filterupload.")
                Text("WAV 1 kHz / 16 Bit / Mono. Filter MIN/MAX und bis zu 40 Bänder: bestehendes Analyzer-Filterbank-Setup. Keine neuen Cloud-Datenfelder.")
                if (error.isNotBlank()) Text(error, modifier = Modifier.padding(top = 6.dp))
            }
        },
        confirmButton = { TextButton(onClick = {
            try {
                val values = LocalAudioSaveSettings.Values(
                    duration.toInt(), min.toInt(), max.toInt(), exportFilterCsv)
                val rollingValues = RollingStatisticsSettings.Values(
                    enabled = rollingEnabled, minimumHz = rollingMin.toInt(),
                    maximumHz = rollingMax.toInt(), exportOverallCsv = overallCsv,
                    filterEnabled = filterEnabled, filterMinimumHz = filterMin.toInt(),
                    filterMaximumHz = filterMax.toInt(), filterCount = filterCount.toInt(),
                    exportFilterCsv = filterCsv, exportEpochCsv = epochCsv, csvFilterMethod = csvFilterMethod)
                rollingValues.validate()
                LocalAudioSaveSettings.store(context, values)
                RollingStatisticsSettings.store(context, rollingValues)
                onClose(values)
            } catch (e: Exception) { error = e.message ?: "Ungültige Eingabe" }
        }) { Text("Speichern") } },
        dismissButton = { TextButton(onClick = { onClose(null) }) { Text("Abbrechen") } })
}
