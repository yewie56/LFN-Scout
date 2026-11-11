package de.brummbeobachter.app

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File

/** The exact screenshot pixels stay untouched; user comment is stored as EXIF metadata. */
internal object AnalyzerScreenCaptureComment {
    fun write(context: Context, uri: Uri, comment: String) = runCatching {
        context.contentResolver.openFileDescriptor(uri, "rw")?.use { fd ->
            ExifInterface(fd.fileDescriptor).apply {
                setAttribute(ExifInterface.TAG_USER_COMMENT, comment)
                saveAttributes()
            }
        }
    }
    fun write(file: File, comment: String) = runCatching {
        ExifInterface(file.absolutePath).apply {
            setAttribute(ExifInterface.TAG_USER_COMMENT, comment)
            saveAttributes()
        }
    }
}
