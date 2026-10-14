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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.currentCoroutineContext

/** Exclusive selectable-rate analyzer source. No rolling, recording or WAV writes in this service. */
class AnalyzerDirectAudioService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AnalyzerAudioTrace.write(this, "DIRECT_SERVICE_COMMAND", "action=${intent?.action} startId=$startId activeJob=${job?.isActive} owner=${MicCaptureArbiter.owner}")
        if (intent?.action == STOP) {
            job?.cancel()
            job = null
            stopSelf()
            return START_NOT_STICKY
        }
        if (job?.isActive == true) return START_NOT_STICKY
        ensureChannel(this)
        startForeground(NOTIFICATION_ID, notification())
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LFN-Scout:Analyzer48")
            .apply { acquire() }
        val captureToken = AnalyzerSetupTransitionCoordinator.captureJobRegistered(AnalyzerSetupTransitionCoordinator.DIRECT)
        job = scope.launch { capture() }
        job?.invokeOnCompletion {
            AnalyzerSetupTransitionCoordinator.captureJobCompleted(AnalyzerSetupTransitionCoordinator.DIRECT, captureToken)
        }
        return START_NOT_STICKY // Do not silently restart microphone after a process crash.
    }

    @SuppressLint("MissingPermission")
    private suspend fun capture() {
        var lease: MicCaptureArbiter.Token? = null
        var recorder: AudioRecord? = null
        var framesRead = 0L
        var lastHeartbeatNs = System.nanoTime()
        AnalyzerAudioTrace.write(this, "DIRECT_CAPTURE_ENTER", "owner=${MicCaptureArbiter.owner}")
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                error("Mikrofonberechtigung fehlt")
            // An old rolling capture can still be shutting down. Never open a second recorder.
            for (attempt in 0 until 100) {
                lease = MicCaptureArbiter.tryAcquire("analyzer_48k")
                if (lease != null) break
                kotlinx.coroutines.delay(50)
            }
            AnalyzerAudioTrace.write(this, "DIRECT_LEASE", "acquired=${lease != null} owner=${MicCaptureArbiter.owner}")
            if (lease == null) error("Mikrofon von ${MicCaptureArbiter.owner} belegt")
            val analyzerPrefs = getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
            val rate = analyzerPrefs.getInt("mode_48_direct_sample_rate",48000)
            require(rate in DirectAnalyzerConfig.inputRates) { "Ungültige Direkt-Abtastrate: $rate" }
            val min = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            if (min <= 0) error("$rate Hz PCM16 vom Gerät nicht unterstützt")
            val prefs = ReminderSettingsStore(applicationContext).settingsFlow.first()
            val requestedSource = analyzerPrefs.getString("mode_48_direct_audio_source","AUTO") ?: "AUTO"
            val sourceOrder = when(requestedSource) {
                "UNPROCESSED" -> listOf("UNPROCESSED")
                "MIC" -> listOf("MIC")
                "VOICE_RECOGNITION" -> listOf("VOICE_RECOGNITION")
                "AUTO" -> AudioInputSelectionPolicy.sourceOrder(prefs.audioSourceMode).map { it.name }
                else -> error("Unbekannte Audioquelle: $requestedSource")
            }
            for (candidate in sourceOrder) {
                val source = when (candidate) {
                    "UNPROCESSED" -> if (Build.VERSION.SDK_INT >= 24) MediaRecorder.AudioSource.UNPROCESSED else continue
                    "MIC" -> MediaRecorder.AudioSource.MIC
                    "VOICE_RECOGNITION" -> MediaRecorder.AudioSource.VOICE_RECOGNITION
                    else -> continue
                }
                val candidateRecorder = runCatching { AudioRecord(source, rate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, maxOf(min, 8192)) }.getOrNull()
                if (candidateRecorder?.state == AudioRecord.STATE_INITIALIZED) {
                    val preferred = AudioInputManager.findInput(applicationContext, prefs.audioInputDeviceId)
                    // Register ownership before optional routing can throw, so
                    // the capture finally always releases this AudioRecord.
                    recorder = candidateRecorder
                    if (preferred != null) candidateRecorder.setPreferredDevice(preferred)
                    break
                }
                runCatching { candidateRecorder?.release() }.onFailure {
                    lease?.markHardwareReleaseUncertain()
                    throw IllegalStateException("Direct AudioRecord candidate release failed; microphone quarantined", it)
                }
            }
            val active = recorder ?: error("Kein $rate-Hz-AudioRecord mit Quelle $requestedSource initialisierbar")
            if (active.sampleRate != rate) error("AudioRecord meldet ${active.sampleRate} statt $rate Hz")
            active.startRecording()
            if (active.recordingState != AudioRecord.RECORDSTATE_RECORDING) error("Direktmikrofon startet nicht ($rate Hz)")
            AnalyzerAudioTrace.write(this, "DIRECT_RECORDING_STARTED", "recordState=${active.recordingState} rate=${active.sampleRate} owner=${MicCaptureArbiter.owner}")
            AnalyzerSessionGateway.rawPcm.markDiscontinuity()
            isCapturing = true
            lastError = ""
            val input = ShortArray(4096)
            while (currentCoroutineContext().isActive) {
                val count = active.read(input, 0, input.size, AudioRecord.READ_BLOCKING)
                if (count < 0) error("$rate-Hz-AudioRecord.read: $count")
                if (count > 0) framesRead += count
                val nowNs = System.nanoTime()
                if (nowNs - lastHeartbeatNs >= 15_000_000_000L) {
                    lastHeartbeatNs = nowNs
                    AnalyzerAudioTrace.write(this, "DIRECT_PCM_HEARTBEAT", "samples=$framesRead subscribers=${AnalyzerSessionGateway.rawPcm.activeSubscribers} owner=${MicCaptureArbiter.owner} recordingState=${active.recordingState}")
                }
                if (count > 0 && AnalyzerSessionGateway.rawPcm.activeSubscribers > 0) {
                    val firstNs = System.nanoTime() - count.toLong() * 1_000_000_000L / rate.toLong()
                    val route = active.routedDevice?.let(AudioInputManager::buildLabel) ?: "Auto"
                    AnalyzerSessionGateway.rawPcm.publish(input, count, rate, "Analyzer direkt", route, 0, firstNs)
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            AnalyzerAudioTrace.write(this, "DIRECT_CAPTURE_CANCELLED", "samples=$framesRead owner=${MicCaptureArbiter.owner}")
            // Controlled STOP is not a hardware failure.
        } catch (e: Exception) {
            lastError = e.message ?: e.javaClass.simpleName
            AnalyzerAudioTrace.write(this, "DIRECT_CAPTURE_FAILURE", "type=${e.javaClass.simpleName} message=$lastError")
            DebugLogger.log(applicationContext, "ANALYZER_48K_FAILURE", lastError)
        } finally {
            AnalyzerAudioTrace.write(this, "DIRECT_CLEANUP_ENTER", "samples=$framesRead record=${recorder?.recordingState} owner=${MicCaptureArbiter.owner}")
            isCapturing = false
            recorder?.let { r ->
                runCatching { if (r.recordingState == AudioRecord.RECORDSTATE_RECORDING) r.stop() }
                runCatching { r.release() }.onFailure { lease?.markHardwareReleaseUncertain() }
            }
            lease?.close()
            AnalyzerAudioTrace.write(this, "DIRECT_CLEANUP_DONE", "owner=${MicCaptureArbiter.owner} subscribers=${AnalyzerSessionGateway.rawPcm.activeSubscribers}")
            AnalyzerSessionGateway.rawPcm.markDiscontinuity()
            stopSelf()
        }
    }

    override fun onDestroy() {
        AnalyzerSetupTransitionCoordinator.serviceDestroyed(AnalyzerSetupTransitionCoordinator.DIRECT)
        AnalyzerAudioTrace.write(this, "DIRECT_ON_DESTROY", "activeJob=${job?.isActive} owner=${MicCaptureArbiter.owner}")
        job?.cancel()
        scope.cancel()
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
        wakeLock = null
        super.onDestroy()
    }

    private fun notification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("LFN-Scout · Direkt-Analyzer")
        .setContentText("Direkte Analyse ohne Rolling-Aufzeichnung")
        .setOngoing(true)
        .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        .build()

    companion object {
        private const val CHANNEL_ID = "analyzer_direct_48k"
        private const val NOTIFICATION_ID = 4103
        private const val START = "de.brummbeobachter.app.ANALYZER_48_START"
        private const val STOP = "de.brummbeobachter.app.ANALYZER_48_STOP"
        @Volatile var isCapturing = false
            private set
        @Volatile var lastError = ""
            private set
        private fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= 26)
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(NotificationChannel(CHANNEL_ID, "Analyzer direkt", NotificationManager.IMPORTANCE_LOW))
        }
        fun requestStart(context: Context): Boolean = AnalyzerSetupTransitionCoordinator.withTrackedServiceStart(AnalyzerSetupTransitionCoordinator.DIRECT) { runCatching {
            AnalyzerAudioTrace.write(context, "DIRECT_REQUEST_START", "owner=${MicCaptureArbiter.owner} capturing=$isCapturing")
            lastError = ""
            ensureChannel(context)
            ContextCompat.startForegroundService(context, Intent(context, AnalyzerDirectAudioService::class.java).setAction(START))
            true
        }.getOrElse { lastError = it.message ?: it.javaClass.simpleName; AnalyzerAudioTrace.write(context, "DIRECT_REQUEST_START_FAILED", "error=$lastError"); false } }
        fun requestStop(context: Context) {
            AnalyzerAudioTrace.write(context, "DIRECT_REQUEST_STOP", "owner=${MicCaptureArbiter.owner} capturing=$isCapturing")
            runCatching { context.startService(Intent(context, AnalyzerDirectAudioService::class.java).setAction(STOP)) }
        }
    }
}
