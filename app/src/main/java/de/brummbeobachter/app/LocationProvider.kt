package de.brummbeobachter.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class ObservationLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val available: Boolean
) {
    companion object {
        fun unavailable() = ObservationLocation(
            latitude = 0.0,
            longitude = 0.0,
            accuracyMeters = null,
            available = false
        )
    }
}

class LocationProvider(private val context: Context) {

    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasFinePermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun hasCoarsePermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun hasAnyLocationPermission(): Boolean =
        hasFinePermission() || hasCoarsePermission()

    suspend fun getObservationLocation(): ObservationLocation {
        if (!hasAnyLocationPermission()) {
            return ObservationLocation.unavailable()
        }

        return try {
            val priority =
                if (hasFinePermission()) {
                    Priority.PRIORITY_HIGH_ACCURACY
                } else {
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY
                }

            val tokenSource = CancellationTokenSource()

            val current = suspendCancellableCoroutine<android.location.Location?> { continuation ->
                fusedClient
                    .getCurrentLocation(priority, tokenSource.token)
                    .addOnSuccessListener { location ->
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }

                continuation.invokeOnCancellation {
                    tokenSource.cancel()
                }
            }

            val location = current ?: getLastKnownLocation()

            if (location == null) {
                ObservationLocation.unavailable()
            } else {
                ObservationLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracyMeters = if (location.hasAccuracy()) location.accuracy else null,
                    available = true
                )
            }
        } catch (_: SecurityException) {
            ObservationLocation.unavailable()
        } catch (_: Exception) {
            ObservationLocation.unavailable()
        }
    }

    private suspend fun getLastKnownLocation(): android.location.Location? =
        suspendCancellableCoroutine { continuation ->
            try {
                fusedClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
            } catch (_: SecurityException) {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
        }
}
