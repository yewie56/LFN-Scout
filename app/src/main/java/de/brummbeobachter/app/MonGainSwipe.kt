package de.brummbeobachter.app

/** Shared gain-gesture mapping: upward drag raises MON level. */
internal object MonGainSwipe {
    fun nextLevel(currentDb: Double, dragDeltaYPx: Double, waterfallHeightPx: Double, maxGainDb: Double = AnalyzerMonitor.DEFAULT_MAX_GAIN_DB): Double {
        if (!waterfallHeightPx.isFinite() || waterfallHeightPx <= 0.0 || !dragDeltaYPx.isFinite()) return currentDb.coerceIn(AnalyzerMonitor.MIN_GAIN_DB, maxGainDb)
        return (currentDb - dragDeltaYPx / waterfallHeightPx * 180.0).coerceIn(AnalyzerMonitor.MIN_GAIN_DB, maxGainDb)
    }
}
