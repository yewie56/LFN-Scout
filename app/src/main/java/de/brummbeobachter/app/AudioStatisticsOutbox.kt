package de.brummbeobachter.app

import android.content.Context
import org.json.JSONObject
import java.io.File

/** Persistent, key-free queue. Requests are replayed with the current observer key. */
internal object AudioStatisticsOutbox {
    private fun directory(context: Context): File =
        File(context.applicationContext.filesDir, "audio_statistics_outbox").also { check(it.isDirectory || it.mkdirs()) }

    private fun file(context: Context, request: JSONObject): File {
        val id = request.getString("recording_id")
        require(Regex("[A-Za-z0-9_-]{1,128}").matches(id))
        val kind = request.getString("set_kind")
        require(kind == "overall_statistics" || kind == "filter_statistics")
        val index = request.getInt("set_index")
        require(if (kind == "overall_statistics") index == 0 else index in 1..40)
        val version = request.getInt("lookup_version")
        require(version >= 1)
        return File(directory(context), "${id}_${kind}_${index}_${version}.json")
    }

    @Synchronized
    fun enqueue(context: Context, requests: List<JSONObject>): Int {
        var added = 0
        for (input in requests) {
            val request = JSONObject(input.toString())
            request.remove("key") // Observer secrets are never persisted in the outbox.
            val destination = file(context, request)
            if (destination.exists() || File(destination.parentFile, destination.nameWithoutExtension + ".ready").exists()) continue
            val temp = File(destination.parentFile, "${destination.name}.tmp")
            try {
                temp.writeText(request.toString(), Charsets.UTF_8)
                check(temp.renameTo(destination)) { "Cannot commit statistics queue entry" }
                added++
            } finally { if (temp.exists()) temp.delete() }
        }
        return added
    }

    @Synchronized
    fun markRecordingUploaded(context: Context, recordingId: String) {
        require(Regex("[A-Za-z0-9_-]{1,128}").matches(recordingId))
        directory(context).listFiles { f ->
            f.isFile && f.name.startsWith("${recordingId}_") && f.extension == "json"
        }?.forEach { pending ->
            val ready = File(pending.parentFile, pending.nameWithoutExtension + ".ready")
            check(pending.renameTo(ready)) { "Cannot make statistics entry ready" }
        }
    }

    /** Successful POST removes exactly one request; failed POST remains queued. */
    suspend fun drain(context: Context, client: SupabaseClient, key: String, maxRequests: Int = 50): Pair<Int, Int> {
        if (key.isBlank()) return 0 to pending(context)
        var sent = 0
        val files = directory(context).listFiles { f -> f.isFile && f.extension == "ready" }
            ?.sortedBy { it.name }?.take(maxRequests).orEmpty()
        for (file in files) {
            val request = try { JSONObject(file.readText(Charsets.UTF_8)) }
                catch (_: Exception) { continue } // preserve corrupt evidence; never silently delete
            request.put("key", key.trim())
            if (client.submitAudioStatistics(request)) {
                if (file.delete()) sent++
            }
        }
        return sent to pending(context)
    }

    fun pending(context: Context): Int = directory(context)
        .listFiles { f -> f.isFile && (f.extension == "json" || f.extension == "ready") }?.size ?: 0
}
