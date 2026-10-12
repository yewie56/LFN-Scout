package de.brummbeobachter.app

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

/** Sidecar for local verification only. Does not write to / alter existing Supabase tables. */
internal object LocalAudioStatisticsRepository {
    data class Saved(val id: String, val wavName: String?, val jsonName: String, val archiveEntry: AudioArchiveEntry? = null, val analysisJson: String? = null)
    private fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it.toInt() and 255) }
    private fun hash(bytes: ByteArray) = hex(MessageDigest.getInstance("SHA-256").digest(bytes))
    private fun array(items: DoubleArray): JSONArray = JSONArray().also { a -> items.forEach { a.put(it) } }
    private fun levels(v: StatisticsWindowEngine.Levels) = JSONObject().apply {
        put("leq_dbfs", v.leq); put("lmin_dbfs", v.min); put("lmax_dbfs", v.max)
        put("l1_dbfs", v.l1); put("l10_dbfs", v.l10); put("l50_dbfs", v.l50)
        put("l90_dbfs", v.l90); put("l95_dbfs", v.l95); put("l99_dbfs", v.l99)
        put("mean_level_dbfs", v.mean); put("level_stddev_db", v.stddev)
        put("crest_factor_db", v.crestDb); put("kurtosis_excess", v.excessKurtosis)
        put("valid_epochs", v.epochs); put("l10_minus_l90_db", v.l10 - v.l90)
    }
    private fun analysisJson(id: String, origin: String, wavName: String,
        startedMs: Long, endedMs: Long, source: String, device: String,
        appliedGainDb: Double, analyzerGainDb: Int?, clip: StatisticsWindowEngine.Result,
        analyzerSetup: JSONObject?, comment: String? = null): JSONObject {
        val c = clip.config
        return JSONObject().apply {
            put("schema", "LFN_AUDIO_LOCAL_V1"); put("schema_version", StatisticsWindowEngine.SCHEMA)
            put("recording_id", id); put("origin", origin); put("wav_file_name", wavName)
            put("comment", comment?.take(AudioArchive.MAX_COMMENT_LENGTH)?.takeIf { it.isNotBlank() } ?: JSONObject.NULL)
            put("recording_started_at_utc", Instant.ofEpochMilli(startedMs).toString())
            put("recording_ended_at_utc", Instant.ofEpochMilli(endedMs).toString())
            put("duration_s", clip.sampleCount.toDouble()/StatisticsWindowEngine.RATE_HZ)
            put("sample_rate_hz", 1000); put("channels", 1); put("bits_per_sample", 16)
            put("audio_source", source); put("audio_device", device)
            put("applied_wav_gain_db", appliedGainDb)
            put("analyzer_display_gain_db", analyzerGainDb ?: JSONObject.NULL)
            put("level_reference", "PRE_APPLIED_DIGITAL_GAIN_DBFS")
            put("gain_compensation_applied_once", true)
            put("clipped_pcm16_samples", clip.clippedSamples)
            put("statistics_min_hz", c.minHz); put("statistics_max_hz", c.maxHz)
            put("filter_min_hz", c.filterMinHz); put("filter_max_hz", c.filterMaxHz)
            put("filter_bands", c.filterBands)
            put("filter_provenance", "RECOMPUTED_FROM_1KHZ_WAV_BIQUAD_NOT_8KHZ_FAST")
            put("filter_validated_for_spl", false)
            put("statistics_epoch_s", 1.0)
            put("overall", levels(clip.overall))
            put("bands", JSONArray().apply { clip.bands.forEach { b ->
                put(JSONObject().apply { put("index", b.index); put("low_hz", b.minHz)
                    put("high_hz", b.maxHz); put("type", "LOCAL_1KHZ_BIQUAD")
                    put("levels", levels(b.levels)) })
            } })
            put("spectral", JSONObject().apply {
                put("frequency_hz", array(clip.frequenciesHz)); put("mean_psd_fs2_per_hz", array(clip.meanPsd))
                put("median_psd_fs2_per_hz", array(clip.medianPsd)); put("max_psd_fs2_per_hz", array(clip.maxPsd))
                put("fft_size", 2048); put("window", "HANN"); put("hop_samples", 1024)
                put("dominant_frequency_hz", clip.dominantFrequencyHz ?: JSONObject.NULL)
            })
            put("impulses", JSONObject().apply { put("status", "NOT_IMPLEMENTED"); put("count", JSONObject.NULL) })
            put("modulation", JSONObject().apply { put("status", "NOT_IMPLEMENTED") })
            put("fast_8khz_statistics", JSONObject().apply { put("status", "NOT_AVAILABLE");
                put("reason", "8-kHz FAST level timeline not captured for this WAV window") })
            put("calibration", JSONObject().apply { put("spl_valid", false); put("status", "NOT_CALIBRATED") })
            put("quality", JSONObject().apply { put("clipping_present", clip.clippedSamples > 0)
                put("continuity_status", if (origin == "ANALYZER") "FRAME_SEQUENCE_AND_CAPTURE_TIME_MONITORED" else "NOT_VERIFIED_BY_1KHZ_WAV");
                put("waveform_frequency_response_validated", false) })
            if (origin == "ANALYZER" && analyzerSetup != null) put("analyzer_setup", analyzerSetup)
            put("analysis_status", "PARTIAL")
            put("schema_groups", "A,B,C,D,E,H; F,G pending")
            put("supabase_upload", "NOT_SCHEDULED_LOCAL_ONLY")
        }
    }
    private fun analyzerSettings(context: Context): JSONObject {
        val prefs = context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
        return JSONObject().apply {
            prefs.all.forEach { (key, value) ->
                if (!key.startsWith("setup_") && !key.startsWith("mode_")) {
                    when (value) {
                        is Boolean, is Int, is Long, is Float, is String -> put(key, value)
                    }
                }
            }
        }
    }
    private fun publish(context: Context, bytes: ByteArray, name: String, mime: String, inRoot: Boolean = false): Uri? {
        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/LFN-Scout/" + if (inRoot) "" else "LocalSaves/"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, mime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Downloads-Datei konnte nicht angelegt werden")
            try {
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: error("Downloads-Ausgabestrom fehlt")
                check(context.contentResolver.update(uri, ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }, null, null) == 1)
                return uri
            } catch (e: Exception) {
                runCatching { context.contentResolver.delete(uri, null, null) }
                throw e
            }
        }
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), if (inRoot) "LFN-Scout" else "LFN-Scout/LocalSaves")
        check(directory.exists() || directory.mkdirs())
        File(directory, name).writeBytes(bytes)
        return null
    }

    /** Optional, one CSV per WAV. Never changes the JSON, audio archive or cloud payload. */
    private fun exportWavFilterCsvIfEnabled(context: Context, analysis: JSONObject) {
        if (!LocalAudioSaveSettings.load(context).exportWavFilterCsv) return
        val id = analysis.getString("recording_id")
        require(Regex("[A-Za-z0-9_-]{1,128}").matches(id))
        val bands = analysis.getJSONArray("bands")
        val columns = listOf("recording_id", "wav_file_name", "origin", "filter_index", "low_hz", "high_hz",
            "leq_dbfs", "lmin_dbfs", "lmax_dbfs", "l1_dbfs", "l10_dbfs", "l50_dbfs", "l90_dbfs",
            "l95_dbfs", "l99_dbfs", "mean_level_dbfs", "level_stddev_db", "crest_factor_db",
            "kurtosis_excess", "valid_epochs")
        fun csv(value: Any?): String {
            val raw = if (value == null || value === JSONObject.NULL) "" else value.toString()
            return "\"" + raw.replace("\"", "\"\"") + "\""
        }
        val lines = StringBuilder(columns.joinToString(";")).append('\n')
        for (i in 0 until bands.length()) {
            val band = bands.getJSONObject(i)
            val levels = band.getJSONObject("levels")
            val record = listOf(analysis.optString("recording_id"), analysis.optString("wav_file_name"),
                analysis.optString("origin"), (band.optInt("index") + 1).toString(),
                band.opt("low_hz"), band.opt("high_hz")) +
                columns.drop(6).map { levels.opt(it) }
            lines.append(record.joinToString(";") { csv(it) }).append('\n')
        }
        // Per-recording identity avoids duplicate CSV rows when a save is retried.
        publish(context, lines.toString().toByteArray(Charsets.UTF_8), "WavImpulseFilterStatistics_${id}.csv", "text/csv", inRoot = true)
    }
    suspend fun saveAnalyzer(context: Context, clip: LocalAnalyzerAudioBuffer.Clip,
        config: StatisticsWindowEngine.Configuration, displayGainDb: Int,
        comment: String?, observerId: String, observations: List<RecentObservation>): Saved = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val recordingStart = java.time.OffsetDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(clip.startEpochMs), java.time.ZoneId.systemDefault())
        val archiveSource = if (clip.source.uppercase() == "MIC") "MIC" else "UNP"
        // Same naming policy as observer saves. The stored analyzer WAV is PRE-GAIN, so G0 is truthful.
        val wavName = AudioFileNamingPolicy.buildLocalWavFileName(
            observerId = observerId, recordingStart = recordingStart, gainDb = 0,
            audioSource = archiveSource, observations = observations)
        val stem = wavName.substringBeforeLast('.')
        val jsonName = "${stem}_statistics.json"
        val pcm = ByteArray(clip.pcm.size * 2)
        clip.pcm.forEachIndexed { i, value ->
            pcm[2*i] = value.toByte(); pcm[2*i+1] = (value.toInt() shr 8).toByte()
        }
        val wav = WavPcm16.buildHeader(1000, 1, 16, pcm.size) + pcm
        val report = StatisticsWindowEngine.analyze(clip.pcm, 0.0, config)
        val json = analysisJson(id, "ANALYZER", wavName, clip.startEpochMs, clip.endEpochMs,
            clip.source, clip.device, 0.0, displayGainDb, report, analyzerSettings(context), comment)
        json.put("input_sample_rate_hz", clip.inputRateHz)
        json.put("input_clipped_intervals", clip.clippedSamples)
        if (clip.clippedSamples > 0) json.getJSONObject("quality").put("clipping_present", true)
        json.put("waveform_resampling", "SIX_POLE_160HZ_LOWPASS_FRACTIONAL_TO_1KHZ")
        json.put("wav_sha256", hash(wav))
        val jsonBytes = json.toString(2).toByteArray(Charsets.UTF_8)
        val manifestName = "${stem}_manifest.json"
        val manifestBytes = JSONObject().apply {
            put("recording_id", id); put("status", "LOCAL_COMPLETE")
            put("origin", "ANALYZER")
            put("comment", comment?.take(AudioArchive.MAX_COMMENT_LENGTH)?.takeIf { it.isNotBlank() } ?: JSONObject.NULL)
            put("wav_file_name", wavName); put("wav_bytes", wav.size); put("wav_sha256", hash(wav))
            put("statistics_file_name", jsonName); put("statistics_bytes", jsonBytes.size)
            put("statistics_sha256", hash(jsonBytes)); put("supabase_audio_upload", "PENDING_SHARED_AUDIO_PIPELINE")
            put("supabase_statistics_upload", "NOT_CONFIGURED")
        }.toString(2).toByteArray(Charsets.UTF_8)

        val tempWav = File(context.cacheDir, "analyzer_${id}.wav")
        tempWav.writeBytes(wav)
        val archived = AudioArchive.createAnalyzer(
            context = context, recordingId = id, wavFile = tempWav, targetDisplayName = wavName,
            recordedAt = java.time.OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(clip.endEpochMs),
                java.time.ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            audioSource = archiveSource, clippedSamples = clip.clippedSamples,
            durationSeconds = (clip.pcm.size / StatisticsWindowEngine.RATE_HZ).coerceAtLeast(0),
            comment = comment, statisticsName = jsonName, statisticsBytes = jsonBytes,
            manifestName = manifestName, manifestBytes = manifestBytes)
        runCatching { exportWavFilterCsvIfEnabled(context, json) }
            .onFailure { DebugLogger.log(context, "WAV_FILTER_CSV_FAILED", "error=${it.javaClass.simpleName}") }
        Saved(id, archived.wavFileName, jsonName, archived)
    }
    suspend fun saveObserverSidecar(context: Context, data: ByteArray, id: String, wavName: String,
        startedMs: Long, gainDb: Int, source: String, device: String,
        config: StatisticsWindowEngine.Configuration): Saved = withContext(Dispatchers.IO) {
        require(data.size >= 44 && String(data, 0, 4, Charsets.US_ASCII) == "RIFF")
        val pcm = ShortArray((data.size - 44) / 2) { i ->
            (((data[45 + i*2].toInt() shl 8) or (data[44 + i*2].toInt() and 255))).toShort()
        }
        val report = StatisticsWindowEngine.analyze(pcm, gainDb.toDouble(), config)
        val json = analysisJson(id, "OBSERVER", wavName, startedMs,
            startedMs + pcm.size * 1000L / 1000L, source, device, gainDb.toDouble(), null, report, null)
        json.put("filter_band_plan", "OBSERVER_LOCAL_DEFAULT_10_BANDS_OVER_STATISTICS_RANGE_NOT_ANALYZER_SETUP")
        json.put("wav_sha256", hash(data))
        json.put("waveform_resampling", "EXISTING_OBSERVER_8KHZ_TO_1KHZ")
        val name = "LFN_${id}_statistics.json"
        publish(context, json.toString(2).toByteArray(Charsets.UTF_8), name, "application/json")
        runCatching { exportWavFilterCsvIfEnabled(context, json) }
            .onFailure { DebugLogger.log(context, "WAV_FILTER_CSV_FAILED", "error=${it.javaClass.simpleName}") }
        Saved(id, null, name, analysisJson = json.toString())
    }
}
