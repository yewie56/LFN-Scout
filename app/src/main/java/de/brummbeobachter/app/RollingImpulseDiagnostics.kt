package de.brummbeobachter.app

import kotlin.math.log10
import kotlin.math.pow

/** Diagnostic only: energy concentration across valid PRE-GAIN 1-second epochs.
 * Does not classify acoustic cause; no absolute SPL calibration implied.
 */
internal class RollingImpulseDiagnostics {
    private var bucket = -1L
    private val levels = ArrayList<Double>(1800)

    data class Result(
        val bucketStartMs: Long,
        val count: Int,
        val peakSecondDbfs: Double?,
        val topOneEnergyPercent: Double?,
        val topTenEnergyPercent: Double?,
        val leqMinusL50Db: Double?
    )

    fun accept(epochStartMs: Long, levelDbfs: Double) {
        if (!levelDbfs.isFinite()) return
        val b = Math.floorDiv(epochStartMs, 1_800_000L) * 1_800_000L
        if (bucket != b) { bucket = b; levels.clear() }
        levels.add(levelDbfs)
    }

    fun finish(bucketStartMs: Long): Result {
        if (bucketStartMs != bucket || levels.isEmpty()) {
            return Result(bucketStartMs, 0, null, null, null, null)
        }
        val sorted = levels.sortedDescending()
        val energy = sorted.map { 10.0.pow(it / 10.0) }
        val total = energy.sum()
        val leq = 10.0 * log10((total / energy.size).coerceAtLeast(1e-20))
        val l50 = sorted[((sorted.size - 1) * 0.50).toInt()]
        return Result(bucketStartMs, sorted.size, sorted.first(),
            if (total > 0) 100.0 * energy.first() / total else null,
            if (total > 0) 100.0 * energy.take(10).sum() / total else null,
            leq - l50)
    }
}
