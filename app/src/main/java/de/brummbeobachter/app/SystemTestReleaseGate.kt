package de.brummbeobachter.app

/** Conservative acceptance gate for the three prioritized recording/data proofs.
 * Never promotes a diagnostic or fixture PASS to acceptance of a different data path.
 */
internal object SystemTestReleaseGate {
    val required = listOf(
        "sample_perfect_continuity",
        "recording_write_continuity",
        "statistics_coverage"
    )
    data class Assessment(val ready: Boolean, val missing: List<String>, val failed: List<String>,
                          val duplicated: List<String>, val invalid: List<String>) {
        val status: String get() = if (ready) "PASS" else "BLOCKED"
    }
    fun assess(results: List<PersistentSystemTest.Result>): Assessment {
        val grouped = results.groupBy { it.id }
        val duplicate = grouped.filterValues { it.size != 1 }.keys.sorted()
        val missing = required.filter { grouped[it]?.singleOrNull()?.status != "PASS" }
        val failed = results.filter { it.status == "FAIL" }.map { it.id }.distinct().sorted()
        val invalid = results.filter { it.status !in setOf("PASS", "FAIL", "SKIP") }.map { it.id }.distinct().sorted()
        return Assessment(missing.isEmpty() && failed.isEmpty() && duplicate.isEmpty() && invalid.isEmpty(),
            missing, failed, duplicate, invalid)
    }
}
