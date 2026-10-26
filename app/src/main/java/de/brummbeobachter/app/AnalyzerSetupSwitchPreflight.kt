package de.brummbeobachter.app

import org.json.JSONObject

/** Phase 3M: read-only risk assessment, NOT a runtime setup applicator.
 * Missing/null values are never synthesized, applied or treated as defaults.
 * The caller must separately enforce recording guards at actual execution time.
 */
internal object AnalyzerSetupSwitchPreflight {
    private val displayOnly = setOf(
        "display_min", "display_max", "autorange_duration", "autorange_margin",
        "current", "average", "max_hold", "floor", "filter_display_min",
        "filter_display_max", "filter_auto_duration", "filter_auto_margin",
        "filter_impulse_mode", "filter_impulse_threshold"
    )
    private val requiresStop = setOf(
        "direct_sample_rate", "direct_audio_source", "decimation_factor", "fft_size",
        "fft_hop", "window", "min_frequency", "range", "filter_min_hz",
        "filter_max_hz", "filter_bands", "history_seconds", "water_interval"
    )

    private val stoppedDraftFields = setOf(
        "smoothing_enabled", "smoothing_time", "gain", "stat_min", "stat_max",
        "stat_window", "stat_average", "filter_show_original", "filter_show_filter",
        "auto", "image_format", "jpg_quality", "direct_smoothing_alpha",
        "direct_time_smoothing", "direct_peak_markers", "direct_subtract_dc", "direct_stay_awake",
        "water_mode"
    )

    data class Report(
        val displayPreviewable: List<String>,
        val stopRequired: List<String>,
        val unsupported: List<String>,
        val missingOrNull: List<String>,
        val changed: List<String>,
        val measurementRunning: Boolean,
        val recordingActive: Boolean
    ) {
        val canStartDisplayPreview: Boolean get() = !recordingActive && displayPreviewable.isNotEmpty()
        val canApplyCompleteSetup: Boolean get() = false // deliberate until transactional applicator exists
        val requiresStopAndRestart: Boolean get() = stopRequired.isNotEmpty()
    }

    fun assess(saved: JSONObject, live: JSONObject, running: Boolean, recording: Boolean): Report {
        AnalyzerSetupFormat.validate(JSONObject().put("legacy", saved))
        AnalyzerSetupFormat.validate(JSONObject().put("legacy", live))
        val display = mutableListOf<String>()
        val stopped = mutableListOf<String>()
        val unsupported = mutableListOf<String>()
        val missing = mutableListOf<String>()
        val changed = mutableListOf<String>()
        val iterator = saved.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            if (saved.isNull(key) || !live.has(key) || live.isNull(key)) {
                missing += key
                continue
            }
            val a = saved.get(key)
            val b = live.get(key)
            val equal = AnalyzerSetupExactComparison.same(a, b)
            if (equal) continue
            changed += key
            when (key) {
                in displayOnly -> display += key
                in requiresStop, in stoppedDraftFields -> stopped += key
                else -> unsupported += key
            }
        }
        // An extra live key is not silently discarded. It makes a full-state
        // no-op or a complete transactional switch unverifiable.
        for (key in AnalyzerSetupExactComparison.keys(live) - AnalyzerSetupExactComparison.keys(saved)) {
            missing += key
        }
        return Report(display.sorted(), stopped.sorted(), unsupported.sorted(), missing.sorted(),
            changed.sorted(), running, recording)
    }
}
