package de.brummbeobachter.app

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.Closeable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.log10
import kotlin.math.floor
import kotlin.math.ceil

/**
 * App-lifetime (not Composable-lifetime) analyzer. One subscriber of the already existing
 * shared pre-gain PCM hub: 8-kHz FIR LF or direct 48-kHz wideband FFT.
 * AudioRecord ownership resides in the separate source services. Never writes WAV.
 * Screenshot rendering is Android Bitmap-based, independent of the visible panel.
 */
internal enum class AnalyzerImageFormat(val extension: String, val mimeType: String) {
    PNG("png", "image/png"), JPG("jpg", "image/jpeg");

    companion object {
        fun fromPreference(value: String?): AnalyzerImageFormat =
            values().firstOrNull { it.name == value } ?: PNG
    }
}

internal object LiveAnalyzerController {
    const val HISTORY_SECONDS = AnalyzerTiming.HISTORY_SECONDS
    const val FFT_SIZE = 4096 // Legacy default, actual size in Snapshot.fftSize.
    const val RATE_HZ = 1000 // Legacy default; use Snapshot.sampleRateHz for active decimation.
    val allowedMaxHz = intArrayOf(20, 50, 100, 200)

    data class Snapshot(
        val running: Boolean, val paused: Boolean, val autoSave: Boolean,
        val imageFormat: AnalyzerImageFormat, val jpgQuality: Int,
        val rangeHz: Int, val minFrequencyHz: Int, val historySeconds: Int,
        val decimationFactor: Int, val sampleRateHz: Int, val statisticsMinHz: Int,
        val statisticsMaxHz: Int, val statisticsWindowSec: Int, val statisticsAverageSec: Double,
        val fftSize: Int, val windowType: AnalyzerWindow, val smoothingEnabled: Boolean,
        val smoothingTimeSeconds: Double, val rows: List<DoubleArray>, val rowEpochMillis: List<Long>, val live: DoubleArray,
        val average: DoubleArray, val maxHold: DoubleArray, val floor: DoubleArray, val frequencyHz: DoubleArray,
        val leqDbFs: Double?, val l50DbFs: Double?, val l90DbFs: Double?, val spectra: Long,
        val dropped: Long, val callbackErrors: Long, val status: String,
        val complete: Boolean, val lastSave: String,
        val diagnostics: AnalyzerRawLfSession.Diagnostics?, val validSignal: Boolean,
        val signalAgeMs: Long, val analysisGainDb: Int, val discontinuities: Long,
        val fftHopSamples: Int, val waterfallIntervalMs: Int, val historyCapacity: Int,
        val waterfallRevision: Long, val minDisplayDb: Int, val maxDisplayDb: Int,
        val waterfallMode: WaterfallMode,
        val showCurrent: Boolean, val showAverage: Boolean, val showMaxHold: Boolean, val showFloor: Boolean,
        val autoRangeActive: Boolean, val autoRangeDurationSec: Int, val autoRangeMarginDb: Int,
        val autoRangeProgress: Float, val cursorFrequencyHz: Double?,
        val direct48k: Boolean, val waterfallFrequencyHz: DoubleArray, val modeBoundaryRows: Int?,
        val testSignal: Boolean,
        val manualSaveEvent: Long, val manualSaveMessage: String,
        val directInputRateHz: Int, val directSmoothingAlpha: Double, val directPeakMarkers: Int,
        val directSubtractDc: Boolean, val directStayAwake: Boolean,
        val showOriginalWaterfall: Boolean, val showFilterWaterfall: Boolean,
        val filterMinHz: Double, val filterMaxHz: Double, val filterBandCount: Int,
        val filterRowsDbFs: List<DoubleArray?>, val filterRowsExcessDb: List<DoubleArray?>,
        val filterDisplayMinDb: Int, val filterDisplayMaxDb: Int,
        val filterAutoRangeActive: Boolean, val filterAutoRangeProgress: Float,
        val filterAutoRangeDurationSec: Int, val filterAutoRangeMarginDb: Int,
        val directTimeSmoothing: Boolean
    )

    private val lock = Any()
    /** Shared in-process controller lock; does not substitute for the service lifecycle gate. */
    internal val setupTransitionLock: Any get() = lock
    private val writer = Executors.newSingleThreadExecutor { r -> Thread(r, "LFN-Analyzer-Image").apply { isDaemon = true } }
    private val writing = AtomicBoolean(false)
    private var session: AnalyzerRawLfSession? = null
    private var direct48k = false
    private var directInputRateHz = 48000
    private var directSmoothingAlpha = 0.25
    private var directPeakMarkers = 0
    private var directSubtractDc = true
    private var directStayAwake = false
    private var directTimeSmoothing = false
    private var showOriginalWaterfall = true
    private var showFilterWaterfall = false
    private var filterMinHz = 0.0
    private var filterMaxHz = 100.0
    private var filterBandCount = 10
    private var filterDisplayMinDb = -120
    private var filterDisplayMaxDb = -60
    private var filterAutoRangeDurationSec = 2
    private var filterAutoRangeMarginDb = 5
    private var filterAutoRange: FilterDisplayRange? = null
    private var filterAutoRangeStartNs = 0L
    private val filterHistory = java.util.ArrayDeque<FastFilterBank.Levels>()
    private var pendingFilter: FastFilterBank.Levels? = null
    // LFN-FILTER-01: diagnostic counters only; do not alter DSP or missing-row semantics.
    private var filterCallbackCount = 0L
    private var filterRowsWithData = 0L
    private var filterRowsMissing = 0L
    private var filterLastCallbackNs = 0L
    private var filterLastTraceNs = 0L
    private var filterInvalidCallbacks = 0L
    private var modeBoundaryRows: Int? = null
    private val activeKeys = listOf("decimation_factor", "statistics_min_hz", "statistics_max_hz", "statistics_window_sec",
        "statistics_average_sec", "range_hz", "min_frequency_hz", "fft_size", "fft_window",
        "smoothing_enabled", "smoothing_time_sec", "auto_png", "image_format", "jpg_quality",
        "analyzer_gain_db", "fft_hop_samples", "waterfall_interval_ms", "history_seconds",
        "waterfall_mode", "display_min_db", "display_max_db", "autorange_duration_sec",
        "autorange_margin_db", "show_current", "show_average", "show_max_hold", "show_floor",
        "direct_sample_rate", "direct_audio_source", "direct_smoothing_alpha", "direct_peak_markers",
        "direct_subtract_dc", "direct_stay_awake", "direct_test1_on", "direct_test2_on",
        "direct_test1_hz", "direct_test2_hz", "direct_time_smoothing",
        "filter_min_hz", "filter_max_hz", "filter_bands", "filter_show_original", "filter_show_filter",
        "filter_display_min_db", "filter_display_max_db", "filter_autorange_duration_sec", "filter_autorange_margin_db")

    /** 48-kHz parameters have a separate persisted copy, never overwrite LF setup slots 1–3. */
    fun configureMode(context: Context, wide: Boolean) {
        synchronized(lock) {
            check(!running) { "Betriebsart nur bei gestopptem Analyzer ändern" }
            val prefs = context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            if (wide) {
                val editor = prefs.edit()
                activeKeys.forEach { key -> copyKey(prefs, editor, key, "mode_8_$key") }
                if (!prefs.getBoolean("mode_48_initialized", false)) {
                    editor.putInt("mode_48_decimation_factor", 1).putInt("mode_48_range_hz", 22000)
                        .putInt("mode_48_min_frequency_hz", 0).putInt("mode_48_fft_size", 8192)
                        .putInt("mode_48_fft_hop_samples", 4800).putInt("mode_48_waterfall_interval_ms", 250)
                        .putInt("mode_48_history_seconds", 120).putInt("mode_48_direct_sample_rate", 48000)
                        .putString("mode_48_direct_audio_source", "AUTO")
                        .putFloat("mode_48_direct_smoothing_alpha", 0.25f)
                        .putInt("mode_48_direct_peak_markers", 0).putBoolean("mode_48_direct_subtract_dc", true)
                        .putBoolean("mode_48_direct_stay_awake", false)
                        .putBoolean("mode_48_initialized", true)
                    if (!prefs.contains("setup_4_range")) editor.putInt("setup_4_range",22000)
                        .putInt("setup_4_min_frequency",0).putInt("setup_4_fft_size",8192)
                        .putInt("setup_4_fft_hop",4800).putInt("setup_4_decimation_factor",1)
                        .putInt("setup_4_history_seconds",120).putString("setup_4_name","Analyzer 48 kHz")
                        .putString("setup_4_description","Breitbandanalyse ohne Rolling; 0–22000 Hz")
                }
                editor.commit()
                val restore = prefs.edit()
                activeKeys.forEach { key -> copyKey(prefs, restore, "mode_48_$key", key) }
                restore.commit()
            } else if (direct48k || prefs.getBoolean("mode_restore_after_crash", false)) {
                val restore = prefs.edit()
                activeKeys.forEach { key -> copyKey(prefs, restore, "mode_8_$key", key) }
                restore.putBoolean("mode_restore_after_crash",false).commit()
            }
            direct48k = wide
            // v0.5.05: after 48-kHz STOP and panel exit, an idle controller can
            // immediately be recomposed. The old in-memory factor 1 is NOT a valid
            // 8-kHz LF factor. Reload the restored values BEFORE releasing the lock.
            if (!wide) {
                decimationFactor = prefs.getInt("decimation_factor", 8)
                    .let { if (it in AnalyzerTiming.allowedDecimationFactors) it else 8 }
                fftHopSamples = prefs.getInt("fft_hop_samples", AnalyzerTiming.DEFAULT_FFT_HOP)
                    .let { if (it in AnalyzerTiming.allowedFftHops) it else AnalyzerTiming.DEFAULT_FFT_HOP }
                fftSize = prefs.getInt("fft_size", AnalyzerTiming.FFT_SIZE)
                    .let { if (it in AnalyzerTiming.allowedFftSizes) it else AnalyzerTiming.FFT_SIZE }
                range = prefs.getInt("range_hz", 100).coerceIn(5, 200)
                minFrequencyHz = prefs.getInt("min_frequency_hz", 0)
                    .let { if (AnalyzerTiming.validRange(it, range)) it else 0 }
                historySeconds = prefs.getInt("history_seconds", AnalyzerTiming.HISTORY_SECONDS)
                    .coerceIn(AnalyzerTiming.MIN_HISTORY_SECONDS, AnalyzerTiming.MAX_HISTORY_SECONDS)
                waterfallIntervalMs = prefs.getInt("waterfall_interval_ms", AnalyzerTiming.DEFAULT_WATERFALL_INTERVAL_MS)
                    .let { if (it in AnalyzerTiming.allowedWaterfallIntervalsMs) it else AnalyzerTiming.DEFAULT_WATERFALL_INTERVAL_MS }
                waterfallIntervalMs = AnalyzerTiming.intervalForDuration(historySeconds, waterfallIntervalMs,
                    fftHopSamples, 8000 / decimationFactor)
                    ?: AnalyzerTiming.allowedWaterfallIntervalsMs.last()
                history.clear(); historyEpochMillis.clear(); filterHistory.clear(); pendingFilter=null; clearStatistics(); current = null; floor = doubleArrayOf()
                waterfallAccumulator.clear(); waterfallPhaseSamples = 0; waterfallRevision++
                validSignal = false; modeBoundaryRows = null
                AnalyzerAudioTrace.write(context, "LF_IDLE_MODE_RESTORED", "factor=$decimationFactor hop=$fftHopSamples")
            }
        }
    }

    fun persistModeSetup(context: Context) {
        synchronized(lock) {
            if (!direct48k) return
            val prefs = context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            activeKeys.forEach { key -> copyKey(prefs, editor, key, "mode_48_$key") }
            editor.commit()
        }
    }

    fun recoverWidebandSettings(context: Context) {
        val prefs = context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        activeKeys.forEach { key -> copyKey(prefs, editor, key, "mode_48_$key") }
        editor.putBoolean("mode_restore_after_crash", true).commit()
    }

    private fun copyKey(prefs: android.content.SharedPreferences, editor: android.content.SharedPreferences.Editor,
                        source: String, target: String) {
        when (val value = prefs.all[source]) {
            is Int -> editor.putInt(target, value)
            is Float -> editor.putFloat(target, value)
            is Boolean -> editor.putBoolean(target, value)
            is String -> editor.putString(target, value)
            is Long -> editor.putLong(target, value)
            null -> editor.remove(target)
        }
    }

    private var latestDiag: AnalyzerRawLfSession.Diagnostics? = null
    private var lastPcmTraceNs = 0L
    private var validSignal = false
    private var analyzerGainDb = 0
    private var fftHopSamples = AnalyzerTiming.DEFAULT_FFT_HOP
    private var fftSize = AnalyzerTiming.FFT_SIZE
    private var minFrequencyHz = 0
    private var historySeconds = AnalyzerTiming.HISTORY_SECONDS
    private var decimationFactor = 8
    private var statisticsMinHz = 2
    private var statisticsMaxHz = 25
    private var statisticsWindowSec = 120
    private var statisticsAverageSec = 5.0
    private val statsPowerWindow = java.util.ArrayDeque<Double>()
    private var statsPowerSum = 0.0
    private val leqPowerHistory = java.util.ArrayDeque<Double>()
    private var leqPowerSum = 0.0
    private var cachedLeq: Double? = null
    private var cachedL50: Double? = null
    private var cachedL90: Double? = null
    private var lastStatsComputeNs = 0L
    private var windowType = AnalyzerWindow.HANN
    private var smoothingEnabled = true
    private var smoothingTimeSeconds = 5.0
    private var waterfallIntervalMs = AnalyzerTiming.DEFAULT_WATERFALL_INTERVAL_MS
    private var autoRangeDurationSec = 2
    private var autoRangeMarginDb = 5
    private var autoRange: AnalyzerDisplayRange? = null
    private var autoRangeStartNs = 0L
    private var displayMinDb = -120
    private var displayMaxDb = -60
    private var showCurrent = true
    private var showAverage = true
    private var showMaxHold = false
    private var showFloor = false
    private var waterfallRevision = 0L
    private var waterfallMode = WaterfallMode.CURRENT
    private val waterfallAccumulator = WaterfallAggregator()
    private var waterfallPhaseSamples = 0
    private var silentWindows = 0L
    private var lastDiscontinuities = 0L
    private var appContext: Context? = null
    private var running = false
    private var paused = false
    private var auto = false
    private var imageFormat = AnalyzerImageFormat.PNG
    private var jpgQuality = 90
    private var manualSaveEvent = 0L
    private var manualSaveMessage = ""
    private var range = 100
    private val history = java.util.ArrayDeque<DoubleArray>()
    // 1:1 companion timeline: the actual wall-clock instant each waterfall row was acquired.
    private val historyEpochMillis = java.util.ArrayDeque<Long>()
    private val levelHistory = java.util.ArrayDeque<Double>()
    private var current: LiveAnalyzerCore.Spectrum? = null
    private var floor: DoubleArray = doubleArrayOf()
    private var seq = 0L
    private var lastAutoNs = 0L
    private var cursorFrequencyHz: Double? = null
    private var lastSave = "–"
    private var status = "Analyzer gestoppt"

    /** Phase 3O/5: optional non-persistent FFT/filter restart override.
     * This is not a complete audio setup switch. Caller must coordinate recorder ownership.
     * Overrides are intentionally consumed only for this start call; normal start reads prefs.
     */
    data class DspStartOverride(
        val fftSize: Int,
        val window: AnalyzerWindow,
        val smoothingEnabled: Boolean,
        val smoothingTimeSeconds: Double,
        val filterMinHz: Double? = null,
        val filterMaxHz: Double? = null,
        val filterBands: Int? = null
    )

    /** Phase 3O/6: in-memory checkpoint of ONLY the stopped FFT/filter geometry.
     * Not a checkpoint of the audio owner, recorder, capture mode or a running session.
     * Never writes SharedPreferences; restoring is forbidden while running.
     */
    data class StoppedDspCheckpoint(
        val directMode: Boolean,
        val fftSize: Int,
        val window: AnalyzerWindow,
        val smoothingEnabled: Boolean,
        val smoothingTimeSeconds: Double,
        val filterMinHz: Double?,
        val filterMaxHz: Double?,
        val filterBands: Int?
    )

    fun captureStoppedDspCheckpoint(): StoppedDspCheckpoint = synchronized(lock) {
        check(!running) { "DSP-Checkpoint nur bei STOP" }
        StoppedDspCheckpoint(direct48k, fftSize, windowType, smoothingEnabled, smoothingTimeSeconds,
            if (direct48k) null else filterMinHz,
            if (direct48k) null else filterMaxHz,
            if (direct48k) null else filterBandCount)
    }

    fun restoreStoppedDspCheckpoint(checkpoint: StoppedDspCheckpoint) {
        synchronized(lock) {
            check(!running) { "DSP-Restore nur bei STOP" }
            check(direct48k == checkpoint.directMode) { "DSP-Restore: Audio-Modus geändert" }
            require(checkpoint.fftSize in if (direct48k) DirectAnalyzerConfig.fftSizes else AnalyzerTiming.allowedFftSizes)
            require(checkpoint.smoothingTimeSeconds.isFinite() && checkpoint.smoothingTimeSeconds in 0.01..60.0)
            val filter = if (direct48k) {
                require(checkpoint.filterMinHz == null && checkpoint.filterMaxHz == null && checkpoint.filterBands == null)
                null
            } else {
                FastFilterBank.Config(
                    requireNotNull(checkpoint.filterMinHz),
                    requireNotNull(checkpoint.filterMaxHz),
                    requireNotNull(checkpoint.filterBands))
            }
            // Validation finished before the first assignment. No preference access.
            fftSize = checkpoint.fftSize
            windowType = checkpoint.window
            smoothingEnabled = checkpoint.smoothingEnabled
            smoothingTimeSeconds = checkpoint.smoothingTimeSeconds
            if (filter != null) {
                filterMinHz = filter.minHz
                filterMaxHz = filter.maxHz
                filterBandCount = filter.bands
            }
            pendingFilter = null
            filterAutoRange = null
            filterAutoRangeStartNs = 0L
            status = "DSP-Checkpoint bei STOP wiederhergestellt"
        }
    }

    /** Phase 3O/7: read-only STOP snapshot of additional audio/timing state.
     * Diagnostic boundary only: DOES NOT restore service, PCM, Recorder or preferences.
     * A future rollback must validate all of these before mutating anything.
     */
    data class StoppedAudioTimingCheckpoint(
        val directMode: Boolean,
        val directInputRateHz: Int,
        val directSmoothingAlpha: Double,
        val directPeakMarkers: Int,
        val directSubtractDc: Boolean,
        val directStayAwake: Boolean,
        val directTimeSmoothing: Boolean,
        val decimationFactor: Int,
        val fftHopSamples: Int,
        val waterfallIntervalMs: Int,
        val historySeconds: Int,
        val analysisGainDb: Int
    )

    fun captureStoppedAudioTimingCheckpoint(): StoppedAudioTimingCheckpoint = synchronized(lock) {
        check(!running) { "Audio-/Timing-Checkpoint nur bei STOP" }
        StoppedAudioTimingCheckpoint(
            direct48k, directInputRateHz, directSmoothingAlpha, directPeakMarkers,
            directSubtractDc, directStayAwake, directTimeSmoothing, decimationFactor,
            fftHopSamples, waterfallIntervalMs, historySeconds, analyzerGainDb
        )
    }

    /** Compare a previously captured stopped state without side effects.
     * False means stale state; caller must not apply/restart. No preference reads.
     */
    fun isStoppedAudioTimingCheckpointCurrent(checkpoint: StoppedAudioTimingCheckpoint): Boolean = synchronized(lock) {
        !running && captureStoppedAudioTimingCheckpoint() == checkpoint
    }

    /** Phase 3O/8: consistent STOP snapshot across DSP + audio/timing under ONE lock.
     * In-memory diagnostics only. Does not capture microphone owner, recorder, or sessions.
     * Not sufficient to authorize an executable JSON setup switch.
     */
    data class StoppedAnalyzerConfigCheckpoint(
        val dsp: StoppedDspCheckpoint,
        val audioTiming: StoppedAudioTimingCheckpoint
    )

    fun captureStoppedAnalyzerConfigCheckpoint(): StoppedAnalyzerConfigCheckpoint = synchronized(lock) {
        check(!running) { "Gesamt-Checkpoint nur bei STOP" }
        StoppedAnalyzerConfigCheckpoint(
            captureStoppedDspCheckpoint(), captureStoppedAudioTimingCheckpoint()
        )
    }

    /** Staleness test is atomic with respect to controller setters taking [lock].
     * No calls to preferences, no hardware changes and no persistence.
     */
    fun isStoppedAnalyzerConfigCheckpointCurrent(checkpoint: StoppedAnalyzerConfigCheckpoint): Boolean = synchronized(lock) {
        !running && captureStoppedDspCheckpoint() == checkpoint.dsp &&
            captureStoppedAudioTimingCheckpoint() == checkpoint.audioTiming
    }

    /** Internal rollback for a failed subscriber start. Called only under [lock] and
     * only with a checkpoint captured immediately before this start attempt.
     * Does not restore hardware ownership, service/recorder or saved preferences.
     */
    private fun restoreStoppedConfigAfterFailedStart(cp: StoppedAnalyzerConfigCheckpoint) {
        check(!running && direct48k == cp.dsp.directMode && direct48k == cp.audioTiming.directMode)
        fftSize = cp.dsp.fftSize
        windowType = cp.dsp.window
        smoothingEnabled = cp.dsp.smoothingEnabled
        smoothingTimeSeconds = cp.dsp.smoothingTimeSeconds
        if (!direct48k) {
            filterMinHz = requireNotNull(cp.dsp.filterMinHz)
            filterMaxHz = requireNotNull(cp.dsp.filterMaxHz)
            filterBandCount = requireNotNull(cp.dsp.filterBands)
        }
        directInputRateHz = cp.audioTiming.directInputRateHz
        directSmoothingAlpha = cp.audioTiming.directSmoothingAlpha
        directPeakMarkers = cp.audioTiming.directPeakMarkers
        directSubtractDc = cp.audioTiming.directSubtractDc
        directStayAwake = cp.audioTiming.directStayAwake
        directTimeSmoothing = cp.audioTiming.directTimeSmoothing
        decimationFactor = cp.audioTiming.decimationFactor
        fftHopSamples = cp.audioTiming.fftHopSamples
        waterfallIntervalMs = cp.audioTiming.waterfallIntervalMs
        historySeconds = cp.audioTiming.historySeconds
        analyzerGainDb = cp.audioTiming.analysisGainDb
        pendingFilter = null
        filterAutoRange = null
        filterAutoRangeStartNs = 0L
    }

    /** Phase 3O: opt-in start without reloading analyzer preferences.
     * Not yet connected to production setup switching. Caller must hold the coordinated
     * audio lease; this method does not establish microphone ownership or hardware rollback.
     */
    fun startFromStoppedConfiguration(context: Context, expected: StoppedAnalyzerConfigCheckpoint) {
        startInternal(context, null, expected)
    }

    fun start(context: Context, dspOverride: DspStartOverride? = null) {
        startInternal(context, dspOverride, null)
    }

    private fun startInternal(context: Context, dspOverride: DspStartOverride?,
                              expectedStoppedConfig: StoppedAnalyzerConfigCheckpoint?) {
        synchronized(lock) {
            if (expectedStoppedConfig != null) {
                check(!running) { "Preference-freier Start erfordert STOP" }
                check(isStoppedAnalyzerConfigCheckpointCurrent(expectedStoppedConfig)) {
                    "STOP-Konfiguration wurde zwischen Checkpoint und Start verändert"
                }
                require(dspOverride == null) { "Keine Overrides im STOP-Konfigurationsstart" }
            } else if (running) return
            // Restore all stopped controller configuration if subscriber creation fails.
            // This snapshot does not include the microphone owner or the recorder.
            val beforeStart = captureStoppedAnalyzerConfigCheckpoint()
            appContext = context.applicationContext
            NoiseFloorReference.attach(context.applicationContext)
            if (expectedStoppedConfig == null) {
            val prefs = context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            if (direct48k) {
                directInputRateHz = prefs.getInt("direct_sample_rate", 48000)
                    .let { if (it in DirectAnalyzerConfig.inputRates) it else 48000 }
                directSmoothingAlpha = prefs.getFloat("direct_smoothing_alpha", 0.25f).toDouble().coerceIn(0.0, 1.0)
                directPeakMarkers = prefs.getInt("direct_peak_markers", 0).coerceIn(0, 2)
                directSubtractDc = prefs.getBoolean("direct_subtract_dc", true)
                directStayAwake = prefs.getBoolean("direct_stay_awake", false)
                directTimeSmoothing = prefs.getBoolean("direct_time_smoothing",false)
            }
            decimationFactor = if (direct48k) prefs.getInt("decimation_factor", 1)
                .let { if (DirectAnalyzerConfig.valid(directInputRateHz, it)) it else 0 }
                else prefs.getInt("decimation_factor", 8)
                    .let { if (it in AnalyzerTiming.allowedDecimationFactors) it else 8 }
            statisticsMinHz = prefs.getInt("statistics_min_hz", 2)
            statisticsMaxHz = prefs.getInt("statistics_max_hz", 25)
            statisticsWindowSec = prefs.getInt("statistics_window_sec", 120)
            statisticsAverageSec = prefs.getFloat("statistics_average_sec", 5f).toDouble()
            if (!AnalyzerTiming.validStatistics(statisticsMinHz, statisticsMaxHz, statisticsWindowSec, statisticsAverageSec)) {
                statisticsMinHz = 2; statisticsMaxHz = 25; statisticsWindowSec = 120; statisticsAverageSec = 5.0
            }
            range = prefs.getInt("range_hz", if (direct48k) 22000 else 100)
                .coerceIn(5, if (direct48k) activeSampleRateHz()/2 else 200)
            minFrequencyHz = prefs.getInt("min_frequency_hz", 0).let {
                if ((if (direct48k) AnalyzerTiming.validWidebandRange(it,range) else AnalyzerTiming.validRange(it,range))) it else 0 }
            fftSize = prefs.getInt("fft_size", AnalyzerTiming.FFT_SIZE)
                .let { if (it in (if (direct48k) DirectAnalyzerConfig.fftSizes else AnalyzerTiming.allowedFftSizes)) it else AnalyzerTiming.FFT_SIZE }
            windowType = prefs.getString("fft_window", AnalyzerWindow.HANN.name)
                ?.let { raw -> AnalyzerWindow.values().firstOrNull { it.name == raw } } ?: AnalyzerWindow.HANN
            smoothingEnabled = prefs.getBoolean("smoothing_enabled", true)
            smoothingTimeSeconds = prefs.getFloat("smoothing_time_sec", 5.0f).toDouble()
                .let { if (it.isFinite() && it in 0.01..60.0) it else 5.0 }
            if (!direct48k) {
                filterMinHz = prefs.getFloat("filter_min_hz",0f).toDouble()
                filterMaxHz = prefs.getFloat("filter_max_hz",100f).toDouble()
                filterBandCount = prefs.getInt("filter_bands",10)
                if (runCatching { FastFilterBank.Config(filterMinHz,filterMaxHz,filterBandCount) }.isFailure) {
                    filterMinHz=0.0; filterMaxHz=100.0; filterBandCount=10
                }
                showOriginalWaterfall=prefs.getBoolean("filter_show_original",true)
                showFilterWaterfall=prefs.getBoolean("filter_show_filter",false)
                filterDisplayMinDb = prefs.getInt("filter_display_min_db",-120)
                filterDisplayMaxDb = prefs.getInt("filter_display_max_db",-60)
                if (!isValidDisplayRange(filterDisplayMinDb, filterDisplayMaxDb)) {
                    filterDisplayMinDb=-120;filterDisplayMaxDb=-60
                }
                filterAutoRangeDurationSec=prefs.getInt("filter_autorange_duration_sec",2).coerceIn(1,30)
                filterAutoRangeMarginDb=prefs.getInt("filter_autorange_margin_db",5).coerceIn(0,30)
            }
            } // End of legacy preference reload; explicit STOP configuration is preserved.
            // Override only after prefs initialization, before newSession(); no writes.
            // Validate the complete override before modifying any DSP state.
            if (dspOverride != null) {
                require(dspOverride.fftSize in if (direct48k) DirectAnalyzerConfig.fftSizes else AnalyzerTiming.allowedFftSizes)
                require(dspOverride.smoothingTimeSeconds.isFinite() && dspOverride.smoothingTimeSeconds in 0.01..60.0)
                val filterValues = listOf(dspOverride.filterMinHz, dspOverride.filterMaxHz, dspOverride.filterBands)
                require(filterValues.all { it == null } || filterValues.all { it != null }) {
                    "Filterentwurf muss alle drei Werte enthalten"
                }
                require(direct48k.not() || filterValues.all { it == null }) {
                    "Filterbank-Entwurf nur im 8-kHz-Modus"
                }
                if (filterValues.all { it != null }) {
                    FastFilterBank.Config(dspOverride.filterMinHz!!, dspOverride.filterMaxHz!!, dspOverride.filterBands!!)
                }
                fftSize = dspOverride.fftSize
                windowType = dspOverride.window
                smoothingEnabled = dspOverride.smoothingEnabled
                smoothingTimeSeconds = dspOverride.smoothingTimeSeconds
                if (filterValues.all { it != null }) {
                    filterMinHz = dspOverride.filterMinHz!!
                    filterMaxHz = dspOverride.filterMaxHz!!
                    filterBandCount = dspOverride.filterBands!!
                }
            }
            filterAutoRange=null; filterAutoRangeStartNs=0L
            if (expectedStoppedConfig == null) {
            val prefs = context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            auto = prefs.getBoolean("auto_png", false)
            imageFormat = AnalyzerImageFormat.fromPreference(prefs.getString("image_format", null))
            jpgQuality = prefs.getInt("jpg_quality", 90).coerceIn(70, 100)
            paused = false
            analyzerGainDb = prefs.getInt("analyzer_gain_db", 0).coerceIn(0,50)
            fftHopSamples = prefs.getInt("fft_hop_samples", if (direct48k) 4800 else AnalyzerTiming.DEFAULT_FFT_HOP)
                .let { if (direct48k) {
                    if (it in DirectAnalyzerConfig.permissibleHops) it else DirectAnalyzerConfig.hop(directInputRateHz, decimationFactor, 100)
                } else if (it in AnalyzerTiming.allowedFftHops) it else AnalyzerTiming.DEFAULT_FFT_HOP }
            waterfallIntervalMs = prefs.getInt("waterfall_interval_ms", AnalyzerTiming.DEFAULT_WATERFALL_INTERVAL_MS)
                .let { if (it in AnalyzerTiming.allowedWaterfallIntervalsMs) it else AnalyzerTiming.DEFAULT_WATERFALL_INTERVAL_MS }
            waterfallIntervalMs = AnalyzerTiming.effectiveWaterfallIntervalMs(fftHopSamples, waterfallIntervalMs, activeSampleRateHz())
            historySeconds = prefs.getInt("history_seconds", AnalyzerTiming.HISTORY_SECONDS)
                .coerceIn(AnalyzerTiming.MIN_HISTORY_SECONDS, AnalyzerTiming.MAX_HISTORY_SECONDS)
            waterfallIntervalMs = AnalyzerTiming.intervalForDuration(historySeconds, waterfallIntervalMs,
                fftHopSamples, activeSampleRateHz()) ?: AnalyzerTiming.allowedWaterfallIntervalsMs.last()
            waterfallMode = prefs.getString("waterfall_mode", WaterfallMode.CURRENT.name)
                ?.let { raw -> WaterfallMode.values().firstOrNull { it.name == raw } } ?: WaterfallMode.CURRENT
            displayMinDb = prefs.getInt("display_min_db", -120)
            displayMaxDb = prefs.getInt("display_max_db", -60)
            if (!isValidDisplayRange(displayMinDb, displayMaxDb)) {
                displayMinDb = -120; displayMaxDb = -60
            }
            autoRangeDurationSec = prefs.getInt("autorange_duration_sec", 2).coerceIn(1, 30)
            autoRangeMarginDb = prefs.getInt("autorange_margin_db", 5).coerceIn(0, 30)
            autoRange = null; autoRangeStartNs = 0L
            showCurrent = prefs.getBoolean("show_current", true)
            showAverage = prefs.getBoolean("show_average", true)
            showMaxHold = prefs.getBoolean("show_max_hold", false)
            showFloor = prefs.getBoolean("show_floor", false)
            } else {
                // No preference lookups or persistence: preserve the previously configured
                // DSP, timing, display, statistics and analyzer-gain values.
                check(fftSize in (if (direct48k) DirectAnalyzerConfig.fftSizes else AnalyzerTiming.allowedFftSizes))
                check(decimationFactor > 0)
                check(fftHopSamples > 0 && historySeconds > 0)
                if (!direct48k) FastFilterBank.Config(filterMinHz, filterMaxHz, filterBandCount)
                paused = false
                autoRange = null; autoRangeStartNs = 0L
            }
            latestDiag = null; validSignal = false; silentWindows = 0L; lastDiscontinuities = 0L
            lastPcmTraceNs = 0L
            history.clear(); historyEpochMillis.clear(); filterHistory.clear(); pendingFilter=null; clearStatistics(); current = null; floor = doubleArrayOf(); seq = 0L
            filterCallbackCount=0L; filterRowsWithData=0L; filterRowsMissing=0L
            filterInvalidCallbacks=0L; filterLastCallbackNs=0L; filterLastTraceNs=0L
            waterfallPhaseSamples = 0; waterfallAccumulator.clear(); waterfallRevision++; modeBoundaryRows = null
            lastAutoNs = 0L
            // The service/recorder is the only microphone owner; AnalyzerLiveSession never opens it.
            // Phase 3O/9: the subscriber may fail to initialize. A failed start
            // must never advertise a running analyzer or retain a local buffer.
            // This does NOT roll back preferences, microphone owner or recordings.
            try {
                LocalAnalyzerAudioBuffer.start()
                session = newSession()
                running = true
            } catch (startFailure: Throwable) {
                runCatching { session?.close() }
                session = null
                runCatching { LocalAnalyzerAudioBuffer.stop() }
                running = false
                paused = false
                // The initial state was captured while holding the same controller lock.
                // Restore ALL captured in-memory configuration, not only the DSP fields.
                // Do not touch SharedPreferences, rolling PCM or active WAV writers.
                runCatching { restoreStoppedConfigAfterFailedStart(beforeStart) }
                    .exceptionOrNull()?.let { startFailure.addSuppressed(it) }
                status = "Analyzer-Start fehlgeschlagen; Analyzer gestoppt"
                throw startFailure
            }
            status = if (direct48k) "Warte auf ${directInputRateHz}-Hz-Direktmikrofon" else "Warte auf unveränderte 8-kHz-Rohdaten. Rolling-Buffer einschalten."
            AnalyzerAudioTrace.write(context, "ANALYZER_SUBSCRIBED", "wide=$direct48k inputRate=${if (direct48k) directInputRateHz else 8000} decimation=$decimationFactor fft=$fftSize hop=$fftHopSamples subscribers=${AnalyzerSessionGateway.rawPcm.activeSubscribers}")
        }
    }

    /** Diagnostic for the analyzer-owned resources only, not AudioRecord or service ownership.
     * Does not stop or mutate anything. The controller lock also guards local session changes.
     */
    internal data class LocalStopVerification(
        val controllerRunning: Boolean,
        val analyzerSessionPresent: Boolean,
        val localBufferSubscribed: Boolean
    ) {
        val locallyStopped: Boolean
            get() = !controllerRunning && !analyzerSessionPresent && !localBufferSubscribed
    }

    internal fun verifyLocalStopForSetupTransition(): LocalStopVerification = synchronized(lock) {
        LocalStopVerification(running, session != null, LocalAnalyzerAudioBuffer.hasActiveSubscription())
    }

    fun stop() {
        synchronized(lock) {
            appContext?.let { AnalyzerAudioTrace.write(it, "ANALYZER_STOP", "wasRunning=$running subscribers=${AnalyzerSessionGateway.rawPcm.activeSubscribers} spectra=$seq") }
            session?.close(); session = null
            LocalAnalyzerAudioBuffer.stop()
            running = false; paused = false
            autoRange = null; autoRangeStartNs = 0L
            filterAutoRange = null; filterAutoRangeStartNs = 0L
            status = "Analyzer gestoppt"
        }
    }

    private fun activeSampleRateHz(): Int = if (direct48k) {
        if (DirectAnalyzerConfig.valid(directInputRateHz, decimationFactor))
            DirectAnalyzerConfig.outputRate(directInputRateHz, decimationFactor) else directInputRateHz
    } else AnalyzerTiming.displayRateHz(false, decimationFactor)

    private fun newSession(): AnalyzerRawLfSession =
        AnalyzerSessionGateway.startRawLf(::accept, ::acceptDiagnostics, analyzerGainDb,
            fftHopSamples, fftSize, windowType, if (smoothingEnabled) smoothingTimeSeconds else 0.0,
            decimationFactor, if (direct48k) directInputRateHz else 8000,
            if (direct48k) activeSampleRateHz()/2.0 else 200.0,
            directMode = direct48k, smoothingAlpha = if (direct48k && !directTimeSmoothing) directSmoothingAlpha else null,
            subtractDc = if (direct48k) directSubtractDc else true,
            filterConfig = if (!direct48k) FastFilterBank.Config(filterMinHz,filterMaxHz,filterBandCount) else null,
            onFilter = if (direct48k) null else ::acceptFilter)

    /** Only the analyzer subscriber is recreated. Shared 8-kHz capture and WAV keep running. */
    private fun resetAnalyzerSession() {
        session?.close()
        current = null; floor = doubleArrayOf(); history.clear(); historyEpochMillis.clear(); filterHistory.clear(); pendingFilter=null; clearStatistics()
        autoRange = null; autoRangeStartNs = 0L
        filterAutoRange=null;filterAutoRangeStartNs=0L
        waterfallPhaseSamples = 0; waterfallAccumulator.clear(); waterfallRevision++; modeBoundaryRows = null
        seq = 0L; validSignal = false; latestDiag = null; lastAutoNs = 0L; lastDiscontinuities = 0L
        if (running) {
            LocalAnalyzerAudioBuffer.start() // gain/source config changed: don't mix audio epochs
            session = newSession()
        }
    }

    fun setAnalysisGain(context: Context, gainDb: Int) {
        require(gainDb in 0..50)
        context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            .edit().putInt("analyzer_gain_db", gainDb).apply()
        synchronized(lock) {
            if (analyzerGainDb == gainDb) return
            analyzerGainDb = gainDb
            // Restart only the analyzer subscriber, never the shared microphone or WAV recorder.
            if (running) {
                resetAnalyzerSession()
            }
        }
    }

    private fun isValidDisplayRange(minDb: Int, maxDb: Int): Boolean =
        minDb in -240..40 && maxDb in -230..50 && maxDb - minDb >= 10

    /** Display-only preferences. Never modifies DSP gain, WAV or shared capture. */
    fun setDisplayRange(context: Context, minDb: Int, maxDb: Int, persist: Boolean = true) {
        require(isValidDisplayRange(minDb, maxDb))
        if (persist) context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            .edit().putInt("display_min_db", minDb).putInt("display_max_db", maxDb).apply()
        synchronized(lock) {
            autoRange = null; autoRangeStartNs = 0L
            displayMinDb = minDb; displayMaxDb = maxDb; waterfallRevision++
        }
    }

    fun setAutoRangeSettings(context: Context, durationSec: Int, marginDb: Int, persist: Boolean = true) {
        require(durationSec in 1..30 && marginDb in 0..30)
        if (persist) context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            .edit().putInt("autorange_duration_sec", durationSec)
            .putInt("autorange_margin_db", marginDb).apply()
        synchronized(lock) { autoRangeDurationSec = durationSec; autoRangeMarginDb = marginDb }
    }

    /** Called only by a tap on the left spectrum dBFS axis. Starts 2 s collection by default. */
    fun startAutoRange(): Boolean = synchronized(lock) {
        if (!running || paused || !validSignal) {
            status = "Autorange: Live-Signal erforderlich"
            return@synchronized false
        }
        autoRange = AnalyzerDisplayRange(autoRangeMarginDb)
        autoRangeStartNs = System.nanoTime()
        status = "Autorange: erfasse ${autoRangeDurationSec} s · ±${autoRangeMarginDb} dB"
        true
    }

    /** FILTER-only display scale; never affects ORIGINAL, DSP or the relative-dB overlay threshold. */
    fun setFilterDisplayRange(context: Context, minDb: Int, maxDb: Int, persist: Boolean = true) {
        require(isValidDisplayRange(minDb,maxDb))
        synchronized(lock) {
            if(direct48k) return
            filterAutoRange=null;filterAutoRangeStartNs=0L
            filterDisplayMinDb=minDb;filterDisplayMaxDb=maxDb;waterfallRevision++
            if (persist) context.getSharedPreferences("lfn_scout_live_analyzer",Context.MODE_PRIVATE).edit()
                .putInt("filter_display_min_db",minDb).putInt("filter_display_max_db",maxDb).apply()
        }
    }

    fun setFilterAutoRangeSettings(context: Context, durationSec: Int, marginDb: Int, persist: Boolean = true) {
        require(durationSec in 1..30 && marginDb in 0..30)
        synchronized(lock) {
            filterAutoRangeDurationSec=durationSec;filterAutoRangeMarginDb=marginDb
            if (persist) context.getSharedPreferences("lfn_scout_live_analyzer",Context.MODE_PRIVATE).edit()
                .putInt("filter_autorange_duration_sec",durationSec)
                .putInt("filter_autorange_margin_db",marginDb).apply()
        }
    }

    /** Long-press FILTER: collect independent band levels for the chosen interval. */
    fun startFilterAutoRange(): Boolean = synchronized(lock) {
        if(direct48k || !running || paused || !validSignal) {
            status="FILTER-Autorange: 8-kHz-Live-Signal erforderlich"
            return@synchronized false
        }
        filterAutoRange=FilterDisplayRange(filterAutoRangeMarginDb)
        filterAutoRangeStartNs=System.nanoTime()
        status="FILTER-Autorange: erfasse ${filterAutoRangeDurationSec} s · ±${filterAutoRangeMarginDb} dB"
        true
    }

    private fun clearStatistics() {
        levelHistory.clear()
        statsPowerWindow.clear()
        statsPowerSum = 0.0
        leqPowerHistory.clear()
        leqPowerSum = 0.0
        cachedLeq = null
        cachedL50 = null; cachedL90 = null; lastStatsComputeNs = 0L
    }

    /** Change decimation ONLY on the analyzer subscriber; WAV and the raw PCM hub stay untouched. */
    fun setDecimation(context: Context, factor: Int) {
        require(!direct48k && factor in AnalyzerTiming.allowedDecimationFactors)
        synchronized(lock) {
            val rate = AnalyzerTiming.lfRateHz(factor)
            val newInterval = AnalyzerTiming.effectiveWaterfallIntervalMs(fftHopSamples, waterfallIntervalMs, rate)
            require(AnalyzerTiming.validHistory(historySeconds, newInterval)) {
                "Wasserfall-Dauer und Intervall mit Decimation $factor überschreiten das Zeilenlimit"
            }
            if (decimationFactor == factor) return
            decimationFactor = factor
            waterfallIntervalMs = newInterval
            context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE).edit()
                .putInt("decimation_factor", factor).putInt("waterfall_interval_ms", newInterval).apply()
            resetAnalyzerSession()
            status = "Decimation $factor:1 · $rate Hz · FFT-Warmup ${fftSize / rate.toDouble()} s"
        }
    }

    /** Independent statistics configuration; never changes spectrum FFT averaging or waterfall time. */
    fun setStatistics(context: Context, minHz: Int, maxHz: Int, windowSec: Int, averageSec: Double) {
        require(AnalyzerTiming.validStatistics(minHz, maxHz, windowSec, averageSec))
        synchronized(lock) {
            statisticsMinHz = minHz; statisticsMaxHz = maxHz
            statisticsWindowSec = windowSec; statisticsAverageSec = averageSec
            clearStatistics()
            context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE).edit()
                .putInt("statistics_min_hz", minHz).putInt("statistics_max_hz", maxHz)
                .putInt("statistics_window_sec", windowSec).putFloat("statistics_average_sec", averageSec.toFloat()).apply()
            status = "Statistik ${minHz}–${maxHz} Hz · ${windowSec} s · MW ${averageSec} s neu gestartet"
        }
    }

    /** L50/L90 and exponential average restart together; FFT ring and waterfall remain intact. */
    fun resetBandStatisticsAndAverage() {
        synchronized(lock) {
            clearStatistics()
            session?.resetAveraging()
            current = current?.let { it.copy(averageDbFs = it.liveDbFs.copyOf()) }
            status = "Leq/L50/L90 und Mittelung neu gestartet"
        }
    }

    fun setTraceVisibility(context: Context, average: Boolean, noiseFloor: Boolean, persist: Boolean = true) {
        if (persist) context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            .edit().putBoolean("show_average", average).putBoolean("show_floor", noiseFloor).apply()
        synchronized(lock) { showAverage = average; showFloor = noiseFloor }
    }

    /** Visibility changes never restart FFT, waterfall or microphone. */
    fun setSpectrumVisibility(context: Context, currentTrace: Boolean, averageTrace: Boolean, maxHoldTrace: Boolean, persist: Boolean = true) {
        if (persist) context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE).edit()
            .putBoolean("show_current", currentTrace).putBoolean("show_average", averageTrace)
            .putBoolean("show_max_hold", maxHoldTrace).apply()
        synchronized(lock) { showCurrent = currentTrace; showAverage = averageTrace; showMaxHold = maxHoldTrace }
    }

    /** Apply a direct-only DSP/recording configuration. Source and rate require STOP + RESTART.
     * Values are also stored under mode_48_* so the direct service sees them before configureMode().
     */
    fun setDirectCapture(context: Context, rateHz: Int, source: String) {
        require(rateHz in DirectAnalyzerConfig.inputRates)
        require(source in listOf("AUTO", "UNPROCESSED", "MIC", "VOICE_RECOGNITION"))
        synchronized(lock) {
            check(direct48k && !running) { "Audioquelle/Rate nur bei gestopptem Direkt-Analyzer ändern" }
            val prefs = context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            prefs.edit().putInt("direct_sample_rate",rateHz).putInt("mode_48_direct_sample_rate",rateHz)
                .putString("direct_audio_source",source).putString("mode_48_direct_audio_source",source).commit()
            directInputRateHz = rateHz
            if (!DirectAnalyzerConfig.valid(rateHz,decimationFactor)) decimationFactor = 0
            val nyquist = activeSampleRateHz()/2
            if (range > nyquist) { range = nyquist; minFrequencyHz = 0 }
            fftHopSamples = DirectAnalyzerConfig.hop(rateHz,decimationFactor,100)
            prefs.edit().putInt("decimation_factor",decimationFactor).putInt("range_hz",range)
                .putInt("min_frequency_hz",minFrequencyHz).putInt("fft_hop_samples",fftHopSamples).commit()
            resetAnalyzerSession()
        }
    }

    fun setDirectDecimation(context: Context, factor: Int) {
        synchronized(lock) {
            require(direct48k && DirectAnalyzerConfig.valid(directInputRateHz, factor))
            // Must be stopped to avoid exchanging the capture source and DSP timebase out of order.
            check(!running) { "Decimation nur bei gestopptem Direkt-Analyzer ändern" }
            decimationFactor = factor
            val nyquist = activeSampleRateHz()/2
            range = range.coerceAtMost(nyquist)
            minFrequencyHz = minFrequencyHz.coerceAtMost((range-5).coerceAtLeast(0))
            fftHopSamples = DirectAnalyzerConfig.hop(directInputRateHz,factor,100)
            context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE).edit()
                .putInt("decimation_factor",factor).putInt("range_hz",range)
                .putInt("min_frequency_hz",minFrequencyHz).putInt("fft_hop_samples",fftHopSamples).commit()
            resetAnalyzerSession()
        }
    }

    fun setDirectOptions(context: Context, alpha: Double, markers: Int, subtractDc: Boolean, stayAwake: Boolean) {
        require(alpha.isFinite() && alpha in 0.0..1.0 && markers in 0..2)
        synchronized(lock) {
            check(direct48k)
            val changed = directSmoothingAlpha != alpha || directSubtractDc != subtractDc
            directSmoothingAlpha = alpha; directPeakMarkers = markers
            directSubtractDc = subtractDc; directStayAwake = stayAwake
            context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE).edit()
                .putFloat("direct_smoothing_alpha",alpha.toFloat()).putInt("direct_peak_markers", markers)
                .putBoolean("direct_subtract_dc",subtractDc).putBoolean("direct_stay_awake",stayAwake).apply()
            if (changed) resetAnalyzerSession()
        }
    }

    /** In worker order before the next FFT callback; retains transient peaks up to the next row. */
    private fun acceptFilter(level: FastFilterBank.Levels) = synchronized(lock) {
        if(!running || paused || direct48k) return@synchronized
        filterAutoRange?.let { collector ->
            collector.accept(level.absoluteDbFs)
            if (System.nanoTime()-filterAutoRangeStartNs >= filterAutoRangeDurationSec*1_000_000_000L) {
                val result=collector.result()
                if (result != null) {
                    filterDisplayMinDb=result.first;filterDisplayMaxDb=result.second
                    appContext?.getSharedPreferences("lfn_scout_live_analyzer",Context.MODE_PRIVATE)?.edit()
                        ?.putInt("filter_display_min_db",result.first)?.putInt("filter_display_max_db",result.second)?.apply()
                    waterfallRevision++
                    status="FILTER-Autorange gespeichert: ${result.first}…${result.second} dBFS"
                } else status="FILTER-Autorange: keine gültigen Bandpegel"
                filterAutoRange=null;filterAutoRangeStartNs=0L
            }
        }
        filterCallbackCount++
        filterLastCallbackNs = System.nanoTime()
        if (level.absoluteDbFs.size != filterBandCount || level.excessDb.size != filterBandCount ||
            level.absoluteDbFs.any { !it.isFinite() } || level.excessDb.any { !it.isFinite() }) {
            filterInvalidCallbacks++
        }
        val old=pendingFilter
        if(old==null) pendingFilter=FastFilterBank.Levels(level.absoluteDbFs.copyOf(), level.excessDb.copyOf())
        else if(old.absoluteDbFs.size==level.absoluteDbFs.size) {
            for(i in level.absoluteDbFs.indices) {
                old.absoluteDbFs[i]=max(old.absoluteDbFs[i],level.absoluteDbFs[i])
                old.excessDb[i]=max(old.excessDb[i],level.excessDb[i])
            }
        }
    }

    fun setWaterfallVisibility(context: Context, original: Boolean, filter: Boolean) {
        synchronized(lock) {
            if(direct48k) return
            showOriginalWaterfall=original; showFilterWaterfall=filter
            context.getSharedPreferences("lfn_scout_live_analyzer",Context.MODE_PRIVATE).edit()
                .putBoolean("filter_show_original",original).putBoolean("filter_show_filter",filter).apply()
            waterfallRevision++ // same historical data, no restart
        }
    }

    /** Filter band geometry is set only while STOP. One existing PCM consumer; no new recorder. */
    fun setFilterConfig(context: Context, minimum: Double, maximum: Double, bands: Int, persist: Boolean = true) {
        val config=FastFilterBank.Config(minimum,maximum,bands)
        synchronized(lock) {
            check(!running && !direct48k) { "FILTER-Parameter nur nach STOP im 8-kHz-Modus ändern" }
            // Phase 3O: draft path does not touch legacy preferences.
            // A later start() still reloads preferences; this is NOT yet an active setup switch.
            if (persist) context.getSharedPreferences("lfn_scout_live_analyzer",Context.MODE_PRIVATE).edit()
                .putFloat("filter_min_hz",minimum.toFloat()).putFloat("filter_max_hz",maximum.toFloat())
                .putInt("filter_bands",bands).apply()
            filterMinHz=config.minHz; filterMaxHz=config.maxHz; filterBandCount=config.bands
            filterHistory.clear()
            repeat(history.size) { filterHistory.addLast(FastFilterBank.Levels(doubleArrayOf(),doubleArrayOf())) }
            pendingFilter=null; filterAutoRange=null;filterAutoRangeStartNs=0L; waterfallRevision++
        }
    }

    /** Direct-mode time-constant option; 5 s is reproducible across hops. */
    fun setDirectTimeSmoothing(context: Context, enabled: Boolean) {
        synchronized(lock) {
            check(direct48k)
            directTimeSmoothing=enabled
            context.getSharedPreferences("lfn_scout_live_analyzer",Context.MODE_PRIVATE).edit()
                .putBoolean("direct_time_smoothing",enabled).apply()
            if(running) resetAnalyzerSession()
        }
    }

    /** Reset ONLY max-hold; no impact on current/average, history, FFT or audio source. */
    fun resetSpectrumMaxHold() {
        synchronized(lock) {
            session?.resetMaxHold()
            current = current?.let { it.copy(maxHoldDbFs = DoubleArray(it.maxHoldDbFs.size) { -240.0 }) }
            status = "Spektrum-MAX zurückgesetzt"
        }
    }

    /** Changes only the analyzer subscriber for FFT hops; waterfall uses its own timebase. */
    fun setTiming(context: Context, fftHop: Int, intervalMs: Int) {
        require(if (direct48k) fftHop in DirectAnalyzerConfig.permissibleHops else fftHop in AnalyzerTiming.allowedFftHops)
        require(intervalMs in AnalyzerTiming.allowedWaterfallIntervalsMs)
        val effectiveInterval = AnalyzerTiming.effectiveWaterfallIntervalMs(fftHop, intervalMs, activeSampleRateHz())
        require(AnalyzerTiming.validHistory(historySeconds, effectiveInterval)) {
            "Wasserfall-Zeitdauer und Zeilenintervall überschreiten das Zeilenlimit"
        }
        context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            .edit().putInt("fft_hop_samples", fftHop)
            .putInt("waterfall_interval_ms", effectiveInterval).apply()
        synchronized(lock) {
            val newFft = fftHop != fftHopSamples
            if (!newFft && effectiveInterval == waterfallIntervalMs) return
            fftHopSamples = fftHop
            waterfallIntervalMs = effectiveInterval
            history.clear(); historyEpochMillis.clear(); filterHistory.clear(); pendingFilter=null; clearStatistics(); waterfallPhaseSamples = 0; waterfallAccumulator.clear(); waterfallRevision++; modeBoundaryRows = null
            autoRange = null; autoRangeStartNs = 0L
            lastAutoNs = 0L
            if (newFft && running) {
                resetAnalyzerSession()
                status = "FFT-Hop geändert · FFT-Warmup ${fftSize / activeSampleRateHz().toDouble()} s"
            }
        }
    }

    /** Receives raw/filtered audio diagnostics from the existing PCM subscriber.
     * Keep this callback separate from accept(Spectrum): diagnostics arrive before the first FFT.
     */
    private fun acceptDiagnostics(diag: AnalyzerRawLfSession.Diagnostics) {
        synchronized(lock) {
            if (!running) return
            latestDiag = diag
            val nowNs = System.nanoTime()
            if (lastPcmTraceNs == 0L || nowNs - lastPcmTraceNs >= 15_000_000_000L) {
                lastPcmTraceNs = nowNs
                appContext?.let { AnalyzerAudioTrace.write(it, "ANALYZER_PCM", "rate=${diag.sourceRateHz} source=${diag.sourceLabel} unsupported=${diag.unsupportedRateFrames} spectra=$seq subscribers=${AnalyzerSessionGateway.rawPcm.activeSubscribers}") }
            }
        }
    }

    /** Mode selection affects only upcoming waterfall rows; do not mix modes in one history. */
    fun setWaterfallMode(context: Context, mode: WaterfallMode) {
        context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            .edit().putString("waterfall_mode", mode.name).apply()
        synchronized(lock) {
            if (waterfallMode == mode) return
            waterfallMode = mode
            waterfallAccumulator.clear()
            waterfallPhaseSamples = 0
            modeBoundaryRows = if (history.isEmpty()) null else 0
            waterfallRevision++
            status = "Wasserfall: ${mode.label} · bisherige Zeilen bleiben erhalten"
        }
    }

    fun setCursorFrequency(hz: Double?) = synchronized(lock) {
        cursorFrequencyHz = hz?.takeIf { it.isFinite() }
    }

    fun setPaused(value: Boolean) = synchronized(lock) {
        if (value && !paused) LocalAnalyzerAudioBuffer.freeze(LocalAudioSaveSettings.load(
            checkNotNull(appContext) { "Analyzer-Kontext fehlt" }).durationSeconds)
        if (!value && paused) LocalAnalyzerAudioBuffer.resume()
        paused = value
        if (value) {
            autoRange = null; autoRangeStartNs = 0L
            filterAutoRange=null;filterAutoRangeStartNs=0L
            waterfallAccumulator.clear(); waterfallPhaseSamples = 0
            status = "Pause – Anzeige eingefroren"
        }
    }
    fun setRange(context: Context, value: Int) = setFrequencyRange(context, 0, value)

    fun setFrequencyRange(context: Context, minHz: Int, maxHz: Int) {
        require(if (direct48k) AnalyzerTiming.validWidebandRange(minHz,maxHz) && maxHz <= activeSampleRateHz()/2
            else AnalyzerTiming.validRange(minHz,maxHz))
        context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE).edit()
            .putInt("min_frequency_hz", minHz).putInt("range_hz", maxHz).apply()
        synchronized(lock) { minFrequencyHz = minHz; range = maxHz; waterfallRevision++ }
    }

    fun setHistorySeconds(context: Context, seconds: Int) {
        synchronized(lock) {
            val newInterval = AnalyzerTiming.intervalForDuration(seconds, waterfallIntervalMs,
                fftHopSamples, activeSampleRateHz()) ?: error("Keine speichersichere Wasserfall-Zeit")
            context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE).edit()
                .putInt("history_seconds", seconds).putInt("waterfall_interval_ms", newInterval).apply()
            waterfallIntervalMs = newInterval
            if (historySeconds == seconds) return
            historySeconds = seconds
            history.clear(); historyEpochMillis.clear(); filterHistory.clear(); pendingFilter=null; waterfallAccumulator.clear(); waterfallPhaseSamples = 0
            waterfallRevision++; lastAutoNs = 0L
            status = "Wasserfall: ${seconds} s · neue Historie"
        }
    }

    fun setDsp(context: Context, size: Int, window: AnalyzerWindow, enabled: Boolean, timeSeconds: Double, persist: Boolean = true) {
        require(size in (if (direct48k) DirectAnalyzerConfig.fftSizes else AnalyzerTiming.allowedFftSizes))
        require(timeSeconds.isFinite() && timeSeconds in 0.01..60.0)
        synchronized(lock) {
            // Draft-only route is restricted to STOP. Preserve legacy live edits when persist=true.
            if (!persist) check(!running) { "FFT-Entwurf nur bei gestopptem Analyzer übernehmen" }
            if (persist) context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE).edit()
                .putInt("fft_size", size).putString("fft_window", window.name)
                .putBoolean("smoothing_enabled", enabled)
                .putFloat("smoothing_time_sec", timeSeconds.toFloat()).apply()
            if (size == fftSize && window == windowType && enabled == smoothingEnabled &&
                kotlin.math.abs(timeSeconds - smoothingTimeSeconds) < 0.0001) return
            fftSize = size; windowType = window; smoothingEnabled = enabled
            smoothingTimeSeconds = timeSeconds
            resetAnalyzerSession()
            status = "FFT-Konfiguration geändert · Warmup ${size / activeSampleRateHz().toDouble()} s"
        }
    }
    fun setAutoSave(context: Context, value: Boolean) {
        synchronized(lock) { auto = value; lastAutoNs = 0L }
        context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            .edit().putBoolean("auto_png", value).apply()
    }

    /** One format for manual and automatic off-screen screenshots. Existing PNG default remains. */
    fun setImageFormat(context: Context, value: AnalyzerImageFormat) {
        context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            .edit().putString("image_format", value.name).apply()
        synchronized(lock) { imageFormat = value }
    }

    fun setJpgQuality(context: Context, value: Int) {
        require(value in 70..100)
        context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            .edit().putInt("jpg_quality", value).apply()
        synchronized(lock) { jpgQuality = value }
    }

    // Independent saved electrical noise reference. FFT power is adjusted in linear domain.
    fun noiseCalibrationState(): NoiseFloorReference.State = NoiseFloorReference.state()
    fun toggleNoiseCalibration(): String = synchronized(lock) {
        val before = NoiseFloorReference.state().enabled
        val response = NoiseFloorReference.toggle()
        if (before != NoiseFloorReference.state().enabled) {
            clearStatistics()
            history.clear(); historyEpochMillis.clear(); filterHistory.clear(); pendingFilter = null
            waterfallAccumulator.clear(); waterfallRevision++
            floor = doubleArrayOf(); current = null
            // NOISE state belongs exclusively inside the NOISE button (not in the global status line).
        }
        response
    }
    fun startNoiseCalibration(): String = synchronized(lock) {
        if (!running || paused) "Analyzer muss laufen (nicht pausiert)"
        else NoiseFloorReference.start(NoiseFloorReference.calibrationSeconds(), activeSampleRateHz(), fftSize, windowType.name, analyzerGainDb)
    }

    private fun accept(received: LiveAnalyzerCore.Spectrum) {
        // The raw hub is pre-gain; AnalyzerRawLfSession applies gain to the FFT only.
        // Correct all displayed/statistical FFT power levels ONCE before any accumulation.
        val correction = analyzerGainDb.toDouble()
        val gainCorrected = if (correction == 0.0) received else received.copy(
            liveDbFs = received.liveDbFs.map { GainReferencePolicy.compensateDb(it, correction) }.toDoubleArray(),
            averageDbFs = received.averageDbFs.map { GainReferencePolicy.compensateDb(it, correction) }.toDoubleArray(),
            maxHoldDbFs = received.maxHoldDbFs.map { GainReferencePolicy.compensateDb(it, correction) }.toDoubleArray())
        val s = NoiseFloorReference.process(gainCorrected, windowType.name, analyzerGainDb)
        var save: Snapshot? = null
        var savedRange: Pair<Int, Int>? = null
        synchronized(lock) {
            if (!running) return
            if (paused) return
            val sourceResets = session?.discontinuityCount ?: 0L
            if (sourceResets > lastDiscontinuities) {
                // Never combine old and new source/gain histories into one PNG/statistic.
                history.clear(); historyEpochMillis.clear(); filterHistory.clear(); pendingFilter=null; clearStatistics(); current = null; floor = doubleArrayOf()
                waterfallPhaseSamples = 0; waterfallAccumulator.clear(); waterfallRevision++; modeBoundaryRows = null
                lastAutoNs = 0L
                autoRange = null; autoRangeStartNs = 0L
            }
            lastDiscontinuities = sourceResets
            // This counter is controller-monotonic. FFT sequence restarts at 1 after
            // a source discontinuity or analyzer gain change, so do not reject it.
            seq++
            // A numerically filled history is NOT a signal. Reject all-zero / no-AC windows.
            // Check the ORIGINAL FFT before noise cancellation. A fully removed noise floor
            // is a valid measurement, not a lost PCM source.
            val signalState = AnalyzerSignalValidity.classify(
                gainCorrected.liveDbFs, s.liveDbFs, NoiseFloorReference.isActive())
            if (signalState == AnalyzerSignalValidity.Result.NO_INPUT) {
                silentWindows++
                validSignal = false
                status = "KEIN NUTZSIGNAL: Roh-PCM/Filter/Audioquelle prüfen"
                return
            }
            silentWindows = 0L
            validSignal = true
            current = s
            autoRange?.let { collector ->
                collector.accept(s, range)
                if (System.nanoTime() - autoRangeStartNs >= autoRangeDurationSec * 1_000_000_000L) {
                    val result = collector.result()
                    savedRange = result
                    if (result == null) status = "Autorange: keine gültigen Spektralwerte"
                    else {
                        displayMinDb = result.first
                        displayMaxDb = result.second
                        waterfallRevision++
                    }
                    autoRange = null; autoRangeStartNs = 0L
                }
            }
            if (floor.size != s.liveDbFs.size) floor = s.liveDbFs.copyOf()
            else for (i in floor.indices) {
                // Rising minimum tracker, explicitly an estimate, NOT a calibrated noise floor.
                floor[i] = min(s.liveDbFs[i], floor[i] + 0.03)
            }
            if (!paused) {
                // Each row contains all bins out to 200 Hz; range only changes the visible crop.
                val (nextPhase, dueRow) = AnalyzerTiming.advanceWaterfall(
                    waterfallPhaseSamples, fftHopSamples, waterfallIntervalMs, s.sampleRateHz)
                waterfallPhaseSamples = nextPhase
                waterfallAccumulator.accept(s.liveDbFs)
                if (dueRow) {
                    // Current: newest FFT; Average: mean LINEAR power; Maximum: binwise maximum.
                    val row = waterfallAccumulator.take(waterfallMode) ?: s.liveDbFs.copyOf()
                    history.addLast(if (direct48k) compressWidebandRow(row) else row)
                    val hadFilter = pendingFilter != null && pendingFilter!!.absoluteDbFs.isNotEmpty()
                    if (!direct48k) {
                        if (hadFilter) filterRowsWithData++ else filterRowsMissing++
                        val nowNs = System.nanoTime()
                        if (filterLastTraceNs == 0L || nowNs - filterLastTraceNs >= 5_000_000_000L) {
                            filterLastTraceNs = nowNs
                            val ageMs = if (filterLastCallbackNs == 0L) -1L else
                                (nowNs - filterLastCallbackNs) / 1_000_000L
                            appContext?.let { AnalyzerAudioTrace.write(it, "FILTER_PATH",
                                "callbacks=$filterCallbackCount rows_ok=$filterRowsWithData rows_missing=$filterRowsMissing invalid=$filterInvalidCallbacks age_ms=$ageMs bands=$filterBandCount interval_ms=$waterfallIntervalMs") }
                        }
                    }
                    filterHistory.addLast(pendingFilter ?: FastFilterBank.Levels(doubleArrayOf(),doubleArrayOf()))
                    pendingFilter=null
                    historyEpochMillis.addLast(System.currentTimeMillis())
                    modeBoundaryRows = modeBoundaryRows?.plus(1)
                    waterfallRevision++
                    val capacity = AnalyzerTiming.historyRows(waterfallIntervalMs, historySeconds)
                    while (history.size > capacity) { history.removeFirst(); historyEpochMillis.removeFirst(); filterHistory.removeFirst() }
                    if ((modeBoundaryRows ?: 0) >= history.size) modeBoundaryRows = null
                }
                val maxBin = s.frequenciesHz.indexOfLast { it <= statisticsMaxHz.toDouble() }
                val minBin = s.frequenciesHz.indexOfFirst { it >= statisticsMinHz.toDouble() }
                if (minBin >= 0 && maxBin >= minBin) {
                    var power = 0.0
                    for (i in minBin..maxBin) power += 10.0.pow(s.liveDbFs[i] / 10.0)
                    // Keep an unsmoothed power history for Leq over the complete configured statistics window.
                    // L50/L90 continue to use the configured short moving average before percentile evaluation.
                    leqPowerHistory.addLast(power)
                    leqPowerSum += power
                    val leqFrames = AnalyzerTiming.statisticFrames(fftHopSamples, statisticsWindowSec, s.sampleRateHz)
                    while (leqPowerHistory.size > leqFrames) leqPowerSum -= leqPowerHistory.removeFirst()

                    statsPowerWindow.addLast(power)
                    statsPowerSum += power
                    val averageFrames = maxOf(1, kotlin.math.ceil(statisticsAverageSec * s.sampleRateHz / fftHopSamples).toInt())
                    while (statsPowerWindow.size > averageFrames) statsPowerSum -= statsPowerWindow.removeFirst()
                    val averagedPower = (statsPowerSum / statsPowerWindow.size).coerceAtLeast(1.0e-24)
                    levelHistory.addLast(10.0 * kotlin.math.log10(averagedPower))
                    while (levelHistory.size > AnalyzerTiming.statisticFrames(fftHopSamples, statisticsWindowSec, s.sampleRateHz))
                        levelHistory.removeFirst()
                }
            }
            status = when {
                s.inputClipped -> "CLIP – Analysegain reduzieren"
                autoRange != null -> "Autorange: erfasse ${autoRangeDurationSec} s · ±${autoRangeMarginDb} dB"
                savedRange != null -> "Autorange gespeichert: ${savedRange.first}…${savedRange.second} dBFS"
                else -> if (direct48k) "Live · 48 kHz direkt · dBFS vor Gain (+${analyzerGainDb} dB komp.)" else
                    "Live · 8 kHz → ${s.sampleRateHz} Hz FIR · dBFS vor Gain (+${analyzerGainDb} dB komp.)"
            }
            if (signalState == AnalyzerSignalValidity.Result.BELOW_NOISE_FLOOR) {
                status = "Unter Rauschboden / kein auflösbares LF-Nutzsignal · Eingang aktiv"
            }
            if (auto && !paused && history.size >= AnalyzerTiming.historyRows(waterfallIntervalMs, historySeconds) && !writing.get()) {
                val nowNs = System.nanoTime()
                if (lastAutoNs == 0L || nowNs - lastAutoNs >= historySeconds * 1_000_000_000L) {
                    lastAutoNs = nowNs
                    save = snapshotLocked()
                }
            }
        }
        savedRange?.let { bounds ->
            appContext?.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
                ?.edit()?.putInt("display_min_db", bounds.first)
                ?.putInt("display_max_db", bounds.second)?.apply()
        }
        save?.let { export(it) }
    }

    /** Store at most 513 extrema per row; high-frequency bursts survive pixel aggregation. */
    private fun compressWidebandRow(row: DoubleArray): DoubleArray {
        val output = DoubleArray(513) { -240.0 }
        for (i in row.indices) {
            val bucket = (i.toLong() * 512L / maxOf(1, row.lastIndex)).toInt().coerceIn(0, 512)
            output[bucket] = maxOf(output[bucket], row[i])
        }
        return output
    }

    private fun snapshotLocked(): Snapshot {
        val s = current
        // At 600 s / hop 5, avoid sorting up to 120,000 statistics frames per 50-ms UI poll.
        // Update L50/L90 at most once a second; never report a percentile before a full window.
        val nowNs = System.nanoTime()
        if (lastStatsComputeNs == 0L || nowNs - lastStatsComputeNs >= 1_000_000_000L) {
            lastStatsComputeNs = nowNs
            val required = AnalyzerTiming.statisticFrames(fftHopSamples, statisticsWindowSec,
                activeSampleRateHz())
            if (leqPowerHistory.size >= required && required > 0) {
                cachedLeq = 10.0 * kotlin.math.log10((leqPowerSum / leqPowerHistory.size).coerceAtLeast(1.0e-24))
            } else {
                cachedLeq = null
            }
            if (levelHistory.size >= required) {
                val levels = levelHistory.sorted()
                cachedL50 = levels[((levels.size - 1) * 0.50).toInt().coerceIn(0, levels.lastIndex)]
                cachedL90 = levels[((levels.size - 1) * 0.10).toInt().coerceIn(0, levels.lastIndex)]
            } else {
                cachedL50 = null; cachedL90 = null
            }
        }
        // Rows are immutable copies when inserted; avoid repeated large copies during UI polling.
        return Snapshot(running, paused, auto, imageFormat, jpgQuality, range, minFrequencyHz,
            historySeconds, decimationFactor, activeSampleRateHz(), statisticsMinHz,
            statisticsMaxHz, statisticsWindowSec, statisticsAverageSec, fftSize, windowType, smoothingEnabled, smoothingTimeSeconds, history.toList(), historyEpochMillis.toList(),
            s?.liveDbFs?.copyOf() ?: doubleArrayOf(), s?.averageDbFs?.copyOf() ?: doubleArrayOf(),
            s?.maxHoldDbFs?.copyOf() ?: doubleArrayOf(), floor.copyOf(), s?.frequenciesHz?.copyOf() ?: doubleArrayOf(),
            if (validSignal) cachedLeq else null,
            if (validSignal) cachedL50 else null,
            if (validSignal) cachedL90 else null, seq,
            session?.droppedFrames ?: 0L, session?.callbackErrors ?: 0L,
            status, history.size >= AnalyzerTiming.historyRows(waterfallIntervalMs, historySeconds), lastSave,
            latestDiag, validSignal, latestDiag?.let { (System.nanoTime()-it.frameAgeNs).coerceAtLeast(0L)/1_000_000L } ?: -1L,
            analyzerGainDb, session?.discontinuityCount ?: 0L,
            fftHopSamples, waterfallIntervalMs,
            AnalyzerTiming.historyRows(waterfallIntervalMs, historySeconds), waterfallRevision,
            displayMinDb, displayMaxDb, waterfallMode, showCurrent, showAverage, showMaxHold, showFloor,
            autoRange != null, autoRangeDurationSec, autoRangeMarginDb,
            if (autoRange == null) 0f else ((System.nanoTime()-autoRangeStartNs).toDouble() /
                (autoRangeDurationSec*1_000_000_000.0)).toFloat().coerceIn(0f,1f),
            cursorFrequencyHz, direct48k,
            if (direct48k) DoubleArray(513) { it * (activeSampleRateHz()/2.0) / 512.0 } else s?.frequenciesHz?.copyOf() ?: doubleArrayOf(),
            modeBoundaryRows,
            AnalyzerModeCoordinator.mode in listOf(AnalyzerModeCoordinator.Mode.TEST_8K,AnalyzerModeCoordinator.Mode.TEST_48K),
            manualSaveEvent, manualSaveMessage,
            directInputRateHz, directSmoothingAlpha, directPeakMarkers, directSubtractDc, directStayAwake,
            showOriginalWaterfall, showFilterWaterfall, filterMinHz, filterMaxHz, filterBandCount,
            filterHistory.map { it.absoluteDbFs.takeIf { a -> a.isNotEmpty() }?.copyOf() }, filterHistory.map { it.excessDb.takeIf { a -> a.isNotEmpty() }?.copyOf() },
            filterDisplayMinDb,filterDisplayMaxDb,filterAutoRange!=null,
            if(filterAutoRange==null) 0f else ((System.nanoTime()-filterAutoRangeStartNs).toDouble() /
                (filterAutoRangeDurationSec*1_000_000_000.0)).toFloat().coerceIn(0f,1f),
            filterAutoRangeDurationSec,filterAutoRangeMarginDb,
            directTimeSmoothing)
    }

    /** AP31: strict STOP-only, preference-free application of a complete legacy
     * snapshot. The *whole* JSON is validated before touching controller fields.
     * No automatic mode switch, no AudioRecord start, no legacy slot write.
     * The caller must coordinate service/recorder lifetime separately.
     */
    internal fun applyStoppedJsonDraft(draft: org.json.JSONObject, rollbackOnFailure: Boolean = true): Unit = synchronized(lock) {
        check(!running && session == null) { "Analyzer muss vollständig STOP sein" }
        val before = AnalyzerRuntimeSetupSnapshot.capture(snapshotLocked())
        AnalyzerSetupFormat.validate(org.json.JSONObject().put("legacy", draft))
        val expectedKeys = AnalyzerSetupExactComparison.keys(before)
        require(AnalyzerSetupExactComparison.keys(draft) == expectedKeys) {
            "JSON nicht vollständig oder enthält nicht abbildbare Parameter"
        }
        require(draft.keys().asSequence().none { draft.isNull(it) }) { "JSON enthält null-Werte" }
        // Validate all value dependencies up-front. No setter is permitted to write preferences.
        val min = draft.getInt("min_frequency")
        val max = draft.getInt("range")
        val factor = draft.getInt("decimation_factor")
        val rate = draft.getInt("direct_sample_rate")
        require(if (direct48k) AnalyzerTiming.validWidebandRange(min, max) &&
            max <= rate / 2 else AnalyzerTiming.validRange(min, max))
        require(if (direct48k) DirectAnalyzerConfig.valid(rate, factor)
            else factor in AnalyzerTiming.allowedDecimationFactors)
        if (direct48k) require(rate in DirectAnalyzerConfig.inputRates)
        val size = draft.getInt("fft_size")
        require(size in (if (direct48k) DirectAnalyzerConfig.fftSizes else AnalyzerTiming.allowedFftSizes))
        val hop = draft.getInt("fft_hop")
        require(if (direct48k) hop in DirectAnalyzerConfig.permissibleHops else hop in AnalyzerTiming.allowedFftHops)
        val interval = draft.getInt("water_interval")
        val historySec = draft.getInt("history_seconds")
        require(interval in AnalyzerTiming.allowedWaterfallIntervalsMs)
        require(AnalyzerTiming.validHistory(historySec, interval))
        val smoothSec = draft.getDouble("smoothing_time")
        require(smoothSec.isFinite() && smoothSec in 0.01..60.0)
        val w = AnalyzerWindow.valueOf(draft.getString("window"))
        val wm = WaterfallMode.valueOf(draft.getString("water_mode"))
        val im = AnalyzerImageFormat.valueOf(draft.getString("image_format"))
        val fmin = draft.getDouble("filter_min_hz")
        val fmax = draft.getDouble("filter_max_hz")
        val fbands = draft.getInt("filter_bands")
        if (!direct48k) FastFilterBank.Config(fmin, fmax, fbands)
        val displayMin = draft.getInt("display_min")
        val displayMax = draft.getInt("display_max")
        require(isValidDisplayRange(displayMin, displayMax))
        require(draft.getInt("jpg_quality") in 70..100)
        require(draft.getInt("autorange_duration") in 1..30 && draft.getInt("autorange_margin") in 0..30)
        require(draft.getInt("filter_auto_duration") in 1..30 && draft.getInt("filter_auto_margin") in 0..30)
        require(AnalyzerTiming.validStatistics(draft.getInt("stat_min"), draft.getInt("stat_max"),
            draft.getInt("stat_window"), draft.getDouble("stat_average")))
        require(draft.getDouble("direct_smoothing_alpha") in 0.0..1.0)
        require(draft.getInt("gain") in -120..120)
        require(draft.getInt("filter_display_max") - draft.getInt("filter_display_min") >= 10)
        require(draft.getInt("direct_peak_markers") in 0..2)
        // Commit is in-memory only; for an unexpected failure restore the prior snapshot.
        try {
        minFrequencyHz = min; range = max; decimationFactor = factor
        fftSize = size; fftHopSamples = hop; windowType = w
        smoothingEnabled = draft.getBoolean("smoothing_enabled"); smoothingTimeSeconds = smoothSec
        showCurrent = draft.getBoolean("current"); showAverage = draft.getBoolean("average")
        showFloor = draft.getBoolean("floor"); showMaxHold = draft.getBoolean("max_hold")
        analyzerGainDb = draft.getInt("gain")
        displayMinDb = displayMin; displayMaxDb = displayMax
        historySeconds = historySec; waterfallIntervalMs = interval; waterfallMode = wm
        statisticsMinHz = draft.getInt("stat_min"); statisticsMaxHz = draft.getInt("stat_max")
        statisticsWindowSec = draft.getInt("stat_window")
        statisticsAverageSec = draft.getDouble("stat_average")
        filterMinHz = fmin; filterMaxHz = fmax; filterBandCount = fbands
        filterDisplayMinDb = draft.getInt("filter_display_min")
        filterDisplayMaxDb = draft.getInt("filter_display_max")
        filterAutoRangeDurationSec = draft.getInt("filter_auto_duration")
        filterAutoRangeMarginDb = draft.getInt("filter_auto_margin")
        showOriginalWaterfall = draft.getBoolean("filter_show_original")
        showFilterWaterfall = draft.getBoolean("filter_show_filter")
        autoRangeDurationSec = draft.getInt("autorange_duration")
        autoRangeMarginDb = draft.getInt("autorange_margin")
        auto = draft.getBoolean("auto"); imageFormat = im
        jpgQuality = draft.getInt("jpg_quality")
        directInputRateHz = rate
        directSmoothingAlpha = draft.getDouble("direct_smoothing_alpha")
        directTimeSmoothing = draft.getBoolean("direct_time_smoothing")
        directPeakMarkers = draft.getInt("direct_peak_markers")
        directSubtractDc = draft.getBoolean("direct_subtract_dc")
        directStayAwake = draft.getBoolean("direct_stay_awake")
        history.clear(); historyEpochMillis.clear(); filterHistory.clear(); pendingFilter = null
        clearStatistics(); current = null; floor = doubleArrayOf()
        autoRange = null; filterAutoRange = null
        waterfallAccumulator.clear(); waterfallPhaseSamples = 0
        waterfallRevision++
        val actual = AnalyzerRuntimeSetupSnapshot.capture(snapshotLocked())
        check(AnalyzerSetupExactComparison.sameComplete(draft, actual)) {
            "Controllerwerte nach JSON-Übernahme nicht identisch"
        }
        } catch (failure: Throwable) {
            // This method is reentrant under the same monitor. Restore the fully validated
            // previous draft, and propagate the error to the transaction coordinator.
            if (rollbackOnFailure) try { applyStoppedJsonDraft(before, false) } catch (restore: Throwable) {
                failure.addSuppressed(restore)
            }
            throw failure
        }
    }

    fun snapshot(): Snapshot = synchronized(lock) {
        if (autoRange != null && System.nanoTime() - autoRangeStartNs > (autoRangeDurationSec + 3L) * 1_000_000_000L) {
            autoRange = null; autoRangeStartNs = 0L
            status = "Autorange abgebrochen: PCM-Quelle unterbrochen"
        }
        if (filterAutoRange != null && System.nanoTime() - filterAutoRangeStartNs >
            (filterAutoRangeDurationSec + 3L) * 1_000_000_000L) {
            filterAutoRange=null;filterAutoRangeStartNs=0L
            status="FILTER-Autorange abgebrochen: PCM-Quelle unterbrochen"
        }
        val snap = snapshotLocked()
        when {
            !snap.running || snap.paused -> snap
            snap.diagnostics == null -> snap.copy(status=if (direct48k) "Warte auf ${directInputRateHz}-Hz-Rohdaten" else "Warte auf 8-kHz-Rohdaten (Rolling-Buffer oder WAV aktivieren)")
            snap.signalAgeMs > 3000 -> snap.copy(status="PCM-Quelle unterbrochen · letzte Rohdaten vor ${snap.signalAgeMs} ms", validSignal=false, l50DbFs=null, l90DbFs=null)
            snap.spectra == 0L -> snap.copy(status="Rohdaten empfangen · FFT-Warmup ca. ${snap.fftSize / snap.sampleRateHz.toDouble()} s")
            !snap.validSignal -> snap.copy(status="Kein verwertbares LF-Nutzsignal · Signaldiagnose prüfen", l50DbFs=null, l90DbFs=null)
            else -> snap
        }
    }

    /** Manual image workflow: freeze the exact displayed data BEFORE the comment dialog opens. */
    fun captureManualImage(): Snapshot? = synchronized(lock) {
        if (current == null || !validSignal || writing.get()) null else snapshotLocked()
    }

    fun saveCapturedImage(snap: Snapshot, comment: String?): Boolean =
        export(snap, manual = true, comment = comment?.take(400)?.takeIf { it.isNotBlank() })

    /** Compatibility entry point for tests/older callers; manual UI uses captureManualImage(). */
    fun saveNow(): Boolean {
        val snap = captureManualImage() ?: return false
        return saveCapturedImage(snap, null)
    }

    private fun export(snap: Snapshot, manual: Boolean = false, comment: String? = null): Boolean {
        val context = appContext ?: return false
        if (!writing.compareAndSet(false, true)) return false
        // Invalidate any previous successful-save status BEFORE announcing a new manual job.
        synchronized(lock) { status = "Bild wird gespeichert …" }
        writer.execute {
            var uri: android.net.Uri? = null
            try {
                val bitmap = renderPng(snap, comment)
                val format = snap.imageFormat
                val compressFormat = if (format == AnalyzerImageFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                val quality = if (format == AnalyzerImageFormat.PNG) 100 else snap.jpgQuality
                val name = "LFN-Scout_${if (snap.testSignal) "TESTSIGNAL" else "Analyzer"}_${SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS", Locale.US).format(Date())}.${format.extension}"
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
                    if (Build.VERSION.SDK_INT >= 29) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/" + PublicDataDirectory.folder)
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }
                // On pre-29 Android use app-specific files rather than request broad storage rights.
                if (Build.VERSION.SDK_INT >= 29) {
                    uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?: error("MediaStore insert returned null")
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        check(bitmap.compress(compressFormat, quality, stream))
                    } ?: error("MediaStore output stream unavailable")
                    context.contentResolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
                } else {
                    val dir = java.io.File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Analyzer")
                    check(dir.exists() || dir.mkdirs())
                    java.io.FileOutputStream(java.io.File(dir, name)).use { stream ->
                        check(bitmap.compress(compressFormat, quality, stream))
                    }
                }
                bitmap.recycle()
                synchronized(lock) {
                    lastSave = name; status = "Bild gespeichert: $name"
                    if (manual) { manualSaveMessage = "Bild gespeichert: ${format.name}"; manualSaveEvent++ }
                }
            } catch (e: Exception) {
                if (uri != null) runCatching { context.contentResolver.delete(uri, null, null) }
                synchronized(lock) {
                    status = "Bildfehler: ${e.message ?: e.javaClass.simpleName}"
                    if (manual) { manualSaveMessage = status; manualSaveEvent++ }
                }
            } finally { writing.set(false) }
        }
        return true
    }

    /** Shared presentation contract: color/range mapping reused by Compose and offscreen PNG. */
    fun color(db: Double, minDb: Int = -120, maxDb: Int = -40): Int {
        val t = ((db - minDb) / (maxDb - minDb)).coerceIn(0.0, 1.0)
        val colors = intArrayOf(0x0D0826, 0x32105A, 0x7D1C6A, 0xBC3754, 0xED692D, 0xFBC84C, 0xFFF1A6)
        val pos = t * (colors.size - 1)
        val i = pos.toInt().coerceIn(0, colors.size - 2)
        val f = pos - i
        val a = colors[i]; val b = colors[i + 1]
        fun channel(shift: Int) = (((a shr shift) and 255) * (1.0-f) + ((b shr shift) and 255)*f).toInt()
        return Color.rgb(channel(16), channel(8), channel(0))
    }

    /** Offscreen export with the same physical axes and time-down waterfall as the on-screen view. */
    private fun renderPng(s: Snapshot, comment: String? = null): Bitmap {
        val w = 1000; val h = 1550
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        c.drawColor(Color.rgb(8, 17, 19))
        fun label(value: String, x: Float, y: Float, size: Float = 24f, color: Int = Color.WHITE,
                  align: Paint.Align = Paint.Align.LEFT) {
            p.style = Paint.Style.FILL; p.color = color; p.textSize = size; p.textAlign = align
            c.drawText(value, x, y, p)
        }
        fun tickHz(): IntArray = AnalyzerTiming.frequencyTicks(s.minFrequencyHz, s.rangeHz)
        val freqSpan = (s.rangeHz - s.minFrequencyHz).toFloat()
        label("LFN-Scout · ${s.minFrequencyHz}–${s.rangeHz} Hz", 30f, 49f, 34f)
        val testSource = s.testSignal
        label(if (testSource) "TESTSIGNAL · KEINE MESSUNG · ${s.sampleRateHz} Hz" else if (s.direct48k)
            "Live · ${s.directInputRateHz} Hz direkt · dBFS · Gain +${s.analysisGainDb} dB uncal" else
            "Live · 8 kHz → ${s.sampleRateHz} Hz FIR · dBFS · Gain +${s.analysisGainDb} dB uncal", 30f, 92f, 22f)
        val left = 124f; val right = 970f; val top = 148f; val specBottom = 484f
        fun xHz(hz: Double): Float = left + ((hz - s.minFrequencyHz) / freqSpan * (right - left)).toFloat()
        label("dBFS", 20f, 134f, 18f)
        for (db in AnalyzerDisplayTicks.db(s.minDisplayDb, s.maxDisplayDb)) {
            val y = specBottom - ((db - s.minDisplayDb).toDouble() / (s.maxDisplayDb-s.minDisplayDb) * (specBottom-top)).toFloat()
            p.color=Color.rgb(50,65,70);p.style=Paint.Style.STROKE;p.strokeWidth=1f
            c.drawLine(left,y,right,y,p)
            p.color=Color.LTGRAY;p.strokeWidth=2f;c.drawLine(left-8f,y,left,y,p)
            label("$db",left-15f,y+7f,20f,Color.LTGRAY,Paint.Align.RIGHT)
        }
        for (hz in tickHz()) {
            val x=xHz(hz.toDouble())
            p.color=Color.rgb(50,65,70);p.style=Paint.Style.STROKE;p.strokeWidth=1f
            c.drawLine(x,top,x,specBottom,p)
            p.color=Color.LTGRAY;p.strokeWidth=2f;c.drawLine(x,specBottom,x,specBottom+8f,p)
            label("$hz",x,specBottom+29f,21f,Color.LTGRAY,Paint.Align.CENTER)
        }
        // Unit shares the frequency-numbers baseline; never needs its own row.
        val unitX = xHz(AnalyzerTiming.unitFrequencyHz(s.minFrequencyHz, s.rangeHz))
        label("Hz", unitX, specBottom + 29f, 20f, Color.LTGRAY, Paint.Align.CENTER)
        // Indicate the actual statistics band; bounds may lie outside the visible spectrum.
        val bandMin = max(s.statisticsMinHz, s.minFrequencyHz)
        val bandMax = min(s.statisticsMaxHz, s.rangeHz)
        if (bandMax > bandMin) {
            p.style = Paint.Style.FILL; p.color = Color.argb(45, 8, 92, 120)
            c.drawRect(xHz(bandMin.toDouble()), top, xHz(bandMax.toDouble()), specBottom, p)
        }
        // Plot curves; selected dBFS range is shared with the UI.
        val minBin = s.frequencyHz.indexOfFirst { it >= s.minFrequencyHz.toDouble() }
        val maxBin = s.frequencyHz.indexOfLast { it <= s.rangeHz.toDouble() }
        fun trace(data: DoubleArray, traceColor: Int) {
            if (data.size <= maxBin || maxBin <= minBin || minBin < 0 || !s.validSignal) return
            val path = Path()
            for (i in minBin..maxBin) {
                val x=xHz(s.frequencyHz[i])
                val y=specBottom-((data[i]-s.minDisplayDb)/(s.maxDisplayDb-s.minDisplayDb)).coerceIn(0.0,1.0).toFloat()*(specBottom-top)
                if(i==minBin) path.moveTo(x,y) else path.lineTo(x,y)
            }
            p.color=traceColor;p.style=Paint.Style.STROKE;p.strokeWidth=1.5f;c.drawPath(path,p)
        }
        if (s.showFloor) trace(s.floor,Color.LTGRAY)
        if (s.showAverage) trace(s.average,Color.RED)
        if (s.showMaxHold) trace(s.maxHold,Color.CYAN)
        if (s.showCurrent) trace(s.live,Color.YELLOW)
        if (s.direct48k && s.directPeakMarkers > 0 && s.validSignal) {
            AnalyzerPeakMarkers.find(s.frequencyHz,s.live,s.minFrequencyHz,s.rangeHz,s.directPeakMarkers)
                .forEachIndexed { index, peak ->
                    val x=xHz(peak.frequencyHz)
                    val y=specBottom-((peak.dbFs-s.minDisplayDb)/(s.maxDisplayDb-s.minDisplayDb)).coerceIn(0.0,1.0).toFloat()*(specBottom-top)
                    p.color=Color.WHITE;p.style=Paint.Style.STROKE;p.strokeWidth=1.5f
                    c.drawLine(x,y-10f,x,y+10f,p)
                    label(String.format(Locale.US,"P%d %.1f Hz %.1f dBFS",index+1,peak.frequencyHz,peak.dbFs),
                        (x+5f).coerceAtMost(right-245f).coerceAtLeast(left), (y-12f).coerceAtLeast(top+19f),17f)
                }
        }
        p.color=Color.rgb(225,65,70);p.style=Paint.Style.STROKE;p.strokeWidth=1.5f
        val statsRange = "${s.statisticsMinHz}-${s.statisticsMaxHz}"
        val leqLabel = "Leq($statsRange): ${s.leqDbFs?.let { String.format(Locale.US, "%.1f",it) } ?: "–"}"
        val l50Label = "L50($statsRange): ${s.l50DbFs?.let { String.format(Locale.US, "%.1f",it) } ?: "–"}"
        val l90Label = "L90($statsRange): ${s.l90DbFs?.let { String.format(Locale.US, "%.1f",it) } ?: "–"}"
        p.textSize = 20f
        val statsWidth = maxOf(p.measureText(leqLabel), p.measureText(l50Label), p.measureText(l90Label)) + 6f
        val statsTop = 158f
        val statsLineH = 22f
        val statsBottom = statsTop + 6f + 3f * statsLineH
        c.drawRoundRect(android.graphics.RectF(right-statsWidth,statsTop,right,statsBottom),10f,10f,p)
        label(leqLabel, right-3f, statsTop+20f, 20f, Color.WHITE, Paint.Align.RIGHT)
        label(l50Label, right-3f, statsTop+20f+statsLineH, 20f, Color.WHITE, Paint.Align.RIGHT)
        label(l90Label, right-3f, statsTop+20f+2f*statsLineH, 20f, Color.WHITE, Paint.Align.RIGHT)
        val wfTop=636f; val wfBottom=1370f
        if(s.validSignal && s.rows.isNotEmpty() && maxBin>0) {
            val width=400; val height=minOf(s.historyCapacity, 1000)
            val pixel=IntArray(width*height) { Color.rgb(19,12,41) }
            val newestFirst = s.rows.takeLast(s.historyCapacity).asReversed()
            val filterFirst = s.filterRowsDbFs.takeLast(s.historyCapacity).asReversed()
            val excessFirst = s.filterRowsExcessDb.takeLast(s.historyCapacity).asReversed()
            for (y in 0 until height) {
                val age = (y.toLong() * s.historyCapacity / height).toInt()
                if (age >= newestFirst.size) continue
                val row = newestFirst[age]
                for(x in 0 until width) {
                    val bin=if (s.direct48k) ((s.minFrequencyHz + x.toDouble()/(width-1)*(s.rangeHz-s.minFrequencyHz))/(s.sampleRateHz/2.0)*row.lastIndex).toInt().coerceIn(0,row.lastIndex)
                        else (minBin + x.toDouble()/(width-1)*(maxBin-minBin)).toInt().coerceIn(0,row.lastIndex)
                    val hz=s.minFrequencyHz + x.toDouble()/(width-1)*(s.rangeHz-s.minFrequencyHz)
                    pixel[y*width+x]=FilterWaterfallRenderer.colorAt(hz,
                        color(row[bin],s.minDisplayDb,s.maxDisplayDb),filterFirst.getOrNull(age),excessFirst.getOrNull(age),
                        s.direct48k || s.showOriginalWaterfall,!s.direct48k && s.showFilterWaterfall,
                        s.filterMinHz,s.filterMaxHz,s.minDisplayDb,s.maxDisplayDb,
                        s.filterDisplayMinDb,s.filterDisplayMaxDb)
                }
            }
            val bitmap=Bitmap.createBitmap(pixel,width,height,Bitmap.Config.ARGB_8888)
            p.style=Paint.Style.FILL;p.color=Color.WHITE;p.isFilterBitmap=true
            c.drawBitmap(bitmap,null,android.graphics.RectF(left,wfTop,right,wfBottom),p)
            bitmap.recycle()
        } else label("Kein LF-Nutzsignal",left+15f,wfTop+50f,25f,Color.YELLOW)
        // The dBFS bar stays on the far left. Timestamp band reuses only the remaining
        // existing axis gutter: the plot starts at exactly the same `left` as the spectrum.
        p.textSize=16f;p.textScaleX=1f
        val barL=10f
        val filterOnly=!s.direct48k && s.showFilterWaterfall && !s.showOriginalWaterfall
        val barMin=if(filterOnly) s.filterDisplayMinDb else s.minDisplayDb
        val barMax=if(filterOnly) s.filterDisplayMaxDb else s.maxDisplayDb
        val barR=barL+AnalyzerDisplayTicks.db(barMin,barMax)
            .maxOf { p.measureText("$it") }+3f
        val timeBandL=barR+4f
        val timeBandR=left-8f
        if (timeBandR>timeBandL) {
            p.color=Color.BLACK;p.style=Paint.Style.FILL
            c.drawRect(timeBandL,wfTop,timeBandR,wfBottom,p)
            val dateFmt=SimpleDateFormat("dd.MM",Locale.getDefault())
            val clockFmt=SimpleDateFormat("HH:mm",Locale.getDefault())
            val textX=(timeBandL+timeBandR)/2f
            val timeSize=16f
            val usedY=mutableListOf<Float>()
            WaterfallTimeAxis.ticks(s.rowEpochMillis,s.historySeconds,s.historyCapacity,s.waterfallIntervalMs)
                .forEach { tick ->
                    val y=wfTop+tick.ageRows/s.historyCapacity.toFloat()*(wfBottom-wfTop)
                    val labelY=if(tick.ageRows==0f) wfTop+22f else y
                    if(labelY-22f<wfTop || labelY+22f>wfBottom || usedY.any { abs(it-labelY)<44f }) return@forEach
                    val date=dateFmt.format(Date(tick.epochMillis))
                    val time=clockFmt.format(Date(tick.epochMillis))
                    p.textSize=timeSize;p.textScaleX=1f
                    val widest=max(p.measureText(date),p.measureText(time))
                    val scale=min(1f,(timeBandR-timeBandL-3f)/widest.coerceAtLeast(1f))
                    p.textScaleX=scale
                    label(date,textX,labelY-3f,timeSize,Color.WHITE,Paint.Align.CENTER)
                    label(time,textX,labelY+15f,timeSize,Color.WHITE,Paint.Align.CENTER)
                    p.textScaleX=1f
                    p.color=Color.WHITE;p.style=Paint.Style.STROKE;p.strokeWidth=2f
                    c.drawLine(left-7f,y,left,y,p)
                    usedY.add(labelY)
                }
            p.textScaleX=1f
        }
        for(hz in tickHz()) {
            val x=xHz(hz.toDouble())
            p.color=Color.LTGRAY;p.style=Paint.Style.STROKE;p.strokeWidth=2f
            c.drawLine(x,wfBottom,x,wfBottom+8f,p)
            label("$hz",x,wfBottom+28f,21f,Color.LTGRAY,Paint.Align.CENTER)
        }
        // dBFS scale is LEFT of the waterfall; numbers start INSIDE the color ramp.
        p.textSize = 16f; p.textScaleX = 1f
        for(i in 0 until 120) {
            val db=barMax-(i+0.5)*(barMax-barMin)/120.0
            p.color=if(filterOnly) FilterWaterfallRenderer.filterColor(db,barMin,barMax)
                else color(db,barMin,barMax)
            p.style=Paint.Style.FILL
            val y=wfTop+i/120f*(wfBottom-wfTop)
            c.drawRect(barL,y,barR,y+(wfBottom-wfTop)/120f+1f,p)
        }
        for(db in AnalyzerDisplayTicks.db(barMin,barMax).asReversed()) {
            val y=wfTop+(barMax-db).toFloat()/(barMax-barMin)*(wfBottom-wfTop)
            p.color=Color.BLACK;p.style=Paint.Style.STROKE;p.strokeWidth=2f
            c.drawLine(barL,y,barL+7f,y,p)
            val foreground = if ((db-barMin).toFloat()/(barMax-barMin)>0.58f) Color.BLACK else Color.WHITE
            label("$db",barL+1f,(y+6f).coerceIn(wfTop+17f,wfBottom-4f),16f,foreground)
        }
        label("Hz",unitX,wfBottom+28f,20f,Color.LTGRAY,Paint.Align.CENTER)
        if(!s.direct48k && s.showFilterWaterfall)
            label(if(s.showOriginalWaterfall) "FILTER ΔdB (3…18): Magenta-Overlay" else "FILTER: dBFS (Magenta–Weiß)",
                left,wfTop-8f,16f,Color.MAGENTA)
        s.modeBoundaryRows?.let { age ->
            val y = wfTop + age.toFloat() / s.historyCapacity * (wfBottom - wfTop)
            if (y in wfTop..wfBottom) {
                p.color = Color.CYAN; p.style = Paint.Style.STROKE; p.strokeWidth = 2f
                c.drawLine(left, y, right, y, p)
            }
        }
        s.cursorFrequencyHz?.takeIf { it >= s.minFrequencyHz && it <= s.rangeHz }?.let { hz ->
            val x=xHz(hz)
            p.style=Paint.Style.STROKE; p.color=Color.rgb(48,215,239); p.strokeWidth=1.5f
            // Same exact x-coordinate through spectrum and down to the last waterfall pixel.
            c.drawLine(x,top,x,specBottom,p)
            c.drawLine(x,wfTop,x,wfBottom,p)
            label(String.format(Locale.US,"%.1f Hz",hz),
                (x+6f).coerceAtMost(right-160f),top+32f,19f,Color.CYAN)
        }
        comment?.takeIf { it.isNotBlank() }?.let { raw ->
            val normalized = ("Kommentar: " + raw.replace(Regex("\\s+"), " ").trim()).take(420)
            val maxWidth = right - left - 24f
            p.textSize = 20f; p.textScaleX = 1f
            val words = normalized.split(" ")
            val lines = mutableListOf<String>()
            var currentLine = ""
            for (word in words) {
                val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (p.measureText(candidate) <= maxWidth) currentLine = candidate
                else {
                    if (currentLine.isNotEmpty()) lines.add(currentLine)
                    currentLine = word
                    if (lines.size == 4) break
                }
            }
            if (lines.size < 4 && currentLine.isNotEmpty()) lines.add(currentLine)
            val shown = lines.take(4).toMutableList()
            if (shown.isNotEmpty() && normalized.length > shown.joinToString(" ").length)
                shown[shown.lastIndex] = shown.last().take(80).trimEnd() + " …"

            // No background panel: comment is painted directly into the frozen waterfall,
            // bottom-right aligned. A small text shadow keeps it legible without a black box.
            val lineH = 24f
            val lastBaseline = wfBottom - 12f
            val firstBaseline = lastBaseline - (shown.size - 1) * lineH
            p.style = Paint.Style.FILL
            p.color = Color.WHITE
            p.textSize = 20f
            p.textAlign = Paint.Align.RIGHT
            p.setShadowLayer(3f, 1f, 1f, Color.BLACK)
            shown.forEachIndexed { i, line ->
                c.drawText(line, right - 12f, firstBaseline + i * lineH, p)
            }
            p.clearShadowLayer()
            p.textAlign = Paint.Align.LEFT
        }
        label("${s.imageFormat.name} ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())} · Wasserfall ${s.waterfallMode.label} (${when(s.waterfallMode) { WaterfallMode.CURRENT -> "Akt"; WaterfallMode.AVERAGE -> "MW"; WaterfallMode.MAXIMUM -> "MAX" }}) · ${s.historySeconds} s · ${s.rows.size}/${s.historyCapacity} Zeilen · Statistik ${s.statisticsMinHz}–${s.statisticsMaxHz} Hz",left,1500f,20f)
        return out
    }
}
