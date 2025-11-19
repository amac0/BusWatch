// ABOUTME: Data transfer object for TfL API arrival prediction response
package com.buswatch.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ArrivalDto(
    @SerializedName("lineId") val lineId: String,
    @SerializedName("lineName") val lineName: String,
    @SerializedName("destinationName") val destinationName: String,
    @SerializedName("timeToStation") val timeToStation: Int,
    @SerializedName("timing") val timing: TimingDto?
)

data class TimingDto(
    @SerializedName("source") val source: String?
)
