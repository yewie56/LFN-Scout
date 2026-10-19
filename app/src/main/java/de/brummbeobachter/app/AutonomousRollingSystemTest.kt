package de.brummbeobachter.app

import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Real, bounded hardware regression: never starts/stops/reconfigures Scout Rolling.
 * Runs only when the Analyzer is already RUN, its mode is ROLLING_8K, and
 * Scout Rolling was active BEFORE the Analyzer session. No named setups touched.
 */
internal object AutonomousRollingSystemTest {
    @Volatile var isTemporaryStopActive = false
        private set
    suspend fun run(context: Context, runId: String): List<PersistentSystemTest.Result> {
        val out = mutableListOf<PersistentSystemTest.Result>()
        fun record(id: String, status: String, detail: String) {
            out += PersistentSystemTest.Result(id, status, detail)
            DebugLogger.log(context, "SYSTEMTEST_CASE", "run_id=$runId id=$id status=$status detail=$detail")
        }
        val wasRunning = LiveAnalyzerController.snapshot().running
        val mode = AnalyzerModeCoordinator.mode
        val rolling = RollingAudioEngine.isRunning
        val original = context.getSharedPreferences("analyzer_audio_mode_session", Context.MODE_PRIVATE)
            .getBoolean("original_rolling", false)
        if (!wasRunning || mode != AnalyzerModeCoordinator.Mode.ROLLING_8K || !rolling || !original || AnalyzerMonitorRecording.isActive()) {
            val reason = "Requires Analyzer RUN, ROLLING_8K and Rolling started in Scout; running=$wasRunning mode=$mode rolling=$rolling scoutOwned=$original monRecording=${AnalyzerMonitorRecording.isActive()}"
            record("live_scout_rolling_guard", "SKIP", reason)
            record("live_analyzer_stop_restart_cycles", "SKIP", reason)
            record("live_mon_state_restored", "SKIP", reason)
            record("live_mon_enabled_stop_restart", "SKIP", reason)
            record("live_filterbank_survives_restart", "SKIP", reason)
            record("live_rolling_survives_analyzer", "SKIP", reason)
            record("live_analyzer_run_restored", "SKIP", reason)
            record("live_pcm_read_callback_continuity", "SKIP", reason)
            return out
        }
        val beforeMon = AnalyzerMonitor.enabled
        val beforeConfig = AnalyzerMonitor.snapshotSetupParameters().toString()
        val beforeAnalyzer = LiveAnalyzerController.snapshot()
        val beforeBuffered = RollingAudioEngine.bufferedSeconds()
        val beforeContinuity = RollingAudioEngine.continuity()
        val beforeReads = RollingAudioService.readAccounting.snapshot()
        record("live_scout_rolling_guard", "PASS", "Rolling already started in Scout; buffer=${beforeBuffered}s")
        var failed = false
        var completed = 0
        var rollingInterrupted = false
        isTemporaryStopActive = true
        try {
            repeat(3) { index ->
                LiveAnalyzerController.stop()
                delay(1200)
                if (!RollingAudioEngine.isRunning || MicCaptureArbiter.owner != "rolling") {
                    rollingInterrupted = true
                    failed = true
                    throw IllegalStateException("Rolling stopped/lost microphone during analyzer STOP cycle=${index + 1}")
                }
                if (!AnalyzerModeCoordinator.restartMeasurement(context)) {
                    failed = true
                    throw IllegalStateException("Analyzer restart returned false: ${AnalyzerModeCoordinator.error}")
                }
                delay(1700)
                if (!LiveAnalyzerController.snapshot().running) {
                    failed = true
                    throw IllegalStateException("Analyzer not RUN after restart cycle=${index + 1}")
                }
                if (!RollingAudioEngine.isRunning || MicCaptureArbiter.owner != "rolling") {
                    rollingInterrupted = true
                    failed = true
                    throw IllegalStateException("Rolling stopped/lost microphone during analyzer RUN cycle=${index + 1}")
                }
                if (AnalyzerMonitor.enabled != beforeMon ||
                    AnalyzerMonitor.snapshotSetupParameters().toString() != beforeConfig) {
                    failed = true
                    throw IllegalStateException("MON state/parameters changed in STOP/RUN cycle=${index + 1}")
                }
                completed++
                DebugLogger.log(context, "SYSTEMTEST_CYCLE", "run_id=$runId index=$completed rolling=true mon=${AnalyzerMonitor.enabled}")
            }
        } catch (c: CancellationException) {
            failed = true
            record("live_analyzer_stop_restart_cycles", "FAIL", "Test interrupted after $completed/3 cycles")
            throw c
        } catch (e: Exception) {
            failed = true
            record("live_analyzer_stop_restart_cycles", "FAIL", "Completed $completed/3; ${e.javaClass.simpleName}: ${e.message}")
        } finally {
            // Even on coroutine cancellation, attempt to restore original Analyzer RUN.
            withContext(NonCancellable) {
                try {
                    if (!LiveAnalyzerController.snapshot().running && AnalyzerModeCoordinator.mode == AnalyzerModeCoordinator.Mode.ROLLING_8K) {
                        runCatching { AnalyzerModeCoordinator.restartMeasurement(context) }
                            .onFailure { DebugLogger.log(context, "SYSTEMTEST_RESTORE_FAILED", "run_id=$runId error=${it.javaClass.simpleName}") }
                    }
                } finally {
                    isTemporaryStopActive = false
                }
            }
        }
        if (!failed) {
            record("live_analyzer_stop_restart_cycles", "PASS", "3/3 actual STOP/RUN cycles, not simulated")
            // Two actual Rolling PCM heartbeats are needed for a meaningful monotonicity
            // check. Allow the existing 15s heartbeat task to emit them; no audio writes.
            DebugLogger.log(context, "SYSTEMTEST_WAIT_PCM", "run_id=$runId duration_ms=33000")
            delay(33000)
        }
        val afterAnalyzer = LiveAnalyzerController.snapshot()
        val filterOk = beforeAnalyzer.filterMinHz == afterAnalyzer.filterMinHz &&
            beforeAnalyzer.filterMaxHz == afterAnalyzer.filterMaxHz &&
            beforeAnalyzer.filterBandCount == afterAnalyzer.filterBandCount &&
            beforeAnalyzer.filterDisplayMinDb == afterAnalyzer.filterDisplayMinDb &&
            beforeAnalyzer.filterDisplayMaxDb == afterAnalyzer.filterDisplayMaxDb &&
            beforeAnalyzer.showFilterWaterfall == afterAnalyzer.showFilterWaterfall
        record("live_filterbank_survives_restart", if (failed) "SKIP" else if (filterOk) "PASS" else "FAIL",
            "Filterbank before=${beforeAnalyzer.filterMinHz}..${beforeAnalyzer.filterMaxHz} bands=${beforeAnalyzer.filterBandCount}; after=${afterAnalyzer.filterMinHz}..${afterAnalyzer.filterMaxHz} bands=${afterAnalyzer.filterBandCount}")
        val monOk = AnalyzerMonitor.enabled == beforeMon && AnalyzerMonitor.snapshotSetupParameters().toString() == beforeConfig
        record("live_mon_state_restored", if (!monOk) "FAIL" else if (failed) "SKIP" else "PASS", "MON enabled before=$beforeMon after=${AnalyzerMonitor.enabled}; parameters ${if (monOk) "unchanged" else "changed"}")
        record("live_mon_enabled_stop_restart", when {
            !beforeMon -> "SKIP"
            !monOk -> "FAIL"
            failed -> "SKIP"
            else -> "PASS"
        }, if (!beforeMon) "MON was OFF at test start; STOP/RUN with MON enabled not exercised" else "MON was ON; STOP/RUN completed=$completed/3; state/parameters restored=$monOk")
        val afterContinuity = RollingAudioEngine.continuity()
        val afterReads = RollingAudioService.readAccounting.snapshot()
        val readEpochUnchanged = beforeReads.epoch != 0L && beforeReads.epoch == afterReads.epoch
        val readsContinued = afterReads.blocks > beforeReads.blocks && afterReads.samples > beforeReads.samples
        val readErrorsUnchanged = afterReads.readErrors == beforeReads.readErrors &&
            afterReads.sequenceErrors == beforeReads.sequenceErrors &&
            afterReads.timestampRegressions == beforeReads.timestampRegressions &&
            afterReads.zeroReads == beforeReads.zeroReads
        // Do not mistake callback progress for Rolling ring-buffer ingestion.
        // The existing appended-delta check below independently verifies delivery
        // into the Rolling engine (after decimation).
        record("live_pcm_read_callback_continuity", when {
            !readEpochUnchanged || !readsContinued || !readErrorsUnchanged -> "FAIL"
            failed -> "SKIP"
            else -> "PASS"
        }, "AudioRecord callback counters, NOT physical losslessness: epoch=${beforeReads.epoch}->${afterReads.epoch} blocks=${beforeReads.blocks}->${afterReads.blocks} samples=${beforeReads.samples}->${afterReads.samples} errors=${beforeReads.readErrors}->${afterReads.readErrors} zeroReads=${beforeReads.zeroReads}->${afterReads.zeroReads} sequenceErrors=${beforeReads.sequenceErrors}->${afterReads.sequenceErrors} timestampRegressions=${beforeReads.timestampRegressions}->${afterReads.timestampRegressions}")
        val rollingOk = !rollingInterrupted && RollingAudioEngine.isRunning && MicCaptureArbiter.owner == "rolling" &&
            beforeContinuity.starts == afterContinuity.starts && beforeContinuity.stops == afterContinuity.stops &&
            beforeContinuity.generation == afterContinuity.generation && afterContinuity.appended > beforeContinuity.appended
        record("live_analyzer_run_restored", if (afterAnalyzer.running) "PASS" else "FAIL",
            "Analyzer originally RUN=$wasRunning; after test RUN=${afterAnalyzer.running}; completed=$completed/3")
        record("live_rolling_survives_analyzer", if (!rollingOk) "FAIL" else if (failed) "SKIP" else "PASS",
            "rolling=$rollingOk start_delta=${afterContinuity.starts - beforeContinuity.starts} stop_delta=${afterContinuity.stops - beforeContinuity.stops} generation_delta=${afterContinuity.generation - beforeContinuity.generation} appended_delta=${afterContinuity.appended - beforeContinuity.appended} buffer_before=${beforeBuffered}s buffer_after=${RollingAudioEngine.bufferedSeconds()}s")
        return out
    }
}
