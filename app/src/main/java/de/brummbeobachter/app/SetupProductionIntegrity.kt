package de.brummbeobachter.app

import java.io.File
import java.security.MessageDigest

/** Read-only inventory of productive named setup files for test safety checks.
 * Includes the active/MRU index and any nested backup files. Never writes them.
 */
internal object SetupProductionIntegrity {
    fun inventory(filesDir: File): Map<String, String> {
        val root = File(filesDir, "analyzer_setups")
        if (!root.exists()) return emptyMap()
        require(root.isDirectory) { "analyzer_setups is not a directory" }
        return root.walkTopDown().filter { it.isFile }.map { file ->
            val relative = file.relativeTo(root).invariantSeparatorsPath
            val hash = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    hash.update(buffer, 0, count)
                }
            }
            relative to hash.digest().joinToString("") { "%02x".format(it) }
        }.toMap()
    }
}
