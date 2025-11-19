// ABOUTME: Data transfer object for TfL API stop point response
package com.buswatch.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StopPointDto(
    @SerializedName("id") val id: String,
    @SerializedName("commonName") val commonName: String,
    @SerializedName("indicator") val indicator: String?,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("lines") val lines: List<LineDto>
)

data class LineDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)
