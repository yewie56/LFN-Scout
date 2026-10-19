package de.brummbeobachter.app

import android.content.Context
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.UUID

/** Publishes JSON and CSV together: a crash can expose only the previous complete pair. */
internal object SystemTestArtifactStore {
    private const val POINTER = "current_generation"
    private val names = setOf("systemtest_results.json", "systemtest_results.csv")

    @Synchronized
    fun save(context: Context, report: PersistentSystemTest.Report) {
        saveFiles(File(context.filesDir, "systemtest"), report.json(), report.csv())
    }

    /** File-only implementation allows crash/restart tests without an Android runtime. */
    @Synchronized
    internal fun saveFiles(dir: File, json: String, csv: String) {
        check(dir.isDirectory || dir.mkdirs()) { "Cannot create systemtest directory" }
        val generation = "generation_${UUID.randomUUID()}"
        val stage = File(dir, generation)
        check(stage.mkdir()) { "Cannot create report generation" }
        try {
            syncWrite(File(stage, "systemtest_results.json"), json)
            syncWrite(File(stage, "systemtest_results.csv"), csv)
            val pointerTmp = File(dir, "${POINTER}.tmp_${UUID.randomUUID()}")
            try {
                syncWrite(pointerTmp, generation + "\n")
                check(pointerTmp.renameTo(File(dir, POINTER))) { "Unable to publish report generation" }
            } finally { pointerTmp.delete() }
        } catch (t: Throwable) {
            // A generation that wasn't published is never visible to readers.
            if (readPointer(dir) != generation) stage.deleteRecursively()
            throw t
        }
        // Keep previous generations: they offer recovery if a published file is damaged.
    }

    @Synchronized
    fun read(context: Context, name: String): String? =
        readFiles(File(context.filesDir, "systemtest"), name)

    @Synchronized
    internal fun readFiles(dir: File, name: String): String? {
        require(name in names)
        val current = readPointer(dir)
        if (current != null) {
            val currentDir = File(dir, current)
            // Never mix files from different generations or silently fall back to stale data.
            check(names.all { File(currentDir, it).isFile }) { "Incomplete published systemtest report: $current" }
            return File(currentDir, name).readText(Charsets.UTF_8)
        }
        // Compatible with systemtest results created by versions before generation storage.
        return File(dir, name).takeIf { it.isFile }?.readText(Charsets.UTF_8)
    }

    private fun readPointer(dir: File): String? {
        val value = File(dir, POINTER).takeIf { it.isFile }?.readText(Charsets.UTF_8)?.trim() ?: return null
        check(Regex("generation_[a-fA-F0-9-]{36}").matches(value)) { "Invalid systemtest generation pointer" }
        return value
    }

    private fun syncWrite(file: File, text: String) {
        file.outputStream().use { stream ->
            stream.write(text.toByteArray(StandardCharsets.UTF_8))
            stream.flush()
            stream.fd.sync()
        }
    }
}
