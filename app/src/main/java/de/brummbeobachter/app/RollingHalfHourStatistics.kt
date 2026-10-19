package de.brummbeobachter.app

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.sqrt

/** RAM-only rolling-capture statistics core. A bounded, independent 1-second epoch stream.
 *  The first-order DC blocker/low-pass is a preliminary implementation; it is NOT
 *  a calibrated high-order measurement filter. Completed results are not uploaded.
 */
internal class RollingHalfHourStatistics(
    private val sampleRate: Int = 1000,
    private val minimumHz: Int = 2,
    private val maximumHz: Int = 20,
    private val onCompleted: (HalfHourResult) -> Unit = {},
    private val onEpoch: (Long, Double) -> Unit = { _, _ -> }
) {
    init { require(sampleRate >= 2 * maximumHz && minimumHz > 0 && maximumHz > minimumHz) }
    private val highPassA = exp(-2.0 * PI * minimumHz / sampleRate)
    private val lowPassA = exp(-2.0 * PI * maximumHz / sampleRate)
    private var previousInput = 0.0
    private var highPassState = 0.0
    private var lowPassState = 0.0
    private var bucketStart = -1L
    private var epochStart = -1L
    private var energy = 0.0
    private var samples = 0
    private val epochLevels = ArrayList<Double>(1800)
    private var lastFftEpochMs = -1L

    data class HalfHourResult(
        val startEpochMs: Long, val endEpochMs: Long,
        val minimumHz: Int, val maximumHz: Int,
        val validEpochs: Int, val leqDbfs: Double?,
        val l50Dbfs: Double?, val l90Dbfs: Double?,
        val qualityStatus: String
    )

    /** Input sample is 16-bit PCM, already gain-applied by the legacy rolling path. */
    fun accept(sample: Short, timeEpochMs: Long) {
        val newBucket = Math.floorDiv(timeEpochMs, 1_800_000L) * 1_800_000L
        if (bucketStart < 0) bucketStart = newBucket
        // Reject late PCM callbacks rather than mixing prior timestamps into a
        // newer half-hour or moving the bucket clock backwards.
        if (newBucket < bucketStart) return
        val newEpoch = Math.floorDiv(timeEpochMs, 1000L) * 1000L
        if (epochStart >= 0 && newEpoch < epochStart) return
        if (newBucket != bucketStart) {
            finishEpoch()
            completeBucket()
            bucketStart = newBucket
        }
        if (epochStart >= 0 && newEpoch != epochStart) finishEpoch()
        epochStart = newEpoch
        val x = sample.toDouble() / 32768.0
        highPassState = highPassA * (highPassState + x - previousInput)
        previousInput = x
        lowPassState = (1.0 - lowPassA) * highPassState + lowPassA * lowPassState
        energy += lowPassState * lowPassState
        samples++
    }

    /** FFT-backed rolling epoch; independent old PCM filter remains in source but is no longer called by service. */
    fun acceptFftEpoch(timeEpochMs: Long, db: Double) {
        val bucket = Math.floorDiv(timeEpochMs, 1_800_000L) * 1_800_000L
        if (bucketStart < 0) bucketStart = bucket
        // A half-hour is COMPLETE only with 1800 distinct seconds. Duplicate,
        // stale or out-of-order FFT callbacks must never inflate validEpochs.
        if (bucket < bucketStart) return
        if (bucket != bucketStart) { completeBucket(); bucketStart = bucket; lastFftEpochMs = -1L }
        val second = Math.floorDiv(timeEpochMs, 1000L) * 1000L
        if (second <= lastFftEpochMs || !db.isFinite()) return
        lastFftEpochMs = second
        epochLevels.add(db)
        onEpoch(second, db)
    }

    private fun finishEpoch() {
        if (samples >= sampleRate * 9 / 10) {
            val rawDb = 10.0 * log10((energy / samples).coerceAtLeast(1e-20))
            val db = NoiseFloorReference.compensateEpoch(
                NoiseFloorReference.overallKey(sampleRate, minimumHz, maximumHz), rawDb)
            epochLevels.add(db)
            onEpoch(epochStart, db)
        }
        energy = 0.0
        samples = 0
    }

    private fun completeBucket() {
        if (bucketStart < 0) return
        val sorted = epochLevels.sortedDescending()
        val n = sorted.size
        val averageEnergy = if (n > 0) epochLevels.sumOf { Math.pow(10.0, it / 10.0) } / n else 0.0
        fun exceedance(p: Double): Double? = if (n == 0) null else sorted[((n - 1) * p).toInt()]
        onCompleted(HalfHourResult(bucketStart, bucketStart + 1_800_000L,
            minimumHz, maximumHz, n,
            if (n > 0) 10.0 * log10(averageEnergy.coerceAtLeast(1e-20)) else null,
            exceedance(0.50), exceedance(0.90),
            when (n) { 0 -> "NO_DATA"; 1800 -> "COMPLETE"; else -> "PARTIAL" }))
        epochLevels.clear()
    }

    /** Called at controlled stop; incomplete seconds do not count as valid epochs. */
    fun stop() { finishEpoch(); completeBucket(); bucketStart = -1L; epochStart = -1L; lastFftEpochMs = -1L }
}

internal object RollingStatisticsSettings {
    data class Values(val enabled: Boolean = true, val minimumHz: Int = 2,
                      val maximumHz: Int = 20, val filterEnabled: Boolean = false,
                      val exportOverallCsv: Boolean = true,
                      val filterMinimumHz: Int = 0, val filterMaximumHz: Int = 100,
                      val filterCount: Int = 10, val exportFilterCsv: Boolean = true,
                      val exportEpochCsv: Boolean = false,
                      val csvFilterMethod: RollingFilterHalfHourStatistics.Method = RollingFilterHalfHourStatistics.Method.LEGACY_IIR) {
        fun validate() { require(minimumHz in 1..199 && maximumHz in 2..200 && minimumHz < maximumHz);
            require(filterMinimumHz in 0..199 && filterMaximumHz in 1..200 && filterMaximumHz > filterMinimumHz);
            require(filterCount == 10 || filterCount == 20) }
    }
    private const val NAME = "lfn_rolling_halfhour_v1"
    fun load(context: android.content.Context): Values {
        val p = context.getSharedPreferences(NAME, android.content.Context.MODE_PRIVATE)
        return Values(p.getBoolean("enabled", true), p.getInt("min_hz", 2),
            p.getInt("max_hz", 20), p.getBoolean("filter_enabled", false),
            p.getBoolean("export_overall_csv", true),
            p.getInt("filter_min_hz", 0), p.getInt("filter_max_hz", 100),
            p.getInt("filter_count", 10), p.getBoolean("export_filter_csv", true),
            p.getBoolean("export_epoch_csv", false),
            runCatching { RollingFilterHalfHourStatistics.Method.valueOf(p.getString("csv_filter_method", "LEGACY_IIR") ?: "LEGACY_IIR") }.getOrDefault(RollingFilterHalfHourStatistics.Method.LEGACY_IIR))
            .let { if (runCatching { it.validate() }.isSuccess) it else Values() }
    }
    fun store(context: android.content.Context, v: Values) {
        v.validate()
        check(context.getSharedPreferences(NAME, android.content.Context.MODE_PRIVATE).edit()
            .putBoolean("enabled", v.enabled).putInt("min_hz", v.minimumHz)
            .putInt("max_hz", v.maximumHz).putBoolean("filter_enabled", v.filterEnabled)
            .putBoolean("export_overall_csv", v.exportOverallCsv)
            .putInt("filter_min_hz", v.filterMinimumHz).putInt("filter_max_hz", v.filterMaximumHz)
            .putInt("filter_count", v.filterCount).putBoolean("export_filter_csv", v.exportFilterCsv)
            .putBoolean("export_epoch_csv", v.exportEpochCsv)
            .putString("csv_filter_method", v.csvFilterMethod.name)
            .commit()) { "Rolling-Statistik-Setup konnte nicht gespeichert werden" }
    }
}
