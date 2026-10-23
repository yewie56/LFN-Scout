package de.brummbeobachter.app

/** Safe migration choice: only a uniquely compatible legacy setup may be pinned. */
internal object AnalyzerModePinMigration {
    fun uniqueCandidate(compatibleIds: List<String>): String? =
        compatibleIds.distinct().singleOrNull()
}
