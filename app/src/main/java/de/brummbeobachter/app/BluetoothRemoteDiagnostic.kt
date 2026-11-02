package de.brummbeobachter.app

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.OutputStreamWriter
import android.view.InputDevice
import android.view.KeyEvent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Read-only diagnostic. No analyzer, recording or screenshot commands are dispatched. */
internal object BluetoothRemoteDiagnostic {
    var listening by mutableStateOf(false)
        private set
    private val events = mutableStateListOf<String>()
    private val devices = mutableStateListOf<String>()
    private val downTimes = mutableMapOf<Pair<Int, Int>, Long>()
    private val lastUps = mutableMapOf<Pair<Int, Int>, Long>()
    private const val LIMIT = 120

    private fun exportText(): String = buildString {
        appendLine("LFN-Scout v0.5.126 – Bluetooth Remote Diagnose")
        appendLine("Export: " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.GERMANY).format(Date()))
        appendLine("Status beim Export: " + if (listening) "Test aktiv" else "Test gestoppt")
        appendLine("Geräte (${devices.size}):")
        if (devices.isEmpty()) appendLine("Keine Tastatur-/DPAD-Geräte erkannt")
        devices.forEach { appendLine(it) }
        appendLine()
        appendLine("Ereignisse (${events.size}, älteste zuerst; maximal 120 im Speicher):")
        events.asReversed().forEach { appendLine(it) }
        appendLine()
        appendLine("Hinweis: Es werden nur tatsächlich an LFN-Scout zugestellte Android-Ereignisse protokolliert.")
    }

    fun start(context: Context) {
        events.clear()
        downTimes.clear()
        lastUps.clear()
        refreshDevices(context)
        listening = true
        log("Test gestartet. Ereignisse werden ausschließlich bei Android-Zustellung erfasst.")
    }

    fun stop() { listening = false; downTimes.clear() }

    fun refreshDevices(context: Context) {
        devices.clear()
        InputDevice.getDeviceIds().map { id -> InputDevice.getDevice(id) }.filterNotNull().forEach { device ->
            val source = device.sources
            if (source and InputDevice.SOURCE_KEYBOARD == InputDevice.SOURCE_KEYBOARD ||
                source and InputDevice.SOURCE_DPAD == InputDevice.SOURCE_DPAD) {
                devices.add("${device.name} | id=${device.id} | descriptor=${device.descriptor} | sources=$source")
            }
        }
    }

    fun onKey(event: KeyEvent): Boolean {
        if (!listening) return false
        val dev = InputDevice.getDevice(event.deviceId)
        val key = event.deviceId to event.keyCode
        val action = when (event.action) {
            KeyEvent.ACTION_DOWN -> "DOWN"
            KeyEvent.ACTION_UP -> "UP"
            else -> "ACTION_${event.action}"
        }
        var suffix = ""
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
            downTimes[key] = event.eventTime
            lastUps[key]?.let { if (event.eventTime - it in 0..400) suffix = " · erneuter Druck ≤400 ms" }
        } else if (event.action == KeyEvent.ACTION_UP) {
            downTimes.remove(key)?.let {
                val duration = event.eventTime - it
                suffix = " · ${duration} ms · " + if (duration >= 600) "lang" else "kurz"
            }
            lastUps[key] = event.eventTime
        }
        log("$action | ${dev?.name ?: "unbekannt"} | device=${event.deviceId} | key=${event.keyCode} (${KeyEvent.keyCodeToString(event.keyCode)}) | scan=${event.scanCode} | repeat=${event.repeatCount}$suffix")
        // Never steal system, volume, media or navigation keys; observing only.
        return false
    }

    private fun log(message: String) {
        val stamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.GERMANY).format(Date())
        events.add(0, "$stamp  $message")
        while (events.size > LIMIT) events.removeAt(events.lastIndex)
    }

    @Composable
    fun Dialog(onDismiss: () -> Unit) {
        val context = LocalContext.current
        var exportError by remember { mutableStateOf<String?>(null) }
        var exportSuccess by remember { mutableStateOf(false) }
        var pendingExport by remember { mutableStateOf("") }
        val saveLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/plain")
        ) { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        OutputStreamWriter(stream, Charsets.UTF_8).use { it.write(pendingExport) }
                    } ?: throw IllegalStateException("Datei konnte nicht geöffnet werden")
                    exportError = null
                    exportSuccess = true
                } catch (e: Exception) {
                    exportError = "Speichern fehlgeschlagen: ${e.message ?: e.javaClass.simpleName}"
                    exportSuccess = false
                }
            }
        }
        AlertDialog(
            onDismissRequest = { stop(); onDismiss() },
            title = { Text("Bluetooth Remote · Testmodus") },
            text = {
                Column(Modifier.fillMaxWidth().heightIn(max = 510.dp).verticalScroll(rememberScrollState())) {
                    Text("Nur Diagnose: Keine Taste löst eine Scout-Funktion aus.")
                    Spacer(Modifier.height(8.dp))
                    Text("Status: " + if (listening) "Empfangstest aktiv" else "Gestoppt")
                    Text("Display gesperrt/aus: nicht bestätigt. Android stellt normale HID-Tasten dabei möglicherweise nicht an diese Activity zu.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Erkannte Eingabegeräte", style = MaterialTheme.typography.titleSmall)
                    if (devices.isEmpty()) Text("Keine Tastatur-/DPAD-Geräte erkannt")
                    devices.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                    Spacer(Modifier.height(8.dp))
                    exportError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (exportSuccess) Text("Protokoll gespeichert.", color = MaterialTheme.colorScheme.primary)
                    Text("Empfangene Tastenereignisse (max. 120)", style = MaterialTheme.typography.titleSmall)
                    if (events.isEmpty()) Text("Noch keine Ereignisse")
                    events.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            },
            confirmButton = { TextButton(onClick = { stop(); onDismiss() }) { Text("Schließen") } },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        exportSuccess = false
                        exportError = null
                        pendingExport = exportText()
                        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.GERMANY).format(Date())
                        saveLauncher.launch("LFN-Scout-Bluetooth-Remote-$stamp.txt")
                    }) { Text("Protokoll speichern") }
                    TextButton(onClick = { if (listening) stop() else start(context) }) {
                        Text(if (listening) "Test stoppen" else "Test starten")
                    }
                }
            }
        )
    }

}
