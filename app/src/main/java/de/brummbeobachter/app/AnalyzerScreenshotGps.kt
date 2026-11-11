package de.brummbeobachter.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Tasks
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/** GPS location for Analyzer JPEG APP1 / PNG eXIf, without networking or modifying image pixels. */
internal object AnalyzerScreenshotGps {
    private const val FIX_MAX_AGE_MS = 5 * 60 * 1000L
    private const val CURRENT_FIX_TIMEOUT_SEC = 5L

    fun getCurrentLocation(context: Context): Location? {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        val cancellation = CancellationTokenSource()
        val priority = if (fine) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
        val current = try {
            Tasks.await(client.getCurrentLocation(priority, cancellation.token), CURRENT_FIX_TIMEOUT_SEC, TimeUnit.SECONDS)
        } catch (_: Exception) {
            null
        } finally {
            cancellation.cancel()
        }
        if (isRecentValid(current)) return current
        val last = try {
            Tasks.await(client.lastLocation, 2, TimeUnit.SECONDS)
        } catch (_: Exception) {
            null
        }
        return last?.takeIf(::isRecentValid)
    }

    private fun isRecentValid(location: Location?): Boolean {
        if (location == null) return false
        if (!location.latitude.isFinite() || !location.longitude.isFinite()) return false
        if (abs(location.latitude) > 90 || abs(location.longitude) > 180) return false
        val age = SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos
        return age >= 0 && age <= FIX_MAX_AGE_MS * 1_000_000L
    }

    fun writeAndVerify(context: Context, uri: Uri, location: Location): Boolean {
        return try {
            val writeFd = context.contentResolver.openFileDescriptor(uri, "rw") ?: return false
            writeFd.use { fd ->
                val exif = ExifInterface(fd.fileDescriptor)
                exif.setGpsInfo(location)
                exif.saveAttributes()
            }
            context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                verify(ExifInterface(fd.fileDescriptor), location)
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    fun writeAndVerify(file: File, location: Location): Boolean = try {
        val exif = ExifInterface(file.absolutePath)
        exif.setGpsInfo(location)
        exif.saveAttributes()
        verify(ExifInterface(file.absolutePath), location)
    } catch (_: Exception) {
        false
    }

    private fun verify(exif: ExifInterface, location: Location): Boolean {
        val latLong = exif.latLong ?: return false
        return abs(latLong[0] - location.latitude) < 0.00001 &&
            abs(latLong[1] - location.longitude) < 0.00001
    }
}
