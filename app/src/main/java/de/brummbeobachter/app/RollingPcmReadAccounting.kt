package de.brummbeobachter.app

/** Process-local AudioRecord read accounting. Counts observed reads, not physical ADC continuity.
 * Sequence gaps are detectable only between instrumented callbacks; source-side lost samples
 * cannot be inferred without a hardware sequence number or timestamp.
 */
internal class RollingPcmReadAccounting {
    data class Snapshot(
        val epoch: Long, val blocks: Long, val samples: Long, val zeroReads: Long,
        val readErrors: Long, val sequenceErrors: Long, val timestampRegressions: Long, val lastSequence: Long,
        val firstMonotonicNs: Long, val lastMonotonicNs: Long
    )
    private var epoch = 0L
    private var blocks = 0L
    private var samples = 0L
    private var zeroReads = 0L
    private var readErrors = 0L
    private var sequenceErrors = 0L
    private var timestampRegressions = 0L
    private var sequence = 0L
    private var firstNs = 0L
    private var lastNs = 0L

    @Synchronized fun begin(monotonicNs: Long) {
        epoch++; blocks = 0; samples = 0; zeroReads = 0; readErrors = 0
        sequenceErrors = 0; timestampRegressions = 0; sequence = 0; firstNs = monotonicNs; lastNs = monotonicNs
    }
    @Synchronized fun onRead(count: Int, monotonicNs: Long, observedSequence: Long = sequence + 1) {
        if (observedSequence != sequence + 1) sequenceErrors++
        sequence = observedSequence
        when {
            count < 0 -> readErrors++
            count == 0 -> zeroReads++
            else -> { blocks++; samples += count.toLong() }
        }
        if (monotonicNs < lastNs) timestampRegressions++
        lastNs = monotonicNs
    }
    @Synchronized fun snapshot() = Snapshot(epoch, blocks, samples, zeroReads, readErrors,
        sequenceErrors, timestampRegressions, sequence, firstNs, lastNs)
}
