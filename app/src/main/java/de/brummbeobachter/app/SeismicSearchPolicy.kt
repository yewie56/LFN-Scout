package de.brummbeobachter.app

import java.time.Instant

data class SeismicMetadataSource(
    val name: String,
    val url: String
)

data class SeismicProbeWindow(
    val start: Instant,
    val end: Instant
)

object SeismicSearchPolicy {
    private const val PROBE_SECONDS = 120L
    private const val FRESH_DATA_LAG_SECONDS = 300L

    fun directMetadataSources(): List<SeismicMetadataSource> = listOf(
        SeismicMetadataSource(
            name = "BGR",
            url = "https://eida.bgr.de/fdsnws/station/1/query"
        ),
        SeismicMetadataSource(
            name = "GEOFON",
            url = "https://geofon.gfz.de/fdsnws/station/1/query"
        )
    )

    fun mergeChannels(channels: Iterable<SeismicChannel>): List<SeismicChannel> =
        channels
            .distinctBy {
                listOf(
                    it.station.network.uppercase(),
                    it.station.station.uppercase(),
                    it.location.uppercase(),
                    it.channel.uppercase()
                )
            }
            .sortedWith(
                compareBy<SeismicChannel> { it.station.distanceKm }
                    .thenBy { SeismicStationPolicy.channelPriority(it.channel) }
                    .thenByDescending { it.sampleRateHz ?: 0.0 }
            )

    fun probeWindow(start: Instant, end: Instant): SeismicProbeWindow {
        require(end.isAfter(start)) { "Probe end must be after start" }
        val totalSeconds = end.epochSecond - start.epochSecond
        val lagSeconds = if (totalSeconds > FRESH_DATA_LAG_SECONDS + PROBE_SECONDS) {
            FRESH_DATA_LAG_SECONDS
        } else {
            0L
        }
        val probeEnd = end.minusSeconds(lagSeconds)
        val availableSeconds = (probeEnd.epochSecond - start.epochSecond).coerceAtLeast(1L)
        val probeSeconds = minOf(PROBE_SECONDS, availableSeconds)
        return SeismicProbeWindow(
            start = probeEnd.minusSeconds(probeSeconds),
            end = probeEnd
        )
    }
}
