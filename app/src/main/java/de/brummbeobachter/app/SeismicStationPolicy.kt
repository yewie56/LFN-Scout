package de.brummbeobachter.app

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class SeismicStation(
    val network: String,
    val station: String,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double?,
    val siteName: String,
    val distanceKm: Double
)

data class SeismicStationParseResult(
    val stations: List<SeismicStation>,
    val skippedLines: Int
)

data class SeismicChannel(
    val station: SeismicStation,
    val location: String,
    val channel: String,
    val sampleRateHz: Double?,
    val sensitivity: Double? = null,
    val sensitivityFrequencyHz: Double? = null,
    val sensitivityUnits: String = ""
)

data class SeismicChannelParseResult(
    val channels: List<SeismicChannel>,
    val skippedLines: Int
)

object SeismicStationPolicy {
    fun parseFdsnStationText(
        text: String,
        userLatitude: Double,
        userLongitude: Double
    ): SeismicStationParseResult {
        val parsed = mutableListOf<SeismicStation>()
        var skipped = 0

        text.lineSequence().forEach { line ->
            if (line.isBlank() || line.startsWith("#")) return@forEach
            val fields = line.split('|')
            if (fields.size < 6) {
                skipped++
                return@forEach
            }

            val latitude = fields[2].toDoubleOrNull()
            val longitude = fields[3].toDoubleOrNull()
            val network = fields[0].trim()
            val station = fields[1].trim()
            if (latitude == null || longitude == null || network.isBlank() || station.isBlank()) {
                skipped++
                return@forEach
            }

            parsed += SeismicStation(
                network = network,
                station = station,
                latitude = latitude,
                longitude = longitude,
                elevationMeters = fields.getOrNull(4)?.toDoubleOrNull(),
                siteName = fields.getOrNull(5)?.trim().orEmpty(),
                distanceKm = haversineKm(
                    userLatitude,
                    userLongitude,
                    latitude,
                    longitude
                )
            )
        }

        return SeismicStationParseResult(
            stations = parsed.sortedBy { it.distanceKm },
            skippedLines = skipped
        )
    }

    fun parseFdsnChannelText(
        text: String,
        userLatitude: Double,
        userLongitude: Double
    ): SeismicChannelParseResult {
        val parsed = mutableListOf<SeismicChannel>()
        var skipped = 0

        text.lineSequence().forEach { line ->
            if (line.isBlank() || line.startsWith("#")) return@forEach
            val fields = line.split('|')
            // FDSN level=channel text:
            // Network|Station|Location|Channel|Latitude|Longitude|Elevation|Depth|Azimuth|Dip|...|SampleRate|...
            if (fields.size < 6) {
                skipped++
                return@forEach
            }

            val network = fields.getOrNull(0)?.trim().orEmpty()
            val stationCode = fields.getOrNull(1)?.trim().orEmpty()
            val location = fields.getOrNull(2)?.trim().orEmpty()
            val channelCode = fields.getOrNull(3)?.trim().orEmpty()
            val latitude = fields.getOrNull(4)?.toDoubleOrNull()
            val longitude = fields.getOrNull(5)?.toDoubleOrNull()
            if (
                network.isBlank() || stationCode.isBlank() ||
                channelCode.isBlank() || !channelCode.endsWith("Z", ignoreCase = true) ||
                latitude == null || longitude == null
            ) {
                skipped++
                return@forEach
            }

            val elevation = fields.getOrNull(6)?.toDoubleOrNull()
            val sensitivity = fields.getOrNull(11)?.toDoubleOrNull()
            val sensitivityFrequency = fields.getOrNull(12)?.toDoubleOrNull()
            val sensitivityUnits = fields.getOrNull(13)?.trim().orEmpty()
            val sampleRate = fields.getOrNull(14)?.toDoubleOrNull()
            val siteName = "" // level=channel text contains no station site-name column
            val station = SeismicStation(
                network = network,
                station = stationCode,
                latitude = latitude,
                longitude = longitude,
                elevationMeters = elevation,
                siteName = siteName,
                distanceKm = haversineKm(userLatitude, userLongitude, latitude, longitude)
            )
            parsed += SeismicChannel(
                station = station,
                location = location,
                channel = channelCode.uppercase(),
                sampleRateHz = sampleRate,
                sensitivity = sensitivity,
                sensitivityFrequencyHz = sensitivityFrequency,
                sensitivityUnits = sensitivityUnits
            )
        }

        val sorted = parsed
            .distinctBy { listOf(it.station.network, it.station.station, it.location, it.channel) }
            .sortedWith(
                compareBy<SeismicChannel> { it.station.distanceKm }
                    .thenBy { channelPriority(it.channel) }
                    .thenByDescending { it.sampleRateHz ?: 0.0 }
            )

        return SeismicChannelParseResult(sorted, skipped)
    }

    fun channelPriority(channel: String): Int = when (channel.uppercase()) {
        "HHZ" -> 0
        "BHZ" -> 1
        "EHZ" -> 2
        "SHZ" -> 3
        "LHZ" -> 4
        else -> 10
    }

    fun haversineKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0088
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        return 2 * earthRadiusKm * asin(sqrt(a.coerceIn(0.0, 1.0)))
    }
}
