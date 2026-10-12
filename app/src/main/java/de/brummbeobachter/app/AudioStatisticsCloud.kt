package de.brummbeobachter.app

import android.content.Context
import org.json.JSONObject

/** Reuses the persisted local analysis, never recomputes or changes gain. */
internal object AudioStatisticsCloud {
    private val metricNames = listOf(
        "leq_dbfs", "lmin_dbfs", "lmax_dbfs", "l1_dbfs", "l10_dbfs", "l50_dbfs",
        "l90_dbfs", "l95_dbfs", "l99_dbfs", "mean_level_dbfs", "level_stddev_db",
        "crest_factor_db", "kurtosis_excess", "valid_epochs", "dominant_frequency_hz"
    )

    internal fun requests(key: String, entry: AudioArchiveEntry, analysis: JSONObject): List<JSONObject> {
        if (analysis.optString("recording_id") != entry.recordingId) return emptyList()
        if (analysis.optString("schema") != "LFN_AUDIO_LOCAL_V1") return emptyList()
        val duration = analysis.optDouble("duration_s")
        if (!duration.isFinite() || duration < 0.0) return emptyList()
        val result = mutableListOf<JSONObject>()
        fun build(kind: String, index: Int, low: Double, high: Double, levels: JSONObject): JSONObject? {
            if (!low.isFinite() || !high.isFinite() || high <= low) return null
            val metrics = JSONObject()
            for (name in metricNames) {
                if (!levels.has(name) || levels.isNull(name)) continue
                val v = levels.optDouble(name, Double.NaN)
                if (v.isFinite() && (name != "valid_epochs" || v >= 0 && v % 1.0 == 0.0)) {
                    metrics.put(name, v)
                }
            }
            return JSONObject().apply {
                put("key", key.trim())
                put("recording_id", entry.recordingId)
                put("set_kind", kind)
                put("set_index", index)
                put("lookup_version", 1)
                put("duration_s", duration)
                put("range_min_hz", low)
                put("range_max_hz", high)
                put("metrics", metrics)
                put("metadata", JSONObject().apply {
                    put("local_schema", analysis.optString("schema"))
                    put("level_reference", analysis.optString("level_reference"))
                    put("filter_provenance", analysis.optString("filter_provenance"))
                })
            }
        }
        val low = analysis.optDouble("statistics_min_hz")
        val high = analysis.optDouble("statistics_max_hz")
        val overall = analysis.optJSONObject("overall") ?: return emptyList()
        val spec = analysis.optJSONObject("spectral")
        if (spec != null && spec.has("dominant_frequency_hz") && !spec.isNull("dominant_frequency_hz")) {
            val dominant = spec.optDouble("dominant_frequency_hz", Double.NaN)
            if (dominant.isFinite()) overall.put("dominant_frequency_hz", dominant)
        }
        result.add(build("overall_statistics", 0, low, high, overall) ?: return emptyList())
        val bands = analysis.optJSONArray("bands") ?: return result
        if (bands.length() > 40) return emptyList()
        val seen = mutableSetOf<Int>()
        for (i in 0 until bands.length()) {
            val band = bands.optJSONObject(i) ?: return emptyList()
            val index = band.optInt("index", -1) + 1
            if (index !in 1..40 || !seen.add(index)) return emptyList()
            result.add(build("filter_statistics", index, band.optDouble("low_hz"),
                band.optDouble("high_hz"), band.optJSONObject("levels") ?: return emptyList())
                ?: return emptyList())
        }
        return result
    }

    suspend fun submitExisting(context: Context, client: SupabaseClient, key: String,
                               entry: AudioArchiveEntry): Boolean {
        val analysis = AudioArchive.readStatisticsCompanion(context, entry) ?: return false
        val requests = requests(key, entry, analysis)
        if (requests.isEmpty()) return false
        AudioStatisticsOutbox.enqueue(context, requests)
        AudioStatisticsOutbox.markRecordingUploaded(context, entry.recordingId)
        val (_, remaining) = AudioStatisticsOutbox.drain(context, client, key)
        return remaining == 0
    }

    fun queueObserver(context: Context, entry: AudioArchiveEntry, analysisJson: String): Int {
        val requests = requests("", entry, JSONObject(analysisJson))
        return if (requests.isNotEmpty()) AudioStatisticsOutbox.enqueue(context, requests) else 0
    }
}
