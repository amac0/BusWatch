// ABOUTME: Retrofit service interface for TfL API endpoints
package com.buswatch.data.remote

import com.buswatch.data.remote.dto.ArrivalDto
import com.buswatch.data.remote.dto.StopPointsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TfLApiService {
    @GET("StopPoint")
    suspend fun getNearbyStops(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("stopTypes") stopTypes: String = "NaptanPublicBusCoachTram",
        @Query("radius") radius: Int = 500
    ): StopPointsResponse

    @GET("StopPoint/{stopId}/Arrivals")
    suspend fun getArrivals(
        @Path("stopId") stopId: String
    ): List<ArrivalDto>
}
