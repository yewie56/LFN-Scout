package de.brummbeobachter.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class RollingAudioService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var recordJob: Job? = null
    private var operationStartElapsedMs: Long = 0L
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AnalyzerAudioTrace.write(this, "ROLLING_SERVICE_COMMAND", "action=${intent?.action} startId=$startId job=${recordJob?.isActive} owner=${MicCaptureArbiter.owner} engine=${RollingAudioEngine.isRunning}")
        if (intent?.action == ACTION_STOP) {
            stopRolling("user_or_setting")
            return START_NOT_STICKY
        }
        // AP24: a quarantined AudioRecord must not spawn a new capture job on a fresh service command.
        if (MicCaptureArbiter.uncertainReleaseOwner != null) {
            AnalyzerAudioTrace.write(this, "ROLLING_START_BLOCKED_UNCERTAIN_RELEASE", "owner=${MicCaptureArbiter.uncertainReleaseOwner}")
            DebugLogger.log(applicationContext, "ROLLING_START_BLOCKED", "uncertainReleaseOwner=${MicCaptureArbiter.uncertainReleaseOwner}")
            stopRolling("uncertain_release")
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, buildNotification("Rolling-Buffer startet …"))
        if (wakeLock?.isHeld != true) {
            wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LFN-Scout:RollingAudio")
                .apply { acquire() }
        }
        // Do not relaunch while a cancelled capture is still releasing AudioRecord.
        if (recordJob?.isCompleted != false) {
            if (operationStartElapsedMs == 0L) operationStartElapsedMs = android.os.SystemClock.elapsedRealtime()
            val captureToken = AnalyzerSetupTransitionCoordinator.captureJobRegistered(AnalyzerSetupTransitionCoordinator.ROLLING)
            recordJob = scope.launch {
                while (true) {
                    runRecorder()
                    // A failed AudioRecord.release() quarantines the microphone for this process.
                    // Do not schedule futile automatic restarts against an unrecoverable lease.
                    if (MicCaptureArbiter.uncertainReleaseOwner != null) {
                        AnalyzerAudioTrace.write(this@RollingAudioService, "ROLLING_RESTART_BLOCKED_UNCERTAIN_RELEASE", "owner=${MicCaptureArbiter.uncertainReleaseOwner}")
                        DebugLogger.log(applicationContext, "ROLLING_RESTART_BLOCKED", "uncertainReleaseOwner=${MicCaptureArbiter.uncertainReleaseOwner}")
                        break
                    }
                    val enabled = runCatching {
                        ReminderSettingsStore(applicationContext).settingsFlow.first().rollingBufferEnabled
                    }.getOrDefault(false)
                    if (!enabled) break
                    DebugLogger.log(applicationContext, "ROLLING_AUTO_RESTART_WAIT", "seconds=5")
                    delay(5_000)
                }
            }
            recordJob?.invokeOnCompletion {
                AnalyzerSetupTransitionCoordinator.captureJobCompleted(AnalyzerSetupTransitionCoordinator.ROLLING, captureToken)
            }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private suspend fun runRecorder() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            DebugLogger.log(applicationContext, "ROLLING_START_FAILED", "reason=microphone_permission")
            stopRolling("permission_missing")
            return
        }
        val settings = ReminderSettingsStore(applicationContext).settingsFlow.first()
        if (!settings.rollingBufferEnabled) {
            stopRolling("setting_disabled")
            return
        }

        // Rolling service is the only microphone owner while its token is held.
        // If a direct recording is finishing, retry without opening a second AudioRecord.
        val captureLease = MicCaptureArbiter.tryAcquire("rolling")
        AnalyzerAudioTrace.write(this, "ROLLING_LEASE", "acquired=${captureLease != null} owner=${MicCaptureArbiter.owner} subscribers=${AnalyzerSessionGateway.rawPcm.activeSubscribers}")
        if (captureLease == null) {
            DebugLogger.log(applicationContext, "ROLLING_WAIT_MIC", "owner=${MicCaptureArbiter.owner}")
            return
        }
        try {
        val sourceRate = AudioRecordingPolicy.SOURCE_SAMPLE_RATE_HZ
        val targetRate = AudioRecordingPolicy.TARGET_SAMPLE_RATE_HZ
        val decimation = sourceRate / targetRate
        val minBuffer = AudioRecord.getMinBufferSize(sourceRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (minBuffer <= 0) {
            DebugLogger.log(applicationContext, "ROLLING_START_FAILED", "reason=unsupported_rate")
            stopRolling("unsupported_rate")
            return
        }

        fun createRecorder(source: Int) = AudioRecord(
            source, sourceRate, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, maxOf(minBuffer, 4096)
        )

        var fftEpoch: RollingFftEpochEngine? = null
        var rollingHalfHour: RollingHalfHourStatistics? = null
        var rollingFilterHalfHour: RollingFilterHalfHourStatistics? = null
        var recorder: AudioRecord? = null
        var usedMode: AudioSourceMode? = null
        var preferredLabel = "Auto"
        try {
            val preferred = AudioInputManager.findInput(applicationContext, settings.audioInputDeviceId)
            preferredLabel = preferred?.let(AudioInputManager::buildLabel) ?: "Auto"
            for (candidate in AudioInputSelectionPolicy.sourceOrder(settings.audioSourceMode)) {
                val source = when (candidate) {
                    AudioSourceMode.UNPROCESSED -> if (Build.VERSION.SDK_INT >= 24) MediaRecorder.AudioSource.UNPROCESSED else continue
                    AudioSourceMode.MIC -> MediaRecorder.AudioSource.MIC
                    AudioSourceMode.AUTO -> continue
                }
                val r = runCatching { createRecorder(source) }.getOrNull()
                if (r != null && r.state == AudioRecord.STATE_INITIALIZED) {
                    // Own the initialized recorder before device routing can throw.
                    // The outer finally must be able to release this instance.
                    recorder = r
                    if (preferred != null) r.setPreferredDevice(preferred)
                    usedMode = candidate
                    break
                } else runCatching { r?.release() }.onFailure {
                    captureLease.markHardwareReleaseUncertain()
                    throw IllegalStateException("Rolling AudioRecord candidate release failed; microphone quarantined", it)
                }
            }
            val active = recorder ?: error("AudioRecord konnte nicht initialisiert werden")
            val mode = usedMode ?: error("Keine Audioquelle")
            active.startRecording()
            AnalyzerAudioTrace.write(this, "ROLLING_RECORDING_STARTED", "rate=${active.sampleRate} recordState=${active.recordingState} owner=${MicCaptureArbiter.owner}")
            AnalyzerSessionGateway.rawPcm.markDiscontinuity()
            AnalyzerSessionGateway.pcm.markDiscontinuity()
            val routed = active.routedDevice?.let(AudioInputManager::buildLabel) ?: preferredLabel
            RollingAudioEngine.start(mode.displayName(), routed)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(RESTART_NOTIFICATION_ID)
            DebugLogger.log(applicationContext, "ROLLING_STARTED", "gain=${settings.audioGainDb} source=${mode.name} device=$routed")

            scope.launch { RollingHalfHourCloudOutbox.retry(applicationContext) }
            NoiseFloorReference.attach(applicationContext)
            val halfHourConfig = RollingStatisticsSettings.load(applicationContext)
            val impulseDiagnostics = RollingImpulseDiagnostics()
            val epochExport = if (halfHourConfig.enabled && halfHourConfig.exportEpochCsv)
                RollingEpochCsvExporter(applicationContext, halfHourConfig.filterEnabled) else null
            fun queueEpochSnapshot(snapshot: RollingEpochCsvExporter.Snapshot?) {
                if (snapshot != null) scope.launch { epochExport?.write(snapshot) }
            }
            rollingHalfHour = if (halfHourConfig.enabled) RollingHalfHourStatistics(
                minimumHz = halfHourConfig.minimumHz, maximumHz = halfHourConfig.maximumHz,
                onCompleted = { result ->
                if (halfHourConfig.exportOverallCsv) {
                    val item = result
                    val diagnostic = impulseDiagnostics.finish(item.startEpochMs)
                    scope.launch { RollingHalfHourCsvExporter.write(applicationContext, item, diagnostic) }
                }
                scope.launch { RollingHalfHourCloudOutbox.enqueueAndDrain(applicationContext, result) }
                if (!halfHourConfig.filterEnabled) queueEpochSnapshot(epochExport?.flush(result.startEpochMs))
                DebugLogger.log(applicationContext, "ROLLING_HALF_HOUR_RESULT",
                    "start=${result.startEpochMs} epochs=${result.validEpochs} " +
                    "fft_observed=${result.fftSecondsObserved} fft_missing=${result.fftSecondsMissing} " +
                    "leq=${result.leqDbfs} l50=${result.l50Dbfs} l90=${result.l90Dbfs} " +
                    "quality=${result.qualityStatus}; csv_enabled=${halfHourConfig.exportOverallCsv}")
            }, onEpoch = { epoch, db ->
                impulseDiagnostics.accept(epoch, db)
                queueEpochSnapshot(epochExport?.overall(epoch, db))
            }) else null
            rollingFilterHalfHour = if (halfHourConfig.enabled && halfHourConfig.filterEnabled) RollingFilterHalfHourStatistics(
                minimumHz = halfHourConfig.filterMinimumHz, maximumHz = halfHourConfig.filterMaximumHz,
                bandCount = halfHourConfig.filterCount, method = halfHourConfig.csvFilterMethod,
                onCompleted = { result ->
                if (halfHourConfig.exportFilterCsv) {
                    scope.launch { RollingFilterHalfHourCsvExporter.write(applicationContext, result) }
                }
                queueEpochSnapshot(epochExport?.flush(result.firstOrNull()?.startEpochMs ?: -1L))
                DebugLogger.log(applicationContext, "ROLLING_FILTER_HALF_HOUR_RESULT",
                    "start=${result.firstOrNull()?.startEpochMs} bands=${result.size} csv_enabled=${halfHourConfig.exportFilterCsv} method=${halfHourConfig.csvFilterMethod}")
            }, onEpoch = { epoch, levels -> queueEpochSnapshot(epochExport?.filters(epoch, levels)) }) else null
            fftEpoch = if (halfHourConfig.enabled) RollingFftEpochEngine(1000) { epochMs, frequencies, powers ->
                val correctedPowers = NoiseFloorReference.compensateRollingSpectrum(powers)
                val overall = RollingFftEpochEngine.bandDb(frequencies, correctedPowers,
                    halfHourConfig.minimumHz.toDouble(), halfHourConfig.maximumHz.toDouble())
                rollingHalfHour?.acceptFftEpoch(epochMs, overall)
                if (halfHourConfig.filterEnabled) {
                    val width = (halfHourConfig.filterMaximumHz - halfHourConfig.filterMinimumHz).toDouble() / halfHourConfig.filterCount
                    val levels = (0 until halfHourConfig.filterCount).map { index ->
                        val low = halfHourConfig.filterMinimumHz + index * width
                        RollingFftEpochEngine.bandDb(frequencies, correctedPowers, low, low + width)
                    }
                    rollingFilterHalfHour?.acceptFftEpoch(epochMs, levels)
                }
            } else null
            val perfMethod = halfHourConfig.csvFilterMethod.name
            val perfBandCount = halfHourConfig.filterCount
            var perfWindowStart = System.currentTimeMillis()
            fun writeFilterPerformanceWindow(endMs: Long) {
                val perf = rollingFilterHalfHour?.consumePerformanceWindow() ?: return
                if (perf.samples <= 0) return
                val begin = perfWindowStart
                perfWindowStart = endMs
                scope.launch {
                    RollingFilterPerformanceCsvExporter.append(applicationContext, begin, endMs, perf)
                }
                DebugLogger.log(applicationContext, "ROLLING_CSV_FILTER_PERFORMANCE",
                    "method=$perfMethod bands=$perfBandCount samples=${perf.samples} mean_us=${perf.meanMicrosPerSample} peak_us=${perf.peakMicrosPerSample} epoch_peak_us=${perf.peakEpochMicros} realtime_pct=${perf.realtimePercent}")
            }
            var rollingSamplesSinceStart = 0L
            val rollingStartedWallMs = System.currentTimeMillis()
            val input = ShortArray(2048)
            val gain = AudioRecordingPolicy.gainFactor(settings.audioGainDb)
            var sum = 0L
            var countGroup = 0
            var notificationTick = 0
            // PCM fan-out is inactive without subscribers. Never call FFT from AudioRecord loop.
            val analyzerBlock = ShortArray(256)
            var analyzerCount = 0
            var samplesRead = 0L
            readAccounting.begin(System.nanoTime())
            var lastHeartbeatNs = System.nanoTime()
            var lastLimitCheckElapsedMs = 0L
            var currentLimitHours = settings.rollingMaxOperationHours
            while (currentCoroutineContext().isActive && settings.rollingBufferEnabled) {
                val elapsedNow = android.os.SystemClock.elapsedRealtime()
                if (elapsedNow - lastLimitCheckElapsedMs >= 5_000L) {
                    lastLimitCheckElapsedMs = elapsedNow
                    currentLimitHours = ReminderSettingsStore(applicationContext).settingsFlow.first().rollingMaxOperationHours
                }
                if (elapsedNow - operationStartElapsedMs >= currentLimitHours.toLong() * 3_600_000L) {
                    DebugLogger.log(applicationContext, "ROLLING_OPERATION_LIMIT", "hours=$currentLimitHours")
                    ReminderSettingsStore(applicationContext).setRollingBufferEnabled(false)
                    if (AnalyzerModeCoordinator.mode == AnalyzerModeCoordinator.Mode.ROLLING_8K) {
                        LiveAnalyzerController.stop()
                    }
                    stopRolling("operation_limit_${currentLimitHours}h")
                    break
                }
                val count = active.read(input, 0, input.size, AudioRecord.READ_BLOCKING)
                // Account for negative AudioRecord return codes BEFORE throwing;
                // otherwise a failed read vanishes from the continuity diagnostics.
                readAccounting.onRead(count, System.nanoTime())
                if (count < 0) error("Audio-Lesefehler: $count")
                if (count > 0) samplesRead += count
                val nowNs = System.nanoTime()
                if (nowNs - lastHeartbeatNs >= 15_000_000_000L) {
                    lastHeartbeatNs = nowNs
                    AnalyzerAudioTrace.write(this, "ROLLING_PCM_HEARTBEAT", "samples=$samplesRead subscribers=${AnalyzerSessionGateway.rawPcm.activeSubscribers} engine=${RollingAudioEngine.isRunning} bufferedSec=${RollingAudioEngine.bufferedSeconds()} owner=${MicCaptureArbiter.owner}")
                }
                val currentRouted = active.routedDevice?.let(AudioInputManager::buildLabel) ?: routed
                RollingAudioEngine.updateInput(mode.displayName(), currentRouted)
                // High-speed PRE-gain and PRE-decimation, only when subscribed; 8-kHz actual rate.
                // Approximate first-sample monotonic time from read completion (not AudioTimestamp).
                if (count > 0 && AnalyzerSessionGateway.rawPcm.activeSubscribers > 0) {
                    val firstSampleNs = System.nanoTime() - count.toLong() * 1_000_000_000L / sourceRate
                    AnalyzerSessionGateway.rawPcm.publish(input, count, sourceRate,
                        mode.displayName(), currentRouted, 0, firstSampleNs)
                }
                for (i in 0 until count) {
                    sum += input[i].toLong(); countGroup++
                    if (countGroup == decimation) {
                        val rawAverage = sum.toDouble() / decimation
                        val rawForStatistics = rawAverage.roundToInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        val amplified = rawAverage * gain
                        val clipped = amplified > Short.MAX_VALUE || amplified < Short.MIN_VALUE
                        val sample = amplified.roundToInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        RollingAudioEngine.append(sample, clipped)
                        fftEpoch?.accept(rawForStatistics, rollingStartedWallMs + rollingSamplesSinceStart)
                        rollingSamplesSinceStart++
                        if (rollingFilterHalfHour != null && rollingSamplesSinceStart % 60_000L == 0L) {
                            writeFilterPerformanceWindow(System.currentTimeMillis())
                        }
                        if (AnalyzerSessionGateway.pcm.activeSubscribers > 0) {
                            analyzerBlock[analyzerCount++] = sample
                            if (analyzerCount == analyzerBlock.size) {
                                AnalyzerSessionGateway.pcm.publish(analyzerBlock, analyzerCount, targetRate,
                                    mode.displayName(), currentRouted, settings.audioGainDb)
                                analyzerCount = 0
                            }
                        } else analyzerCount = 0
                        sum = 0; countGroup = 0
                    }
                }
                notificationTick++
                if (notificationTick >= 30) {
                    notificationTick = 0
                    val sec = RollingAudioEngine.bufferedSeconds()
                    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                        .notify(NOTIFICATION_ID, buildNotification("Rolling-Buffer · ${RollingAudioPolicy.bufferStatusText(sec)}"))
                }
            }
        } catch (e: CancellationException) {
            AnalyzerAudioTrace.write(this, "ROLLING_CAPTURE_CANCELLED", "owner=${MicCaptureArbiter.owner}")
            // User stop/service shutdown is not a hardware fault.
            throw e
        } catch (e: Exception) {
            AnalyzerAudioTrace.write(this, "ROLLING_CAPTURE_FAILED", "type=${e.javaClass.simpleName} message=${e.message}")
            DebugLogger.log(applicationContext, "ROLLING_RECORD_FAILED", "error=${e.javaClass.simpleName} message=${e.message ?: "-"}")
            showRestartNotification()
        } finally {
            AnalyzerAudioTrace.write(this, "ROLLING_CLEANUP_ENTER", "owner=${MicCaptureArbiter.owner} record=${recorder?.recordingState}")
            recorder?.let { activeRecorder ->
                runCatching {
                    if (activeRecorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        activeRecorder.stop()
                    }
                }
                runCatching { activeRecorder.release() }.onFailure { captureLease.markHardwareReleaseUncertain() }
            }
            fftEpoch?.stop()
            rollingHalfHour?.stop()
            rollingFilterHalfHour?.stop()
            // v0.5.45.1: The separate PCM filterbank is inactive in FFT rolling mode.
            // Its performance callback is scoped to the capture body; do not call it
            // from this outer finally block. Rolling FFT epochs are flushed above.
            RollingAudioEngine.stop()
        }
        } finally {
            // Also releases on unsupported sample rate and other early exits.
            captureLease.close()
            AnalyzerAudioTrace.write(this, "ROLLING_CLEANUP_DONE", "owner=${MicCaptureArbiter.owner} engine=${RollingAudioEngine.isRunning}")
        }
    }

    private fun stopRolling(reason: String) {
        AnalyzerAudioTrace.write(this, "ROLLING_STOP_REQUEST", "reason=$reason job=${recordJob?.isActive} owner=${MicCaptureArbiter.owner}")
        DebugLogger.log(applicationContext, "ROLLING_STOPPED", "reason=$reason")
        // Preserve the cancelled job reference until completion, not merely cancellation.
        recordJob?.cancel()
        operationStartElapsedMs = 0L
        RollingAudioEngine.stop()
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
        wakeLock = null
        if (Build.VERSION.SDK_INT >= 24) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        AnalyzerSetupTransitionCoordinator.serviceDestroyed(AnalyzerSetupTransitionCoordinator.ROLLING)
        AnalyzerAudioTrace.write(this, "ROLLING_ON_DESTROY", "job=${recordJob?.isActive} owner=${MicCaptureArbiter.owner}")
        RollingAudioEngine.stop()
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
        wakeLock = null
        scope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(text: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("LFN-Scout")
        .setContentText(text)
        .setOngoing(true)
        .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        .build()

    private fun showRestartNotification() {
        showRestartNeededNotification(applicationContext)
    }

    companion object {
        internal val readAccounting = RollingPcmReadAccounting()
        private const val CHANNEL_ID = "rolling_audio"
        private const val NOTIFICATION_ID = 4101
        private const val RESTART_NOTIFICATION_ID = 4102
        private const val ACTION_START = "de.brummbeobachter.app.ROLLING_START"
        private const val ACTION_STOP = "de.brummbeobachter.app.ROLLING_STOP"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= 26) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Rolling-Audio", NotificationManager.IMPORTANCE_LOW))
            }
        }

        fun showRestartNeededNotification(context: Context) {
            ensureChannel(context)
            val n = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Rolling-Buffer gestoppt")
                .setContentText("Tippen zum Fortsetzen")
                .setAutoCancel(true)
                .setContentIntent(
                    PendingIntent.getActivity(
                        context,
                        1,
                        Intent(context, MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .build()
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(RESTART_NOTIFICATION_ID, n)
        }

        fun requestStart(context: Context): Boolean = AnalyzerSetupTransitionCoordinator.withTrackedServiceStart(AnalyzerSetupTransitionCoordinator.ROLLING) {
            AnalyzerAudioTrace.write(context, "ROLLING_REQUEST_START", "owner=${MicCaptureArbiter.owner} engine=${RollingAudioEngine.isRunning}")
            ensureChannel(context)
            runCatching {
                val i = Intent(context, RollingAudioService::class.java).setAction(ACTION_START)
                ContextCompat.startForegroundService(context, i)
                true
            }.getOrElse {
                DebugLogger.log(context.applicationContext, "ROLLING_RESTART_DEFERRED", "error=${it.javaClass.simpleName}")
                false
            }
        }

        fun requestStop(context: Context) {
            AnalyzerAudioTrace.write(context, "ROLLING_REQUEST_STOP", "owner=${MicCaptureArbiter.owner} engine=${RollingAudioEngine.isRunning}")
            runCatching { context.startService(Intent(context, RollingAudioService::class.java).setAction(ACTION_STOP)) }
        }

        private fun formatDuration(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
    }
}
