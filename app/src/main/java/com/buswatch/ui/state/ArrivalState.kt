// ABOUTME: UI state for arrival times screen
package com.buswatch.ui.state

import com.buswatch.domain.model.BusArrival

data class ArrivalData(
    val stopCode: String,
    val stopName: String,
    val arrivalsByRoute: Map<String, List<BusArrival>>
)
