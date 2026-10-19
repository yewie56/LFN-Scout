package de.brummbeobachter.app

/** Deterministic, offline tests of production statistics implementations.
 * No rolling session, persistent settings, cloud connection or recorder is touched.
 */
internal object IsolatedStatisticsSystemTest {
    data class Check(val id: String, val passed: Boolean, val detail: String)

    fun execute(): List<Check> {
        val checks = mutableListOf<Check>()
        fun check(id: String, detail: String, test: () -> Boolean) {
            try {
                checks += Check(id, test(), detail)
            } catch (t: Throwable) {
                checks += Check(id, false, "$detail; ${t.javaClass.simpleName}: ${t.message}")
            }
        }
        val start = 1_800_000L
        check("isolated_statistics_complete_distinct_seconds", "1800 distinct FFT seconds produce exactly one COMPLETE bucket") {
            val done = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
            val stats = RollingHalfHourStatistics(onCompleted = { done.add(it) })
            repeat(1800) { i -> stats.acceptFftEpoch(start + i * 1000L, -40.0) }
            stats.stop()
            done.size == 1 && done[0].validEpochs == 1800 && done[0].qualityStatus == "COMPLETE"
        }
        check("isolated_statistics_duplicates_and_gaps", "Duplicates and a missing second must not fake COMPLETE") {
            val done = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
            val stats = RollingHalfHourStatistics(onCompleted = { done.add(it) })
            repeat(1800) { i ->
                if (i != 47) {
                    stats.acceptFftEpoch(start + i * 1000L, -40.0)
                    stats.acceptFftEpoch(start + i * 1000L, -5.0)
                }
            }
            stats.stop()
            done.size == 1 && done[0].validEpochs == 1799 && done[0].qualityStatus == "PARTIAL" &&
                (done[0].leqDbfs ?: 0.0) < -39.9
        }
        check("isolated_statistics_late_previous_bucket", "A late callback from the previous bucket cannot contaminate the new bucket") {
            val done = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
            val stats = RollingHalfHourStatistics(onCompleted = { done.add(it) })
            stats.acceptFftEpoch(start + 1000L, -30.0)
            stats.acceptFftEpoch(start + 1_800_000L, -35.0)
            stats.acceptFftEpoch(start + 2000L, -10.0)
            stats.stop()
            done.size == 2 && done[0].validEpochs == 1 && done[1].validEpochs == 1
        }
        for (count in listOf(10, 20)) {
            check("isolated_statistics_filterbank_${count}_bands", "Production CSV bank accepts $count bands and preserves independent frequency limits") {
                val done = mutableListOf<List<RollingFilterHalfHourStatistics.BandResult>>()
                val bank = RollingFilterHalfHourStatistics(
                    sampleRate = 1000, minimumHz = 2, maximumHz = 102, bandCount = count,
                    onCompleted = { done.add(it) })
                bank.accept(0, start)
                bank.stop()
                val result = done.single()
                result.size == count && result.first().minimumHz == 2.0 &&
                    result.last().maximumHz == 102.0 &&
                    result.all { it.qualityStatus == "NO_DATA" }
            }
        }
        return checks
    }
}
