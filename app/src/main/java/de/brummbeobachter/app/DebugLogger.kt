package de.brummbeobachter.app

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DebugLogger {
    private const val FILE_NAME = "lfn_scout_debug.log"
    private const val MAX_LINES = 2000

    private val timestampFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    private val displayTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

    @Synchronized
    fun log(
        context: Context,
        event: String,
        details: String = ""
    ) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            val timestamp = Instant.now()
                .atZone(ZoneId.systemDefault())
                .format(timestampFormatter)

            val cleanDetails = details
                .replace('\n', ' ')
                .replace('\r', ' ')
                .trim()

            val line =
                if (cleanDetails.isBlank()) {
                    "$timestamp $event"
                } else {
                    "$timestamp $event $cleanDetails"
                }

            val lines =
                if (file.exists()) {
                    file.readLines(Charsets.UTF_8).toMutableList()
                } else {
                    mutableListOf()
                }

            lines.add(line)

            file.writeText(
                lines.takeLast(MAX_LINES).joinToString("\n") + "\n",
                Charsets.UTF_8
            )
        } catch (_: Exception) {
            // Debug-Logging darf die eigentliche App-Funktion nie blockieren.
        }
    }

    @Synchronized
    fun readLines(context: Context): List<String> {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) {
                file.readLines(Charsets.UTF_8).takeLast(MAX_LINES)
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun clear(context: Context) {
        try {
            File(context.filesDir, FILE_NAME).writeText("", Charsets.UTF_8)
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun saveCopy(
        context: Context,
        developerModeEnabled: Boolean
    ): String {
        val appContext = context.applicationContext
        val timestamp = Instant.now()
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        val fileName = "LFN-Scout_Log_${timestamp}.txt"
        val text = readLines(appContext)
            .map { line ->
                if (developerModeEnabled) line else sanitizeForUser(line)
            }
            .joinToString("\n", postfix = "\n")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, AudioArchive.PUBLIC_RELATIVE_PATH)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = appContext.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            ) ?: error("Logdatei konnte nicht angelegt werden")
            try {
                appContext.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(text.toByteArray(Charsets.UTF_8))
                } ?: error("Logdatei konnte nicht geöffnet werden")
                val ready = ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }
                appContext.contentResolver.update(uri, ready, null, null)
            } catch (error: Exception) {
                runCatching { appContext.contentResolver.delete(uri, null, null) }
                throw error
            }
        } else {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "LFN-Scout"
            )
            check(dir.exists() || dir.mkdirs()) { "Log-Verzeichnis konnte nicht angelegt werden" }
            File(dir, fileName).writeText(text, Charsets.UTF_8)
        }

        return "${AudioArchive.PUBLIC_DISPLAY_PATH}$fileName"
    }

    fun sanitizeForUser(line: String): String =
        line
            .replace("SUPABASE", "CLOUD", ignoreCase = false)
            .replace("Supabase", "Cloud", ignoreCase = false)
            .replace("supabase", "cloud", ignoreCase = false)
            .replace(Regex("accuracy_m=[^ ]+"), "accuracy_m=hidden")

    fun formatEpoch(epochMs: Long): String {
        if (epochMs <= 0L) return "-"
        return Instant.ofEpochMilli(epochMs)
            .atZone(ZoneId.systemDefault())
            .format(displayTimeFormatter)
    }
}
