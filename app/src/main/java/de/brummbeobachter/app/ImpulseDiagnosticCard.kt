package de.brummbeobachter.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

/** Developer-only diagnostic; uses shared raw-PCM hub, never acquires a second microphone. */
@Composable
internal fun ImpulseDiagnosticCard() {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("lfn_scout_impulse_diagnostic", Context.MODE_PRIVATE) }
    var enabled by remember { mutableStateOf(false) }
    var gainDb by remember { mutableStateOf(prefs.getInt("gain_db", 0).coerceIn(0, 50)) }
    var minPeakPcm by remember { mutableStateOf(prefs.getInt("min_peak_pcm", 1400).coerceIn(50, 3000)) }
    var noiseMultiplier by remember { mutableStateOf((prefs.getInt("noise_x10", 60) / 10.0).coerceIn(1.5, 12.0)) }
    val telemetry = remember { ImpulseDiagnosticTelemetry() }
    var snapshot by remember { mutableStateOf(telemetry.snapshot()) }
    var copied by remember { mutableStateOf(false) }

    fun updateSettings(gain: Int = gainDb, minimum: Int = minPeakPcm, noise: Double = noiseMultiplier) {
        gainDb = gain.coerceIn(0, 50)
        minPeakPcm = minimum.coerceIn(50, 3000)
        noiseMultiplier = noise.coerceIn(1.5, 12.0)
        telemetry.setSensitivity(ImpulseSensitivity(gainDb, minPeakPcm, noiseMultiplier))
        copied = false
    }
    fun persistSettings() {
        prefs.edit().putInt("gain_db", gainDb).putInt("min_peak_pcm", minPeakPcm)
            .putInt("noise_x10", (noiseMultiplier * 10).roundToInt()).apply()
    }
    DisposableEffect(enabled) {
        if (enabled) {
            telemetry.setSensitivity(ImpulseSensitivity(gainDb, minPeakPcm, noiseMultiplier))
            telemetry.start()
        }
        onDispose { telemetry.stop() }
    }
    LaunchedEffect(enabled) {
        while (true) {
            snapshot = telemetry.snapshot()
            delay(250)
        }
    }
    val selectedPreset = ImpulseSensitivityPreset.entries.firstOrNull {
        it.minPeakPcm == minPeakPcm && it.noiseMultiplier == noiseMultiplier
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Highspeed-Impulsdiagnose (Test)", style = MaterialTheme.typography.titleMedium)
            Text("Roh-PCM vor 8:1-Decimation und Aufnahme-Gain; keine zusätzliche Mikrofonaufnahme.",
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            if (enabled) {
                OutlinedButton(onClick = { enabled = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Diagnose stoppen")
                }
            } else {
                Button(onClick = { telemetry.reset(); enabled = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Diagnose starten")
                }
            }
            Text("Status: " + if (enabled) {
                if (snapshot.frames == 0L) "Warte auf Aufnahme / Rolling-Buffer" else "Rohdaten empfangen"
            } else "AUS")
            Text("Tatsächliche PCM-Rate: ${snapshot.rateHz?.toString() ?: "–"} Hz (48 kHz noch nicht aktiviert)")
            Text("Quelle: ${snapshot.source ?: "–"}")
            Spacer(Modifier.height(8.dp))
            Text("Empfindlichkeitsprofil: ${selectedPreset?.label ?: "Manuell"}")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                ImpulseSensitivityPreset.entries.forEach { preset ->
                    OutlinedButton(
                        onClick = { updateSettings(minimum = preset.minPeakPcm, noise = preset.noiseMultiplier); persistSettings() },
                        modifier = Modifier.weight(1f)
                    ) { Text(preset.label, style = MaterialTheme.typography.labelSmall) }
                }
            }
            Text("Analyzer-Gain: +${gainDb} dB (nur Diagnose, WAV unverändert)")
            Slider(value = gainDb.toFloat(), onValueChange = { updateSettings(gain = it.roundToInt()) },
                onValueChangeFinished = { persistSettings() }, valueRange = 0f..50f, steps = 49)
            Text("Mindestschwelle VOR Gain: ${minPeakPcm} PCM (50–3000; kleiner = empfindlicher)")
            Slider(value = minPeakPcm.toFloat(), onValueChange = {
                updateSettings(minimum = (it / 50.0f).roundToInt() * 50)
            }, onValueChangeFinished = { persistSettings() }, valueRange = 50f..3000f, steps = 58)
            Text("Rauschfaktor: ×${"%.1f".format(Locale.US, noiseMultiplier)} (1,5–12; kleiner = empfindlicher)")
            Slider(value = noiseMultiplier.toFloat(), onValueChange = {
                updateSettings(noise = ((it * 2.0f).roundToInt() / 2.0).coerceIn(1.5, 12.0))
            }, onValueChangeFinished = { persistSettings() }, valueRange = 1.5f..12f, steps = 20)
            Spacer(Modifier.height(8.dp))
            Text("Letzter Block-Peak: ${snapshot.blockPeak} PCM · Max-Peak seit Start: ${snapshot.samplePeak} / 32768")
            Text("Analyse-Peak-Äquivalent: ${"%.1f".format(Locale.US, snapshot.analysisPeak)} PCM (ohne 16-Bit-Clipping)")
            Text("Feste Rohschwelle: ${snapshot.fixedThresholdRaw} PCM · Rauschbasis: ${"%.1f".format(Locale.US, snapshot.noiseRaw)} PCM")
            Text("Adaptive Rohschwelle: ${"%.1f".format(Locale.US, snapshot.adaptiveThresholdRaw)} PCM")
            Text("Effektive Rohschwelle: ${"%.1f".format(Locale.US, snapshot.thresholdRaw)} PCM (Maximum aus beiden)")
            Text("Detektierte Ereignisse: ${snapshot.events}")
            Text("Letzter Ereignis-Peak: ${snapshot.eventPeak ?: "–"} PCM")
            Text("Letzte Impulsdauer: ${snapshot.durationMs?.let { "%.2f ms".format(Locale.US, it) } ?: "–"}")
            Text("Rohblöcke: ${snapshot.frames} · Queue-Verluste: ${snapshot.dropped}")
            Text("Kontinuitäts-Resets: ${snapshot.resets} · Callback-Fehler: ${snapshot.failures}")
            OutlinedButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("LFN-Scout Impulstest", snapshot.report(gainDb, minPeakPcm, noiseMultiplier)))
                copied = true
            }, modifier = Modifier.fillMaxWidth()) { Text(if (copied) "Testwerte kopiert" else "Testwerte kopieren") }
            Text("Gain und Schwellenparameter ändern nur die Impulsdiagnose. Digitaler Gain verbessert das Signal/Rausch-Verhältnis nicht; geringe Rauschfaktoren können Fehltrigger auslösen.", style = MaterialTheme.typography.bodySmall)
            Text("PCM-Werte sind keine kalibrierten Schalldruckpegel. Bei Stille oder deaktiviertem Rolling-Buffer können Daten fehlen.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Thread-safe bridge from asynchronous PCM workers into the periodic Compose snapshot. */
internal class ImpulseDiagnosticTelemetry {
    internal data class Snapshot(
        val frames: Long, val rateHz: Int?, val source: String?, val samplePeak: Int, val blockPeak: Int,
        val events: Long, val eventPeak: Int?, val durationMs: Double?,
        val dropped: Long, val resets: Long, val failures: Long,
        val analysisPeak: Double, val thresholdRaw: Double, val fixedThresholdRaw: Int,
        val noiseRaw: Double, val adaptiveThresholdRaw: Double
    ) {
        /** Local clipboard only: no microphone samples, location, identifiers or network upload. */
        fun report(gainDb: Int, minPeakPcm: Int, noiseMultiplier: Double): String = buildString {
            appendLine("LFN-Scout – Impulstest v0.4.89")
            appendLine("PCM-Rate: ${rateHz ?: "keine Daten"} Hz")
            appendLine("Gain: +$gainDb dB; Mindestschwelle: $minPeakPcm PCM; Rauschfaktor: $noiseMultiplier")
            appendLine("Block-Peak: $blockPeak; Max-Peak: $samplePeak PCM; Analyse-Peak: ${"%.1f".format(Locale.US, analysisPeak)}")
            appendLine("Rauschbasis: ${"%.1f".format(Locale.US, noiseRaw)}; fest/adaptiv/effektiv: $fixedThresholdRaw/${"%.1f".format(Locale.US, adaptiveThresholdRaw)}/${"%.1f".format(Locale.US, thresholdRaw)} PCM")
            appendLine("Impulse: $events; letzter Peak: ${eventPeak ?: "–"}; Dauer: ${durationMs ?: "–"} ms")
            appendLine("Rohblöcke: $frames; Queue-Verluste: $dropped; Resets: $resets; Callback-Fehler: $failures")
        }
    }
    private val frameCount = AtomicLong()
    private val peak = AtomicLong()
    private val blockPeak = AtomicLong()
    private val lastFrame = AtomicReference<AnalyzerPcmHub.Frame?>()
    private val lastEvent = AtomicReference<HighspeedImpulseDetector.Event?>()
    private var monitor: AnalyzerPcmHub.Subscription? = null
    private var detector: HighspeedImpulseSession? = null
    @Volatile private var requestedSensitivity = ImpulseSensitivity()
    @Synchronized fun setSensitivity(value: ImpulseSensitivity) {
        requestedSensitivity = value
        detector?.sensitivity = value
    }
    @Synchronized fun reset() {
        check(monitor == null && detector == null)
        frameCount.set(0); peak.set(0); blockPeak.set(0); lastFrame.set(null); lastEvent.set(null)
    }
    @Synchronized fun start() {
        if (monitor != null || detector != null) return
        monitor = AnalyzerSessionGateway.rawPcm.subscribe(queueCapacity = 8) { frame ->
            lastFrame.set(frame)
            frameCount.incrementAndGet()
            var maximum = 0
            for (x in frame.samples) maximum = maxOf(maximum, abs(x.toInt()))
            blockPeak.set(maximum.toLong())
            peak.set(maxOf(peak.get(), maximum.toLong()))
        }
        detector = AnalyzerSessionGateway.startImpulseDetector { event -> lastEvent.set(event) }
        detector?.sensitivity = requestedSensitivity
    }
    @Synchronized fun stop() {
        detector?.close(); detector = null
        monitor?.close(); monitor = null
    }
    @Synchronized fun snapshot(): Snapshot {
        val frame = lastFrame.get()
        val event = lastEvent.get()
        val session = detector
        return Snapshot(frameCount.get(), frame?.sampleRateHz,
            frame?.let { "${it.sourceLabel} / ${it.deviceLabel}" }, peak.get().toInt(), blockPeak.get().toInt(),
            session?.detectedEvents ?: 0L, event?.peakPcm, event?.durationMs,
            (monitor?.droppedFrames ?: 0L) + (session?.droppedFrames ?: 0L),
            session?.discontinuityCount ?: 0L,
            (monitor?.listenerFailures ?: 0L) + (session?.callbackFailures ?: 0L),
            peak.get().toDouble() * 10.0.pow(requestedSensitivity.gainDb / 20.0),
            session?.effectiveThresholdRawPcm ?: 0.0,
            session?.fixedThresholdRawPcm ?: requestedSensitivity.fixedThresholdRawPcm(),
            session?.measuredNoisePcm ?: 0.0,
            session?.adaptiveThresholdRawPcm ?: 0.0)
    }
}
