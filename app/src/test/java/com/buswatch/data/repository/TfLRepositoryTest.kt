// ABOUTME: Unit tests for TfLRepository with mocked API service
package com.buswatch.data.repository

import com.buswatch.data.remote.TfLApiService
import com.buswatch.data.remote.dto.ArrivalDto
import com.buswatch.data.remote.dto.LineDto
import com.buswatch.data.remote.dto.StopPointDto
import com.buswatch.data.remote.dto.TimingDto
import com.buswatch.domain.model.ArrivalType
import com.buswatch.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TfLRepositoryTest {

    private lateinit var apiService: TfLApiService
    private lateinit var repository: TfLRepository

    @Before
    fun setup() {
        apiService = mockk()
        repository = TfLRepository(apiService)
    }

    @Test
    fun `getNearbyStops returns success with stops`() = runTest {
        val mockStops = listOf(
            StopPointDto(
                id = "490000001B",
                commonName = "Oxford Street",
                indicator = "BP",
                lat = 51.5074,
                lon = -0.1278,
                lines = listOf(LineDto("25", "25"))
            )
        )

        coEvery { apiService.getNearbyStops(any(), any(), any(), any()) } returns mockStops

        val result = repository.getNearbyStops(51.5074, -0.1278)

        assertTrue(result is Result.Success)
        val stops = (result as Result.Success).data
        assertEquals(1, stops.size)
        assertEquals("BP", stops[0].code)
    }

    @Test
    fun `getArrivals returns success with live arrivals`() = runTest {
        val mockArrivals = listOf(
            ArrivalDto(
                lineId = "25",
                lineName = "25",
                destinationName = "Ilford",
                timeToStation = 180,
                timing = TimingDto("Estimated")
            )
        )

        coEvery { apiService.getArrivals(any()) } returns mockArrivals

        val result = repository.getArrivals("490000001B")

        assertTrue(result is Result.Success)
        val arrivals = (result as Result.Success).data
        assertEquals(1, arrivals.size)
        assertEquals("25", arrivals[0].route)
        assertEquals("Ilf", arrivals[0].destinationShort)
        assertEquals(3, arrivals[0].minutesUntil)
        assertEquals(ArrivalType.LIVE, arrivals[0].arrivalType)
    }
}
