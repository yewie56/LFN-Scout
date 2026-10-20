package de.brummbeobachter.app

import android.content.Context

/** Durable receipts from production paths. A receipt certifies only the specified completed operation. */
internal object ProductionEvidenceLedger {
    private const val PREFS = "lfn_production_evidence_v1"
    data class Verdict(val status: String, val detail: String)

    fun recordWav(context: Context, blocks: Int, frames: Int, bytes: Long, sha: String) {
        if (blocks < 2 || frames <= 0 || bytes != frames.toLong() * 2L || !Regex("[a-f0-9]{64}").matches(sha)) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong("wav_time", System.currentTimeMillis())
            .putInt("wav_blocks", blocks).putInt("wav_frames", frames)
            .putLong("wav_bytes", bytes).putString("wav_sha", sha).apply()
    }

    fun recordStatistics(context: Context, bucket: Long, independent: RollingFftCoverageAudit.Evidence,
                         validEpochs: Int, fftObserved: Int) {
        if (bucket < 0 || bucket % 1800000L != 0L || independent.bucketMs != bucket ||
            !independent.consistent || independent.observed != 1800 || independent.missing != 0 ||
            validEpochs != 1800 || fftObserved != 1800) return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong("stats_time", System.currentTimeMillis())
            .putLong("stats_bucket", bucket).putInt("stats_count", independent.observed).apply()
    }

    fun inspect(context: Context, now: Long = System.currentTimeMillis()): Pair<Verdict, Verdict> {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val wavAge = now - p.getLong("wav_time", 0L)
        val blocks = p.getInt("wav_blocks", 0)
        val frames = p.getInt("wav_frames", 0)
        val bytes = p.getLong("wav_bytes", -1)
        val wavOk = wavAge in 0..86_400_000L && blocks >= 2 && frames > 0 && bytes == frames.toLong() * 2L &&
            Regex("[a-f0-9]{64}").matches(p.getString("wav_sha", "") ?: "")
        val statsAge = now - p.getLong("stats_time", 0L)
        val statsOk = statsAge in 0..86_400_000L && p.getLong("stats_bucket", -1) >= 0 && p.getInt("stats_count", 0) == 1800
        return Verdict(if (wavOk) "PASS" else "SKIP",
            "Production Rolling WAV receipt: verified=$wavOk blocks=$blocks frames=$frames bytes=$bytes age_ms=$wavAge; single completed recording, not continuous multi-hour writer") to
            Verdict(if (statsOk) "PASS" else "SKIP",
                "Completed production half-hour: verified=$statsOk bucket=${p.getLong("stats_bucket", -1)} distinct_seconds=${p.getInt("stats_count", 0)} age_ms=$statsAge; independent FFT ledger and published CSV")
    }
}
