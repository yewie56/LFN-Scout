package de.brummbeobachter.app

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import java.util.Locale

/** One directly accessible, bounded, semicolon-delimited CSV per completed bucket. */
internal object RollingFilterHalfHourCsvExporter {
    fun write(context: Context, rows: List<RollingFilterHalfHourStatistics.BandResult>) {
        if (rows.isEmpty()) return
        try {
            if (Build.VERSION.SDK_INT < 29) error("MediaStore Downloads requires API 29")
            val name = "HalfHourFilterStatistics_${rows.first().startEpochMs}.csv"
            val resolver = context.contentResolver
            val relative = "Download/LFN-Scout/"
            var existing: android.net.Uri? = null
            resolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns._ID),
                "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
                arrayOf(name, relative), null)?.use { cursor ->
                if (cursor.moveToFirst()) existing = ContentUris.withAppendedId(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, cursor.getLong(0))
            }
            val uri = existing ?: resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relative)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }) ?: error("MediaStore insert returned null")
            fun num(v: Double?) = v?.let { String.format(Locale.ROOT, "%.5f", it) } ?: ""
            resolver.openOutputStream(uri, "wt")?.bufferedWriter(Charsets.UTF_8)?.use { output ->
                output.write("bucket_start_ms;bucket_end_ms;band_index;min_hz;max_hz;valid_epochs;coverage_seconds;leq_dbfs;l50_dbfs;l90_dbfs;quality_status;reference\n")
                rows.forEach { row ->
                    output.write("${row.startEpochMs};${row.endEpochMs};${row.bandIndex};${num(row.minimumHz)};${num(row.maximumHz)};${row.validEpochs};${row.validEpochs};${num(row.leqDbfs)};${num(row.l50Dbfs)};${num(row.l90Dbfs)};${row.qualityStatus};PRE_GAIN_PCM_FILTER_DIAGNOSTIC\n")
                }
            } ?: error("Cannot open $uri")
            resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
            DebugLogger.log(context, "ROLLING_FILTER_CSV_OK", "name=$name bands=${rows.size}")
        } catch (e: Exception) {
            DebugLogger.log(context, "ROLLING_FILTER_CSV_ERROR", "type=${e.javaClass.simpleName} message=${e.message}")
        }
    }
}
