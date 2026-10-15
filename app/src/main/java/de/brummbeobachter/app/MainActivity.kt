package de.brummbeobachter.app

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.abs

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        DebugLogger.log(
            applicationContext,
            "APP_START"
        )
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            runCatching { AnalyzerInspectionExport.exportOnceAfterUpdate(applicationContext) }
                .onFailure { DebugLogger.log(applicationContext, "ANALYZER_SETUP_EXPORT_ERROR", it.javaClass.simpleName) }
        }

        lifecycleScope.launch {
            OfflineObservationRetry.flush(
                applicationContext,
                "app_start"
            )
            OfflineRetryScheduler.syncWithQueue(
                applicationContext
            )
        }

        if (
            intent.getBooleanExtra(
                ReminderActionReceiver.EXTRA_FROM_REMINDER,
                false
            )
        ) {
            ReminderAlertController.stop(
                applicationContext,
                "MAIN_ACTIVITY_FROM_REMINDER"
            )
        }

        setContent {
            LFNScoutTheme {
                // AP27: opt-in permission onboarding; does not start or stop audio services.
                val missingRuntimePermissions = buildList {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity, Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) add(Manifest.permission.RECORD_AUDIO)
                    val fineGranted = ContextCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    val coarseGranted = ContextCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!fineGranted) {
                        // Android 12+: FINE must be requested together with COARSE.
                        add(Manifest.permission.ACCESS_FINE_LOCATION)
                        add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    } else if (!coarseGranted) {
                        add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                }
                var showPermissionIntro by remember {
                    mutableStateOf(savedInstanceState == null && missingRuntimePermissions.isNotEmpty())
                }
                val startupPermissionsLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    DebugLogger.log(
                        applicationContext,
                        "STARTUP_PERMISSIONS",
                        "mic=${results[Manifest.permission.RECORD_AUDIO]} " +
                            "fine=${results[Manifest.permission.ACCESS_FINE_LOCATION]} " +
                            "coarse=${results[Manifest.permission.ACCESS_COARSE_LOCATION]}"
                    )
                }
                if (showPermissionIntro) {
                    AlertDialog(
                        onDismissRequest = { showPermissionIntro = false },
                        title = { Text("Berechtigungen für LFN-Scout") },
                        text = {
                            Text(
                                "Für Analyzer, MON und Audioaufnahmen benötigt LFN-Scout " +
                                    "Zugriff auf das Mikrofon. Für die Standortzuordnung " +
                                    "der Messungen wird die GPS-/Standortberechtigung " +
                                    "benötigt. Android fragt beide Berechtigungen separat ab. " +
                                    "Du kannst auch ohne Freigabe fortfahren; betroffene " +
                                    "Funktionen stehen dann nur eingeschränkt zur Verfügung."
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showPermissionIntro = false
                                startupPermissionsLauncher.launch(missingRuntimePermissions.toTypedArray())
                            }) { Text("Berechtigungen erteilen") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPermissionIntro = false }) {
                                Text("Später")
                            }
                        }
                    )
                }
                LFNScoutApp(
                    keyStore =
                        KeyStore(applicationContext),
                    supabaseClient =
                        SupabaseClient(),
                    locationProvider =
                        LocationProvider(applicationContext),
                    reminderStore =
                        ReminderSettingsStore(applicationContext),
                    soundStore =
                        ReminderSoundStore(applicationContext),
                    onBackgroundApp = {
                        DebugLogger.log(
                            applicationContext,
                            "APP_AUTO_BACKGROUND",
                            "requested=true"
                        )
                        moveTaskToBack(true)
                    }
                )
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (
            intent.getBooleanExtra(
                ReminderActionReceiver.EXTRA_FROM_REMINDER,
                false
            )
        ) {
            ReminderAlertController.stop(
                applicationContext,
                "MAIN_ACTIVITY_NEW_INTENT_FROM_REMINDER"
            )
        }
    }

    override fun onStop() {
        DebugLogger.log(
            applicationContext,
            "APP_STOP"
        )
        super.onStop()
    }
}

@Composable
fun LFNScoutApp(
    keyStore: KeyStore,
    supabaseClient: SupabaseClient,
    locationProvider: LocationProvider,
    reminderStore: ReminderSettingsStore,
    soundStore: ReminderSoundStore,
    onBackgroundApp: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val appContext =
        LocalContext.current.applicationContext

    var keyInput by remember {
        mutableStateOf("")
    }
    var keyStatus by remember {
        mutableStateOf("")
    }
    var observerId by remember {
        mutableStateOf("")
    }
    var lastRating by remember {
        mutableIntStateOf(-1)
    }
    var transmitStatus by remember {
        mutableStateOf(
            "Noch keine Bewertung übertragen"
        )
    }
    var isSending by remember {
        mutableStateOf(false)
    }

    var recentItems by remember {
        mutableStateOf<List<RecentObservation>>(
            emptyList()
        )
    }
    var recentStatus by remember {
        mutableStateOf(
            "Letzte Bewertungen werden geladen ..."
        )
    }

    var locationStatus by remember {
        mutableStateOf(
            if (
                locationProvider
                    .hasAnyLocationPermission()
            ) {
                "Standort: Berechtigung vorhanden"
            } else {
                "Standort: nicht freigegeben"
            }
        )
    }

    var pendingRating by remember {
        mutableIntStateOf(-1)
    }

    var pendingComment by remember {
        mutableStateOf("")
    }
    var pendingCommentTouched by remember {
        mutableStateOf(false)
    }
    var exportRows by remember {
        mutableStateOf<List<OwnObservation>>(
            emptyList()
        )
    }
    var exportStatus by remember {
        mutableStateOf("")
    }
    var isExporting by remember {
        mutableStateOf(false)
    }

    var reminderSettings by remember {
        mutableStateOf(ReminderSettings())
    }
    var reminderSound by remember {
        mutableStateOf(
            ReminderSound(
                uri = "",
                title = "Standard-Weckton"
            )
        )
    }
    var intervalInput by remember {
        mutableStateOf(
            ReminderPolicy.DEFAULT_INTERVAL_MINUTES
                .toString()
        )
    }

    var currentPanel by remember {
        mutableStateOf(AppPanel.MAIN)
    }
    var showDebugLog by remember {
        mutableStateOf(false)
    }
    val panelHistory = remember { mutableListOf<AppPanel>() }
    fun navigateTo(panel: AppPanel) {
        if (panel == AppPanel.MAIN) {
            panelHistory.clear()
            currentPanel = AppPanel.MAIN
        } else if (panel != currentPanel) {
            panelHistory.add(currentPanel)
            currentPanel = panel
        }
    }
    fun navigateBack() {
        currentPanel = if (panelHistory.isNotEmpty()) {
            panelHistory.removeAt(panelHistory.lastIndex)
        } else {
            AppPanel.MAIN
        }
    }
    BackHandler(enabled = showDebugLog || currentPanel != AppPanel.MAIN) {
        if (showDebugLog) {
            showDebugLog = false
        } else {
            navigateBack()
        }
    }
    var logSaveStatus by remember {
        mutableStateOf("")
    }
    var debugLines by remember {
        mutableStateOf<List<String>>(
            emptyList()
        )
    }
    var soundSelfTestStatus by remember {
        mutableStateOf("")
    }

    var radiusKmInput by remember {
        mutableStateOf(
            RadiusAnalysisPolicy
                .DEFAULT_RADIUS_KM
                .toString()
        )
    }
    var radiusHistogramResult by remember {
        mutableStateOf<RadiusHistogramResult?>(
            null
        )
    }
    var radiusHistogramStatus by remember {
        mutableStateOf("")
    }

    var audioArchiveEntries by remember {
        mutableStateOf<List<AudioArchiveEntry>>(
            emptyList()
        )
    }
    var audioArchiveStatus by remember {
        mutableStateOf("")
    }
    var pendingRollingCommentEntry by remember {
        mutableStateOf<AudioArchiveEntry?>(null)
    }
    var rollingComment by remember {
        mutableStateOf("")
    }
    var rollingDeleteCandidate by remember {
        mutableStateOf<AudioArchiveEntry?>(null)
    }
    var rollingStatus by remember {
        mutableStateOf("")
    }
    var rollingBufferedSeconds by remember {
        mutableIntStateOf(0)
    }

    var waterfallResult by remember {
        mutableStateOf<WaterfallResult?>(
            null
        )
    }
    var waterfallStatus by remember {
        mutableStateOf("")
    }
    var isGeneratingWaterfall by remember {
        mutableStateOf(false)
    }
    var generatingWaterfallRecordingId by remember {
        mutableStateOf<String?>(null)
    }

    val seismicService = remember {
        SeismicLiveService(appContext)
    }
    var seismicSnapshot by remember {
        mutableStateOf<SeismicLiveService.Snapshot?>(null)
    }
    var seismicStatus by remember {
        mutableStateOf("Noch keine Seismikdaten geladen")
    }
    var isLoadingSeismic by remember {
        mutableStateOf(false)
    }
    var seismicSelectedDate by remember {
        mutableStateOf(LocalDate.now(ZoneId.systemDefault()))
    }


fun generateWaterfall(
    entry: AudioArchiveEntry? = null
) {
    if (isGeneratingWaterfall) {
        return
    }

    isGeneratingWaterfall =
        true
    generatingWaterfallRecordingId = entry?.recordingId
    waterfallStatus =
        "Wasserfallspektrum wird erzeugt ..."

    scope.launch {
        try {
            val entries =
                AudioArchive.list(
                    appContext
                )

            val statsRange =
                WaterfallStatsPolicy.effectiveRange(
                    developerModeEnabled = reminderSettings.developerModeEnabled,
                    storedMinHz = reminderSettings.waterfallStatsMinHz,
                    storedMaxHz = reminderSettings.waterfallStatsMaxHz
                )

            DebugLogger.log(
                appContext,
                "WATERFALL_GENERATE_START",
                "local_entries=${entries.size} freq=0-140Hz level_stats=${statsRange.minHz.toInt()}-${statsRange.maxHz.toInt()}Hz requested=${entry?.recordingId ?: "latest"}"
            )

            val selected =
                entry
                    ?: entries.firstOrNull()

            generatingWaterfallRecordingId = selected?.recordingId

            if (selected == null) {
                waterfallStatus =
                    "Keine lokale Audioaufnahme verfügbar"

                DebugLogger.log(
                    appContext,
                    "WATERFALL_GENERATE_FAILED",
                    "reason=no_local_wav"
                )

                return@launch
            }

            val bytes =
                AudioArchive
                    .readWavBytes(
                        appContext,
                        selected
                    )

            if (
                bytes == null ||
                bytes.isEmpty()
            ) {
                waterfallStatus =
                    "WAV-Datei konnte lokal nicht gelesen werden"

                DebugLogger.log(
                    appContext,
                    "WATERFALL_GENERATE_FAILED",
                    "reason=wav_read_failed recording_id=${selected.recordingId} file=${selected.wavFileName}"
                )

                return@launch
            }

            DebugLogger.log(
                appContext,
                "WATERFALL_SOURCE_SELECTED",
                "recording_id=${selected.recordingId} file=${selected.wavFileName} bytes=${bytes.size} gain=${selected.gainDb} comment_present=${!selected.comment.isNullOrBlank()}"
            )

            val result =
                WaterfallGenerator
                    .generate(
                        wavBytes =
                            bytes,
                        sourceFileName =
                            selected.wavFileName,
                        comment =
                            selected.comment,
                        gainDb =
                            selected.gainDb,
                        startEpochMs = runCatching {
                            val base = OffsetDateTime.parse(selected.recordedAt).toInstant().toEpochMilli()
                            if (selected.rollingBuffer) base - selected.durationSeconds * 1000L else base
                        }.getOrNull(),
                        statsMinHz =
                            statsRange.minHz,
                        statsMaxHz =
                            statsRange.maxHz,
                        levelAuto = reminderSettings.waterfallLevelAuto,
                        levelMinDbAt50 = reminderSettings.waterfallLevelMinDbAt50,
                        levelMaxDbAt50 = reminderSettings.waterfallLevelMaxDbAt50
                    )

            waterfallResult =
                result

            waterfallStatus =
                "Wasserfall bereit · " +
                    "%.1f … %.1f dBFS · L50 %.1f · L90 %.1f".format(
                        result.minDb,
                        result.maxDb,
                        result.l50Db,
                        result.l90Db
                    )

            DebugLogger.log(
                appContext,
                "WATERFALL_GENERATE_OK",
                "file=${selected.wavFileName} frames=${result.frameCount} duration_s=${"%.1f".format(result.durationSeconds)} min_db=${"%.1f".format(result.minDb)} max_db=${"%.1f".format(result.maxDb)} l50=${"%.1f".format(result.l50Db)} l90=${"%.1f".format(result.l90Db)} image=${result.imageFileName}"
            )
        } catch (
            error: Exception
        ) {
            waterfallStatus =
                "Wasserfall-Fehler: " +
                    (
                        error.message
                            ?: error.javaClass.simpleName
                        )

            DebugLogger.log(
                appContext,
                "WATERFALL_GENERATE_FAILED",
                "error=${error.javaClass.simpleName} message=${error.message ?: "-"}"
            )
        } finally {
            isGeneratingWaterfall =
                false
            generatingWaterfallRecordingId = null
        }
    }
}


    fun loadSeismic(date: LocalDate = seismicSelectedDate) {
        if (isLoadingSeismic) return

        val today = LocalDate.now(ZoneId.systemDefault())
        if (date.isAfter(today)) return

        seismicSelectedDate = date
        isLoadingSeismic = true
        seismicSnapshot = null
        seismicStatus = "Seismische Tagesdaten für ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} werden geladen ..."

        scope.launch {
            try {
                val location = locationProvider.getObservationLocation()
                if (!location.available) {
                    seismicStatus = "Standort nicht verfügbar – Seismik kann nicht zugeordnet werden"
                    DebugLogger.log(
                        appContext,
                        "SEISMIC_UNAVAILABLE",
                        "reason=location_unavailable"
                    )
                    return@launch
                }

                val result = withContext(Dispatchers.IO) {
                    seismicService.loadNearestVertical(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        selectedDate = date,
                        zoneId = ZoneId.systemDefault(),
                        nearPlotGain = reminderSettings.nearPlotGain
                    )
                }

                when (result) {
                    is SeismicLiveService.Result.Success -> {
                        seismicSnapshot = result.snapshot
                        seismicStatus =
                            if (result.snapshot.isToday) {
                                "Tagesseismogramm 00:00 bis jetzt geladen"
                            } else {
                                "Tagesseismogramm ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} geladen"
                            }
                    }
                    is SeismicLiveService.Result.Failure -> {
                        seismicStatus = result.message
                    }
                }
            } catch (error: Exception) {
                seismicStatus =
                    "Seismik-Fehler: ${error.message ?: error.javaClass.simpleName}"
                DebugLogger.log(
                    appContext,
                    "SEISMIC_FAILED",
                    "phase=ui error=${error.javaClass.simpleName} message=${error.message ?: "-"}"
                )
            } finally {
                isLoadingSeismic = false
            }
        }
    }

    fun refreshAudioArchive() {
        val deleted =
            AudioArchive.cleanup(
                appContext
            )

        audioArchiveEntries =
            AudioArchive.list(
                appContext
            )

        if (deleted > 0) {
            audioArchiveStatus =
                "$deleted gesendete Aufnahme(n) nach 24 h gelöscht"
        }
    }

    LaunchedEffect(currentPanel) {
        if (currentPanel == AppPanel.MORE) {
            refreshAudioArchive()
        }
        if (currentPanel == AppPanel.SEISMIC && seismicSnapshot == null && !isLoadingSeismic) {
            loadSeismic()
        }
    }

    suspend fun retryAudioUpload(
        entry: AudioArchiveEntry
    ) {
        if (entry.rollingBuffer) {
            audioArchiveStatus = "Rolling-Aufnahmen werden nicht in die Cloud übertragen"
            DebugLogger.log(appContext, "ROLLING_UPLOAD_BLOCKED", "recording_id=${entry.recordingId}")
            return
        }
        if (keyInput.isBlank()) {
            audioArchiveStatus =
                "Teilnehmerschlüssel fehlt"
            return
        }

        val wavBytes =
            AudioArchive.readWavBytes(
                appContext,
                entry
            )

        if (
            wavBytes == null ||
            wavBytes.isEmpty()
        ) {
            AudioArchive.markFailed(
                appContext,
                entry.recordingId,
                "Lokale WAV-Datei fehlt"
            )
            DebugLogger.log(
                appContext,
                "AUDIO_RETRY_FILE_MISSING",
                "recording_id=${entry.recordingId}"
            )
            audioArchiveStatus =
                "Lokale WAV-Datei fehlt"
            refreshAudioArchive()
            return
        }

        DebugLogger.log(
            appContext,
            "AUDIO_UPLOAD_START",
            "reason=retry recording_id=${entry.recordingId} bytes=${wavBytes.size} comment_chars=${entry.comment?.length ?: 0}"
        )

        audioArchiveStatus =
            "Audio wird erneut übertragen ..."

        val upload =
            supabaseClient.uploadWav(
                key = keyInput.trim(),
                recordingId =
                    entry.recordingId,
                wavBytes = wavBytes,
                recordedAt =
                    entry.recordedAt,
                gainDb =
                    entry.gainDb,
                audioSource =
                    entry.audioSource,
                clippedSamples =
                    entry.clippedSamples,
                comment =
                    entry.comment,
                captureOrigin = entry.origin,
                triggerType = "MANUAL"
            )

        if (upload.success) {
            AudioArchive.markUploaded(
                appContext,
                entry.recordingId,
                upload.storagePath
            )
            AudioStatisticsOutbox.markRecordingUploaded(appContext, entry.recordingId)
            val (_, statsPending) = AudioStatisticsOutbox.drain(appContext, supabaseClient, keyInput.trim())
            DebugLogger.log(appContext, "AUDIO_STATISTICS_RETRY", "pending=$statsPending")
            DebugLogger.log(
                appContext,
                "AUDIO_UPLOAD_OK",
                "reason=retry recording_id=${entry.recordingId} storage_path=${upload.storagePath ?: "-"}"
            )
            audioArchiveStatus =
                "Audio erfolgreich gesendet · lokale Kopie bleibt 24 h"
        } else {
            AudioArchive.markFailed(
                appContext,
                entry.recordingId,
                upload.message
            )
            DebugLogger.log(
                appContext,
                "AUDIO_UPLOAD_FAILED",
                "reason=retry recording_id=${entry.recordingId} message=${upload.message}"
            )
            audioArchiveStatus =
                upload.message
        }

        refreshAudioArchive()
    }

    fun saveRollingBuffer() {
        if (!reminderSettings.rollingBufferEnabled) {
            rollingStatus = "Rolling-Buffer ist in den Einstellungen deaktiviert"
            return
        }
        if (!RollingAudioEngine.isRunning) {
            rollingStatus = "Rolling-Buffer läuft noch nicht"
            RollingAudioService.requestStart(appContext)
            return
        }
        rollingStatus = "Rolling-Buffer wird gesichert ..."
        scope.launch {
            try {
                val snapshot = withContext(Dispatchers.IO) { RollingAudioEngine.snapshot() }
                if (snapshot == null || snapshot.pcm16.isEmpty()) {
                    rollingStatus = "Rolling-Buffer enthält noch keine Audiodaten"
                    return@launch
                }
                val start = OffsetDateTime.ofInstant(Instant.ofEpochMilli(snapshot.startEpochMs), ZoneId.systemDefault())
                val end = OffsetDateTime.ofInstant(Instant.ofEpochMilli(snapshot.endEpochMs), ZoneId.systemDefault())
                val source = if (snapshot.sourceLabel == "MIC") "MIC" else "UNP"
                val ratingTag = AudioFileNamingPolicy.selectRatingTag(recentItems, end)
                val fileName = AudioFileNamingPolicy.buildRollingWavFileName(
                    observerId = observerId,
                    start = start,
                    end = end,
                    gainDb = reminderSettings.audioGainDb,
                    audioSource = source,
                    observations = recentItems
                )
                val tempFile = withContext(Dispatchers.IO) {
                    val dir = java.io.File(appContext.cacheDir, "audio_recording_temp").apply { mkdirs() }
                    java.io.File(dir, "rolling_${System.currentTimeMillis()}.wav").also { file ->
                        file.outputStream().use { out ->
                            out.write(WavPcm16.buildHeader(
                                sampleRateHz = RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ,
                                channels = 1,
                                bitsPerSample = 16,
                                dataBytes = snapshot.pcm16.size
                            ))
                            out.write(snapshot.pcm16)
                        }
                    }
                }
                val entry = withContext(Dispatchers.IO) {
                    AudioArchive.createRolling(
                        context = appContext,
                        recordingId = UUID.randomUUID().toString(),
                        wavFile = tempFile,
                        targetDisplayName = fileName,
                        recordedAt = end.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        gainDb = reminderSettings.audioGainDb,
                        audioSource = source,
                        clippedSamples = snapshot.clippedSamples,
                        durationSeconds = snapshot.durationSeconds,
                        startTime = start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        endTime = end.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        deviceLabel = snapshot.deviceLabel,
                        ratingTag = ratingTag,
                        comment = null
                    )
                }
                refreshAudioArchive()
                pendingRollingCommentEntry = entry
                rollingComment = ""
                rollingStatus = "Rolling-Aufnahme gespeichert · Kommentar möglich"
            } catch (e: Exception) {
                rollingStatus = "Rolling-Speicherfehler: ${e.message ?: e.javaClass.simpleName}"
                DebugLogger.log(appContext, "ROLLING_SAVE_FAILED", "error=${e.javaClass.simpleName} message=${e.message ?: "-"}")
            }
        }
    }

    suspend fun refreshReminderSettings() {
        reminderSettings =
            reminderStore.settingsFlow.first()
        intervalInput =
            reminderSettings.intervalMinutes
                .toString()
        reminderSound =
            soundStore.soundFlow.first()
        radiusKmInput =
            reminderSettings.radiusAnalysisKm
                .toString()
    }

    suspend fun refreshRecent(key: String) {
        if (key.isBlank()) {
            recentItems = emptyList()
            recentStatus =
                "Kein Teilnehmerschlüssel gespeichert"
            return
        }

        recentStatus =
            "Letzte Bewertungen werden geladen ..."

        val result =
            supabaseClient
                .getRecentObservations(key.trim())

        if (result.success) {
            recentItems = result.items

            if (
                result.observerId.isNotBlank()
            ) {
                observerId =
                    result.observerId
                keyStore.saveObserverId(
                    result.observerId
                )
            }

            recentStatus =
                if (result.items.isEmpty()) {
                    "Noch keine Bewertungen vorhanden"
                } else {
                    ""
                }
        } else {
            recentStatus = result.message

            if (result.keyRevoked) {
                reminderStore
                    .setDeveloperModeEnabled(
                        false
                    )
                refreshReminderSettings()

                DebugLogger.log(
                    appContext,
                    "DEVELOPER_MODE_DISABLED",
                    "reason=key_revoked_recent"
                )
            }
        }
    }

    suspend fun scheduleAccordingToSettings(
        reason: String,
        resetFromNow: Boolean
    ) {
        val settings =
            reminderStore.settingsFlow.first()
        val now =
            System.currentTimeMillis()

        when {
            !settings.enabled -> {
                ReminderEngine.cancelAll(
                    appContext,
                    reason
                )
                reminderStore
                    .setNextReminderEpochMs(0L)
            }

            settings.pauseUntilEpochMs > now -> {
                ReminderEngine.scheduleAt(
                    appContext,
                    reminderStore,
                    settings,
                    settings.pauseUntilEpochMs,
                    reason
                )
            }

            resetFromNow -> {
                ReminderEngine
                    .scheduleAfterInterval(
                        appContext,
                        reminderStore,
                        settings,
                        reason
                    )
            }

            settings.nextReminderEpochMs > now -> {
                ReminderEngine.scheduleAt(
                    appContext,
                    reminderStore,
                    settings,
                    settings.nextReminderEpochMs,
                    reason
                )
            }

            else -> {
                ReminderEngine
                    .scheduleAfterInterval(
                        appContext,
                        reminderStore,
                        settings,
                        reason
                    )
            }
        }

        refreshReminderSettings()
    }

    fun submitRating(
        rating: Int,
        comment: String?
    ) {
        if (isSending) return

        lastRating = rating

        if (keyInput.isBlank()) {
            transmitStatus =
                "Kein Teilnehmerschlüssel gespeichert"
            return
        }

        isSending = true
        transmitStatus =
            "Ermittle Standort ..."

        scope.launch {
            DebugLogger.log(
                appContext,
                "RATING_SELECTED",
                "rating=$rating"
            )

            ReminderResponseTimeoutScheduler.cancel(
                appContext
            )

            scheduleAccordingToSettings(
                reason = "rating_$rating",
                resetFromNow = true
            )

            val location =
                if (
                    locationProvider
                        .hasAnyLocationPermission()
                ) {
                    locationProvider
                        .getObservationLocation()
                } else {
                    ObservationLocation.unavailable()
                }

            locationStatus =
                if (location.available) {
                    if (reminderSettings.developerModeEnabled && location.accuracyMeters != null) {
                        "Standort: verfügbar (±${location.accuracyMeters.toInt()} m)"
                    } else {
                        "Standort: verfügbar"
                    }
                } else {
                    "Standort: nicht verfügbar · sende 0.0 / 0.0"
                }

            DebugLogger.log(
                appContext,
                "LOCATION_STATUS",
                if (location.available) {
                    "available=true accuracy_m=" +
                        "${location.accuracyMeters ?: -1}"
                } else {
                    "available=false"
                }
            )

            val eventId =
                UUID.randomUUID().toString()

            val now =
                OffsetDateTime.now().format(
                    DateTimeFormatter
                        .ISO_OFFSET_DATE_TIME
                )

            val payload =
                ObservationPayload(
                    key = keyInput.trim(),
                    eventId = eventId,
                    scheduledTime = now,
                    responseTime = now,
                    rating = rating,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    appVersion = BuildConfig.VERSION_NAME,
                    comment =
                        comment
                            ?.trim()
                            ?.takeIf {
                                it.isNotEmpty()
                            }
                            ?.take(2000)
                )

            LocalHalfHourObservations.record(appContext, payload)

            recentItems =
                (
                    listOf(
                        RecentObservation(
                            rating = rating,
                            responseTime = now,
                            scheduledTime = now
                        )
                    ) +
                        recentItems
                    )
                    .distinctBy {
                        item ->
                        item.displayTime
                    }
                    .take(5)

            transmitStatus =
                "Sende Bewertung $rating ..."

            val result =
                supabaseClient
                    .submitObservation(payload)

            isSending = false

            transmitStatus =
                if (result.success) {
                    buildString {
                        append(result.message)
                        result.observerId?.let {
                            append(" · $it")
                        }
                    }
                } else {
                    result.message
                }

            DebugLogger.log(
                appContext,
                if (result.success) {
                    "RATING_SUBMIT_OK"
                } else {
                    "RATING_SUBMIT_FAIL"
                },
                "rating=$rating retryable=${result.retryable} message=${result.message}"
            )

            if (result.keyRevoked) {
                reminderStore
                    .setDeveloperModeEnabled(
                        false
                    )
                refreshReminderSettings()

                DebugLogger.log(
                    appContext,
                    "DEVELOPER_MODE_DISABLED",
                    "reason=key_revoked"
                )
            }

            if (result.success) {
                result.observerId
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        resolvedObserverId ->
                        observerId =
                            resolvedObserverId
                        keyStore.saveObserverId(
                            resolvedObserverId
                        )
                    }

                pendingRating = -1
                pendingComment = ""
                pendingCommentTouched = false

                OfflineObservationQueue.remove(
                    appContext,
                    payload.eventId
                )
                refreshRecent(keyInput)

                val latestSettings =
                    reminderStore.settingsFlow.first()

                if (latestSettings.radiusAnalysisEnabled) {
                    radiusHistogramStatus =
                        "Radius-Auswertung wird geladen ..."

                    val radiusResult =
                        supabaseClient
                            .getRadiusHistograms(
                                key =
                                    keyInput.trim(),
                                latitude =
                                    if (location.available) {
                                        location.latitude
                                    } else {
                                        null
                                    },
                                longitude =
                                    if (location.available) {
                                        location.longitude
                                    } else {
                                        null
                                    },
                                radiusKm =
                                    latestSettings
                                        .radiusAnalysisKm
                            )

                    if (radiusResult.success) {
                        radiusHistogramResult =
                            radiusResult
                        radiusHistogramStatus = ""

                        DebugLogger.log(
                            appContext,
                            "RADIUS_ANALYSIS_OK",
                            "radius_km=${radiusResult.radiusKm} " +
                                "center=${radiusResult.centerSource} " +
                                "observers=${radiusResult.observerCount} " +
                                "observations=${radiusResult.observationCount}"
                        )
                    } else {
                        radiusHistogramResult = null
                        radiusHistogramStatus =
                            radiusResult.message

                        if (
                            radiusResult
                                .keyRevoked
                        ) {
                            reminderStore
                                .setDeveloperModeEnabled(
                                    false
                                )
                            refreshReminderSettings()

                            DebugLogger.log(
                                appContext,
                                "DEVELOPER_MODE_DISABLED",
                                "reason=key_revoked_radius"
                            )
                        }

                        DebugLogger.log(
                            appContext,
                            "RADIUS_ANALYSIS_FAIL",
                            "message=${radiusResult.message}"
                        )
                    }
                } else {
                    radiusHistogramResult = null
                    radiusHistogramStatus = ""
                }

                if (latestSettings.autoBackgroundAfterAction) {
                    DebugLogger.log(
                        appContext,
                        "APP_AUTO_BACKGROUND",
                        "reason=RATING"
                    )
                    onBackgroundApp()
                }
            } else if (result.retryable) {
                pendingRating = -1
                pendingComment = ""
                pendingCommentTouched = false

                val pendingCount =
                    OfflineObservationQueue.enqueue(
                        appContext,
                        payload
                    )

                transmitStatus =
                    "${result.message} · lokal gepuffert"

                DebugLogger.log(
                    appContext,
                    "OFFLINE_QUEUE_ENQUEUED",
                    "event_id=${payload.eventId} rating=$rating pending=$pendingCount"
                )
            } else {
                DataTransferFailureNotifier
                    .showPermanentFailure(
                        appContext,
                        result.message
                    )
            }
        }
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .RequestMultiplePermissions()
        ) { permissions ->
            val fine =
                permissions[
                    Manifest.permission
                        .ACCESS_FINE_LOCATION
                ] == true

            val coarse =
                permissions[
                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                ] == true

            locationStatus =
                when {
                    fine ->
                        "Standort: genaue Berechtigung erteilt"
                    coarse ->
                        "Standort: ungefähre Berechtigung erteilt"
                    else ->
                        "Standort: nicht freigegeben"
                }

            val rating = pendingRating
            pendingRating = -1

            if (rating in 0..5) {
                submitRating(
                    rating,
                    pendingComment
                )
            }
        }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) { granted ->
            DebugLogger.log(
                appContext,
                "NOTIFICATION_PERMISSION_RESULT",
                "granted=$granted"
            )
        }

    val soundPickerLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .StartActivityForResult()
        ) { result ->
            val data = result.data

            @Suppress("DEPRECATION")
            val pickedUri =
                data?.getParcelableExtra<Uri>(
                    RingtoneManager
                        .EXTRA_RINGTONE_PICKED_URI
                )

            if (pickedUri != null) {
                val title =
                    runCatching {
                        RingtoneManager
                            .getRingtone(
                                appContext,
                                pickedUri
                            )
                            ?.getTitle(appContext)
                    }.getOrNull()
                        ?: "Erinnerungston"

                scope.launch {
                    soundStore.save(
                        pickedUri,
                        title
                    )

                    DebugLogger.log(
                        appContext,
                        "REMINDER_SOUND_CHANGED",
                        "title=$title"
                    )

                    refreshReminderSettings()
                }
            }
        }


    val csvExportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .CreateDocument("text/csv")
        ) { uri ->
            if (uri == null) {
                exportStatus =
                    "CSV-Export abgebrochen"
                return@rememberLauncherForActivityResult
            }

            runCatching {
                appContext
                    .contentResolver
                    .openOutputStream(uri)
                    ?.use { output ->
                        ObservationExportWriter
                            .writeCsv(
                                output,
                                exportRows
                            )
                    }
                    ?: error(
                        "Datei konnte nicht geöffnet werden"
                    )
            }.onSuccess {
                exportStatus =
                    "CSV gespeichert · ${exportRows.size} Beobachtungen"

                DebugLogger.log(
                    appContext,
                    "OBSERVATION_EXPORT_OK",
                    "format=csv count=${exportRows.size}"
                )
            }.onFailure { error ->
                exportStatus =
                    "CSV-Export fehlgeschlagen: " +
                        (
                            error.message
                                ?: error.javaClass.simpleName
                            )

                DebugLogger.log(
                    appContext,
                    "OBSERVATION_EXPORT_FAIL",
                    "format=csv message=${error.message ?: error.javaClass.simpleName}"
                )
            }
        }

    val xlsxExportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .CreateDocument(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
        ) { uri ->
            if (uri == null) {
                exportStatus =
                    "XLSX-Export abgebrochen"
                return@rememberLauncherForActivityResult
            }

            runCatching {
                appContext
                    .contentResolver
                    .openOutputStream(uri)
                    ?.use { output ->
                        ObservationExportWriter
                            .writeXlsx(
                                output,
                                exportRows
                            )
                    }
                    ?: error(
                        "Datei konnte nicht geöffnet werden"
                    )
            }.onSuccess {
                exportStatus =
                    "XLSX gespeichert · ${exportRows.size} Beobachtungen"

                DebugLogger.log(
                    appContext,
                    "OBSERVATION_EXPORT_OK",
                    "format=xlsx count=${exportRows.size}"
                )
            }.onFailure { error ->
                exportStatus =
                    "XLSX-Export fehlgeschlagen: " +
                        (
                            error.message
                                ?: error.javaClass.simpleName
                            )

                DebugLogger.log(
                    appContext,
                    "OBSERVATION_EXPORT_FAIL",
                    "format=xlsx message=${error.message ?: error.javaClass.simpleName}"
                )
            }
        }

    fun startObservationExport(
        format: String
    ) {
        if (
            isExporting ||
            keyInput.isBlank()
        ) {
            if (keyInput.isBlank()) {
                exportStatus =
                    "Kein Teilnehmerschlüssel gespeichert"
            }
            return
        }

        isExporting = true
        exportStatus =
            "Alle eigenen Beobachtungen werden abgerufen ..."

        scope.launch {
            val result =
                supabaseClient
                    .getAllObservations(
                        keyInput.trim()
                    )

            isExporting = false

            if (!result.success) {
                exportStatus =
                    result.message

                if (result.keyRevoked) {
                    reminderStore
                        .setDeveloperModeEnabled(
                            false
                        )
                    refreshReminderSettings()

                    DebugLogger.log(
                        appContext,
                        "DEVELOPER_MODE_DISABLED",
                        "reason=key_revoked_export"
                    )
                }

                DebugLogger.log(
                    appContext,
                    "OBSERVATION_EXPORT_FETCH_FAIL",
                    "message=${result.message}"
                )
                return@launch
            }

            exportRows =
                result.items
            exportStatus =
                "${result.items.size} Beobachtungen abgerufen"

            val observerPart =
                result.observerId
                    .ifBlank {
                        "Beobachter"
                    }
                    .replace(
                        Regex(
                            "[^A-Za-z0-9_-]"
                        ),
                        "_"
                    )

            val date =
                LocalDate.now()
                    .toString()

            val baseName =
                "LFN-Scout_" +
                    observerPart +
                    "_" +
                    date

            when (
                format.lowercase()
            ) {
                "csv" ->
                    csvExportLauncher.launch(
                        "$baseName.csv"
                    )

                "xlsx" ->
                    xlsxExportLauncher.launch(
                        "$baseName.xlsx"
                    )
            }
        }
    }

    fun openSoundPicker() {
        val currentUri =
            reminderSound.uri
                .takeIf { it.isNotBlank() }
                ?.let(Uri::parse)

        val intent =
            Intent(
                RingtoneManager
                    .ACTION_RINGTONE_PICKER
            ).apply {
                putExtra(
                    RingtoneManager
                        .EXTRA_RINGTONE_TYPE,
                    RingtoneManager.TYPE_ALARM or
                        RingtoneManager.TYPE_NOTIFICATION or
                        RingtoneManager.TYPE_RINGTONE
                )
                putExtra(
                    RingtoneManager
                        .EXTRA_RINGTONE_SHOW_SILENT,
                    false
                )
                putExtra(
                    RingtoneManager
                        .EXTRA_RINGTONE_SHOW_DEFAULT,
                    true
                )
                putExtra(
                    RingtoneManager
                        .EXTRA_RINGTONE_EXISTING_URI,
                    currentUri
                )
                putExtra(
                    RingtoneManager
                        .EXTRA_RINGTONE_TITLE,
                    "Erinnerungston auswählen"
                )
            }

        soundPickerLauncher.launch(intent)
    }

    fun requestExactAlarmAccess() {
        if (Build.VERSION.SDK_INT >= 31) {
            val intent =
                Intent(
                    Settings
                        .ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse(
                        "package:${appContext.packageName}"
                    )
                )
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
            appContext.startActivity(intent)
        }
    }

    fun requestFullScreenAccess() {
        if (Build.VERSION.SDK_INT >= 34) {
            val intent =
                Intent(
                    Settings
                        .ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                    Uri.parse(
                        "package:${appContext.packageName}"
                    )
                )
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
            appContext.startActivity(intent)
        }
    }

    fun requestDndAccess() {
        val intent =
            Intent(
                Settings
                    .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            ).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }

        appContext.startActivity(intent)
    }

    fun pauseReminders(hours: Int) {
        scope.launch {
            ReminderResponseTimeoutScheduler.cancel(appContext)
            val until = reminderStore.pauseHours(hours)
            val settings = reminderStore.settingsFlow.first()
            ReminderEngine.scheduleAt(
                appContext,
                reminderStore,
                settings,
                until,
                "pause_${hours}h"
            )
            refreshReminderSettings()

            val latestSettings = reminderStore.settingsFlow.first()
            if (latestSettings.autoBackgroundAfterAction) {
                DebugLogger.log(
                    appContext,
                    "APP_AUTO_BACKGROUND",
                    "reason=PAUSE_${hours}H"
                )
                onBackgroundApp()
            }
        }
    }

    fun disableReminders() {
        scope.launch {
            ReminderResponseTimeoutScheduler.cancel(appContext)
            reminderStore.disableCompletely()
            ReminderEngine.cancelAll(
                appContext,
                "disabled_by_user"
            )
            refreshReminderSettings()

            val latestSettings = reminderStore.settingsFlow.first()
            if (latestSettings.autoBackgroundAfterAction) {
                DebugLogger.log(
                    appContext,
                    "APP_AUTO_BACKGROUND",
                    "reason=REMINDER_DISABLED"
                )
                onBackgroundApp()
            }
        }
    }

    fun enableReminders() {
        scope.launch {
            reminderStore.enableNow()
            scheduleAccordingToSettings(
                "enabled_by_user",
                true
            )

            if (Build.VERSION.SDK_INT >= 33) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    LaunchedEffect(reminderSettings.rollingBufferEnabled) {
        while (reminderSettings.rollingBufferEnabled) {
            rollingBufferedSeconds = RollingAudioEngine.bufferedSeconds()
            delay(1_000)
        }
        rollingBufferedSeconds = 0
    }

    LaunchedEffect(Unit) {
        DebugLogger.log(
            appContext,
            "APP_START",
            "version=${BuildConfig.VERSION_NAME}"
        )

        DebugLogger.log(
            appContext,
            "AUDIO_STORAGE_INIT",
            "path=${AudioArchive.displayDirectoryPath(appContext)}"
        )

        AudioArchive.deleteLegacyWavsOnce(
            appContext
        )

        keyInput =
            keyStore.keyFlow.first()

        observerId =
            keyStore.observerIdFlow.first()

        refreshRecent(keyInput)
        // Restore the original rolling preference after an abnormal 48-kHz process death.
        AnalyzerModeCoordinator.recoverAfterProcessDeath(appContext)
        refreshReminderSettings()
        if (reminderSettings.rollingBufferEnabled &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        ) {
            RollingAudioService.requestStart(appContext)
        }
        refreshAudioArchive()

        DebugLogger.log(
            appContext,
            "AUDIO_ARCHIVE_SCAN",
            "entries=${audioArchiveEntries.size}"
        )

        scheduleAccordingToSettings(
            reason = "app_start",
            resetFromNow = false
        )
    }

    val visibleWaterfall =
        waterfallResult

    if (
        visibleWaterfall != null
    ) {
        var waterfallZoom by remember(
            visibleWaterfall.imageFileName
        ) {
            mutableStateOf(1f)
        }
        var waterfallOffset by remember(
            visibleWaterfall.imageFileName
        ) {
            mutableStateOf(Offset.Zero)
        }
        var waterfallOverwriteNames by remember(
            visibleWaterfall.imageFileName
        ) {
            mutableStateOf<List<String>?>(null)
        }

        fun saveWaterfallImage(overwriteExisting: Boolean) {
            scope.launch {
                try {
                    val outputFormat = reminderSettings.waterfallOutputFormat
                    DebugLogger.log(
                        appContext,
                        "WATERFALL_IMAGE_SAVE_START",
                        "source=${visibleWaterfall.sourceFileName} format=${outputFormat.name} overwrite=$overwriteExisting"
                    )
                    val paths = withContext(Dispatchers.IO) {
                        WaterfallGenerator.saveImages(
                            appContext,
                            visibleWaterfall,
                            outputFormat,
                            overwriteExisting = overwriteExisting
                        )
                    }
                    waterfallStatus = if (paths.size == 1) {
                        "Bild gespeichert: ${paths.first()}"
                    } else {
                        "PNG + JPG gespeichert"
                    }
                    DebugLogger.log(
                        appContext,
                        "WATERFALL_IMAGE_SAVE_OK",
                        "files=${paths.size} format=${outputFormat.name} overwrite=$overwriteExisting"
                    )
                } catch (error: Exception) {
                    waterfallStatus =
                        "Bild-Speicherfehler: " + (error.message ?: error.javaClass.simpleName)
                    DebugLogger.log(
                        appContext,
                        "WATERFALL_IMAGE_SAVE_FAILED",
                        "error=${error.javaClass.simpleName} message=${error.message ?: "-"}"
                    )
                }
            }
        }

        val waterfallImageBaseName =
            visibleWaterfall.imageFileName
                .substringBeforeLast(
                    ".",
                    visibleWaterfall.imageFileName
                )

        val selectedOutputNames =
            when (
                reminderSettings.waterfallOutputFormat
            ) {
                WaterfallOutputFormat.PNG ->
                    listOf(
                        "$waterfallImageBaseName.png"
                    )

                WaterfallOutputFormat.JPG ->
                    listOf(
                        "$waterfallImageBaseName.jpg"
                    )

                WaterfallOutputFormat.PNG_AND_JPG ->
                    listOf(
                        "$waterfallImageBaseName.png",
                        "$waterfallImageBaseName.jpg"
                    )
            }

        AlertDialog(
            onDismissRequest = {
                waterfallResult =
                    null
            },
            title = {
                Text(
                    "Wasserfallspektrum"
                )
            },
            text = {
                Column {
                    Image(
                        bitmap =
                            visibleWaterfall
                                .bitmap
                                .asImageBitmap(),
                        contentDescription =
                            "Fallendes Wasserfallspektrum 0 bis 140 Hz",
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    520.dp
                                )
                                .graphicsLayer {
                                    scaleX = waterfallZoom
                                    scaleY = waterfallZoom
                                    translationX =
                                        waterfallOffset.x
                                    translationY =
                                        waterfallOffset.y
                                    clip = true
                                }
                                .pointerInput(
                                    visibleWaterfall.imageFileName
                                ) {
                                    detectTransformGestures {
                                            _,
                                            pan,
                                            gestureZoom,
                                            _ ->
                                        val newZoom =
                                            (
                                                waterfallZoom *
                                                    gestureZoom
                                                ).coerceIn(
                                                1f,
                                                5f
                                            )

                                        waterfallZoom =
                                            newZoom

                                        waterfallOffset =
                                            if (newZoom <= 1f) {
                                                Offset.Zero
                                            } else {
                                                waterfallOffset +
                                                    pan
                                            }
                                    }
                                }
                                .pointerInput(
                                    visibleWaterfall.imageFileName,
                                    "double_tap_reset"
                                ) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            waterfallZoom = 1f
                                            waterfallOffset =
                                                Offset.Zero
                                        }
                                    )
                                },
                        contentScale =
                            ContentScale.Fit
                    )

                    Text(
                        "0–140 Hz · Inferno · Pegel automatisch " +
                            "%.1f … %.1f dBFS · L50/L90 %.0f–%.0f Hz: %.1f / %.1f dBFS".format(
                                visibleWaterfall
                                    .minDb,
                                visibleWaterfall
                                    .maxDb,
                                visibleWaterfall
                                    .statsMinHz,
                                visibleWaterfall
                                    .statsMaxHz,
                                visibleWaterfall
                                    .l50Db,
                                visibleWaterfall
                                    .l90Db
                            ),
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )

                    if (
                        !visibleWaterfall
                            .comment
                            .isNullOrBlank()
                    ) {
                        Text(
                            "Kommentar: " +
                                visibleWaterfall
                                    .comment,
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall
                        )
                    }

                    Text(
                        "Bildformat: " +
                            reminderSettings
                                .waterfallOutputFormat
                                .displayName,
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )

                    Text(
                        "Bilddatei: " +
                            selectedOutputNames
                                .joinToString(
                                    " · "
                                ),
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )

                    Text(
                        "Zoom: Zwei Finger · Verschieben bei Zoom · Doppeltipp = Gesamtansicht",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val outputFormat = reminderSettings.waterfallOutputFormat
                            val existing = withContext(Dispatchers.IO) {
                                WaterfallGenerator.existingOutputNames(
                                    appContext,
                                    visibleWaterfall,
                                    outputFormat
                                )
                            }
                            if (existing.isEmpty()) {
                                saveWaterfallImage(overwriteExisting = false)
                            } else {
                                waterfallOverwriteNames = existing
                            }
                        }
                    }
                ) {
                    Text(
                        when (
                            reminderSettings
                                .waterfallOutputFormat
                        ) {
                            WaterfallOutputFormat.PNG ->
                                "PNG speichern"

                            WaterfallOutputFormat.JPG ->
                                "JPG speichern"

                            WaterfallOutputFormat.PNG_AND_JPG ->
                                "PNG + JPG speichern"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        waterfallResult =
                            null
                    }
                ) {
                    Text(
                        "Schließen"
                    )
                }
            }
        )

        val existingWaterfallImages = waterfallOverwriteNames
        if (existingWaterfallImages != null) {
            AlertDialog(
                onDismissRequest = { waterfallOverwriteNames = null },
                title = { Text("Bilddatei vorhanden") },
                text = {
                    Text(
                        "Folgende Bilddatei ist bereits vorhanden: " +
                            existingWaterfallImages.joinToString(" · ") +
                            "\n\nÜberschreiben?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            waterfallOverwriteNames = null
                            saveWaterfallImage(overwriteExisting = true)
                        }
                    ) { Text("Überschreiben") }
                },
                dismissButton = {
                    TextButton(onClick = { waterfallOverwriteNames = null }) {
                        Text("Abbrechen")
                    }
                }
            )
        }
    }

    val rollingCommentEntry = pendingRollingCommentEntry
    if (rollingCommentEntry != null) {
        AlertDialog(
            onDismissRequest = {
                scope.launch {
                    AudioArchive.updateRollingComment(appContext, rollingCommentEntry.recordingId, rollingComment)
                    pendingRollingCommentEntry = null
                    rollingComment = ""
                    refreshAudioArchive()
                }
            },
            title = { Text("Kommentar zur Rolling-Aufnahme") },
            text = {
                Column {
                    Text("Optionaler Kommentar; wird in der gleichnamigen JSON-Datei gespeichert.")
                    OutlinedTextField(
                        value = rollingComment,
                        onValueChange = { rollingComment = it.take(AudioArchive.MAX_COMMENT_LENGTH) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Kommentar") }
                    )
                    Text("${rollingComment.length} / ${AudioArchive.MAX_COMMENT_LENGTH}", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        AudioArchive.updateRollingComment(appContext, rollingCommentEntry.recordingId, rollingComment)
                        pendingRollingCommentEntry = null
                        rollingComment = ""
                        refreshAudioArchive()
                    }
                }) { Text("Speichern") }
            },
            dismissButton = {
                TextButton(onClick = {
                    scope.launch {
                        AudioArchive.updateRollingComment(appContext, rollingCommentEntry.recordingId, null)
                        pendingRollingCommentEntry = null
                        rollingComment = ""
                        refreshAudioArchive()
                    }
                }) { Text("Ohne Kommentar") }
            }
        )
    }

    val deleteRolling = rollingDeleteCandidate
    if (deleteRolling != null) {
        AlertDialog(
            onDismissRequest = { rollingDeleteCandidate = null },
            title = { Text("Rolling-Aufnahme löschen?") },
            text = { Text("WAV und gleichnamige JSON-Datei werden dauerhaft gelöscht.") },
            confirmButton = {
                Button(onClick = {
                    val ok = AudioArchive.deleteRolling(appContext, deleteRolling)
                    rollingDeleteCandidate = null
                    rollingStatus = if (ok) "Rolling-Aufnahme gelöscht" else "Rolling-Aufnahme konnte nicht vollständig gelöscht werden"
                    refreshAudioArchive()
                }) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { rollingDeleteCandidate = null }) { Text("Abbrechen") }
            }
        )
    }

    when {
        showDebugLog -> {
            DebugLogScreen(
                lines = debugLines,
                onReload = {
                    debugLines =
                        DebugLogger
                            .readLines(appContext)
                },
                onClear = {
                    DebugLogger.clear(appContext)
                    DebugLogger.log(
                        appContext,
                        "LOG_CLEARED"
                    )
                    debugLines =
                        DebugLogger
                            .readLines(appContext)
                },
                onSave = {
                    scope.launch {
                        logSaveStatus = runCatching {
                            withContext(Dispatchers.IO) {
                                DebugLogger.saveCopy(appContext, reminderSettings.developerModeEnabled)
                            }
                        }.fold(
                            onSuccess = { "Logfile gespeichert: $it" },
                            onFailure = { "Logfile-Speicherfehler: ${it.message ?: it.javaClass.simpleName}" }
                        )
                    }
                },
                saveStatus = logSaveStatus,
                onBack = {
                    showDebugLog = false
                }
            )
        }

        currentPanel == AppPanel.SETTINGS -> {
            val exactAllowed =
                ExactAlarmScheduler
                    .canScheduleExact(appContext)

            val fullScreenAllowed =
                if (Build.VERSION.SDK_INT >= 34) {
                    (
                        appContext
                            .getSystemService(
                                Context
                                    .NOTIFICATION_SERVICE
                            ) as NotificationManager
                        ).canUseFullScreenIntent()
                } else {
                    true
                }

            val dndAllowed =
                (
                    appContext.getSystemService(
                        Context.NOTIFICATION_SERVICE
                    ) as NotificationManager
                    ).isNotificationPolicyAccessGranted

            ReminderSettingsScreen(
                keyInput = keyInput,
                onKeyInputChange = {
                    keyInput = it.uppercase()
                    keyStatus = ""
                },
                onSaveKey = {
                    scope.launch {
                        keyStore.saveKey(keyInput)
                        observerId = ""
                        keyStore.saveObserverId("")
                        keyStatus = if (keyInput.isBlank()) {
                            "Leerer Schlüssel gespeichert."
                        } else {
                            "Schlüssel gespeichert."
                        }
                        refreshRecent(keyInput)
                    }
                },
                keyStatus = keyStatus,
                settings = reminderSettings,
                sound = reminderSound,
                intervalInput = intervalInput,
                exactAllowed = exactAllowed,
                fullScreenAllowed =
                    fullScreenAllowed,
                dndAllowed = dndAllowed,
onAudioSourceModeChange = { mode ->
    scope.launch {
        reminderStore.setAudioSourceMode(mode)
        refreshReminderSettings()
    }
},
onAudioInputDeviceChange = { deviceId ->
    scope.launch {
        reminderStore.setAudioInputDeviceId(deviceId)
        refreshReminderSettings()
    }
},
onWaterfallOutputFormatChange = { format ->
    scope.launch {
        reminderStore
            .setWaterfallOutputFormat(format)
        DebugLogger.log(
            appContext,
            "WATERFALL_OUTPUT_FORMAT_CHANGED",
            "format=${format.name}"
        )
        refreshReminderSettings()
    }
},
onWaterfallStatsRangeChange = { minHz, maxHz ->
    scope.launch {
        reminderStore.setWaterfallStatsRange(minHz, maxHz)
        DebugLogger.log(
            appContext,
            "WATERFALL_STATS_RANGE_CHANGED",
            "min_hz=$minHz max_hz=$maxHz"
        )
        refreshReminderSettings()
    }
},
onNearPlotGainChange = { value ->
    scope.launch {
        reminderStore.setNearPlotGain(value)
        DebugLogger.log(appContext, "SEISMIC_NEAR_GAIN_CHANGED", "gain=${SeismicPlotUiPolicy.normalizeNearPlotGain(value)}")
        refreshReminderSettings()
    }
},
onWaterfallLevelSettingsChange = { auto, minDb, maxDb ->
    scope.launch {
        reminderStore.setWaterfallLevelSettings(auto, minDb, maxDb)
        DebugLogger.log(appContext, "WATERFALL_LEVEL_SETTINGS_CHANGED", "auto=$auto min50=$minDb max50=$maxDb")
        refreshReminderSettings()
    }
},
onCommentTimeoutsChange = { ratingStart, ratingEdit, audioStart, audioEdit ->
    scope.launch {
        reminderStore.setCommentTimeouts(ratingStart, ratingEdit, audioStart, audioEdit)
        DebugLogger.log(appContext, "COMMENT_TIMEOUTS_CHANGED", "rating=${ratingStart}/${ratingEdit}s audio=${audioStart}/${audioEdit}s")
        refreshReminderSettings()
    }
},
onRollingMaxOperationHoursChange = { hours ->
    scope.launch {
        reminderStore.setRollingMaxOperationHours(hours)
        refreshReminderSettings()
    }
},
onRollingBufferEnabledChange = { enabled ->
    scope.launch {
        reminderStore.setRollingBufferEnabled(enabled)
        refreshReminderSettings()
        if (enabled) {
            if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                RollingAudioService.requestStart(appContext)
            } else {
                audioArchiveStatus = "Mikrofonberechtigung für Rolling-Buffer fehlt"
            }
        } else {
            RollingAudioService.requestStop(appContext)
        }
        DebugLogger.log(appContext, "ROLLING_SETTING_CHANGED", "enabled=$enabled")
    }
},
                onAlarmVolumeChange = { percent ->
                    scope.launch {
                        reminderStore
                            .setAlarmVolumePercent(
                                percent
                            )

                        AlarmVolumeController
                            .applyPercent(
                                appContext,
                                percent
                            )

                        DebugLogger.log(
                            appContext,
                            "ALARM_VOLUME_CHANGED",
                            "percent=$percent"
                        )

                        refreshReminderSettings()
                    }
                },
                onDeveloperModeChange = { enabled ->
                    scope.launch {
                        reminderStore
                            .setDeveloperModeEnabled(
                                enabled
                            )

                        DebugLogger.log(
                            appContext,
                            "DEVELOPER_MODE_CHANGED",
                            "enabled=$enabled"
                        )

                        refreshReminderSettings()
                    }
                },
                onMaxAudioRecordingsPerDayChange = { value ->
                    scope.launch {
                        reminderStore
                            .setMaxAudioRecordingsPerDay(
                                value
                            )

                        DebugLogger.log(
                            appContext,
                            "AUDIO_DAILY_LIMIT_CHANGED",
                            "limit=" +
                                AudioRecordingPolicy
                                    .normalizeDailyLimit(
                                        value
                                    )
                        )

                        refreshReminderSettings()
                    }
                },
                onAutoBackgroundChange = { enabled ->
                    scope.launch {
                        reminderStore
                            .setAutoBackgroundAfterAction(enabled)

                        DebugLogger.log(
                            appContext,
                            "AUTO_BACKGROUND_SETTING_CHANGED",
                            "enabled=$enabled"
                        )

                        refreshReminderSettings()
                    }
                },
                radiusKmInput = radiusKmInput,
                onRadiusAnalysisEnabledChange = { enabled ->
                    scope.launch {
                        reminderStore
                            .setRadiusAnalysisEnabled(enabled)

                        DebugLogger.log(
                            appContext,
                            "RADIUS_ANALYSIS_SETTING_CHANGED",
                            "enabled=$enabled"
                        )

                        refreshReminderSettings()
                    }
                },
                onRadiusKmInputChange = {
                    radiusKmInput =
                        it.filter(Char::isDigit)
                },
                onSaveRadiusKm = {
                    scope.launch {
                        val requested =
                            radiusKmInput
                                .toIntOrNull()
                                ?: RadiusAnalysisPolicy
                                    .DEFAULT_RADIUS_KM

                        reminderStore
                            .setRadiusAnalysisKm(
                                requested
                            )

                        DebugLogger.log(
                            appContext,
                            "RADIUS_ANALYSIS_RADIUS_CHANGED",
                            "radius_km=" +
                                RadiusAnalysisPolicy
                                    .normalizeRadiusKm(
                                        requested
                                    )
                        )

                        refreshReminderSettings()
                    }
                },
                onIntervalInputChange = {
                    intervalInput =
                        it.filter(Char::isDigit)
                },
                onSaveInterval = {
                    scope.launch {
                        val requested =
                            intervalInput
                                .toIntOrNull()
                                ?: ReminderPolicy
                                    .DEFAULT_INTERVAL_MINUTES

                        reminderStore
                            .setIntervalMinutes(
                                requested
                            )

                        scheduleAccordingToSettings(
                            "interval_changed",
                            true
                        )
                    }
                },
                onModeChange = { mode ->
                    scope.launch {
                        reminderStore.setMode(mode)

                        DebugLogger.log(
                            appContext,
                            "REMINDER_MODE_CHANGED",
                            "mode=${mode.name}"
                        )

                        scheduleAccordingToSettings(
                            "mode_changed",
                            true
                        )
                    }
                },
                onChooseSound = {
                    ReminderAlertController
                        .stopSoundSelfTest(
                            appContext,
                            "sound_picker_opened"
                        )
                    soundSelfTestStatus = ""
                    openSoundPicker()
                },
                soundSelfTestStatus =
                    soundSelfTestStatus,
                onStartSoundSelfTest = {
                    scope.launch {
                        val result =
                            ReminderAlertController
                                .startSoundSelfTest(
                                    appContext
                                )
                        soundSelfTestStatus =
                            result.displayText
                    }
                },
                onStopSoundSelfTest = {
                    ReminderAlertController
                        .stopSoundSelfTest(
                            appContext,
                            "user_button"
                        )
                    soundSelfTestStatus =
                        SoundSelfTestResult
                            .STOPPED
                            .displayText
                },
                onTestNotification = {
                    ReminderAlertController
                        .stopSoundSelfTest(
                            appContext,
                            "notification_test_started"
                        )
                    soundSelfTestStatus = ""
                    scope.launch {
                        ReminderNotifier.show(
                            appContext,
                            requestFullScreen =
                                reminderSettings.mode ==
                                    ReminderMode.ALARM
                        )

                        if (
                            reminderSettings.mode ==
                                ReminderMode.ALARM
                        ) {
                            val responseWindowMinutes =
                                ReminderPolicy.responseWindowMinutes(
                                    reminderSettings.intervalMinutes
                                )

                            ReminderAlertController.playAlarmTonePackage(
                                appContext,
                                packageIndex = 1,
                                packageCount = 3,
                                offsetMs = 0L,
                                responseWindowMinutes = responseWindowMinutes
                            )

                            ReminderInProcessToneScheduler.scheduleRemaining(
                                appContext,
                                responseWindowMinutes
                            )
                        }
                    }
                },
                onPauseHours = { hours ->
                    scope.launch {
                        ReminderResponseTimeoutScheduler.cancel(
                            appContext
                        )

                        val until =
                            reminderStore
                                .pauseHours(hours)

                        val settings =
                            reminderStore
                                .settingsFlow
                                .first()

                        ReminderEngine.scheduleAt(
                            appContext,
                            reminderStore,
                            settings,
                            until,
                            "pause_${hours}h"
                        )

                        refreshReminderSettings()

                        val latestSettings =
                            reminderStore.settingsFlow.first()

                        if (latestSettings.autoBackgroundAfterAction) {
                            DebugLogger.log(
                                appContext,
                                "APP_AUTO_BACKGROUND",
                                "reason=PAUSE_${hours}H"
                            )
                            onBackgroundApp()
                        }
                    }
                },
                onDisable = {
                    scope.launch {
                        ReminderResponseTimeoutScheduler.cancel(
                            appContext
                        )

                        reminderStore
                            .disableCompletely()

                        ReminderEngine.cancelAll(
                            appContext,
                            "disabled_by_user"
                        )

                        refreshReminderSettings()

                        val latestSettings =
                            reminderStore.settingsFlow.first()

                        if (latestSettings.autoBackgroundAfterAction) {
                            DebugLogger.log(
                                appContext,
                                "APP_AUTO_BACKGROUND",
                                "reason=REMINDER_DISABLED"
                            )
                            onBackgroundApp()
                        }
                    }
                },
                onEnable = {
                    scope.launch {
                        reminderStore.enableNow()

                        scheduleAccordingToSettings(
                            "enabled_by_user",
                            true
                        )

                        if (
                            Build.VERSION.SDK_INT >= 33
                        ) {
                            notificationPermissionLauncher
                                .launch(
                                    Manifest.permission
                                        .POST_NOTIFICATIONS
                                )
                        }
                    }
                },
                onRequestExactAlarmAccess = {
                    requestExactAlarmAccess()
                },
                onRequestFullScreenAccess = {
                    requestFullScreenAccess()
                },
                onRequestDndAccess = {
                    requestDndAccess()
                },
                onOpenDebugLog = {
                    debugLines =
                        DebugLogger
                            .readLines(appContext)
                    showDebugLog = true
                },
                onSwipeLeft = { },
                onSwipeRight = { navigateTo(AppPanel.MORE) },
                onBack = {
                    ReminderAlertController
                        .stopSoundSelfTest(
                            appContext,
                            "settings_closed"
                        )
                    soundSelfTestStatus = ""
                    navigateBack()
                }
            )
        }

        currentPanel == AppPanel.SEISMIC -> {
            SeismicScreen(
                snapshot = seismicSnapshot,
                status = seismicStatus,
                isLoading = isLoadingSeismic,
                selectedDate = seismicSelectedDate,
                onRefresh = { loadSeismic(seismicSelectedDate) },
                onDateSelected = { date -> loadSeismic(date) },
                onPreviousDate = { loadSeismic(seismicSelectedDate.minusDays(1)) },
                onNextDate = {
                    val next = seismicSelectedDate.plusDays(1)
                    if (!next.isAfter(LocalDate.now(ZoneId.systemDefault()))) loadSeismic(next)
                },
                onToday = { loadSeismic(LocalDate.now(ZoneId.systemDefault())) },
                onSwipeLeft = { navigateTo(AppPanel.MORE) },
                onSwipeRight = { navigateTo(AppPanel.MAIN) },
                onBack = { navigateBack() }
            )
        }

        currentPanel == AppPanel.ANALYZER -> {
            LiveAnalyzerScreen(
                dailyCount = reminderSettings.audioRecordingsToday,
                dailyLimit = reminderSettings.maxAudioRecordingsPerDay,
                onLocalAudioSaved = {
                    val accepted = reminderStore.tryMarkAudioRecordingCompleted()
                    refreshReminderSettings()
                    accepted != null
                },
                onAnalyzerAudioArchived = { archived ->
                    val wavBytes = AudioArchive.readWavBytes(appContext, archived)
                    if (wavBytes == null || wavBytes.isEmpty()) {
                        AudioArchive.markFailed(appContext, archived.recordingId, "Lokale WAV-Datei fehlt")
                        false
                    } else {
                        val upload = supabaseClient.uploadWav(
                            key = keyInput.trim(), recordingId = archived.recordingId, wavBytes = wavBytes,
                            recordedAt = archived.recordedAt, gainDb = archived.gainDb,
                            audioSource = archived.audioSource, clippedSamples = archived.clippedSamples,
                            comment = archived.comment, captureOrigin = archived.origin, triggerType = "MANUAL")
                        if (upload.success) {
                            AudioArchive.markUploaded(appContext, archived.recordingId, upload.storagePath)
                            val statsOk = AudioStatisticsCloud.submitExisting(
                                appContext, supabaseClient, keyInput.trim(), archived)
                            DebugLogger.log(appContext, "ANALYZER_AUDIO_STATISTICS",
                                "recording_id=${archived.recordingId} success=$statsOk")
                            AudioArchive.cleanup(appContext)
                            DebugLogger.log(appContext, "ANALYZER_AUDIO_UPLOAD_OK",
                                "recording_id=${archived.recordingId} storage_path=${upload.storagePath ?: "-"}")
                            true
                        } else {
                            AudioArchive.markFailed(appContext, archived.recordingId, upload.message)
                            DebugLogger.log(appContext, "ANALYZER_AUDIO_UPLOAD_FAILED",
                                "recording_id=${archived.recordingId} message=${upload.message}")
                            if (upload.keyRevoked) reminderStore.setDeveloperModeEnabled(false)
                            false
                        }
                    }
                },
                observerId = observerId,
                participantKey = keyInput,
                recentObservations = recentItems,
                audioNoCommentTimeoutSeconds = reminderSettings.audioNoCommentTimeoutSeconds,
                audioCommentTimeoutSeconds = reminderSettings.audioCommentTimeoutSeconds,
                onBack = { navigateBack() },
                rollingEnabled = reminderSettings.rollingBufferEnabled,
                onAudioModeChanged = { scope.launch { refreshReminderSettings() } },
                onEnableRolling = {
                    scope.launch {
                        reminderStore.setRollingBufferEnabled(true)
                        refreshReminderSettings()
                        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            RollingAudioService.requestStart(appContext)
                        } else {
                            audioArchiveStatus = "Mikrofonberechtigung fehlt; in den App-Einstellungen freigeben"
                        }
                    }
                }
            )
        }

        currentPanel == AppPanel.MORE -> {
            MoreFunctionsScreen(
                developerModeEnabled =
                    reminderSettings
                        .developerModeEnabled,
                exportStatus = exportStatus,
                isExporting = isExporting,
                transmitStatus = transmitStatus,
                locationStatus = locationStatus,
                audioDirectoryPath =
                    AudioArchive
                        .displayDirectoryPath(
                            appContext
                        ),
                audioEntries =
                    audioArchiveEntries,
                audioArchiveStatus =
                    audioArchiveStatus,
                waterfallStatus =
                    waterfallStatus,
                isGeneratingWaterfall =
                    isGeneratingWaterfall,
                generatingWaterfallRecordingId =
                    generatingWaterfallRecordingId,
                onGenerateWaterfallForEntry = {
                        selectedEntry ->
                    generateWaterfall(
                        selectedEntry
                    )
                },
                rollingBufferEnabled = reminderSettings.rollingBufferEnabled,
                rollingBufferedSeconds = rollingBufferedSeconds,
                rollingStatus = rollingStatus,
                onOpenAnalyzer = { navigateTo(AppPanel.ANALYZER) },
                onSaveRollingBuffer = { saveRollingBuffer() },
                onDeleteRolling = { entry -> rollingDeleteCandidate = entry },
                onExportCsv = {
                    startObservationExport("csv")
                },
                onExportXlsx = {
                    startObservationExport("xlsx")
                },
                onCopyAudioPath = {
                    val path =
                        AudioArchive
                            .displayDirectoryPath(
                                appContext
                            )

                    val clipboard =
                        appContext
                            .getSystemService(
                                Context.CLIPBOARD_SERVICE
                            ) as ClipboardManager

                    clipboard.setPrimaryClip(
                        ClipData.newPlainText(
                            "Audio-Aufzeichnungsverzeichnis",
                            path
                        )
                    )

                    audioArchiveStatus =
                        "Pfad kopiert"
                },
                onRefreshAudioArchive = {
                    refreshAudioArchive()
                },
                onRetryAudio = {
                        entry ->
                    scope.launch {
                        retryAudioUpload(
                            entry
                        )
                    }
                },
                logSaveStatus = logSaveStatus,
                onSaveLog = {
                    scope.launch {
                        logSaveStatus = runCatching {
                            withContext(Dispatchers.IO) {
                                DebugLogger.saveCopy(
                                    appContext,
                                    reminderSettings.developerModeEnabled
                                )
                            }
                        }.fold(
                            onSuccess = { "Logfile gespeichert: $it" },
                            onFailure = { "Logfile-Speicherfehler: ${it.message ?: it.javaClass.simpleName}" }
                        )
                    }
                },
                onOpenDebugLog = {
                    debugLines =
                        DebugLogger.readLines(appContext)
                    showDebugLog = true
                },
                onSwipeLeft = { navigateTo(AppPanel.SETTINGS) },
                onSwipeRight = { navigateTo(AppPanel.SEISMIC) },
                onBack = {
                    navigateBack()
                }
            )
        }

        else -> {
            LFNScoutScreen(
                lastRating = lastRating,
                transmitStatus = transmitStatus,
                locationStatus = locationStatus,
                reminderStatus =
                    formatReminderStatus(reminderSettings),
                nextReminderStatus =
                    formatNextReminderStatus(reminderSettings),
                reminderMode = reminderSettings.mode,
                audioSettings = reminderSettings,
                keyInput = keyInput,
                observerId = observerId,
                reminderStore = reminderStore,
                supabaseClient = supabaseClient,
                isSending = isSending,
                pendingRating = pendingRating,
                pendingComment = pendingComment,
                pendingCommentTouched =
                    pendingCommentTouched,
                onPendingCommentTouched = {
                    pendingCommentTouched = true
                },
                onPendingCommentChange = {
                    pendingCommentTouched = true
                    pendingComment = it.take(2000)
                },
                onSendPendingRating = {
                    val rating = pendingRating

                    if (rating !in 0..5 || isSending) {
                        return@LFNScoutScreen
                    }

                    if (
                        locationProvider
                            .hasAnyLocationPermission()
                    ) {
                        submitRating(
                            rating,
                            pendingComment
                        )
                    } else {
                        locationStatus =
                            "Standort: Berechtigung wird angefragt"

                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                onAutoSendPendingRating = {
                    val rating = pendingRating

                    if (rating in 0..5 && !isSending) {
                        if (
                            locationProvider
                                .hasAnyLocationPermission()
                        ) {
                            DebugLogger.log(
                                appContext,
                                "COMMENT_AUTO_SEND",
                                "rating=$rating comment_touched=$pendingCommentTouched"
                            )

                            submitRating(
                                rating,
                                pendingComment
                            )
                        } else {
                            locationStatus =
                                "Standort: Berechtigung wird angefragt"

                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                },
                onCancelPendingRating = {
                    pendingRating = -1
                    pendingComment = ""
                    pendingCommentTouched = false
                },
                recentItems = recentItems,
                recentStatus = recentStatus,
                radiusAnalysisEnabled =
                    reminderSettings.radiusAnalysisEnabled,
                radiusHistogramStatus =
                    radiusHistogramStatus,
                radiusHistogramResult =
                    radiusHistogramResult,
                onPauseHours = ::pauseReminders,
                onDisable = ::disableReminders,
                onOpenSettings = {
                    navigateTo(AppPanel.SETTINGS)
                    if (Build.VERSION.SDK_INT >= 33) {
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                },
                onOpenAnalyzer = { navigateTo(AppPanel.ANALYZER) },
                onOpenMore = {
                    refreshAudioArchive()
                    navigateTo(AppPanel.MORE)
                },
                onOpenSeismic = {
                    navigateTo(AppPanel.SEISMIC)
                },
                onSwipeLeft = { navigateTo(AppPanel.SEISMIC) },
                onSwipeRight = { },
                onRating = { rating ->
                    if (isSending) {
                        return@LFNScoutScreen
                    }

                    pendingRating = rating
                    pendingComment = ""
                    pendingCommentTouched = false
                }
            )
        }
    }
}

private const val PANEL_SWIPE_THRESHOLD_DP = 80

private fun Modifier.panelSwipeNavigation(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
): Modifier =
    pointerInput(
        onSwipeLeft,
        onSwipeRight
    ) {
        var totalDragX = 0f

        detectHorizontalDragGestures(
            onDragStart = {
                totalDragX = 0f
            },
            onHorizontalDrag = {
                    _,
                    dragAmount ->
                totalDragX += dragAmount
            },
            onDragCancel = {
                totalDragX = 0f
            },
            onDragEnd = {
                val thresholdPx =
                    PANEL_SWIPE_THRESHOLD_DP.dp.toPx()

                when {
                    totalDragX >= thresholdPx ->
                        onSwipeRight()

                    totalDragX <= -thresholdPx ->
                        onSwipeLeft()
                }

                totalDragX = 0f
            }
        )
    }

private fun formatReminderStatus(
    settings: ReminderSettings,
    nowEpochMs: Long =
        System.currentTimeMillis()
): String {
    if (!settings.enabled) {
        return "Erinnerungen: aus"
    }

    if (
        settings.pauseUntilEpochMs >
        nowEpochMs
    ) {
        val localTime =
            Instant
                .ofEpochMilli(
                    settings.pauseUntilEpochMs
                )
                .atZone(
                    ZoneId.systemDefault()
                )
                .format(
                    DateTimeFormatter
                        .ofPattern(
                            "dd.MM.yyyy HH:mm"
                        )
                )

        return "Erinnerungen: pausiert bis $localTime"
    }

    return "Erinnerungen: aktiv · alle ${settings.intervalMinutes} min"
}

private fun formatNextReminderStatus(
    settings: ReminderSettings
): String {
    if (
        !settings.enabled ||
        settings.nextReminderEpochMs <= 0L
    ) {
        return "Nächste Erinnerung: –"
    }

    return "Nächste Erinnerung: " +
        DebugLogger.formatEpoch(
            settings.nextReminderEpochMs
        )
}

@Composable
private fun LFNScoutScreen(
    lastRating: Int,
    transmitStatus: String,
    locationStatus: String,
    reminderStatus: String,
    nextReminderStatus: String,
    reminderMode: ReminderMode,
    audioSettings: ReminderSettings,
    keyInput: String,
    observerId: String,
    reminderStore: ReminderSettingsStore,
    supabaseClient: SupabaseClient,
    isSending: Boolean,
    pendingRating: Int,
    pendingComment: String,
    pendingCommentTouched: Boolean,
    onPendingCommentTouched: () -> Unit,
    onPendingCommentChange: (String) -> Unit,
    onSendPendingRating: () -> Unit,
    onAutoSendPendingRating: () -> Unit,
    onCancelPendingRating: () -> Unit,
    recentItems: List<RecentObservation>,
    recentStatus: String,
    radiusAnalysisEnabled: Boolean,
    radiusHistogramStatus: String,
    radiusHistogramResult: RadiusHistogramResult?,
    onPauseHours: (Int) -> Unit,
    onDisable: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAnalyzer: () -> Unit,
    onOpenMore: () -> Unit,
    onOpenSeismic: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onRating: (Int) -> Unit
) {
    val latestAutoSendAction =
        rememberUpdatedState(
            onAutoSendPendingRating
        )

    LaunchedEffect(
        pendingRating,
        pendingCommentTouched,
        pendingComment
    ) {
        if (pendingRating !in 0..5) {
            return@LaunchedEffect
        }

        val timeoutMs =
            if (pendingCommentTouched) {
                CommentAutoSendPolicy.toMillis(audioSettings.ratingCommentTimeoutSeconds)
            } else {
                CommentAutoSendPolicy.toMillis(audioSettings.ratingNoCommentTimeoutSeconds)
            }

        delay(timeoutMs)
        latestAutoSendAction.value.invoke()
    }

    Surface(
        modifier =
            Modifier
                .fillMaxSize()
                .panelSwipeNavigation(
                    onSwipeLeft = onSwipeLeft,
                    onSwipeRight = onSwipeRight
                )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FaceliftLayoutPolicy.SCREEN_PADDING_DP.dp)
        ) {
            MainPanelHeader(
                developerModeEnabled = audioSettings.developerModeEnabled,
                onOpenSettings = onOpenSettings,
                onOpenAnalyzer = onOpenAnalyzer,
                onOpenMore = onOpenMore,
                onOpenSeismic = onOpenSeismic
            )

            Spacer(Modifier.height(FaceliftLayoutPolicy.MAIN_SECTION_GAP_DP.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(FaceliftLayoutPolicy.MAIN_CARD_PADDING_DP.dp)
                ) {
                    Text(
                        nextReminderStatus,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        reminderStatus +
                            " · Modus: " +
                            if (reminderMode == ReminderMode.ALARM) {
                                "Wecker-Modus"
                            } else {
                                "Sehr auffällig"
                            },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(FaceliftLayoutPolicy.MAIN_SECTION_GAP_DP.dp))

            SectionCard(title = "Erinnerungen pausieren", compact = true) {
                PauseControls(
                    onPauseHours = onPauseHours,
                    onDisable = onDisable
                )
            }

            Spacer(Modifier.height(FaceliftLayoutPolicy.MAIN_SECTION_GAP_DP.dp))

            SectionCard(
                title = "Bewertung",
                detail = "0 = nichts wahrgenommen · 5 = sehr stark",
                detailFontSizeSp = 8,
                compact = true
            ) {

                val selected =
                    if (pendingRating in 0..5) {
                        pendingRating
                    } else {
                        lastRating
                    }

                RatingRow(
                    values = 0..2,
                    selected = selected,
                    enabled = !isSending,
                    onRating = onRating
                )

                Spacer(Modifier.height(FaceliftLayoutPolicy.FIELD_GAP_DP.dp))

                RatingRow(
                    values = 3..5,
                    selected = selected,
                    enabled = !isSending,
                    onRating = onRating
                )

                if (pendingRating in 0..5) {
                    Spacer(Modifier.height(FaceliftLayoutPolicy.MAIN_SECTION_GAP_DP.dp))

                    OutlinedTextField(
                        value = pendingComment,
                        onValueChange = onPendingCommentChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Kommentar (optional)")
                        },
                        supportingText = {
                            Text("${pendingComment.length} / 2000")
                        },
                        minLines = 1,
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onSendPendingRating,
                            enabled = !isSending,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Bewertung senden")
                        }

                        OutlinedButton(
                            onClick = onCancelPendingRating,
                            enabled = !isSending,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Abbrechen")
                        }
                    }
                }
            }

            Spacer(Modifier.height(FaceliftLayoutPolicy.MAIN_SECTION_GAP_DP.dp))

            AudioRecordingCard(
                keyInput = keyInput,
                observerId = observerId,
                recentItems = recentItems,
                settings = audioSettings,
                reminderStore = reminderStore,
                supabaseClient = supabaseClient
            )

            Spacer(Modifier.height(FaceliftLayoutPolicy.MAIN_SECTION_GAP_DP.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(Modifier.padding(FaceliftLayoutPolicy.MAIN_CARD_PADDING_DP.dp)) {
                        Text(
                            "Übertragung",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            transmitStatus,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                OutlinedCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(Modifier.padding(FaceliftLayoutPolicy.MAIN_CARD_PADDING_DP.dp)) {
                        Text(
                            "Standort",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            locationStatus,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(FaceliftLayoutPolicy.MAIN_SECTION_GAP_DP.dp))

            SectionCard(title = "Letzte Bewertungen", compact = true) {
                if (recentStatus.isNotBlank()) {
                    Text(
                        recentStatus,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                recentItems.take(5).forEach { item ->
                    RecentObservationRow(item)
                    HorizontalDivider()
                }
            }

            if (radiusAnalysisEnabled) {
                Spacer(Modifier.height(FaceliftLayoutPolicy.MAIN_SECTION_GAP_DP.dp))

                SectionCard(title = "Radius-Histogramme · letzte 2 h", compact = true) {
                    if (radiusHistogramStatus.isNotBlank()) {
                        Text(
                            radiusHistogramStatus,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    radiusHistogramResult?.let { result ->
                        RadiusHistogramSummary(result)
                    }
                }
            }

            Spacer(Modifier.height(FaceliftLayoutPolicy.FIELD_GAP_DP.dp))

            Text(
                "v${BuildConfig.VERSION_NAME}",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MainPanelHeader(
    developerModeEnabled: Boolean,
    onOpenSettings: () -> Unit,
    onOpenAnalyzer: () -> Unit,
    onOpenMore: () -> Unit,
    onOpenSeismic: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("LFN-Scout", fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (developerModeEnabled) {
                Text(
                    "ENTWICKLERMODUS",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
        Text("Beobachten · bewerten · dokumentieren", style = MaterialTheme.typography.bodySmall)
        // Lucide vector glyphs: activity, blocks, settings, waves. Order is mandatory.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            listOf(
                Triple("Analyzer", R.drawable.nav_activity, onOpenAnalyzer),
                Triple("Zusatzfunktionen", R.drawable.nav_blocks, onOpenMore),
                Triple("Einstellungen", R.drawable.nav_settings, onOpenSettings),
                Triple("Seismik", R.drawable.nav_waves, onOpenSeismic)
            ).forEach { (title, drawable, action) ->
                TextButton(onClick = action, modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 1.dp, vertical = 2.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(painter = painterResource(drawable), contentDescription = title,
                            modifier = Modifier.height(22.dp).width(22.dp))
                        Text(title, fontSize = if (title == "Zusatzfunktionen") 8.sp else 9.sp,
                            maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactInlineHeader(
    title: String,
    detail: String,
    detailFontSizeSp: Int = 8
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )

        Spacer(
            Modifier.padding(horizontal = 3.dp)
        )

        Text(
            detail,
            modifier = Modifier.weight(1f),
            fontSize = detailFontSizeSp.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    detail: String? = null,
    detailFontSizeSp: Int = 8,
    compact: Boolean = false,
    content: @Composable () -> Unit
) {
    val paddingDp =
        if (compact) {
            FaceliftLayoutPolicy.MAIN_CARD_PADDING_DP
        } else {
            FaceliftLayoutPolicy.CARD_PADDING_DP
        }

    val titleGapDp =
        if (compact) {
            FaceliftLayoutPolicy.MAIN_CARD_TITLE_GAP_DP
        } else {
            FaceliftLayoutPolicy.CARD_TITLE_GAP_DP
        }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(paddingDp.dp)
        ) {
            if (detail == null) {
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                CompactInlineHeader(
                    title = title,
                    detail = detail,
                    detailFontSizeSp = detailFontSizeSp
                )
            }
            Spacer(Modifier.height(titleGapDp.dp))
            content()
        }
    }
}

@Composable
private fun PauseControls(
    onPauseHours: (Int) -> Unit,
    onDisable: () -> Unit
) {
    PauseControlsPolicy.rows
        .forEach { values ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {
                values.forEach { value ->
                    OutlinedButton(
                        onClick = {
                            if (
                                value ==
                                PauseControlsPolicy.DISABLE
                            ) {
                                onDisable()
                            } else {
                                onPauseHours(value)
                            }
                        },
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(36.dp),
                        contentPadding =
                            PaddingValues(
                                horizontal = 2.dp,
                                vertical = 0.dp
                            )
                    ) {
                        Text(
                            if (
                                value ==
                                PauseControlsPolicy.DISABLE
                            ) {
                                "Ganz aus"
                            } else {
                                "$value h"
                            },
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
}

@Composable
private fun AudioRecordingCard(
    keyInput: String,
    observerId: String,
    recentItems: List<RecentObservation>,
    settings: ReminderSettings,
    reminderStore: ReminderSettingsStore,
    supabaseClient: SupabaseClient
) {
    val context =
        LocalContext.current
    val appContext =
        context.applicationContext
    val scope =
        rememberCoroutineScope()
    val latestAudioRecentItems =
        rememberUpdatedState(
            recentItems
        )

    var localSaveConfig by remember { mutableStateOf(LocalAudioSaveSettings.load(appContext)) }
    var status by remember {
        mutableStateOf("Bereit")
    }
    var countdownVisible by remember {
        mutableStateOf(false)
    }
    var countdownSeconds by remember {
        mutableIntStateOf(
            AudioRecordingPolicy.COUNTDOWN_SECONDS
        )
    }
    var isRecording by remember {
        mutableStateOf(false)
    }
    var elapsedSeconds by remember {
        mutableIntStateOf(0)
    }
    var clippedSamples by remember {
        mutableIntStateOf(0)
    }
    var gainDb by remember(
        settings.audioGainDb
    ) {
        mutableIntStateOf(
            settings.audioGainDb
        )
    }
    var sourceMode by remember(
        settings.audioSourceMode
    ) {
        mutableStateOf(
            settings.audioSourceMode
        )
    }
    var selectedInputDeviceId by remember(
        settings.audioInputDeviceId
    ) {
        mutableIntStateOf(
            settings.audioInputDeviceId
        )
    }
    var availableInputs by remember {
        mutableStateOf(
            runCatching {
                AudioInputManager.listInputs(
                    appContext
                )
            }.getOrDefault(emptyList())
        )
    }
    var actualSourceLabel by remember {
        mutableStateOf("")
    }
    var actualDeviceLabel by remember {
        mutableStateOf("")
    }

    var pendingAudio by remember {
        mutableStateOf<AudioArchiveEntry?>(
            null
        )
    }
    var isUploadingAudio by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(
        settings.audioInputDeviceId,
        availableInputs
    ) {
        val normalized =
            AudioInputSelectionPolicy
                .normalizeDeviceId(
                    selectedDeviceId =
                        selectedInputDeviceId,
                    availableDeviceIds =
                        availableInputs
                            .map {
                                it.deviceId
                            }
                            .toSet()
                )

        if (
            normalized !=
            selectedInputDeviceId
        ) {
            selectedInputDeviceId =
                normalized
            reminderStore
                .setAudioInputDeviceId(
                    normalized
                )
        }
    }

    var recordingsToday by remember(
        settings.audioRecordingsToday
    ) {
        mutableIntStateOf(
            settings.audioRecordingsToday
        )
    }
    var activeJob by remember {
        mutableStateOf<Job?>(null)
    }

    val dailyLimit =
        settings.maxAudioRecordingsPerDay
    val limitReached =
        !AudioRecordingPolicy.canRecordToday(
            recordingsToday,
            dailyLimit
        )

    suspend fun sendPendingAudio(
        comment: String?
    ) {
        val pending =
            pendingAudio
                ?: return

        if (isUploadingAudio) {
            return
        }

        isUploadingAudio = true

        val normalizedComment =
            comment
                ?.take(
                    AudioArchive
                        .MAX_COMMENT_LENGTH
                )
                ?.takeIf {
                    it.isNotBlank()
                }

        val archived =
            AudioArchive
                .updateComment(
                    appContext,
                    pending.recordingId,
                    normalizedComment
                )
                ?: pending.copy(
                    comment =
                        normalizedComment
                )

        val wavBytes =
            AudioArchive.readWavBytes(
                appContext,
                archived
            )

        if (
            wavBytes == null ||
            wavBytes.isEmpty()
        ) {
            AudioArchive.markFailed(
                appContext,
                archived.recordingId,
                "Lokale WAV-Datei fehlt"
            )
            DebugLogger.log(
                appContext,
                "AUDIO_WAV_READ_FAILED",
                "recording_id=${archived.recordingId}"
            )
            status =
                "Audio-Upload nicht möglich · lokale WAV-Datei fehlt"
            isUploadingAudio = false
            pendingAudio = null
            return
        }

        DebugLogger.log(
            appContext,
            "AUDIO_UPLOAD_START",
            "recording_id=${archived.recordingId} bytes=${wavBytes.size} comment_chars=${archived.comment?.length ?: 0}"
        )

        status =
            "Audio wird übertragen ..."

        val upload =
            supabaseClient
                .uploadWav(
                    key = keyInput.trim(),
                    recordingId =
                        archived.recordingId,
                    wavBytes = wavBytes,
                    recordedAt =
                        archived.recordedAt,
                    gainDb =
                        archived.gainDb,
                    audioSource =
                        archived.audioSource,
                    clippedSamples =
                        archived.clippedSamples,
                    comment =
                        archived.comment,
                    captureOrigin = archived.origin,
                    triggerType = "MANUAL"
                )

        if (upload.success) {
            AudioArchive.markUploaded(
                appContext,
                archived.recordingId,
                upload.storagePath
            )
            AudioStatisticsOutbox.markRecordingUploaded(appContext, archived.recordingId)
            val (_, statsPending) = AudioStatisticsOutbox.drain(appContext, supabaseClient, keyInput.trim())
            DebugLogger.log(appContext, "OBSERVER_STATISTICS_SYNC", "pending=$statsPending")
            DebugLogger.log(
                appContext,
                "AUDIO_UPLOAD_OK",
                "recording_id=${archived.recordingId} storage_path=${upload.storagePath ?: "-"}"
            )
            AudioArchive.cleanup(
                appContext
            )
            status =
                "Aufnahme und Versand abgeschlossen · lokal 24 h aufbewahrt"
        } else {
            AudioArchive.markFailed(
                appContext,
                archived.recordingId,
                upload.message
            )
            DebugLogger.log(
                appContext,
                "AUDIO_UPLOAD_FAILED",
                "recording_id=${archived.recordingId} message=${upload.message}"
            )
            status =
                upload.message +
                    " · WAV bleibt lokal gespeichert"

            if (upload.keyRevoked) {
                reminderStore
                    .setDeveloperModeEnabled(
                        false
                    )
                status +=
                    " · Entwicklermodus deaktiviert"
            }
        }

        isUploadingAudio = false
        pendingAudio = null
    }

    val startSequence: () -> Unit = {
        DebugLogger.log(
            appContext,
            "AUDIO_RECORD_REQUEST",
            "gain=$gainDb source=${sourceMode.name} input_device_id=$selectedInputDeviceId"
        )

        activeJob?.cancel()
        countdownVisible = true
        countdownSeconds =
            AudioRecordingPolicy.COUNTDOWN_SECONDS
        elapsedSeconds = 0
        clippedSamples = 0
        actualSourceLabel = ""
        actualDeviceLabel = ""

        val normalizedDeviceId =
            AudioInputSelectionPolicy
                .normalizeDeviceId(
                    selectedDeviceId = selectedInputDeviceId,
                    availableDeviceIds =
                        availableInputs
                            .map { it.deviceId }
                            .toSet()
                )

        if (
            normalizedDeviceId !=
            selectedInputDeviceId
        ) {
            selectedInputDeviceId =
                normalizedDeviceId
            scope.launch {
                reminderStore
                    .setAudioInputDeviceId(
                        normalizedDeviceId
                    )
            }
        }

        status =
            "Aufnahme startet in " +
                "${AudioRecordingPolicy.COUNTDOWN_SECONDS} s"

        activeJob =
            scope.launch {
                for (
                    second in
                    AudioRecordingPolicy
                        .COUNTDOWN_SECONDS
                        downTo 1
                ) {
                    countdownSeconds = second
                    status =
                        "Aufnahme startet in $second s"
                    delay(1_000)
                }

                countdownVisible = false
                isRecording = true
                status = "Aufnahme läuft"

                DebugLogger.log(
                    appContext,
                    "AUDIO_RECORD_STARTED",
                    "gain=$gainDb source=${sourceMode.name}"
                )

                val saveConfig = LocalAudioSaveSettings.load(appContext)
                val recordedAt =
                    OffsetDateTime.now()
                        .format(
                            DateTimeFormatter
                                .ISO_OFFSET_DATE_TIME
                        )
                val observationsAtRecordingStart =
                    latestAudioRecentItems.value

                val result =
                    AudioRecorder.recordTwoMinutes(
                        context = appContext,
                        gainDb = gainDb,
                        durationSeconds = saveConfig.durationSeconds,
                        sourceMode = sourceMode,
                        preferredDeviceId =
                            selectedInputDeviceId,
                        onProgressSeconds = {
                            elapsedSeconds = it
                        },
                        onClippingChanged = {
                            clippedSamples = it
                        },
                        onInputResolved = {
                                source,
                                device ->
                            actualSourceLabel =
                                source
                            actualDeviceLabel =
                                device
                            DebugLogger.log(
                                appContext,
                                "AUDIO_INPUT_RESOLVED",
                                "source=$source device=$device"
                            )
                        }
                    )

                isRecording = false
                clippedSamples =
                    result.clippedSamples

                DebugLogger.log(
                    appContext,
                    "AUDIO_RECORD_FINISHED",
                    "success=${result.success} clipped=${result.clippedSamples} source=${result.sourceLabel} device=${result.deviceLabel}"
                )

                if (!result.success) {
                    DebugLogger.log(
                        appContext,
                        "AUDIO_RECORD_FAILED",
                        "message=${result.message}"
                    )
                    status = result.message
                    return@launch
                }

                val file =
                    result.file

                if (file == null) {
                    status =
                        "Aufnahme beendet, WAV-Datei fehlt"
                    return@launch
                }

                // Preserve source bytes BEFORE AudioArchive.create deletes the temporary WAV.
                val wavForStatistics = withContext(Dispatchers.IO) { file.readBytes() }
                val recordingId =
                    UUID.randomUUID()
                        .toString()

                val audioSource =
                    if (
                        result.sourceLabel ==
                        "MIC"
                    ) {
                        "MIC"
                    } else {
                        "UNP"
                    }

                val recordingStart =
                    OffsetDateTime.parse(
                        recordedAt
                    )

                val localWavFileName =
                    AudioFileNamingPolicy
                        .buildLocalWavFileName(
                            observerId =
                                observerId,
                            recordingStart =
                                recordingStart,
                            gainDb =
                                gainDb,
                            audioSource =
                                audioSource,
                            observations =
                                observationsAtRecordingStart
                        )

                DebugLogger.log(
                    appContext,
                    "AUDIO_LOCAL_FILENAME",
                    "name=$localWavFileName observer_id=${observerId.ifBlank { "unknown" }}"
                )

                val archiveEntry =
                    runCatching {
                        AudioArchive.create(
                            context =
                                appContext,
                            recordingId =
                                recordingId,
                            wavFile =
                                file,
                            targetDisplayName =
                                localWavFileName,
                            recordedAt =
                                recordedAt,
                            gainDb =
                                gainDb,
                            audioSource =
                                audioSource,
                            clippedSamples =
                                result.clippedSamples
                        )
                    }.getOrElse {
                        status =
                            "Aufnahme gespeichert, Metadatenfehler: " +
                                (
                                    it.message
                                        ?: it.javaClass.simpleName
                                    )
                        return@launch
                    }

                // Sidecar is local-only; failure cannot break the already working cloud upload.
                try {
                    val observerStatistics = LocalAudioStatisticsRepository.saveObserverSidecar(
                        appContext, wavForStatistics, recordingId, archiveEntry.wavFileName,
                        recordingStart.toInstant().toEpochMilli(), gainDb,
                        result.sourceLabel, result.deviceLabel,
                        StatisticsWindowEngine.Configuration(
                            saveConfig.minHz.toDouble(), saveConfig.maxHz.toDouble(),
                            saveConfig.minHz.toDouble(), saveConfig.maxHz.toDouble(), 10))
                    observerStatistics.analysisJson?.let { analysis ->
                        val queued = AudioStatisticsCloud.queueObserver(appContext, archiveEntry, analysis)
                        DebugLogger.log(appContext, "OBSERVER_STATISTICS_QUEUED", "recording_id=$recordingId count=$queued")
                    }
                } catch (e: Exception) {
                    DebugLogger.log(appContext, "AUDIO_LOCAL_STAT_ERROR", e.message ?: e.javaClass.simpleName)
                    status = "WAV gespeichert; Statistik nicht verfügbar: ${e.message}"
                }
                val updatedCount = reminderStore.tryMarkAudioRecordingCompleted()
                if (updatedCount == null) {
                    status = "Tageslimit inzwischen erreicht · WAV bleibt lokal gesichert"
                    return@launch
                }
                recordingsToday = updatedCount
                DebugLogger.log(
                    appContext,
                    "AUDIO_COMMENT_DIALOG_OPEN",
                    "recording_id=${archiveEntry.recordingId} timeout=${settings.audioNoCommentTimeoutSeconds}"
                )

                pendingAudio =
                    archiveEntry
                status =
                    result.message +
                        " · Kommentar möglich"
            }
    }

    val microphonePermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) { granted ->
            if (granted) {
                startSequence()
            } else {
                status =
                    "Mikrofonberechtigung verweigert"
            }
        }

    if (countdownVisible) {
        AlertDialog(
            onDismissRequest = {
                // Nicht versehentlich wegwischen.
            },
            title = {
                Text(
                    "Audioaufnahme in " +
                        "$countdownSeconds s"
                )
            },
            text = {
                Column {
                    Text(
                        "Bitte Handy auf eine leicht gepolsterte Oberfläche " +
                            "(zusammengefaltetes Küchentuch) legen und ${localSaveConfig.durationSeconds} s " +
                            "keine Geräusche machen."
                    )
                    Spacer(
                        Modifier.height(
                            FaceliftLayoutPolicy
                                .FIELD_GAP_DP.dp
                        )
                    )
                    Text(
                        "$countdownSeconds",
                        fontSize = 34.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        activeJob?.cancel()
                        activeJob = null
                        countdownVisible = false
                        status =
                            "Aufnahmestart abgebrochen"
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }


    val currentPending = pendingAudio

    if (currentPending != null) {
        AudioCommentDialog(
            recordingId = currentPending.recordingId,
            noCommentTimeoutSeconds = settings.audioNoCommentTimeoutSeconds,
            commentTimeoutSeconds = settings.audioCommentTimeoutSeconds,
            busy = isUploadingAudio,
            actionWord = "gesendet",
            busyText = "Versand läuft ...",
            confirmLabel = "Jetzt senden",
            withoutCommentLabel = "Ohne Kommentar senden",
            onInteraction = { timeout ->
                DebugLogger.log(
                    appContext,
                    "AUDIO_COMMENT_INTERACTION",
                    "recording_id=${currentPending.recordingId} timeout=$timeout"
                )
            },
            onCommentChanged = { value ->
                DebugLogger.log(
                    appContext,
                    "AUDIO_COMMENT_CHANGED",
                    "recording_id=${currentPending.recordingId} chars=${value.length}"
                )
                AudioArchive.updateComment(appContext, currentPending.recordingId, value)
            },
            onAutoComplete = { comment, interacted ->
                DebugLogger.log(
                    appContext,
                    "AUDIO_COMMENT_AUTO_SEND",
                    "recording_id=${currentPending.recordingId} mode=${if (interacted) "after_last_edit" else "untouched"} comment_chars=${comment?.length ?: 0}"
                )
                scope.launch { sendPendingAudio(comment) }
            },
            onConfirm = { comment ->
                DebugLogger.log(
                    appContext,
                    "AUDIO_COMMENT_MANUAL_SEND",
                    "recording_id=${currentPending.recordingId} comment_chars=${comment?.length ?: 0}"
                )
                scope.launch { sendPendingAudio(comment) }
            },
            onWithoutComment = {
                DebugLogger.log(
                    appContext,
                    "AUDIO_COMMENT_MANUAL_SEND",
                    "recording_id=${currentPending.recordingId} comment_chars=0 forced_without_comment=true"
                )
                AudioArchive.updateComment(appContext, currentPending.recordingId, null)
                scope.launch { sendPendingAudio(null) }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        SectionCard(
        title = "Audio aufzeichnen",
        detail = "WAV · ${localSaveConfig.durationSeconds} s · 1000 Hz · mono · 16 Bit",
        detailFontSizeSp = 8,
        compact = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            Text(
                "Verstärkung: +$gainDb dB",
                fontWeight =
                    FontWeight.SemiBold
            )

            Text(
                if (clippedSamples > 0) {
                    "CLIP · $clippedSamples"
                } else {
                    "CLIP: nein"
                },
                color =
                    if (clippedSamples > 0) {
                        MaterialTheme
                            .colorScheme
                            .error
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurface
                    },
                fontWeight =
                    if (clippedSamples > 0) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )
        }

        Slider(
            value = gainDb.toFloat(),
            onValueChange = {
                gainDb =
                    AudioRecordingPolicy
                        .normalizeGainDb(
                            it.toInt()
                        )
            },
            onValueChangeFinished = {
                scope.launch {
                    reminderStore.setAudioGainDb(gainDb)
                }
            },
            valueRange = 0f..50f,
            steps = 49,
            enabled =
                !isRecording &&
                    !countdownVisible &&
                    pendingAudio == null
        )

        Text(
            "Audio heute: " +
                "$recordingsToday / $dailyLimit",
            style =
                MaterialTheme
                    .typography
                    .bodySmall
        )

        if (
            actualSourceLabel.isNotBlank() ||
            actualDeviceLabel.isNotBlank()
        ) {
            Text(
                listOf(
                    actualSourceLabel,
                    actualDeviceLabel
                )
                    .filter {
                        it.isNotBlank()
                    }
                    .joinToString(" · "),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )
        }

        if (limitReached) {
            Text(
                "Tageslimit erreicht",
                color =
                    MaterialTheme
                        .colorScheme
                        .secondary,
                fontWeight =
                    FontWeight.Bold
            )
        }

        if (isRecording) {
            Text(
                "Aufnahme: " +
                    "${elapsedSeconds.coerceAtMost(localSaveConfig.durationSeconds)} / ${localSaveConfig.durationSeconds} s",
                fontWeight =
                    FontWeight.Bold
            )

            Button(
                onClick = {
                    activeJob?.cancel()
                    activeJob = null
                    isRecording = false
                    status =
                        "Aufnahme abgebrochen"
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .secondary,
                        contentColor =
                            MaterialTheme
                                .colorScheme
                                .onSecondary
                    ),
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text("Aufnahme abbrechen")
            }
        } else {
            Button(
                onClick = {
                    when {
                        keyInput.isBlank() -> {
                            status =
                                "Teilnehmerschlüssel fehlt"
                        }

                        limitReached -> {
                            status =
                                "Tageslimit erreicht"
                        }

                        ContextCompat
                            .checkSelfPermission(
                                appContext,
                                Manifest.permission
                                    .RECORD_AUDIO
                            ) ==
                            PackageManager
                                .PERMISSION_GRANTED -> {
                            startSequence()
                        }

                        else -> {
                            microphonePermissionLauncher
                                .launch(
                                    Manifest.permission
                                        .RECORD_AUDIO
                                )
                        }
                    }
                },
                enabled =
                    !limitReached &&
                        keyInput.isNotBlank() &&
                        !countdownVisible &&
                        pendingAudio == null,
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text("Aufzeichnung starten")
            }
        }

        if (
            status.isNotBlank() &&
            status != "Bereit"
        ) {
            Text(
                status,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )
        }
        }

        // AUDIO_RECORDING_TRANSPARENT_OVERLAY
        // Only the large remaining-seconds number is overlaid. There is no dialog
        // and no opaque surface, so CLIP and the yellow abort button underneath
        // remain visible and reachable.
        if (isRecording) {
            val remainingRecordingSeconds =
                (localSaveConfig.durationSeconds - elapsedSeconds).coerceAtLeast(0)
            Text(
                "$remainingRecordingSeconds",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun RadiusHistogramSummary(
    result: RadiusHistogramResult
) {
    val sourceText =
        when (result.centerSource.lowercase()) {
            "gps" -> "GPS"
            "basis" -> "Basisstandort"
            else -> result.centerSource
        }

    Text(
        "${result.radiusKm} km · $sourceText · " +
            "${result.observerCount} Beobachter",
        style = MaterialTheme.typography.bodySmall
    )

    Spacer(Modifier.height(FaceliftLayoutPolicy.RADIUS_GAP_DP.dp))

    result.blocks
        .take(4)
        .chunked(FaceliftLayoutPolicy.RADIUS_HISTOGRAM_COLUMNS)
        .forEach { rowBlocks ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowBlocks.forEach { block ->
                    CompactRadiusHistogramCard(
                        block = block,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (rowBlocks.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(FaceliftLayoutPolicy.RADIUS_GAP_DP.dp))
        }
}

@Composable
private fun CompactRadiusHistogramCard(
    block: RadiusHistogramBlock,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(7.dp)
        ) {
            Text(
                formatRadiusHistogramTimeRange(block),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )

            val maxCount =
                block.ratingCounts
                    .maxOrNull()
                    ?.coerceAtLeast(1)
                    ?: 1

            block.ratingCounts.forEachIndexed { rating, count ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        rating.toString(),
                        modifier = Modifier.weight(0.14f),
                        fontSize = 9.sp
                    )
                    LinearProgressIndicator(
                        progress = {
                            normalizedHistogramFraction(
                                count = count,
                                maxCount = maxCount
                            )
                        },
                        modifier = Modifier
                            .weight(0.66f)
                            .height(6.dp)
                    )
                    Text(
                        count.toString(),
                        modifier = Modifier.weight(0.20f),
                        fontSize = 9.sp
                    )
                }
            }

            Text(
                "-1: ${block.noResponseCount}",
                fontSize = 9.sp
            )
        }
    }
}

private fun normalizedHistogramFraction(
    count: Int,
    maxCount: Int
): Float {
    if (
        count <= 0 ||
        maxCount <= 0
    ) {
        return 0f
    }

    return (
        count.toFloat() /
            maxCount.toFloat()
        ).coerceIn(
        0f,
        1f
    )
}

private fun formatRadiusHistogramTimeRange(
    block: RadiusHistogramBlock
): String {
    val formatter =
        DateTimeFormatter.ofPattern(
            "HH:mm"
        )

    return runCatching {
        val start =
            Instant.parse(
                block.startTime
            )
                .atZone(
                    ZoneId.systemDefault()
                )
                .format(formatter)

        val end =
            Instant.parse(
                block.endTime
            )
                .atZone(
                    ZoneId.systemDefault()
                )
                .format(formatter)

        "$start–$end"
    }.getOrElse {
        "${block.startTime}–${block.endTime}"
    }
}

@Composable
private fun ReminderSettingsScreen(
    keyInput: String,
    onKeyInputChange: (String) -> Unit,
    onSaveKey: () -> Unit,
    keyStatus: String,
    settings: ReminderSettings,
    sound: ReminderSound,
    intervalInput: String,
    exactAllowed: Boolean,
    fullScreenAllowed: Boolean,
    dndAllowed: Boolean,
    onAudioSourceModeChange: (AudioSourceMode) -> Unit,
    onAudioInputDeviceChange: (Int) -> Unit,
    onWaterfallOutputFormatChange: (WaterfallOutputFormat) -> Unit,
    onWaterfallStatsRangeChange: (Int, Int) -> Unit,
    onNearPlotGainChange: (Int) -> Unit,
    onWaterfallLevelSettingsChange: (Boolean, Int, Int) -> Unit,
    onCommentTimeoutsChange: (Int, Int, Int, Int) -> Unit,
    onRollingBufferEnabledChange: (Boolean) -> Unit,
    onRollingMaxOperationHoursChange: (Int) -> Unit,
    onAlarmVolumeChange: (Int) -> Unit,
    onDeveloperModeChange: (Boolean) -> Unit,
    onMaxAudioRecordingsPerDayChange: (Int) -> Unit,
    onAutoBackgroundChange: (Boolean) -> Unit,
    radiusKmInput: String,
    onRadiusAnalysisEnabledChange: (Boolean) -> Unit,
    onRadiusKmInputChange: (String) -> Unit,
    onSaveRadiusKm: () -> Unit,
    onIntervalInputChange: (String) -> Unit,
    onSaveInterval: () -> Unit,
    onModeChange: (ReminderMode) -> Unit,
    onChooseSound: () -> Unit,
    soundSelfTestStatus: String,
    onStartSoundSelfTest: () -> Unit,
    onStopSoundSelfTest: () -> Unit,
    onTestNotification: () -> Unit,
    onPauseHours: (Int) -> Unit,
    onDisable: () -> Unit,
    onEnable: () -> Unit,
    onRequestExactAlarmAccess: () -> Unit,
    onRequestFullScreenAccess: () -> Unit,
    onRequestDndAccess: () -> Unit,
    onOpenDebugLog: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val appContext =
        LocalContext.current.applicationContext

    var audioSourceMode by remember(
        settings.audioSourceMode
    ) {
        mutableStateOf(
            settings.audioSourceMode
        )
    }
    var audioInputDeviceId by remember(
        settings.audioInputDeviceId
    ) {
        mutableIntStateOf(
            settings.audioInputDeviceId
        )
    }
    var availableAudioInputs by remember {
        mutableStateOf(
            runCatching {
                AudioInputManager.listInputs(
                    appContext
                )
            }.getOrDefault(emptyList())
        )
    }
    var audioSourceMenuExpanded by remember {
        mutableStateOf(false)
    }
    var audioInputMenuExpanded by remember {
        mutableStateOf(false)
    }
    var waterfallOutputMenuExpanded by remember {
        mutableStateOf(false)
    }
    var waterfallStatsMinInput by remember(settings.waterfallStatsMinHz) {
        mutableStateOf(settings.waterfallStatsMinHz.toString())
    }
    var waterfallStatsMaxInput by remember(settings.waterfallStatsMaxHz) {
        mutableStateOf(settings.waterfallStatsMaxHz.toString())
    }
    var nearPlotGainInput by remember(settings.nearPlotGain) {
        mutableStateOf(settings.nearPlotGain.toString())
    }
    var waterfallLevelMinInput by remember(settings.waterfallLevelMinDbAt50) {
        mutableStateOf(settings.waterfallLevelMinDbAt50.toString())
    }
    var waterfallLevelMaxInput by remember(settings.waterfallLevelMaxDbAt50) {
        mutableStateOf(settings.waterfallLevelMaxDbAt50.toString())
    }
    var ratingNoCommentTimeoutInput by remember(settings.ratingNoCommentTimeoutSeconds) {
        mutableStateOf(settings.ratingNoCommentTimeoutSeconds.toString())
    }
    var ratingCommentTimeoutInput by remember(settings.ratingCommentTimeoutSeconds) {
        mutableStateOf(settings.ratingCommentTimeoutSeconds.toString())
    }
    var audioNoCommentTimeoutInput by remember(settings.audioNoCommentTimeoutSeconds) {
        mutableStateOf(settings.audioNoCommentTimeoutSeconds.toString())
    }
    var audioCommentTimeoutInput by remember(settings.audioCommentTimeoutSeconds) {
        mutableStateOf(settings.audioCommentTimeoutSeconds.toString())
    }
    var audioInputStatus by remember {
        mutableStateOf("")
    }
    var localSaveConfig by remember { mutableStateOf(LocalAudioSaveSettings.load(appContext)) }
    var showLocalSaveSettings by remember { mutableStateOf(false) }

    if (showLocalSaveSettings) {
        LocalAudioSaveSettingsDialog(appContext) { updated ->
            if (updated != null) localSaveConfig = updated
            showLocalSaveSettings = false
        }
    }

    LaunchedEffect(
        settings.audioInputDeviceId,
        availableAudioInputs
    ) {
        val normalized =
            AudioInputSelectionPolicy
                .normalizeDeviceId(
                    selectedDeviceId =
                        audioInputDeviceId,
                    availableDeviceIds =
                        availableAudioInputs
                            .map { it.deviceId }
                            .toSet()
                )

        if (
            normalized !=
            audioInputDeviceId
        ) {
            audioInputDeviceId = normalized
            onAudioInputDeviceChange(normalized)
        }
    }

    var alarmVolumePercent by remember(
        settings.alarmVolumePercent
    ) {
        mutableIntStateOf(
            settings.alarmVolumePercent
        )
    }

    var showDeveloperPasswordDialog by remember {
        mutableStateOf(false)
    }
    var developerPassword by remember {
        mutableStateOf("")
    }
    var developerPasswordError by remember {
        mutableStateOf("")
    }
    var dailyLimitInput by remember(
        settings.maxAudioRecordingsPerDay
    ) {
        mutableStateOf(
            settings
                .maxAudioRecordingsPerDay
                .toString()
        )
    }

    if (showDeveloperPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeveloperPasswordDialog = false
                developerPassword = ""
                developerPasswordError = ""
            },
            title = {
                Text("Entwicklermodus freischalten")
            },
            text = {
                Column {
                    Text(
                        "Passwort eingeben. Es wird nicht gespeichert."
                    )
                    OutlinedTextField(
                        value = developerPassword,
                        onValueChange = {
                            developerPassword = it
                            developerPasswordError = ""
                        },
                        singleLine = true,
                        label = {
                            Text("Passwort")
                        },
                        keyboardOptions =
                            KeyboardOptions(
                                capitalization =
                                    KeyboardCapitalization
                                        .Characters,
                                keyboardType =
                                    KeyboardType.Ascii
                            )
                    )
                    if (
                        developerPasswordError
                            .isNotBlank()
                    ) {
                        Text(
                            developerPasswordError,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error,
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (
                            DeveloperModePolicy
                                .passwordMatches(
                                    keyInput,
                                    developerPassword
                                )
                        ) {
                            onDeveloperModeChange(
                                true
                            )
                            showDeveloperPasswordDialog =
                                false
                            developerPassword = ""
                            developerPasswordError = ""
                        } else {
                            developerPasswordError =
                                if (
                                    DeveloperModePolicy
                                        .expectedPassword(
                                            keyInput
                                        ) == null
                                ) {
                                    "Kein gültiger Schlüssel mit mindestens drei Vierergruppen."
                                } else {
                                    "Passwort falsch."
                                }
                        }
                    }
                ) {
                    Text("Freischalten")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeveloperPasswordDialog =
                            false
                        developerPassword = ""
                        developerPasswordError = ""
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }

    Surface(
        modifier =
            Modifier
                .fillMaxSize()
                .panelSwipeNavigation(
                    onSwipeLeft = onSwipeLeft,
                    onSwipeRight = onSwipeRight
                )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FaceliftLayoutPolicy.SCREEN_PADDING_DP.dp)
        ) {
            PanelHeader(
                title = "Einstellungen",
                onBack = onBack
            )

            Spacer(Modifier.height(FaceliftLayoutPolicy.SECTION_GAP_DP.dp))

            SectionCard(title = "Erinnerungen") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(formatReminderStatus(settings))
                        Text(
                            formatNextReminderStatus(settings),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = settings.enabled,
                        onCheckedChange = { enabled ->
                            if (enabled) onEnable() else onDisable()
                        }
                    )
                }

                Spacer(Modifier.height(FaceliftLayoutPolicy.FIELD_GAP_DP.dp))

                OutlinedTextField(
                    value = intervalInput,
                    onValueChange = onIntervalInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Intervall in Minuten") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                val shownInterval =
                    intervalInput.toIntOrNull()
                        ?: ReminderPolicy.DEFAULT_INTERVAL_MINUTES

                Text(
                    "Antwortfenster: " +
                        "${ReminderPolicy.responseWindowMinutes(shownInterval)} min " +
                        "(30 %, min. 5, max. 15 min)",
                    style = MaterialTheme.typography.bodySmall
                )

                Button(
                    onClick = onSaveInterval,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Intervall speichern")
                }
            }

            Spacer(Modifier.height(FaceliftLayoutPolicy.SECTION_GAP_DP.dp))

            SectionCard(title = "Auffälligkeit & Signal") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            onModeChange(ReminderMode.VERY_NOTICEABLE)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (settings.mode == ReminderMode.VERY_NOTICEABLE) {
                                "✓ Auffällig"
                            } else {
                                "Auffällig"
                            }
                        )
                    }

                    Button(
                        onClick = {
                            onModeChange(ReminderMode.ALARM)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (settings.mode == ReminderMode.ALARM) {
                                "✓ Wecker"
                            } else {
                                "Wecker"
                            }
                        )
                    }
                }

                Text(
                    "Ton: ${sound.title}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    "Wecklautstärke: $alarmVolumePercent %",
                    style = MaterialTheme.typography.bodySmall
                )

                Slider(
                    value =
                        alarmVolumePercent
                            .toFloat(),
                    onValueChange = {
                        alarmVolumePercent =
                            AudioRecordingPolicy
                                .normalizeAlarmVolumePercent(
                                    it.toInt()
                                )
                    },
                    onValueChangeFinished = {
                        onAlarmVolumeChange(
                            alarmVolumePercent
                        )
                    },
                    valueRange = 0f..100f,
                    steps = 19
                )

                OutlinedButton(
                    onClick = onChooseSound,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Erinnerungston wählen")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onStartSoundSelfTest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ton testen")
                    }
                    OutlinedButton(
                        onClick = onStopSoundSelfTest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stop")
                    }
                }

                OutlinedButton(
                    onClick = onTestNotification,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Erinnerung jetzt testen")
                }

                if (soundSelfTestStatus.isNotBlank()) {
                    Text(
                        soundSelfTestStatus,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

Spacer(Modifier.height(FaceliftLayoutPolicy.SECTION_GAP_DP.dp))

SectionCard(title = "Audioaufnahme") {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("30-min Rolling-Buffer", fontWeight = FontWeight.SemiBold)
            Text(
                if (settings.rollingBufferEnabled) "Läuft auch im Hintergrund; lokale Sicherung in MORE" else "Deaktiviert",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Switch(
            checked = settings.rollingBufferEnabled,
            onCheckedChange = onRollingBufferEnabledChange
        )
    }

    var rollingMaxHoursInput by remember(settings.rollingMaxOperationHours) {
        mutableStateOf(settings.rollingMaxOperationHours.toString())
    }
    OutlinedTextField(
        value = rollingMaxHoursInput,
        onValueChange = { rollingMaxHoursInput = it.filter(Char::isDigit).take(3) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Max. Rollingbuffer-Betriebsdauer (Stunden; 1–168)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    OutlinedButton(
        enabled = rollingMaxHoursInput.toIntOrNull()?.let { it in 1..168 } == true,
        onClick = { rollingMaxHoursInput.toIntOrNull()?.let(onRollingMaxOperationHoursChange) },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Maximale Betriebsdauer speichern") }

    OutlinedButton(
        onClick = { showLocalSaveSettings = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Audio / Statistik · ${localSaveConfig.durationSeconds} s · ${localSaveConfig.minHz}–${localSaveConfig.maxHz} Hz")
    }

    Text(
        "AudioSource",
        fontWeight =
            FontWeight.SemiBold
    )

    OutlinedButton(
        onClick = {
            audioSourceMenuExpanded = true
        },
        modifier =
            Modifier.fillMaxWidth()
    ) {
        Text(
            audioSourceMode.displayName()
        )
    }

    DropdownMenu(
        expanded =
            audioSourceMenuExpanded,
        onDismissRequest = {
            audioSourceMenuExpanded =
                false
        }
    ) {
        AudioSourceMode.entries
            .forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Text(
                            mode.displayName()
                        )
                    },
                    onClick = {
                        audioSourceMode =
                            mode
                        audioSourceMenuExpanded =
                            false
                        onAudioSourceModeChange(
                            mode
                        )
                    }
                )
            }
    }

    Text(
        "Audioeingang",
        fontWeight =
            FontWeight.SemiBold
    )

    OutlinedButton(
        onClick = {
            audioInputMenuExpanded = true
        },
        modifier =
            Modifier.fillMaxWidth()
    ) {
        Text(
            availableAudioInputs
                .firstOrNull {
                    it.deviceId ==
                        audioInputDeviceId
                }
                ?.label
                ?: "Auto · Android wählt Eingang"
        )
    }

    DropdownMenu(
        expanded =
            audioInputMenuExpanded,
        onDismissRequest = {
            audioInputMenuExpanded =
                false
        }
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    "Auto · Android wählt Eingang"
                )
            },
            onClick = {
                audioInputDeviceId =
                    AudioInputSelectionPolicy
                        .AUTO_DEVICE_ID
                audioInputMenuExpanded =
                    false
                onAudioInputDeviceChange(
                    AudioInputSelectionPolicy
                        .AUTO_DEVICE_ID
                )
            }
        )

        availableAudioInputs
            .forEach { input ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "${input.label} · ID ${input.deviceId}"
                        )
                    },
                    onClick = {
                        audioInputDeviceId =
                            input.deviceId
                        audioInputMenuExpanded =
                            false
                        onAudioInputDeviceChange(
                            input.deviceId
                        )
                    }
                )
            }
    }

    OutlinedButton(
        onClick = {
            val refreshed =
                runCatching {
                    AudioInputManager
                        .listInputs(
                            appContext
                        )
                }.getOrDefault(
                    emptyList()
                )

            availableAudioInputs =
                refreshed

            val normalized =
                AudioInputSelectionPolicy
                    .normalizeDeviceId(
                        selectedDeviceId =
                            audioInputDeviceId,
                        availableDeviceIds =
                            refreshed
                                .map {
                                    it.deviceId
                                }
                                .toSet()
                    )

            if (
                normalized !=
                audioInputDeviceId
            ) {
                audioInputDeviceId =
                    normalized
                onAudioInputDeviceChange(
                    normalized
                )
                audioInputStatus =
                    "Gespeicherter Eingang nicht verfügbar · Auto gewählt"
            } else {
                audioInputStatus =
                    "${refreshed.size} Audioeingänge gefunden"
            }
        },
        modifier =
            Modifier.fillMaxWidth()
    ) {
        Text("Inputs aktualisieren")
    }

    if (
        audioInputStatus.isNotBlank()
    ) {
        Text(
            audioInputStatus,
            style =
                MaterialTheme
                    .typography
                    .bodySmall
        )
    }

    Text(
        "Wasserfall-Bildformat",
        fontWeight = FontWeight.SemiBold
    )

    OutlinedButton(
        onClick = {
            waterfallOutputMenuExpanded = true
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            settings.waterfallOutputFormat.displayName
        )
    }

    DropdownMenu(
        expanded = waterfallOutputMenuExpanded,
        onDismissRequest = {
            waterfallOutputMenuExpanded = false
        }
    ) {
        WaterfallOutputFormat.entries
            .forEach { format ->
                DropdownMenuItem(
                    text = {
                        Text(format.displayName)
                    },
                    onClick = {
                        waterfallOutputMenuExpanded = false
                        onWaterfallOutputFormatChange(
                            format
                        )
                    }
                )
            }
    }

}

Spacer(Modifier.height(FaceliftLayoutPolicy.SECTION_GAP_DP.dp))

SectionCard(title = "Berechtigungen & System") {
                PermissionStatusRow(
                    label = "Exakte Alarme",
                    allowed = exactAllowed,
                    onRequest =
                        if (exactAllowed) null else onRequestExactAlarmAccess
                )

                PermissionStatusRow(
                    label = "Vollbild / Sperrbildschirm",
                    allowed = fullScreenAllowed,
                    onRequest =
                        if (
                            Build.VERSION.SDK_INT >= 34 &&
                            !fullScreenAllowed
                        ) {
                            onRequestFullScreenAccess
                        } else {
                            null
                        }
                )

                PermissionStatusRow(
                    label = "Nicht stören Zugriff",
                    allowed = dndAllowed,
                    onRequest =
                        if (dndAllowed) null else onRequestDndAccess
                )
            }

            Spacer(Modifier.height(FaceliftLayoutPolicy.SECTION_GAP_DP.dp))

            SectionCard(title = "Verhalten") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "App nach Aktion automatisch\nin den Hintergrund",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = settings.autoBackgroundAfterAction,
                        onCheckedChange = onAutoBackgroundChange
                    )
                }
            }

            Spacer(Modifier.height(FaceliftLayoutPolicy.SECTION_GAP_DP.dp))

            SectionCard(title = "Radius-Auswertung") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "2h-Auswertung nach Bewertung anzeigen",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = settings.radiusAnalysisEnabled,
                        onCheckedChange = onRadiusAnalysisEnabledChange
                    )
                }

                Text(
                    "Mittelpunkt: aktuelles GPS; sonst Basisstandort.",
                    style = MaterialTheme.typography.bodySmall
                )

                OutlinedTextField(
                    value = radiusKmInput,
                    onValueChange = onRadiusKmInputChange,
                    label = { Text("Radius in km") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = onSaveRadiusKm,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Radius speichern")
                }

                Text(
                    "Standard: 600 km · Bereich: 1–20000 km",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(FaceliftLayoutPolicy.SECTION_GAP_DP.dp))

            SectionCard(title = "Teilnehmerschlüssel") {
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = onKeyInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii
                    )
                )

                Button(
                    onClick = onSaveKey,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Schlüssel speichern")
                }

                if (keyStatus.isNotBlank()) {
                    Text(
                        keyStatus,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(FaceliftLayoutPolicy.SECTION_GAP_DP.dp))

            SectionCard(
                title = "Entwicklermodus"
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Text(
                            if (
                                settings
                                    .developerModeEnabled
                            ) {
                                "Entwicklermodus aktiv"
                            } else {
                                "Entwicklermodus aus"
                            },
                            fontWeight =
                                FontWeight.SemiBold,
                            color =
                                if (
                                    settings
                                        .developerModeEnabled
                                ) {
                                    MaterialTheme
                                        .colorScheme
                                        .secondary
                                } else {
                                    MaterialTheme
                                        .colorScheme
                                        .onSurface
                                }
                        )
                        Text(
                            "Freischaltung über abgeleitetes Passwort; dauerhaft bis zum Abschalten.",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall
                        )
                    }

                    Switch(
                        checked =
                            settings
                                .developerModeEnabled,
                        onCheckedChange = {
                            enabled ->
                            if (enabled) {
                                showDeveloperPasswordDialog =
                                    true
                            } else {
                                onDeveloperModeChange(
                                    false
                                )
                            }
                        }
                    )
                }

                if (
                    settings
                        .developerModeEnabled
                ) {
                    Text(
                        "Max. Audioaufnahmen pro Tag",
                        fontWeight =
                            FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value =
                            dailyLimitInput,
                        onValueChange = {
                            dailyLimitInput =
                                it.filter(
                                    Char::isDigit
                                )
                        },
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Number
                            ),
                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = {
                            onMaxAudioRecordingsPerDayChange(
                                dailyLimitInput
                                    .toIntOrNull()
                                    ?: AudioRecordingPolicy
                                        .DEFAULT_DAILY_LIMIT
                            )
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Tageslimit speichern"
                        )
                    }

                    Text(
                        "Standard 10 · Bereich 1–100",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )

                    Spacer(Modifier.height(FaceliftLayoutPolicy.FIELD_GAP_DP.dp))
                    Text("Näheplot-Messwertverstärkung", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = nearPlotGainInput,
                        onValueChange = { nearPlotGainInput = it.filter(Char::isDigit) },
                        label = { Text("Faktor 1–20") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = { onNearPlotGainChange(nearPlotGainInput.toIntOrNull() ?: SeismicPlotUiPolicy.DEFAULT_NEAR_PLOT_GAIN) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Näheplot-Verstärkung speichern") }
                    Text("Ganzzahlig 1–20 · verändert den Plot-Messwert, nicht die feste Y-Skala.", style = MaterialTheme.typography.bodySmall)

                    Spacer(Modifier.height(FaceliftLayoutPolicy.FIELD_GAP_DP.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Wasserfall-Pegelbereich automatisch", fontWeight = FontWeight.SemiBold)
                            Text("Ein: Automatik. Aus: manuelle gain-korrigierte dBFS-Grenzen.", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = settings.waterfallLevelAuto,
                            onCheckedChange = { enabled ->
                                onWaterfallLevelSettingsChange(
                                    enabled,
                                    waterfallLevelMinInput.toIntOrNull() ?: WaterfallLevelPolicy.DEFAULT_MIN_DB_AT_50,
                                    waterfallLevelMaxInput.toIntOrNull() ?: WaterfallLevelPolicy.DEFAULT_MAX_DB_AT_50
                                )
                            }
                        )
                    }
                    if (!settings.waterfallLevelAuto) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = waterfallLevelMinInput,
                                onValueChange = { value -> waterfallLevelMinInput = value.filter { ch -> ch.isDigit() || ch == '-' } },
                                label = { Text("Min dBFS (Gain korr.)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = waterfallLevelMaxInput,
                                onValueChange = { value -> waterfallLevelMaxInput = value.filter { ch -> ch.isDigit() || ch == '-' } },
                                label = { Text("Max dBFS (Gain korr.)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                onWaterfallLevelSettingsChange(
                                    false,
                                    waterfallLevelMinInput.toIntOrNull() ?: WaterfallLevelPolicy.DEFAULT_MIN_DB_AT_50,
                                    waterfallLevelMaxInput.toIntOrNull() ?: WaterfallLevelPolicy.DEFAULT_MAX_DB_AT_50
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Wasserfall-Pegelgrenzen speichern") }
                        val effective = WaterfallLevelPolicy.effectiveManualRange(
                            settings.audioGainDb,
                            settings.waterfallLevelMinDbAt50,
                            settings.waterfallLevelMaxDbAt50
                        )
                        Text(
                            "Aktuell bei ${settings.audioGainDb} dB Gain: ${effective.minDb.toInt()} … ${effective.maxDb.toInt()} dB",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(FaceliftLayoutPolicy.FIELD_GAP_DP.dp))
                    Text(
                        "L50/L90-Frequenzbereich (Entwickler)",
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = waterfallStatsMinInput,
                            onValueChange = { waterfallStatsMinInput = it.filter(Char::isDigit) },
                            label = { Text("von Hz") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = waterfallStatsMaxInput,
                            onValueChange = { waterfallStatsMaxInput = it.filter(Char::isDigit) },
                            label = { Text("bis Hz") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            onWaterfallStatsRangeChange(
                                waterfallStatsMinInput.toIntOrNull() ?: WaterfallStatsPolicy.DEFAULT_MIN_HZ.toInt(),
                                waterfallStatsMaxInput.toIntOrNull() ?: WaterfallStatsPolicy.DEFAULT_MAX_HZ.toInt()
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("L50/L90-Bereich speichern")
                    }
                    Text(
                        "Normalmodus fest 2–25 Hz · Entwicklerbereich 0–140 Hz",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(FaceliftLayoutPolicy.FIELD_GAP_DP.dp))
                    Text("Kommentar-Timeouts [s]", fontWeight = FontWeight.SemiBold)
                    Text("Die hier gespeicherten Werte gelten auch im Normalmodus.", style = MaterialTheme.typography.bodySmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = ratingNoCommentTimeoutInput, onValueChange = { ratingNoCommentTimeoutInput = it.filter(Char::isDigit) }, label = { Text("Bewertung Start") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = ratingCommentTimeoutInput, onValueChange = { ratingCommentTimeoutInput = it.filter(Char::isDigit) }, label = { Text("Bewertung Eingabe") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = audioNoCommentTimeoutInput, onValueChange = { audioNoCommentTimeoutInput = it.filter(Char::isDigit) }, label = { Text("Audio Start") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = audioCommentTimeoutInput, onValueChange = { audioCommentTimeoutInput = it.filter(Char::isDigit) }, label = { Text("Audio Eingabe") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    }
                    OutlinedButton(onClick = { onCommentTimeoutsChange(ratingNoCommentTimeoutInput.toIntOrNull() ?: 5, ratingCommentTimeoutInput.toIntOrNull() ?: 5, audioNoCommentTimeoutInput.toIntOrNull() ?: 60, audioCommentTimeoutInput.toIntOrNull() ?: 10) }, modifier = Modifier.fillMaxWidth()) { Text("Kommentar-Timeouts speichern") }
                    Text("Default: Bewertung 5/5 s · Audio 60/10 s · Bereich 1–300 s", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(FaceliftLayoutPolicy.SECTION_GAP_DP.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zurück zum Hauptpanel")
            }
        }
    }
}

@Composable
private fun PermissionStatusRow(
    label: String,
    allowed: Boolean,
    onRequest: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f)
        )

        if (onRequest != null) {
            OutlinedButton(onClick = onRequest) {
                Text("Freigeben")
            }
        } else {
            Text(
                if (allowed) "Erlaubt" else "Nicht verfügbar",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SeismicScreen(
    snapshot: SeismicLiveService.Snapshot?,
    status: String,
    isLoading: Boolean,
    selectedDate: LocalDate,
    onRefresh: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onToday: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    var seismicPlotZoom by remember(selectedDate, snapshot?.loadedAtEpochMs) { mutableStateOf(1f) }
    var seismicPlotOffset by remember(selectedDate, snapshot?.loadedAtEpochMs) { mutableStateOf(Offset.Zero) }
    var seismicPlotSaveStatus by remember(selectedDate, snapshot?.loadedAtEpochMs) { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .panelSwipeNavigation(
                onSwipeLeft = onSwipeLeft,
                onSwipeRight = onSwipeRight
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FaceliftLayoutPolicy.SCREEN_PADDING_DP.dp)
        ) {
            PanelHeader(
                title = "Seismik",
                onBack = onBack
            )

            Text(
                "Nächste verfügbare offene EIDA-Station · vertikaler Z-Kanal · echte FDSN-Wellenformdaten",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                if (selectedDate == today) {
                    "Tagesplot von 00:00 lokaler Zeit bis jetzt."
                } else {
                    "Tagesplot von 00:00 bis 24:00 lokaler Zeit."
                },
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onPreviousDate,
                    enabled = !isLoading
                ) {
                    Text("◀", fontSize = 20.sp)
                }

                OutlinedButton(
                    onClick = {
                        val dialog = android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val picked = LocalDate.of(year, month + 1, dayOfMonth)
                                if (!picked.isAfter(LocalDate.now(zoneId))) {
                                    onDateSelected(picked)
                                }
                            },
                            selectedDate.year,
                            selectedDate.monthValue - 1,
                            selectedDate.dayOfMonth
                        )
                        dialog.datePicker.maxDate = System.currentTimeMillis()
                        dialog.show()
                    },
                    enabled = !isLoading
                ) {
                    Text(selectedDate.format(dateFormatter))
                }

                TextButton(
                    onClick = onNextDate,
                    enabled = !isLoading && selectedDate.isBefore(today)
                ) {
                    Text("▶", fontSize = 20.sp)
                }

                TextButton(
                    onClick = onToday,
                    enabled = !isLoading && selectedDate != today
                ) {
                    Text("Heute")
                }
            }

            Spacer(Modifier.height(6.dp))

            Button(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Seismik wird geladen ..." else "Aktualisieren / nächste Station suchen")
            }

            Spacer(Modifier.height(8.dp))

            Text(
                status,
                fontWeight = FontWeight.SemiBold
            )

            if (isLoading) {
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            snapshot?.let { data ->
                val dataZone = runCatching { ZoneId.of(data.zoneId) }.getOrDefault(zoneId)
                val requestStartLocal = Instant.ofEpochMilli(data.requestedStartEpochMs).atZone(dataZone)
                val requestEndLocal = Instant.ofEpochMilli(data.requestedEndEpochMs).atZone(dataZone)

                Spacer(Modifier.height(10.dp))
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(8.dp)) {
                        Text(
                            "${data.station.network}.${data.station.station}",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (data.station.siteName.isNotBlank()) {
                            Text(data.station.siteName)
                        }
                        Text(
                            "Entfernung: ${"%.1f".format(data.station.distanceKm)} km · Höhe: " +
                                (data.station.elevationMeters?.let { "${it.toInt()} m" } ?: "–"),
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (data.sourceKind == "HLNUG_WBBU_IMAGE") {
                            Text(
                                "Aktuelles öffentliches 24-h-Seismogramm des Hessischen Erdbebendienstes",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Station WBBU · Vertikalkomponente · Stationsabtastung 100 Hz",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                "Kanal: ${if (data.locationCode.isBlank()) "--" else data.locationCode}.${data.channel} · vertikal · ${"%.1f".format(data.sampleRateHz)} Hz",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Angefordert: ${requestStartLocal.format(DateTimeFormatter.ofPattern("dd.MM. HH:mm"))} – " +
                                    if (data.isToday) "jetzt" else requestEndLocal.format(DateTimeFormatter.ofPattern("dd.MM. HH:mm")),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Messdaten: ${DebugLogger.formatEpoch(data.traceStartEpochMs)} – ${DebugLogger.formatEpoch(data.traceEndEpochMs)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Samples: ${data.sampleCount} · MiniSEED-Records: ${data.recordCount} · Encoding: ${data.encodings.sorted().joinToString(",")}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Datenknoten: ${data.routeHost}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            "Geladen: ${DebugLogger.formatEpoch(data.loadedAtEpochMs)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                val seismicBlue = Color(0xFF1565C0)
                val seismicRed = Color(0xFFC62828)

                if (data.sourceKind == "HLNUG_WBBU_IMAGE" && data.sourceImageBytes != null) {
                    val bitmap = remember(data.sourceImageBytes) {
                        runCatching { SeismicPlotExporter.renderWbbuPlot(data.sourceImageBytes) }
                            .getOrNull()
                            ?.asImageBitmap()
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(8.dp)) {
                            Text(
                                "WBBU Bürstadt · aktuelles 24-h-Seismogramm",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Sonderquelle für die Region Bürstadt/Viernheim · Originaldarstellung HLNUG",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    seismicPlotSaveStatus = "Plot wird gespeichert ..."
                                    scope.launch {
                                        try {
                                            val path = withContext(Dispatchers.IO) {
                                                SeismicPlotExporter.save(context, data)
                                            }
                                            seismicPlotSaveStatus = "Plot gespeichert: $path"
                                            Toast.makeText(context, "Plot erfolgreich gespeichert", Toast.LENGTH_LONG).show()
                                            DebugLogger.log(context, "SEISMIC_PLOT_SAVE_OK", "path=$path")
                                        } catch (error: Exception) {
                                            val errorMessage = "Speicherfehler: ${error.message ?: error.javaClass.simpleName}"
                                            seismicPlotSaveStatus = errorMessage
                                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                            DebugLogger.log(context, "SEISMIC_PLOT_SAVE_FAILED", "error=${error.javaClass.simpleName} message=${error.message ?: "-"}")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Plot speichern")
                            }
                            if (seismicPlotSaveStatus != null) {
                                Text(seismicPlotSaveStatus!!, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.height(6.dp))
                            bitmap?.let { image ->
                                Image(
                                    bitmap = image,
                                    contentDescription = "24-h-Seismogramm WBBU Bürstadt",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            scaleX = seismicPlotZoom
                                            scaleY = seismicPlotZoom
                                            translationX = seismicPlotOffset.x
                                            translationY = seismicPlotOffset.y
                                            clip = true
                                        }
                                        .pointerInput(data.loadedAtEpochMs, "wbbu_zoom") {
                                            detectTransformGestures { _, pan, gestureZoom, _ ->
                                                val newZoom = SeismicPlotUiPolicy.clampZoom(seismicPlotZoom * gestureZoom)
                                                seismicPlotZoom = newZoom
                                                seismicPlotOffset = if (newZoom <= SeismicPlotUiPolicy.MIN_ZOOM) Offset.Zero else seismicPlotOffset + pan
                                            }
                                        }
                                        .pointerInput(data.loadedAtEpochMs, "wbbu_double_tap_reset") {
                                            detectTapGestures(onDoubleTap = {
                                                seismicPlotZoom = SeismicPlotUiPolicy.MIN_ZOOM
                                                seismicPlotOffset = Offset.Zero
                                            })
                                        },
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                        }
                    }
                } else {
                    val nearPlotBitmap = remember(data.loadedAtEpochMs) {
                        SeismicPlotExporter.renderNearPlot(data)
                    }
                    val nearPlotImage = remember(nearPlotBitmap) { nearPlotBitmap.asImageBitmap() }
                    Spacer(Modifier.height(8.dp))
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(8.dp)) {
                            Text(
                                "${data.station.network}.${data.station.station} ${data.channel} · ${data.selectedDate}",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (data.amplitudeUnit == "nm/s") {
                                    "48 Zeilen · 30 min/Zeile · feste WBBU-Y-Skalierung · Messwerte ×${data.plotValueGain} · 3072 Bins/Zeile · Master-Bitmap 6144×3456"
                                } else {
                                    "48 Zeilen · 30 min/Zeile · Counts-Fallback · Messwerte ×${data.plotValueGain} · 3072 Bins/Zeile · Master-Bitmap 6144×3456"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (data.amplitudeUnit == "nm/s") {
                                Text(
                                    "WBBU-Referenz: 40000 nm/s ≈ 4,95 Zeilenabstände · ca. 8080 nm/s je Zeilenabstand · Plot-Messwerte ×${data.plotValueGain}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "FDSN-Sensitivität: ${data.sensitivity?.let { "%.6g".format(it) } ?: "–"} ${data.sensitivityUnits}" +
                                        (data.sensitivityFrequencyHz?.let { " @ ${"%.3g".format(it)} Hz" } ?: ""),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Text(
                                    "Keine passende Geschwindigkeits-Sensitivität verfügbar; physikalische Vergleichsskala ist für diesen Kanal nicht verfügbar. Plot-Messwerte werden trotzdem ×${data.plotValueGain} verstärkt.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    seismicPlotSaveStatus = "Plot wird gespeichert ..."
                                    scope.launch {
                                        try {
                                            val path = withContext(Dispatchers.IO) {
                                                SeismicPlotExporter.save(context, data, nearPlotBitmap)
                                            }
                                            seismicPlotSaveStatus = "Plot gespeichert: $path"
                                            Toast.makeText(context, "Plot erfolgreich gespeichert", Toast.LENGTH_LONG).show()
                                            DebugLogger.log(context, "SEISMIC_PLOT_SAVE_OK", "path=$path")
                                        } catch (error: Exception) {
                                            val errorMessage = "Speicherfehler: ${error.message ?: error.javaClass.simpleName}"
                                            seismicPlotSaveStatus = errorMessage
                                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                            DebugLogger.log(context, "SEISMIC_PLOT_SAVE_FAILED", "error=${error.javaClass.simpleName} message=${error.message ?: "-"}")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Plot speichern")
                            }
                            if (seismicPlotSaveStatus != null) {
                                Text(seismicPlotSaveStatus!!, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.height(6.dp))
                            Image(
                                bitmap = nearPlotImage,
                                contentDescription = "Selbst erzeugter 24-h-Helicorder mit Uhrzeiten",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f / SeismicPlotUiPolicy.LIVE_ASPECT_RATIO)
                                    .graphicsLayer {
                                        scaleX = seismicPlotZoom
                                        scaleY = seismicPlotZoom
                                        translationX = seismicPlotOffset.x
                                        translationY = seismicPlotOffset.y
                                        clip = true
                                    }
                                    .pointerInput(data.loadedAtEpochMs, "near_bitmap_zoom") {
                                        detectTransformGestures { _, pan, gestureZoom, _ ->
                                            val newZoom = SeismicPlotUiPolicy.clampZoom(seismicPlotZoom * gestureZoom)
                                            seismicPlotZoom = newZoom
                                            seismicPlotOffset = if (newZoom <= SeismicPlotUiPolicy.MIN_ZOOM) Offset.Zero else seismicPlotOffset + pan
                                        }
                                    }
                                    .pointerInput(data.loadedAtEpochMs, "near_bitmap_double_tap_reset") {
                                        detectTapGestures(onDoubleTap = {
                                            seismicPlotZoom = SeismicPlotUiPolicy.MIN_ZOOM
                                            seismicPlotOffset = Offset.Zero
                                        })
                                    },
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    }
                }

                Text(
                    "Zoom: Zwei Finger · Verschieben im Zoom · Doppeltipp setzt zurück",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(6.dp))
                Text(
                    if (data.sourceKind == "HLNUG_WBBU_IMAGE") {
                        "Quelle: HLNUG Hessischer Erdbebendienst · HS.WBBU Bürstadt"
                    } else {
                        "Quelle: EIDA Routing + FDSN DataSelect · Näheprinzip"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Hinweis: Im Umkreis von 30 km um WBBU Bürstadt gilt für den aktuellen Tag die regionale HLNUG-Sonderquelle. Für frühere Tage sowie außerhalb dieser Zone gilt das Näheprinzip: verwendet wird die nächstgelegene tatsächlich verfügbare offene Station mit vertikalem Z-Kanal. Valencia und andere Orte verwenden daher ausschließlich das Näheprinzip.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PanelHeader(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("‹", fontSize = 30.sp)
        }
        Text(
            title,
            modifier = Modifier.weight(1f),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MoreFunctionsScreen(
    developerModeEnabled: Boolean,
    exportStatus: String,
    isExporting: Boolean,
    transmitStatus: String,
    locationStatus: String,
    audioDirectoryPath: String,
    audioEntries: List<AudioArchiveEntry>,
    audioArchiveStatus: String,
    waterfallStatus: String,
    isGeneratingWaterfall: Boolean,
    generatingWaterfallRecordingId: String?,
    onGenerateWaterfallForEntry: (AudioArchiveEntry) -> Unit,
    rollingBufferEnabled: Boolean,
    rollingBufferedSeconds: Int,
    rollingStatus: String,
    onSaveRollingBuffer: () -> Unit,
    onOpenAnalyzer: () -> Unit,
    onDeleteRolling: (AudioArchiveEntry) -> Unit,
    onExportCsv: () -> Unit,
    onExportXlsx: () -> Unit,
    onCopyAudioPath: () -> Unit,
    onRefreshAudioArchive: () -> Unit,
    onRetryAudio: (AudioArchiveEntry) -> Unit,
    logSaveStatus: String,
    onSaveLog: () -> Unit,
    onOpenDebugLog: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onBack: () -> Unit
) {
    Surface(
        modifier =
            Modifier
                .fillMaxSize()
                .panelSwipeNavigation(
                    onSwipeLeft = onSwipeLeft,
                    onSwipeRight = onSwipeRight
                )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    FaceliftLayoutPolicy
                        .SCREEN_PADDING_DP.dp
                )
        ) {
            PanelHeader(
                title = "Zusatzfunktionen",
                onBack = onBack
            )

            Spacer(
                Modifier.height(
                    FaceliftLayoutPolicy
                        .SECTION_GAP_DP.dp
                )
            )

            SectionCard(title = "Spektrum & Wasserfall") {
                Text("Live-Analyzer · 0–200 Hz · gemeinsamer PCM-Datenstrom")
                Button(onClick = onOpenAnalyzer, modifier = Modifier.fillMaxWidth()) {
                    Text("Spektrum-Analyzer öffnen")
                }
            }
            Spacer(Modifier.height(FaceliftLayoutPolicy.SECTION_GAP_DP.dp))

            SectionCard(
                title = "Datenexport"
            ) {
                Text(
                    "Eigene Beobachtungen vom Server abrufen und lokal speichern.",
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )

                Button(
                    onClick = onExportCsv,
                    enabled = !isExporting,
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isExporting) {
                            "Abruf läuft ..."
                        } else {
                            "Als CSV speichern"
                        }
                    )
                }

                OutlinedButton(
                    onClick = onExportXlsx,
                    enabled = !isExporting,
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text("Als XLSX speichern")
                }

                if (
                    exportStatus.isNotBlank()
                ) {
                    Text(
                        exportStatus,
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }
            }

            Spacer(
                Modifier.height(
                    FaceliftLayoutPolicy
                        .SECTION_GAP_DP.dp
                )
            )

            SectionCard(
                title = "Audio-Aufzeichnungen"
            ) {
                Button(
                    onClick = onSaveRollingBuffer,
                    enabled = rollingBufferEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Rolling-Buffer sichern")
                }
                Text(
                    if (rollingBufferEnabled) {
                        "Rolling-Buffer: ${RollingAudioPolicy.bufferStatusText(rollingBufferedSeconds)}"
                    } else {
                        "Rolling-Buffer: deaktiviert"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                if (rollingStatus.isNotBlank()) {
                    Text(rollingStatus, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                Text(
                    "Lokales Aufzeichnungsverzeichnis:",
                    fontWeight =
                        FontWeight.SemiBold
                )

                SelectionContainer {
                    Text(
                        audioDirectoryPath,
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            6.dp
                        )
                ) {
                    OutlinedButton(
                        onClick =
                            onCopyAudioPath,
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Text("Pfad kopieren")
                    }

                    if (developerModeEnabled) {
                        OutlinedButton(
                            onClick = onRefreshAudioArchive,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Aktualisieren")
                        }
                    }
                }

                OutlinedButton(
                    onClick = onSaveLog,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logfile speichern")
                }
                if (logSaveStatus.isNotBlank()) {
                    Text(logSaveStatus, style = MaterialTheme.typography.bodySmall)
                }

                Text(
                    "Erfolgreich gesendete WAV-Dateien bleiben 24 h lokal. " +
                        "Nicht erfolgreich gesendete Aufnahmen werden nicht automatisch gelöscht.",
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )

                if (
                    waterfallStatus.isNotBlank()
                ) {
                    Text(
                        waterfallStatus,
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }

                if (
                    audioArchiveStatus
                        .isNotBlank()
                ) {
                    Text(
                        audioArchiveStatus,
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }

                if (
                    audioEntries.isEmpty()
                ) {
                    Text(
                        "Keine lokalen Audio-Aufzeichnungen vorhanden.",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                } else {
                    audioEntries
                        .forEach {
                                entry ->
                            OutlinedCard(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .pointerInput(entry.recordingId, entry.rollingBuffer) {
                                            detectTapGestures(
                                                onTap = {
                                                    if (!isGeneratingWaterfall) onGenerateWaterfallForEntry(entry)
                                                },
                                                onLongPress = {
                                                    if (entry.rollingBuffer) onDeleteRolling(entry)
                                                }
                                            )
                                        }
                            ) {
                                Column(
                                    modifier =
                                        Modifier.padding(
                                            7.dp
                                        )
                                ) {
                                    Column(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    vertical = 3.dp
                                                )
                                    ) {
                                        Text(
                                            entry
                                                .formattedRecordedAt(),
                                            fontWeight =
                                                FontWeight
                                                    .SemiBold
                                        )

                                        Text(
                                            entry.wavFileName,
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodySmall
                                        )

                                        if (entry.rollingBuffer) {
                                            Text(
                                                "ROLL · %02d:%02d min".format(entry.durationSeconds / 60, entry.durationSeconds % 60),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Text(
                                            "G${entry.gainDb} · " +
                                                entry.audioSource +
                                                " · CLIP ${entry.clippedSamples}",
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodySmall
                                        )

                                        Text(
                                            if (entry.rollingBuffer) {
                                                "Status: ROLL · lokal · keine Cloud-Übertragung · dauerhaft bis zum Löschen"
                                            } else if (entry.isUploaded) {
                                                "Status: gesendet · Löschung 24 h nach erfolgreichem Upload"
                                            } else {
                                                "Status: wartet auf erfolgreichen Versand"
                                            },
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodySmall
                                        )

                                        if (
                                            !entry.comment
                                                .isNullOrBlank()
                                        ) {
                                            Text(
                                                "Kommentar: " +
                                                    entry.comment,
                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .bodySmall
                                            )
                                        }

                                        if (
                                            !entry.lastError
                                                .isNullOrBlank()
                                        ) {
                                            Text(
                                                "Letzter Fehler: " +
                                                    entry.lastError,
                                                color =
                                                    MaterialTheme
                                                        .colorScheme
                                                        .error,
                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .bodySmall
                                            )
                                        }

                                        Text(
                                            if (
                                                generatingWaterfallRecordingId == entry.recordingId
                                            ) {
                                                "Wasserfall wird erzeugt ..."
                                            } else {
                                                "Antippen: Wasserfall öffnen"
                                            },
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodySmall
                                        )
                                    }

                                    if (
                                        !entry.isUploaded && !entry.rollingBuffer
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                onRetryAudio(
                                                    entry
                                                )
                                            },
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                        ) {
                                            Text(
                                                "Erneut senden"
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    if (
                        audioEntries.size >
                        20
                    ) {
                        Text(
                            "Es werden alle lokalen Aufnahmen angezeigt.",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall
                        )
                    }
                }
            }

            if (developerModeEnabled) {
                Spacer(
                    Modifier.height(
                        FaceliftLayoutPolicy
                            .SECTION_GAP_DP.dp
                    )
                )

                SectionCard(
                    title = "Verlauf & Rohdaten"
                ) {
                    Text(
                        "Alle Beobachtungen: über Export abrufbar",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                    Text(
                        "WAV-Dateien: private Speicherung im Bucket brumm-audio",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                    Text(
                        "Radius-Rohdaten: Serverabruf für die 2h-Auswertung",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }

                Spacer(
                    Modifier.height(
                        FaceliftLayoutPolicy
                            .SECTION_GAP_DP.dp
                    )
                )

                SectionCard(
                    title = "Debug & Diagnose"
                ) {
                    OutlinedButton(
                        onClick =
                            onOpenDebugLog,
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Debug-Protokoll anzeigen"
                        )
                    }

                    Text(
                        "Übertragung: $transmitStatus",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                    Text(
                        locationStatus,
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }

                Spacer(
                    Modifier.height(
                        FaceliftLayoutPolicy
                            .SECTION_GAP_DP.dp
                    )
                )

                SectionCard(
                    title = "Systemstatus"
                ) {
                    Text(
                        "App-Version: ${BuildConfig.VERSION_NAME}"
                    )
                    Text(
                        "Tonpakete: In-Process 0 / 1/3 / 2/3",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                    Text(
                        "Audio: 120 s / 1000 Hz / PCM16 / mono",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                    Text(
                        "Offline-Puffer Bewertung: automatische 2h-Wiederholung",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }
            } else {
                Spacer(
                    Modifier.height(
                        FaceliftLayoutPolicy
                            .SECTION_GAP_DP.dp
                    )
                )

                SectionCard(
                    title = "Entwicklerfunktionen"
                ) {
                    Text(
                        "Rohdaten, Debug und technische Systemdetails sind im Entwicklermodus sichtbar.",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                    )
                }
            }

            Spacer(
                Modifier.height(
                    FaceliftLayoutPolicy
                        .SECTION_GAP_DP.dp
                )
            )

            SectionCard(
                title = "Info"
            ) {
                Text("LFN-Scout")
                Text(
                    "Begleit-App zum InversionSystem / InversionAnalyzer",
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )
            }

            if (developerModeEnabled) {
                Spacer(Modifier.height(FaceliftLayoutPolicy.SECTION_GAP_DP.dp))
                ImpulseDiagnosticCard()
            }
        }
    }
}

@Composable
private fun DebugLogScreen(
    lines: List<String>,
    onReload: () -> Unit,
    onClear: () -> Unit,
    onSave: () -> Unit,
    saveStatus: String,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(FaceliftLayoutPolicy.SCREEN_PADDING_DP.dp)
        ) {
            Text(
                "Debug-Protokoll",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onReload,
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text("Neu laden")
                }

                OutlinedButton(
                    onClick = onClear,
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text("Log löschen")
                }
            }

            OutlinedButton(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Protokoll speichern")
            }
            if (saveStatus.isNotBlank()) {
                Text(saveStatus, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedButton(
                onClick = onBack,
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text("Zurück")
            }

            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(
                            rememberScrollState()
                        )
                ) {
                    if (lines.isEmpty()) {
                        Text(
                            "Noch keine Logeinträge."
                        )
                    } else {
                        lines.asReversed()
                            .forEach { line ->
                                Text(
                                    line,
                                    fontSize = 12.sp
                                )
                                Spacer(
                                    Modifier.height(
                                        4.dp
                                    )
                                )
                            }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentObservationRow(
    item: RecentObservation
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {
        Text(
            item.formattedLocalTime()
        )
        Text(
            item.rating.toString(),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RatingRow(
    values: IntRange,
    selected: Int,
    enabled: Boolean,
    onRating: (Int) -> Unit
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(6.dp)
    ) {
        values.forEach { value ->
            Button(
                onClick = {
                    onRating(value)
                },
                enabled = enabled,
                colors =
                    if (selected == value) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    },
                modifier = Modifier
                    .weight(1f)
                    .height(FaceliftLayoutPolicy.RATING_BUTTON_HEIGHT_DP.dp)
            ) {
                Text(
                    if (selected == value) {
                        "✓ $value"
                    } else {
                        value.toString()
                    },
                    fontSize = 23.sp
                )
            }
        }
    }
}

@Composable
fun LFNScoutTheme(
    content: @Composable () -> Unit
) {
    val colors =
        lightColorScheme(
            primary = Color(0xFF1F8F8A),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD7F0EE),
            onPrimaryContainer = Color(0xFF082F2D),
            secondary = Color(0xFFE0B000),
            onSecondary = Color(0xFF2D2400),
            secondaryContainer = Color(0xFFFFE59A),
            onSecondaryContainer = Color(0xFF332700),
            background = Color(0xFFF8FBFA),
            onBackground = Color(0xFF1A1C1C),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1C1C)
        )

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
