package de.brummbeobachter.app

import org.json.JSONObject

/** Phase 3B format validation. Missing values remain missing; null remains null. */
internal object AnalyzerSetupFormat {
    private val allowedLegacy = setOf(

        "min_frequency", "range", "decimation_factor", "fft_size", "fft_hop", "window",
        "smoothing_enabled", "smoothing_time", "direct_smoothing_alpha", "direct_time_smoothing",
        "direct_peak_markers", "current", "average", "floor", "max_hold", "direct_audio_source",
        "direct_sample_rate", "direct_stay_awake", "direct_subtract_dc", "direct_test1_on",
        "direct_test1_hz", "direct_test2_on", "direct_test2_hz", "gain", "display_min",
        "display_max", "history_seconds", "water_interval", "water_mode", "stat_min", "stat_max",
        "stat_window", "stat_average", "filter_min_hz", "filter_max_hz", "filter_bands",
        "filter_display_min", "filter_display_max", "filter_auto_duration", "filter_auto_margin",
        "filter_show_original", "filter_show_filter", "autorange_duration", "autorange_margin",
        "auto", "image_format", "jpg_quality"
    )
    fun isLegacyField(field: String): Boolean = field in allowedLegacy
    private val integerFields = setOf(

        "min_frequency", "range", "decimation_factor", "fft_size", "fft_hop", "direct_peak_markers",
        "direct_sample_rate", "gain", "display_min", "display_max", "history_seconds",
        "water_interval", "stat_min", "stat_max", "stat_window", "filter_min_hz", "filter_max_hz",
        "filter_bands", "filter_display_min", "filter_display_max", "filter_auto_duration",
        "filter_auto_margin", "autorange_duration", "autorange_margin", "jpg_quality"
    )
    private val floatFields = setOf(
"smoothing_time", "direct_smoothing_alpha", "direct_test1_hz",
        "direct_test2_hz", "stat_average"
    )
    private val booleanFields = setOf(
"smoothing_enabled", "direct_time_smoothing", "current",
        "average", "floor", "max_hold", "direct_stay_awake", "direct_subtract_dc",
        "direct_test1_on", "direct_test2_on", "filter_show_original", "filter_show_filter", "auto"
    )
    private val stringFields = allowedLegacy - integerFields - floatFields - booleanFields
    private val monitorNumbers = setOf(
        "center", "level", "width", "shift", "comp_thr", "comp_ratio",
        "comp_attack", "comp_release", "comp_knee", "comp_makeup",
        "limit_ceiling", "limit_attack", "limit_release", "fade"
    )
    private val monitorBooleans = setOf("comp_on", "limit_on")
    private fun keys(o: JSONObject): Set<String> = buildSet { val it = o.keys(); while (it.hasNext()) add(it.next()) }
    private fun numeric(value: Any): Double {
        require(value is Number) { "Expected numeric value" }
        return value.toDouble().also { require(it.isFinite()) { "Invalid numeric value" } }
    }
    private fun validateLegacy(o: JSONObject) {
        require(keys(o).all { it in allowedLegacy }) { "Unknown legacy field" }
        for (key in keys(o)) {
            val value = o.get(key)
            if (value == JSONObject.NULL) continue
            when (key) {
                in integerFields -> {
                    val n = numeric(value)
                    require(n >= Int.MIN_VALUE && n <= Int.MAX_VALUE && n == n.toInt().toDouble()) { "Invalid integer: $key" }
                }
                in floatFields -> require(numeric(value) in -1e6..1e6) { "Invalid float: $key" }
                in booleanFields -> require(value is Boolean) { "Invalid boolean: $key" }
                in stringFields -> require(value is String && value.length <= 1024) { "Invalid string: $key" }
            }
        }
    }
    private fun validateMonitor(o: JSONObject) {
        require(keys(o).all { it in monitorNumbers || it in monitorBooleans }) { "Unknown MON field" }
        for (key in keys(o)) {
            val value = o.get(key)
            if (value == JSONObject.NULL) continue
            if (key in monitorBooleans) require(value is Boolean) { "Invalid MON boolean: $key" }
            else require(numeric(value) in -1e6..1e6) { "Invalid MON number: $key" }
        }
    }
    fun validate(parameters: JSONObject) {
        val names = keys(parameters)
        require(names.isNotEmpty() && names.all { it in setOf("legacy", "monitor", "display", "provenance") }) {
            "Unsupported or empty parameter object"
        }
        for (key in names) require(parameters.get(key) is JSONObject) { "Invalid parameter group: $key" }
        parameters.optJSONObject("legacy")?.let(::validateLegacy)
        parameters.optJSONObject("monitor")?.let(::validateMonitor)
        parameters.optJSONObject("display")?.let {
            require(keys(it).all { key -> key in setOf("waterfallExpanded", "visibleCurves", "audioDeviceId") })
            if (it.has("waterfallExpanded") && !it.isNull("waterfallExpanded")) require(it.get("waterfallExpanded") is Boolean)
            if (it.has("audioDeviceId") && !it.isNull("audioDeviceId")) require(it.get("audioDeviceId") is String)
            if (it.has("visibleCurves") && !it.isNull("visibleCurves")) require(it.get("visibleCurves") is JSONObject)
        }
        parameters.optJSONObject("provenance")?.let {
            require(keys(it).all { key -> key in setOf("sourceSlot", "mode", "sourceStore", "missingFields", "notApplicableFields") })
            if (it.has("sourceSlot")) require(it.get("sourceSlot") is Int && it.getInt("sourceSlot") in 1..4)
            if (it.has("mode")) require(it.get("mode") in listOf("8K", "DIRECT"))
            if (it.has("sourceStore")) require(it.get("sourceStore") == "lfn_scout_live_analyzer")
            for (arrayKey in listOf("missingFields", "notApplicableFields")) {
                if (it.has(arrayKey)) {
                    val arr = it.get(arrayKey)
                    require(arr is org.json.JSONArray) { "Invalid provenance array: $arrayKey" }
                    for (i in 0 until arr.length()) require(arr.get(i) is String && isLegacyField(arr.getString(i))) {
                        "Invalid provenance entry: $arrayKey"
                    }
                }
            }
        }
        require((parameters.optJSONObject("legacy")?.length() ?: 0) +
            (parameters.optJSONObject("monitor")?.length() ?: 0) +
            (parameters.optJSONObject("display")?.length() ?: 0) > 0) { "No actual parameter data" }
    }
}
