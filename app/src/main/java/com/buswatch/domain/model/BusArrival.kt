// ABOUTME: Domain model representing a bus arrival with route, destination, and timing
package com.buswatch.domain.model

data class BusArrival(
    val route: String,
    val destinationShort: String,
    val minutesUntil: Int,
    val arrivalType: ArrivalType
)
