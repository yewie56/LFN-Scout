package de.brummbeobachter.app

/** Independent ledger of FFT callbacks, separate from RollingHalfHourStatistics.
 * Not a claim of COMPLETE before 1800 distinct seconds actually arrive.
 */
internal class RollingFftCoverageAudit {
    private var bucket = -1L
    private val present = BooleanArray(1800)
    private var lastEpoch = -1L
    data class Evidence(val bucketMs: Long, val observed: Int, val missing: Int,
                        val duplicatesOrLate: Int, val consistent: Boolean)
    private var rejected = 0
    private var previous: Evidence? = null
    @Synchronized fun accept(epochMs: Long, valid: Boolean) {
        if (epochMs < 0 || epochMs % 1000L != 0L || !valid) { rejected++; return }
        val next = Math.floorDiv(epochMs, 1_800_000L) * 1_800_000L
        if (bucket < 0) bucket = next
        if (next < bucket || epochMs <= lastEpoch) { rejected++; return }
        if (next > bucket) {
            val completed = present.count { it }
            previous = Evidence(bucket, completed, 1800 - completed, rejected, true)
            bucket = next; present.fill(false); rejected = 0
        }
        val slot = ((epochMs - bucket) / 1000L).toInt()
        if (slot !in 0 until 1800 || present[slot]) { rejected++; return }
        present[slot] = true
        lastEpoch = epochMs
    }
    @Synchronized fun compare(bucketMs: Long, validEpochs: Int, fftObserved: Int): Evidence {
        val stored = previous?.takeIf { it.bucketMs == bucketMs }
        val known = bucket == bucketMs || stored != null
        val observed = stored?.observed ?: if (bucket == bucketMs) present.count { it } else 0
        return Evidence(bucketMs, observed, 1800 - observed, stored?.duplicatesOrLate ?: rejected,
            known && observed == validEpochs && observed == fftObserved)
    }
}
