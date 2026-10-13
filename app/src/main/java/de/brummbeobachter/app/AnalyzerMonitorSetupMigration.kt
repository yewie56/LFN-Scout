package de.brummbeobachter.app

import android.content.Context
import android.util.AtomicFile
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Phase 3D: explicit, inactive import of MON SharedPreferences into a JSON setup.
 * Never invokes AnalyzerMonitor.load/start/save or switches runtime settings.
 */
internal class AnalyzerMonitorSetupMigration(private val context: Context) {
    private val storeName = "lfn_analyzer_monitor_049"
    private val numeric = setOf(
        "center", "level", "width", "shift", "comp_thr", "comp_ratio",
        "comp_attack", "comp_release", "comp_knee", "comp_makeup",
        "limit_ceiling", "limit_attack", "limit_release", "fade"
    )
    private val boolean = setOf("comp_on", "limit_on")
    private val allowed = numeric + boolean
    private val backups = File(context.filesDir, "analyzer_setups/backups/monitor")

    data class Snapshot(val values: JSONObject, val rawBackup: JSONObject, val unknownKeys: List<String>)

    /** Missing values remain missing. No default values or coerceIn are injected. */
    fun snapshot(): Snapshot {
        val prefs = context.getSharedPreferences(storeName, Context.MODE_PRIVATE).all
        val known = JSONObject()
        val raw = JSONObject()
        val unknown = mutableListOf<String>()
        for ((key, value) in prefs.toSortedMap()) {
            // Preserve the full original store, even keys not understood by this importer.
            when (value) {
                is Boolean, is String, is Int, is Long -> raw.put(key, value)
                is Float -> {
                    require(value.isFinite()) { "Non-finite value in MON store: $key" }
                    raw.put(key, value.toDouble())
                }
                is Set<*> -> {
                    require(value.all { it is String }) { "Invalid string set in MON store: $key" }
                    raw.put(key, org.json.JSONArray(value.sortedBy { it.toString() }))
                }
                null -> raw.put(key, JSONObject.NULL)
                else -> error("Unsupported MON store value type: $key")
            }
            if (key !in allowed) {
                unknown += key
                continue
            }
            require(if (key in boolean) value is Boolean else value is Float && value.isFinite()) {
                "Unexpected MON preference type: $key"
            }
            known.put(key, if (value is Float) value.toDouble() else value)
        }
        if (known.length() > 0) AnalyzerSetupFormat.validate(JSONObject().put("monitor", known))
        return Snapshot(known, raw, unknown)
    }

    /** Return a proposed copy for review; no persistence, runtime change or activation. */
    fun preview(setup: JSONObject): JSONObject {
        val draft = JSONObject(setup.toString())
        val parameters = draft.getJSONObject("parameters")
        require(!parameters.has("monitor")) { "Setup already has MON parameters; refusing overwrite" }
        val snap = snapshot()
        require(snap.values.length() > 0) { "MON store has no explicit values" }
        parameters.put("monitor", snap.values)
        AnalyzerSetupFormat.validate(parameters)
        return draft
    }

    /**
     * Explicit user-triggered operation ONLY. A verified, immutable backup of the
     * entire original MON preferences store is written before touching the setup.
     * The legacy preference store itself is never modified.
     */
    @Synchronized fun backupAndSave(setupId: String, repository: AnalyzerJsonSetupRepository): File {
        val original = repository.load(setupId)
        val snap = snapshot()
        require(snap.values.length() > 0) { "MON store has no explicit values" }
        val proposed = JSONObject(original.toString())
        val parameters = proposed.getJSONObject("parameters")
        require(!parameters.has("monitor")) { "MON parameters already present; refusing overwrite" }
        parameters.put("monitor", snap.values)
        AnalyzerSetupFormat.validate(parameters)
        require(backups.mkdirs() || backups.isDirectory)
        val backupFile = File(backups, "mon_${UUID.randomUUID()}.json")
        val envelope = JSONObject().put("schema", "lfn-scout-monitor-legacy-backup")
            .put("schemaVersion", 1).put("sourceStore", storeName)
            .put("targetSetupId", setupId).put("capturedAtEpochMs", System.currentTimeMillis())
            .put("rawPreferences", snap.rawBackup)
        writeOnce(backupFile, envelope)
        require(JSONObject(backupFile.readText()).getJSONObject("rawPreferences").toString() == snap.rawBackup.toString()) {
            "MON backup verification failed"
        }
        repository.save(proposed)
        return backupFile
    }

    private fun writeOnce(file: File, document: JSONObject) {
        require(!file.exists()) { "Backup file already exists" }
        val atomic = AtomicFile(file)
        val out = atomic.startWrite()
        try {
            out.write(document.toString(2).toByteArray(StandardCharsets.UTF_8))
            atomic.finishWrite(out)
        } catch (ex: Exception) {
            atomic.failWrite(out)
            throw ex
        }
    }
}
