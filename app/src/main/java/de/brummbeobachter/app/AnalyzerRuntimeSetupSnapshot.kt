package de.brummbeobachter.app

import org.json.JSONObject

/**
 * Phase 3H: read-only snapshot of actual Analyzer controller settings.
 * This is deliberately not a restore/apply path: several controller setters persist
 * immediately, so applying a JSON setup requires a separate transaction strategy.
 * Fields without a controller source are OMITTED rather than guessed from defaults.
 */
internal object AnalyzerRuntimeSetupSnapshot {
    fun capture(state: LiveAnalyzerController.Snapshot): JSONObject = JSONObject().apply {
        put("min_frequency", state.minFrequencyHz)
        put("range", state.rangeHz)
        put("decimation_factor", state.decimationFactor)
        put("fft_size", state.fftSize)
        put("fft_hop", state.fftHopSamples)
        put("window", state.windowType.name)
        put("smoothing_enabled", state.smoothingEnabled)
        put("smoothing_time", state.smoothingTimeSeconds)
        put("current", state.showCurrent)
        put("average", state.showAverage)
        put("floor", state.showFloor)
        put("max_hold", state.showMaxHold)
        put("gain", state.analysisGainDb)
        put("display_min", state.minDisplayDb)
        put("display_max", state.maxDisplayDb)
        put("history_seconds", state.historySeconds)
        put("water_interval", state.waterfallIntervalMs)
        put("water_mode", state.waterfallMode.name)
        put("stat_min", state.statisticsMinHz)
        put("stat_max", state.statisticsMaxHz)
        put("stat_window", state.statisticsWindowSec)
        put("stat_average", state.statisticsAverageSec)
        put("filter_min_hz", state.filterMinHz.toInt())
        put("filter_max_hz", state.filterMaxHz.toInt())
        put("filter_bands", state.filterBandCount)
        put("filter_display_min", state.filterDisplayMinDb)
        put("filter_display_max", state.filterDisplayMaxDb)
        put("filter_auto_duration", state.filterAutoRangeDurationSec)
        put("filter_auto_margin", state.filterAutoRangeMarginDb)
        put("filter_show_original", state.showOriginalWaterfall)
        put("filter_show_filter", state.showFilterWaterfall)
        put("autorange_duration", state.autoRangeDurationSec)
        put("autorange_margin", state.autoRangeMarginDb)
        put("auto", state.autoSave)
        put("image_format", state.imageFormat.name)
        put("jpg_quality", state.jpgQuality)
        put("direct_sample_rate", state.directInputRateHz)
        put("direct_smoothing_alpha", state.directSmoothingAlpha)
        put("direct_time_smoothing", state.directTimeSmoothing)
        put("direct_peak_markers", state.directPeakMarkers)
        put("direct_subtract_dc", state.directSubtractDc)
        put("direct_stay_awake", state.directStayAwake)
    }.also { AnalyzerSetupFormat.validate(JSONObject().put("legacy", it)) }

    /** Compare only fields that are present and non-null in both datasets.
     * A missing saved value remains unknown, never interpreted as an instruction.
     */
    fun compare(saved: JSONObject, current: JSONObject): Comparison {
        AnalyzerSetupFormat.validate(JSONObject().put("legacy", current))
        val changed = mutableListOf<String>()
        val unchanged = mutableListOf<String>()
        val notComparable = mutableListOf<String>()
        val iterator = saved.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            require(AnalyzerSetupFormat.isLegacyField(key)) { "Unknown field: $key" }
            if (saved.isNull(key) || !current.has(key) || current.isNull(key)) {
                notComparable.add(key)
            } else {
                val a = saved.get(key)
                val b = current.get(key)
                val same = if (a is Number && b is Number) a.toDouble() == b.toDouble() else a == b
                if (same) unchanged.add(key) else changed.add(key)
            }
        }
        return Comparison(changed.sorted(), unchanged.sorted(), notComparable.sorted())
    }

    data class Comparison(val changed: List<String>, val unchanged: List<String>, val notComparable: List<String>)
}
