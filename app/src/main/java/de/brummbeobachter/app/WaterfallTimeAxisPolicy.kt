package de.brummbeobachter.app

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object WaterfallTimeAxisPolicy {
    const val LABEL_COUNT = 5
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    fun labels(
        startEpochMs: Long,
        durationSeconds: Double,
        zoneId: ZoneId = ZoneId.systemDefault(),
        count: Int = LABEL_COUNT
    ): List<String> {
        require(count >= 2)
        val safeDuration = durationSeconds.coerceAtLeast(0.0)
        return (0 until count).map { index ->
            val fraction = index.toDouble() / (count - 1).toDouble()
            val epochMs = startEpochMs + (fraction * safeDuration * 1000.0).toLong()
            Instant.ofEpochMilli(epochMs).atZone(zoneId).format(formatter)
        }
    }
}
