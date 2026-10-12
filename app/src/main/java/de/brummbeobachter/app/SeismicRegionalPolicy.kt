package de.brummbeobachter.app

object SeismicRegionalPolicy {
    const val WBBU_LATITUDE = 49.62294
    const val WBBU_LONGITUDE = 8.49384
    const val SPECIAL_RADIUS_KM = 30.0

    fun isBuerstadtSpecialZone(latitude: Double, longitude: Double): Boolean =
        SeismicStationPolicy.haversineKm(latitude, longitude, WBBU_LATITUDE, WBBU_LONGITUDE) <= SPECIAL_RADIUS_KM
}

data class SeismicHelicorderRow(
    val startMinuteOfDay: Int,
    val bins: List<SeismicEnvelopeBin>,
    val usePrimaryColor: Boolean
)

object SeismicHelicorderPolicy {
    private const val ROW_MINUTES = 30
    private const val ROW_COUNT = 48

    fun buildRows(
        samples: IntArray,
        sampleRateHz: Double,
        traceStartEpochMs: Long,
        requestedStartEpochMs: Long,
        requestedEndEpochMs: Long,
        binsPerRow: Int = 240,
        valueScale: Double? = null
    ): List<SeismicHelicorderRow> {
        if (sampleRateHz <= 0.0 || binsPerRow <= 0) {
            return List(ROW_COUNT) { row -> SeismicHelicorderRow(row * ROW_MINUTES, emptyList(), row % 2 == 0) }
        }
        val mean = if (samples.isEmpty()) 0.0 else samples.fold(0.0) { acc, v -> acc + v } / samples.size.toDouble()
        return List(ROW_COUNT) { row ->
            val rowStartMs = requestedStartEpochMs + row * ROW_MINUTES * 60_000L
            val rowEndMs = rowStartMs + ROW_MINUTES * 60_000L
            val effectiveStart = maxOf(rowStartMs, traceStartEpochMs)
            val traceEndMs = traceStartEpochMs + ((samples.size / sampleRateHz) * 1000.0).toLong()
            val effectiveEnd = minOf(rowEndMs, requestedEndEpochMs, traceEndMs)
            val bins = if (effectiveEnd <= effectiveStart || samples.isEmpty()) {
                emptyList()
            } else {
                val first = (((effectiveStart - traceStartEpochMs) / 1000.0) * sampleRateHz).toInt().coerceIn(0, samples.size)
                val last = (((effectiveEnd - traceStartEpochMs) / 1000.0) * sampleRateHz).toInt().coerceIn(first, samples.size)
                buildBins(samples, first, last, binsPerRow, mean, valueScale ?: 1.0)
            }
            SeismicHelicorderRow(row * ROW_MINUTES, bins, row % 2 == 0)
        }
    }

    private fun buildBins(samples: IntArray, first: Int, last: Int, maxBins: Int, mean: Double, valueScale: Double): List<SeismicEnvelopeBin> {
        val count = last - first
        if (count <= 0) return emptyList()
        val binCount = minOf(maxBins, count)
        return List(binCount) { bin ->
            val start = first + (bin.toLong() * count / binCount).toInt()
            val end = first + (((bin + 1L) * count / binCount).toInt()).coerceAtLeast((start - first) + 1)
            var minV = Double.POSITIVE_INFINITY
            var maxV = Double.NEGATIVE_INFINITY
            var minIndex = start
            var maxIndex = start
            val actualEnd = minOf(end, last)
            val firstValue = (samples[start] - mean) * valueScale
            var lastValue = firstValue
            for (i in start until actualEnd) {
                val c = (samples[i] - mean) * valueScale
                if (c < minV) {
                    minV = c
                    minIndex = i
                }
                if (c > maxV) {
                    maxV = c
                    maxIndex = i
                }
                lastValue = c
            }
            SeismicEnvelopeBin(
                min = minV.toFloat(),
                max = maxV.toFloat(),
                first = firstValue.toFloat(),
                last = lastValue.toFloat(),
                minBeforeMax = minIndex <= maxIndex
            )
        }
    }

    fun maxAbs(rows: List<SeismicHelicorderRow>): Double = rows.asSequence()
        .flatMap { it.bins.asSequence() }
        .fold(0.0) { acc, bin -> maxOf(acc, kotlin.math.abs(bin.min.toDouble()), kotlin.math.abs(bin.max.toDouble())) }
}
