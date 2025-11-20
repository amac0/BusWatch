// ABOUTME: Repository managing device location acquisition using Play Services
package com.buswatch.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.buswatch.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationClient: FusedLocationProviderClient
) {
    val locationUpdates: Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            30000L // 30 seconds
        ).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    Timber.d("Location update: ${location.latitude}, ${location.longitude}")
                    trySend(location)
                }
            }
        }

        try {
            locationClient.requestLocationUpdates(locationRequest, callback, null)
                .addOnFailureListener { e ->
                    Timber.e(e, "Failed to request location updates")
                    close(e)
                }
        } catch (e: SecurityException) {
            Timber.e(e, "SecurityException requesting location updates")
            close(e)
        }

        awaitClose {
            locationClient.removeLocationUpdates(callback)
            Timber.d("Location updates stopped")
        }
    }
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
