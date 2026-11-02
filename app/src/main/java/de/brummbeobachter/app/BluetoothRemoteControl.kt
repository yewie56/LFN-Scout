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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

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
    // Updated from the Compose navigation state; never consume outside Scout/Analyzer.
    @Volatile private var activePanelIsRemoteSurface = false
    @Volatile private var activityInForeground = false
    fun updateRemoteSurface(panel: AppPanel) {
        activePanelIsRemoteSurface = panel == AppPanel.MAIN || panel == AppPanel.ANALYZER
        if (!activePanelIsRemoteSurface) strokes.clear()
    }
    fun setActivityForeground(active: Boolean) {
        activityInForeground = active
        if (!active) strokes.clear()
    }
    /** Entire S18 gesture is owned by LFN-Scout only on its two remote surfaces. */
    fun shouldConsumeMotion(context: Context, event: MotionEvent): Boolean {
        if (!activityInForeground || !activePanelIsRemoteSurface || settingsVisible ||
            BluetoothRemoteDiagnostic.listening || !enabled(context)) return false
        val device = InputDevice.getDevice(event.deviceId) ?: return false
        return device.isExternal && device.descriptor == S18_DESCRIPTOR &&
            (event.isFromSource(InputDevice.SOURCE_MOUSE) ||
             event.isFromSource(InputDevice.SOURCE_TOUCHPAD))
    }
    // Media buttons may arrive via Activity dispatch OR MediaSession, with different
    // device descriptors. Normalize their identity so one learned mapping works for both.
    private val mediaButtons = setOf(
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY,
        KeyEvent.KEYCODE_MEDIA_PAUSE, KeyEvent.KEYCODE_MEDIA_NEXT,
        KeyEvent.KEYCODE_MEDIA_PREVIOUS, KeyEvent.KEYCODE_MEDIA_STOP,
        KeyEvent.KEYCODE_HEADSETHOOK
    )
    private val volumeButtons = setOf(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN)
    fun isMediaButton(code: Int) = code in mediaButtons
    private fun normalizedMediaKey(code: Int): String = "MEDIA|$code"
    private var lastActivation = 0L
    private data class S18Stroke(val x: Float, val y: Float, var lastX: Float, var lastY: Float,
                                 var maxDx: Float = 0f, var maxDy: Float = 0f, val startedAtMs: Long = 0L)
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
            MotionEvent.ACTION_DOWN -> strokes[id] = S18Stroke(event.x,event.y,event.x,event.y, startedAtMs=event.eventTime)
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
                    // S18 center is a short, stationary DOWN/UP. Do not depend on
                    // absolute screen coordinates (Analyzer and Scout layouts differ).
                    event.eventTime - stroke.startedAtMs in 0L..350L &&
                        stroke.maxDx < 35f && stroke.maxDy < 35f &&
                        abs(dx) < 35f && abs(dy) < 35f -> "MITTE"
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
                } else if (shouldConsumeMotion(context, event)) {
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
        // PAUSE and TRIGGER are handled independently from the Compose analyzer screen.
        // SCREEN and AUDIO keep their established UI confirmation/save workflows.
        when (command) {
            "PAUSE" -> {
                val analyzer = LiveAnalyzerController.snapshot()
                if (!analyzer.running) {
                    DebugLogger.log(context, "BT_REMOTE_IGNORED", "PAUSE: Analyzer nicht RUN")
                    return true
                }
                val targetPaused = !analyzer.paused
                LiveAnalyzerController.setPaused(targetPaused)
                DebugLogger.log(context, "BT_REMOTE_PAUSE", "paused=$targetPaused")
            }
            "TRIGGER" -> {
                val timestampMs = System.currentTimeMillis()
                lastTrigger = "Trigger empfangen: $timestampMs ms (Systemzeit)"
                // An event marker, not an implemented special measurement algorithm.
                DebugLogger.log(context, "BT_REMOTE_TRIGGER", "timestampMs=$timestampMs")
            }
            "SCREEN", "AUDIO" -> {
                pendingAction = command
                actionNumber += 1
                DebugLogger.log(context, "BT_REMOTE_COMMAND", command)
            }
        }
        return true
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private fun keyId(event: KeyEvent): String {
        val descriptor = InputDevice.getDevice(event.deviceId)?.descriptor ?: "unknown"
        return if (event.keyCode in mediaButtons) normalizedMediaKey(event.keyCode)
            else "${descriptor}|${event.keyCode}|${event.scanCode}"
    }
    /** MediaSession callbacks may not supply an InputDevice. Do not require an external HID descriptor.
     *  Only explicitly mapped commands execute. The Activity must be visible and on a remote surface.
     */
    fun onMediaButton(context: Context, event: KeyEvent): Boolean {
        if (event.keyCode !in mediaButtons) return false
        val key = normalizedMediaKey(event.keyCode)
        if (event.action == KeyEvent.ACTION_UP) return enabled(context) &&
            activityInForeground && activePanelIsRemoteSurface && !settingsVisible &&
            !BluetoothRemoteDiagnostic.listening && commands.any { mapping(context, it) == key }
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount != 0) return false
        latestKey = "Headset: ${KeyEvent.keyCodeToString(event.keyCode)}"
        if (learning) {
            learning = false
            val edit = prefs(context).edit()
            commands.forEach { if (mapping(context, it) == key) edit.remove(it) }
            edit.putString(learnCommand, key).apply()
            return true
        }
        if (!enabled(context) || !activityInForeground || !activePanelIsRemoteSurface ||
            settingsVisible || BluetoothRemoteDiagnostic.listening) return false
        return dispatchCommand(context, key, android.os.SystemClock.uptimeMillis())
    }
    /** MediaSession path (not ordinary Activity key dispatch).
     *  The user explicitly enabled the remote. PAUSE and TRIGGER have no UI
     *  dependency and can execute when another Activity has focus. SCREEN and
     *  AUDIO continue to require the visible analyzer/UI save flow.
     *  Android background routing is best effort until a foreground service exists.
     */
    fun onMediaSessionButton(context: Context, event: KeyEvent): Boolean {
        if (event.keyCode !in mediaButtons) return false
        val key = normalizedMediaKey(event.keyCode)
        if (event.action == KeyEvent.ACTION_UP) return enabled(context) &&
            !BluetoothRemoteDiagnostic.listening && commands.any { mapping(context, it) == key }
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount != 0) return false
        latestKey = "Headset(MediaSession): ${KeyEvent.keyCodeToString(event.keyCode)}"
        if (learning && activityInForeground) {
            learning = false
            val edit = prefs(context).edit()
            commands.forEach { if (mapping(context, it) == key) edit.remove(it) }
            edit.putString(learnCommand, key).apply()
            return true
        }
        if (!enabled(context) || BluetoothRemoteDiagnostic.listening || settingsVisible) return false
        val command = commands.firstOrNull { mapping(context, it) == key } ?: return false
        if (!activityInForeground && command != "PAUSE" && command != "TRIGGER") {
            DebugLogger.log(context, "BT_REMOTE_IGNORED", "$command: Oberfläche nicht aktiv")
            return true
        }
        if (activityInForeground && !activePanelIsRemoteSurface) return false
        return dispatchCommand(context, key, android.os.SystemClock.uptimeMillis())
    }
    fun hasMediaMapping(context: Context, code: Int) = commands.any {
        mapping(context, it) == normalizedMediaKey(code)
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
        if (event.keyCode in mediaButtons) return onMediaButton(context, event)
        if (event.action == KeyEvent.ACTION_UP) {
            val key = keyId(event)
            return enabled(context) && activityInForeground && activePanelIsRemoteSurface && !settingsVisible && commands.any { mapping(context, it) == key } && !BluetoothRemoteDiagnostic.listening
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
        if (!enabled(context) || !activityInForeground || !activePanelIsRemoteSurface || settingsVisible || BluetoothRemoteDiagnostic.listening) return false
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
    @Composable fun SettingsDialog(context: Context, developerModeEnabled: Boolean, onDismiss: () -> Unit) {
        var active by remember { mutableStateOf(enabled(context)) }
        var revision by remember { mutableIntStateOf(0) }
        Dialog(onDismissRequest = { stopLearning(); onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
          Surface(modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.88f), shape = MaterialTheme.shapes.large, tonalElevation = 6.dp) {
           Column(Modifier.padding(18.dp)) {
           Text("Bluetooth Remote · Tastenbelegung", style=MaterialTheme.typography.titleLarge)
           Spacer(Modifier.height(8.dp))
            Box(Modifier.weight(1f)) {
                ObserveSettingsDialog(context)
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Text("S18: OBEN, UNTEN, LINKS, RECHTS, MITTE. Headset: Play/Pause, Vorwärts, Rückwärts (auch separate Play-/Pause-Tasten). Zum Zuweisen 'Anlernen' wählen und die Taste drücken. Lautstärke +/− wird nur erkannt, wenn Android sie als externe HID-Taste direkt an die App liefert; Android kann sie systemweit abfangen.", style=MaterialTheme.typography.bodySmall)
                    if (developerModeEnabled) {
                        Text(recognizedS18, style=MaterialTheme.typography.bodySmall)
                    } else {
                        Text(recognizedS18.substringBefore(" ("), style=MaterialTheme.typography.bodySmall)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Fernbedienung aktiviert"); ParameterNumber.Marker(9001, developerModeEnabled) }
                        Switch(checked=active, onCheckedChange={ active=it; setEnabled(context,it); (context as? MainActivity)?.refreshRemoteMediaSession() })
                    }
                    Text("Nur von Android an die App zugestellte externe Tastensignale. Bei gesperrtem Display nicht für jedes Gerät garantiert.", style=MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    commands.forEach { command ->
                        Row { Text(label(command), style=MaterialTheme.typography.titleSmall); ParameterNumber.Marker(when(command) { "PAUSE" -> 9002; "SCREEN" -> 9003; "AUDIO" -> 9004; else -> 9005 }, developerModeEnabled) }
                        val assigned = mapping(context,command)
                        // Re-read mappings when learning completes (latestKey changes).
                        @Suppress("UNUSED_VARIABLE") val refreshToken = revision to latestKey
                        val readable = when {
                            assigned.isBlank() -> "Nicht belegt"
                            assigned.contains("|S18:") -> "S18 · " + assigned.substringAfter("|S18:")
                            assigned.startsWith("MEDIA|") -> "Headset · " +
                                KeyEvent.keyCodeToString(assigned.substringAfter("MEDIA|").toIntOrNull() ?: 0)
                            else -> "Externe Taste angelernt"
                        }
                        Text(if (developerModeEnabled && assigned.isNotBlank()) "$readable · $assigned" else readable,
                            style=MaterialTheme.typography.bodySmall)
                        Row {
                            TextButton(onClick={ learn(command); revision++ }) { Text("Anlernen") }
                            TextButton(onClick={ clear(context,command); revision++ }) { Text("Löschen") }
                        }
                    }
                    Text(if (developerModeEnabled) latestKey else if (isLearning())
                        "Taste jetzt drücken …" else "Letzte Eingabe erkannt",
                        style=MaterialTheme.typography.bodySmall)
                    if (isLearning()) Text("Anlernen aktiv – Taste drücken, danach Dialog erneut öffnen, um Belegung zu prüfen.")
                    Text(lastTrigger, style=MaterialTheme.typography.bodySmall)
                }
            }
            TextButton(onClick={ stopLearning(); onDismiss() }) { Text("Schließen") }
           }
          }
        }
    }
}
