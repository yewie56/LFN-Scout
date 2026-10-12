package de.brummbeobachter.app

import android.content.Context
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Lädt vertikale Wellenformdaten aus der europäischen EIDA-Infrastruktur.
 *
 * v0.4.69:
 * - klassische 48x30-min-Helicorderdarstellung mit WBBU-Referenz-Y-Skalierung
 * - FDSN-Kanalsensitivität wird für Geschwindigkeitskanäle zu nm/s umgerechnet
 * - bei nicht verwertbarer Sensitivität klarer Counts-Fallback statt falscher physikalischer Skala
 * - schnelle direkte Stations-/Kanalsuche zuerst bei BGR und GEOFON
 * - EIDA Federator nur noch als Fallback, falls direkte Metadatenquellen leer bleiben
 * - EIDA Routing nur für wenige nahe Kandidaten
 * - kurzer DataSelect-Probeabruf vor dem vollständigen Tagesdownload
 * - Streams ohne EIDA-Route werden ohne aussichtslosen Federator-Probeabruf übersprungen
 * - Zeitfenster: gewählter lokaler Kalendertag, heute 00:00..JETZT,
 *   vergangene Tage 00:00..24:00 (DST-sicher über ZoneId)
 * - Plot wird lokal aus MiniSEED erzeugt
 */
class SeismicLiveService(context: Context) {
    private val appContext = context.applicationContext

    data class Snapshot(
        val station: SeismicStation,
        val locationCode: String,
        val channel: String,
        val sampleRateHz: Double,
        val traceStartEpochMs: Long,
        val traceEndEpochMs: Long,
        val requestedStartEpochMs: Long,
        val requestedEndEpochMs: Long,
        val selectedDate: LocalDate,
        val zoneId: String,
        val isToday: Boolean,
        val sampleCount: Int,
        val recordCount: Int,
        val encodings: Set<Int>,
        val envelope: SeismicEnvelope,
        val helicorderRows: List<SeismicHelicorderRow>,
        val helicorderMaxAbs: Double,
        val sourceKind: String,
        val sourceImageBytes: ByteArray?,
        val routeHost: String,
        val loadedAtEpochMs: Long,
        val amplitudeUnit: String = "counts",
        val amplitudeScaleMode: String = "COUNTS_AUTO",
        val sensitivity: Double? = null,
        val sensitivityFrequencyHz: Double? = null,
        val sensitivityUnits: String = "",
        val plotValueGain: Int = 1
    )

    sealed interface Result {
        data class Success(val snapshot: Snapshot) : Result
        data class Failure(val message: String) : Result
    }

    suspend fun loadNearestVertical(
        latitude: Double,
        longitude: Double,
        selectedDate: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault(),
        now: Instant = Instant.now(),
        nearPlotGain: Int = SeismicPlotUiPolicy.DEFAULT_NEAR_PLOT_GAIN
    ): Result {
        val window = try {
            SeismicDayPolicy.windowFor(selectedDate, zoneId, now)
        } catch (_: IllegalArgumentException) {
            return Result.Failure("Ein Datum in der Zukunft kann nicht geladen werden")
        }

        DebugLogger.log(
            appContext,
            "SEISMIC_DATE_SELECTED",
            "local_date=${window.date} zone=${window.zoneId.id} today=${window.isToday}"
        )
        DebugLogger.log(
            appContext,
            "SEISMIC_WINDOW",
            "local_start=${window.localStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)} " +
                "local_end=${window.localEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)} " +
                "utc_start=${window.start} utc_end=${window.end}"
        )
        val specialZone = SeismicRegionalPolicy.isBuerstadtSpecialZone(latitude, longitude)
        DebugLogger.log(
            appContext,
            "SEISMIC_LOAD_START",
            "lat=${"%.5f".format(Locale.US, latitude)} lon=${"%.5f".format(Locale.US, longitude)} " +
                "source=DIRECT_FDSN_ROUTING date=${window.date} buerstadt_special=$specialZone"
        )

        if (specialZone && window.isToday) {
            val wbbu = fetchWbbuCurrentPlot(latitude, longitude, window)
            if (wbbu != null) return Result.Success(wbbu)
        }

        return try {
            val channels = fetchVerticalChannels(latitude, longitude, window.start, window.end)
            if (channels.isEmpty()) {
                DebugLogger.log(appContext, "SEISMIC_NO_STATION", "reason=no_vertical_channel_candidates")
                return Result.Failure("Keine geeignete EIDA-Station mit vertikalem Z-Kanal gefunden")
            }

            DebugLogger.log(
                appContext,
                "SEISMIC_CHANNELS_OK",
                "count=${channels.size} nearest=${streamId(channels.first())} " +
                    "distance_km=${"%.1f".format(Locale.US, channels.first().station.distanceKm)}"
            )

            for (candidate in channels.take(MAX_WAVEFORM_CANDIDATES)) {
                val routes = fetchRoutes(candidate, window.start, window.end)
                if (routes.isEmpty()) {
                    DebugLogger.log(
                        appContext,
                        "SEISMIC_ROUTE_NONE",
                        "stream=${streamId(candidate)} action=skip"
                    )
                    continue
                }

                DebugLogger.log(
                    appContext,
                    "SEISMIC_ROUTE_OK",
                    "stream=${streamId(candidate)} routes=${routes.size}"
                )

                val routeEndpoints = EidaRoutingPolicy.probeEndpoints(
                    routes = routes,
                    fallbackEndpoint = FEDERATOR_DATASELECT_URL,
                    maxRoutes = MAX_ROUTES_PER_CANDIDATE
                )

                for (routeEndpoint in routeEndpoints) {
                    val probeWindow = SeismicSearchPolicy.probeWindow(window.start, window.end)
                    val probeUrl = buildDataUrl(routeEndpoint, candidate, probeWindow.start, probeWindow.end)
                    DebugLogger.log(
                        appContext,
                        "SEISMIC_PROBE_START",
                        "stream=${streamId(candidate)} start=${probeWindow.start} end=${probeWindow.end} host=${safeHost(probeUrl)}"
                    )
                    val probe = fetchWaveform(
                        candidate = candidate,
                        routedUrl = probeUrl,
                        phase = "probe",
                        timeoutMs = PROBE_TIMEOUT_MS,
                        maxBytes = MAX_PROBE_BYTES
                    )
                    if (probe !is WaveformFetch.Success) {
                        continue
                    }
                    DebugLogger.log(
                        appContext,
                        "SEISMIC_PROBE_OK",
                        "stream=${streamId(candidate)} samples=${probe.trace.samples.size} host=${probe.routeHost}"
                    )

                    val fullUrl = buildDataUrl(routeEndpoint, candidate, window.start, window.end)
                    when (val waveform = fetchWaveform(candidate, fullUrl)) {
                        is WaveformFetch.Success -> {
                            val trace = waveform.trace
                            val envelope = SeismicWaveformPolicy.buildEnvelope(trace.samples, PLOT_BINS)
                            val velocityScale = SeismicAmplitudePolicy.velocityNmPerSecPerCount(candidate)
                            if (velocityScale != null) {
                                DebugLogger.log(
                                    appContext,
                                    "SEISMIC_CALIBRATION_OK",
                                    "stream=${streamId(candidate)} sensitivity=${candidate.sensitivity} " +
                                        "frequency_hz=${candidate.sensitivityFrequencyHz ?: 0.0} units=${candidate.sensitivityUnits} " +
                                        "nm_per_sec_per_count=${"%.9f".format(Locale.US, velocityScale)}"
                                )
                            } else {
                                DebugLogger.log(
                                    appContext,
                                    "SEISMIC_CALIBRATION_FALLBACK",
                                    "stream=${streamId(candidate)} sensitivity=${candidate.sensitivity ?: 0.0} units=${candidate.sensitivityUnits.ifBlank { "-" }} mode=counts"
                                )
                            }
                            val helicorderRows = SeismicHelicorderPolicy.buildRows(
                                samples = trace.samples,
                                sampleRateHz = trace.sampleRateHz,
                                traceStartEpochMs = trace.startEpochMs,
                                requestedStartEpochMs = window.start.toEpochMilli(),
                                requestedEndEpochMs = window.end.toEpochMilli(),
                                binsPerRow = HELICORDER_BINS_PER_ROW,
                                valueScale = (velocityScale ?: 1.0) * SeismicPlotUiPolicy.normalizeNearPlotGain(nearPlotGain).toDouble()
                            )
                            val helicorderMaxAbs = SeismicHelicorderPolicy.maxAbs(helicorderRows)
                            if (envelope.bins.isEmpty() || envelope.maxAbs <= 0.0 || helicorderMaxAbs <= 0.0) {
                                DebugLogger.log(
                                    appContext,
                                    "SEISMIC_NO_CURRENT_DATA",
                                    "reason=empty_or_flat_trace stream=${streamId(candidate)}"
                                )
                                continue
                            }

                            DebugLogger.log(
                                appContext,
                                "SEISMIC_STATION_SELECTED",
                                "network=${candidate.station.network} station=${candidate.station.station} " +
                                    "distance_km=${"%.1f".format(Locale.US, candidate.station.distanceKm)}"
                            )
                            DebugLogger.log(
                                appContext,
                                "SEISMIC_CHANNEL_SELECTED",
                                "location=${displayLocation(candidate.location)} channel=${candidate.channel} " +
                                    "metadata_sample_rate=${candidate.sampleRateHz ?: 0.0}"
                            )
                            DebugLogger.log(
                                appContext,
                                "SEISMIC_WAVEFORM_OK",
                                "bytes=${waveform.byteCount} records=${trace.recordCount} samples=${trace.samples.size} " +
                                    "sample_rate=${"%.3f".format(Locale.US, trace.sampleRateHz)} " +
                                    "encoding=${trace.encodings.sorted().joinToString(",")} host=${waveform.routeHost}"
                            )
                            DebugLogger.log(
                                appContext,
                                "SEISMIC_PLOT_OK",
                                "bins=${envelope.bins.size} max_abs_${if (velocityScale != null) "nm_s" else "counts"}=" +
                                    "${"%.1f".format(Locale.US, helicorderMaxAbs)} date=${window.date}"
                            )
                            return Result.Success(
                                Snapshot(
                                    station = candidate.station,
                                    locationCode = candidate.location,
                                    channel = candidate.channel,
                                    sampleRateHz = trace.sampleRateHz,
                                    traceStartEpochMs = trace.startEpochMs,
                                    traceEndEpochMs = trace.endEpochMs,
                                    requestedStartEpochMs = window.start.toEpochMilli(),
                                    requestedEndEpochMs = window.end.toEpochMilli(),
                                    selectedDate = window.date,
                                    zoneId = window.zoneId.id,
                                    isToday = window.isToday,
                                    sampleCount = trace.samples.size,
                                    recordCount = trace.recordCount,
                                    encodings = trace.encodings,
                                    envelope = envelope,
                                    helicorderRows = helicorderRows,
                                    helicorderMaxAbs = helicorderMaxAbs,
                                    sourceKind = "FDSN_RAW",
                                    sourceImageBytes = null,
                                    routeHost = waveform.routeHost,
                                    loadedAtEpochMs = System.currentTimeMillis(),
                                    amplitudeUnit = if (velocityScale != null) "nm/s" else "counts",
                                    amplitudeScaleMode = if (velocityScale != null) "FDSN_SENSITIVITY_WBBU_REFERENCE" else "COUNTS_AUTO",
                                    sensitivity = candidate.sensitivity,
                                    sensitivityFrequencyHz = candidate.sensitivityFrequencyHz,
                                    sensitivityUnits = candidate.sensitivityUnits,
                                    plotValueGain = SeismicPlotUiPolicy.normalizeNearPlotGain(nearPlotGain)
                                )
                            )
                        }
                        is WaveformFetch.Unavailable -> Unit
                    }
                }
            }

            DebugLogger.log(
                appContext,
                "SEISMIC_UNAVAILABLE",
                "reason=no_waveform_for_selected_day candidates=${minOf(channels.size, MAX_WAVEFORM_CANDIDATES)} date=${window.date}"
            )
            Result.Failure("Für die nächstgelegenen offenen EIDA-Stationen sind für ${window.date} keine Wellenformdaten verfügbar")
        } catch (error: SocketTimeoutException) {
            DebugLogger.log(appContext, "SEISMIC_TIMEOUT", "phase=channel_list message=${safe(error.message)}")
            Result.Failure("Zeitüberschreitung beim Abruf der seismischen Daten")
        } catch (error: Exception) {
            DebugLogger.log(
                appContext,
                "SEISMIC_FAILED",
                "error=${error.javaClass.simpleName} message=${safe(error.message)}"
            )
            Result.Failure("Seismik konnte nicht geladen werden: ${error.message ?: error.javaClass.simpleName}")
        }
    }


    private fun fetchWbbuCurrentPlot(latitude: Double, longitude: Double, window: SeismicDayWindow): Snapshot? {
        DebugLogger.log(appContext, "SEISMIC_WBBU_SPECIAL_START", "station=HS.WBBU source=HLNUG")
        val connection = try {
            open(URL(WBBU_PLOT_URL), WBBU_TIMEOUT_MS, "image/png,image/*,*/*")
        } catch (error: Exception) {
            DebugLogger.log(appContext, "SEISMIC_WBBU_SPECIAL_FAIL", "phase=open error=${error.javaClass.simpleName} message=${safe(error.message)}")
            return null
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                DebugLogger.log(appContext, "SEISMIC_WBBU_SPECIAL_FAIL", "phase=http code=${connection.responseCode}")
                return null
            }
            val bytes = readLimited(connection, MAX_WBBU_IMAGE_BYTES)
            if (bytes.size < 1024 || !(bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4e.toByte() && bytes[3] == 0x47.toByte())) {
                DebugLogger.log(appContext, "SEISMIC_WBBU_SPECIAL_FAIL", "phase=validate bytes=${bytes.size}")
                return null
            }
            val distance = SeismicStationPolicy.haversineKm(latitude, longitude, SeismicRegionalPolicy.WBBU_LATITUDE, SeismicRegionalPolicy.WBBU_LONGITUDE)
            DebugLogger.log(appContext, "SEISMIC_WBBU_SPECIAL_OK", "station=HS.WBBU distance_km=${"%.1f".format(Locale.US, distance)} bytes=${bytes.size}")
            return Snapshot(
                station = SeismicStation("HS", "WBBU", SeismicRegionalPolicy.WBBU_LATITUDE, SeismicRegionalPolicy.WBBU_LONGITUDE, 87.0, "Bürstadt", distance),
                locationCode = "",
                channel = "Z",
                sampleRateHz = 100.0,
                traceStartEpochMs = window.start.toEpochMilli(),
                traceEndEpochMs = window.end.toEpochMilli(),
                requestedStartEpochMs = window.start.toEpochMilli(),
                requestedEndEpochMs = window.end.toEpochMilli(),
                selectedDate = window.date,
                zoneId = window.zoneId.id,
                isToday = true,
                sampleCount = 0,
                recordCount = 0,
                encodings = emptySet(),
                envelope = SeismicEnvelope(emptyList(), 0.0, 0.0),
                helicorderRows = emptyList(),
                helicorderMaxAbs = 0.0,
                sourceKind = "HLNUG_WBBU_IMAGE",
                sourceImageBytes = bytes,
                routeHost = "www.hlnug.de",
                loadedAtEpochMs = System.currentTimeMillis()
            )
        } catch (error: Exception) {
            DebugLogger.log(appContext, "SEISMIC_WBBU_SPECIAL_FAIL", "phase=download error=${error.javaClass.simpleName} message=${safe(error.message)}")
            return null
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchVerticalChannels(
        latitude: Double,
        longitude: Double,
        start: Instant,
        end: Instant
    ): List<SeismicChannel> {
        val collected = mutableListOf<SeismicChannel>()

        for (source in SeismicSearchPolicy.directMetadataSources()) {
            val channels = fetchChannelsFromSource(
                source = source,
                latitude = latitude,
                longitude = longitude,
                start = start,
                end = end,
                radiusDegrees = DIRECT_SEARCH_RADIUS_DEGREES,
                timeoutMs = DIRECT_STATION_TIMEOUT_MS
            )
            if (channels.isNotEmpty()) {
                DebugLogger.log(
                    appContext,
                    "SEISMIC_CHANNEL_SOURCE_OK",
                    "source=${source.name} count=${channels.size}"
                )
                collected += channels
            }

            val merged = SeismicSearchPolicy.mergeChannels(collected)
            if (merged.size >= DIRECT_TARGET_CANDIDATES) {
                DebugLogger.log(
                    appContext,
                    "SEISMIC_CHANNEL_DIRECT_READY",
                    "count=${merged.size} after=${source.name}"
                )
                return merged
            }
        }

        val mergedDirect = SeismicSearchPolicy.mergeChannels(collected)
        if (mergedDirect.isNotEmpty()) {
            return mergedDirect
        }

        DebugLogger.log(
            appContext,
            "SEISMIC_CHANNEL_FALLBACK",
            "source=EIDA_FEDERATOR reason=no_direct_candidates"
        )
        return fetchChannelsFromSource(
            source = SeismicMetadataSource("EIDA_FEDERATOR", FEDERATOR_STATION_URL),
            latitude = latitude,
            longitude = longitude,
            start = start,
            end = end,
            radiusDegrees = FEDERATOR_SEARCH_RADIUS_DEGREES,
            timeoutMs = FEDERATOR_STATION_TIMEOUT_MS
        )
    }

    private fun fetchChannelsFromSource(
        source: SeismicMetadataSource,
        latitude: Double,
        longitude: Double,
        start: Instant,
        end: Instant,
        radiusDegrees: Double,
        timeoutMs: Int
    ): List<SeismicChannel> {
        val query = linkedMapOf(
            "network" to "*",
            "station" to "*",
            "location" to "*",
            "level" to "channel",
            "format" to "text",
            "channel" to "??Z",
            "latitude" to "%.6f".format(Locale.US, latitude),
            "longitude" to "%.6f".format(Locale.US, longitude),
            "maxradius" to radiusDegrees.toString(),
            "starttime" to start.toString(),
            "endtime" to end.toString(),
            "nodata" to "404"
        )

        val connection = try {
            open(
                URL("${source.url}?${encodeQuery(query)}"),
                timeoutMs,
                "text/plain,*/*"
            )
        } catch (error: Exception) {
            DebugLogger.log(
                appContext,
                "SEISMIC_CHANNEL_SOURCE_FAIL",
                "source=${source.name} phase=open error=${error.javaClass.simpleName} message=${safe(error.message)}"
            )
            return emptyList()
        }

        try {
            val code = connection.responseCode
            if (code == HttpURLConnection.HTTP_NOT_FOUND || code == HttpURLConnection.HTTP_NO_CONTENT) {
                DebugLogger.log(appContext, "SEISMIC_CHANNEL_SOURCE_EMPTY", "source=${source.name} code=$code")
                return emptyList()
            }
            if (code != HttpURLConnection.HTTP_OK) {
                DebugLogger.log(
                    appContext,
                    "SEISMIC_HTTP_ERROR",
                    "phase=channel_list source=${source.name} code=$code"
                )
                return emptyList()
            }

            val text = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val parsed = SeismicStationPolicy.parseFdsnChannelText(text, latitude, longitude)
            if (parsed.skippedLines > 0) {
                DebugLogger.log(
                    appContext,
                    "SEISMIC_PARSE_ERROR",
                    "phase=channel_list source=${source.name} skipped=${parsed.skippedLines} parsed=${parsed.channels.size}"
                )
            }
            return parsed.channels
        } catch (error: SocketTimeoutException) {
            DebugLogger.log(
                appContext,
                "SEISMIC_TIMEOUT",
                "phase=channel_list source=${source.name} message=${safe(error.message)}"
            )
            return emptyList()
        } catch (error: Exception) {
            DebugLogger.log(
                appContext,
                "SEISMIC_CHANNEL_SOURCE_FAIL",
                "source=${source.name} error=${error.javaClass.simpleName} message=${safe(error.message)}"
            )
            return emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchRoutes(candidate: SeismicChannel, start: Instant, end: Instant): List<String> {
        val query = linkedMapOf(
            "service" to "dataselect",
            "format" to "get",
            "net" to candidate.station.network,
            "sta" to candidate.station.station,
            "loc" to if (candidate.location.isBlank()) "--" else candidate.location,
            "cha" to candidate.channel,
            "starttime" to start.toString(),
            "endtime" to end.toString()
        )
        val connection = open(
            URL("$ROUTING_URL?${encodeQuery(query)}"),
            ROUTING_TIMEOUT_MS,
            "text/plain,*/*"
        )
        try {
            val code = connection.responseCode
            if (code == HttpURLConnection.HTTP_NOT_FOUND || code == HttpURLConnection.HTTP_NO_CONTENT) return emptyList()
            if (code != HttpURLConnection.HTTP_OK) {
                DebugLogger.log(
                    appContext,
                    "SEISMIC_HTTP_ERROR",
                    "phase=routing code=$code stream=${streamId(candidate)}"
                )
                return emptyList()
            }
            val text = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            return EidaRoutingPolicy.parseGetResponse(text)
        } catch (error: SocketTimeoutException) {
            DebugLogger.log(
                appContext,
                "SEISMIC_TIMEOUT",
                "phase=routing stream=${streamId(candidate)} message=${safe(error.message)}"
            )
            return emptyList()
        } catch (error: Exception) {
            DebugLogger.log(
                appContext,
                "SEISMIC_FAILED",
                "phase=routing stream=${streamId(candidate)} error=${error.javaClass.simpleName} message=${safe(error.message)}"
            )
            return emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun buildDataUrl(
        endpointOrUrl: String,
        candidate: SeismicChannel,
        start: Instant,
        end: Instant
    ): String {
        val query = linkedMapOf(
            "network" to candidate.station.network,
            "station" to candidate.station.station,
            "location" to if (candidate.location.isBlank()) "--" else candidate.location,
            "channel" to candidate.channel,
            "starttime" to start.toString(),
            "endtime" to end.toString()
        )
        return "${EidaRoutingPolicy.endpointOnly(endpointOrUrl)}?${encodeQuery(query)}"
    }

    private sealed interface WaveformFetch {
        data class Success(
            val trace: MiniSeedTrace,
            val byteCount: Int,
            val routeHost: String
        ) : WaveformFetch

        data class Unavailable(val detail: String) : WaveformFetch
    }

    private fun fetchWaveform(
        candidate: SeismicChannel,
        routedUrl: String,
        phase: String = "waveform",
        timeoutMs: Int = WAVEFORM_TIMEOUT_MS,
        maxBytes: Int = MAX_WAVEFORM_BYTES
    ): WaveformFetch {
        val preferredUrl = EidaRoutingPolicy.httpsPreferred(routedUrl)
        val url = appendQuery(preferredUrl, "nodata", "404")
        val host = try { URL(url).host } catch (_: Exception) { "-" }

        if (phase == "waveform") {
            DebugLogger.log(
                appContext,
                "SEISMIC_WAVEFORM_DOWNLOAD_START",
                "stream=${streamId(candidate)} host=$host"
            )
        }

        val connection = try {
            open(URL(url), timeoutMs, "application/vnd.fdsn.mseed,application/octet-stream,*/*")
        } catch (error: Exception) {
            DebugLogger.log(
                appContext,
                "SEISMIC_FAILED",
                "phase=${phase}_open stream=${streamId(candidate)} error=${error.javaClass.simpleName} message=${safe(error.message)}"
            )
            return WaveformFetch.Unavailable("open_${error.javaClass.simpleName}")
        }

        try {
            val code = connection.responseCode
            if (code == HttpURLConnection.HTTP_NOT_FOUND || code == HttpURLConnection.HTTP_NO_CONTENT) {
                DebugLogger.log(
                    appContext,
                    "SEISMIC_NO_CURRENT_DATA",
                    "reason=http_$code stream=${streamId(candidate)} host=$host"
                )
                return WaveformFetch.Unavailable("http_$code")
            }
            if (code != HttpURLConnection.HTTP_OK) {
                DebugLogger.log(
                    appContext,
                    "SEISMIC_HTTP_ERROR",
                    "phase=$phase code=$code stream=${streamId(candidate)} host=$host"
                )
                return WaveformFetch.Unavailable("http_$code")
            }

            val bytes = readLimited(connection, maxBytes)
            if (bytes.size < 64) {
                DebugLogger.log(
                    appContext,
                    "SEISMIC_NO_CURRENT_DATA",
                    "reason=response_too_small bytes=${bytes.size} stream=${streamId(candidate)}"
                )
                return WaveformFetch.Unavailable("too_small")
            }

            val trace = try {
                MiniSeed2Decoder.decode(bytes)
            } catch (error: Exception) {
                DebugLogger.log(
                    appContext,
                    "SEISMIC_MINISEED_PARSE_ERROR",
                    "stream=${streamId(candidate)} bytes=${bytes.size} error=${error.javaClass.simpleName} message=${safe(error.message)}"
                )
                return WaveformFetch.Unavailable("parse_${error.javaClass.simpleName}")
            }

            if (!trace.channel.endsWith("Z", ignoreCase = true) || trace.samples.isEmpty() || trace.sampleRateHz <= 0.0) {
                DebugLogger.log(
                    appContext,
                    "SEISMIC_NO_CURRENT_DATA",
                    "reason=invalid_trace stream=${trace.network}.${trace.station}.${trace.location}.${trace.channel} " +
                        "samples=${trace.samples.size} sample_rate=${trace.sampleRateHz}"
                )
                return WaveformFetch.Unavailable("invalid_trace")
            }

            return WaveformFetch.Success(trace, bytes.size, host)
        } catch (error: SocketTimeoutException) {
            DebugLogger.log(
                appContext,
                "SEISMIC_TIMEOUT",
                "phase=$phase stream=${streamId(candidate)} host=$host message=${safe(error.message)}"
            )
            return WaveformFetch.Unavailable("timeout")
        } catch (error: Exception) {
            DebugLogger.log(
                appContext,
                "SEISMIC_FAILED",
                "phase=$phase stream=${streamId(candidate)} host=$host error=${error.javaClass.simpleName} message=${safe(error.message)}"
            )
            return WaveformFetch.Unavailable(error.javaClass.simpleName)
        } finally {
            connection.disconnect()
        }
    }

    private fun open(url: URL, timeoutMs: Int, accept: String): HttpURLConnection =
        (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            useCaches = false
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "LFN-Scout/${BuildConfig.VERSION_NAME}")
            setRequestProperty("Accept", accept)
        }

    private fun readLimited(connection: HttpURLConnection, maxBytes: Int): ByteArray {
        val expected = connection.contentLength
        if (expected > maxBytes) error("Seismik-Antwort zu groß: $expected Bytes")
        val out = ByteArrayOutputStream(if (expected in 1..maxBytes) expected else 512 * 1024)
        connection.inputStream.use { input ->
            val buffer = ByteArray(64 * 1024)
            var total = 0
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                total += count
                if (total > maxBytes) error("Seismik-Antwort überschreitet $maxBytes Bytes")
                out.write(buffer, 0, count)
            }
        }
        return out.toByteArray()
    }

    private fun encodeQuery(values: Map<String, String>): String = values.entries.joinToString("&") { (key, value) ->
        "${URLEncoder.encode(key, StandardCharsets.UTF_8.name())}=${URLEncoder.encode(value, StandardCharsets.UTF_8.name())}"
    }

    private fun appendQuery(url: String, key: String, value: String): String {
        val separator = if (url.contains('?')) "&" else "?"
        return "$url$separator${URLEncoder.encode(key, StandardCharsets.UTF_8.name())}=${URLEncoder.encode(value, StandardCharsets.UTF_8.name())}"
    }

    private fun displayLocation(location: String): String = if (location.isBlank()) "--" else location

    private fun streamId(candidate: SeismicChannel): String =
        "${candidate.station.network}.${candidate.station.station}.${displayLocation(candidate.location)}.${candidate.channel}"

    private fun safe(value: String?): String = value?.replace('\n', ' ')?.replace('\r', ' ') ?: "-"

    private fun safeHost(url: String): String = try { URL(url).host } catch (_: Exception) { "-" }

    companion object {
        private const val FEDERATOR_STATION_URL = "https://federator.orfeus-eu.org/fdsnws/station/1/query"
        private const val ROUTING_URL = "https://www.orfeus-eu.org/eidaws/routing/1/query"
        private const val FEDERATOR_DATASELECT_URL = "https://federator.orfeus-eu.org/fdsnws/dataselect/1/query"
        private const val DIRECT_SEARCH_RADIUS_DEGREES = 4.0
        private const val FEDERATOR_SEARCH_RADIUS_DEGREES = 6.0
        private const val DIRECT_TARGET_CANDIDATES = 12
        private const val MAX_WAVEFORM_CANDIDATES = 12
        private const val MAX_ROUTES_PER_CANDIDATE = 2
        private const val DIRECT_STATION_TIMEOUT_MS = 8_000
        private const val FEDERATOR_STATION_TIMEOUT_MS = 12_000
        private const val ROUTING_TIMEOUT_MS = 10_000
        private const val PROBE_TIMEOUT_MS = 12_000
        private const val WAVEFORM_TIMEOUT_MS = 60_000
        private const val MAX_PROBE_BYTES = 4 * 1024 * 1024
        private const val MAX_WAVEFORM_BYTES = 64 * 1024 * 1024
        private const val PLOT_BINS = 1200
        private const val HELICORDER_BINS_PER_ROW = 3072
        private const val WBBU_PLOT_URL = "https://www.hlnug.de/fileadmin/messwerte/erdbebendienst/HS.WBBU.png"
        private const val WBBU_TIMEOUT_MS = 10_000
        private const val MAX_WBBU_IMAGE_BYTES = 8 * 1024 * 1024
    }
}
