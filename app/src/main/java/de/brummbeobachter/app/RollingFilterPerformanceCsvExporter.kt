package de.brummbeobachter.app

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import java.util.Locale

/** Separate diagnostic CSV; never changes the measurement/filter CSV or cloud schema. */
internal object RollingFilterPerformanceCsvExporter {
    private const val NAME = "RollingCsvFilterPerformance.csv"
    private val PATH = PublicDataDirectory.relativePath
    private const val HEADER = "window_start_ms;window_end_ms;method;bands;sample_rate_hz;samples;mean_us_per_sample;peak_us_per_sample;peak_epoch_us;p50_epoch_us;p95_epoch_us;p99_epoch_us;epoch_count;total_us;realtime_percent\n"

    @Synchronized
    fun append(context: Context, start: Long, end: Long, p: RollingFilterHalfHourStatistics.Performance) {
        try {
            if (Build.VERSION.SDK_INT < 29) error("MediaStore Downloads requires API 29")
            val resolver = context.contentResolver
            var found: android.net.Uri? = null
            resolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns._ID),
                "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
                arrayOf(NAME, PATH), null)?.use { c ->
                if (c.moveToFirst()) found = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, c.getLong(0))
            }
            val uri = found ?: resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, NAME)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, PATH)
                }) ?: error("MediaStore insert failed")
            fun n(v: Double) = String.format(Locale.ROOT, "%.5f", v)
            val row = "$start;$end;${p.methodName};${p.bandCount};${p.sampleRate};${p.samples};${n(p.meanMicrosPerSample)};${n(p.peakMicrosPerSample)};${n(p.peakEpochMicros)};${n(p.p50EpochMicros)};${n(p.p95EpochMicros)};${n(p.p99EpochMicros)};${p.epochCount};${n(p.totalMicros)};${n(p.realtimePercent)}\n"
            // MediaStore output stream mode 'wa' is append when supported by the provider.
            // On providers not supporting append, fail explicitly rather than silently overwriting results.
            val existing = found != null
            resolver.openOutputStream(uri, if (existing) "wa" else "w")?.bufferedWriter(Charsets.UTF_8)?.use {
                if (!existing) it.write(HEADER)
                it.write(row)
            } ?: error("Unable to open performance CSV")
            DebugLogger.log(context, "ROLLING_FILTER_PERF_CSV_OK", "method=${p.methodName} bands=${p.bandCount} samples=${p.samples}")
        } catch (e: Exception) {
            DebugLogger.log(context, "ROLLING_FILTER_PERF_CSV_ERROR", "type=${e.javaClass.simpleName} message=${e.message}")
        }
    }
}
