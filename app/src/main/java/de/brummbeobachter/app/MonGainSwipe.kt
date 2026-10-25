package de.brummbeobachter.app

/** Shared gain-gesture mapping: upward drag raises MON level. */
internal object MonGainSwipe {
    fun nextLevel(currentDb: Double, dragDeltaYPx: Double, waterfallHeightPx: Double): Double {
        if (!waterfallHeightPx.isFinite() || waterfallHeightPx <= 0.0 || !dragDeltaYPx.isFinite()) return currentDb.coerceIn(-60.0, 140.0)
        return (currentDb - dragDeltaYPx / waterfallHeightPx * 180.0).coerceIn(-60.0, 140.0)
    }
}
