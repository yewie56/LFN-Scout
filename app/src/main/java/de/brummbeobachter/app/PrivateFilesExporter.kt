package de.brummbeobachter.app

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Best-effort complete app-private file snapshot. A file changing during copy is reported as failed. */
internal object PrivateFilesExporter {
    data class Result(val copied: Int, val failed: Int, val bytes: Long, val folder: String, val details: String)
    fun export(context: Context): Result {
        require(Build.VERSION.SDK_INT >= 29) { "Android 10 oder neuer erforderlich" }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(Date())
        val folder = "Download/lfn-scout-test/Exports/$stamp/"
        val root = context.applicationInfo.dataDir?.let(::File) ?: error("App-Verzeichnis fehlt")
        var copied = 0; var failed = 0; var bytes = 0L
        val manifest = StringBuilder("LFN-Scout Datei-Export\nZeit: $stamp\nQuelle: app-interner Speicher\n")
        val resolver = context.contentResolver
        fun publish(name: String, path: String, data: File?, mime: String): Pair<Long, String> {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, mime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, path)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: error("MediaStore insert fehlgeschlagen")
            try {
                val md = MessageDigest.getInstance("SHA-256")
                var length = 0L
                resolver.openOutputStream(uri, "w")?.use { output ->
                    if (data != null) data.inputStream().use { input ->
                        val buf = ByteArray(32768)
                        while (true) { val n = input.read(buf); if (n < 0) break; output.write(buf, 0, n); md.update(buf, 0, n); length += n }
                    } else {
                        val payload = manifest.toString().toByteArray(Charsets.UTF_8)
                        output.write(payload); md.update(payload); length = payload.size.toLong()
                    }
                } ?: error("Ausgabestream fehlt")
                if (data != null && data.length() != length) error("Quelldatei während Export geändert")
                val digest = md.digest().joinToString("") { "%02x".format(it) }
                resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
                return length to digest
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
        }
        root.walkTopDown().onFail { f, ex -> failed++; manifest.append("ERROR DIR ").append(f.relativeTo(root).path).append(' ').append(ex.message).append('\n') }
            .filter { it.isFile }.forEach { file ->
                val relative = file.relativeTo(root).invariantSeparatorsPath
                try {
                    val subdir = relative.substringBeforeLast('/', "")
                    val path = folder + if (subdir.isEmpty()) "" else "$subdir/"
                    val (size, sha) = publish(file.name, path, file, "application/octet-stream")
                    copied++; bytes += size
                    manifest.append("OK ").append(relative).append(" bytes=").append(size).append(" sha256=").append(sha).append('\n')
                } catch (e: Exception) {
                    failed++; manifest.append("ERROR ").append(relative).append(" reason=").append(e.javaClass.simpleName).append(':').append(e.message).append('\n')
                }
            }
        manifest.append("SUMMARY copied=$copied failed=$failed bytes=$bytes\n")
        try { publish("EXPORT_MANIFEST.txt", folder, null, "text/plain") } catch (e: Exception) { failed++ }
        return Result(copied, failed, bytes, folder, manifest.toString())
    }
}
