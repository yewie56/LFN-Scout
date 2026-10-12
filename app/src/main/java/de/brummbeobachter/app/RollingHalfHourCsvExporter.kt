package de.brummbeobachter.app

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import java.util.Locale

/** One small, directly accessible CSV per completed half-hour; no per-second flash writes.
 * In-place replacement by stable filename avoids duplicate rows on repeated completion.
 * Export remains diagnostic until the DSP frequency response has been validated.
 */
internal object RollingHalfHourCsvExporter {
    fun write(context: Context, result: RollingHalfHourStatistics.HalfHourResult, diagnostic: RollingImpulseDiagnostics.Result? = null) {
        try {
            if (Build.VERSION.SDK_INT < 29) {
                DebugLogger.log(context, "ROLLING_HALF_HOUR_CSV_ERROR", "API below 29 not supported")
                return
            }
            val name = "HalfHourStatistics_${result.startEpochMs}.csv"
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
            val observations = LocalHalfHourObservations.forBucket(context, result.startEpochMs, result.endEpochMs)
            val header = "bucket_start_ms;bucket_end_ms;min_hz;max_hz;valid_epochs;coverage_seconds;leq_dbfs;l50_dbfs;l90_dbfs;quality_status;reference;peak_second_dbfs;top_1s_energy_pct;top_10s_energy_pct;leq_minus_l50_db;observation_mean;observation_count;observation_min;observation_max;observation_unavailable_count\n"
            fun number(value: Double?) = value?.let { String.format(Locale.ROOT, "%.5f", it) } ?: ""
            val row = "${result.startEpochMs};${result.endEpochMs};${result.minimumHz};${result.maximumHz};" +
                "${result.validEpochs};${result.validEpochs};${number(result.leqDbfs)};" +
                "${number(result.l50Dbfs)};${number(result.l90Dbfs)};${result.qualityStatus};PRE_GAIN_PCM_DIAGNOSTIC;" +
                "${number(diagnostic?.peakSecondDbfs)};${number(diagnostic?.topOneEnergyPercent)};" +
                "${number(diagnostic?.topTenEnergyPercent)};${number(diagnostic?.leqMinusL50Db)};" +
                "${number(observations.mean)};${observations.count};${observations.minimum ?: ""};" +
                "${observations.maximum ?: ""};${observations.unavailable}\n"
            resolver.openOutputStream(uri, "wt")?.bufferedWriter(Charsets.UTF_8)?.use {
                it.write(header); it.write(row)
            } ?: error("Cannot write $uri")
            resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
            DebugLogger.log(context, "ROLLING_HALF_HOUR_CSV_OK", "name=$name epochs=${result.validEpochs}")
        } catch (e: Exception) {
            DebugLogger.log(context, "ROLLING_HALF_HOUR_CSV_ERROR", "type=${e.javaClass.simpleName} message=${e.message}")
        }
    }
}
