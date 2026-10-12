package de.brummbeobachter.app

data class WaterfallLevelRange(val minDb: Double, val maxDb: Double)

object WaterfallLevelPolicy {
    const val REFERENCE_GAIN_DB = 50
    const val DEFAULT_MIN_DB_AT_50 = -100
    const val DEFAULT_MAX_DB_AT_50 = -20
    const val MIN_ALLOWED_DB = -200
    const val MAX_ALLOWED_DB = 20
    const val MIN_SPAN_DB = 10

    fun normalizeMinDb(value: Int): Int = value.coerceIn(MIN_ALLOWED_DB, MAX_ALLOWED_DB - MIN_SPAN_DB)
    fun normalizeMaxDb(value: Int): Int = value.coerceIn(MIN_ALLOWED_DB + MIN_SPAN_DB, MAX_ALLOWED_DB)

    fun normalizeReferences(minDbAt50: Int, maxDbAt50: Int): Pair<Int, Int> {
        var min = normalizeMinDb(minDbAt50)
        var max = normalizeMaxDb(maxDbAt50)
        if (max - min < MIN_SPAN_DB) max = (min + MIN_SPAN_DB).coerceAtMost(MAX_ALLOWED_DB)
        if (max - min < MIN_SPAN_DB) min = (max - MIN_SPAN_DB).coerceAtLeast(MIN_ALLOWED_DB)
        return min to max
    }

    fun effectiveManualRange(gainDb: Int, minDbAt50: Int, maxDbAt50: Int): WaterfallLevelRange {
        // Since v0.5.19, the spectrogram itself is referred to the pre-gain input.
        // Therefore the manual display range must no longer move with recording gain.
        AudioRecordingPolicy.normalizeGainDb(gainDb) // validate/normalize legacy argument only
        val refs = normalizeReferences(minDbAt50, maxDbAt50)
        return WaterfallLevelRange(refs.first.toDouble(), refs.second.toDouble())
    }
}
