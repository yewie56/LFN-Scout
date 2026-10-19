package de.brummbeobachter.app

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.exp
import kotlin.math.ln

/** v0.4.99: one-column category navigation replaces the horizontal legacy register strip.
 * All active controls delegate to the existing controller and its persistent preferences.
 * Planned features are shown only as information, never as clickable dummy switches.
 */
@Composable
internal fun LiveAnalyzerSettingsPanel(
    context: Context,
    state: LiveAnalyzerController.Snapshot,
    onRefresh: () -> Unit,
    onNavigate: () -> Unit,
    selected: String?,
    onSelect: (String?) -> Unit,
    onOpenLocalSaveSettings: () -> Unit
) {
    val prefs = remember(context) {
        context.getSharedPreferences("lfn_scout_live_analyzer",Context.MODE_PRIVATE)
    }
    var slot by remember(state.direct48k) { mutableStateOf(if (state.direct48k) 4 else prefs.getInt("last_setup_slot",1).coerceIn(1,3)) }
    var message by remember { mutableStateOf("") }
    var selfTestSummary by remember { mutableStateOf("") }
    var systemTestBusy by remember { mutableStateOf(false) }
    val exportScope = rememberCoroutineScope()
    var setupName by remember(slot) { mutableStateOf(prefs.getString("setup_${slot}_name", "") ?: "") }
    var setupDescription by remember(slot) { mutableStateOf(prefs.getString("setup_${slot}_description", "") ?: "") }
    val white = Color(0xFFF1F6F8)
    val muted = Color(0xFFABB8C0)
    val cyan = Color(0xFF30D7EF)
    val amber = Color(0xFFFFB33B)
    val surface = Color(0xFF112027)
    val stroke = Color(0xFF34515B)

    fun saveSetup() {
        prefs.edit()
            .putString("setup_${slot}_name",setupName.trim())
            .putString("setup_${slot}_description",setupDescription.trim())
            .putInt("setup_${slot}_decimation_factor",state.decimationFactor)
            .putInt("setup_${slot}_stat_min",state.statisticsMinHz)
            .putInt("setup_${slot}_stat_max",state.statisticsMaxHz)
            .putInt("setup_${slot}_stat_window",state.statisticsWindowSec)
            .putFloat("setup_${slot}_stat_average",state.statisticsAverageSec.toFloat())
            .putInt("setup_${slot}_range",state.rangeHz)
            .putInt("setup_${slot}_min_frequency",state.minFrequencyHz)
            .putInt("setup_${slot}_history_seconds",state.historySeconds)
            .putInt("setup_${slot}_fft_size",state.fftSize)
            .putString("setup_${slot}_window",state.windowType.name)
            .putBoolean("setup_${slot}_smoothing_enabled",state.smoothingEnabled)
            .putFloat("setup_${slot}_smoothing_time",state.smoothingTimeSeconds.toFloat())
            .putBoolean("setup_${slot}_auto",state.autoSave)
            .putString("setup_${slot}_image_format",state.imageFormat.name)
            .putInt("setup_${slot}_jpg_quality",state.jpgQuality)
            .putInt("setup_${slot}_fft_hop",state.fftHopSamples)
            .putInt("setup_${slot}_water_interval",state.waterfallIntervalMs)
            .putString("setup_${slot}_water_mode",state.waterfallMode.name)
            .putInt("setup_${slot}_display_min",state.minDisplayDb)
            .putInt("setup_${slot}_display_max",state.maxDisplayDb)
            .putBoolean("setup_${slot}_current",state.showCurrent)
            .putBoolean("setup_${slot}_average",state.showAverage)
            .putBoolean("setup_${slot}_max_hold",state.showMaxHold)
            .putBoolean("setup_${slot}_floor",state.showFloor)
            .putInt("setup_${slot}_gain",state.analysisGainDb)
            .putInt("setup_${slot}_autorange_duration",state.autoRangeDurationSec)
            .putInt("setup_${slot}_autorange_margin",state.autoRangeMarginDb)
            .putInt("setup_${slot}_direct_sample_rate",state.directInputRateHz)
            .putString("setup_${slot}_direct_audio_source",prefs.getString("direct_audio_source","AUTO"))
            .putFloat("setup_${slot}_direct_smoothing_alpha",state.directSmoothingAlpha.toFloat())
            .putBoolean("setup_${slot}_direct_time_smoothing",state.directTimeSmoothing)
            .putInt("setup_${slot}_filter_min_hz",state.filterMinHz.toInt())
            .putInt("setup_${slot}_filter_max_hz",state.filterMaxHz.toInt())
            .putInt("setup_${slot}_filter_bands",state.filterBandCount)
            .putInt("setup_${slot}_filter_display_min",state.filterDisplayMinDb)
            .putInt("setup_${slot}_filter_display_max",state.filterDisplayMaxDb)
            .putInt("setup_${slot}_filter_auto_duration",state.filterAutoRangeDurationSec)
            .putInt("setup_${slot}_filter_auto_margin",state.filterAutoRangeMarginDb)
            .putBoolean("setup_${slot}_filter_show_original",state.showOriginalWaterfall)
            .putBoolean("setup_${slot}_filter_show_filter",state.showFilterWaterfall)
            .putInt("setup_${slot}_direct_peak_markers",state.directPeakMarkers)
            .putBoolean("setup_${slot}_direct_subtract_dc",state.directSubtractDc)
            .putBoolean("setup_${slot}_direct_stay_awake",state.directStayAwake)
            .putBoolean("setup_${slot}_direct_test1_on",prefs.getBoolean("mode_48_direct_test1_on",true))
            .putBoolean("setup_${slot}_direct_test2_on",prefs.getBoolean("mode_48_direct_test2_on",false))
            .putFloat("setup_${slot}_direct_test1_hz",prefs.getFloat("mode_48_direct_test1_hz",50f))
            .putFloat("setup_${slot}_direct_test2_hz",prefs.getFloat("mode_48_direct_test2_hz",195f))
            .commit()
        message = "Setup $slot gespeichert; JSON-Export läuft …"
        exportScope.launch {
            runCatching { withContext(Dispatchers.IO) { AnalyzerInspectionExport.exportSetups(context) } }
                .onSuccess { message="Setup $slot gespeichert · $it JSON-Datei(en) in ${PublicDataDirectory.relativePath}Setups/" }
                .onFailure { message="Setup intern gespeichert; JSON-Export fehlgeschlagen: ${it.javaClass.simpleName}: ${it.message ?: "ohne Details"}" }
        }
    }
    fun loadSetup() {
        if(!prefs.contains("setup_${slot}_range")) {
            message="Setup $slot ist leer";return
        }
        setupName = prefs.getString("setup_${slot}_name", "") ?: ""
        setupDescription = prefs.getString("setup_${slot}_description", "") ?: ""
        if (state.direct48k) {
            if (state.running) { message="Direkt-Setup erst nach STOP laden"; return }
            val loadedRate=prefs.getInt("setup_${slot}_direct_sample_rate",48000)
                .let { if (it in DirectAnalyzerConfig.inputRates) it else 48000 }
            val loadedSource=prefs.getString("setup_${slot}_direct_audio_source","AUTO") ?: "AUTO"
            LiveAnalyzerController.setDirectCapture(context, loadedRate,
                if (loadedSource in listOf("AUTO","MIC","UNPROCESSED","VOICE_RECOGNITION")) loadedSource else "AUTO")
            val loadedFactor=prefs.getInt("setup_${slot}_decimation_factor",1)
            LiveAnalyzerController.setDirectDecimation(context,
                if (DirectAnalyzerConfig.valid(loadedRate,loadedFactor)) loadedFactor else 0)
            LiveAnalyzerController.setDirectOptions(context,
                prefs.getFloat("setup_${slot}_direct_smoothing_alpha",0.25f).toDouble().coerceIn(0.0,1.0),
                prefs.getInt("setup_${slot}_direct_peak_markers",0).coerceIn(0,2),
                prefs.getBoolean("setup_${slot}_direct_subtract_dc",true),
                prefs.getBoolean("setup_${slot}_direct_stay_awake",false))
            LiveAnalyzerController.setDirectTimeSmoothing(context,
                prefs.getBoolean("setup_${slot}_direct_time_smoothing",false))
            prefs.edit()
                .putBoolean("direct_test1_on",prefs.getBoolean("setup_${slot}_direct_test1_on",true))
                .putBoolean("direct_test2_on",prefs.getBoolean("setup_${slot}_direct_test2_on",false))
                .putFloat("direct_test1_hz",prefs.getFloat("setup_${slot}_direct_test1_hz",50f))
                .putFloat("direct_test2_hz",prefs.getFloat("setup_${slot}_direct_test2_hz",195f))
                .putBoolean("mode_48_direct_test1_on",prefs.getBoolean("setup_${slot}_direct_test1_on",true))
                .putBoolean("mode_48_direct_test2_on",prefs.getBoolean("setup_${slot}_direct_test2_on",false))
                .putFloat("mode_48_direct_test1_hz",prefs.getFloat("setup_${slot}_direct_test1_hz",50f))
                .putFloat("mode_48_direct_test2_hz",prefs.getFloat("setup_${slot}_direct_test2_hz",195f)).commit()
        }
        else {
            val lower=prefs.getInt("setup_${slot}_filter_min_hz",0).toDouble()
            val upper=prefs.getInt("setup_${slot}_filter_max_hz",100).toDouble()
            val count=prefs.getInt("setup_${slot}_filter_bands",10)
            if(runCatching { FastFilterBank.Config(lower,upper,count) }.isSuccess)
                LiveAnalyzerController.setFilterConfig(context,lower,upper,count)
            val filterLow=prefs.getInt("setup_${slot}_filter_display_min",-120)
            val filterHigh=prefs.getInt("setup_${slot}_filter_display_max",-60)
            LiveAnalyzerController.setFilterDisplayRange(context,
                if (filterLow in -240..40 && filterHigh in -230..50 && filterHigh-filterLow>=10) filterLow else -120,
                if (filterLow in -240..40 && filterHigh in -230..50 && filterHigh-filterLow>=10) filterHigh else -60)
            LiveAnalyzerController.setFilterAutoRangeSettings(context,
                prefs.getInt("setup_${slot}_filter_auto_duration",2).coerceIn(1,30),
                prefs.getInt("setup_${slot}_filter_auto_margin",5).coerceIn(0,30))
            LiveAnalyzerController.setWaterfallVisibility(context,
                prefs.getBoolean("setup_${slot}_filter_show_original",true),
                prefs.getBoolean("setup_${slot}_filter_show_filter",false))
        }
        // Apply compatible parameters on legacy slots without these keys.
        val statMin = prefs.getInt("setup_${slot}_stat_min",2)
        val statMax = prefs.getInt("setup_${slot}_stat_max",25)
        val statWindow = prefs.getInt("setup_${slot}_stat_window",120)
        val statAvg = prefs.getFloat("setup_${slot}_stat_average",5f).toDouble()
        LiveAnalyzerController.setStatistics(context,
            if (AnalyzerTiming.validStatistics(statMin,statMax,statWindow,statAvg)) statMin else 2,
            if (AnalyzerTiming.validStatistics(statMin,statMax,statWindow,statAvg)) statMax else 25,
            if (AnalyzerTiming.validStatistics(statMin,statMax,statWindow,statAvg)) statWindow else 120,
            if (AnalyzerTiming.validStatistics(statMin,statMax,statWindow,statAvg)) statAvg else 5.0)
        val lowFrequency = prefs.getInt("setup_${slot}_min_frequency",0)
        val highFrequency = prefs.getInt("setup_${slot}_range",100)
        val acceptedRange = if (state.direct48k) AnalyzerTiming.validWidebandRange(lowFrequency,highFrequency) &&
            highFrequency <= LiveAnalyzerController.snapshot().sampleRateHz/2
            else AnalyzerTiming.validRange(lowFrequency,highFrequency)
        LiveAnalyzerController.setFrequencyRange(context,
            if (acceptedRange) lowFrequency else 0,
            if (acceptedRange) highFrequency else if(state.direct48k) minOf(22000,LiveAnalyzerController.snapshot().sampleRateHz/2) else 100)
        val savedSize = prefs.getInt("setup_${slot}_fft_size",AnalyzerTiming.FFT_SIZE)
        val savedWindow = prefs.getString("setup_${slot}_window",AnalyzerWindow.HANN.name)
            ?.let { raw -> AnalyzerWindow.values().firstOrNull { it.name == raw } } ?: AnalyzerWindow.HANN
        val savedTime = prefs.getFloat("setup_${slot}_smoothing_time",5f).toDouble()
            .let { if (it.isFinite() && it in 0.01..60.0) it else 5.0 }
        LiveAnalyzerController.setDsp(context,
            if (savedSize in (if (state.direct48k) DirectAnalyzerConfig.fftSizes else AnalyzerTiming.allowedFftSizes)) savedSize else AnalyzerTiming.FFT_SIZE,
            savedWindow, prefs.getBoolean("setup_${slot}_smoothing_enabled",true), savedTime)
        LiveAnalyzerController.setAutoSave(context,prefs.getBoolean("setup_${slot}_auto",false))
        LiveAnalyzerController.setImageFormat(context,AnalyzerImageFormat.fromPreference(
            prefs.getString("setup_${slot}_image_format",AnalyzerImageFormat.PNG.name)))
        LiveAnalyzerController.setJpgQuality(context,
            prefs.getInt("setup_${slot}_jpg_quality",90).coerceIn(70,100))
        // Apply a safe history first, then the new interval, then the saved duration.
        LiveAnalyzerController.setHistorySeconds(context,AnalyzerTiming.HISTORY_SECONDS)
        // Legacy 8-kHz mode retains its independent FIR options; direct factor already applied above.
        if (!state.direct48k) LiveAnalyzerController.setDecimation(context,8)
        LiveAnalyzerController.setTiming(context,
            prefs.getInt("setup_${slot}_fft_hop",if (state.direct48k) LiveAnalyzerController.snapshot().sampleRateHz/10 else AnalyzerTiming.DEFAULT_FFT_HOP)
                .let { if(state.direct48k) {
                    val snap=LiveAnalyzerController.snapshot()
                    if (it in DirectAnalyzerConfig.transformIntervalsMs.map { ms ->
                            DirectAnalyzerConfig.hop(snap.directInputRateHz,snap.decimationFactor,ms) }) it
                        else DirectAnalyzerConfig.hop(snap.directInputRateHz,snap.decimationFactor,100)
                } else if (it in AnalyzerTiming.allowedFftHops) it else AnalyzerTiming.DEFAULT_FFT_HOP },
            prefs.getInt("setup_${slot}_water_interval",AnalyzerTiming.DEFAULT_WATERFALL_INTERVAL_MS)
                .let { if(it in AnalyzerTiming.allowedWaterfallIntervalsMs) it else AnalyzerTiming.DEFAULT_WATERFALL_INTERVAL_MS })
        val savedFactor = prefs.getInt("setup_${slot}_decimation_factor",8)
            .let { if(it in AnalyzerTiming.allowedDecimationFactors) it else 8 }
        if (!state.direct48k) LiveAnalyzerController.setDecimation(context,savedFactor)
        val newInterval = LiveAnalyzerController.snapshot().waterfallIntervalMs
        val savedHistory = prefs.getInt("setup_${slot}_history_seconds",AnalyzerTiming.HISTORY_SECONDS)
        LiveAnalyzerController.setHistorySeconds(context,
            if (AnalyzerTiming.intervalForDuration(savedHistory,newInterval,LiveAnalyzerController.snapshot().fftHopSamples,LiveAnalyzerController.snapshot().sampleRateHz) != null) savedHistory else AnalyzerTiming.HISTORY_SECONDS)
        LiveAnalyzerController.setWaterfallMode(context,
            prefs.getString("setup_${slot}_water_mode",WaterfallMode.CURRENT.name)
                ?.let { raw -> WaterfallMode.values().firstOrNull { it.name == raw } } ?: WaterfallMode.CURRENT)
        val low=prefs.getInt("setup_${slot}_display_min",-120)
        val high=prefs.getInt("setup_${slot}_display_max",-60)
        LiveAnalyzerController.setDisplayRange(context,
            if (low in -240..40 && high in -230..50 && high-low>=10) low else -120,
            if (low in -240..40 && high in -230..50 && high-low>=10) high else -60)
        LiveAnalyzerController.setSpectrumVisibility(context,
            prefs.getBoolean("setup_${slot}_current",true),
            prefs.getBoolean("setup_${slot}_average",true),
            prefs.getBoolean("setup_${slot}_max_hold",false))
        LiveAnalyzerController.setTraceVisibility(context,
            prefs.getBoolean("setup_${slot}_average",true),
            prefs.getBoolean("setup_${slot}_floor",false))
        LiveAnalyzerController.setAnalysisGain(context,prefs.getInt("setup_${slot}_gain",0).coerceIn(0,50))
        LiveAnalyzerController.setAutoRangeSettings(context,
            prefs.getInt("setup_${slot}_autorange_duration",2).coerceIn(1,30),
            prefs.getInt("setup_${slot}_autorange_margin",5).coerceIn(0,30))
        onRefresh();message="Setup $slot geladen"
    }
    if(selected == null) {
        // Navigation only: existing section identifiers and controller interactions remain unchanged.
        // Keep MON live controls in the analyzer itself so editing during RUN is unaffected.
        val groups = listOf(
            "SETUPS & PROFILE" to listOf(
                Triple("Setups", "Benannte Analyzer-JSONs · Auswahl und Schnellzugriff", "▤")
            ),
            "MESSUNG & SIGNAL" to listOf(
                Triple("Spektrum", "Frequenzbereich · FFT · Kurven · Glättung", "⌁"),
                Triple("Audio", "Audioquelle · Analysegain · Eingangsoptionen", "♫"),
                Triple("MON & Audio", "Mithören · Bandpass · Dynamik · Aufnahme", "♫"),
                Triple("Filterbank", "FAST-Filter · 10/20 Bänder · Bandgrenzen", "▥")
            ),
            "ANZEIGE & AUSWERTUNG" to listOf(
                Triple("Anzeige", "Wasserfall · Pegelbereich · Zeitachse", "▣"),
                Triple("Statistik", "Leq · L50/L90 · lokale SAVE-Einstellungen", "▥"),
                Triple("Automatiken", "Autorange · Bildspeicherung", "⚙")
            ),
            "SERVICE" to listOf(
                Triple("Kalibrierung", "Pegelbezug · zukünftige Profile", "◎"),
                Triple("Diagnose", "Audiozustand · Rohpegel · Fehlersuche", "⌕")
            )
        )
        Text("Analyzer-Einstellungen", color=white, fontSize=19.sp,
            fontWeight=FontWeight.Bold, modifier=Modifier.padding(bottom=4.dp))
        Text("Ein Bereich je Aufgabe · Änderungen werden wie bisher übernommen",
            color=muted, fontSize=12.sp, modifier=Modifier.padding(bottom=9.dp))
        groups.forEach { (heading, entries) ->
            Text(heading, color=cyan, fontSize=12.sp, fontWeight=FontWeight.Bold,
                modifier=Modifier.padding(top=10.dp, bottom=6.dp))
            entries.forEach { (name, subtitle, symbol) ->
                Row(Modifier.fillMaxWidth().padding(bottom=7.dp)
                    .border(1.dp,Color(0xFFACD4DF),RoundedCornerShape(12.dp))
                    .background(Color(0xFFEAF4F7),RoundedCornerShape(12.dp))
                    .clickable { onSelect(name); onNavigate() }
                    .padding(horizontal=14.dp, vertical=14.dp),
                    verticalAlignment=Alignment.CenterVertically) {
                    Text(symbol,color=Color(0xFF176D84),fontSize=24.sp,
                        modifier=Modifier.width(38.dp))
                    Column(Modifier.weight(1f)) {
                        Text(name,color=Color(0xFF16333D),fontSize=16.sp,
                            fontWeight=FontWeight.Bold)
                        Text(subtitle,color=Color(0xFF3E5A65),fontSize=12.sp)
                    }
                    Text("›",color=Color(0xFF176D84),fontSize=25.sp)
                }
            }
        }
    } else {
        Row(Modifier.fillMaxWidth().clickable { onSelect(null); onNavigate() }.padding(vertical=6.dp),
            verticalAlignment=Alignment.CenterVertically) {
            Text("‹  Alle Bereiche",color=cyan,fontSize=12.sp)
        }
        Column(Modifier.fillMaxWidth().border(1.dp,stroke,RoundedCornerShape(10.dp))
            .background(surface,RoundedCornerShape(10.dp)).padding(10.dp)) {
            Text(selected,color=amber,fontWeight=FontWeight.Bold,fontSize=17.sp)
            Spacer(Modifier.height(8.dp))
            when(selected) {
                "MON & Audio" -> {
                    SettingNote("MON-Parameter stehen direkt unterhalb dieses Panels. " +
                        "Gain und Mittenfrequenz bleiben während RUN über den Wasserfall einstellbar.")
                }
                "Filterbank" -> {
                    if(state.direct48k) {
                        SettingNote("Nur 8 kHz · NICHT VERFÜGBAR. Die folgenden Werte sind inaktiv und werden nicht angewendet.")
                        DisabledSetting("FILTER MIN Hz", "—")
                        DisabledSetting("FILTER MAX Hz", "—")
                        DisabledSetting("Anzahl Filter", "—")
                        DisabledSetting("FILTER Min dBFS", "—")
                        DisabledSetting("FILTER Max dBFS", "—")
                        DisabledSetting("FILTER-Autorange-Dauer", "—")
                        DisabledSetting("FILTER-Autorange-Reserve", "—")
                        DisabledSetting("FILTER anzeigen", "Nicht unterstützt")
                    }
                    else {
                        SettingCaption("FAST: eigene Frequenzgrenzen und frei einstellbare Filteranzahl")
                        var minimum by remember { mutableStateOf(state.filterMinHz.toInt().toString()) }
                        var maximum by remember { mutableStateOf(state.filterMaxHz.toInt().toString()) }
                        var count by remember { mutableStateOf(state.filterBandCount.toString()) }
                        var filterError by remember { mutableStateOf("") }
                        Row(horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                            OutlinedTextField(value=minimum,onValueChange={minimum=it},label={Text("FILTER MIN Hz")},
                                singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
                                textStyle=TextStyle(color=white),modifier=Modifier.weight(1f))
                            OutlinedTextField(value=maximum,onValueChange={maximum=it},label={Text("FILTER MAX Hz")},
                                singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
                                textStyle=TextStyle(color=white),modifier=Modifier.weight(1f))
                        }
                        OutlinedTextField(value=count,onValueChange={count=it},label={Text("Anzahl Filter 2–40 (Standard 10)")},
                            singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
                            textStyle=TextStyle(color=white))
                        // Analyzer is automatically stopped when this panel opens.
                        // The controller still guards against unexpected RUN races.
                        if(state.running) SettingNote("Analyzer wird für Filterkonfiguration angehalten.")
                        Button(onClick={
                            val low=minimum.toDoubleOrNull();val high=maximum.toDoubleOrNull();val n=count.toIntOrNull()
                            if(low==null||high==null||n==null || runCatching { FastFilterBank.Config(low,high,n) }.isFailure)
                                filterError="Gültig: 0 ≤ MIN < MAX ≤ 200 Hz; 2–40 Filter"
                            else { LiveAnalyzerController.setFilterConfig(context,low,high,n);onRefresh();filterError="Filterkonfiguration gespeichert" }
                        }) { Text("Filter übernehmen") }
                        if(filterError.isNotBlank()) SettingNote(filterError)
                        SettingNote("Filterbänder: ${(state.filterMaxHz-state.filterMinHz)/state.filterBandCount} Hz nominell. " +
                            "Filterreaktion und Ringing hängen von Bandbreite und Frequenz ab. Keine garantierte 5-ms-Onset-Genauigkeit.")
                        SettingNote("FILTER allein: digitale Band-RMS-Pegel (dBFS); eigene Farbskala. GEMISCHT: transienter relativer Pegelüberschuss (dB).")
                        SettingCaption("FILTER-Pegelbereich (nur Farbdarstellung): ${state.filterDisplayMinDb}…${state.filterDisplayMaxDb} dBFS")
                        var filterLowDb by remember(state.filterDisplayMinDb) { mutableStateOf(state.filterDisplayMinDb.toString()) }
                        var filterHighDb by remember(state.filterDisplayMaxDb) { mutableStateOf(state.filterDisplayMaxDb.toString()) }
                        var filterRangeError by remember { mutableStateOf("") }
                        Row(horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                            OutlinedTextField(value=filterLowDb,onValueChange={filterLowDb=it},label={Text("FILTER Min dBFS")},
                                singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
                                textStyle=TextStyle(color=white),modifier=Modifier.weight(1f))
                            OutlinedTextField(value=filterHighDb,onValueChange={filterHighDb=it},label={Text("FILTER Max dBFS")},
                                singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
                                textStyle=TextStyle(color=white),modifier=Modifier.weight(1f))
                        }
                        Button(onClick={
                            val low=filterLowDb.toIntOrNull();val high=filterHighDb.toIntOrNull()
                            if(low==null||high==null||low !in -240..40||high !in -230..50||high-low<10)
                                filterRangeError="Gültig: −240 ≤ Min < Max ≤ +50 dBFS, mindestens 10 dB Abstand"
                            else { LiveAnalyzerController.setFilterDisplayRange(context,low,high);onRefresh();filterRangeError="FILTER-Skala gespeichert" }
                        }) { Text("FILTER-Pegelgrenzen übernehmen") }
                        if(filterRangeError.isNotBlank()) SettingNote(filterRangeError)
                        SettingCaption("FILTER-Autorange: Dauer ${state.filterAutoRangeDurationSec} s")
                        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                            listOf(1,2,3,5,10).forEach { sec -> SettingChoice("$sec s",sec==state.filterAutoRangeDurationSec) {
                                LiveAnalyzerController.setFilterAutoRangeSettings(context,sec,state.filterAutoRangeMarginDb);onRefresh()
                            } }
                        }
                        SettingCaption("FILTER-Autorange: Reserve ±${state.filterAutoRangeMarginDb} dB")
                        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                            listOf(0,3,5,10,15).forEach { margin -> SettingChoice("±$margin",margin==state.filterAutoRangeMarginDb) {
                                LiveAnalyzerController.setFilterAutoRangeSettings(context,state.filterAutoRangeDurationSec,margin);onRefresh()
                            } }
                        }
                        SettingNote("FILTER-Button lange drücken: Pegelautomatik starten (Live erforderlich). Kurzer Tipp: Wasserfall EIN/AUS. ORIGINAL-Skala bleibt unabhängig.")
                    }
                }
                "Spektrum" -> {
                    SettingCaption("Frequenzbereich Min/Max (Hz, 0–${if(state.direct48k) state.sampleRateHz/2 else 200})")
                    var minFreqText by remember { mutableStateOf(state.minFrequencyHz.toString()) }
                    var maxFreqText by remember { mutableStateOf(state.rangeHz.toString()) }
                    var frequencyEditing by remember { mutableStateOf(false) }
                    var frequencyError by remember { mutableStateOf("") }
                    LaunchedEffect(state.minFrequencyHz,state.rangeHz,frequencyEditing) {
                        if (!frequencyEditing) {
                            minFreqText=state.minFrequencyHz.toString(); maxFreqText=state.rangeHz.toString()
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value=minFreqText,onValueChange={minFreqText=it;frequencyError=""},
                            label={Text("Min Hz")},singleLine=true,
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
                            textStyle=TextStyle(color=white),modifier=Modifier.weight(1f)
                                .onFocusChanged { frequencyEditing=it.isFocused })
                        OutlinedTextField(value=maxFreqText,onValueChange={maxFreqText=it;frequencyError=""},
                            label={Text("Max Hz")},singleLine=true,
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
                            textStyle=TextStyle(color=white),modifier=Modifier.weight(1f)
                                .onFocusChanged { frequencyEditing=it.isFocused })
                    }
                    Button(onClick={
                        val minHz=minFreqText.toIntOrNull(); val maxHz=maxFreqText.toIntOrNull()
                        if (minHz==null || maxHz==null || !(if(state.direct48k) AnalyzerTiming.validWidebandRange(minHz,maxHz) && maxHz<=state.sampleRateHz/2 else AnalyzerTiming.validRange(minHz,maxHz)))
                            frequencyError="Ungültig: 0 ≤ Min < Max ≤ ${if(state.direct48k) state.sampleRateHz/2 else 200} Hz, Abstand mindestens 5 Hz"
                        else {
                            LiveAnalyzerController.setFrequencyRange(context,minHz,maxHz)
                            onRefresh();frequencyError="Gespeichert"
                        }
                    }) { Text("Frequenzgrenzen übernehmen",fontSize=11.sp) }
                    if (frequencyError.isNotEmpty()) SettingNote(frequencyError)
                    SettingCaption("Schnellwahl: 0–X Hz")
                    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                        (if(state.direct48k) intArrayOf(200,1000,5000,10000,22000,48000).filter { it<=state.sampleRateHz/2 } else LiveAnalyzerController.allowedMaxHz.toList()).forEach { hz ->
                            SettingChoice("$hz Hz",hz==state.rangeHz) {
                                LiveAnalyzerController.setRange(context,hz);onRefresh()
                            }
                        }
                    }
                    SettingCaption(if(state.direct48k) "Direkt: Decimation ${state.decimationFactor} · effektiv ${state.sampleRateHz} Hz" else
                        "Decimation: ${state.decimationFactor}:1 · LF ${state.sampleRateHz} Hz")
                    if(state.direct48k) {
                        if (state.running) SettingNote("STOP erforderlich · Decimation kann während RUN nicht geändert werden.")
                        else Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                            (0..12).forEach { factor ->
                                if (DirectAnalyzerConfig.valid(state.directInputRateHz,factor)) {
                                    SettingChoice(if(factor==0) "0 AUS" else "$factor:1",factor==state.decimationFactor) {
                                        LiveAnalyzerController.setDirectDecimation(context,factor);onRefresh()
                                    }
                                }
                            }
                        }
                    }
                    if(!state.direct48k) Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                        AnalyzerTiming.allowedDecimationFactors.forEach { factor ->
                            SettingChoice("$factor:1 (${8000/factor} Hz)",factor==state.decimationFactor) {
                                val newInterval=AnalyzerTiming.effectiveWaterfallIntervalMs(
                                    state.fftHopSamples,state.waterfallIntervalMs,8000/factor)
                                if(AnalyzerTiming.validHistory(state.historySeconds,newInterval)) {
                                    LiveAnalyzerController.setDecimation(context,factor);onRefresh()
                                } else frequencyError="Wasserfall-Dauer und Zeilenintervall für Decimation $factor:1 zu groß"
                            }
                        }
                    }
                    SettingNote("FIR-Anti-Aliasing nur im Analyzer. Gemeinsamer 8-kHz-PCM-/WAV-Pfad bleibt unverändert.")
                    SettingCaption("FFT Size: ${state.fftSize} · Δf ${String.format(Locale.US,"%.3f",state.sampleRateHz.toDouble()/state.fftSize)} Hz")
                    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                        (if(state.direct48k) DirectAnalyzerConfig.fftSizes else AnalyzerTiming.allowedFftSizes).forEach { size ->
                            SettingChoice("$size",size==state.fftSize) {
                                LiveAnalyzerController.setDsp(context,size,state.windowType,
                                    state.smoothingEnabled,state.smoothingTimeSeconds);onRefresh()
                            }
                        }
                    }
                    SettingCaption("FFT-Hop: ${state.fftHopSamples} Samples · Transformation alle ${AnalyzerTiming.frameIntervalMs(state.fftHopSamples,state.sampleRateHz)} ms")
                    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                        (if(state.direct48k) DirectAnalyzerConfig.transformIntervalsMs.map { DirectAnalyzerConfig.hop(state.directInputRateHz,state.decimationFactor,it) }.distinct().toIntArray() else AnalyzerTiming.allowedFftHops).forEach { hop ->
                            SettingChoice("$hop",hop==state.fftHopSamples) {
                                val effective=AnalyzerTiming.effectiveWaterfallIntervalMs(hop,state.waterfallIntervalMs,state.sampleRateHz)
                                if(AnalyzerTiming.validHistory(state.historySeconds,effective)) {
                                    LiveAnalyzerController.setTiming(context,hop,state.waterfallIntervalMs);onRefresh()
                                } else frequencyError="Wasserfall-Dauer und Zeilenintervall für Hop $hop zu groß"
                            }
                        }
                    }
                    SettingCaption("Desired Transformation Interval (ms) – LF-Rate ${state.sampleRateHz} Hz")
                    var transformText by remember(state.fftHopSamples,state.sampleRateHz) {
                        mutableStateOf(String.format(Locale.US,"%.2f",state.fftHopSamples*1000.0/state.sampleRateHz)) }
                    var transformError by remember { mutableStateOf("") }
                    OutlinedTextField(value=transformText,onValueChange={transformText=it;transformError=""},
                        label={Text("Gewünschtes Intervall in ms")},singleLine=true,
                        keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
                        textStyle=TextStyle(color=white))
                    OutlinedButton(onClick={
                        val requested=transformText.replace(',','.').toDoubleOrNull()
                        if (requested == null || !requested.isFinite() || requested<=0.0)
                            transformError="Positives Intervall in ms eingeben"
                        else {
                            val nearest=(if(state.direct48k) DirectAnalyzerConfig.transformIntervalsMs.map { DirectAnalyzerConfig.hop(state.directInputRateHz,state.decimationFactor,it) }.toIntArray() else AnalyzerTiming.allowedFftHops).minByOrNull {
                                kotlin.math.abs(it*1000.0/state.sampleRateHz-requested) }!!
                            val effective=AnalyzerTiming.effectiveWaterfallIntervalMs(nearest,state.waterfallIntervalMs,state.sampleRateHz)
                            if(!AnalyzerTiming.validHistory(state.historySeconds,effective))
                                transformError="Wasserfall-Dauer und Zeilenintervall zu groß"
                            else {
                                LiveAnalyzerController.setTiming(context,nearest,state.waterfallIntervalMs)
                                onRefresh()
                                transformError="Realisierbar: $nearest Samples = ${String.format(Locale.US,"%.2f",nearest*1000.0/state.sampleRateHz)} ms"
                            }
                        }
                    }) {Text("Intervall übernehmen",color=white,fontSize=11.sp)}
                    if(transformError.isNotBlank()) SettingNote(transformError)
                    if (state.direct48k) SettingNote("Direkt-Intervalle: 10/15/20/25/30/50/100 ms. Bei kleinen FFTs darf Hop > FFT Size sein.")
                    SettingCaption("Window-Funktion")
                    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                        AnalyzerWindow.values().forEach { window ->
                            SettingChoice(window.name,window==state.windowType) {
                                LiveAnalyzerController.setDsp(context,state.fftSize,window,
                                    state.smoothingEnabled,state.smoothingTimeSeconds);onRefresh()
                            }
                        }
                    }
                    if (state.direct48k) {
                        SettingToggle("Exponential Smoothing",state.smoothingEnabled) {
                            LiveAnalyzerController.setDsp(context,state.fftSize,state.windowType,it,state.smoothingTimeSeconds)
                            onRefresh()
                        }
                        SettingCaption("Exponential Smoothing α: ${String.format(Locale.US,"%.2f",state.directSmoothingAlpha)}")
                        var directAlpha by remember(state.directSmoothingAlpha) { mutableStateOf(state.directSmoothingAlpha.toFloat()) }
                        Slider(value=directAlpha,onValueChange={directAlpha=it},onValueChangeFinished={
                            LiveAnalyzerController.setDirectOptions(context,
                                (kotlin.math.round(directAlpha*100)/100.0),state.directPeakMarkers,
                                state.directSubtractDc,state.directStayAwake);onRefresh()
                        },valueRange=0f..1f,steps=99)
                        SettingNote("α=0: letzten Mittelwert halten, α=1: keine Mittelung.")
                        SettingCaption("Peak Markers")
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                            (0..2).forEach { count ->
                                SettingChoice("$count",count==state.directPeakMarkers) {
                                    LiveAnalyzerController.setDirectOptions(context,state.directSmoothingAlpha,
                                        count,state.directSubtractDc,state.directStayAwake);onRefresh()
                                }
                            }
                        }
                    } else {
                    DisabledSetting("Peak Markers", "Nicht unterstützt")
                    SettingToggle("Exponential Smoothing",state.smoothingEnabled) {
                        LiveAnalyzerController.setDsp(context,state.fftSize,state.windowType,it,state.smoothingTimeSeconds)
                        onRefresh()
                    }
                    val alpha = if (!state.smoothingEnabled) 1.0 else
                        1.0-exp(-state.fftHopSamples/state.sampleRateHz.toDouble()/state.smoothingTimeSeconds)
                    SettingCaption("Zeitkonstante: ${String.format(Locale.US,"%.3f",state.smoothingTimeSeconds)} s · Faktor α: ${String.format(Locale.US,"%.5f",alpha)}")
                    var tauText by remember(state.smoothingTimeSeconds) {
                        mutableStateOf(String.format(Locale.US,"%.3f",state.smoothingTimeSeconds)) }
                    var alphaText by remember(alpha) { mutableStateOf(String.format(Locale.US,"%.5f",alpha)) }
                    var smoothingError by remember { mutableStateOf("") }
                    OutlinedTextField(value=tauText,onValueChange={tauText=it;smoothingError=""},
                        label={Text("Zeitkonstante τ in s (0,01…60)")},singleLine=true,
                        keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
                        textStyle=TextStyle(color=white))
                    OutlinedButton(onClick={
                        val tau=tauText.replace(',','.').toDoubleOrNull()
                        if(tau==null || !tau.isFinite() || tau !in 0.01..60.0)
                            smoothingError="Zeitkonstante zwischen 0,01 und 60 s eingeben"
                        else {LiveAnalyzerController.setDsp(context,state.fftSize,state.windowType,state.smoothingEnabled,tau)
                            onRefresh();smoothingError="Übernommen"}
                    }) { Text("Zeitkonstante übernehmen",color=white,fontSize=11.sp) }
                    OutlinedTextField(value=alphaText,onValueChange={alphaText=it;smoothingError=""},
                        label={Text("Alternativ: Faktor α (0…1)")},singleLine=true,
                        keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
                        textStyle=TextStyle(color=white))
                    OutlinedButton(onClick={
                        val a=alphaText.replace(',','.').toDoubleOrNull()
                        if(a==null || !a.isFinite() || a<=0.0 || a>1.0)
                            smoothingError="Faktor α muss größer 0 und höchstens 1 sein"
                        else if(a==1.0) {
                            LiveAnalyzerController.setDsp(context,state.fftSize,state.windowType,false,state.smoothingTimeSeconds)
                            onRefresh();smoothingError="α = 1: Glättung AUS"
                        } else {
                            val time = -state.fftHopSamples/state.sampleRateHz.toDouble()/ln(1-a)
                            if(!time.isFinite() || time !in 0.01..60.0)
                                smoothingError="Faktor entspricht keiner zulässigen Zeitkonstante (0,01…60 s)"
                            else {LiveAnalyzerController.setDsp(context,state.fftSize,state.windowType,true,time)
                                onRefresh();smoothingError="Faktor übernommen"}
                        }
                    }) {Text("Faktor übernehmen",color=white,fontSize=11.sp)}
                    if(smoothingError.isNotBlank()) SettingNote(smoothingError)
                    }
                    if(state.direct48k) {
                        SettingToggle("Direkt: Glättungszeit in Sekunden statt α",state.directTimeSmoothing) {
                            LiveAnalyzerController.setDirectTimeSmoothing(context,it);onRefresh()
                        }
                        SettingNote(if(state.directTimeSmoothing)
                            "Zeitkonstante ${state.smoothingTimeSeconds} s; α wird aus dem FFT-Hop berechnet (5 s möglich)."
                            else "Bisheriger Direkt-α aktiv; Glättungszeit nur bei aktiviertem Sekundenmodus wirksam.")
                        // Same stored time constant as 8K, editable in DIRECT only when its time mode is active.
                        var directTau by remember(state.smoothingTimeSeconds) {
                            mutableStateOf(String.format(Locale.US,"%.3f",state.smoothingTimeSeconds))
                        }
                        var directTauError by remember { mutableStateOf("") }
                        OutlinedTextField(value=directTau,onValueChange={directTau=it;directTauError=""},
                            enabled=state.directTimeSmoothing,
                            label={Text("Zeitkonstante τ in s (0,01…60)")},singleLine=true,
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),
                            textStyle=TextStyle(color=white))
                        OutlinedButton(enabled=state.directTimeSmoothing,onClick={
                            val tau=directTau.replace(',','.').toDoubleOrNull()
                            if(tau==null || !tau.isFinite() || tau !in 0.01..60.0)
                                directTauError="Zeitkonstante zwischen 0,01 und 60 s eingeben"
                            else {
                                LiveAnalyzerController.setDsp(context,state.fftSize,state.windowType,state.smoothingEnabled,tau)
                                onRefresh();directTauError="Übernommen (FFT-Warmup)"
                            }
                        }) { Text("Zeitkonstante übernehmen",color=white,fontSize=11.sp) }
                        if(directTauError.isNotBlank()) SettingNote(directTauError)
                    }
                    SettingNote(if(state.direct48k) "Direkt ${state.directInputRateHz} Hz → ${state.sampleRateHz} Hz; kein Rolling. Intervall = Hop / Analyse-Rate." else
                        "Quelle 8 kHz → FIR/Decimation ${state.decimationFactor}:1 → ${state.sampleRateHz} Hz. Intervall = Hop / LF-Abtastrate.")
                    SettingToggle("Aktuell (gelbe Kurve)",state.showCurrent) {
                        LiveAnalyzerController.setSpectrumVisibility(context,it,state.showAverage,state.showMaxHold);onRefresh()
                    }
                    SettingToggle("Rote Kurve / Average (${if(state.direct48k) String.format(Locale.US,"α %.2f",state.directSmoothingAlpha) else if(state.smoothingEnabled) String.format(Locale.US,"%.2f s",state.smoothingTimeSeconds) else "ungeglättet"})",state.showAverage) {
                        LiveAnalyzerController.setTraceVisibility(context,it,state.showFloor);onRefresh()
                    }
                    SettingCaption("Elektrische Rauschboden-Einmessung (Average)")
                    SettingNote("Der NOISE-Button: kurz = AUS / MESSPROFIL / 1/f-MODELL, lang = Power-Average einmessen. Nur mit geeignet abgeschlossenem Eingang messen; ein mit Nutzsignal eingemessenes Profil kann dieses nicht vom Rauschen unterscheiden.")
                    var captureSeconds by remember { mutableStateOf(NoiseFloorReference.calibrationSeconds()) }
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                        listOf(10,30,60,120,300,600).forEach { seconds ->
                            SettingChoice("${seconds} s", captureSeconds == seconds) {
                                if (NoiseFloorReference.setCalibrationSeconds(seconds)) captureSeconds = seconds
                            }
                        }
                    }
                    SettingToggle("Grauer Rauschboden",state.showFloor) {
                        LiveAnalyzerController.setTraceVisibility(context,state.showAverage,it);onRefresh()
                    }
                    SettingToggle("Maxhold (türkise Kurve)",state.showMaxHold) {
                        LiveAnalyzerController.setSpectrumVisibility(context,state.showCurrent,state.showAverage,it);onRefresh()
                    }
                    SettingNote("MAX / CLR erscheint bei aktivem Maxhold unten neben der Kamera. Maxhold zurücksetzen verändert weder Rolling noch die Wasserfallhistorie.")
                }
                "Audio" -> {
                    if (state.direct48k) {
                        SettingCaption("Audio Source · STOP für Änderung")
                        val selectedSource=prefs.getString("direct_audio_source","AUTO") ?: "AUTO"
                        if (state.running) SettingNote("Audioquelle/Sampling Rate ändern: zuerst STOP, dann START.")
                        else Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                            listOf("AUTO","UNPROCESSED","MIC","VOICE_RECOGNITION").forEach { source ->
                                SettingChoice(source,source==selectedSource) {
                                    LiveAnalyzerController.setDirectCapture(context,state.directInputRateHz,source);onRefresh()
                                }
                            }
                        }
                        SettingCaption("Sampling Rate: ${state.directInputRateHz} Hz · STOP für Änderung")
                        if (!state.running) Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                            DirectAnalyzerConfig.inputRates.forEach { rate ->
                                SettingChoice("$rate",rate==state.directInputRateHz) {
                                    LiveAnalyzerController.setDirectCapture(context,rate,selectedSource);onRefresh()
                                }
                            }
                        }
                        SettingNote("Geräteabhängig: nicht initialisierbare Audioquelle/Rate wird beim START als Fehler gemeldet. Kein automatisches falsches Resampling.")
                        SettingToggle("Stay Awake (Bildschirm bei Messung an)",state.directStayAwake) {
                            LiveAnalyzerController.setDirectOptions(context,state.directSmoothingAlpha,
                                state.directPeakMarkers,state.directSubtractDc,it);onRefresh()
                        }
                        SettingToggle("Subtract DC",state.directSubtractDc) {
                            LiveAnalyzerController.setDirectOptions(context,state.directSmoothingAlpha,
                                state.directPeakMarkers,it,state.directStayAwake);onRefresh()
                        }
                        val directTestPrefs=remember(context){context.getSharedPreferences("lfn_scout_live_analyzer",Context.MODE_PRIVATE)}
                        var tone1 by remember { mutableStateOf(directTestPrefs.getFloat("mode_48_direct_test1_hz",50f).toString()) }
                        var tone2 by remember { mutableStateOf(directTestPrefs.getFloat("mode_48_direct_test2_hz",195f).toString()) }
                        var on1 by remember { mutableStateOf(directTestPrefs.getBoolean("mode_48_direct_test1_on",true)) }
                        var on2 by remember { mutableStateOf(directTestPrefs.getBoolean("mode_48_direct_test2_on",false)) }
                        var toneError by remember { mutableStateOf("") }
                        SettingCaption("Testsignal 1 / 2 · ausschließlich digitale TEST-Betriebsart")
                        SettingToggle("Testsignal 1 EIN",on1) { on1=it }
                        OutlinedTextField(value=tone1,onValueChange={tone1=it;toneError=""},label={Text("Testsignal 1 Hz")},singleLine=true,
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),textStyle=TextStyle(color=white))
                        SettingToggle("Testsignal 2 EIN",on2) { on2=it }
                        OutlinedTextField(value=tone2,onValueChange={tone2=it;toneError=""},label={Text("Testsignal 2 Hz")},singleLine=true,
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),textStyle=TextStyle(color=white))
                        OutlinedButton(onClick={
                            val a=tone1.replace(',','.').toFloatOrNull();val b=tone2.replace(',','.').toFloatOrNull()
                            val nyquist=state.directInputRateHz/2f
                            if(a==null || b==null || !a.isFinite() || !b.isFinite() ||
                                a<1f || b<1f || a>=nyquist || b>=nyquist)
                                toneError="Beide Frequenzen: 1 Hz bis kleiner als $nyquist Hz"
                            else if (state.testSignal && !on1 && !on2) toneError="Bei laufendem TESTSIGNAL mindestens einen Ton aktivieren"
                            else {
                                directTestPrefs.edit().putFloat("direct_test1_hz",a).putFloat("direct_test2_hz",b)
                                    .putBoolean("direct_test1_on",on1).putBoolean("direct_test2_on",on2)
                                    .putFloat("mode_48_direct_test1_hz",a).putFloat("mode_48_direct_test2_hz",b)
                                    .putBoolean("mode_48_direct_test1_on",on1).putBoolean("mode_48_direct_test2_on",on2).commit()
                                val ok=if (state.testSignal && AnalyzerTestSignal.isRunning)
                                    AnalyzerTestSignal.start(context,state.directInputRateHz,true) else true
                                toneError=if(ok) "Testsignale gespeichert" else "Testsignalstart fehlgeschlagen"
                                onRefresh()
                            }
                        }) { Text("Testsignale übernehmen",color=white) }
                        if(toneError.isNotBlank()) SettingNote(toneError)
                    } else {
                        DisabledSetting("Audio Source", "Gemeinsame 8-kHz-Quelle (fest)")
                        DisabledSetting("Sampling Rate", "8000 Hz (fest)")
                        DisabledSetting("Stay Awake", "Im 8-kHz-Panel nicht getrennt verfügbar")
                        DisabledSetting("Subtract DC", "Kein separater Schalter")
                        if (AnalyzerModeCoordinator.mode != AnalyzerModeCoordinator.Mode.TEST_8K) {
                            DisabledSetting("Testsignal 1 / 2", "Nur im 8-kHz-TEST-Modus")
                        }
                    }
                    SettingCaption("Analysegain: +${state.analysisGainDb} dB (unabhängig vom Recorder)")
                    var gain by remember(state.analysisGainDb) { mutableStateOf(state.analysisGainDb.toFloat()) }
                    Slider(value=gain,onValueChange={gain=it},onValueChangeFinished={
                        LiveAnalyzerController.setAnalysisGain(context,gain.toInt().coerceIn(0,50));onRefresh()
                    },valueRange=0f..50f,steps=49)
                    SettingNote("Rohquelle: ${state.diagnostics?.sourceLabel ?: "–"}; ${state.diagnostics?.sourceRateHz ?: 0} Hz. " +
                        if(state.direct48k) "Exklusiver ${state.directInputRateHz}-Hz-Eingang, Rolling ausgeschaltet." else "Gemeinsamer 8-kHz-Eingang.")
                    if(AnalyzerModeCoordinator.mode == AnalyzerModeCoordinator.Mode.TEST_8K) {
                        val testPrefs=remember(context){context.getSharedPreferences("analyzer_test_signal",Context.MODE_PRIVATE)}
                        var f1 by remember { mutableStateOf(testPrefs.getFloat("f1",50f).toString()) }
                        var f2 by remember { mutableStateOf(testPrefs.getFloat("f2",195f).toString()) }
                        var db by remember { mutableStateOf(testPrefs.getInt("level_dbfs",-24).toString()) }
                        var dual by remember { mutableStateOf(testPrefs.getBoolean("dual",false)) }
                        var testError by remember { mutableStateOf("") }
                        SettingCaption("Digitaler Testgenerator · nur Analyzer · KEINE MESSUNG")
                        OutlinedTextField(value=f1,onValueChange={f1=it;testError=""},label={Text("Frequenz 1 (Hz), z. B. 50")},singleLine=true,
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),textStyle=TextStyle(color=white))
                        SettingToggle("Zweiten Sinus zuschalten",dual){dual=it}
                        if(dual) OutlinedTextField(value=f2,onValueChange={f2=it;testError=""},label={Text("Frequenz 2 (Hz), z. B. 195")},singleLine=true,
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),textStyle=TextStyle(color=white))
                        OutlinedTextField(value=db,onValueChange={db=it;testError=""},label={Text("Digitaler Spitzenpegel −100…−1 dBFS")},singleLine=true,
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Text),textStyle=TextStyle(color=white))
                        Button(onClick={
                            val a=f1.replace(',','.').toFloatOrNull()
                            val b=f2.replace(',','.').toFloatOrNull()
                            val level=db.toIntOrNull()
                            if(a==null || b==null || level==null || a<=0f || a>22000f || a>=state.sampleRateHz/2f ||
                                (dual && (b<=0f || b>22000f || b>=state.sampleRateHz/2f)) || level !in -100..-1)
                                testError="Frequenzen müssen positiv und unter Nyquist liegen; Pegel −100…−1 dBFS"
                            else {
                                testPrefs.edit().putFloat("f1",a).putFloat("f2",b).putInt("level_dbfs",level).putBoolean("dual",dual).apply()
                                if(AnalyzerTestSignal.isRunning) AnalyzerTestSignal.start(context,state.sampleRateHz)
                                testError="Testsignal gespeichert / angewendet"
                            }
                        }) {Text("Testsignal übernehmen")}
                        if(testError.isNotBlank()) SettingNote(testError)
                    }
                    SettingNote("Mithören und Clip-Regelung bleiben zurückgestellt. Testsignale gelangen NIE in Rolling oder WAV.")
                }
                "Anzeige" -> {
                    SettingCaption("dBFS-Grenzen – auch manuell einstellbar")
                    var minText by remember { mutableStateOf(state.minDisplayDb.toString()) }
                    var maxText by remember { mutableStateOf(state.maxDisplayDb.toString()) }
                    var editingMin by remember { mutableStateOf(false) }
                    var editingMax by remember { mutableStateOf(false) }
                    var rangeError by remember { mutableStateOf("") }
                    // Accept finished Autorange/presets on both fields, without erasing typing in a focused field.
                    LaunchedEffect(state.minDisplayDb, editingMin) {
                        if (!editingMin) minText = state.minDisplayDb.toString()
                    }
                    LaunchedEffect(state.maxDisplayDb, editingMax) {
                        if (!editingMax) maxText = state.maxDisplayDb.toString()
                    }
                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor=white, unfocusedTextColor=white,
                        cursorColor=cyan,
                        focusedLabelColor=cyan, unfocusedLabelColor=muted,
                        focusedBorderColor=cyan, unfocusedBorderColor=stroke,
                        focusedContainerColor=Color(0xFF071319), unfocusedContainerColor=Color(0xFF071319))
                    CompositionLocalProvider(LocalTextSelectionColors provides TextSelectionColors(
                        handleColor=cyan, backgroundColor=cyan.copy(alpha=0.35f))) {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value=minText,onValueChange={minText=it;rangeError=""},
                            label={Text("Minimum dBFS")},singleLine=true,
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Text),
                            textStyle=TextStyle(color=white,fontSize=16.sp),colors=fieldColors,
                            modifier=Modifier.weight(1f).onFocusChanged { editingMin=it.isFocused })
                        OutlinedTextField(value=maxText,onValueChange={maxText=it;rangeError=""},
                            label={Text("Maximum dBFS")},singleLine=true,
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Text),
                            textStyle=TextStyle(color=white,fontSize=16.sp),colors=fieldColors,
                            modifier=Modifier.weight(1f).onFocusChanged { editingMax=it.isFocused })
                    }
                    }
                    Button(onClick={
                        val low=minText.trim().toIntOrNull();val high=maxText.trim().toIntOrNull()
                        if (low==null || high==null || low !in -240..40 || high !in -230..50 || high-low<10) {
                            rangeError="Ungültig: Minimum −240…40; Maximum −230…50; Abstand mindestens 10 dB"
                        } else {
                            LiveAnalyzerController.setDisplayRange(context,low,high);onRefresh();rangeError="Gespeichert"
                        }
                    },colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF166779))) {
                        Text("Grenzen übernehmen und speichern",fontSize=11.sp)
                    }
                    if(rangeError.isNotBlank())Text(rangeError,color=if(rangeError=="Gespeichert")cyan else Color(0xFFFFA9A0),fontSize=10.sp)
                    SettingCaption("Schnell-Presets")
                    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                        listOf(-60,-40,0).forEach { high ->
                            SettingChoice("−120…$high",state.minDisplayDb == -120 && state.maxDisplayDb == high) {
                                LiveAnalyzerController.setDisplayRange(context,-120,high);onRefresh()
                            }
                        }
                    }
                    SettingNote("Autorange per Tipp auf die linke Spektrum-dBFS-Achse; manuelle Grenzen und Autorange werden für den nächsten Start gespeichert.")
                    SettingCaption("Wasserfall-Zeitdauer (Sekunden, freie Eingabe)")
                    var historyText by remember(state.historySeconds) { mutableStateOf(state.historySeconds.toString()) }
                    var historyError by remember { mutableStateOf("") }
                    OutlinedTextField(value=historyText,onValueChange={historyText=it;historyError=""},
                        label={Text("Dauer in Sekunden")},singleLine=true,
                        keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
                        textStyle=TextStyle(color=white))
                    OutlinedButton(onClick={
                        val seconds=historyText.toIntOrNull()
                        if (seconds == null || seconds !in 10..AnalyzerTiming.MAX_HISTORY_SECONDS ||
                            AnalyzerTiming.intervalForDuration(seconds,state.waterfallIntervalMs,state.fftHopSamples,state.sampleRateHz)==null)
                            historyError="10…${AnalyzerTiming.MAX_HISTORY_SECONDS} s; maximal ${AnalyzerTiming.MAX_HISTORY_ROWS} Zeilen"
                        else { LiveAnalyzerController.setHistorySeconds(context,seconds);onRefresh();historyError="Gespeichert" }
                    }) {Text("Wasserfall-Zeit übernehmen",color=white,fontSize=11.sp)}
                    if(historyError.isNotBlank()) SettingNote(historyError)
                    SettingCaption("Wasserfall-Zeile alle ${state.waterfallIntervalMs} ms")
                    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                        AnalyzerTiming.allowedWaterfallIntervalsMs.forEach { interval ->
                            SettingChoice("$interval",state.waterfallIntervalMs==interval) {
                                if (AnalyzerTiming.validHistory(state.historySeconds,
                                        AnalyzerTiming.effectiveWaterfallIntervalMs(state.fftHopSamples,interval,state.sampleRateHz))) {
                                    LiveAnalyzerController.setTiming(context,state.fftHopSamples,interval);onRefresh()
                                } else historyError="Bei ${state.historySeconds} s sind höchstens ${AnalyzerTiming.MAX_HISTORY_ROWS} Zeilen zulässig"
                            }
                        }
                    }
                    SettingCaption("Wasserfall-Auswertung: ${state.waterfallMode.label}")
                    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                        WaterfallMode.values().forEach { mode ->
                            SettingChoice(mode.label,mode==state.waterfallMode) {
                                LiveAnalyzerController.setWaterfallMode(context,mode);onRefresh()
                            }
                        }
                    }
                    SettingNote("${state.historySeconds} s, neueste Zeile oben. Akt/MW/MAX-Wechsel lässt alte Zeilen stehen; Umschaltgrenze wird markiert.")
                }
                "Statistik" -> {
                    OutlinedButton(onClick=onOpenLocalSaveSettings, modifier=Modifier.fillMaxWidth()) {
                        Text("SAVE-Setup · Audio / lokale Statistik", color=white, fontSize=11.sp)
                    }
                    SettingNote("Speicherdauer und Frequenzbereich der lokalen Audio-/Statistik-Saves einstellen.")
                    Spacer(Modifier.height(6.dp))
                    SettingCaption("Statistikbereich ${state.statisticsMinHz}–${state.statisticsMaxHz} Hz")
                    var statMinText by remember(state.statisticsMinHz) { mutableStateOf(state.statisticsMinHz.toString()) }
                    var statMaxText by remember(state.statisticsMaxHz) { mutableStateOf(state.statisticsMaxHz.toString()) }
                    var statWindowText by remember(state.statisticsWindowSec) { mutableStateOf(state.statisticsWindowSec.toString()) }
                    var statAverageText by remember(state.statisticsAverageSec) { mutableStateOf(state.statisticsAverageSec.toString()) }
                    var statError by remember { mutableStateOf("") }
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                        OutlinedTextField(value=statMinText,onValueChange={statMinText=it;statError=""},
                            label={Text("Min Hz")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
                            textStyle=TextStyle(color=white),modifier=Modifier.weight(1f))
                        OutlinedTextField(value=statMaxText,onValueChange={statMaxText=it;statError=""},
                            label={Text("Max Hz")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
                            textStyle=TextStyle(color=white),modifier=Modifier.weight(1f))
                    }
                    OutlinedTextField(value=statWindowText,onValueChange={statWindowText=it;statError=""},
                        label={Text("Statistik-Zeitfenster (s)")},singleLine=true,
                        keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),textStyle=TextStyle(color=white))
                    OutlinedTextField(value=statAverageText,onValueChange={statAverageText=it;statError=""},
                        label={Text("Statistik Average Time (s)")},singleLine=true,
                        keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),textStyle=TextStyle(color=white))
                    Button(onClick={
                        val lo=statMinText.toIntOrNull();val hi=statMaxText.toIntOrNull()
                        val window=statWindowText.toIntOrNull()
                        val average=statAverageText.replace(',','.').toDoubleOrNull()
                        if(lo==null||hi==null||window==null||average==null||
                            !AnalyzerTiming.validStatistics(lo,hi,window,average))
                            statError="0 ≤ Min < Max ≤ 200 Hz; Fenster 10–600 s; Average 0,1–60 s und ≤ Fenster"
                        else {
                            LiveAnalyzerController.setStatistics(context,lo,hi,window,average)
                            onRefresh();statError="Statistik neu gestartet und gespeichert"
                        }
                    }) {Text("Statistik übernehmen",fontSize=11.sp)}
                    if(statError.isNotBlank()) SettingNote(statError)
                    val statsRange = "${state.statisticsMinHz}-${state.statisticsMaxHz}"
                    SettingCaption("Leq($statsRange): ${state.leqDbFs?.let { String.format(Locale.US,"%.1f",it) } ?: "–"} dBFS")
                    SettingCaption("L50($statsRange): ${state.l50DbFs?.let { String.format(Locale.US,"%.1f",it) } ?: "–"} dBFS")
                    SettingCaption("L90($statsRange): ${state.l90DbFs?.let { String.format(Locale.US,"%.1f",it) } ?: "–"} dBFS")
                    SettingNote("Fenster ${state.statisticsWindowSec} s · gleitendes Leistungsmittel ${state.statisticsAverageSec} s. Unabhängig von Spektrum-Glättung und Wasserfallzeit; Statistikband im Spektrum markiert.")
                    OutlinedButton(onClick={LiveAnalyzerController.resetBandStatisticsAndAverage();onRefresh()}) {
                        Text("Leq / L50 / L90 und Spektrummittelung neu starten",color=white,fontSize=11.sp)
                    }
                    SettingNote("Unkalibrierte dBFS-Werte, keine dB-SPL-Kalibrierung.")
                }
                "Automatiken" -> {
                    SettingCaption("Autorange · Dauer ${state.autoRangeDurationSec} s")
                    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                        listOf(1,2,3,5,10).forEach { sec ->
                            SettingChoice("$sec s",sec==state.autoRangeDurationSec) {
                                LiveAnalyzerController.setAutoRangeSettings(context,sec,state.autoRangeMarginDb);onRefresh()
                            }
                        }
                    }
                    SettingCaption("Feste Reserve: ±${state.autoRangeMarginDb} dB")
                    Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                        listOf(0,3,5,10,15).forEach { margin ->
                            SettingChoice("±$margin",margin==state.autoRangeMarginDb) {
                                LiveAnalyzerController.setAutoRangeSettings(context,state.autoRangeDurationSec,margin);onRefresh()
                            }
                        }
                    }
                    SettingCaption("Bildformat (manueller Langdruck und Automatik)")
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                        AnalyzerImageFormat.values().forEach { format ->
                            SettingChoice(format.name,format==state.imageFormat) {
                                LiveAnalyzerController.setImageFormat(context,format);onRefresh()
                            }
                        }
                    }
                    if (state.imageFormat == AnalyzerImageFormat.JPG) {
                        SettingCaption("JPG-Qualität: ${state.jpgQuality} %")
                        Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                            listOf(70,80,90,100).forEach { quality ->
                                SettingChoice("$quality %",quality==state.jpgQuality) {
                                    LiveAnalyzerController.setJpgQuality(context,quality);onRefresh()
                                }
                            }
                        }
                    }
                    SettingToggle("Auto-Bild: erste volle ${state.historySeconds}-s-Historie, danach je ${state.historySeconds} s",state.autoSave) {
                        LiveAnalyzerController.setAutoSave(context,it);onRefresh()
                    }
                    SettingNote("Speicherort Android 10+: ${PublicDataDirectory.relativePath} · Dateiendung .png oder .jpg. Beim langen Druck wird unabhängig von der Automatik ein Einzelbild gespeichert.")
                    SettingNote("Auslöser für Ereignis-PNG und CSV-Export: geplant, derzeit ohne Schalter.")
                }
                "Kalibrierung" -> {
                    SettingNote("Der aktuelle Pegel ist dBFS (unkalibriert), nicht dB SPL. Analyse-Gain ist keine absolute Kalibrierung.")
                    SettingNote("Mikrofonprofile, Frequenzgangkorrektur und absolute Referenzkalibrierung sind geplant, im Live-Analyzer aber noch nicht implementiert.")
                }
                "Setups" -> {
                    AnalyzerSimpleSetupPanel(context)
                }
                "Diagnose" -> {
                    val d=state.diagnostics
                    SettingNote("Rohquelle: ${d?.sourceRateHz ?: "–"} Hz · ${d?.sourceLabel ?: "–"}")
                    SettingNote("Rohpeak: ${d?.rawPeak ?: "–"} PCM · RMS: ${d?.rawRms ?: "–"}")
                    SettingNote("LF-Peak: ${d?.lfPeak ?: "–"} PCM · LF-RMS: ${d?.lfRms ?: "–"}")
                    SettingNote("FFT: ${state.spectra} · Signalalter: ${state.signalAgeMs} ms")
                    SettingNote("Drops: ${state.dropped} · Resets: ${state.discontinuities} · Callback-Fehler: ${state.callbackErrors}")
                    SettingNote("Letztes Bild: ${state.lastSave} · Status: ${state.status}")
                    SettingNote("Export enthält nur erlaubte Einstellungen, Setup-Kopien und AUDIO_DIAG-Zeilen; keine privaten Rohdateien oder Zugangsschlüssel.")
                    OutlinedButton(onClick={
                        exportScope.launch {
                            message = "Erzeuge Diagnose-ZIP …"
                            runCatching { withContext(Dispatchers.IO) { AnalyzerInspectionExport.exportDiagnostics(context) } }
                                .onSuccess { message="Diagnose erstellt: $it" }
                                .onFailure { message="Diagnose-Export fehlgeschlagen: ${it.javaClass.simpleName}" }
                        }
                    }) { Text("Diagnose-ZIP exportieren", color=white) }
                    if (message.isNotBlank()) SettingNote(message)
                    SettingNote("Automatischer Systemtest · LFN-Scout v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE}). Prüft isolierte A/B-JSON-Setups, WAV-Datei-I/O und Statistiklogik sowie bei aktivem Scout-Rolling echte Analyzer-STOP/RUN-Zyklen, MON und Filterbank-A/B/A. Temporäre Teständerungen werden zurückgesetzt; Nutzersetups und Rolling-Aufnahmen müssen geschützt bleiben. PASS bedeutet geprüfter Nachweis, FAIL erkannter Fehler, SKIP fehlender oder nicht sicher ausführbarer Nachweis. Aktive benannte Setupwechsel, durchgängige PCM-/WAV-Schreibkontinuität und Statistik-End-to-End-Abdeckung sind nicht allein durch isolierte Tests bestätigt.")
                    OutlinedButton(onClick={
                        exportScope.launch {
                            systemTestBusy = true
                            message="Systemtest läuft … (ca. 45 Sekunden, bitte nicht navigieren)"
                            runCatching { PersistentSystemTest.runAutomated(context) }
                                .onSuccess { report ->
                                    selfTestSummary=report.summary()
                                    message="Systemtest beendet: $selfTestSummary"
                                }
                                .onFailure { message="Systemtest fehlgeschlagen: ${it.javaClass.simpleName}" }
                            systemTestBusy = false
                        }
                    }, enabled = !systemTestBusy) { Text("Automatischen Systemtest starten",color=white) }
                    if(selfTestSummary.isNotBlank()) SettingNote(selfTestSummary)
                    OutlinedButton(onClick={
                        exportScope.launch {
                            message="Erzeuge Systemtest-Paket …"
                            runCatching { withContext(Dispatchers.IO) { AnalyzerInspectionExport.exportSystemTest(context) } }
                                .onSuccess { message="Systemtest-Paket exportiert: $it" }
                                .onFailure { message="Export fehlgeschlagen: ${it.javaClass.simpleName}" }
                        }
                    }) { Text("Systemtest-Paket exportieren",color=white) }
                    SettingNote("Messung mit STOP in der Analyzer-Kopfzeile beenden; dabei bleibt die Audio-Betriebsart bis zum Verlassen des gestoppten Panels erhalten.")
                }
            }
        }
    }
}

@Composable
private fun DisabledSetting(label: String, fixed: String) {
    Row(Modifier.fillMaxWidth().padding(vertical=5.dp),verticalAlignment=Alignment.CenterVertically) {
        Text(label,color=Color(0xFF798A93),fontSize=11.sp,modifier=Modifier.weight(1f))
        Text(fixed,color=Color(0xFF798A93),fontSize=11.sp)
    }
}

@Composable
private fun SettingCaption(value: String) {
    Text(value,color=Color(0xFFF1F6F8),fontSize=12.sp,fontWeight=FontWeight.Medium,
        modifier=Modifier.padding(top=5.dp,bottom=4.dp))
}

@Composable
private fun SettingNote(value: String) {
    Text(value,color=Color(0xFFABB8C0),fontSize=11.sp,modifier=Modifier.padding(vertical=4.dp))
}

@Composable
private fun SettingChoice(value: String, selected: Boolean, action: () -> Unit) {
    OutlinedButton(onClick=action,border=BorderStroke(1.dp,if(selected) Color(0xFFFFB33B) else Color(0xFF34515B)),
        contentPadding=androidx.compose.foundation.layout.PaddingValues(horizontal=9.dp,vertical=0.dp)) {
        Text(if(selected) "● $value" else value,color=Color.White,fontSize=11.sp)
    }
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical=3.dp),verticalAlignment=Alignment.CenterVertically) {
        Text(label,color=Color(0xFFF1F6F8),fontSize=11.sp,modifier=Modifier.weight(1f))
        Switch(checked=checked,onCheckedChange=change)
    }
}
