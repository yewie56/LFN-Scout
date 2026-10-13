
package de.brummbeobachter.app

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import org.json.JSONObject
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class AudioArchiveEntry(
    val recordingId: String,
    val wavFileName: String,
    val recordedAt: String,
    val gainDb: Int,
    val audioSource: String,
    val clippedSamples: Int,
    val comment: String?,
    val uploadedAtEpochMs: Long?,
    val storagePath: String?,
    val lastError: String?,
    val contentUri: String?,
    val legacyPublicPath: String?,
    val origin: String = "OBSERVER",
    val rollingBuffer: Boolean = false,
    val durationSeconds: Int = 0,
    val companionJsonName: String? = null,
    val companionJsonUri: String? = null,
    val companionJsonLegacyPath: String? = null,
    val manifestJsonName: String? = null,
    val manifestJsonUri: String? = null,
    val manifestJsonLegacyPath: String? = null
) {
    val isUploaded: Boolean
        get() = uploadedAtEpochMs != null

    fun formattedRecordedAt(): String =
        runCatching {
            OffsetDateTime
                .parse(recordedAt)
                .atZoneSameInstant(
                    ZoneId.systemDefault()
                )
                .format(
                    DateTimeFormatter
                        .ofPattern("dd.MM.yyyy HH:mm:ss")
                )
        }.getOrDefault(recordedAt)
}

object AudioArchive {

    const val PUBLIC_RELATIVE_PATH =
        AudioArchiveCompatibilityPolicy.CURRENT_RELATIVE_PATH

    const val PUBLIC_DISPLAY_PATH =
        "Download/LFN-Scout/"

    private const val METADATA_DIRECTORY_NAME =
        "audio_archive_meta"

    private const val LEGACY_DIRECTORY_NAME =
        "audio_recordings"

    private const val PREFS_NAME =
        "audio_archive_migration"

    const val LEGACY_CLEANUP_DONE =
        "legacy_audio_recordings_wav_cleanup_done_v0445"

    const val MAX_COMMENT_LENGTH =
        2000

    const val RETENTION_MS =
        24L * 60L * 60L * 1000L

    fun displayDirectoryPath(
        context: Context
    ): String {
        return if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q
        ) {
            PUBLIC_DISPLAY_PATH
        } else {
            File(
                Environment
                    .getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    ),
                "LFN-Scout"
            ).absolutePath
        }
    }

    fun deleteLegacyWavsOnce(
        context: Context
    ): Int {
        val appContext =
            context.applicationContext

        val prefs =
            appContext.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        if (
            prefs.getBoolean(
                LEGACY_CLEANUP_DONE,
                false
            )
        ) {
            DebugLogger.log(
                appContext,
                "AUDIO_LEGACY_CLEANUP_SKIP",
                "reason=already_done"
            )
            return 0
        }

        val legacyDirectory =
            File(
                appContext.filesDir,
                LEGACY_DIRECTORY_NAME
            )

        DebugLogger.log(
            appContext,
            "AUDIO_LEGACY_CLEANUP_START",
            "path=${legacyDirectory.absolutePath}"
        )

        var deleted = 0
        var failed = 0

        legacyDirectory
            .listFiles()
            ?.filter {
                it.isFile &&
                    it.extension.equals(
                        "wav",
                        ignoreCase = true
                    )
            }
            ?.forEach {
                file ->
                if (file.delete()) {
                    deleted++
                    DebugLogger.log(
                        appContext,
                        "AUDIO_LEGACY_WAV_DELETED",
                        "name=${file.name}"
                    )
                } else {
                    failed++
                    DebugLogger.log(
                        appContext,
                        "AUDIO_LEGACY_WAV_DELETE_FAILED",
                        "name=${file.name}"
                    )
                }
            }

        prefs.edit()
            .putBoolean(
                LEGACY_CLEANUP_DONE,
                true
            )
            .apply()

        DebugLogger.log(
            appContext,
            "AUDIO_LEGACY_CLEANUP_DONE",
            "deleted=$deleted failed=$failed"
        )

        return deleted
    }

    fun create(
        context: Context,
        recordingId: String,
        wavFile: File,
        targetDisplayName: String,
        recordedAt: String,
        gainDb: Int,
        audioSource: String,
        clippedSamples: Int
    ): AudioArchiveEntry {
        val appContext =
            context.applicationContext

        DebugLogger.log(
            appContext,
            "AUDIO_STORAGE_INIT",
            "path=${displayDirectoryPath(appContext)}"
        )

        val publication =
            publishWav(
                appContext,
                wavFile,
                targetDisplayName
            )

        val entry =
            AudioArchiveEntry(
                recordingId =
                    recordingId,
                wavFileName =
                    publication.displayName,
                recordedAt =
                    recordedAt,
                gainDb =
                    gainDb.coerceIn(
                        0,
                        50
                    ),
                audioSource =
                    if (
                        audioSource == "MIC"
                    ) {
                        "MIC"
                    } else {
                        "UNP"
                    },
                clippedSamples =
                    clippedSamples
                        .coerceAtLeast(0),
                comment = null,
                uploadedAtEpochMs = null,
                storagePath = null,
                lastError = null,
                contentUri =
                    publication.contentUri,
                legacyPublicPath =
                    publication.legacyPublicPath,
                origin = "OBSERVER"
            )

        write(
            appContext,
            entry
        )

        if (
            wavFile.exists() &&
            !wavFile.delete()
        ) {
            DebugLogger.log(
                appContext,
                "AUDIO_TEMP_DELETE_FAILED",
                "name=${wavFile.name}"
            )
        }

        DebugLogger.log(
            appContext,
            "AUDIO_WAV_SAVED",
            "name=${entry.wavFileName} path=${displayDirectoryPath(appContext)}"
        )

        return entry
    }

    fun readWavBytes(
        context: Context,
        entry: AudioArchiveEntry
    ): ByteArray? {
        val appContext = context.applicationContext

        val direct = runCatching {
            when {
                !entry.contentUri.isNullOrBlank() -> {
                    appContext.contentResolver
                        .openInputStream(Uri.parse(entry.contentUri))
                        ?.use { it.readBytes() }
                }
                !entry.legacyPublicPath.isNullOrBlank() -> {
                    File(entry.legacyPublicPath)
                        .takeIf { it.exists() }
                        ?.readBytes()
                }
                else -> null
            }
        }.getOrNull()

        if (direct != null) return direct

        val fallbackLookup = findPublishedWavByName(appContext, entry.wavFileName)
        val fallbackUri = fallbackLookup.uri
        if (fallbackUri != null) {
            val bytes = runCatching {
                appContext.contentResolver
                    .openInputStream(fallbackUri)
                    ?.use { it.readBytes() }
            }.getOrNull()
            if (bytes != null) {
                DebugLogger.log(
                    appContext,
                    "AUDIO_WAV_LEGACY_FALLBACK_OK",
                    "recording_id=${entry.recordingId} name=${entry.wavFileName} uri=$fallbackUri"
                )
                return bytes
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            for (directoryName in listOf("LFN-Scout", "BrummBeobachter")) {
                val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "$directoryName/${entry.wavFileName}"
                )
                if (file.exists()) {
                    return runCatching { file.readBytes() }.getOrNull()
                }
            }
        }

        DebugLogger.log(
            appContext,
            "AUDIO_WAV_READ_MISSING",
            "recording_id=${entry.recordingId} name=${entry.wavFileName}"
        )
        return null
    }

    fun updateComment(
        context: Context,
        recordingId: String,
        comment: String?
    ): AudioArchiveEntry? {
        val current =
            read(
                context,
                recordingId
            ) ?: return null

        val normalized =
            comment
                ?.take(
                    MAX_COMMENT_LENGTH
                )
                ?.takeIf {
                    it.isNotBlank()
                }

        val updated =
            current.copy(
                comment =
                    normalized
            )

        write(
            context,
            updated
        )

        return updated
    }

    fun createRolling(
        context: Context,
        recordingId: String,
        wavFile: File,
        targetDisplayName: String,
        recordedAt: String,
        gainDb: Int,
        audioSource: String,
        clippedSamples: Int,
        durationSeconds: Int,
        startTime: String,
        endTime: String,
        deviceLabel: String,
        ratingTag: String,
        comment: String? = null,
        origin: String = "OBSERVER",
        isRolling: Boolean = true,
        monMetadata: JSONObject? = null
    ): AudioArchiveEntry {
        val publication = publishWav(context.applicationContext, wavFile, targetDisplayName)
        val jsonName = targetDisplayName.substringBeforeLast('.') + ".json"
        val entry = AudioArchiveEntry(
            recordingId = recordingId,
            wavFileName = publication.displayName,
            recordedAt = recordedAt,
            gainDb = gainDb.coerceIn(0, 50),
            audioSource = if (audioSource == "MIC") "MIC" else "UNP",
            clippedSamples = clippedSamples.coerceAtLeast(0),
            comment = comment?.take(MAX_COMMENT_LENGTH)?.takeIf { it.isNotBlank() },
            uploadedAtEpochMs = null,
            storagePath = null,
            lastError = null,
            contentUri = publication.contentUri,
            legacyPublicPath = publication.legacyPublicPath,
            origin = origin,
            rollingBuffer = isRolling,
            durationSeconds = durationSeconds.coerceAtLeast(0),
            companionJsonName = jsonName
        )
        val jsonPublication = writeRollingCompanionJson(context, entry, startTime, endTime, deviceLabel, ratingTag, monMetadata)
        val finalEntry = entry.copy(
            companionJsonUri = jsonPublication.contentUri,
            companionJsonLegacyPath = jsonPublication.legacyPublicPath
        )
        write(context, finalEntry)
        if (wavFile.exists()) wavFile.delete()
        DebugLogger.log(context.applicationContext, "ROLLING_SAVED", "name=${finalEntry.wavFileName} duration_s=${finalEntry.durationSeconds}")
        return finalEntry
    }

    /**
     * Analyzer saves intentionally use the SAME public Download/LFN-Scout/ root as observer audio.
     * No analyzer/audio subdirectory is created: this keeps the established upload/24-h retention
     * path compatible during the transition. The WAV is PRE-GAIN, therefore gainDb is 0 here;
     * the Analyzer display gain remains documented in the statistics sidecar.
     */
    fun createAnalyzer(
        context: Context,
        recordingId: String,
        wavFile: File,
        targetDisplayName: String,
        recordedAt: String,
        audioSource: String,
        clippedSamples: Int,
        durationSeconds: Int,
        comment: String?,
        statisticsName: String,
        statisticsBytes: ByteArray,
        manifestName: String,
        manifestBytes: ByteArray
    ): AudioArchiveEntry {
        val appContext = context.applicationContext
        val wavPublication = publishWav(appContext, wavFile, targetDisplayName)
        var statisticsPublication: PublishedWav? = null
        var manifestPublication: PublishedWav? = null
        try {
            val statTemp = File(appContext.cacheDir, statisticsName).apply { writeBytes(statisticsBytes) }
            statisticsPublication = publishDownloadFile(appContext, statTemp, statisticsName, "application/json")
            statTemp.delete()
            val manifestTemp = File(appContext.cacheDir, manifestName).apply { writeBytes(manifestBytes) }
            manifestPublication = publishDownloadFile(appContext, manifestTemp, manifestName, "application/json")
            manifestTemp.delete()

            val entry = AudioArchiveEntry(
                recordingId = recordingId,
                wavFileName = wavPublication.displayName,
                recordedAt = recordedAt,
                gainDb = 0,
                audioSource = if (audioSource == "MIC") "MIC" else "UNP",
                clippedSamples = clippedSamples.coerceAtLeast(0),
                comment = comment?.take(MAX_COMMENT_LENGTH)?.takeIf { it.isNotBlank() },
                uploadedAtEpochMs = null,
                storagePath = null,
                lastError = null,
                contentUri = wavPublication.contentUri,
                legacyPublicPath = wavPublication.legacyPublicPath,
                origin = "ANALYZER",
                rollingBuffer = false,
                durationSeconds = durationSeconds.coerceAtLeast(0),
                companionJsonName = statisticsName,
                companionJsonUri = statisticsPublication.contentUri,
                companionJsonLegacyPath = statisticsPublication.legacyPublicPath,
                manifestJsonName = manifestName,
                manifestJsonUri = manifestPublication.contentUri,
                manifestJsonLegacyPath = manifestPublication.legacyPublicPath
            )
            write(appContext, entry)
            if (wavFile.exists()) wavFile.delete()
            DebugLogger.log(appContext, "ANALYZER_AUDIO_ARCHIVED",
                "recording_id=$recordingId wav=${entry.wavFileName} root=${displayDirectoryPath(appContext)}")
            return entry
        } catch (e: Exception) {
            val partial = AudioArchiveEntry(
                recordingId = recordingId, wavFileName = wavPublication.displayName, recordedAt = recordedAt,
                gainDb = 0, audioSource = if (audioSource == "MIC") "MIC" else "UNP", clippedSamples = clippedSamples,
                comment = null, uploadedAtEpochMs = null, storagePath = null, lastError = null,
                contentUri = wavPublication.contentUri, legacyPublicPath = wavPublication.legacyPublicPath, origin = "ANALYZER",
                companionJsonName = statisticsName, companionJsonUri = statisticsPublication?.contentUri,
                companionJsonLegacyPath = statisticsPublication?.legacyPublicPath,
                manifestJsonName = manifestName, manifestJsonUri = manifestPublication?.contentUri,
                manifestJsonLegacyPath = manifestPublication?.legacyPublicPath
            )
            deletePublishedWav(appContext, partial)
            deleteCompanionJson(appContext, partial)
            deleteManifestJson(appContext, partial)
            throw e
        }
    }

    fun updateRollingComment(context: Context, recordingId: String, comment: String?): AudioArchiveEntry? {
        val current = read(context, recordingId) ?: return null
        if (!current.rollingBuffer) return updateComment(context, recordingId, comment)
        val updated = current.copy(comment = comment?.take(MAX_COMMENT_LENGTH)?.takeIf { it.isNotBlank() })
        write(context, updated)
        rewriteRollingCompanionJson(context, updated)
        return updated
    }

    fun deleteRolling(context: Context, entry: AudioArchiveEntry): Boolean {
        if (!entry.rollingBuffer) return false
        val wavOk = deletePublishedWav(context.applicationContext, entry)
        val jsonOk = deleteCompanionJson(context.applicationContext, entry)
        val meta = metadataFile(context, entry.recordingId)
        val metaOk = !meta.exists() || meta.delete()
        DebugLogger.log(context.applicationContext, "ROLLING_DELETE", "recording_id=${entry.recordingId} wav=$wavOk json=$jsonOk meta=$metaOk")
        return wavOk && jsonOk && metaOk
    }

    private fun rewriteRollingCompanionJson(context: Context, entry: AudioArchiveEntry) {
        val original = readCompanionJson(context, entry) ?: JSONObject()
        original.put("comment", entry.comment ?: JSONObject.NULL)
        replaceCompanionJson(context, entry, original.toString(2).toByteArray(Charsets.UTF_8))
    }

    private fun writeRollingCompanionJson(
        context: Context,
        entry: AudioArchiveEntry,
        startTime: String,
        endTime: String,
        deviceLabel: String,
        ratingTag: String,
        monMetadata: JSONObject? = null
    ): PublishedWav {
        val json = JSONObject().apply {
            put("rolling_buffer", entry.rollingBuffer)
            put("recording_id", entry.recordingId)
            put("wav_file_name", entry.wavFileName)
            put("start_time", startTime)
            put("end_time", endTime)
            put("duration_seconds", entry.durationSeconds)
            put("sample_rate_hz", if (monMetadata != null) 48000 else RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ)
            put("channels", 1)
            put("bits_per_sample", 16)
            put("gain_db", entry.gainDb)
            put("audio_source", entry.audioSource)
            put("audio_device", deviceLabel)
            put("rating", ratingTag)
            put("clipped_samples", entry.clippedSamples)
            put("comment", entry.comment ?: JSONObject.NULL)
            if (monMetadata != null) put("mon_audio", monMetadata)
        }
        val temp = File(context.cacheDir, entry.companionJsonName ?: "rolling.json")
        temp.writeText(json.toString(2), Charsets.UTF_8)
        return publishDownloadFile(context.applicationContext, temp, entry.companionJsonName ?: "rolling.json", "application/json").also { temp.delete() }
    }

    fun readStatisticsCompanion(context: Context, entry: AudioArchiveEntry): JSONObject? =
        readCompanionJson(context, entry)

    private fun readCompanionJson(context: Context, entry: AudioArchiveEntry): JSONObject? = try {
        val bytes = when {
            !entry.companionJsonUri.isNullOrBlank() -> context.contentResolver.openInputStream(Uri.parse(entry.companionJsonUri))?.use { it.readBytes() }
            !entry.companionJsonLegacyPath.isNullOrBlank() -> File(entry.companionJsonLegacyPath).takeIf { it.exists() }?.readBytes()
            else -> null
        }
        bytes?.let { JSONObject(String(it, Charsets.UTF_8)) }
    } catch (_: Exception) { null }

    private fun replaceCompanionJson(context: Context, entry: AudioArchiveEntry, bytes: ByteArray) {
        try {
            when {
                !entry.companionJsonUri.isNullOrBlank() -> context.contentResolver.openOutputStream(Uri.parse(entry.companionJsonUri), "wt")?.use { it.write(bytes) }
                !entry.companionJsonLegacyPath.isNullOrBlank() -> File(entry.companionJsonLegacyPath).writeBytes(bytes)
            }
        } catch (e: Exception) {
            DebugLogger.log(context.applicationContext, "ROLLING_JSON_UPDATE_FAILED", "recording_id=${entry.recordingId} error=${e.javaClass.simpleName}")
        }
    }

    private fun deleteCompanionJson(context: Context, entry: AudioArchiveEntry): Boolean = try {
        when {
            !entry.companionJsonUri.isNullOrBlank() -> context.contentResolver.delete(Uri.parse(entry.companionJsonUri), null, null) >= 0
            !entry.companionJsonLegacyPath.isNullOrBlank() -> File(entry.companionJsonLegacyPath).let { !it.exists() || it.delete() }
            else -> true
        }
    } catch (_: Exception) { false }

    private fun deleteManifestJson(context: Context, entry: AudioArchiveEntry): Boolean = try {
        when {
            !entry.manifestJsonUri.isNullOrBlank() -> context.contentResolver.delete(Uri.parse(entry.manifestJsonUri), null, null) >= 0
            !entry.manifestJsonLegacyPath.isNullOrBlank() -> File(entry.manifestJsonLegacyPath).let { !it.exists() || it.delete() }
            else -> true
        }
    } catch (_: Exception) { false }

    private fun publishDownloadFile(context: Context, sourceFile: File, targetDisplayName: String, mimeType: String): PublishedWav {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, targetDisplayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, PUBLIC_RELATIVE_PATH)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Downloads-Eintrag konnte nicht angelegt werden")
            try {
                context.contentResolver.openOutputStream(uri)?.use { out -> sourceFile.inputStream().use { it.copyTo(out) } }
                    ?: error("Downloads-Datei konnte nicht geöffnet werden")
                context.contentResolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
                return PublishedWav(targetDisplayName, uri.toString(), null)
            } catch (e: Exception) {
                runCatching { context.contentResolver.delete(uri, null, null) }
                throw e
            }
        }
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LFN-Scout").apply { mkdirs() }
        val destination = File(directory, targetDisplayName)
        sourceFile.copyTo(destination, overwrite = true)
        return PublishedWav(destination.name, null, destination.absolutePath)
    }

    fun markUploaded(
        context: Context,
        recordingId: String,
        storagePath: String?,
        nowEpochMs: Long =
            System.currentTimeMillis()
    ): AudioArchiveEntry? {
        val current =
            read(
                context,
                recordingId
            ) ?: return null

        val updated =
            current.copy(
                uploadedAtEpochMs =
                    nowEpochMs,
                storagePath =
                    storagePath,
                lastError = null
            )

        write(
            context,
            updated
        )

        return updated
    }

    fun markFailed(
        context: Context,
        recordingId: String,
        error: String
    ): AudioArchiveEntry? {
        val current =
            read(
                context,
                recordingId
            ) ?: return null

        val updated =
            current.copy(
                lastError =
                    error.take(1000)
            )

        write(
            context,
            updated
        )

        return updated
    }

    fun list(
        context: Context
    ): List<AudioArchiveEntry> =
        metadataDirectory(
            context
        )
            .listFiles {
                    file ->
                file.isFile &&
                    file.extension.equals(
                        "json",
                        ignoreCase = true
                    )
            }
            ?.mapNotNull {
                file ->
                runCatching {
                    fromJson(
                        file.readText(
                            Charsets.UTF_8
                        )
                    )
                }.getOrNull()
            }
            ?.sortedByDescending {
                entry ->
                runCatching {
                    OffsetDateTime
                        .parse(
                            entry.recordedAt
                        )
                        .toInstant()
                        .toEpochMilli()
                }.getOrDefault(
                    0L
                )
            }
            ?: emptyList()

    fun cleanup(
        context: Context,
        nowEpochMs: Long =
            System.currentTimeMillis()
    ): Int {
        val appContext =
            context.applicationContext

        var deleted = 0
        var retained = 0

        list(appContext)
            .forEach {
                entry ->
                if (entry.rollingBuffer) {
                    retained++
                    return@forEach
                }
                val uploadedAt =
                    entry.uploadedAtEpochMs
                        ?: run {
                            retained++
                            return@forEach
                        }

                if (
                    nowEpochMs -
                    uploadedAt <
                    RETENTION_MS
                ) {
                    retained++
                    return@forEach
                }

                val wavDeleted =
                    deletePublishedWav(
                        appContext,
                        entry
                    )

                val companionDeleted = deleteCompanionJson(appContext, entry)
                val manifestDeleted = deleteManifestJson(appContext, entry)

                val metadata =
                    metadataFile(
                        appContext,
                        entry.recordingId
                    )

                val metadataDeleted =
                    !metadata.exists() ||
                        metadata.delete()

                if (
                    wavDeleted && companionDeleted && manifestDeleted &&
                    metadataDeleted
                ) {
                    deleted++
                    DebugLogger.log(
                        appContext,
                        "AUDIO_ARCHIVE_DELETED",
                        "recording_id=${entry.recordingId}"
                    )
                } else {
                    retained++
                    DebugLogger.log(
                        appContext,
                        "AUDIO_ARCHIVE_DELETE_FAILED",
                        "recording_id=${entry.recordingId}"
                    )
                }
            }

        DebugLogger.log(
            appContext,
            "AUDIO_ARCHIVE_CLEANUP",
            "deleted=$deleted retained=$retained"
        )

        return deleted
    }

    private data class PublishedWav(
        val displayName: String,
        val contentUri: String?,
        val legacyPublicPath: String?
    )

    private fun publishWav(
        context: Context,
        sourceFile: File,
        targetDisplayName: String
    ): PublishedWav {
        return if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q
        ) {
            val values =
                ContentValues().apply {
                    put(
                        MediaStore
                            .MediaColumns
                            .DISPLAY_NAME,
                        targetDisplayName
                    )
                    put(
                        MediaStore
                            .MediaColumns
                            .MIME_TYPE,
                        "audio/wav"
                    )
                    put(
                        MediaStore
                            .MediaColumns
                            .RELATIVE_PATH,
                        PUBLIC_RELATIVE_PATH
                    )
                    put(
                        MediaStore
                            .MediaColumns
                            .IS_PENDING,
                        1
                    )
                }

            val uri =
                context
                    .contentResolver
                    .insert(
                        MediaStore
                            .Downloads
                            .EXTERNAL_CONTENT_URI,
                        values
                    )
                    ?: error(
                        "Downloads-Eintrag konnte nicht angelegt werden"
                    )

            try {
                context
                    .contentResolver
                    .openOutputStream(
                        uri
                    )
                    ?.use {
                        output ->
                        sourceFile
                            .inputStream()
                            .use {
                                input ->
                                input.copyTo(
                                    output
                                )
                            }
                    }
                    ?: error(
                        "Downloads-Datei konnte nicht geöffnet werden"
                    )

                val readyValues =
                    ContentValues().apply {
                        put(
                            MediaStore
                                .MediaColumns
                                .IS_PENDING,
                            0
                        )
                    }

                context
                    .contentResolver
                    .update(
                        uri,
                        readyValues,
                        null,
                        null
                    )

                PublishedWav(
                    displayName =
                        targetDisplayName,
                    contentUri =
                        uri.toString(),
                    legacyPublicPath =
                        null
                )
            } catch (
                error: Exception
            ) {
                runCatching {
                    context
                        .contentResolver
                        .delete(
                            uri,
                            null,
                            null
                        )
                }
                throw error
            }
        } else {
            val directory =
                File(
                    Environment
                        .getExternalStoragePublicDirectory(
                            Environment
                                .DIRECTORY_DOWNLOADS
                        ),
                    "LFN-Scout"
                ).apply {
                    mkdirs()
                }

            val destination =
                File(
                    directory,
                    targetDisplayName
                )

            sourceFile.copyTo(
                destination,
                overwrite = true
            )

            PublishedWav(
                displayName =
                    destination.name,
                contentUri = null,
                legacyPublicPath =
                    destination.absolutePath
            )
        }
    }

    private fun deletePublishedWav(
        context: Context,
        entry: AudioArchiveEntry
    ): Boolean {
        val result = deletePublishedWavResult(context, entry)
        return AudioArchiveCompatibilityPolicy.shouldRemoveMetadataAfterDelete(result)
    }

    private fun deletePublishedWavResult(
        context: Context,
        entry: AudioArchiveEntry
    ): AudioArchiveCompatibilityPolicy.DeleteResult {
        var realFailure = false

        if (!entry.contentUri.isNullOrBlank()) {
            try {
                val deleted = context.contentResolver.delete(Uri.parse(entry.contentUri), null, null)
                if (deleted > 0) {
                    return AudioArchiveCompatibilityPolicy.DeleteResult.DELETED
                }
            } catch (error: Exception) {
                realFailure = true
                DebugLogger.log(
                    context,
                    "AUDIO_ARCHIVE_DELETE_URI_EXCEPTION",
                    "recording_id=${entry.recordingId} error=${error.javaClass.simpleName}"
                )
            }
        }

        if (!entry.legacyPublicPath.isNullOrBlank()) {
            try {
                val file = File(entry.legacyPublicPath)
                if (file.exists()) {
                    if (file.delete()) return AudioArchiveCompatibilityPolicy.DeleteResult.DELETED
                    realFailure = true
                }
            } catch (error: Exception) {
                realFailure = true
            }
        }

        val fallbackLookup = findPublishedWavByName(context, entry.wavFileName)
        val fallbackUri = fallbackLookup.uri
        if (fallbackUri != null) {
            return try {
                if (context.contentResolver.delete(fallbackUri, null, null) > 0) {
                    AudioArchiveCompatibilityPolicy.DeleteResult.DELETED
                } else {
                    AudioArchiveCompatibilityPolicy.DeleteResult.FAILED
                }
            } catch (error: Exception) {
                DebugLogger.log(
                    context,
                    "AUDIO_ARCHIVE_DELETE_FALLBACK_EXCEPTION",
                    "recording_id=${entry.recordingId} error=${error.javaClass.simpleName}"
                )
                AudioArchiveCompatibilityPolicy.DeleteResult.FAILED
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            for (directoryName in listOf("LFN-Scout", "BrummBeobachter")) {
                val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "$directoryName/${entry.wavFileName}"
                )
                if (file.exists()) {
                    return if (file.delete()) {
                        AudioArchiveCompatibilityPolicy.DeleteResult.DELETED
                    } else {
                        AudioArchiveCompatibilityPolicy.DeleteResult.FAILED
                    }
                }
            }
        }

        return if (realFailure && !fallbackLookup.querySucceeded) {
            AudioArchiveCompatibilityPolicy.DeleteResult.FAILED
        } else {
            AudioArchiveCompatibilityPolicy.DeleteResult.ALREADY_ABSENT
        }
    }

    private data class PublishedWavLookup(
        val uri: Uri?,
        val querySucceeded: Boolean
    )

    private fun findPublishedWavByName(
        context: Context,
        displayName: String
    ): PublishedWavLookup {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return PublishedWavLookup(null, true)
        }

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.RELATIVE_PATH
        )
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME}=?"
        val args = arrayOf(displayName)

        return try {
            val uri = context.contentResolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                args,
                null
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
                while (cursor.moveToNext()) {
                    val relativePath = cursor.getString(pathIndex) ?: continue
                    if (relativePath in AudioArchiveCompatibilityPolicy.relativePaths()) {
                        val id = cursor.getLong(idIndex)
                        return@use Uri.withAppendedPath(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            id.toString()
                        )
                    }
                }
                null
            }
            PublishedWavLookup(uri, true)
        } catch (error: Exception) {
            DebugLogger.log(
                context,
                "AUDIO_ARCHIVE_MEDIASTORE_LOOKUP_FAILED",
                "name=$displayName error=${error.javaClass.simpleName}"
            )
            PublishedWavLookup(null, false)
        }
    }

    private fun metadataDirectory(
        context: Context
    ): File =
        File(
            context
                .applicationContext
                .filesDir,
            METADATA_DIRECTORY_NAME
        ).apply {
            mkdirs()
        }

    private fun read(
        context: Context,
        recordingId: String
    ): AudioArchiveEntry? {
        val file =
            metadataFile(
                context,
                recordingId
            )

        if (!file.exists()) {
            return null
        }

        return runCatching {
            fromJson(
                file.readText(
                    Charsets.UTF_8
                )
            )
        }.getOrNull()
    }

    private fun write(
        context: Context,
        entry: AudioArchiveEntry
    ) {
        val target =
            metadataFile(
                context,
                entry.recordingId
            )

        val temporary =
            File(
                target.parentFile,
                target.name + ".tmp"
            )

        temporary.writeText(
            toJson(entry)
                .toString(2),
            Charsets.UTF_8
        )

        if (
            target.exists() &&
            !target.delete()
        ) {
            temporary.delete()
            error(
                "Audio-Metadatei konnte nicht ersetzt werden"
            )
        }

        if (
            !temporary.renameTo(
                target
            )
        ) {
            temporary.delete()
            error(
                "Audio-Metadatei konnte nicht gespeichert werden"
            )
        }
    }

    private fun metadataFile(
        context: Context,
        recordingId: String
    ): File {
        val safeId =
            recordingId.replace(
                Regex(
                    "[^A-Za-z0-9_-]"
                ),
                "_"
            )

        return File(
            metadataDirectory(
                context
            ),
            "$safeId.json"
        )
    }

    private fun toJson(
        entry: AudioArchiveEntry
    ): JSONObject =
        JSONObject().apply {
            put(
                "recording_id",
                entry.recordingId
            )
            put(
                "wav_file_name",
                entry.wavFileName
            )
            put(
                "recorded_at",
                entry.recordedAt
            )
            put(
                "gain_db",
                entry.gainDb
            )
            put(
                "audio_source",
                entry.audioSource
            )
            put(
                "clipped_samples",
                entry.clippedSamples
            )

            if (
                entry.comment == null
            ) {
                put(
                    "comment",
                    JSONObject.NULL
                )
            } else {
                put(
                    "comment",
                    entry.comment
                )
            }

            if (
                entry.uploadedAtEpochMs ==
                null
            ) {
                put(
                    "uploaded_at_epoch_ms",
                    JSONObject.NULL
                )
            } else {
                put(
                    "uploaded_at_epoch_ms",
                    entry.uploadedAtEpochMs
                )
            }

            if (
                entry.storagePath == null
            ) {
                put(
                    "storage_path",
                    JSONObject.NULL
                )
            } else {
                put(
                    "storage_path",
                    entry.storagePath
                )
            }

            if (
                entry.lastError == null
            ) {
                put(
                    "last_error",
                    JSONObject.NULL
                )
            } else {
                put(
                    "last_error",
                    entry.lastError
                )
            }

            if (
                entry.contentUri == null
            ) {
                put(
                    "content_uri",
                    JSONObject.NULL
                )
            } else {
                put(
                    "content_uri",
                    entry.contentUri
                )
            }

            if (
                entry.legacyPublicPath ==
                null
            ) {
                put(
                    "legacy_public_path",
                    JSONObject.NULL
                )
            } else {
                put(
                    "legacy_public_path",
                    entry.legacyPublicPath
                )
            }
            put("origin", entry.origin)
            put("rolling_buffer", entry.rollingBuffer)
            put("duration_seconds", entry.durationSeconds)
            if (entry.companionJsonName == null) put("companion_json_name", JSONObject.NULL) else put("companion_json_name", entry.companionJsonName)
            if (entry.companionJsonUri == null) put("companion_json_uri", JSONObject.NULL) else put("companion_json_uri", entry.companionJsonUri)
            if (entry.companionJsonLegacyPath == null) put("companion_json_legacy_path", JSONObject.NULL) else put("companion_json_legacy_path", entry.companionJsonLegacyPath)
            if (entry.manifestJsonName == null) put("manifest_json_name", JSONObject.NULL) else put("manifest_json_name", entry.manifestJsonName)
            if (entry.manifestJsonUri == null) put("manifest_json_uri", JSONObject.NULL) else put("manifest_json_uri", entry.manifestJsonUri)
            if (entry.manifestJsonLegacyPath == null) put("manifest_json_legacy_path", JSONObject.NULL) else put("manifest_json_legacy_path", entry.manifestJsonLegacyPath)
        }

    private fun fromJson(
        text: String
    ): AudioArchiveEntry {
        val json =
            JSONObject(
                text
            )

        fun optionalString(
            key: String
        ): String? =
            if (
                json.isNull(
                    key
                )
            ) {
                null
            } else {
                json.optString(
                    key,
                    ""
                ).takeIf {
                    it.isNotBlank()
                }
            }

        return AudioArchiveEntry(
            recordingId =
                json.getString(
                    "recording_id"
                ),
            wavFileName =
                json.getString(
                    "wav_file_name"
                ),
            recordedAt =
                json.getString(
                    "recorded_at"
                ),
            gainDb =
                json.optInt(
                    "gain_db",
                    0
                ),
            audioSource =
                json.optString(
                    "audio_source",
                    "UNP"
                ),
            clippedSamples =
                json.optInt(
                    "clipped_samples",
                    0
                ),
            comment =
                optionalString(
                    "comment"
                ),
            uploadedAtEpochMs =
                if (
                    json.isNull(
                        "uploaded_at_epoch_ms"
                    )
                ) {
                    null
                } else {
                    json.optLong(
                        "uploaded_at_epoch_ms"
                    )
                },
            storagePath =
                optionalString(
                    "storage_path"
                ),
            lastError =
                optionalString(
                    "last_error"
                ),
            contentUri =
                optionalString(
                    "content_uri"
                ),
            legacyPublicPath =
                optionalString(
                    "legacy_public_path"
                ),
            origin = json.optString("origin", "OBSERVER"),
            rollingBuffer = json.optBoolean("rolling_buffer", false),
            durationSeconds = json.optInt("duration_seconds", 0),
            companionJsonName = optionalString("companion_json_name"),
            companionJsonUri = optionalString("companion_json_uri"),
            companionJsonLegacyPath = optionalString("companion_json_legacy_path"),
            manifestJsonName = optionalString("manifest_json_name"),
            manifestJsonUri = optionalString("manifest_json_uri"),
            manifestJsonLegacyPath = optionalString("manifest_json_legacy_path")
        )
    }
}
