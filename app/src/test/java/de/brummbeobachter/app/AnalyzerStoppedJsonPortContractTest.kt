package de.brummbeobachter.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Source-level contracts to prevent a temporary STOP draft being mislabeled as a
 * persisted setup; the runtime transaction tests exercise rollback separately.
 */
class AnalyzerStoppedJsonPortContractTest {
    private fun source(name: String) = File("src/main/java/de/brummbeobachter/app/$name").readText()

    @Test fun stoppedDraftDoesNotWriteLegacyPreferences() {
        val source = source("LiveAnalyzerController.kt")
        val body = source.substringAfter("internal fun applyStoppedJsonDraft(")
            .substringBefore("    fun snapshot(): Snapshot")
        assertFalse(body.contains("getSharedPreferences"))
        assertFalse(body.contains(".commit()"))
        assertFalse(body.contains(".apply()"))
        assertTrue(body.contains("!running && session == null"))
        assertTrue(body.contains("applyStoppedJsonDraft(before, false)"))
    }

    @Test fun adapterVerifiesCompleteEqualityAndDisclosesTemporaryState() {
        val adapter = source("AnalyzerStoppedJsonControllerPort.kt")
        val browser = source("AnalyzerJsonSetupBrowser.kt")
        assertTrue(adapter.contains("AnalyzerSetupExactComparison.sameComplete(saved, live())"))
        assertTrue(adapter.contains("AnalyzerSetupSwitchTransaction.execute(plan"))
        assertTrue(browser.contains("nur im Speicher"))
    }
}
