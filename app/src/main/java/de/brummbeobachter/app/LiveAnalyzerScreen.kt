package de.brummbeobachter.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint as NativePaint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

/** Preview-free, real PCM-driven Android UI. Impulse diagnostics remain separate. */
/** Dark compact layout based on the user's side-by-side Mithören EIN/AUS concept.
 * Only implemented controls are enabled; no fake monitoring or impulse waterfall.
 */
@Composable
internal fun LiveAnalyzerScreen(
    onBack: () -> Unit,
    dailyCount: Int,
    dailyLimit: Int,
    onLocalAudioSaved: suspend () -> Boolean,
    onAnalyzerAudioArchived: suspend (AudioArchiveEntry) -> Boolean,
    observerId: String,
    recentObservations: List<RecentObservation>,
    audioNoCommentTimeoutSeconds: Int,
    audioCommentTimeoutSeconds: Int,
    rollingEnabled: Boolean,
    onEnableRolling: () -> Unit,
    onAudioModeChanged: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var state by remember { mutableStateOf(LiveAnalyzerController.snapshot()) }
    // SCREEN_ON is a visual preference, distinct from the service's existing CPU wakelock.
    DisposableEffect(state.direct48k, state.directStayAwake, state.running) {
        val activity = context as? android.app.Activity
        val keep = state.direct48k && state.directStayAwake && state.running
        if (keep) activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { if (keep) activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
    var settings by remember { mutableStateOf(false) }
    var settingsSection by remember { mutableStateOf<String?>(null) }
    fun backFromSettings() {
        if (settingsSection != null) settingsSection = null else settings = false
    }
    BackHandler(enabled=settings) { backFromSettings() }
    val screenScroll = rememberScrollState()
    val scrollScope = rememberCoroutineScope()
    var cursorHz by remember { mutableStateOf<Double?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var localSaveConfig by remember { mutableStateOf(LocalAudioSaveSettings.load(context)) }
    var localSettingsShown by remember { mutableStateOf(false) }
    var audioSaveBusy by remember { mutableStateOf(false) }
    var audioSaveStatus by remember { mutableStateOf("") }
    var pendingAudioClip by remember { mutableStateOf<LocalAnalyzerAudioBuffer.Clip?>(null) }
    var pendingAudioConfig by remember { mutableStateOf<StatisticsWindowEngine.Configuration?>(null) }
    var pendingAudioDisplayGainDb by remember { mutableStateOf(0) }
    var saveMessage by remember { mutableStateOf("") }
    var pendingImageSnapshot by remember { mutableStateOf<LiveAnalyzerController.Snapshot?>(null) }
    var imageComment by remember { mutableStateOf("") }
    // Transient, two-line indication of WATERFALL DISPLAY changes only. Not an image/raster layer.
    var waterfallNotice by remember { mutableStateOf<AnalyzerWaterfallNotice.Content?>(null) }
    // Never use rows.size, spectra count, waterfallRevision or autorange progress in this key:
    // those are live measurements and would retrigger the 5-second popup every UI refresh.
    val waterfallNoticeKey = listOf(
        state.waterfallMode, state.historySeconds, state.sampleRateHz, state.fftHopSamples,
        state.waterfallIntervalMs, state.minFrequencyHz, state.rangeHz,
        state.minDisplayDb, state.maxDisplayDb, state.direct48k,
        state.showOriginalWaterfall, state.showFilterWaterfall, expanded,
        if (!state.direct48k && state.showFilterWaterfall) state.filterBandCount else 0,
        if (!state.direct48k && state.showFilterWaterfall) state.filterMinHz else 0.0,
        if (!state.direct48k && state.showFilterWaterfall) state.filterMaxHz else 0.0,
        if (!state.direct48k && state.showFilterWaterfall) state.filterDisplayMinDb else 0,
        if (!state.direct48k && state.showFilterWaterfall) state.filterDisplayMaxDb else 0,
        if (!state.direct48k) state.filterAutoRangeActive else false
    ).joinToString("|")
    // Track only NEW manual completions; automatic images never open the confirmation popup.
    var observedManualSaveEvent by remember { mutableStateOf(state.manualSaveEvent) }
    var showModeDialog by remember { mutableStateOf(AnalyzerModeCoordinator.mode == null) }
    var modeBusy by remember { mutableStateOf(false) }
    var modeError by remember { mutableStateOf("") }
    val modeScope = rememberCoroutineScope()
    fun leavePanel() {
        if (state.running) { onBack(); return } // Panel change while running NEVER changes the source.
        modeScope.launch {
            val left = AnalyzerModeCoordinator.finishStopped(context)
            if (left) { onAudioModeChanged(); onBack() }
            else modeError = "Analyzer muss vor dem Beenden gestoppt sein"
        }
    }
    BackHandler(enabled = !settings) { leavePanel() }
    val bg = Color(0xFF081113)
    val edge = Color(0xFF34444A)
    val white = Color(0xFFF1F6F8)
    val muted = Color(0xFF9DAAB1)
    val cyan = Color(0xFF30D7EF)
    LaunchedEffect(Unit) {
        while (true) {
            state = LiveAnalyzerController.snapshot()
            // Display refresh is independent from FFT computation and waterfall decimation.
            delay(if (state.fftHopSamples <= 50) 50L else 100L)
        }
    }
    LaunchedEffect(settings) { screenScroll.scrollTo(0) }
    LaunchedEffect(cursorHz) { LiveAnalyzerController.setCursorFrequency(cursorHz) }
    LaunchedEffect(state.manualSaveEvent) {
        if (state.manualSaveEvent > observedManualSaveEvent) {
            observedManualSaveEvent = state.manualSaveEvent
            saveMessage = state.manualSaveMessage
        }
    }
    LaunchedEffect(saveMessage) {
        if (saveMessage.isNotEmpty()) {
            delay(2200)
            saveMessage = ""
        }
    }
    LaunchedEffect(audioSaveStatus) {
        if (audioSaveStatus.isNotEmpty() && audioSaveStatus != "Speichert …") {
            delay(3_000L)
            audioSaveStatus = ""
        }
    }
    LaunchedEffect(waterfallNoticeKey) {
        waterfallNotice = AnalyzerWaterfallNotice.content(
            mode=state.waterfallMode.name,
            historySeconds=state.historySeconds,
            sampleRateHz=state.sampleRateHz,
            fftHopSamples=state.fftHopSamples,
            waterfallIntervalMs=state.waterfallIntervalMs,
            direct=state.direct48k,
            original=state.showOriginalWaterfall,
            filter=state.showFilterWaterfall,
            filterBands=state.filterBandCount,
            filterMinHz=state.filterMinHz,
            filterMaxHz=state.filterMaxHz,
            filterAutoRangeActive=state.filterAutoRangeActive
        )
        delay(5_000L)
        waterfallNotice = null
    }
    // Fill the parent viewport even when the scroll content is short. The old content-sized
    // column exposed the host's light background as a large white strip below the analyzer.
    BoxWithConstraints(Modifier.fillMaxSize().background(bg)) {
        // Reserve only the compact header/status, waterfall status, PNG actions and padding.
        // Remaining viewport height is used by the TWO plots instead of obsolete status cards.
        // On small screens, minimum plot heights permit scrolling rather than clipping axes.
        val plotBudget = (maxHeight - 150.dp).coerceAtLeast(400.dp)
        val spectrumHeight = if (expanded) 150.dp else (plotBudget * 0.53f).coerceAtLeast(215.dp)
        val waterfallHeight = if (expanded) (plotBudget - 150.dp).coerceAtLeast(300.dp)
            else (plotBudget * 0.47f).coerceAtLeast(190.dp)
        // Computed in the BoxWithConstraints scope, before entering the nested BoxScope.
        // Same shared 54/12-dp plot margins plus 12 dp notification inset.
        val waterfallNoticeMaxWidth = (maxWidth - 78.dp).coerceAtLeast(100.dp)
        if (localSettingsShown) LocalAudioSaveSettingsDialog(context) { next ->
            if (next != null) localSaveConfig = next
            localSettingsShown = false
        }
        val analyzerPendingClip = pendingAudioClip
        val analyzerPendingConfig = pendingAudioConfig
        if (analyzerPendingClip != null && analyzerPendingConfig != null) {
            fun submitAnalyzerAudio(comment: String?) {
                if (audioSaveBusy) return
                audioSaveBusy = true
                audioSaveStatus = "Speichert …"
                modeScope.launch {
                    try {
                        val saved = LocalAudioStatisticsRepository.saveAnalyzer(
                            context, analyzerPendingClip, analyzerPendingConfig,
                            pendingAudioDisplayGainDb, comment, observerId, recentObservations
                        )
                        val quotaAccepted = onLocalAudioSaved()
                        if (!quotaAccepted) {
                            audioSaveStatus = "Lokal gespeichert · Tageslimit erreicht"
                        } else {
                            val archived = saved.archiveEntry
                            val uploadOk = archived != null && onAnalyzerAudioArchived(archived)
                            audioSaveStatus = if (uploadOk) "Lokal gespeichert · Cloud ok"
                                else "Lokal gespeichert · Upload offen"
                        }
                    } catch (e: Exception) {
                        if (analyzerPendingClip.fromPause) LocalAnalyzerAudioBuffer.restoreFrozenOnFailure()
                        audioSaveStatus = "Audio-Save fehlgeschlagen: ${e.message ?: e.javaClass.simpleName}"
                    } finally {
                        audioSaveBusy = false
                        pendingAudioClip = null
                        pendingAudioConfig = null
                    }
                }
            }
            AudioCommentDialog(
                recordingId = "analyzer-${analyzerPendingClip.endEpochMs}",
                noCommentTimeoutSeconds = audioNoCommentTimeoutSeconds,
                commentTimeoutSeconds = audioCommentTimeoutSeconds,
                busy = audioSaveBusy,
                actionWord = "gespeichert",
                busyText = "Speichern läuft ...",
                confirmLabel = "Jetzt speichern",
                withoutCommentLabel = "Ohne Kommentar speichern",
                onAutoComplete = { comment, _ -> submitAnalyzerAudio(comment) },
                onConfirm = { comment -> submitAnalyzerAudio(comment) },
                onWithoutComment = { submitAnalyzerAudio(null) }
            )
        }
        val capturedImage = pendingImageSnapshot
        if (capturedImage != null) {
            AlertDialog(
                onDismissRequest = { pendingImageSnapshot = null; imageComment = "" },
                title = { Text("Kommentar zum Analyzer-Bild") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Der Wasserfall ist bereits eingefroren. Der Kommentar wird unten in das gespeicherte Bild eingeblendet.")
                        OutlinedTextField(
                            value = imageComment,
                            onValueChange = { imageComment = it.take(400) },
                            label = { Text("Kommentar (optional)") },
                            minLines = 3, maxLines = 6, modifier = Modifier.fillMaxWidth()
                        )
                        Text("${imageComment.length}/400", fontSize = 11.sp, color = muted)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val snap = pendingImageSnapshot
                        pendingImageSnapshot = null
                        if (snap != null && !LiveAnalyzerController.saveCapturedImage(snap, imageComment))
                            saveMessage = "Bild nicht gespeichert: Bildexport beschäftigt"
                        imageComment = ""
                    }) { Text("Speichern") }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            val snap = pendingImageSnapshot
                            pendingImageSnapshot = null
                            if (snap != null && !LiveAnalyzerController.saveCapturedImage(snap, null))
                                saveMessage = "Bild nicht gespeichert: Bildexport beschäftigt"
                            imageComment = ""
                        }) { Text("Ohne Kommentar") }
                        TextButton(onClick = { pendingImageSnapshot = null; imageComment = "" }) { Text("Abbrechen") }
                    }
                }
            )
        }
        if (showModeDialog) AlertDialog(
            onDismissRequest = { if (!modeBusy) leavePanel() },
            title = { Text("Analyzer starten") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Möchtest du parallel Audio aufzeichnen?")
                Text("JA: bestehender 8-kHz-Eingang mit Rolling. NEIN: exklusiver Direkt-Analyzer (8–96 kHz) ohne Rolling.")
                Text("Digitaler Test: unabhängig vom Mikrofon, ohne Rolling/WAV; deutlich als TESTSIGNAL markiert.")
                Row(horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                    TextButton(enabled=!modeBusy, onClick={
                        modeBusy=true
                        modeScope.launch {
                            val ok=AnalyzerModeCoordinator.begin(context, AnalyzerModeCoordinator.Mode.TEST_8K)
                            modeBusy=false
                            if (ok) { showModeDialog=false; state=LiveAnalyzerController.snapshot(); onAudioModeChanged() }
                            else modeError=AnalyzerModeCoordinator.error
                        }
                    }) { Text("Test 8 kHz") }
                    TextButton(enabled=!modeBusy, onClick={
                        modeBusy=true
                        modeScope.launch {
                            val ok=AnalyzerModeCoordinator.begin(context, AnalyzerModeCoordinator.Mode.TEST_48K)
                            modeBusy=false
                            if (ok) { showModeDialog=false; state=LiveAnalyzerController.snapshot(); onAudioModeChanged() }
                            else modeError=AnalyzerModeCoordinator.error
                        }
                    }) { Text("Test Direkt") }
                }
                if (modeError.isNotBlank()) Text(modeError, color=Color(0xFFFF6655))
            } },
            confirmButton = { Button(enabled=!modeBusy, onClick={
                modeBusy=true
                modeScope.launch {
                    val ok = AnalyzerModeCoordinator.begin(context, AnalyzerModeCoordinator.Mode.ROLLING_8K)
                    modeBusy=false
                    if (ok) { showModeDialog=false; state=LiveAnalyzerController.snapshot(); onAudioModeChanged() }
                    else modeError=AnalyzerModeCoordinator.error
                }
            }) { Text("JA · 8 kHz + Rolling") } },
            dismissButton = { TextButton(enabled=!modeBusy, onClick={
                modeBusy=true
                modeScope.launch {
                    val ok=AnalyzerModeCoordinator.begin(context, AnalyzerModeCoordinator.Mode.DIRECT_48K)
                    modeBusy=false
                    if (ok) { showModeDialog=false; state=LiveAnalyzerController.snapshot(); onAudioModeChanged() }
                    else modeError=AnalyzerModeCoordinator.error
                }
            }) { Text("NEIN · Direkt") } }
        )
        Column(Modifier.fillMaxSize().verticalScroll(screenScroll)
            .padding(horizontal=8.dp, vertical=5.dp)) {
        if (settings) {
            Row(Modifier.fillMaxWidth().height(45.dp), verticalAlignment=Alignment.CenterVertically) {
                Text("‹", color=white, fontSize=27.sp,
                    modifier=Modifier.clickable { backFromSettings() }.padding(horizontal=8.dp))
                Column(Modifier.weight(1f)) {
                    Text("LFN-Scout",color=white,fontSize=17.sp,fontWeight=FontWeight.Bold)
                    Text("Einstellungen",color=muted,fontSize=11.sp)
                }
                Text(if (state.running && !state.paused) "● Messung aktiv" else if (state.paused) "Ⅱ Pause" else "○ Stopp",
                    color=if(state.running && !state.paused) Color(0xFF2CDAA2) else muted,fontSize=10.sp)
            }
            LiveAnalyzerSettingsPanel(context,state,onRefresh={ state=LiveAnalyzerController.snapshot() },
                onNavigate={ scrollScope.launch { screenScroll.scrollTo(0) } },
                selected=settingsSection, onSelect={ settingsSection=it },
                onOpenLocalSaveSettings={ localSettingsShown = true })
        } else {
            Row(Modifier.fillMaxWidth().height(42.dp), verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.SpaceBetween) {
                Text("‹", color=white, modifier=Modifier.clickable { leavePanel() }.padding(5.dp),fontSize=27.sp)
                Column(Modifier.weight(1f).padding(start=6.dp)) {
                    Text("LFN-Scout · ${state.minFrequencyHz}–${state.rangeHz} Hz", color=white,fontSize=17.sp,fontWeight=FontWeight.Bold)
                }
                OutlinedButton(onClick={
                    if (state.running) {
                        AnalyzerModeCoordinator.stopMeasurement(context)
                        state=LiveAnalyzerController.snapshot()
                    } else if (AnalyzerModeCoordinator.mode != null) {
                        modeScope.launch {
                            if (!AnalyzerModeCoordinator.restartMeasurement(context)) modeError=AnalyzerModeCoordinator.error
                            state=LiveAnalyzerController.snapshot()
                        }
                    } else showModeDialog=true
                }, modifier=Modifier.height(38.dp)) {
                    Text(if(!state.running) "▶" else "■", color=cyan, fontSize=15.sp)
                }
                if (state.running) OutlinedButton(onClick={
                    LiveAnalyzerController.setPaused(!state.paused)
                    state = LiveAnalyzerController.snapshot()
                }, modifier=Modifier.height(38.dp),
                    contentPadding=androidx.compose.foundation.layout.PaddingValues(horizontal=6.dp,vertical=0.dp)) {
                    Text(if (state.paused) "▶" else "Ⅱ", color=cyan, fontSize=15.sp)
                }
                Column(Modifier.padding(start=5.dp),horizontalAlignment=Alignment.End) {
                    Text(if(state.running && !state.paused) "● Messung aktiv" else if(state.paused) "Ⅱ Pause" else "○ Stopp",
                        color=if(state.running&&!state.paused)Color(0xFF2CDAA2) else muted,fontSize=10.sp)
                    Text("${state.spectra} FFT",color=muted,fontSize=10.sp)
                }
                Text("⋮",color=white,fontSize=24.sp,modifier=Modifier.clickable { settings=true }.padding(start=8.dp))
            }
        val age = state.signalAgeMs
        if(AnalyzerModeCoordinator.mode in listOf(AnalyzerModeCoordinator.Mode.TEST_8K,AnalyzerModeCoordinator.Mode.TEST_48K))
            Text("TESTSIGNAL · KEINE MESSUNG · ${if(state.direct48k) "${state.directInputRateHz} Hz" else "8 kHz"}",
                color=Color(0xFFFFD17C),fontSize=12.sp,fontWeight=FontWeight.Bold)
        Text(state.status,color=if(state.validSignal)muted else Color(0xFFFFD17C),fontSize=10.sp)
        if(modeError.isNotBlank()) Text(modeError,color=Color(0xFFFF6655),fontSize=11.sp)
        val testMode = AnalyzerModeCoordinator.mode == AnalyzerModeCoordinator.Mode.TEST_8K ||
            AnalyzerModeCoordinator.mode == AnalyzerModeCoordinator.Mode.TEST_48K
        val availableSeconds = LocalAnalyzerAudioBuffer.bufferedSeconds()
        val canLocalSave = state.running && !testMode && !audioSaveBusy && pendingAudioClip == null &&
            dailyCount < dailyLimit && LocalAnalyzerAudioBuffer.canSave(localSaveConfig.durationSeconds, state.paused)
        if(state.running && !state.direct48k && !RollingAudioEngine.isRunning && age < 0) {
            Text("Rolling-Buffer startet …",color=muted,fontSize=11.sp)
        }
        // Axes carry the units; L50/L90 are overlaid in the plot (no duplicate title or legend).
        // The plot has its own real dBFS and Hz axes. Touch positions subtract its left margin.
        // One shared horizontal viewport for BOTH canvases; narrow left ramp saves plot space.
        val plotLeftDp = 54.dp
        val plotRightDp = 12.dp
        val plotBottomDp = 23.dp
        val plotTopDp = 4.dp
        Box(Modifier.fillMaxWidth().height(spectrumHeight)
            .background(Color(0xFF0B1215)).pointerInput(state.rangeHz, state.minFrequencyHz, cursorHz, state.autoRangeActive) {
                detectTapGestures { point ->
                    val left = plotLeftDp.toPx()
                    val right = size.width - plotRightDp.toPx()
                    val top = plotTopDp.toPx()
                    val bottom = size.height - plotBottomDp.toPx()
                    if (cursorHz != null) {
                        val hz = cursorHz!!
                        val cx = left + ((hz-state.minFrequencyHz) / (state.rangeHz-state.minFrequencyHz) * (right-left)).toFloat()
                        val label = cursorReadout(state, hz)
                        val paint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply { textSize=10.sp.toPx() }
                        val width = paint.measureText(label) + 2.dp.toPx()
                        val nearStats = cx + width + 3.dp.toPx() > right - 108.dp.toPx()
                        val lx = (if (nearStats) cx-width-3.dp.toPx() else cx+3.dp.toPx())
                            .coerceIn(left, (right-width).coerceAtLeast(left))
                        val ly = top + 9.dp.toPx()
                        // Only the text itself is the OFF target; no visible readout frame.
                        if (point.x in (lx-2.dp.toPx())..(lx+width+2.dp.toPx()) &&
                            point.y in (ly-3.dp.toPx())..(ly+13.dp.toPx())) {
                            cursorHz = null
                        } else if (point.x < left && point.y in top..bottom) {
                            LiveAnalyzerController.startAutoRange()
                            state = LiveAnalyzerController.snapshot()
                        } else if (point.x in left..right && point.y in top..bottom) {
                            cursorHz = (state.minFrequencyHz+(point.x-left)/(right-left)*(state.rangeHz-state.minFrequencyHz))
                                .toDouble().coerceIn(state.minFrequencyHz.toDouble(),state.rangeHz.toDouble())
                        }
                    } else if (point.x < left && point.y in top..bottom) {
                        LiveAnalyzerController.startAutoRange()
                        state = LiveAnalyzerController.snapshot()
                    } else if (point.x in left..right && point.y in top..bottom) {
                        cursorHz = (state.minFrequencyHz+(point.x-left)/(right-left)*(state.rangeHz-state.minFrequencyHz))
                            .toDouble().coerceIn(state.minFrequencyHz.toDouble(),state.rangeHz.toDouble())
                    }
                }
            }.pointerInput(state.rangeHz,state.minFrequencyHz) {
                detectDragGestures(onDragStart = { pt ->
                    val left = plotLeftDp.toPx(); val right = size.width-plotRightDp.toPx()
                    if (pt.x in left..right) {
                        cursorHz = (state.minFrequencyHz+(pt.x-left)/(right-left)*(state.rangeHz-state.minFrequencyHz)).toDouble()
                            .coerceIn(state.minFrequencyHz.toDouble(),state.rangeHz.toDouble())
                    }
                }, onDrag = { change, _ ->
                    val left = plotLeftDp.toPx(); val right = size.width-plotRightDp.toPx()
                    if (change.position.x in left..right) {
                        cursorHz = (state.minFrequencyHz+(change.position.x-left)/(right-left)*(state.rangeHz-state.minFrequencyHz)).toDouble()
                            .coerceIn(state.minFrequencyHz.toDouble(),state.rangeHz.toDouble())
                        change.consume()
                    }
                })
            }) {
            val freq=state.frequencyHz
            Canvas(Modifier.matchParentSize()) {
                val left=plotLeftDp.toPx(); val right=size.width-plotRightDp.toPx()
                val top=plotTopDp.toPx(); val bottom=size.height-plotBottomDp.toPx()
                val plotW=right-left; val plotH=bottom-top
                if (plotW <= 0f || plotH <= 0f) return@Canvas
                val tickColor=Color(0xFFABB6BE)
                val textPaint=NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
                    color=AndroidColor.rgb(180,192,200); textSize=10.sp.toPx()
                }
                // dBFS: visible tick marks, numeric labels and precisely aligned horizontal grid.
                for(db in AnalyzerDisplayTicks.db(state.minDisplayDb, state.maxDisplayDb)) {
                    val y=bottom-(db-state.minDisplayDb).toFloat()/(state.maxDisplayDb-state.minDisplayDb)*plotH
                    drawLine(edge,Offset(left,y),Offset(right,y),strokeWidth=0.7f)
                    drawLine(tickColor,Offset(left-5.dp.toPx(),y),Offset(left,y),strokeWidth=1.dp.toPx())
                    drawIntoCanvas { canvas ->
                        textPaint.textAlign=NativePaint.Align.RIGHT
                        canvas.nativeCanvas.drawText("$db",left-8.dp.toPx(),y+3.dp.toPx(),textPaint)
                    }
                }
                for(hz in AnalyzerTiming.frequencyTicks(state.minFrequencyHz,state.rangeHz)) {
                    val x=left+(hz-state.minFrequencyHz).toFloat()/(state.rangeHz-state.minFrequencyHz)*plotW
                    drawLine(edge,Offset(x,top),Offset(x,bottom),strokeWidth=0.7f)
                    drawLine(tickColor,Offset(x,bottom),Offset(x,bottom+5.dp.toPx()),strokeWidth=1.dp.toPx())
                    drawIntoCanvas { canvas ->
                        textPaint.textAlign=NativePaint.Align.CENTER
                        canvas.nativeCanvas.drawText("$hz",x,bottom+17.dp.toPx(),textPaint)
                    }
                }
                // Place Hz between two numerical ticks, on their baseline.
                val unitX = left + ((AnalyzerTiming.unitFrequencyHz(state.minFrequencyHz,state.rangeHz) - state.minFrequencyHz) /
                    (state.rangeHz-state.minFrequencyHz) * plotW).toFloat()
                drawIntoCanvas { canvas ->
                    textPaint.textAlign=NativePaint.Align.CENTER
                    canvas.nativeCanvas.drawText("Hz",unitX,bottom+17.dp.toPx(),textPaint)
                }
                val bandLeft=left+((maxOf(state.statisticsMinHz,state.minFrequencyHz)-state.minFrequencyHz).toFloat() /
                    (state.rangeHz-state.minFrequencyHz))*plotW
                val bandRight=left+((minOf(state.statisticsMaxHz,state.rangeHz)-state.minFrequencyHz).toFloat() /
                    (state.rangeHz-state.minFrequencyHz))*plotW
                if (bandRight > bandLeft) drawRect(Color(0xFF085C78).copy(alpha=0.20f),Offset(bandLeft,top),
                    Size(bandRight-bandLeft,plotH))
                val lo=freq.indexOfFirst { it>=state.minFrequencyHz.toDouble() }
                val hi=freq.indexOfLast { it<=state.rangeHz.toDouble() }
                fun curve(samples:DoubleArray,traceColor:Color) {
                    if(!state.validSignal || lo<0 || hi<=lo || samples.size<=hi) return
                    val path=Path()
                    for(i in lo..hi) {
                        val x=left+((freq[i]-state.minFrequencyHz)/(state.rangeHz-state.minFrequencyHz)*plotW).toFloat()
                        val y=bottom-((samples[i]-state.minDisplayDb)/(state.maxDisplayDb-state.minDisplayDb)).coerceIn(0.0,1.0).toFloat()*plotH
                        if(i==lo)path.moveTo(x,y) else path.lineTo(x,y)
                    }
                    drawPath(path,traceColor,style=Stroke(width=1.0.dp.toPx()))
                }
                if(state.showFloor) curve(state.floor,Color(0xFFB5B7BC))
                if(state.showAverage) curve(state.average,Color(0xFFE62B38))
                if(state.showMaxHold) curve(state.maxHold,Color(0xFF30D7EF))
                if(state.showCurrent) curve(state.live,Color(0xFFFFDC24))
                if(state.direct48k && state.directPeakMarkers > 0 && state.validSignal) {
                    val peaks = AnalyzerPeakMarkers.find(freq,state.live,state.minFrequencyHz,state.rangeHz,state.directPeakMarkers)
                    val labelPaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
                        textSize=9.sp.toPx(); color=AndroidColor.WHITE
                        setShadowLayer(3f,0f,0f,AndroidColor.BLACK)
                    }
                    peaks.forEachIndexed { index, peak ->
                        val x=left+((peak.frequencyHz-state.minFrequencyHz)/(state.rangeHz-state.minFrequencyHz)*plotW).toFloat()
                        val y=bottom-((peak.dbFs-state.minDisplayDb)/(state.maxDisplayDb-state.minDisplayDb)).coerceIn(0.0,1.0).toFloat()*plotH
                        drawLine(Color.White,Offset(x,(y-7.dp.toPx()).coerceAtLeast(top)),Offset(x,(y+7.dp.toPx()).coerceAtMost(bottom)),strokeWidth=1.dp.toPx())
                        val label=String.format(Locale.US,"P%d %.1f Hz %.1f dBFS",index+1,peak.frequencyHz,peak.dbFs)
                        val textX=(x+4.dp.toPx()).coerceAtMost(right-labelPaint.measureText(label))
                        drawIntoCanvas { canvas -> canvas.nativeCanvas.drawText(label,textX.coerceAtLeast(left),
                            (y-9.dp.toPx()).coerceIn(top+10.dp.toPx(),bottom),labelPaint) }
                    }
                }

                cursorHz?.let { hz ->
                    val x=left+((hz-state.minFrequencyHz)/(state.rangeHz-state.minFrequencyHz)*plotW).toFloat()
                    drawLine(cyan,Offset(x,top),Offset(x,bottom),strokeWidth=0.85.dp.toPx())
                    val label=cursorReadout(state,hz)
                    val paint=NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
                        color=AndroidColor.rgb(48,215,239)
                        textSize=10.sp.toPx()
                        // Text outline/shadow preserves readability without a box.
                        setShadowLayer(2.dp.toPx(),0f,1.dp.toPx(),AndroidColor.BLACK)
                    }
                    val width=paint.measureText(label)+2.dp.toPx()
                    val nearStats=x+width+3.dp.toPx()>right-108.dp.toPx()
                    val bx=(if (nearStats) x-width-3.dp.toPx() else x+3.dp.toPx())
                        .coerceIn(left,(right-width).coerceAtLeast(left))
                    val baseline=top+22.dp.toPx()
                    drawIntoCanvas { canvas -> canvas.nativeCanvas.drawText(label,bx+1.dp.toPx(),baseline,paint) }
                }
                if(state.autoRangeActive) {
                    drawRect(cyan.copy(alpha=0.7f),Offset(left,top),Size(plotW*state.autoRangeProgress,2.dp.toPx()))
                }
            }
            // The complete compact frame is also the sensitive reset surface. Keep the
            // three statistic rows tightly stacked so Leq does not make the box oversized.
            val statsRange = "${state.statisticsMinHz}-${state.statisticsMaxHz}"
            Column(Modifier.align(Alignment.TopEnd).padding(top=4.dp,end=4.dp)
                .border(BorderStroke(1.dp,Color(0xFFE34949)),RoundedCornerShape(6.dp))
                .clickable {
                    LiveAnalyzerController.resetBandStatisticsAndAverage()
                    state=LiveAnalyzerController.snapshot()
                }.padding(horizontal=3.dp,vertical=2.dp),
                horizontalAlignment=Alignment.End,
                verticalArrangement=Arrangement.spacedBy(0.dp)) {
                Text("Leq($statsRange): ${state.leqDbFs?.let { String.format(Locale.US,"%.1f",it) } ?: "–"}",
                    color=white,fontSize=10.sp,lineHeight=10.sp)
                Text("L50($statsRange): ${state.l50DbFs?.let { String.format(Locale.US,"%.1f",it) } ?: "–"}",
                    color=white,fontSize=10.sp,lineHeight=10.sp)
                Text("L90($statsRange): ${state.l90DbFs?.let { String.format(Locale.US,"%.1f",it) } ?: "–"}",
                    color=white,fontSize=10.sp,lineHeight=10.sp)
            }
            if(!state.validSignal) Text("Kein verwertbares Eingangssignal · Diagnose in Einstellungen",color=Color(0xFFFFD17C),
                fontSize=12.sp,modifier=Modifier.align(Alignment.Center).clickable { settings=true })
        }
        // Waterfall is directly below the spectrum; tapping it toggles expanded mode.
        val waterfall=remember(state.waterfallRevision,state.rangeHz,state.minFrequencyHz,
            state.minDisplayDb,state.maxDisplayDb,state.filterDisplayMinDb,state.filterDisplayMaxDb){
            makeWaterfall(state.rows,state.waterfallFrequencyHz,state.minFrequencyHz,state.rangeHz,state.historyCapacity,
                state.minDisplayDb,state.maxDisplayDb,state.filterDisplayMinDb,state.filterDisplayMaxDb,
                state.filterRowsDbFs,state.filterRowsExcessDb,
                state.direct48k || state.showOriginalWaterfall,!state.direct48k && state.showFilterWaterfall,
                state.filterMinHz,state.filterMaxHz)
        }
        Box(Modifier.fillMaxWidth().height(waterfallHeight).background(Color(0xFF130C29))
            .clickable {expanded=!expanded}) {
            Canvas(Modifier.matchParentSize()) {
                val left=plotLeftDp.toPx();val right=size.width-plotRightDp.toPx()
                val top=4.dp.toPx();val bottom=size.height-plotBottomDp.toPx()
                val plotW=right-left;val plotH=bottom-top
                if(plotW<=0f||plotH<=0f)return@Canvas
                // The dBFS legend now lives LEFT of the waterfall; numbers are INSIDE it.
                val barLeft=3.dp.toPx()
                // The color ramp consumes only the width of its widest signed label.
                val filterOnly=!state.direct48k && state.showFilterWaterfall && !state.showOriginalWaterfall
                val legendMin=if(filterOnly) state.filterDisplayMinDb else state.minDisplayDb
                val legendMax=if(filterOnly) state.filterDisplayMaxDb else state.maxDisplayDb
                val labelWidth = AnalyzerDisplayTicks.db(legendMin,legendMax)
                    .maxOf { db -> NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
                        textSize=7.sp.toPx(); textScaleX=0.86f
                    }.measureText("$db") }
                val barRight=barLeft+labelWidth+2.dp.toPx()
                val axisColor=Color(0xFFB8C4CA)
                val textPaint=NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
                    color=AndroidColor.rgb(185,196,205);textSize=9.sp.toPx()
                }
                waterfall?.let { image ->
                    if(state.validSignal) drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawBitmap(image,Rect(0,0,image.width,image.height),
                            RectF(left,top,right,bottom),NativePaint(NativePaint.FILTER_BITMAP_FLAG))
                    }
                }
                // Black live timeline between the existing colorbar and the FIXED spectrum x-origin.
                // Ticks are attached to real acquired rows, not fabricated elapsed seconds.
                val timeBandL = barRight + 1.dp.toPx()
                val timeBandR = left - 5.dp.toPx()
                if (timeBandR > timeBandL) {
                    drawRect(Color.Black, Offset(timeBandL, top), Size(timeBandR - timeBandL, plotH))
                    val ticks = WaterfallTimeAxis.ticks(state.rowEpochMillis,
                        state.historySeconds, state.historyCapacity, state.waterfallIntervalMs)
                    val dateFmt = SimpleDateFormat("dd.MM", Locale.getDefault())
                    val clockFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val timePaint = NativePaint(NativePaint.ANTI_ALIAS_FLAG).apply {
                        color=AndroidColor.WHITE; textSize=7.sp.toPx(); textAlign=NativePaint.Align.CENTER
                    }
                    val centerX = (timeBandL + timeBandR) / 2f
                    val halfLine = 4.2.dp.toPx()
                    val guard = halfLine * 2f + 1.dp.toPx()
                    val usedPositions = mutableListOf<Float>()
                    ticks.forEach { tick ->
                        val y = top + (tick.ageRows / state.historyCapacity.toFloat()) * plotH
                        val labelY = if (tick.ageRows == 0f) top + guard else y
                        if (labelY - guard < top || labelY + guard > bottom ||
                            usedPositions.any { kotlin.math.abs(it - labelY) < guard * 2f }) return@forEach
                        val date = dateFmt.format(Date(tick.epochMillis))
                        val clock = clockFmt.format(Date(tick.epochMillis))
                        timePaint.textScaleX = 1f
                        val widest = maxOf(timePaint.measureText(date), timePaint.measureText(clock))
                        timePaint.textScaleX = minOf(1f, (timeBandR-timeBandL-1.dp.toPx()) / widest.coerceAtLeast(1f))
                        drawLine(Color.White, Offset(left-4.dp.toPx(), y), Offset(left, y),
                            strokeWidth=1.dp.toPx())
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(date,centerX,labelY-halfLine*0.48f,timePaint)
                            canvas.nativeCanvas.drawText(clock,centerX,labelY+halfLine*1.52f,timePaint)
                        }
                        usedPositions.add(labelY)
                    }
                }
                for(hz in AnalyzerTiming.frequencyTicks(state.minFrequencyHz,state.rangeHz)) {
                    val x=left+(hz-state.minFrequencyHz).toFloat()/(state.rangeHz-state.minFrequencyHz)*plotW
                    drawLine(axisColor,Offset(x,bottom),Offset(x,bottom+5.dp.toPx()),strokeWidth=1.dp.toPx())
                    drawIntoCanvas { canvas ->
                        textPaint.textAlign=NativePaint.Align.CENTER
                        canvas.nativeCanvas.drawText("$hz",x,bottom+17.dp.toPx(),textPaint)
                    }
                }
                for(i in 0 until 120) {
                    val db=legendMax-(i+0.5)*(legendMax-legendMin)/120.0
                    drawRect(Color(if(filterOnly) FilterWaterfallRenderer.filterColor(db,legendMin,legendMax)
                        else LiveAnalyzerController.color(db,legendMin,legendMax)),
                        Offset(barLeft,top+i/120f*plotH),Size(barRight-barLeft,plotH/120f+0.5f))
                }
                for(db in AnalyzerDisplayTicks.db(legendMin,legendMax).asReversed()) {
                    val y=top+(legendMax-db).toFloat()/(legendMax-legendMin)*plotH
                    drawLine(Color.Black,Offset(barLeft,y),Offset(barLeft+3.dp.toPx(),y),strokeWidth=1.dp.toPx())
                    drawIntoCanvas { canvas ->
                        textPaint.textAlign=NativePaint.Align.LEFT
                        textPaint.textSize=7.sp.toPx()
                        textPaint.textScaleX=0.86f // Fit signed four-digit labels in the narrow ramp.
                        textPaint.color=if ((db-legendMin).toFloat()/(legendMax-legendMin)>0.58f)
                            AndroidColor.BLACK else AndroidColor.WHITE
                        // Clamp baseline at the edges: no clipped first/last number.
                        val base=y.coerceIn(top+8.dp.toPx(), bottom-2.dp.toPx())
                        canvas.nativeCanvas.drawText("$db",barLeft+1.dp.toPx(),base,textPaint)
                    }
                }
                val unitX = left + ((AnalyzerTiming.unitFrequencyHz(state.minFrequencyHz,state.rangeHz) - state.minFrequencyHz) /
                    (state.rangeHz-state.minFrequencyHz) * plotW).toFloat()
                drawIntoCanvas { canvas ->
                    textPaint.textSize=9.sp.toPx();textPaint.textAlign=NativePaint.Align.CENTER
                    textPaint.color=AndroidColor.rgb(185,196,205)
                    canvas.nativeCanvas.drawText("Hz",unitX,bottom+17.dp.toPx(),textPaint)
                }
                cursorHz?.takeIf { it >= state.minFrequencyHz && it <= state.rangeHz }?.let { hz ->
                    val x=left+((hz-state.minFrequencyHz)/(state.rangeHz-state.minFrequencyHz)*plotW).toFloat()
                    drawLine(cyan,Offset(x,top),Offset(x,bottom),strokeWidth=0.85.dp.toPx())
                }
                state.modeBoundaryRows?.let { age ->
                    val y = top + age.toFloat()/state.historyCapacity*plotH
                    if (y in top..bottom) drawLine(cyan,Offset(left,y),Offset(right,y),strokeWidth=1.dp.toPx())
                }
            }
            // Two-line waterfall notification is a composited overlay at the LOWER-RIGHT
            // of the plot (inside Hz-axis margins). It does not change the Bitmap, plot
            // coordinates, underlying pixels, colorbar, time axis, or PNG/JPG export.
            if (waterfallNotice != null) {
                val notice = waterfallNotice!!
                Column(Modifier.align(Alignment.BottomEnd)
                    .padding(end=plotRightDp + 4.dp, bottom=plotBottomDp + 4.dp)
                    .widthIn(max=waterfallNoticeMaxWidth)
                    .border(BorderStroke(1.dp, Color(0xFF8995A0)), RoundedCornerShape(5.dp))
                    .background(Color(0xE6081016), RoundedCornerShape(5.dp))
                    .padding(horizontal=5.dp, vertical=3.dp),
                    horizontalAlignment=Alignment.End,
                    verticalArrangement=Arrangement.spacedBy(0.dp)) {
                    Text(notice.first, color=white, fontSize=10.sp, lineHeight=11.sp,
                        maxLines=1, overflow=TextOverflow.Ellipsis)
                    Text(notice.second,
                        color=if (notice.isFilter) Color(0xFFFF99E8) else muted,
                        fontSize=9.sp, lineHeight=10.sp,
                        maxLines=1, overflow=TextOverflow.Ellipsis)
                }
            }
            if(!state.validSignal) Text("Warte auf gültige FFT-Daten",color=muted,fontSize=11.sp,
                modifier=Modifier.align(Alignment.Center))
        }
        // Mithören / Superfast are later features; the redundant Anzeige card is removed.
        // Average and noise-floor switches remain accessible under Einstellungen → Spektrum.
        var noiseMessage by remember { mutableStateOf("") }
        var noiseState by remember { mutableStateOf(NoiseFloorReference.state()) }
        LaunchedEffect(Unit) {
            while (true) { noiseState = NoiseFloorReference.state(); delay(500) }
        }
        Row(Modifier.fillMaxWidth().padding(top=4.dp),horizontalArrangement=Arrangement.Start){
            Box(Modifier.size(52.dp).border(BorderStroke(1.dp,
                if (noiseState.enabled) cyan else muted), RoundedCornerShape(6.dp))
                .pointerInput(Unit) { detectTapGestures(
                    onTap = { noiseMessage=LiveAnalyzerController.toggleNoiseCalibration(); noiseState=NoiseFloorReference.state() },
                    onLongPress = { noiseMessage=LiveAnalyzerController.startNoiseCalibration(); noiseState=NoiseFloorReference.state() }
                ) }, contentAlignment=Alignment.Center) {
                Column(horizontalAlignment=Alignment.CenterHorizontally) {
                    Text("NOISE", color=if (noiseState.enabled) cyan else white, fontSize=10.sp, lineHeight=12.sp)
                    Text(if(noiseState.capturing) "${noiseState.progress}%" else when(noiseState.mode) { "MEASURED" -> "MESS"; "MODEL" -> "1/f"; else -> "AUS" },
                        color=if(noiseState.enabled) cyan else muted, fontSize=11.sp, lineHeight=12.sp)
                }
            }
            if (noiseMessage.isNotEmpty()) Text(noiseMessage, color=cyan,fontSize=10.sp, modifier=Modifier.widthIn(max=100.dp))
            var diagnosticOpen by remember { mutableStateOf(false) }
            Text("DIAG", color=cyan, fontSize=12.sp,
                modifier=Modifier.padding(start=6.dp,top=13.dp).clickable { diagnosticOpen=true })
            if(diagnosticOpen) {
                AlertDialog(onDismissRequest={diagnosticOpen=false},
                    title={ Text("Rauschdiagnose") },
                    text={ Column(modifier=Modifier.verticalScroll(rememberScrollState()), verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        var diagnosticStatus by remember { mutableStateOf(NoiseDiagnostic.result) }
                        LaunchedEffect(Unit) {
                            while(true) { diagnosticStatus=NoiseDiagnostic.result; delay(500) }
                        }
                        Text("Synthetisches weißes PCM-Signal durch denselben LiveAnalyzerCore (1000 Hz / 4096 / Hann). Keine Mikrofonmessung.")
                        Text(diagnosticStatus)
                        Text(NoiseFloorReference.diagnosticReference())
                        Text(NoiseFloorReference.modelDiagnostic())
                        var timingText by remember { mutableStateOf(NoiseFloorReference.timingDiagnostic()) }
                        LaunchedEffect(Unit) {
                            while (true) { timingText=NoiseFloorReference.timingDiagnostic(); delay(1000) }
                        }
                        Text("1/f-Laufzeitdiagnose (Main und Rolling getrennt):")
                        Text(timingText)
                        OutlinedButton(onClick={ { NoiseFloorReference.resetTimingDiagnostic(); timingText=NoiseFloorReference.timingDiagnostic() } }) { Text("Laufzeitzähler zurücksetzen") }
                        OutlinedButton(onClick={
                            val snap = LiveAnalyzerController.snapshot()
                            val report = timingText + "\nPCM-Alter=" + snap.signalAgeMs +
                                "ms; FFT-Zaehler=" + snap.spectra + "; validSignal=" + snap.validSignal +
                                "; status=" + snap.status
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("LFN 1f Diagnose", report))
                        }) { Text("Diagnose kopieren") }
                        val rawSnapshot = LiveAnalyzerController.snapshot()
                        Text("PCM-Alter=${rawSnapshot.signalAgeMs}ms · FFT-Zähler=${rawSnapshot.spectra} · Signal=${rawSnapshot.validSignal} · Status=${rawSnapshot.status}")
                        Text("NOISE kurz: AUS → MESSPROFIL → 1/f-MODELL → AUS. Lang: neue Referenz einmessen.")
                        Text("Vergleich: NOISE AUS = Original; NOISE EIN = Rest. Referenzkurve in dieser Version als Steigung, nicht als separater Plot.")
                        OutlinedButton(onClick={NoiseDiagnostic.startWhiteNoiseTest()}) { Text("FFT-Selbsttest starten") }
                    } },
                    confirmButton={TextButton(onClick={diagnosticOpen=false}) { Text("Schließen") }})
            }

            // Tap toggles auto images, long-press requests exactly one independent manual image.
            // No separate Auto EIN/AUS label; the crossed camera represents OFF.
            Box(Modifier.size(48.dp).pointerInput(state.validSignal) {
                detectTapGestures(
                    onTap = {
                        LiveAnalyzerController.setAutoSave(context,!LiveAnalyzerController.snapshot().autoSave)
                        state=LiveAnalyzerController.snapshot()
                        saveMessage=""
                    },
                    onLongPress = {
                        saveMessage=""
                        val captured = LiveAnalyzerController.captureManualImage()
                        if (captured == null) {
                            saveMessage="Bild nicht gespeichert: kein Nutzsignal oder Bildexport beschäftigt"
                        } else {
                            pendingImageSnapshot = captured
                            imageComment = ""
                        }
                    }
                )
            },contentAlignment=Alignment.Center) {
                Text("📷",color=white,fontSize=27.sp)
                if (!state.autoSave) Canvas(Modifier.matchParentSize()) {
                    val margin=7.dp.toPx()
                    drawLine(Color(0xFFF04438),Offset(margin,margin),
                        Offset(size.width-margin,size.height-margin),strokeWidth=3.dp.toPx())
                    drawLine(Color(0xFFF04438),Offset(size.width-margin,margin),
                        Offset(margin,size.height-margin),strokeWidth=3.dp.toPx())
                }
                // Anchor the actual completed-save notification at the CAMERA, rather than
                // at the top of the screen. Popup is an independent window and does not
                // expand or clip the 48-dp button's layout.
                if (saveMessage.isNotEmpty()) {
                    val popY = with(LocalDensity.current) { (-82).dp.roundToPx() }
                    Popup(alignment=Alignment.TopStart,offset=IntOffset(0,popY)) {
                        Box(Modifier.border(BorderStroke(1.dp,
                            if(saveMessage.startsWith("Bild gespeichert")) cyan else Color(0xFFFF6655)),
                            RoundedCornerShape(9.dp))
                            .background(Color(0xFF102E36),RoundedCornerShape(9.dp))
                            .padding(horizontal=12.dp,vertical=9.dp)) {
                            Text(saveMessage,
                                color=if(saveMessage.startsWith("Bild gespeichert")) cyan else Color(0xFFFF6655),
                                fontSize=13.sp,fontWeight=FontWeight.Bold)
                        }
                    }
                }
            }
            // SAVE AUDIO is a 48-dp action button like the camera. No status text is
            // inserted into measurement/display rows; all save feedback is a transient popup.
            Box(Modifier.size(48.dp)
                .clickable(enabled=canLocalSave) {
                    // Freeze/copy the complete audio window BEFORE opening the comment dialog.
                    // Keyboard/tap sounds therefore cannot enter this saved clip.
                    val clip = LocalAnalyzerAudioBuffer.take(localSaveConfig.durationSeconds, state.paused)
                    if (clip == null) {
                        audioSaveStatus = "Audiofenster nicht vollständig verfügbar"
                    } else {
                        pendingAudioClip = clip
                        pendingAudioConfig = StatisticsWindowEngine.Configuration(
                            localSaveConfig.minHz.toDouble(), localSaveConfig.maxHz.toDouble(),
                            if (state.direct48k) localSaveConfig.minHz.toDouble() else state.filterMinHz,
                            if (state.direct48k) localSaveConfig.maxHz.toDouble() else state.filterMaxHz,
                            if (state.direct48k) 10 else state.filterBandCount)
                        pendingAudioDisplayGainDb = state.analysisGainDb
                        audioSaveStatus = ""
                    }
                }, contentAlignment=Alignment.Center) {
                Canvas(Modifier.size(32.dp)) {
                    val c = if (canLocalSave) cyan else muted
                    val stroke = 2.dp.toPx()
                    val xs = listOf(6f, 10f, 14f, 18f, 22f).map { it.dp.toPx() }
                    val heights = listOf(7f, 14f, 20f, 14f, 7f).map { it.dp.toPx() }
                    xs.forEachIndexed { i, x ->
                        drawLine(c, Offset(x, 13.dp.toPx()-heights[i]/2f),
                            Offset(x, 13.dp.toPx()+heights[i]/2f), strokeWidth=stroke)
                    }
                    val center = 24.dp.toPx()
                    drawLine(c, Offset(center, 5.dp.toPx()), Offset(center, 24.dp.toPx()), strokeWidth=stroke)
                    drawLine(c, Offset(center, 24.dp.toPx()), Offset(19.dp.toPx(), 19.dp.toPx()), strokeWidth=stroke)
                    drawLine(c, Offset(center, 24.dp.toPx()), Offset(29.dp.toPx(), 19.dp.toPx()), strokeWidth=stroke)
                    drawLine(c, Offset(18.dp.toPx(), 28.dp.toPx()), Offset(30.dp.toPx(), 28.dp.toPx()), strokeWidth=stroke)
                }
                if (audioSaveBusy) Text("…", color=cyan, fontSize=16.sp, modifier=Modifier.align(Alignment.BottomEnd))
                if (audioSaveStatus.isNotEmpty()) {
                    val popY = with(LocalDensity.current) { (-82).dp.roundToPx() }
                    val ok = audioSaveStatus.startsWith("Lokal gespeichert")
                    val info = audioSaveStatus == "Speichert …"
                    Popup(alignment=Alignment.TopStart,offset=IntOffset(0,popY)) {
                        Box(Modifier.border(BorderStroke(1.dp, if(ok || info) cyan else Color(0xFFFF6655)),
                            RoundedCornerShape(9.dp))
                            .background(Color(0xFF102E36),RoundedCornerShape(9.dp))
                            .padding(horizontal=12.dp,vertical=9.dp)) {
                            Text(audioSaveStatus, color=if(ok || info) cyan else Color(0xFFFF6655),
                                fontSize=13.sp,fontWeight=FontWeight.Bold)
                        }
                    }
                }
            }
            Text("${if(state.paused) (if(canLocalSave || pendingAudioClip != null) localSaveConfig.durationSeconds else 0) else availableSeconds}/${localSaveConfig.durationSeconds}s\n${dailyCount}/${dailyLimit}",
                color=muted,fontSize=9.sp,lineHeight=10.sp,modifier=Modifier.widthIn(min=42.dp,max=58.dp))
            if(!state.direct48k) {
                WaterfallSwitchIcon(original=true,active=state.showOriginalWaterfall, onLongPress={}) {
                    LiveAnalyzerController.setWaterfallVisibility(context,!state.showOriginalWaterfall,state.showFilterWaterfall)
                    state=LiveAnalyzerController.snapshot()
                }
                WaterfallSwitchIcon(original=false,active=state.showFilterWaterfall, onLongPress={
                    LiveAnalyzerController.startFilterAutoRange()
                    state=LiveAnalyzerController.snapshot()
                }) {
                    LiveAnalyzerController.setWaterfallVisibility(context,state.showOriginalWaterfall,!state.showFilterWaterfall)
                    state=LiveAnalyzerController.snapshot()
                }
            }
            if (state.showMaxHold) Box(Modifier.size(48.dp).padding(start=2.dp)
                .border(BorderStroke(1.dp, cyan),RoundedCornerShape(6.dp))
                .clickable {
                    LiveAnalyzerController.resetSpectrumMaxHold()
                    state=LiveAnalyzerController.snapshot()
                }, contentAlignment=Alignment.Center) {
                Column(horizontalAlignment=Alignment.CenterHorizontally,
                    verticalArrangement=Arrangement.spacedBy(0.dp)) {
                    Text("MAX",color=cyan,fontSize=10.sp,fontWeight=FontWeight.Bold,lineHeight=11.sp)
                    Text("CLR",color=cyan,fontSize=10.sp,fontWeight=FontWeight.Bold,lineHeight=11.sp)
                }
            }
        } // bottom action row
        } // end main analyzer view
        } // scrolling content
    } // full-viewport dark background
}

/** Only the four implemented display ranges are currently available. Units are in axis titles,
 * never appended to the final (100/200) numeric tick. */
private fun cursorReadout(state: LiveAnalyzerController.Snapshot, hz: Double): String {
    val bin=state.frequencyHz.indexOfFirst { it >= hz }
    val value=if(state.validSignal && bin in state.live.indices)
        String.format(Locale.US,"%.1f",state.live[bin]) else "–"
    return String.format(Locale.US,"%.1f Hz · %s dBFS",hz,value)
}

/** Time is VERTICAL. First pixel row is the newest actual spectrum; old rows fall down.
 * Empty history is left blank at the bottom. No fabricated data / interpolation in time.
 */
private fun makeWaterfall(rows: List<DoubleArray>, freq: DoubleArray, minHz: Int, maxHz: Int, capacity: Int, minDb: Int, maxDb: Int,
    filterMinDb: Int, filterMaxDb: Int,
    filterDb: List<DoubleArray?>, filterExcess: List<DoubleArray?>, original: Boolean, filter: Boolean,
    filterMin: Double, filterMax: Double): Bitmap? {
    if(rows.isEmpty() || freq.isEmpty() || capacity<=0) return null
    val width=400
    // At 250-ms default waterfall timebase, the 120-s history has 480 rows.
    // Cap the UI raster to 480 pixels high; sample REAL rows at corresponding ages.
    // Preserve full history in the controller/PNG; do not create interpolated spectra.
    val height=minOf(capacity,480)
    val background=AndroidColor.rgb(19,12,41)
    val out=IntArray(width*height) { background }
    val lowBin=freq.indexOfFirst { it>=minHz.toDouble() }
    val bins=freq.indexOfLast { it<=maxHz.toDouble() }
    if(lowBin<0 || bins<=lowBin) return null
    val newestFirst=rows.takeLast(capacity).asReversed()
    val filterFirst=filterDb.takeLast(capacity).asReversed()
    val excessFirst=filterExcess.takeLast(capacity).asReversed()
    for(y in 0 until height) {
        val age=(y.toLong()*capacity/height).toInt()
        if(age>=newestFirst.size) continue
        val row=newestFirst[age]
        for(x in 0 until width) {
            val bin=(lowBin+x.toDouble()/(width-1)*(bins-lowBin)).roundToInt().coerceIn(0,row.lastIndex)
            val hz=minHz+x.toDouble()/(width-1)*(maxHz-minHz)
            out[y*width+x]=FilterWaterfallRenderer.colorAt(hz,
                LiveAnalyzerController.color(row[bin],minDb,maxDb),filterFirst.getOrNull(age),excessFirst.getOrNull(age),
                original,filter,filterMin,filterMax,minDb,maxDb,filterMinDb,filterMaxDb)
        }
    }
    return Bitmap.createBitmap(out,width,height,Bitmap.Config.ARGB_8888)
}


/** Two fixed 48-dp icons. ORIGINAL uses exact user-supplied PNG; FILTER retains five
 * symmetric magenta/pink/white bars. Tap toggles visibility; FILTER long-press auto-ranges. */
@Composable
private fun WaterfallSwitchIcon(original: Boolean, active: Boolean,
    onLongPress: () -> Unit, onClick: () -> Unit) {
    val outline=if(active) Color(0xFFE8D9F1) else Color(0xFF48525C)
    Box(Modifier.size(48.dp).padding(1.dp)
        .border(BorderStroke(1.dp,outline),androidx.compose.foundation.shape.RoundedCornerShape(7.dp))
        .background(Color(0xFF101016),androidx.compose.foundation.shape.RoundedCornerShape(7.dp))
        .pointerInput(original,active) {
            detectTapGestures(onTap={ onClick() }, onLongPress={ onLongPress() })
        },contentAlignment=Alignment.Center) {
        if (original) {
            // Byte-for-byte user-provided image, resized to fit the approved camera-sized button.
            Image(painter=painterResource(R.drawable.original_waterfall_icon),
                contentDescription="ORIGINAL-Wasserfall",modifier=Modifier.size(36.dp))
        } else Canvas(Modifier.size(36.dp)) {
            val w=size.width; val h=size.height
            run {
                val heights=floatArrayOf(.40f,.72f,1.0f,.72f,.40f)
                val colors=intArrayOf(0xFF9911A9.toInt(),0xFFE348C6.toInt(),0xFFFFFFFF.toInt(),
                    0xFFE348C6.toInt(),0xFF9911A9.toInt())
                for(i in 0..4) {
                    val bh=h*heights[i]
                    drawRect(Color(colors[i]),Offset(i*w/5f+1f,h-bh),Size(w/5f-2f,bh))
                }
            }
        }
        if(!active) Canvas(Modifier.matchParentSize()) {
            drawLine(Color(0xFFFA5252),Offset(5.dp.toPx(),size.height-5.dp.toPx()),
                Offset(size.width-5.dp.toPx(),5.dp.toPx()),strokeWidth=1.5.dp.toPx())
        }
    }
}
