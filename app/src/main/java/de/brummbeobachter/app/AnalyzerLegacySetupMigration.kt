package de.brummbeobachter.app

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import org.json.JSONObject
import java.io.File

/** Conservative, one-time migration of app-owned legacy JSON exports.
 * A conflict, unreadable file, or failed readback NEVER results in deletion.
 * No external picker or broad-storage permission is requested.
 */
internal object AnalyzerLegacySetupMigration {
    private const val STORE = "lfn_scout_live_analyzer"
    private const val STATUS = "lfn_scout_setup_migration"
    private const val DIRECTORY = "LFN-Scout/Setups/"
    private val files = (1..4).associateWith { if (it == 4) "Setup_4_48k.json" else "Setup_$it.json" }
    private val allowed = setOf(
        "min_frequency", "range", "decimation_factor", "fft_size", "fft_hop", "window",
        "smoothing_enabled", "smoothing_time", "direct_smoothing_alpha", "direct_time_smoothing",
        "direct_peak_markers", "current", "average", "floor", "max_hold", "direct_audio_source",
        "direct_sample_rate", "direct_stay_awake", "direct_subtract_dc", "direct_test1_on",
        "direct_test1_hz", "direct_test2_on", "direct_test2_hz", "gain", "display_min",
        "display_max", "history_seconds", "water_interval", "water_mode", "stat_min", "stat_max",
        "stat_window", "stat_average", "filter_min_hz", "filter_max_hz", "filter_bands",
        "filter_display_min", "filter_display_max", "filter_auto_duration", "filter_auto_margin",
        "filter_show_original", "filter_show_filter", "autorange_duration", "autorange_margin",
        "auto", "image_format", "jpg_quality"
    )
    private val integerFields = setOf(
        "min_frequency", "range", "decimation_factor", "fft_size", "fft_hop", "direct_peak_markers",
        "direct_sample_rate", "gain", "display_min", "display_max", "history_seconds",
        "water_interval", "stat_min", "stat_max", "stat_window", "filter_min_hz", "filter_max_hz",
        "filter_bands", "filter_display_min", "filter_display_max", "filter_auto_duration",
        "filter_auto_margin", "autorange_duration", "autorange_margin", "jpg_quality"
    )
    private val floatFields = setOf("smoothing_time", "direct_smoothing_alpha", "direct_test1_hz",
        "direct_test2_hz", "stat_average")
    private val booleanFields = setOf("smoothing_enabled", "direct_time_smoothing", "current",
        "average", "floor", "max_hold", "direct_stay_awake", "direct_subtract_dc",
        "direct_test1_on", "direct_test2_on", "filter_show_original", "filter_show_filter", "auto")
    private val stringFields = allowed - integerFields - floatFields - booleanFields

    private fun value(key: String, input: Any): Any {
        return when (key) {
            in integerFields -> {
                require(input is Number && input.toDouble().isFinite() && input.toDouble() == input.toInt().toDouble())
                input.toInt()
            }
            in floatFields -> {
                require(input is Number && input.toDouble().isFinite() && input.toDouble() in -1e6..1e6)
                input.toFloat()
            }
            in booleanFields -> { require(input is Boolean); input }
            in stringFields -> { require(input is String && input.length <= 1024); input }
            else -> error("Unexpected setup field")
        }
    }
    private fun same(a: Any?, b: Any): Boolean = when {
        a is Number && b is Number -> a.toDouble() == b.toDouble()
        else -> a == b
    }

    private fun migrateOne(context: Context, slot: Int, jsonText: String, remove: () -> Boolean): Boolean {
        val obj = JSONObject(jsonText)
        val schema = obj.optString("schema")
        require(schema == "lfn-scout-analyzer-setup-export-v1" ||
            schema == "lfn-scout-analyzer-setup-export-v2") { "Unknown schema: $schema" }
        require(obj.getInt("slot") == slot) { "Slot mismatch" }
        if (schema.endsWith("v2")) require(obj.optInt("schemaVersion") == 2)
        val settings = obj.getJSONObject("settings")
        require(settings.has("range") && !settings.isNull("range")) { "No saved range" }
        val converted = linkedMapOf<String, Any>()
        val keys = settings.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            require(key in allowed) { "Unknown parameter: $key" }
            if (!settings.isNull(key)) converted["setup_${slot}_$key"] = value(key, settings.get(key))
        }
        // The exported copy may be older than the current private slot. Never overwrite differences.
        val name = obj.optString("name", "")
        val description = obj.optString("description", "")
        if (name.isNotEmpty()) converted["setup_${slot}_name"] = name
        if (description.isNotEmpty()) converted["setup_${slot}_description"] = description
        val prefs = context.getSharedPreferences(STORE, Context.MODE_PRIVATE)
        val current = prefs.all
        converted.forEach { (key, v) ->
            if (current.containsKey(key)) require(same(current[key], v)) { "Conflict: $key" }
        }
        val edit = prefs.edit()
        converted.forEach { (key, v) -> when (v) {
            is Int -> edit.putInt(key, v)
            is Float -> edit.putFloat(key, v)
            is Boolean -> edit.putBoolean(key, v)
            is String -> edit.putString(key, v)
        } }
        check(edit.commit()) { "Preference commit failed" }
        val reread = prefs.all
        check(converted.all { (key, v) -> same(reread[key], v) }) { "Readback failed" }
        // Delete only after all values are durably readable. A deletion failure retries next launch.
        return remove()
    }

    /** Report pending legacy slots; never assume that a public file is safe to overwrite. */
    fun pendingSlots(context: Context): Set<Int> {
        val status = context.applicationContext.getSharedPreferences(STATUS, Context.MODE_PRIVATE)
        return files.keys.filterTo(linkedSetOf()) { !status.getBoolean("legacy_slot_$it", false) }
    }

    fun errorSummary(context: Context): String {
        val status = context.applicationContext.getSharedPreferences(STATUS, Context.MODE_PRIVATE)
        return files.keys.mapNotNull { status.getString("legacy_error_$it", null) }.joinToString(" | ")
    }

    /** A failed migration keeps the legacy file and is reported per slot; other slots may export. */
    @Synchronized fun run(context: Context): Boolean {
        val app = context.applicationContext
        val status = app.getSharedPreferences(STATUS, Context.MODE_PRIVATE)
        var allOk = true
        for ((slot, name) in files) {
            val id = "legacy_slot_$slot"
            if (status.getBoolean(id, false)) continue
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Query only the known filenames under our known public export directory.
                    val path = Environment.DIRECTORY_DOWNLOADS + "/" + DIRECTORY
                    val candidates = mutableListOf<Long>()
                    app.contentResolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.MediaColumns._ID),
                        "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
                        arrayOf(name, path), null)?.use { cursor ->
                        while (cursor.moveToNext()) candidates.add(cursor.getLong(0))
                    } ?: error("MediaStore query failed")
                    for (candidate in candidates) {
                        val uri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, candidate)
                        val bytes = app.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: error("Cannot read legacy setup")
                        require(bytes.size <= 1024 * 1024) { "Oversized setup" }
                        val text = bytes.toString(Charsets.UTF_8)
                        val oldSchema = JSONObject(text).optString("schema")
                        // v2 already is the new export. Do not delete it.
                        if (oldSchema == "lfn-scout-analyzer-setup-export-v1") {
                            check(migrateOne(app, slot, text) {
                                app.contentResolver.delete(uri, null, null) == 1
                            }) { "Legacy JSON deletion failed" }
                        }
                    }
                } else {
                    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), DIRECTORY + name)
                    if (file.exists()) {
                        require(file.length() <= 1024 * 1024) { "Oversized setup" }
                        val text = file.readText(Charsets.UTF_8)
                        if (JSONObject(text).optString("schema") == "lfn-scout-analyzer-setup-export-v1") {
                            check(migrateOne(app, slot, text) { file.delete() }) { "Legacy JSON deletion failed" }
                        }
                    }
                }
                check(status.edit().putBoolean(id, true).remove("legacy_error_$slot").commit()) { "Migration status not saved" }
            } catch (e: Exception) {
                allOk = false
                val explanation = "Slot $slot ($name): ${e.javaClass.simpleName}: ${e.message ?: "unknown cause"}"
                status.edit().putString("legacy_error_$slot", explanation).commit()
                android.util.Log.w("LFN-SetupMigration", explanation, e)
            }
        }
        return allOk
    }
}
