// ABOUTME: Unit tests for BusStop domain model
package com.buswatch.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BusStopTest {
    @Test
    fun `BusStop creation with all properties`() {
        val stop = BusStop(
            id = "490000001B",
            code = "BP",
            name = "Oxford Street",
            latitude = 51.5074,
            longitude = -0.1278,
            routes = listOf("25", "73", "98"),
            distanceMeters = 150
        )

        assertEquals("490000001B", stop.id)
        assertEquals("BP", stop.code)
        assertEquals("Oxford Street", stop.name)
        assertEquals(150, stop.distanceMeters)
        assertEquals(3, stop.routes.size)
    }
}
