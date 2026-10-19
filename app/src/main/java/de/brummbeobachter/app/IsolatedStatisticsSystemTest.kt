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
        check("isolated_statistics_independent_coverage", "A distinct FFT-second bitmap independently counts present and missing slots") {
            val done = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
            val stats = RollingHalfHourStatistics(onCompleted = { done.add(it) })
            stats.acceptFftEpoch(start, -40.0)
            stats.acceptFftEpoch(start, -30.0) // duplicate must not increase bitmap
            stats.acceptFftEpoch(start + 2000L, -40.0) // slot 1 missing
            stats.acceptFftEpoch(start + 1_800_000L, -35.0)
            stats.stop()
            done.size == 2 && done[0].fftSecondsObserved == 2 && done[0].fftSecondsMissing == 1798 &&
                done[1].fftSecondsObserved == 1 && done[1].fftSecondsMissing == 1799
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
        check("isolated_statistics_nonfinite_ignored", "NaN and infinities do not count as valid FFT seconds") {
            val done = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
            val stats = RollingHalfHourStatistics(onCompleted = { done.add(it) })
            stats.acceptFftEpoch(start, Double.NaN)
            stats.acceptFftEpoch(start + 1000L, Double.POSITIVE_INFINITY)
            stats.acceptFftEpoch(start + 2000L, Double.NEGATIVE_INFINITY)
            stats.acceptFftEpoch(start + 3000L, -45.0)
            stats.stop()
            done.size == 1 && done.single().validEpochs == 1 && done.single().qualityStatus == "PARTIAL"
        }
        check("isolated_statistics_empty_bucket", "Invalid-only bucket reports NO_DATA and no Leq") {
            val done = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
            val stats = RollingHalfHourStatistics(onCompleted = { done.add(it) })
            stats.acceptFftEpoch(start, Double.NaN)
            stats.stop()
            done.size == 1 && done.single().validEpochs == 0 &&
                done.single().qualityStatus == "NO_DATA" && done.single().leqDbfs == null
        }
        check("isolated_statistics_reuse_after_stop", "A new half-hour session after stop starts with fresh epoch accounting") {
            val done = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
            val stats = RollingHalfHourStatistics(onCompleted = { done.add(it) })
            stats.acceptFftEpoch(start, -30.0)
            stats.stop()
            stats.acceptFftEpoch(start, -35.0)
            stats.stop()
            done.size == 2 && done.all { it.validEpochs == 1 && it.qualityStatus == "PARTIAL" }
        }
        check("isolated_statistics_bucket_boundary", "FFT epochs on an exact :00/:30 boundary go to the next bucket") {
            val done = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
            val stats = RollingHalfHourStatistics(onCompleted = { done.add(it) })
            stats.acceptFftEpoch(start + 1_799_000L, -21.0)
            stats.acceptFftEpoch(start + 1_800_000L, -44.0)
            stats.stop()
            done.size == 2 && done[0].validEpochs == 1 && done[1].validEpochs == 1 &&
                done[0].startEpochMs + 1_800_000L == done[1].startEpochMs
        }
        check("isolated_statistics_stale_pcm_callback", "Late PCM timestamp cannot move a completed bucket backwards") {
            val done = mutableListOf<RollingHalfHourStatistics.HalfHourResult>()
            val stats = RollingHalfHourStatistics(onCompleted = { done.add(it) })
            stats.accept(1000, start + 1_799_000L)
            stats.accept(1000, start + 1_800_000L)
            stats.accept(1000, start + 1_799_001L)
            stats.stop()
            done.size == 2 && done[0].startEpochMs == start &&
                done[1].startEpochMs == start + 1_800_000L
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
