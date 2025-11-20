// ABOUTME: Domain model representing a London bus stop with location and routes
package com.buswatch.domain.model

data class BusStop(
    val id: String,
    val code: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val routes: List<String>,
    val distanceMeters: Int
)
