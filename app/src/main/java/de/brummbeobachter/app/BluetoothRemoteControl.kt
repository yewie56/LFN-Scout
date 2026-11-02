package de.brummbeobachter.app

import android.content.Context
import android.view.InputDevice
import android.view.KeyEvent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** User-configured HID keys. Activity-delivered events only; not a BLE GATT listener. */
internal object BluetoothRemoteControl {
    private const val PREFS = "bluetooth_remote_127"
    private val commands = listOf("PAUSE", "SCREEN", "AUDIO", "TRIGGER")
    var latestKey by mutableStateOf("Noch keine Taste erkannt")
        private set
    var lastTrigger by mutableStateOf("Noch kein Mess-Trigger")
        private set
    var actionNumber by mutableIntStateOf(0)
        private set
    var pendingAction by mutableStateOf("")
        private set
    private var learning = false
    private var lastActivation = 0L

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private fun keyId(event: KeyEvent): String {
        val descriptor = InputDevice.getDevice(event.deviceId)?.descriptor ?: "unknown"
        return "${descriptor}|${event.keyCode}|${event.scanCode}"
    }
    fun enabled(context: Context) = prefs(context).getBoolean("enabled", false)
    fun setEnabled(context: Context, value: Boolean) { prefs(context).edit().putBoolean("enabled", value).apply() }
    fun mapping(context: Context, command: String) = prefs(context).getString(command, "") ?: ""
    fun clear(context: Context, command: String) { prefs(context).edit().remove(command).apply() }
    fun beginLearning() { learning = true; latestKey = "Taste jetzt kurz drücken …" }
    fun stopLearning() { learning = false }
    private var learnCommand = ""
    fun learn(command: String) { learnCommand = command; beginLearning() }
    fun isLearning() = learning
    fun onKey(context: Context, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP) {
            val key = keyId(event)
            return enabled(context) && commands.any { mapping(context, it) == key } && !BluetoothRemoteDiagnostic.listening
        }
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount != 0) return false
        // Never intercept local keyboard, system/back/power, or unknown devices.
        val device = InputDevice.getDevice(event.deviceId) ?: return false
        if (!device.isExternal || event.keyCode in listOf(KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_HOME,
                KeyEvent.KEYCODE_POWER, KeyEvent.KEYCODE_MENU)) return false
        val key = keyId(event)
        latestKey = "${device.name}: ${KeyEvent.keyCodeToString(event.keyCode)} (scan ${event.scanCode})"
        if (learning) {
            learning = false
            // Reassigning a key removes any previous command for the same key.
            val edit = prefs(context).edit()
            commands.forEach { if (mapping(context, it) == key) edit.remove(it) }
            edit.putString(learnCommand, key).apply()
            return true
        }
        if (!enabled(context) || BluetoothRemoteDiagnostic.listening) return false
        val command = commands.firstOrNull { mapping(context, it) == key } ?: return false
        // Suppress nearby duplicates from remotes which send burst key sequences.
        if (event.eventTime - lastActivation < 280L) return true
        lastActivation = event.eventTime
        pendingAction = command
        actionNumber += 1
        if (command == "TRIGGER") {
            lastTrigger = "Trigger empfangen: ${System.currentTimeMillis()} ms (Systemzeit)"
        }
        return true
    }
    fun label(command: String) = when(command) {
        "PAUSE" -> "Analyzer Pause / Fortsetzen"
        "SCREEN" -> "Screen speichern"
        "AUDIO" -> "Rolling-Audio speichern (bestehender Ablauf)"
        else -> "Mess-Trigger (Ereignis markieren)"
    }
    @Composable fun SettingsDialog(context: Context, onDismiss: () -> Unit) {
        var active by remember { mutableStateOf(enabled(context)) }
        var revision by remember { mutableIntStateOf(0) }
        AlertDialog(onDismissRequest = { stopLearning(); onDismiss() },
            title = { Text("Bluetooth Remote · Tastenbelegung") },
            text = {
                Column(Modifier.fillMaxWidth().heightIn(max=500.dp).verticalScroll(rememberScrollState())) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fernbedienung aktiviert")
                        Switch(checked=active, onCheckedChange={ active=it; setEnabled(context,it) })
                    }
                    Text("Nur von Android an die App zugestellte externe Tastensignale. Bei gesperrtem Display nicht für jedes Gerät garantiert.", style=MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    commands.forEach { command ->
                        Text(label(command), style=MaterialTheme.typography.titleSmall)
                        val assigned = mapping(context,command)
                        Text(if (assigned.isBlank()) "Nicht belegt" else assigned, style=MaterialTheme.typography.bodySmall)
                        Row {
                            TextButton(onClick={ learn(command); revision++ }) { Text("Anlernen") }
                            TextButton(onClick={ clear(context,command); revision++ }) { Text("Löschen") }
                        }
                    }
                    Text(latestKey, style=MaterialTheme.typography.bodySmall)
                    if (isLearning()) Text("Anlernen aktiv – Taste drücken, danach Dialog erneut öffnen, um Belegung zu prüfen.")
                    Text(lastTrigger, style=MaterialTheme.typography.bodySmall)
                }
            }, confirmButton = { TextButton(onClick={ stopLearning(); onDismiss() }) { Text("Schließen") } })
    }
}
