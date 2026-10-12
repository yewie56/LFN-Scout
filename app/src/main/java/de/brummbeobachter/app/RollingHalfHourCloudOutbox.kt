package de.brummbeobachter.app

import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.UUID

/** Durable, key-free upload queue for acoustic overall 30-minute buckets only.
 * Never contains subjective ratings, filter bands, epoch samples or impulse diagnostics.
 */
internal object RollingHalfHourCloudOutbox {
    private val drainMutex = Mutex()
    private const val RETENTION_MS: Long = 24L * 60L * 60L * 1000L
    private const val SENT_PENDING_DELETE = "SENT_PENDING_DELETE"
    internal fun deleteAfterMs(confirmedMs: Long): Long? =
        if (confirmedMs > 0L && confirmedMs <= Long.MAX_VALUE - RETENTION_MS) confirmedMs + RETENTION_MS else null
    // Status and confirmed timestamp are persisted alongside the original payload.
    internal fun isExpired(sentAtEpochMs: Long, nowEpochMs: Long): Boolean =
        deleteAfterMs(sentAtEpochMs)?.let { nowEpochMs >= it } ?: false
    private fun saveAtomic(file: File, json: JSONObject) {
        val tmp = File(file.parentFile, file.name + ".tmp")
        try {
            tmp.writeText(json.toString(), Charsets.UTF_8)
            check(tmp.renameTo(file)) { "Cloud-Status konnte nicht gespeichert werden" }
        } finally { if (tmp.exists()) tmp.delete() }
    }

    private fun folder(context: Context): File = File(context.filesDir, "half_hour_cloud_outbox")
        .also { check(it.isDirectory || it.mkdirs()) }
    private fun fingerprint(key: String): String = MessageDigest.getInstance("SHA-256")
        .digest(key.trim().toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    internal fun payload(result: RollingHalfHourStatistics.HalfHourResult, observerHash: String): JSONObject {
        val stableName = "lfn-scout/half-hour/overall/$observerHash/${result.startEpochMs}/${result.minimumHz}-${result.maximumHz}"
        val id = UUID.nameUUIDFromBytes(stableName.toByteArray(Charsets.UTF_8)).toString()
        fun iso(ms: Long): String = java.time.Instant.ofEpochMilli(ms).toString()
        return JSONObject().apply {
            put("bucket_id", id)
            put("bucket_start", iso(result.startEpochMs))
            put("bucket_end", iso(result.endEpochMs))
            put("statistics_source", "ROLLING")
            put("range_min_hz", result.minimumHz)
            put("range_max_hz", result.maximumHz)
            put("coverage_seconds", result.validEpochs)
            put("valid_epoch_count", result.validEpochs)
            put("quality_status", result.qualityStatus)
            put("lookup_version", 1)
            put("app_version", "0.5.38")
            put("observer_fingerprint", observerHash)
            if (result.leqDbfs != null) put("leq_dbfs", result.leqDbfs)
            if (result.l50Dbfs != null) put("l50_dbfs", result.l50Dbfs)
            if (result.l90Dbfs != null) put("l90_dbfs", result.l90Dbfs)
        }
    }

    @Synchronized
    fun enqueue(context: Context, result: RollingHalfHourStatistics.HalfHourResult, key: String): Boolean {
        if (key.isBlank()) return false
        val request = payload(result, fingerprint(key))
        val dest = File(folder(context), request.getString("bucket_id") + ".json")
        if (dest.exists()) return true
        request.put("local_queue_status", "PENDING")
        saveAtomic(dest, request)
        return true
    }

    fun count(context: Context): Int = folder(context).listFiles { f -> f.extension == "json" }?.size ?: 0

    /** Attempts bounded replay. Failed entries remain untouched for the next service run/bucket. */
    suspend fun drain(context: Context, client: SupabaseClient, key: String, max: Int = 30): Pair<Int, Int> = drainMutex.withLock {
        if (key.isBlank()) return@withLock 0 to count(context)
        val hash = fingerprint(key)
        var sent = 0
        val files = folder(context).listFiles { f -> f.isFile && f.extension == "json" }
            ?.sortedBy { it.name }.orEmpty()
        var attempted = 0
        var retained = 0
        var pendingCount = 0
        var skippedOtherObserver = 0
        var unreadable = 0
        var deletedExpired = 0
        var earliestDeleteMs: Long? = null
        val now = System.currentTimeMillis()
        for (f in files) {
            val json = try { JSONObject(f.readText(Charsets.UTF_8)) } catch (_: Exception) { unreadable++; continue }
            // A different participant's historical queue must never be sent using a new key.
            if (json.optString("observer_fingerprint") != hash) { skippedOtherObserver++; continue }
            if (json.optString("local_queue_status") == SENT_PENDING_DELETE) {
                val confirmed = json.optLong("cloud_confirmed_at_ms", 0L)
                if (isExpired(confirmed, now)) {
                    if (f.delete()) deletedExpired++ else {
                        retained++
                        earliestDeleteMs = if (earliestDeleteMs == null || confirmed < earliestDeleteMs) confirmed else earliestDeleteMs
                    }
                } else {
                    retained++
                    val due = deleteAfterMs(confirmed)
                    if (due != null && (earliestDeleteMs == null || due < earliestDeleteMs)) earliestDeleteMs = due
                }
                continue
            }
            pendingCount++
            if (attempted >= max) continue
            attempted++
            val wire = JSONObject(json.toString()).apply {
                remove("observer_fingerprint")
                remove("local_queue_status")
                remove("cloud_confirmed_at_ms")
                put("key", key.trim())
            }
            if (client.submitHalfHourStatistics(wire)) {
                json.put("local_queue_status", SENT_PENDING_DELETE)
                json.put("cloud_confirmed_at_ms", System.currentTimeMillis())
                saveAtomic(f, json)
                sent++
                pendingCount--
                retained++
                val due = deleteAfterMs(json.getLong("cloud_confirmed_at_ms"))
                if (due != null && (earliestDeleteMs == null || due < earliestDeleteMs)) earliestDeleteMs = due
            }
        }
        DebugLogger.log(context, "ROLLING_CLOUD_QUEUE_STATUS", "newly_sent=$sent retained_sent=$retained total_local=${count(context)} pending_upload=$pendingCount deleted_expired=$deletedExpired other_observer=$skippedOtherObserver unreadable=$unreadable next_delete_at_ms=${earliestDeleteMs ?: 0L}")
        sent to count(context)
    }

    suspend fun enqueueAndDrain(context: Context, result: RollingHalfHourStatistics.HalfHourResult) {
        try {
            val key = KeyStore(context).keyFlow.first().trim()
            if (!enqueue(context, result, key)) {
                DebugLogger.log(context, "ROLLING_CLOUD_PENDING", "reason=missing_observer_key")
                return
            }
            val (sent, pending) = drain(context, SupabaseClient(), key)
            DebugLogger.log(context, "ROLLING_CLOUD_SYNC", "sent=$sent pending=$pending")
        } catch (e: Exception) {
            DebugLogger.log(context, "ROLLING_CLOUD_ERROR", "type=${e.javaClass.simpleName} message=${e.message}")
        }
    }

    suspend fun retry(context: Context) {
        try {
            val key = KeyStore(context).keyFlow.first().trim()
            if (key.isBlank()) return
            val (sent, pending) = drain(context, SupabaseClient(), key)
            if (sent > 0 || pending > 0) DebugLogger.log(context, "ROLLING_CLOUD_RETRY", "sent=$sent pending=$pending")
        } catch (e: Exception) {
            DebugLogger.log(context, "ROLLING_CLOUD_RETRY_ERROR", "type=${e.javaClass.simpleName}")
        }
    }
}
