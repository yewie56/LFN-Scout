package de.brummbeobachter.app

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import java.util.Locale
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Optional 1-second diagnostic export. RAM-buffered, snapshot every 300 epochs and at bucket close.
 * No per-second flash writes. Separate from Cloud and from WAV saving. */
internal class RollingEpochCsvExporter(private val context: Context, private val includeFilters: Boolean) {
    private data class Row(val epochMs: Long, var overall: Double? = null, var filters: List<Double>? = null)
    private val rows = sortedMapOf<Long, Row>()
    private val writeMutex = Mutex()
    private val lastWrittenSize = mutableMapOf<Long, Int>()
    private var bucketMs = -1L
    @Synchronized fun overall(epoch: Long, value: Double): Snapshot? {
        if (epoch < 0) return null
        val bucket = Math.floorDiv(epoch, 1_800_000L) * 1_800_000L
        if (bucketMs != bucket) { bucketMs = bucket; rows.keys.removeAll { it < bucket - 1_800_000L } }
        rows.getOrPut(epoch) { Row(epoch) }.overall = value
        return if (!includeFilters && rows.keys.count { it >= bucket && it < bucket + 1_800_000L } % 300 == 0) snapshot(bucket) else null
    }
    @Synchronized fun filters(epoch: Long, values: List<Double>): Snapshot? {
        if (epoch < 0 || !includeFilters) return null
        val bucket = Math.floorDiv(epoch, 1_800_000L) * 1_800_000L
        if (bucketMs != bucket) { bucketMs = bucket; rows.keys.removeAll { it < bucket - 1_800_000L } }
        rows.getOrPut(epoch) { Row(epoch) }.filters = values.toList()
        return if (rows.keys.count { it >= bucket && it < bucket + 1_800_000L } % 300 == 0) snapshot(bucket) else null
    }
    @Synchronized fun flush(bucket: Long): Snapshot? = if (rows.keys.any { Math.floorDiv(it, 1_800_000L) * 1_800_000L == bucket }) snapshot(bucket) else null
    data class Snapshot(val bucket: Long, val content: String, val rowCount: Int)
    private fun snapshot(bucket: Long): Snapshot {
        val selected = rows.values.filter { it.epochMs >= bucket && it.epochMs < bucket + 1_800_000L }
        val columns = if (includeFilters) (1..(selected.firstOrNull { it.filters != null }?.filters?.size ?: 0))
            .joinToString("") { ";filter_${it}_dbfs" } else ""
        fun num(v: Double?) = v?.let { String.format(Locale.ROOT, "%.5f", it) } ?: ""
        return Snapshot(bucket, buildString {
            append("epoch_start_ms;overall_dbfs$columns;reference\n")
            selected.forEach { row ->
                append(row.epochMs).append(';').append(num(row.overall))
                if (includeFilters) {
                    val width = selected.firstOrNull { it.filters != null }?.filters?.size ?: 0
                    for (i in 0 until width) append(';').append(num(row.filters?.getOrNull(i)))
                }
                append(";PRE_GAIN_PCM_DIAGNOSTIC\n")
            }
        }, selected.size)
    }
    suspend fun write(snapshot: Snapshot) = writeMutex.withLock {
        if ((lastWrittenSize[snapshot.bucket] ?: -1) > snapshot.rowCount) return@withLock
        try {
            val coverage = RollingEpochCsvIntegrity.verify(snapshot.content, snapshot.bucket, snapshot.rowCount)
            if (Build.VERSION.SDK_INT < 29) error("Android 10+ required for public Downloads")
            val name = "HalfHourEpochs_${snapshot.bucket}.csv"
            val resolver = context.contentResolver
            val relative = PublicDataDirectory.relativePath
            var uri: android.net.Uri? = null
            resolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns._ID),
                "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
                arrayOf(name, relative), null)?.use { c ->
                if (c.moveToFirst()) uri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, c.getLong(0))
            }
            val destination = uri ?: resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relative)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }) ?: error("MediaStore insert failed")
            resolver.openOutputStream(destination, "wt")?.bufferedWriter(Charsets.UTF_8)?.use { it.write(snapshot.content) }
                ?: error("MediaStore write failed")
            val persisted = resolver.openInputStream(destination)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: error("Cannot read back epoch CSV")
            val persistedCoverage = RollingEpochCsvIntegrity.verify(persisted, snapshot.bucket, snapshot.rowCount)
            check(persisted == snapshot.content && persistedCoverage == coverage) {
                "Epoch CSV content/coverage changed during persistence"
            }
            val updated = resolver.update(destination,
                ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
            check(updated > 0) { "Epoch CSV publication failed" }
            // An object can change its visibility/storage representation when
            // IS_PENDING is cleared. Verify the public object, not just pending bytes.
            val published = resolver.openInputStream(destination)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: error("Cannot read published epoch CSV")
            val publicCoverage = RollingEpochCsvIntegrity.verify(published, snapshot.bucket, snapshot.rowCount)
            check(published == snapshot.content && publicCoverage == coverage) {
                "Published epoch CSV differs from verified source"
            }
            lastWrittenSize[snapshot.bucket] = snapshot.rowCount
            DebugLogger.log(context, "ROLLING_EPOCH_CSV_OK",
                "bucket=${snapshot.bucket} distinct_seconds=${coverage.distinctSeconds} missing_seconds=${coverage.missingSeconds} verified=true")
        } catch (e: Exception) {
            DebugLogger.log(context, "ROLLING_EPOCH_CSV_ERROR", "type=${e.javaClass.simpleName} message=${e.message}")
        }
    }
}
