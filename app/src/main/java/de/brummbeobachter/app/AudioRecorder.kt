package de.brummbeobachter.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

data class AudioRecordingResult(
    val success: Boolean,
    val file: File? = null,
    val clippedSamples: Int = 0,
    val sourceLabel: String = "",
    val deviceLabel: String = "",
    val message: String
)

object AudioRecorder {

    @SuppressLint("MissingPermission")
    suspend fun recordTwoMinutes(
        context: Context,
        gainDb: Int,
        durationSeconds: Int = AudioRecordingPolicy.DURATION_SECONDS,
        sourceMode: AudioSourceMode = AudioSourceMode.AUTO,
        preferredDeviceId: Int =
            AudioInputSelectionPolicy.AUTO_DEVICE_ID,
        onProgressSeconds: suspend (Int) -> Unit = {},
        onClippingChanged: suspend (Int) -> Unit = {},
        onInputResolved: suspend (String, String) -> Unit = { _, _ -> }
    ): AudioRecordingResult =
        withContext(Dispatchers.IO) {
            require(durationSeconds in LocalAudioSaveSettings.MIN_SECONDS..LocalAudioSaveSettings.MAX_SECONDS)
            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@withContext AudioRecordingResult(
                    success = false,
                    message = "Mikrofonberechtigung fehlt"
                )
            }

            if (RollingAudioEngine.isRunning) {
                val source = RollingAudioEngine.sourceLabel.ifBlank { "UNPROCESSED" }
                val device = RollingAudioEngine.deviceLabel.ifBlank { "Auto" }
                withContext(Dispatchers.Main) {
                    onInputResolved(source, device)
                }
                val captured = RollingAudioEngine.captureNext(
                    seconds = durationSeconds,
                    onProgressSeconds = onProgressSeconds,
                    onClippingChanged = onClippingChanged
                )
                if (captured == null || captured.pcm16.isEmpty()) {
                    return@withContext AudioRecordingResult(
                        success = false,
                        message = "Rolling-Audiostream nicht verfügbar"
                    )
                }
                // Rolling supplies PRE-GAIN 1-kHz PCM. Apply requested WAV recording gain
                // exactly once here, like the dedicated AudioRecord branch below.
                val wavPcm = captured.pcm16.copyOf()
                val recordingGain = AudioRecordingPolicy.gainFactor(gainDb)
                var gainClipped = 0
                if (gainDb != 0) {
                    for (i in 0 until wavPcm.size / 2) {
                        val raw = ((wavPcm[2*i+1].toInt() shl 8) or
                            (wavPcm[2*i].toInt() and 255)).toShort().toInt()
                        val amplified = raw * recordingGain
                        if (amplified > Short.MAX_VALUE || amplified < Short.MIN_VALUE) gainClipped++
                        val scaled = amplified.roundToInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        wavPcm[2*i] = scaled.toByte()
                        wavPcm[2*i+1] = (scaled shr 8).toByte()
                    }
                }
                val clippedOutput = maxOf(captured.clippedSamples, gainClipped)
                val directory = File(context.cacheDir, "audio_recording_temp").apply { mkdirs() }
                val file = File(directory, "brumm_${System.currentTimeMillis()}.wav")
                file.outputStream().use {
                    it.write(WavPcm16.buildHeader(
                        sampleRateHz = AudioRecordingPolicy.TARGET_SAMPLE_RATE_HZ,
                        channels = 1,
                        bitsPerSample = 16,
                        dataBytes = wavPcm.size
                    ))
                    it.write(wavPcm)
                }
                return@withContext AudioRecordingResult(
                    success = true,
                    file = file,
                    clippedSamples = clippedOutput,
                    sourceLabel = source,
                    deviceLabel = device,
                    message = if (clippedOutput > 0) {
                        "Aufnahme fertig · $clippedOutput übersteuerte Samples"
                    } else {
                        "Aufnahme fertig · keine Übersteuerung"
                    }
                )
            }

            val sourceRate =
                AudioRecordingPolicy.SOURCE_SAMPLE_RATE_HZ
            val targetRate =
                AudioRecordingPolicy.TARGET_SAMPLE_RATE_HZ
            val decimation =
                sourceRate / targetRate

            require(
                sourceRate % targetRate == 0
            )

            val minBuffer =
                AudioRecord.getMinBufferSize(
                    sourceRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

            if (minBuffer <= 0) {
                return@withContext AudioRecordingResult(
                    success = false,
                    message = "Audioeingang unterstützt 8000 Hz nicht"
                )
            }

            fun createRecorder(source: Int): AudioRecord =
                AudioRecord(
                    source,
                    sourceRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    maxOf(minBuffer, 4096)
                )

            var recorder: AudioRecord? = null
            var usedSourceMode: AudioSourceMode? = null
            var preferredDeviceLabel = "Auto"

            // Prevent a concurrent RollingAudioService start while direct AudioRecord owns mic.
            val captureLease = MicCaptureArbiter.tryAcquire("direct_recording")
                ?: return@withContext AudioRecordingResult(
                    success = false,
                    message = "Mikrofon durch ${MicCaptureArbiter.owner ?: "andere Aufnahme"} belegt"
                )
            try {
                val preferredDevice =
                    AudioInputManager.findInput(
                        context,
                        preferredDeviceId
                    )

                preferredDeviceLabel =
                    preferredDevice
                        ?.let(AudioInputManager::buildLabel)
                        ?: "Auto"

                for (
                    candidate in
                    AudioInputSelectionPolicy.sourceOrder(sourceMode)
                ) {
                    val source =
                        when (candidate) {
                            AudioSourceMode.UNPROCESSED ->
                                if (
                                    android.os.Build.VERSION.SDK_INT >= 24
                                ) {
                                    MediaRecorder.AudioSource.UNPROCESSED
                                } else {
                                    continue
                                }

                            AudioSourceMode.MIC ->
                                MediaRecorder.AudioSource.MIC

                            AudioSourceMode.AUTO ->
                                continue
                        }

                    val candidateRecorder =
                        runCatching {
                            createRecorder(source)
                        }.getOrNull()

                    if (
                        candidateRecorder != null &&
                        candidateRecorder.state ==
                            AudioRecord.STATE_INITIALIZED
                    ) {
                        recorder = candidateRecorder
                        usedSourceMode = candidate

                        if (preferredDevice != null) {
                            candidateRecorder
                                .setPreferredDevice(
                                    preferredDevice
                                )
                        }

                        break
                    } else {
                        runCatching {
                            candidateRecorder?.release()
                        }
                    }
                }

                if (
                    recorder == null ||
                    usedSourceMode == null
                ) {
                    return@withContext AudioRecordingResult(
                        success = false,
                        message =
                            if (
                                sourceMode ==
                                AudioSourceMode.UNPROCESSED
                            ) {
                                "UNPROCESSED ist auf diesem Gerät nicht verfügbar"
                            } else {
                                "AudioRecord konnte nicht initialisiert werden"
                            }
                    )
                }

                val resolvedSourceMode =
                    usedSourceMode

                if (
                    recorder.state !=
                    AudioRecord.STATE_INITIALIZED
                ) {
                    return@withContext AudioRecordingResult(
                        success = false,
                        message = "AudioRecord konnte nicht initialisiert werden"
                    )
                }

                val targetSamples =
                    targetRate *
                        durationSeconds

                val input =
                    ShortArray(2048)
                val pcm =
                    ByteArrayOutputStream(
                        targetSamples * 2
                    )

                val gain =
                    AudioRecordingPolicy
                        .gainFactor(gainDb)

                var groupSum = 0L
                var groupCount = 0
                var outputSamples = 0
                var clipped = 0
                var lastReportedClipped = 0
                var lastProgress = -1

                recorder.startRecording()
                AnalyzerSessionGateway.rawPcm.markDiscontinuity()
                AnalyzerSessionGateway.pcm.markDiscontinuity()

                var inputReported = false
                val analyzerBlock = ShortArray(256)
                var analyzerCount = 0
                var currentDeviceLabel = preferredDeviceLabel

                while (
                    outputSamples < targetSamples &&
                    currentCoroutineContext().isActive
                ) {
                    val count =
                        recorder.read(
                            input,
                            0,
                            input.size,
                            AudioRecord.READ_BLOCKING
                        )

                    if (count < 0) {
                        return@withContext AudioRecordingResult(
                            success = false,
                            clippedSamples = clipped,
                            message = "Audio-Lesefehler: $count"
                        )
                    }

                    if (
                        count > 0 &&
                        !inputReported
                    ) {
                        inputReported = true

                        val routedLabel =
                            recorder.routedDevice
                                ?.let(
                                    AudioInputManager::buildLabel
                                )
                                ?: preferredDeviceLabel

                        currentDeviceLabel = routedLabel
                        withContext(Dispatchers.Main) {
                            onInputResolved(
                                resolvedSourceMode
                                    .displayName(),
                                routedLabel
                            )
                        }
                    }

                    // Match Rolling path: raw PCM must be captured before 8:1 averaging and gain.
                    // Use only the samples actually read, never publish a fabricated 48-kHz rate.
                    if (count > 0 && AnalyzerSessionGateway.rawPcm.activeSubscribers > 0) {
                        val firstSampleNs = System.nanoTime() - count.toLong() * 1_000_000_000L / sourceRate
                        AnalyzerSessionGateway.rawPcm.publish(input, count, sourceRate,
                            resolvedSourceMode.displayName(), currentDeviceLabel, 0, firstSampleNs)
                    }
                    for (index in 0 until count) {
                        groupSum += input[index].toLong()
                        groupCount++

                        if (groupCount == decimation) {
                            val averaged =
                                groupSum.toDouble() /
                                    decimation.toDouble()

                            val amplified =
                                averaged * gain

                            if (
                                amplified > Short.MAX_VALUE ||
                                amplified < Short.MIN_VALUE
                            ) {
                                clipped++
                            }

                            val sample =
                                amplified
                                    .roundToInt()
                                    .coerceIn(
                                        Short.MIN_VALUE.toInt(),
                                        Short.MAX_VALUE.toInt()
                                    )

                            pcm.write(sample and 0xFF)
                            pcm.write((sample shr 8) and 0xFF)
                            if (AnalyzerSessionGateway.pcm.activeSubscribers > 0) {
                                analyzerBlock[analyzerCount++] = sample.toShort()
                                if (analyzerCount == analyzerBlock.size) {
                                    AnalyzerSessionGateway.pcm.publish(analyzerBlock, analyzerCount, targetRate,
                                        resolvedSourceMode.displayName(), currentDeviceLabel, gainDb)
                                    analyzerCount = 0
                                }
                            } else analyzerCount = 0

                            outputSamples++
                            groupSum = 0L
                            groupCount = 0

                            val progress =
                                outputSamples /
                                    targetRate

                            if (
                                progress != lastProgress &&
                                progress <=
                                    durationSeconds
                            ) {
                                lastProgress = progress
                                withContext(Dispatchers.Main) {
                                    onProgressSeconds(progress)
                                }
                            }

                            if (
                                outputSamples >=
                                targetSamples
                            ) {
                                break
                            }
                        }
                    }

                    if (clipped != lastReportedClipped) {
                        lastReportedClipped = clipped
                        withContext(Dispatchers.Main) {
                            onClippingChanged(clipped)
                        }
                    }
                }

                if (analyzerCount > 0) {
                    AnalyzerSessionGateway.pcm.publish(analyzerBlock, analyzerCount, targetRate,
                        resolvedSourceMode.displayName(), currentDeviceLabel, gainDb)
                }
                if (!currentCoroutineContext().isActive) {
                    return@withContext AudioRecordingResult(
                        success = false,
                        clippedSamples = clipped,
                        message = "Aufnahme abgebrochen"
                    )
                }

                val raw =
                    pcm.toByteArray()
                val header =
                    WavPcm16.buildHeader(
                        sampleRateHz = targetRate,
                        channels = 1,
                        bitsPerSample = 16,
                        dataBytes = raw.size
                    )

                val directory =
                    File(
                        context.cacheDir,
                        "audio_recording_temp"
                    ).apply {
                        mkdirs()
                    }

                val file =
                    File(
                        directory,
                        "brumm_${System.currentTimeMillis()}.wav"
                    )

                file.outputStream().use {
                    it.write(header)
                    it.write(raw)
                }

                AudioRecordingResult(
                    success = true,
                    file = file,
                    clippedSamples = clipped,
                    sourceLabel =
                        resolvedSourceMode.displayName(),
                    deviceLabel =
                        recorder.routedDevice
                            ?.let(AudioInputManager::buildLabel)
                            ?: preferredDeviceLabel,
                    message =
                        if (clipped > 0) {
                            "Aufnahme fertig · $clipped übersteuerte Samples"
                        } else {
                            "Aufnahme fertig · keine Übersteuerung"
                        }
                )
            } catch (error: Exception) {
                AudioRecordingResult(
                    success = false,
                    message =
                        "Aufnahmefehler: " +
                            (
                                error.message
                                    ?: error.javaClass.simpleName
                                )
                )
            } finally {
                runCatching {
                    if (
                        recorder?.recordingState ==
                        AudioRecord.RECORDSTATE_RECORDING
                    ) {
                        recorder.stop()
                    }
                }
                runCatching {
                    recorder?.release()
                }
                captureLease.close()
            }
        }
}
