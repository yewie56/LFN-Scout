package de.brummbeobachter.app

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object SeismicPlotUiPolicy {
    const val TRACE_STROKE_WIDTH_PX = 1.6f
    const val TRACE_DENSITY_FACTOR = 1.35f
    const val HELICORDER_ROW_HEIGHT_DP = 11f
    const val LIVE_ASPECT_RATIO = 16f / 9f
    const val TIME_LABEL_FONT_SP = 4f
    const val LIVE_X_AXIS_FONT_SP = 6f
    const val MIN_ZOOM = 1f
    const val MAX_ZOOM = 5f
    const val LEGEND_VALUE_NM_PER_SEC = 40000.0
    const val DEFAULT_NEAR_PLOT_GAIN = 2
    const val MIN_NEAR_PLOT_GAIN = 1
    const val MAX_NEAR_PLOT_GAIN = 20
    const val EXPORT_WIDTH_PX = 1536
    const val EXPORT_HEIGHT_PX = 864
    const val EXPORT_SUPERSAMPLE = 4
    const val MASTER_WIDTH_PX = 6144
    const val MASTER_HEIGHT_PX = 3456
    const val SCHELI_ROWS = 48
    const val SCHELI_ROW_SECONDS = 1800

    fun clampZoom(value: Float): Float = value.coerceIn(MIN_ZOOM, MAX_ZOOM)

    fun normalizeNearPlotGain(value: Int): Int = value.coerceIn(MIN_NEAR_PLOT_GAIN, MAX_NEAR_PLOT_GAIN)

    fun exportFileName(
        date: LocalDate,
        network: String,
        station: String,
        channel: String,
        sourceKind: String,
        savedAt: LocalDateTime = LocalDateTime.now()
    ): String {
        val sourcePart = if (sourceKind == "HLNUG_WBBU_IMAGE") {
            "HS-WBBU"
        } else {
            listOf(network, station, channel)
                .filter { it.isNotBlank() }
                .joinToString("-")
        }
        val safe = sourcePart.replace(Regex("[^A-Za-z0-9._-]+"), "-")
        val timePart = savedAt.format(DateTimeFormatter.ofPattern("HH-mm-ss"))
        return "Seismik_${date}_${timePart}_$safe.png"
    }
}


object SeismicScheliPolicy {
    /**
     * Builds one continuous, peak-preserving trace from envelope bins.
     * Every bin contributes four temporally meaningful values: the first
     * sample, both extrema in their real time order, and the last sample.
     * This preserves substantially more waveform structure than the former
     * two-extrema approximation while still keeping the rendering bounded.
     */
    fun orderedTraceValues(bins: List<SeismicEnvelopeBin>): FloatArray {
        if (bins.isEmpty()) return FloatArray(0)
        val out = FloatArray(bins.size * 4)
        var write = 0
        for (bin in bins) {
            out[write++] = bin.first
            if (bin.minBeforeMax) {
                out[write++] = bin.min
                out[write++] = bin.max
            } else {
                out[write++] = bin.max
                out[write++] = bin.min
            }
            out[write++] = bin.last
        }
        return out
    }
}
