package de.brummbeobachter.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Permanent regression-test registry. Live mode temporarily stops/restarts Analyzer only under strict safety preconditions. */
internal object PersistentSystemTest {
    private val running = java.util.concurrent.atomic.AtomicBoolean(false)
    data class Result(val id: String, val status: String, val detail: String)
    data class Report(val results: List<Result>, val checkedLines: Int, val timestampMs: Long, val mode: String = "LATEST_ROLLING_SESSION_LOG_REPLAY") {
        val passed get() = results.count { it.status == "PASS" }
        val failed get() = results.count { it.status == "FAIL" }
        val skipped get() = results.count { it.status == "SKIP" }
        fun json(): String = JSONObject().apply {
            put("schema", "lfn-scout-systemtest-v1")
            put("appVersion", "0.5.77")
            put("timestampMs", timestampMs)
            put("mode", mode)
            put("checkedLogLines", checkedLines)
            put("pass", passed); put("fail", failed); put("skip", skipped)
            put("limitations", "Run-scoped event analysis and optional safe real Analyzer STOP/RUN cycles. No live named setup switching or filter mutation. Missing evidence is SKIP; heartbeats are not sample-perfect proof.")
            put("results", JSONArray().apply { results.forEach { r -> put(JSONObject().apply {
                put("id", r.id); put("status", r.status); put("detail", r.detail)
            }) } })
        }.toString(2) + "\n"
        fun csv(): String = buildString {
            append("test_id,status,detail\n")
            results.forEach { r ->
                append(listOf(r.id, r.status, r.detail).joinToString(",") { v -> "\"${v.replace("\"", "\"\"").replace("\n", " ")}\"" }); append('\n')
            }
        }
        fun summary(): String = "PASS $passed · FAIL $failed · SKIP $skipped"
    }

    /** Pure evaluator, also testable by ordinary JVM tests. */
    fun evaluate(lines: List<String>, nowMs: Long = System.currentTimeMillis()): Report {
        val results = mutableListOf<Result>()
        fun result(id: String, status: String, reason: String) { results += Result(id, status, reason) }
        // Each real AudioRecord start opens a new, independent counter epoch. Never compare
        // sample counters across separate sessions; their natural reset to zero is valid.
        // Analyze the latest complete/ongoing session only, not unrelated historical log.
        val sessionStarts = lines.indices.filter { "AUDIO_DIAG_ROLLING_RECORDING_STARTED" in lines[it] }
        val selectedStart = sessionStarts.lastOrNull()
        val scopedLines = if (selectedStart != null) lines.drop(selectedStart) else lines
        val scopedNote = if (selectedStart != null)
            "latest rolling session (${sessionStarts.size} sessions in source log; beginning at line ${selectedStart + 1})"
        else "no explicit rolling session start; available log window only"
        val rollingStarts = scopedLines.count { "AUDIO_DIAG_ROLLING_RECORDING_STARTED" in it }
        val rollingStops = scopedLines.count { "AUDIO_DIAG_ROLLING_CLEANUP_DONE" in it }
        val stopRequests = scopedLines.count { "AUDIO_DIAG_ROLLING_STOP_REQUEST" in it }
        val starts = scopedLines.count { "AUDIO_DIAG_BEGIN_SUCCESS" in it }
        val restartRequests = scopedLines.count { "AUDIO_DIAG_RESTART_REQUEST" in it }
        val restartSuccess = scopedLines.count { "AUDIO_DIAG_RESTART_DONE" in it }
        val heartbeats = scopedLines.filter { "AUDIO_DIAG_ROLLING_PCM_HEARTBEAT" in it }
        val pcmCounts = heartbeats.mapNotNull { Regex("\\bsamples=(\\d+)").find(it)?.groupValues?.get(1)?.toLongOrNull() }
        val reset = scopedLines.count { "ROLLING_RESET" in it || "AUDIO_DIAG_ROLLING_RESET" in it }
        val failures = scopedLines.filter { "FATAL EXCEPTION" in it || "ANALYZER_RESTART_FAILED" in it }
        result("rolling_started", if (rollingStarts > 0) "PASS" else "SKIP", "Recording starts=$rollingStarts; $scopedNote")
        result("rolling_restart_vs_analyzer", when {
            restartRequests == 0 -> "SKIP"
            restartRequests != restartSuccess -> "FAIL"
            else -> "PASS"
        }, "Analyzer restart requests=$restartRequests, completed=$restartSuccess; $scopedNote")
        result("rolling_counter_monotonic", when {
            pcmCounts.size < 2 -> "SKIP"
            pcmCounts.zipWithNext().any { (a,b) -> b <= a } -> "FAIL"
            else -> "PASS"
        }, "Heartbeats=${pcmCounts.size}; samples first=${pcmCounts.firstOrNull() ?: "?"} last=${pcmCounts.lastOrNull() ?: "?"}; $scopedNote; monotonicity only")
        result("rolling_no_reset_observed", if (heartbeats.size < 2) "SKIP" else if (reset > 0) "FAIL" else "PASS", "Explicit reset events=$reset; $scopedNote; absence of events does not prove absence of resets")
        result("rolling_lifecycle", if (rollingStarts == 0) "SKIP" else if (rollingStops > rollingStarts || stopRequests > rollingStarts) "FAIL" else "PASS", "Recording starts=$rollingStarts, stop requests=$stopRequests, cleanups=$rollingStops; explicit user stop allowed; $scopedNote")
        result("analyzer_start_observed", if (starts > 0) "PASS" else "SKIP", "Analyzer successful initializations=$starts; $scopedNote")
        result("unhandled_crash_observed", if (failures.isNotEmpty()) "FAIL" else "SKIP", "Fatal/restart failures in selected log window=${failures.size}; Android-only crashes require logcat")
        result("sample_perfect_continuity", "SKIP", "PCM block sequence/gap verification is not yet instrumented")
        result("recording_write_continuity", "SKIP", "Per-block long-term recording write acknowledgements not yet instrumented")
        result("setup_mon_roundtrip", "SKIP", "Requires safe isolated setup fixture and state-controller harness")
        result("filterbank_roundtrip", "SKIP", "Requires safe isolated filterbank fixture and controller harness")
        result("statistics_coverage", "SKIP", "Requires independent valid-sample and gap accounting")
        return Report(results, scopedLines.size, nowMs)
    }

    /** Runtime checks execute against a real, already-running Scout Rolling session.
     * Never creates, stops, or clears that session. No named JSON setup is changed.
     */
    suspend fun runAutomated(context: Context): Report {
        check(running.compareAndSet(false, true)) { "Ein Systemtest läuft bereits" }
        try {
        val runId = System.currentTimeMillis().toString(36)
        DebugLogger.log(context, "SYSTEMTEST_BEGIN", "id=$runId version=0.5.77")
        val live = AutonomousRollingSystemTest.run(context, runId)
        DebugLogger.log(context, "SYSTEMTEST_END", "id=$runId live_pass=${live.count { it.status == "PASS" }} live_fail=${live.count { it.status == "FAIL" }}")
        val recent = DebugLogger.readLines(context)
        val first = recent.indexOfLast { "SYSTEMTEST_BEGIN id=$runId" in it }
        val scoped = if (first >= 0) recent.drop(first) else emptyList()
        val historical = evaluate(scoped)
        val unauthorizedRollingStop = scoped.any { "AUDIO_DIAG_ROLLING_STOP_REQUEST" in it || "AUDIO_DIAG_ROLLING_CLEANUP_ENTER" in it || "AUDIO_DIAG_ROLLING_RECORDING_STARTED" in it }
        val rollingIntegrity = if (live.any { it.id == "live_scout_rolling_guard" && it.status == "PASS" }) {
            listOf(Result("live_no_rolling_lifecycle_change", if (unauthorizedRollingStop) "FAIL" else "PASS", "No Rolling stop, cleanup or restart events permitted during test; observed=$unauthorizedRollingStop"))
        } else listOf(Result("live_no_rolling_lifecycle_change", "SKIP", "Live Scout-owned Rolling precondition not met"))
        val report = Report(live + rollingIntegrity + historical.results, scoped.size, System.currentTimeMillis(), "SAFE_LIVE_CYCLES_PLUS_RUN_SCOPED_LOG")
        SystemTestArtifactStore.save(context, report)
        DebugLogger.log(context, "SYSTEMTEST_RESULT", "run_id=$runId pass=${report.passed} fail=${report.failed} skip=${report.skipped} mode=live_safe_plus_scoped_log")
        return report
        } finally { running.set(false) }
    }

    fun run(context: Context): Report {
        val report = evaluate(DebugLogger.readLines(context))
        DebugLogger.log(context, "SYSTEMTEST_RESULT", "pass=${report.passed} fail=${report.failed} skip=${report.skipped} mode=log_replay")
        return report
    }
}
