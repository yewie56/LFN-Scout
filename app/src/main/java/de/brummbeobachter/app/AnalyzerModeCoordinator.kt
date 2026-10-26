package de.brummbeobachter.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/** The analyzer session owns only its TEMPORARY audio mode. STOP does not restore rolling.
 * The ORIGINAL rolling preference is restored on exit from an already stopped panel.
 * Note: Rolling is an in-memory buffer; it is NOT a continuous disk WAV writer.
 */
internal object AnalyzerModeCoordinator {
    enum class Mode { ROLLING_8K, DIRECT_48K, TEST_8K, TEST_48K }
    @Volatile var mode: Mode? = null
        private set
    @Volatile var error: String = ""
        private set
    private var originalRolling = false
    private const val PREFS = "analyzer_audio_mode_session"

    suspend fun begin(context: Context, chosen: Mode): Boolean {
        if (mode != null) {
            AnalyzerAudioTrace.write(context, "BEGIN_ALREADY_ACTIVE", "existing=$mode requested=$chosen owner=${MicCaptureArbiter.owner}")
            return true // Re-entering a running analyzer is NOT a new session.
        }
        AnalyzerAudioTrace.newSession()
        AnalyzerAudioTrace.write(context, "BEGIN_REQUEST", "chosen=$chosen owner=${MicCaptureArbiter.owner} rolling=${RollingAudioEngine.isRunning} direct=${AnalyzerDirectAudioService.isCapturing} subscribers=${AnalyzerSessionGateway.rawPcm.activeSubscribers}")
        error = ""
        if (chosen !in listOf(Mode.TEST_8K,Mode.TEST_48K) &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            error = "Mikrofonberechtigung fehlt"
            AnalyzerAudioTrace.write(context, "BEGIN_DENIED", "reason=permission")
            return false
        }
        val store = ReminderSettingsStore(context.applicationContext)
        originalRolling = RollingAudioEngine.isRunning
        AnalyzerAudioTrace.write(context, "ORIGINAL_SAVED", "originalRolling=$originalRolling chosen=$chosen")
        // Save state before either temporary change, including for crash recovery.
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("restore_pending", true).putBoolean("original_rolling", originalRolling)
            .putString("chosen_mode", chosen.name).commit()
        return try {
            LiveAnalyzerController.stop()
            AnalyzerAudioTrace.write(context, "PREPARE_MODE", "chosen=$chosen owner=${MicCaptureArbiter.owner}")
            if (chosen != Mode.ROLLING_8K) {
                // Discard old rolling contents. We do NOT preserve/continue any uncommitted audio.
                store.setRollingBufferEnabled(false)
                RollingAudioService.requestStop(context)
                AnalyzerAudioTrace.write(context, "ROLLING_STOP_REQUESTED", "owner=${MicCaptureArbiter.owner} rolling=${RollingAudioEngine.isRunning}")
                var released = false
                for (i in 0 until 120) {
                    if (MicCaptureArbiter.owner == null && !RollingAudioEngine.isRunning) { released = true; break }
                    delay(50)
                }
                AnalyzerAudioTrace.write(context, "ROLLING_RELEASE_RESULT", "released=$released owner=${MicCaptureArbiter.owner} rolling=${RollingAudioEngine.isRunning}")
                if (!released) this.error = "8-kHz-Mikrofon wurde nicht freigegeben"
                if (released && chosen in listOf(Mode.TEST_8K,Mode.TEST_48K)) {
                    if (!AnalyzerTestSignal.start(context, if (chosen == Mode.TEST_48K)
                        context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
                            .getInt("mode_48_direct_sample_rate",48000) else 8000, chosen == Mode.TEST_48K))
                        this.error = "Ungültiges digitales Testsignal für die gewählte Abtastrate"
                } else if (released && AnalyzerDirectAudioService.requestStart(context)) {
                    for (i in 0 until 120) {
                        if (AnalyzerDirectAudioService.isCapturing || AnalyzerDirectAudioService.lastError.isNotBlank()) break
                        delay(50)
                    }
                    AnalyzerAudioTrace.write(context, "DIRECT_START_RESULT", "capturing=${AnalyzerDirectAudioService.isCapturing} owner=${MicCaptureArbiter.owner} error=${AnalyzerDirectAudioService.lastError}")
                    if (!AnalyzerDirectAudioService.isCapturing) this.error = AnalyzerDirectAudioService.lastError.ifBlank { "Direkt-Start Timeout" }
                } else if (released) this.error = AnalyzerDirectAudioService.lastError
            } else {
                store.setRollingBufferEnabled(true)
                AnalyzerAudioTrace.write(context, "ROLLING_8K_SELECT", "rolling=${RollingAudioEngine.isRunning} owner=${MicCaptureArbiter.owner} subscribers=${AnalyzerSessionGateway.rawPcm.activeSubscribers}")
                if (!RollingAudioEngine.isRunning && !RollingAudioService.requestStart(context))
                    this.error = "Rolling-Dienst konnte nicht gestartet werden"
                if (error.isBlank()) {
                    for (i in 0 until 120) {
                        if (RollingAudioEngine.isRunning) break
                        delay(50)
                    }
                    AnalyzerAudioTrace.write(context, "ROLLING_START_RESULT", "rolling=${RollingAudioEngine.isRunning} owner=${MicCaptureArbiter.owner}")
                    if (!RollingAudioEngine.isRunning) this.error = "Rolling-Start Timeout"
                }
            }
            if (error.isNotBlank()) {
                AnalyzerAudioTrace.write(context, "BEGIN_FAILED", "chosen=$chosen error=$error; restore=true")
                restore(context)
                mode = null
                false
            } else {
                mode = chosen
                LiveAnalyzerController.configureMode(context, chosen in listOf(Mode.DIRECT_48K,Mode.TEST_48K))
                // Save a readable copy of a newly created wideband slot without touching the audio path.
                if (chosen in listOf(Mode.DIRECT_48K, Mode.TEST_48K)) {
                    runCatching { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        AnalyzerInspectionExport.exportSetups(context)
                    } }.onFailure { AnalyzerAudioTrace.write(context, "SETUP_EXPORT_FAILED", "type=${it.javaClass.simpleName}") }
                }
                // Both modes activate their own selected named JSON before starting the analyzer.
                // Avoid the old DIRECT path that started from stale in-memory/legacy values.
                AnalyzerNamedSetupStart.start(context, chosen in listOf(Mode.DIRECT_48K, Mode.TEST_48K))
                AnalyzerAudioTrace.write(context, "BEGIN_SUCCESS", "chosen=$chosen owner=${MicCaptureArbiter.owner} rolling=${RollingAudioEngine.isRunning} direct=${AnalyzerDirectAudioService.isCapturing} subscribers=${AnalyzerSessionGateway.rawPcm.activeSubscribers}")
                true
            }
        } catch (e: Exception) {
            error = e.message ?: e.javaClass.simpleName
            AnalyzerAudioTrace.write(context, "BEGIN_EXCEPTION", "type=${e.javaClass.simpleName} message=$error")
            runCatching { restore(context) }.onFailure { AnalyzerAudioTrace.write(context, "RESTORE_EXCEPTION", "type=${it.javaClass.simpleName} message=${it.message}") }
            mode = null
            false
        }
    }

    /** Only call after the analyzer has been STOPPED. No implicit stop on a running panel exit. */
    suspend fun finishStopped(context: Context): Boolean {
        AnalyzerAudioTrace.write(context, "PANEL_EXIT_REQUEST", "mode=$mode analyzerRunning=${LiveAnalyzerController.snapshot().running} owner=${MicCaptureArbiter.owner}")
        if (LiveAnalyzerController.snapshot().running) return false
        if (mode == null) return true
        LiveAnalyzerController.persistModeSetup(context)
        val restored = restore(context)
        if (!restored) return false
        mode = null
        AnalyzerAudioTrace.write(context, "PANEL_EXIT_DONE", "owner=${MicCaptureArbiter.owner} rolling=${RollingAudioEngine.isRunning} originalRolling=$originalRolling")
        return true
    }

    /** The audio-only source may be shut down at STOP; the temporary MODE stays selected. */
    fun stopMeasurement(context: Context) {
        AnalyzerAudioTrace.write(context, "STOP_REQUEST", "mode=$mode owner=${MicCaptureArbiter.owner} rolling=${RollingAudioEngine.isRunning}")
        LiveAnalyzerController.stop()
        if (mode == Mode.DIRECT_48K) AnalyzerDirectAudioService.requestStop(context)
        if (mode in listOf(Mode.TEST_8K,Mode.TEST_48K)) AnalyzerTestSignal.stop()
        AnalyzerAudioTrace.write(context, "STOP_DONE", "mode=$mode owner=${MicCaptureArbiter.owner} rolling=${RollingAudioEngine.isRunning}")
    }

    /** Resume an existing analyzer session. Never reload the active named JSON on resume:
     * it is already applied, and its strict STOP/MON guards are only valid for a
     * genuine setup activation. This path does NOT stop or recreate Rolling PCM.
     * Errors must be returned, never escape from a Compose coroutine.
     */
    suspend fun restartMeasurement(context: Context): Boolean {
        AnalyzerAudioTrace.write(context, "RESTART_REQUEST", "mode=$mode owner=${MicCaptureArbiter.owner} rolling=${RollingAudioEngine.isRunning}")
        error = ""
        val chosen = mode ?: run { error = "Keine Analyzer-Betriebsart"; return false }
        if (LiveAnalyzerController.snapshot().running) {
            AnalyzerAudioTrace.write(context, "RESTART_ALREADY_RUNNING", "mode=$chosen")
            return true
        }
        return try {
            if (chosen in listOf(Mode.TEST_8K, Mode.TEST_48K)) {
                val rate = if (chosen == Mode.TEST_48K)
                    context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
                        .getInt("mode_48_direct_sample_rate", 48000) else 8000
                if (!AnalyzerTestSignal.start(context, rate, chosen == Mode.TEST_48K)) {
                    error = "Ungültiges digitales Testsignal für die gewählte Abtastrate"
                    return false
                }
            }
            if (chosen == Mode.DIRECT_48K) {
                for (i in 0 until 120) {
                    if (MicCaptureArbiter.owner != "analyzer_48k") break
                    delay(50)
                }
                if (MicCaptureArbiter.owner == "analyzer_48k") {
                    error = "Direkteingang noch belegt"
                    return false
                }
                if (!AnalyzerDirectAudioService.requestStart(context)) {
                    error = AnalyzerDirectAudioService.lastError
                    return false
                }
                for (i in 0 until 120) {
                    if (AnalyzerDirectAudioService.isCapturing || AnalyzerDirectAudioService.lastError.isNotBlank()) break
                    delay(50)
                }
                AnalyzerAudioTrace.write(context, "RESTART_DIRECT_RESULT", "capturing=${AnalyzerDirectAudioService.isCapturing} owner=${MicCaptureArbiter.owner} error=${AnalyzerDirectAudioService.lastError}")
                if (!AnalyzerDirectAudioService.isCapturing) {
                    error = AnalyzerDirectAudioService.lastError.ifBlank { "Direkt-Neustart Timeout" }
                    return false
                }
            }
            if (chosen == Mode.ROLLING_8K) {
                // Do not reload named setups or old SharedPreferences here.
                // Preserve the exact DSP/filterbank draft edited during STOP.
                val checkpoint = LiveAnalyzerController.captureStoppedAnalyzerConfigCheckpoint()
                LiveAnalyzerController.startFromStoppedConfiguration(context, checkpoint)
            } else {
                LiveAnalyzerController.start(context)
            }
            AnalyzerAudioTrace.write(context, "RESTART_DONE", "mode=$chosen owner=${MicCaptureArbiter.owner} subscribers=${AnalyzerSessionGateway.rawPcm.activeSubscribers}")
            true
        } catch (failure: kotlinx.coroutines.CancellationException) {
            // Lifecycle cancellation is not a recoverable restart failure.
            throw failure
        } catch (failure: Exception) {
            error = failure.message ?: failure.javaClass.simpleName
            AnalyzerAudioTrace.write(context, "RESTART_EXCEPTION", "mode=$chosen type=${failure.javaClass.simpleName} message=$error")
            false
        }
    }

    private suspend fun restore(context: Context): Boolean {
        AnalyzerAudioTrace.write(context, "RESTORE_BEGIN", "mode=$mode originalRolling=$originalRolling owner=${MicCaptureArbiter.owner} rolling=${RollingAudioEngine.isRunning}")
        LiveAnalyzerController.stop()
        AnalyzerDirectAudioService.requestStop(context)
        AnalyzerTestSignal.stop()
        for (i in 0 until 120) {
            if (MicCaptureArbiter.owner != "analyzer_48k") break
            delay(50)
        }
        AnalyzerAudioTrace.write(context, "RESTORE_MIC_RELEASE", "owner=${MicCaptureArbiter.owner} direct=${AnalyzerDirectAudioService.isCapturing}")
        val store = ReminderSettingsStore(context.applicationContext)
        store.setRollingBufferEnabled(originalRolling)
        val requestAccepted = if (originalRolling) RollingAudioService.requestStart(context)
            else { RollingAudioService.requestStop(context); true }
        // Reconcile the stale wideband controller configuration synchronously even
        // if starting Rolling is still asynchronous. The UI can recompose immediately.
        LiveAnalyzerController.configureMode(context, false)
        // Both start and stop requests are asynchronous. Confirm the final resource
        // state before returning; otherwise the first exit falsely fails and a second
        // back-arrow press succeeds only after the stop request catches up.
        for (i in 0 until 120) {
            val ready = if (originalRolling)
                RollingAudioEngine.isRunning && MicCaptureArbiter.owner == "rolling"
            else !RollingAudioEngine.isRunning && MicCaptureArbiter.owner == null
            if (ready) break
            delay(50)
        }
        val restored = if (originalRolling) RollingAudioEngine.isRunning && MicCaptureArbiter.owner == "rolling"
            else MicCaptureArbiter.owner == null && !RollingAudioEngine.isRunning
        AnalyzerAudioTrace.write(context, "RESTORE_RESULT", "requestAccepted=$requestAccepted restored=$restored originalRolling=$originalRolling rolling=${RollingAudioEngine.isRunning} owner=${MicCaptureArbiter.owner}")
        if (!restored) {
            error = "Rolling-Wiederherstellung nicht bestätigt"
            // Leave the recovery marker intact for the next app start. Never claim success.
            return false
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("restore_pending", false).commit()
        AnalyzerAudioTrace.write(context, "RESTORE_DONE", "originalRolling=$originalRolling rolling=${RollingAudioEngine.isRunning} owner=${MicCaptureArbiter.owner} subscribers=${AnalyzerSessionGateway.rawPcm.activeSubscribers}")
        return true
    }

    suspend fun recoverAfterProcessDeath(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean("restore_pending", false)) return
        AnalyzerAudioTrace.write(context, "PROCESS_RECOVERY", "pending=true owner=${MicCaptureArbiter.owner}")
        originalRolling = prefs.getBoolean("original_rolling", false)
        val analyzerPrefs = context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
        if (prefs.getString("chosen_mode", "") in listOf(Mode.DIRECT_48K.name, Mode.TEST_48K.name) &&
            analyzerPrefs.contains("mode_8_fft_size")) {
            LiveAnalyzerController.recoverWidebandSettings(context)
        }
        restore(context)
        mode = null
    }
}
