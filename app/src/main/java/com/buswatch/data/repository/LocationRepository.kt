// ABOUTME: Repository managing device location acquisition using Play Services
package com.buswatch.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.buswatch.util.Result
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepository @Inject constructor(
    private val context: Context,
    private val locationClient: FusedLocationProviderClient
) {
    suspend fun getCurrentLocation(): Result<Location> {
        if (!hasLocationPermission()) {
            return Result.Error("Location permission required. Please enable in settings.")
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                locationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            Timber.d("Location acquired: ${location.latitude}, ${location.longitude}")
                            continuation.resume(Result.Success(location))
                        } else {
                            Timber.w("Location is null")
                            continuation.resume(Result.Error("Unable to get location. Please ensure GPS is enabled."))
                        }
                    }
                    .addOnFailureListener { exception ->
                        Timber.e(exception, "Failed to get location")
                        continuation.resume(Result.Error("Unable to get location. Please ensure GPS is enabled."))
                    }
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException getting location")
                continuation.resume(Result.Error("Location permission required. Please enable in settings."))
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
