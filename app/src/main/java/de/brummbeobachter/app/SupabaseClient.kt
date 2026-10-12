package de.brummbeobachter.app

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class SupabaseClient {

    companion object {
        private const val SUBMIT_URL =
            "https://kbejualnruiwtnmeytif.supabase.co/functions/v1/submit_observation"

        private const val RECENT_URL =
            "https://kbejualnruiwtnmeytif.supabase.co/functions/v1/get_recent_observations"

        private const val RADIUS_HISTOGRAM_URL =
            "https://kbejualnruiwtnmeytif.supabase.co/functions/v1/get_radius_histograms"

        private const val ALL_OBSERVATIONS_URL =
            "https://kbejualnruiwtnmeytif.supabase.co/functions/v1/get_all_observations"

        private const val UPLOAD_WAV_URL =
            "https://kbejualnruiwtnmeytif.supabase.co/functions/v1/upload_wav"

        private const val AUDIO_STATS_URL =
            "https://kbejualnruiwtnmeytif.supabase.co/functions/v1/submit_audio_statistics"

        private const val HALF_HOUR_STATS_URL =
            "https://kbejualnruiwtnmeytif.supabase.co/functions/v1/submit_half_hour_statistics"

        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 20_000
    }


    /** Existing backend v1.0.0. Returns true only for HTTP success with {ok:true}. */
    suspend fun submitHalfHourStatistics(payload: JSONObject): Boolean = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(HALF_HOUR_STATS_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }
            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            status in 200..299 && JSONObject(body).optBoolean("ok", false)
        } catch (_: Exception) { false }
        finally { connection?.disconnect() }
    }

    /** Submit a single, versioned, idempotent statistics set after WAV upload. */
    suspend fun submitAudioStatistics(payload: JSONObject): Boolean =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL(AUDIO_STATS_URL).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                }
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                code in 200..299 && JSONObject(body).optBoolean("ok")
            } catch (_: Exception) {
                false
            } finally {
                connection?.disconnect()
            }
        }

    suspend fun submitObservation(payload: ObservationPayload): SubmitResult =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null

            try {
                connection = (URL(SUBMIT_URL).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                }

                val bytes = payload.toJson().toByteArray(Charsets.UTF_8)

                connection.outputStream.use { output ->
                    output.write(bytes)
                }

                val httpCode = connection.responseCode
                val stream = if (httpCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val body = stream?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText)
                    ?: ""

                SubmitResultParser.parse(httpCode, body)
            } catch (e: Exception) {
                SubmitResult(
                    success = false,
                    retryable = true,
                    message = "Netzwerkfehler: ${e.message ?: e.javaClass.simpleName}"
                )
            } finally {
                connection?.disconnect()
            }
        }


    suspend fun getRecentObservations(key: String): RecentObservationsResult =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null

            try {
                connection = (URL(RECENT_URL).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                }

                val escapedKey = key
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")

                val bytes = """{"key":"$escapedKey"}""".toByteArray(Charsets.UTF_8)

                connection.outputStream.use { output ->
                    output.write(bytes)
                }

                val httpCode = connection.responseCode
                val stream = if (httpCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val body = stream?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText)
                    ?: ""

                RecentObservationsParser.parse(httpCode, body)
            } catch (e: Exception) {
                RecentObservationsResult(
                    success = false,
                    message = "Abruffehler: ${e.message ?: e.javaClass.simpleName}"
                )
            } finally {
                connection?.disconnect()
            }
        }


suspend fun getRadiusHistograms(
    key: String,
    latitude: Double?,
    longitude: Double?,
    radiusKm: Int
): RadiusHistogramResult =
    withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null

        try {
            connection =
                (
                    URL(
                        RADIUS_HISTOGRAM_URL
                    ).openConnection()
                        as HttpURLConnection
                    ).apply {
                    requestMethod = "POST"
                    connectTimeout =
                        CONNECT_TIMEOUT_MS
                    readTimeout =
                        READ_TIMEOUT_MS
                    doOutput = true
                    setRequestProperty(
                        "Content-Type",
                        "application/json; charset=utf-8"
                    )
                    setRequestProperty(
                        "Accept",
                        "application/json"
                    )
                }

            val request =
                JSONObject().apply {
                    put(
                        "key",
                        key.trim()
                    )
                    put(
                        "radius_km",
                        RadiusAnalysisPolicy
                            .normalizeRadiusKm(
                                radiusKm
                            )
                    )

                    if (
                        latitude != null &&
                        longitude != null
                    ) {
                        put(
                            "latitude",
                            latitude
                        )
                        put(
                            "longitude",
                            longitude
                        )
                    } else {
                        put(
                            "latitude",
                            JSONObject.NULL
                        )
                        put(
                            "longitude",
                            JSONObject.NULL
                        )
                    }
                }

            connection.outputStream.use {
                output ->
                output.write(
                    request.toString()
                        .toByteArray(
                            Charsets.UTF_8
                        )
                )
            }

            val httpCode =
                connection.responseCode

            val stream =
                if (
                    httpCode in 200..299
                ) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val body =
                stream
                    ?.bufferedReader(
                        Charsets.UTF_8
                    )
                    ?.use(
                        BufferedReader::readText
                    )
                    ?: ""

            RadiusHistogramParser.parse(
                httpCode,
                body
            )
        } catch (error: Exception) {
            RadiusHistogramResult(
                success = false,
                message =
                    "Auswertungsabruf fehlgeschlagen: " +
                        (
                            error.message
                                ?: error.javaClass.simpleName
                            )
            )
        } finally {
            connection?.disconnect()
        }
    }


suspend fun uploadWav(
    key: String,
    recordingId: String,
    wavBytes: ByteArray,
    recordedAt: String,
    gainDb: Int,
    audioSource: String,
    clippedSamples: Int,
    comment: String?,
    captureOrigin: String = "LEGACY_UNKNOWN",
    triggerType: String = "LEGACY_UNKNOWN"
): AudioUploadResult =
    withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null

        try {
            connection =
                (
                    URL(UPLOAD_WAV_URL)
                        .openConnection()
                        as HttpURLConnection
                    ).apply {
                    requestMethod = "POST"
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = 45_000
                    doOutput = true
                    setRequestProperty(
                        "Content-Type",
                        "application/json; charset=utf-8"
                    )
                    setRequestProperty(
                        "Accept",
                        "application/json"
                    )
                }

            val request =
                JSONObject().apply {
                    put("key", key.trim())
                    put("recording_id", recordingId)
                    put("capture_origin", captureOrigin)
                    put("trigger_type", triggerType)
                    put(
                        "recorded_at",
                        recordedAt
                    )
                    put(
                        "sample_rate_hz",
                        AudioRecordingPolicy
                            .TARGET_SAMPLE_RATE_HZ
                    )
                    put("channels", 1)
                    put("bits_per_sample", 16)
                    put(
                        "gain_db",
                        AudioRecordingPolicy
                            .normalizeGainDb(gainDb)
                    )
                    put(
                        "audio_source",
                        if (
                            audioSource ==
                            "MIC"
                        ) {
                            "MIC"
                        } else {
                            "UNP"
                        }
                    )
                    put(
                        "clipped_samples",
                        clippedSamples
                            .coerceAtLeast(0)
                    )
                    val normalizedComment =
                        comment
                            ?.take(2000)
                            ?.takeIf {
                                it.isNotBlank()
                            }
                    if (
                        normalizedComment ==
                        null
                    ) {
                        put(
                            "comment",
                            JSONObject.NULL
                        )
                    } else {
                        put(
                            "comment",
                            normalizedComment
                        )
                    }
                    put(
                        "wav_base64",
                        Base64.encodeToString(
                            wavBytes,
                            Base64.NO_WRAP
                        )
                    )
                }

            connection.outputStream.use {
                it.write(
                    request
                        .toString()
                        .toByteArray(
                            Charsets.UTF_8
                        )
                )
            }

            val code =
                connection.responseCode
            val stream =
                if (code in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val body =
                stream
                    ?.bufferedReader(
                        Charsets.UTF_8
                    )
                    ?.use(
                        BufferedReader::readText
                    )
                    ?: ""

            val json =
                runCatching {
                    JSONObject(body)
                }.getOrNull()

            if (
                code in 200..299 &&
                json?.optBoolean("ok") == true
            ) {
                return@withContext AudioUploadResult(
                    success = true,
                    storagePath =
                        json.optString(
                            "storage_path",
                            ""
                        ).takeIf {
                            it.isNotBlank()
                        },
                    message = "Audio automatisch übertragen"
                )
            }

            val error =
                json?.optString("error")
                    .orEmpty()

            AudioUploadResult(
                success = false,
                keyRevoked =
                    error == "key_revoked",
                message =
                    if (error.isNotBlank()) {
                        "Audio-Upload fehlgeschlagen: $error"
                    } else {
                        "Audio-Upload fehlgeschlagen (HTTP $code)"
                    }
            )
        } catch (error: Exception) {
            AudioUploadResult(
                success = false,
                message =
                    "Audio-Upload fehlgeschlagen: " +
                        (
                            error.message
                                ?: error.javaClass.simpleName
                            )
            )
        } finally {
            connection?.disconnect()
        }
    }



suspend fun getAllObservations(
    key: String
): AllOwnObservationsResult =
    withContext(Dispatchers.IO) {
        val allItems =
            mutableListOf<OwnObservation>()

        var offset = 0
        var observerId = ""
        var pages = 0

        while (pages < 1000) {
            val page =
                getAllObservationsPage(
                    key = key,
                    offset = offset,
                    limit = 500
                )

            if (!page.success) {
                return@withContext AllOwnObservationsResult(
                    success = false,
                    observerId = observerId,
                    items = allItems,
                    keyRevoked =
                        page.keyRevoked,
                    message = page.message
                )
            }

            if (observerId.isBlank()) {
                observerId =
                    page.observerId
            }

            allItems.addAll(
                page.items
            )

            if (!page.hasMore) {
                return@withContext AllOwnObservationsResult(
                    success = true,
                    observerId = observerId,
                    items = allItems,
                    message =
                        "${allItems.size} Beobachtungen abgerufen"
                )
            }

            val next =
                page.nextOffset

            if (next <= offset) {
                return@withContext AllOwnObservationsResult(
                    success = false,
                    observerId = observerId,
                    items = allItems,
                    message =
                        "Abruf abgebrochen: ungültige Seitennavigation"
                )
            }

            offset = next
            pages++
        }

        AllOwnObservationsResult(
            success = false,
            observerId = observerId,
            items = allItems,
            message =
                "Abruf abgebrochen: zu viele Seiten"
        )
    }

private suspend fun getAllObservationsPage(
    key: String,
    offset: Int,
    limit: Int
): OwnObservationsPageResult =
    withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null

        try {
            connection =
                (
                    URL(
                        ALL_OBSERVATIONS_URL
                    ).openConnection()
                        as HttpURLConnection
                    ).apply {
                    requestMethod = "POST"
                    connectTimeout =
                        CONNECT_TIMEOUT_MS
                    readTimeout =
                        READ_TIMEOUT_MS
                    doOutput = true
                    setRequestProperty(
                        "Content-Type",
                        "application/json; charset=utf-8"
                    )
                    setRequestProperty(
                        "Accept",
                        "application/json"
                    )
                }

            val request =
                JSONObject().apply {
                    put(
                        "key",
                        key.trim()
                    )
                    put(
                        "offset",
                        offset.coerceAtLeast(0)
                    )
                    put(
                        "limit",
                        limit.coerceIn(
                            1,
                            1000
                        )
                    )
                }

            connection.outputStream.use {
                output ->
                output.write(
                    request
                        .toString()
                        .toByteArray(
                            Charsets.UTF_8
                        )
                )
            }

            val httpCode =
                connection.responseCode

            val stream =
                if (
                    httpCode in 200..299
                ) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val body =
                stream
                    ?.bufferedReader(
                        Charsets.UTF_8
                    )
                    ?.use(
                        BufferedReader::readText
                    )
                    ?: ""

            OwnObservationsParser.parse(
                httpCode,
                body
            )
        } catch (error: Exception) {
            OwnObservationsPageResult(
                success = false,
                message =
                    "Export-Abruf fehlgeschlagen: " +
                        (
                            error.message
                                ?: error.javaClass.simpleName
                        )
            )
        } finally {
            connection?.disconnect()
        }
    }

}
