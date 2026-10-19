package de.brummbeobachter.app

import android.content.Context
import java.io.File

/** Last completed test report survives app restarts and is exported unchanged. */
internal object SystemTestArtifactStore {
    fun save(context: Context, report: PersistentSystemTest.Report) {
        val dir = File(context.filesDir, "systemtest").apply { mkdirs() }
        File(dir, "systemtest_results.json").writeText(report.json(), Charsets.UTF_8)
        File(dir, "systemtest_results.csv").writeText(report.csv(), Charsets.UTF_8)
    }
    fun read(context: Context, name: String): String? {
        require(name == "systemtest_results.json" || name == "systemtest_results.csv")
        return File(File(context.filesDir, "systemtest"), name).takeIf { it.isFile }?.readText(Charsets.UTF_8)
    }
}
