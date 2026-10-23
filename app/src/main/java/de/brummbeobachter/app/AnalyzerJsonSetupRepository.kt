package de.brummbeobachter.app

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.UUID

/** Phase 3A: isolated, inactive repository. Does not alter existing SharedPreferences. */
internal class AnalyzerJsonSetupRepository(context: Context) {
    private val root = File(context.filesDir, "analyzer_setups")
    private val setups = File(root, "setups")
    private val index = File(root, "setup_index.json")
    private val idPattern = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")

    data class Entry(val id: String, val name: String, val description: String)
    data class Listing(val entries: List<Entry>, val recent: List<String>, val activeId: String?)

    private fun requireId(id: String) { require(idPattern.matches(id)) { "Invalid setup identifier" } }
    private fun fileFor(id: String): File { requireId(id); return File(setups, "$id.json") }

    private fun readJson(file: File): JSONObject = JSONObject(AtomicFile(file).openRead().bufferedReader(Charsets.UTF_8).use { it.readText() })
    private fun writeAtomic(file: File, content: JSONObject) {
        require(file.parentFile?.mkdirs() == true || file.parentFile?.isDirectory == true)
        val atomic = AtomicFile(file)
        val stream = atomic.startWrite()
        try {
            stream.write(content.toString(2).toByteArray(StandardCharsets.UTF_8))
            atomic.finishWrite(stream)
        } catch (e: Exception) {
            atomic.failWrite(stream)
            throw e
        }
    }

    private fun check(doc: JSONObject, expectedId: String? = null): JSONObject {
        require(doc.optString("schema") == SCHEMA) { "Unknown setup schema" }
        require(doc.optInt("schemaVersion", -1) == VERSION) { "Unsupported schema version" }
        val id = doc.getString("id")
        requireId(id)
        if (expectedId != null) require(id == expectedId) { "Identifier mismatch" }
        require(doc.get("name") is String && doc.getString("name").trim().isNotEmpty()) { "Setup name missing" }
        require(doc.getString("name").length <= 120) { "Setup name too long" }
        require(doc.has("parameters") && doc.get("parameters") is JSONObject) { "Parameters missing" }
        AnalyzerSetupFormat.validate(doc.getJSONObject("parameters"))
        return doc
    }

    @Synchronized fun listing(): Listing {
        val raw = if (index.exists()) readJson(index) else JSONObject()
        val found = linkedMapOf<String, Entry>()
        setups.listFiles()?.filter { it.isFile && it.extension == "json" }?.sortedBy { it.name }?.forEach { file ->
            runCatching {
                val doc = check(readJson(file), file.nameWithoutExtension)
                val id = doc.getString("id")
                found[id] = Entry(id, doc.getString("name"), doc.optString("description"))
            }
            // Invalid files remain on disk, are not shown, and are never deleted automatically.
        }
        val ids = found.keys
        val recent = mutableListOf<String>()
        val arr = raw.optJSONArray("recent") ?: JSONArray()
        for (i in 0 until arr.length()) {
            val id = arr.optString(i)
            if (id in ids && id !in recent) recent.add(id)
            if (recent.size == 3) break
        }
        val active = raw.optString("activeId").takeIf { it in ids }
        return Listing(found.values.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }), recent, active)
    }

    /** Schema v1 remains readable; mode pins are stored additively and never rewrite setup JSON. */
    @Synchronized fun selectedForMode(direct48k: Boolean): String? {
        val valid = listing().entries.map { it.id }.toSet()
        val raw = if (index.exists()) readJson(index) else JSONObject()
        val key = if (direct48k) "DIRECT_48K" else "ROLLING_8K"
        val pinned = raw.optJSONObject("activeByMode")?.optString(key)?.takeIf { it in valid }
        if (pinned != null) return pinned
        // Legacy migration: do not use a Direct-only activeId for the 8-kHz analyzer.
        val candidates = listing().entries.map { it.id }.filter { id ->
            runCatching {
                val legacy = load(id).getJSONObject("parameters").getJSONObject("legacy")
                AnalyzerModeSetupGuard.compatible(legacy, direct48k)
            }.getOrDefault(false)
        }
        val global = listing().activeId
        if (candidates.size == 1) return candidates.single()
        // A mathematically Direct-compatible 8-kHz setup must not be treated as
        // the user's Direct setup. Require an explicit direct-typical legacy state.
        if (direct48k && global in candidates) {
            val legacy = load(global!!).getJSONObject("parameters").getJSONObject("legacy")
            if (legacy.optInt("decimation_factor", -1) in 0..1) return global
        }
        return null
    }

    @Synchronized fun selectForMode(id: String, direct48k: Boolean) {
        val legacy = load(id).getJSONObject("parameters").getJSONObject("legacy")
        require(AnalyzerModeSetupGuard.compatible(legacy, direct48k)) {
            "Setup ist mit ${if (direct48k) "Direct 48 kHz" else "8 kHz"} nicht kompatibel: " +
                AnalyzerModeSetupGuard.reason(legacy, direct48k)
        }
        val raw = if (index.exists()) readJson(index) else JSONObject()
        val pins = raw.optJSONObject("activeByMode") ?: JSONObject()
        pins.put(if (direct48k) "DIRECT_48K" else "ROLLING_8K", id)
        raw.put("schemaVersion", 2).put("activeByMode", pins)
        raw.put("activeId", id) // compatibility with legacy setup UI and MRU readers
        val recent = (listOf(id) + listing().recent.filterNot { it == id }).take(3)
        raw.put("recent", JSONArray(recent))
        writeAtomic(index, raw)
    }

    @Synchronized fun restoreModePin(direct48k: Boolean, previous: String?) {
        val raw = if (index.exists()) readJson(index) else JSONObject()
        val pins = raw.optJSONObject("activeByMode") ?: JSONObject()
        val key = if (direct48k) "DIRECT_48K" else "ROLLING_8K"
        if (previous == null) pins.remove(key) else {
            load(previous)
            pins.put(key, previous)
        }
        raw.put("activeByMode", pins)
        writeAtomic(index, raw)
    }

    @Synchronized fun load(id: String): JSONObject = check(readJson(fileFor(id)), id)

    @Synchronized fun create(name: String, parameters: JSONObject, description: String = ""): Entry {
        val id = UUID.randomUUID().toString()
        val doc = JSONObject().put("schema", SCHEMA).put("schemaVersion", VERSION)
            .put("id", id).put("name", name.trim()).put("description", description)
            .put("parameters", JSONObject(parameters.toString()))
        save(doc, createOnly = true)
        return Entry(id, name.trim(), description)
    }

    /** Explicit save only. Never called implicitly by a slider or MON runtime handler. */
    @Synchronized fun save(document: JSONObject, createOnly: Boolean = false) {
        val copy = check(JSONObject(document.toString()))
        val file = fileFor(copy.getString("id"))
        if (createOnly) require(!file.exists()) { "Setup already exists" }
        writeAtomic(file, copy)
        check(readJson(file), copy.getString("id"))
    }

    @Synchronized fun select(id: String) {
        load(id)  // Reject missing/corrupt setup without changing MRU.
        val current = listing()
        val recent = (listOf(id) + current.recent.filterNot { it == id }).take(3)
        val raw = (if (index.exists()) readJson(index) else JSONObject())
            .put("activeId", id).put("recent", JSONArray(recent))
        writeAtomic(index, raw)
    }

    /** Restore an exact prior selection, including the legitimate "none selected" state.
     * Used only by a failed transaction; never invents an active setup.
     * Reject invalid identifiers before rewriting the index atomically.
     */
    @Synchronized fun restoreSelection(activeId: String?, recentIds: List<String>) {
        val valid = listing().entries.map { it.id }.toSet()
        require(activeId == null || activeId in valid) { "Rollback active setup no longer exists" }
        require(recentIds.size <= 3 && recentIds.distinct().size == recentIds.size &&
            recentIds.all { it in valid }) { "Rollback MRU contains invalid setup identifiers" }
        val raw = (if (index.exists()) readJson(index) else JSONObject())
            .put("activeId", activeId ?: JSONObject.NULL)
            .put("recent", JSONArray(recentIds))
        writeAtomic(index, raw)
        val restored = listing()
        check(restored.activeId == activeId && restored.recent == recentIds) {
            "Rollback index verification failed"
        }
    }

    @Synchronized fun delete(id: String) {
        val file = fileFor(id)
        require(file.exists()) { "Setup does not exist" }
        val current = listing()
        // Preserve previous setup bytes for a potential manual recovery.
        val backup = File(root, "backups/${id}_${System.currentTimeMillis()}.json")
        require(backup.parentFile?.mkdirs() == true || backup.parentFile?.isDirectory == true)
        file.copyTo(backup, overwrite = false)
        require(file.delete()) { "Could not delete setup" }
        val remaining = current.recent.filterNot { it == id }
        val raw = (if (index.exists()) readJson(index) else JSONObject())
            .put("activeId", current.activeId?.takeIf { it != id } ?: JSONObject.NULL)
            .put("recent", JSONArray(remaining))
        raw.optJSONObject("activeByMode")?.let { pins ->
            listOf("ROLLING_8K", "DIRECT_48K").forEach { key ->
                if (pins.optString(key) == id) pins.remove(key)
            }
        }
        writeAtomic(index, raw)
    }

    /** Import is create-new, never overwrites an existing setup ID. */
    @Synchronized fun importNew(json: String): Entry {
        val input = check(JSONObject(json))
        val name = input.getString("name")
        val description = input.optString("description")
        val entry = create(name, input.getJSONObject("parameters"), description)
        // Imported metadata/provenance are intentionally not trusted in this first stage.
        return entry
    }

    @Synchronized fun exportCopy(id: String): String = load(id).toString(2)

    companion object {
        const val SCHEMA = "lfn-scout-analyzer-setup"
        const val VERSION = 3
    }
}
