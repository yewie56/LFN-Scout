package de.brummbeobachter.app

/** Pure display formatting for the transient two-line waterfall notification.
 * Never call this from the audio thread; the Compose caller debounces by a display-only key.
 */
internal object AnalyzerWaterfallNotice {
    internal data class Content(val first: String, val second: String, val isFilter: Boolean)

    fun content(
        mode: String, historySeconds: Int, sampleRateHz: Int, fftHopSamples: Int,
        waterfallIntervalMs: Int, direct: Boolean, original: Boolean, filter: Boolean,
        filterBands: Int, filterMinHz: Double, filterMaxHz: Double,
        filterAutoRangeActive: Boolean
    ): Content {
        val shortMode = when (mode) {
            "CURRENT" -> "Akt"; "AVERAGE" -> "MW"; "MAXIMUM" -> "MAX"; else -> mode
        }
        val first = "Wasserfall $shortMode · ${historySeconds}s · ${sampleRateHz}Hz · Hop $fftHopSamples"
        val filterVisible = !direct && filter
        val second = when {
            filterVisible && filterAutoRangeActive -> "FILTER: Pegelautomatik läuft"
            filterVisible -> {
                val state = if (original) "Overlay" else "Band-RMS dBFS"
                "FILTER: $filterBands Bänder · ${filterMinHz.toInt()}–${filterMaxHz.toInt()}Hz · $state"
            }
            direct || original -> "${waterfallIntervalMs}ms/Zeile · ORIGINAL"
            else -> "${waterfallIntervalMs}ms/Zeile · Wasserfall AUS"
        }
        return Content(first, second, filterVisible)
    }
}
