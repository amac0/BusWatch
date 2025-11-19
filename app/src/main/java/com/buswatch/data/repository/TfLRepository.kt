// ABOUTME: Repository handling TfL API data fetching, transformation, and error handling
package com.buswatch.data.repository

import android.location.Location
import com.buswatch.data.remote.TfLApiService
import com.buswatch.domain.model.ArrivalType
import com.buswatch.domain.model.BusArrival
import com.buswatch.domain.model.BusStop
import com.buswatch.util.Result
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TfLRepository @Inject constructor(
    private val apiService: TfLApiService
) {
    suspend fun getNearbyStops(latitude: Double, longitude: Double): Result<List<BusStop>> {
        return executeWithRetry {
            val stops = apiService.getNearbyStops(latitude, longitude)

            val userLocation = Location("").apply {
                this.latitude = latitude
                this.longitude = longitude
            }

            stops.map { dto ->
                val stopLocation = Location("").apply {
                    this.latitude = dto.lat
                    this.longitude = dto.lon
                }
                val distance = userLocation.distanceTo(stopLocation).toInt()

                BusStop(
                    id = dto.id,
                    code = dto.indicator ?: "N/A",
                    name = dto.commonName,
                    latitude = dto.lat,
                    longitude = dto.lon,
                    routes = dto.lines.map { it.name },
                    distanceMeters = distance
                )
            }.sortedBy { it.distanceMeters }
        }
    }

    suspend fun getArrivals(stopId: String): Result<List<BusArrival>> {
        return executeWithRetry {
            val arrivals = apiService.getArrivals(stopId)

            arrivals.map { dto ->
                val minutesUntil = (dto.timeToStation / 60).coerceAtLeast(0)
                val arrivalType = when (dto.timing?.source) {
                    "Estimated" -> ArrivalType.LIVE
                    else -> ArrivalType.SCHEDULED
                }
                val destinationShort = dto.destinationName.take(3)

                BusArrival(
                    route = dto.lineName,
                    destinationShort = destinationShort,
                    minutesUntil = minutesUntil,
                    arrivalType = arrivalType
                )
            }.sortedBy { it.minutesUntil }
        }
    }

    private suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        initialDelay: Long = 1000,
        block: suspend () -> T
    ): Result<T> {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            try {
                val result = block()
                return Result.Success(result)
            } catch (e: Exception) {
                Timber.e(e, "API call failed (attempt ${attempt + 1}/$maxRetries)")
                if (attempt == maxRetries - 1) {
                    return Result.Error(getErrorMessage(e))
                }
                delay(currentDelay)
                currentDelay *= 2
            }
        }
        return Result.Error("Unknown error")
    }

    private fun getErrorMessage(exception: Exception): String {
        return when {
            exception is java.net.UnknownHostException -> "No internet connection"
            exception.message?.contains("429") == true -> "Service temporarily unavailable. Please try again in a moment."
            exception.message?.contains("404") == true -> "Bus stop not found"
            exception.message?.contains("timeout") == true -> "Request timed out"
            else -> "Unable to load bus times. Please try again later."
        }
    }
}
