package de.brummbeobachter.app

import android.content.Context
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Window
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import kotlin.math.abs
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
    private var settingsVisible = false
    private var lastActivation = 0L
    private data class S18Stroke(val x: Float, val y: Float, var lastX: Float, var lastY: Float,
                                 var maxDx: Float = 0f, var maxDy: Float = 0f)
    private val strokes = mutableMapOf<Int, S18Stroke>()
    private const val S18_DESCRIPTOR = "1f9732f291ce3e3ddc26c59de5cc9968625367d8"
    private fun s18Key(direction: String) = "$S18_DESCRIPTOR|S18:$direction"
    /** Interpret only the known S18 pointer signals, not local screen touches.
      * Gesture classification is intentionally conservative: ambiguous swipes are ignored.
      * Does not consume MotionEvents, so Android pointer behavior stays unchanged.
      */
    fun onMotion(context: Context, event: MotionEvent) {
        val device = InputDevice.getDevice(event.deviceId) ?: return
        if (!device.isExternal || device.descriptor != S18_DESCRIPTOR ||
            (!event.isFromSource(InputDevice.SOURCE_MOUSE) &&
             !event.isFromSource(InputDevice.SOURCE_TOUCHPAD))) return
        val id = event.deviceId
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> strokes[id] = S18Stroke(event.x,event.y,event.x,event.y)
            MotionEvent.ACTION_MOVE -> strokes[id]?.let { stroke ->
                stroke.lastX = event.x; stroke.lastY = event.y
                stroke.maxDx = maxOf(stroke.maxDx, abs(event.x - stroke.x))
                stroke.maxDy = maxOf(stroke.maxDy, abs(event.y - stroke.y))
            }
            MotionEvent.ACTION_UP -> {
                val stroke = strokes.remove(id) ?: return
                val dx = event.x - stroke.x
                val dy = event.y - stroke.y
                val direction = when {
                    // Center sends a brief DOWN/UP near the reported fixed center.
                    stroke.maxDx < 35f && stroke.maxDy < 35f &&
                        abs(stroke.x - 402f) < 90f && abs(stroke.y - 995f) < 120f -> "MITTE"
                    abs(dx) < 80f && abs(dy) < 80f -> null
                    abs(dx) >= 90f && abs(dx) > abs(dy) * 1.6f -> if (dx > 0) "RECHTS" else "LINKS"
                    abs(dy) >= 90f && abs(dy) > abs(dx) * 1.6f -> if (dy > 0) "UNTEN" else "OBEN"
                    else -> null
                } ?: return
                recognizedS18 = "S18: $direction (dx=${dx.toInt()}, dy=${dy.toInt()})"
                val key = s18Key(direction)
                if (learning) {
                    learning = false
                    val edit = prefs(context).edit()
                    commands.forEach { if (mapping(context,it) == key) edit.remove(it) }
                    edit.putString(learnCommand,key).apply()
                    latestKey = recognizedS18
                } else if (enabled(context) && !BluetoothRemoteDiagnostic.listening && !settingsVisible) {
                    dispatchCommand(context, key, event.eventTime)
                }
            }
            MotionEvent.ACTION_CANCEL -> strokes.remove(id)
        }
    }
    var recognizedS18 by mutableStateOf("Noch keine S18-Richtung erkannt")
        private set
    private fun dispatchCommand(context: Context, key: String, whenMs: Long): Boolean {
        val command = commands.firstOrNull { mapping(context,it) == key } ?: return false
        if (whenMs - lastActivation < 280L) return true
        lastActivation = whenMs
        pendingAction = command
        actionNumber += 1
        if (command == "TRIGGER") lastTrigger = "Trigger empfangen: ${System.currentTimeMillis()} ms (Systemzeit)"
        return true
    }

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
        return dispatchCommand(context, key, event.eventTime)
    }
    fun label(command: String) = when(command) {
        "PAUSE" -> "Analyzer Pause / Fortsetzen"
        "SCREEN" -> "Screen speichern"
        "AUDIO" -> "Rolling-Audio speichern (bestehender Ablauf)"
        else -> "Mess-Trigger (Ereignis markieren)"
    }
    @Composable private fun ObserveSettingsDialog(context: Context) {
        val view = LocalView.current
        DisposableEffect(view) {
            settingsVisible = true
            val window = (view.parent as? DialogWindowProvider)?.window
            val original = window?.callback
            if (window != null && original != null) {
                val observer = object : Window.Callback by original {
                    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
                        onMotion(context,event)
                        return original.dispatchTouchEvent(event)
                    }
                    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
                        onMotion(context,event)
                        return original.dispatchGenericMotionEvent(event)
                    }
                    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                        val used = onKey(context,event)
                        return used || original.dispatchKeyEvent(event)
                    }
                }
                window.callback = observer
            }
            onDispose {
                settingsVisible = false
                if (window != null && original != null && window.callback !== original) window.callback = original
            }
        }
    }
    @Composable fun SettingsDialog(context: Context, onDismiss: () -> Unit) {
        var active by remember { mutableStateOf(enabled(context)) }
        var revision by remember { mutableIntStateOf(0) }
        AlertDialog(onDismissRequest = { stopLearning(); onDismiss() },
            title = { Text("Bluetooth Remote · Tastenbelegung") },
            text = {
                ObserveSettingsDialog(context)
                Column(Modifier.fillMaxWidth().heightIn(max=500.dp).verticalScroll(rememberScrollState())) {
                    Text("S18: OBEN, UNTEN, LINKS, RECHTS, MITTE durch Bewegung erkennbar. Erst eine Funktion auf 'Anlernen' setzen, dann S18-Taste drücken.", style=MaterialTheme.typography.bodySmall)
                    Text(recognizedS18, style=MaterialTheme.typography.bodySmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fernbedienung aktiviert")
                        Switch(checked=active, onCheckedChange={ active=it; setEnabled(context,it) })
                    }
                    Text("Nur von Android an die App zugestellte externe Tastensignale. Bei gesperrtem Display nicht für jedes Gerät garantiert.", style=MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    commands.forEach { command ->
                        Text(label(command), style=MaterialTheme.typography.titleSmall)
                        val assigned = mapping(context,command)
                        // Re-read mappings when learning completes (latestKey changes).
                        @Suppress("UNUSED_VARIABLE") val refreshToken = revision to latestKey
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
