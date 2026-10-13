package de.brummbeobachter.app

import android.content.Context
import android.util.AtomicFile
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.UUID

/** Explicit opt-in migration. Never edits or deletes the legacy SharedPreferences. */
internal class AnalyzerPrivateSlotMigrator(context: Context) {
    private val app = context.applicationContext
    private val root = File(app.filesDir, "analyzer_setups")
    private val backup = File(root, "legacy_backups")
    private val repository = AnalyzerJsonSetupRepository(app)
    private val store = "lfn_scout_live_analyzer"

    data class Result(val migrated: List<Int>, val skipped: List<Int>)

    private fun atomicWriteOnce(file: File, content: String) {
        require(file.parentFile?.isDirectory == true || file.parentFile?.mkdirs() == true)
        if (file.exists()) return
        val atomic = AtomicFile(file)
        val stream = atomic.startWrite()
        try {
            stream.write(content.toByteArray(StandardCharsets.UTF_8))
            atomic.finishWrite(stream)
        } catch (e: Exception) { atomic.failWrite(stream); throw e }
    }

    /** Idempotence uses a deterministic UUID; a prior generated setup is never overwritten. */
    @Synchronized fun migrate(): Result {
        val source = app.getSharedPreferences(store, Context.MODE_PRIVATE).all
        val migrated = mutableListOf<Int>()
        val skipped = mutableListOf<Int>()
        for (slot in 1..4) {
            val prefix = "setup_${slot}_"
            val snapshot = source.filterKeys { it.startsWith(prefix) }.toSortedMap()
            if (!snapshot.containsKey(prefix + "range")) { skipped.add(slot); continue }
            val raw = JSONObject()
            for ((key, value) in snapshot) {
                require(value is String || value is Int || value is Long || value is Float || value is Double || value is Boolean) {
                    "Unsupported preference type: $key"
                }
                raw.put(key, value)
            }
            // Durable, verbatim-typed snapshot, prior to creating any new setup.
            val backupFile = File(backup, "slot_${slot}_prefs.json")
            atomicWriteOnce(backupFile, raw.toString(2))
            val id = UUID.nameUUIDFromBytes("lfn-scout-legacy-private-slot-$slot".toByteArray(StandardCharsets.UTF_8)).toString()
            val target = File(root, "setups/$id.json")
            if (target.exists()) {
                repository.load(id) // corrupt prior migration must fail, not overwrite
                skipped.add(slot); continue
            }
            val legacy = JSONObject()
            for ((key, value) in snapshot) {
                val field = key.removePrefix(prefix)
                if (field != "name" && field != "description") legacy.put(field, value)
            }
            // Include only saved fields: no value is synthesized from the live analyzer.
            val parameters = JSONObject().put("legacy", legacy).put("provenance", JSONObject()
                .put("sourceSlot", slot).put("mode", if (slot == 4) "DIRECT" else "8K")
                .put("sourceStore", store))
            AnalyzerSetupFormat.validate(parameters)
            val originalName = snapshot[prefix + "name"] as? String
            val name = originalName?.takeIf { it.isNotBlank() } ?: "Altes Setup $slot"
            val description = snapshot[prefix + "description"] as? String ?: ""
            val doc = JSONObject().put("schema", AnalyzerJsonSetupRepository.SCHEMA)
                .put("schemaVersion", AnalyzerJsonSetupRepository.VERSION).put("id", id)
                .put("name", name).put("description", description).put("parameters", parameters)
            repository.save(doc, createOnly = true)
            check(repository.load(id).getJSONObject("parameters").getJSONObject("legacy").toString() == legacy.toString()) {
                "Migrated setup verification failed"
            }
            migrated.add(slot)
        }
        return Result(migrated, skipped)
    }
}
