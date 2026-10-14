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
        "filter_display_max", "filter_auto_duration", "filter_auto_margin"
    )
    private val requiresStop = setOf(
        "direct_sample_rate", "direct_audio_source", "decimation_factor", "fft_size",
        "fft_hop", "window", "min_frequency", "range", "filter_min_hz",
        "filter_max_hz", "filter_bands", "history_seconds", "water_interval"
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
            val equal = if (a is Number && b is Number) a.toDouble() == b.toDouble() else a == b
            if (equal) continue
            changed += key
            when (key) {
                in displayOnly -> display += key
                in requiresStop -> stopped += key
                else -> unsupported += key
            }
        }
        return Report(display.sorted(), stopped.sorted(), unsupported.sorted(), missing.sorted(),
            changed.sorted(), running, recording)
    }
}
