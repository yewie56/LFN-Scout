package de.brummbeobachter.app

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Read-only copies of the analyzer's PRIVATE preference-backed setups.
 * Legacy v1 imports are handled by AnalyzerLegacySetupMigration before the first v2 export.
 * Only explicitly allowlisted stores/keys are exported.
 * Never put observation queues, account IDs, auth or service keys in Downloads.
 */
internal object AnalyzerInspectionExport {
    private const val PREFS = "lfn_scout_live_analyzer"
    private const val EXPORT_PREFS = "lfn_scout_inspection_export"
    private val FOLDER = PublicDataDirectory.setupFolder
    private val DIAGNOSTICS_FOLDER = PublicDataDirectory.diagnosticFolder

    private fun safeScalar(value: Any?): Any? = when (value) {
        is String, is Boolean, is Int, is Long, is Float, is Double -> value
        else -> null
    }

    /** Single, ordered allowlist for both modes. A missing old value stays JSON null;
     * NEVER silently replace absent setup data with a live mode value or a guessed default.
     * Keys retain their v1 spelling for straightforward comparisons and future migration.
     */
    private val setupFields: LinkedHashMap<String, String> = linkedMapOf(
        "min_frequency" to "spectrum", "range" to "spectrum", "decimation_factor" to "spectrum",
        "fft_size" to "spectrum", "fft_hop" to "spectrum", "window" to "spectrum",
        "smoothing_enabled" to "spectrum", "smoothing_time" to "spectrum",
        "direct_smoothing_alpha" to "spectrum", "direct_time_smoothing" to "spectrum",
        "direct_peak_markers" to "spectrum", "current" to "spectrum",
        "average" to "spectrum", "floor" to "spectrum", "max_hold" to "spectrum",
        "direct_audio_source" to "audio", "direct_sample_rate" to "audio",
        "direct_stay_awake" to "audio", "direct_subtract_dc" to "audio",
        "direct_test1_on" to "audio", "direct_test1_hz" to "audio",
        "direct_test2_on" to "audio", "direct_test2_hz" to "audio", "gain" to "audio",
        "display_min" to "display", "display_max" to "display",
        "history_seconds" to "display", "water_interval" to "display", "water_mode" to "display",
        "stat_min" to "statistics", "stat_max" to "statistics",
        "stat_window" to "statistics", "stat_average" to "statistics",
        "filter_min_hz" to "filterBank", "filter_max_hz" to "filterBank",
        "filter_bands" to "filterBank", "filter_display_min" to "filterBank",
        "filter_display_max" to "filterBank", "filter_auto_duration" to "filterBank",
        "filter_auto_margin" to "filterBank", "filter_show_original" to "filterBank",
        "filter_show_filter" to "filterBank",
        "autorange_duration" to "automations", "autorange_margin" to "automations",
        "auto" to "automations", "image_format" to "automations", "jpg_quality" to "automations"
    )

    /** Public projection of an explicitly saved setup, not an active-setting snapshot. */
    private fun setupContent(context: Context, slot: Int): String? {
        require(slot in 1..4)
        val all = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).all
        val prefix = "setup_${slot}_"
        if (!all.containsKey("${prefix}range")) return null
        val direct = slot == 4
        val fields = JSONObject()
        val availability = JSONObject()
        val provenance = JSONObject()
        val groups = JSONObject()
        setupFields.forEach { (key, group) ->
            val storageKey = prefix + key
            val value = if (all.containsKey(storageKey)) safeScalar(all[storageKey]) else null
            fields.put(key, value ?: JSONObject.NULL)
            provenance.put(key, if (value == null) "MISSING_IN_SAVED_SLOT" else "SAVED_SLOT")
            availability.put(key, when {
                key.startsWith("filter_") && direct -> "UNSUPPORTED"
                key.startsWith("direct_") && !direct -> when (key) {
                    "direct_sample_rate" -> "FIXED"
                    "direct_audio_source" -> "FIXED"
                    else -> "UNSUPPORTED"
                }
                else -> "AVAILABLE"
            })
            // Same group and field names for both analyzer modes.
            val objectForGroup = groups.optJSONObject(group) ?: JSONObject().also { groups.put(group, it) }
            objectForGroup.put(key, value ?: JSONObject.NULL)
        }
        val name = (all[prefix + "name"] as? String).orEmpty()
        val description = (all[prefix + "description"] as? String).orEmpty()
        return JSONObject().apply {
            put("schema", "lfn-scout-analyzer-setup-export-v2")
            put("schemaVersion", 2)
            put("slot", slot)
            put("mode", if (direct) "DIRECT" else "8K")
            put("source", PREFS)
            put("read_only_copy", true)
            put("name", name)
            put("description", description)
            put("settings", fields)
            put("groups", groups)
            put("availability", availability)
            put("provenance", provenance)
            put("notes", "Missing values remain null; do not interpret as disabled or as live settings. Unsupported keys are not active in this mode. Effective runtime configuration is in Diagnostics/Settings/analyzer_live.json.")
        }.toString(2) + "\n"
    }

    private fun writePublic(context: Context, folder: String, name: String, mime: String, data: ByteArray): String {
        val app = context.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, mime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/" + folder)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            // Reuse our own existing filename when possible (MediaStore.insert otherwise
            // creates numbered duplicates on each setup save).
            val existing = if (folder == FOLDER) runCatching {
                app.contentResolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.MediaColumns._ID),
                    "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
                    arrayOf(name, Environment.DIRECTORY_DOWNLOADS + "/" + folder), null)?.use { cursor ->
                    if (cursor.moveToFirst()) android.content.ContentUris.withAppendedId(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, cursor.getLong(0)) else null
                }
            }.getOrNull() else null
            val uri = existing ?: app.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Datei konnte nicht angelegt werden: $name")
            try {
                app.contentResolver.openOutputStream(uri, "rwt")?.use { it.write(data) }
                    ?: error("Datei konnte nicht geöffnet werden: $name")
                app.contentResolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
            } catch (e: Exception) {
                if (existing == null) runCatching { app.contentResolver.delete(uri, null, null) }
                throw e
            }
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folder)
            check(dir.exists() || dir.mkdirs()) { "Exportordner konnte nicht erstellt werden" }
            File(dir, name).writeBytes(data)
        }
        return "Download/$folder$name"
    }

    /** Called once after update, and again after saving a setup. Not a second source of truth. */
    @Synchronized fun exportSetups(context: Context): Int {
        AnalyzerLegacySetupMigration.run(context)
        val pending = AnalyzerLegacySetupMigration.pendingSlots(context)
        var count = 0
        for (slot in 1..4) {
            val content = setupContent(context, slot) ?: continue
            // Never open/truncate a legacy filename while its migration is unresolved.
            // Export the current private configuration under a separate, unmistakable name.
            val name = if (slot in pending) {
                if (slot == 4) "Setup_4_48k_CURRENT_v2.json" else "Setup_${slot}_CURRENT_v2.json"
            } else if (slot == 4) "Setup_4_48k.json" else "Setup_$slot.json"
            writePublic(context, FOLDER, name, "application/json", content.toByteArray(Charsets.UTF_8))
            count++
        }
        return count
    }

    fun exportOnceAfterUpdate(context: Context) {
        val flag = context.getSharedPreferences(EXPORT_PREFS, Context.MODE_PRIVATE)
        if (flag.getBoolean("v0_5_16_export_done", false)) return
        // Don't mark done if the MediaStore export fails; allow retry next launch.
        exportSetups(context)
        flag.edit().putBoolean("v0_5_16_export_done", true).apply()
    }

    private fun preferencesJson(context: Context, store: String, keyFilter: (String) -> Boolean): ByteArray {
        val result = JSONObject()
        val all = context.getSharedPreferences(store, Context.MODE_PRIVATE).all
        all.keys.filter(keyFilter).sorted().forEach { key ->
            safeScalar(all[key])?.let { result.put(key, it) }
        }
        return (result.toString(2) + "\n").toByteArray(Charsets.UTF_8)
    }

    /** Explicit manual diagnostic export. Deliberately no raw app-private file dump. */
    fun exportDiagnostics(context: Context): String {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            fun add(path: String, data: ByteArray) {
                zip.putNextEntry(ZipEntry(path))
                zip.write(data)
                zip.closeEntry()
            }
            val manifest = """LFN-Scout v0.5.16 diagnostic export
Only allowlisted analyzer/test/impulse preferences and AUDIO_DIAG lines.
No observation records, auth information, GPS, raw audio or full private files.
JSON setup files are read-only copies. Missing slots are omitted.
"""
            add("README.txt", manifest.toByteArray(Charsets.UTF_8))
            for (slot in 1..4) {
                val content = setupContent(context, slot) ?: continue
                add("Setups/" + if (slot == 4) "Setup_4_48k.json" else "Setup_$slot.json", content.toByteArray(Charsets.UTF_8))
            }
            add("Settings/analyzer_live.json", preferencesJson(context, PREFS) { key ->
                // Only known analyzer keys, never unrestricted app store copying.
                key.startsWith("setup_") || key.startsWith("mode_8_") || key.startsWith("mode_48_") ||
                    key in setOf("decimation_factor", "statistics_min_hz", "statistics_max_hz",
                        "statistics_window_sec", "statistics_average_sec", "range_hz", "min_frequency_hz",
                        "fft_size", "fft_window", "smoothing_enabled", "smoothing_time_sec", "auto_png",
                        "image_format", "jpg_quality", "analyzer_gain_db", "fft_hop_samples",
                        "waterfall_interval_ms", "history_seconds", "waterfall_mode", "display_min_db",
                        "display_max_db", "autorange_duration_sec", "autorange_margin_db",
                        "show_current", "show_average", "show_max_hold", "show_floor", "last_setup_slot")
            })
            add("Settings/setup_migration.json", JSONObject().apply {
                val pending = AnalyzerLegacySetupMigration.pendingSlots(context)
                put("pending_slots", org.json.JSONArray(pending.toList()))
                put("error", AnalyzerLegacySetupMigration.errorSummary(context))
                put("note", "Pending legacy files were not deleted and are never overwritten by normal-name export.")
            }.toString(2).toByteArray(Charsets.UTF_8))
            add("Settings/audio_mode_state.json", preferencesJson(context, "analyzer_audio_mode_session") {
                it == "original_rolling" || it == "restore_pending"
            })
            add("Settings/test_signal.json", preferencesJson(context, "analyzer_test_signal") { it in setOf("frequency_hz", "frequency2_hz", "level_dbfs", "two_tones", "enabled", "test_frequency", "test_level") })
            add("Settings/impulse_diagnostic.json", preferencesJson(context, "lfn_scout_impulse_diagnostic") {
                // Settings for the local diagnostic only; omit anything suggestive of user IDs.
                it in setOf("gain_db", "threshold", "threshold_factor", "noise_factor", "minimum_threshold")
            })
            val audioTrace = DebugLogger.readLines(context).filter { it.contains("AUDIO_DIAG_") }
                .map(DebugLogger::sanitizeForUser).joinToString("\n", postfix = "\n")
            add("Diagnostics/audio_trace.txt", audioTrace.toByteArray(Charsets.UTF_8))
        }
        val stamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS", Locale.US).format(Date())
        return writePublic(context, DIAGNOSTICS_FOLDER, "LFN-Scout_Diagnose_$stamp.zip", "application/zip", out.toByteArray())
    }
}
