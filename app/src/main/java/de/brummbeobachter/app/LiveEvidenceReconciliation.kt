package de.brummbeobachter.app

/** Combines distinct live evidence with historic-log outcomes without upgrading missing evidence.
 * The proof IDs remain the same, while details disclose precisely which live signal was used.
 */
internal object LiveEvidenceReconciliation {
    data class Verdict(val id: String, val status: String, val detail: String)

    fun reconcile(log: List<Verdict>, live: List<Verdict>, scopeComplete: Boolean): List<Verdict> {
        val byId = live.associateBy { it.id }
        fun isPass(id: String) = byId[id]?.status == "PASS"
        fun isFail(id: String) = byId[id]?.status == "FAIL"
        return log.map { prior ->
            if (prior.status != "SKIP") return@map prior
            // Contradictory real live evidence must stay FAIL even if the bounded
            // debug log has lost its BEGIN marker. Log completeness is required
            // only to upgrade an unobserved historical event to PASS.
            val contradiction = when (prior.id) {
                "rolling_started" -> isFail("live_rolling_survives_analyzer") || isFail("live_scout_rolling_guard")
                "rolling_lifecycle" -> isFail("live_rolling_survives_analyzer") || isFail("live_no_rolling_lifecycle_change")
                "analyzer_start_observed", "rolling_restart_vs_analyzer" ->
                    isFail("live_analyzer_stop_restart_cycles") || isFail("live_analyzer_run_restored")
                "rolling_counter_monotonic" -> isFail("live_pcm_read_callback_continuity")
                "filterbank_roundtrip" -> isFail("live_filter_geometry_stop_ab_a") || isFail("live_filterbank_after_mon_probe") || isFail("live_analyzer_run_restored")
                "sample_perfect_continuity" -> isFail("live_capture_to_ring_sample_accounting") || isFail("live_pcm_read_callback_continuity")
                "rolling_no_reset_observed" -> isFail("live_rolling_survives_analyzer")
                else -> false
            }
            if (contradiction) return@map prior.copy(status = "FAIL", detail = "Contradictory live evidence; ${prior.detail}")
            if (!scopeComplete) return@map prior
            val proof = when (prior.id) {
                "rolling_started" -> if (isPass("live_scout_rolling_guard") && isPass("live_rolling_survives_analyzer"))
                    "Rolling was already running before this test; live service and ring-buffer ingestion verified"
                    else null
                "rolling_lifecycle" -> if (isPass("live_rolling_survives_analyzer") && isPass("live_no_rolling_lifecycle_change"))
                    "Rolling start/stop/generation counters stable across three actual Analyzer STOP/RUN cycles"
                    else null
                "analyzer_start_observed" -> if (isPass("live_analyzer_stop_restart_cycles") && isPass("live_analyzer_run_restored"))
                    "Three actual Analyzer restartMeasurement calls succeeded and RUN was restored"
                    else null
                "rolling_restart_vs_analyzer" -> if (isPass("live_analyzer_stop_restart_cycles") && isPass("live_analyzer_run_restored"))
                    "Three real Analyzer STOP/RUN cycles and final RUN confirmed by the live controller"
                    else null
                "rolling_counter_monotonic" -> if (isPass("live_pcm_read_callback_continuity") && isPass("live_rolling_survives_analyzer"))
                    "Real AudioRecord epoch and monotonically increasing read counts verified; does not prove sample-perfect acquisition"
                    else null
                "filterbank_roundtrip" -> if (isPass("live_filter_geometry_stop_ab_a") &&
                    isPass("live_filterbank_after_mon_probe") && isPass("live_analyzer_run_restored") &&
                    isPass("live_scout_rolling_guard"))
                    "Production controller applied filter B in Analyzer RUN, restored A and persisted preferences, with Rolling and post-MON controls verified; no named setup switch claimed"
                    else null
                "sample_perfect_continuity" -> if (isPass("live_capture_to_ring_sample_accounting") &&
                    isPass("live_pcm_read_callback_continuity") && isPass("live_rolling_survives_analyzer") &&
                    isPass("live_no_rolling_lifecycle_change"))
                    "Bounded SOFTWARE-path continuity: AudioRecord reads, decimator delivery and independent engine append counter agree across uninterrupted capture epoch; physical microphone/ADC sample-loss detection not claimed"
                    else null
                "rolling_no_reset_observed" -> if (isPass("live_rolling_survives_analyzer") && isPass("live_no_rolling_lifecycle_change"))
                    "Rolling service generation/start/stop counters unchanged during run; not a physical gap proof"
                    else null
                else -> null
            }
            if (proof != null) prior.copy(status = "PASS", detail = "$proof; historical log: ${prior.detail}")
            else prior
        }
    }
}
