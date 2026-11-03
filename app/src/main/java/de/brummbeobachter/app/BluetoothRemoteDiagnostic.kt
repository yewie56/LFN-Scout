package de.brummbeobachter.app

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.OutputStreamWriter
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Window
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
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
    private var appContext: Context? = null
    private var activityRef: java.lang.ref.WeakReference<MainActivity>? = null
    var listening by mutableStateOf(false)
        private set
    private val events = mutableStateListOf<String>()
    private val devices = mutableStateListOf<String>()
    private val downTimes = mutableMapOf<Pair<Int, Int>, Long>()
    private val lastUps = mutableMapOf<Pair<Int, Int>, Long>()
    private const val LIMIT = 120
    private val lastMotionTimes = mutableMapOf<Int, Long>()
    private val previousCoordinates = mutableMapOf<Int, Pair<Float, Float>>()
    private val motionCounters = mutableMapOf<Int, Int>()

    private fun exportText(): String = buildString {
        appendLine("LFN-Scout v0.5.158 – Bluetooth Remote Diagnose")
        appendLine("Export: " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.GERMANY).format(Date()))
        appendLine("Status beim Export: " + if (listening) "Test aktiv" else "Test gestoppt")
        appContext?.let { ctx ->
            val selected = BluetoothRemoteControl.mediaProfileSelection(ctx)
            appendLine("Media-Profilwahl: " + if (selected == -1) "Automatisch (bei Mehrdeutigkeit keine Zuordnung)"
                else "Manuell: ${BluetoothRemoteControl.profileLabel(selected)}")
        }
        appendLine("Geräte (${devices.size}):")
        if (devices.isEmpty()) appendLine("Keine unterstützten Eingabegeräte erkannt")
        devices.forEach { appendLine(it) }
        appendLine()
        appendLine("Ereignisse (${events.size}, älteste zuerst; maximal 120 im Speicher):")
        events.asReversed().forEach { appendLine(it) }
        appendLine()
        appendLine("Hinweis: Es werden nur tatsächlich an LFN-Scout zugestellte Android-Ereignisse protokolliert.")
    }

    fun onSessionStatus(message: String) {
        if (listening) log("SESSION | $message")
    }

    fun onMediaSessionKey(event: KeyEvent) {
        if (!listening) return
        log("MEDIASESSION | " + (if (event.action == KeyEvent.ACTION_DOWN) "DOWN" else "UP") +
            " | key=${event.keyCode} (${KeyEvent.keyCodeToString(event.keyCode)})" +
            " | scan=${event.scanCode} meta=${event.metaState} repeat=${event.repeatCount}" +
            " | Profil: ${appContext?.let { BluetoothRemoteControl.profileForEvent(it, event) } ?: "unbekannt"}")
    }

    fun start(context: Context) {
        appContext = context.applicationContext
        activityRef = (context as? MainActivity)?.let { java.lang.ref.WeakReference(it) }
        events.clear()
        downTimes.clear()
        lastUps.clear()
        lastMotionTimes.clear()
        previousCoordinates.clear()
        motionCounters.clear()
        refreshDevices(context)
        listening = true
        log("Test gestartet. Ereignisse werden ausschließlich bei Android-Zustellung erfasst.")
        activityRef?.get()?.refreshRemoteMediaSession()
    }

    fun stop() {
        listening = false
        downTimes.clear()
        previousCoordinates.clear()
        activityRef?.get()?.refreshRemoteMediaSession()
    }

    fun refreshDevices(context: Context) {
        devices.clear()
        InputDevice.getDeviceIds().map { id -> InputDevice.getDevice(id) }.filterNotNull().forEach { device ->
            val source = device.sources
            if (source and InputDevice.SOURCE_KEYBOARD == InputDevice.SOURCE_KEYBOARD ||
                source and InputDevice.SOURCE_DPAD == InputDevice.SOURCE_DPAD ||
                source and InputDevice.SOURCE_MOUSE == InputDevice.SOURCE_MOUSE ||
                source and InputDevice.SOURCE_TOUCHPAD == InputDevice.SOURCE_TOUCHPAD) {
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
        log("$action | ${dev?.name ?: "unbekannt"} | device=${event.deviceId} | key=${event.keyCode} (${KeyEvent.keyCodeToString(event.keyCode)}) | scan=${event.scanCode} | meta=${event.metaState} | repeat=${event.repeatCount}$suffix" +
            " | Profil: ${appContext?.let { BluetoothRemoteControl.profileForEvent(it, event) } ?: "unbekannt"}")
        // Never steal system, volume, media or navigation keys; observing only.
        return false
    }

    /** Observe externally delivered pointer events without consuming them.
     * Mouse movement can be very frequent; movement-only records are throttled,
     * whereas action transitions and button changes are always retained.
     */
    fun onMotion(event: MotionEvent, entryPoint: String) {
        // Route the same S18 stream to the independent learner/command dispatcher.
        // The diagnostic logger remains read-only and never consumes events.
        if (entryPoint.startsWith("dialog")) appContext?.let { BluetoothRemoteControl.onMotion(it, event) }
        if (!listening) return
        val device = InputDevice.getDevice(event.deviceId) ?: return
        if (!device.isExternal) return
        if (!event.isFromSource(InputDevice.SOURCE_MOUSE) &&
            !event.isFromSource(InputDevice.SOURCE_TOUCHPAD)) return
        val id = event.deviceId
        val action = event.actionMasked
        val movement = action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_HOVER_MOVE
        val previous = previousCoordinates[id]
        val dx = if (previous == null) 0f else event.x - previous.first
        val dy = if (previous == null) 0f else event.y - previous.second
        previousCoordinates[id] = event.x to event.y
        motionCounters[id] = (motionCounters[id] ?: 0) + 1
        val previousTime = lastMotionTimes[id] ?: 0L
        if (movement && event.eventTime - previousTime < 100L) return
        lastMotionTimes[id] = event.eventTime
        val actionName = when (action) {
            MotionEvent.ACTION_MOVE -> "MOVE"
            MotionEvent.ACTION_HOVER_MOVE -> "HOVER_MOVE"
            MotionEvent.ACTION_DOWN -> "DOWN"
            MotionEvent.ACTION_UP -> "UP"
            MotionEvent.ACTION_BUTTON_PRESS -> "BUTTON_PRESS"
            MotionEvent.ACTION_BUTTON_RELEASE -> "BUTTON_RELEASE"
            MotionEvent.ACTION_SCROLL -> "SCROLL"
            else -> "ACTION_$action"
        }
        val xRelative = event.getAxisValue(MotionEvent.AXIS_RELATIVE_X)
        val yRelative = event.getAxisValue(MotionEvent.AXIS_RELATIVE_Y)
        log("MOTION $actionName | $entryPoint | ${device.name} | device=$id | " +
            "source=${event.source} | x=${event.x} y=${event.y} | dx=$dx dy=$dy | " +
            "relX=$xRelative relY=$yRelative | buttons=${event.buttonState} " +
            "actionButton=${event.actionButton} | count=${motionCounters[id]} | Profil: " +
            if (device.descriptor == "1f9732f291ce3e3ddc26c59de5cc9968625367d8") "S18 (HID-Descriptor)" else "Unbekannt (Maus/HID)")
    }

    private fun log(message: String) {
        val stamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.GERMANY).format(Date())
        events.add(0, "$stamp  $message")
        while (events.size > LIMIT) events.removeAt(events.lastIndex)
    }

    @Composable
    private fun ObserveDialogWindow() {
        val composeView = LocalView.current
        // AlertDialog uses its own Android window. Motion/key events targeted at
        // that window do not pass through MainActivity.dispatch*(). Observe this
        // dialog window without consuming any events.
        DisposableEffect(composeView) {
            val dialogWindow = (composeView.parent as? DialogWindowProvider)?.window
            val original = dialogWindow?.callback
            if (dialogWindow == null || original == null) {
                log("WARNUNG: Dialog-Fenster nicht gefunden – Eingangsdiagnose ggf. unvollständig")
            } else {
                val observer = object : Window.Callback by original {
                    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
                        onMotion(event, "dialog-generic")
                        return original.dispatchGenericMotionEvent(event)
                    }
                    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
                        onMotion(event, "dialog-touch")
                        return original.dispatchTouchEvent(event)
                    }
                    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                        onKey(event)
                        return original.dispatchKeyEvent(event)
                    }
                }
                dialogWindow.callback = observer
                log("Dialog-Fenster-Eventempfang aktiviert")
            }
            onDispose {
                if (dialogWindow != null && original != null && dialogWindow.callback !== original) {
                    dialogWindow.callback = original
                }
            }
        }
    }

    @Composable
    fun Dialog(onDismiss: () -> Unit) {
        val context = LocalContext.current
        var exportError by remember { mutableStateOf<String?>(null) }
        var exportSuccess by remember { mutableStateOf(false) }
        var pendingExport by remember { mutableStateOf("") }
        var mediaProfile by remember { mutableIntStateOf(BluetoothRemoteControl.mediaProfileSelection(context)) }
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
                ObserveDialogWindow()
                Column(Modifier.fillMaxWidth().heightIn(max = 510.dp).verticalScroll(rememberScrollState())) {
                    Text("Diagnose für S18-HID/Maus und klassische Headset-Medientasten. Quelle MEDIASESSION oder direkter Tasteneingang wird protokolliert. Während des Tests werden keine Fernsteuerungsaktionen ausgeführt.")
                    Spacer(Modifier.height(8.dp))
                    Text("Status: " + if (listening) "Empfangstest aktiv" else "Gestoppt")
                    Text("Display gesperrt/aus: nicht bestätigt. Android stellt normale HID-Tasten dabei möglicherweise nicht an diese Activity zu.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Profilzuordnung für nicht unterscheidbare MediaSession-Tasten", style = MaterialTheme.typography.titleSmall)
                    Text("Automatisch nutzt nur eindeutige gespeicherte Tastenbelegungen; ohne eindeutige Gerätekennung wird kein Bluetooth-Gerät behauptet.", style = MaterialTheme.typography.bodySmall)
                    (listOf(-1) + BluetoothRemoteControl.profileNames().indices.toList()).forEach { index ->
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(selected = mediaProfile == index, onClick = {
                                mediaProfile = index
                                BluetoothRemoteControl.setMediaProfileSelection(context, index)
                            })
                            Text(if (index == -1) "Automatisch" else "Manuell: ${BluetoothRemoteControl.profileLabel(index)}")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Erkannte Eingabegeräte", style = MaterialTheme.typography.titleSmall)
                    if (devices.isEmpty()) Text("Keine unterstützten Eingabegeräte erkannt")
                    devices.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
                    Spacer(Modifier.height(8.dp))
                    exportError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (exportSuccess) Text("Protokoll gespeichert.", color = MaterialTheme.colorScheme.primary)
                    Text("Empfangene Tasten- und Mausereignisse (max. 120)", style = MaterialTheme.typography.titleSmall)
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
