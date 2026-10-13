package de.brummbeobachter.app

import android.content.Context
import org.json.JSONObject

/**
 * Phase 3I: conservative, explicit runtime-only preview for DISPLAY controls.
 * This adapter neither writes SharedPreferences nor the Analyzer setup JSON.
 * Do NOT use for audio, DSP geometry, recording, calibration or MON.
 * Call from UI only with explicit user intent; not during initial composition.
 */
internal object AnalyzerDisplayDraftApplier {
    private val supported = setOf(
        "display_min", "display_max", "autorange_duration", "autorange_margin",
        "current", "average", "max_hold", "floor",
        "filter_display_min", "filter_display_max", "filter_auto_duration", "filter_auto_margin"
    )

    data class Result(val appliedKeys: List<String>, val excludedKeys: List<String>)

    @Synchronized
    fun apply(context: Context, legacy: JSONObject): Result {
        AnalyzerSetupFormat.validate(JSONObject().put("legacy", legacy))
        val current = LiveAnalyzerController.snapshot()
        val keys = mutableSetOf<String>()
        val it = legacy.keys()
        while (it.hasNext()) keys.add(it.next())
        val selected = keys.intersect(supported)
        require(selected.isNotEmpty()) { "Kein sicher anwendbarer Anzeigeparameter vorhanden" }
        require(selected.none { legacy.isNull(it) }) { "Anzeigeparameter darf nicht null sein" }
        val filtered = selected.any { it.startsWith("filter_") }
        check(!filtered || !current.direct48k) { "FILTER-Anzeige nicht im 48-kHz-Modus aktivieren" }

        fun intValue(key: String, default: Int): Int = if (key in selected) legacy.getInt(key) else default
        fun boolValue(key: String, default: Boolean): Boolean = if (key in selected) legacy.getBoolean(key) else default
        val dMin = intValue("display_min", current.minDisplayDb)
        val dMax = intValue("display_max", current.maxDisplayDb)
        val duration = intValue("autorange_duration", current.autoRangeDurationSec)
        val margin = intValue("autorange_margin", current.autoRangeMarginDb)
        val fMin = intValue("filter_display_min", current.filterDisplayMinDb)
        val fMax = intValue("filter_display_max", current.filterDisplayMaxDb)
        val fDuration = intValue("filter_auto_duration", current.filterAutoRangeDurationSec)
        val fMargin = intValue("filter_auto_margin", current.filterAutoRangeMarginDb)
        // Full preflight BEFORE the first mutation, including existing counterpart values.
        fun validRange(lo: Int, hi: Int) = lo in -240..40 && hi in -230..50 && hi - lo >= 10
        require(validRange(dMin, dMax)) { "Ungültige ORIGINAL-Anzeigeskala" }
        require(duration in 1..30 && margin in 0..30) { "Ungültiges ORIGINAL-Autorange" }
        if (filtered) {
            require(validRange(fMin, fMax)) { "Ungültige FILTER-Anzeigeskala" }
            require(fDuration in 1..30 && fMargin in 0..30) { "Ungültiges FILTER-Autorange" }
        }
        val showCurrent = boolValue("current", current.showCurrent)
        val showAverage = boolValue("average", current.showAverage)
        val showMax = boolValue("max_hold", current.showMaxHold)
        val showFloor = boolValue("floor", current.showFloor)

        // Calls are safe and non-persisting by construction; restore runtime display
        // if any unforeseen failure occurs. Rollback also never writes old preferences.
        try {
            if ("display_min" in selected || "display_max" in selected)
                LiveAnalyzerController.setDisplayRange(context, dMin, dMax, persist = false)
            if ("autorange_duration" in selected || "autorange_margin" in selected)
                LiveAnalyzerController.setAutoRangeSettings(context, duration, margin, persist = false)
            if (selected.any { it in setOf("current", "average", "max_hold") })
                LiveAnalyzerController.setSpectrumVisibility(context, showCurrent, showAverage, showMax, persist = false)
            if ("floor" in selected)
                LiveAnalyzerController.setTraceVisibility(context, showAverage, showFloor, persist = false)
            if ("filter_display_min" in selected || "filter_display_max" in selected)
                LiveAnalyzerController.setFilterDisplayRange(context, fMin, fMax, persist = false)
            if ("filter_auto_duration" in selected || "filter_auto_margin" in selected)
                LiveAnalyzerController.setFilterAutoRangeSettings(context, fDuration, fMargin, persist = false)
        } catch (error: Exception) {
            runCatching {
                LiveAnalyzerController.setDisplayRange(context, current.minDisplayDb, current.maxDisplayDb, persist = false)
                LiveAnalyzerController.setAutoRangeSettings(context, current.autoRangeDurationSec, current.autoRangeMarginDb, persist = false)
                LiveAnalyzerController.setSpectrumVisibility(context, current.showCurrent, current.showAverage, current.showMaxHold, persist = false)
                LiveAnalyzerController.setTraceVisibility(context, current.showAverage, current.showFloor, persist = false)
                if (!current.direct48k) {
                    LiveAnalyzerController.setFilterDisplayRange(context, current.filterDisplayMinDb, current.filterDisplayMaxDb, persist = false)
                    LiveAnalyzerController.setFilterAutoRangeSettings(context, current.filterAutoRangeDurationSec, current.filterAutoRangeMarginDb, persist = false)
                }
            }
            throw error
        }
        return Result(selected.sorted(), (keys - supported).sorted())
    }
}
