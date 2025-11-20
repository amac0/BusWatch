// ABOUTME: Unit tests for StopListViewModel state management
package com.buswatch.ui.viewmodel

import android.location.Location
import com.buswatch.data.local.LastStop
import com.buswatch.data.local.PreferencesDataStore
import com.buswatch.data.repository.LocationRepository
import com.buswatch.data.repository.TfLRepository
import com.buswatch.domain.model.BusStop
import com.buswatch.ui.state.UiState
import com.buswatch.util.Result
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
@OptIn(ExperimentalCoroutinesApi::class)
class StopListViewModelTest {

    private lateinit var locationRepository: LocationRepository
    private lateinit var tflRepository: TfLRepository
    private lateinit var preferencesDataStore: PreferencesDataStore
    private lateinit var viewModel: StopListViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        locationRepository = mockk()
        tflRepository = mockk()
        preferencesDataStore = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNearbyStops success shows stops`() = runTest {
        val mockLocation = mockk<Location> {
            every { latitude } returns 51.5074
            every { longitude } returns -0.1278
        }
        val mockStops = listOf(
            BusStop("1", "BP", "Oxford St", 51.5074, -0.1278, listOf("25"), 100)
        )

        val locationFlow = kotlinx.coroutines.flow.MutableStateFlow(mockLocation)
        coEvery { locationRepository.locationUpdates } returns locationFlow
        coEvery { tflRepository.getNearbyStops(any(), any()) } returns Result.Success(mockStops)
        every { preferencesDataStore.getLastStop() } returns flowOf(null)

        viewModel = StopListViewModel(locationRepository, tflRepository, preferencesDataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
    }

    @Test
    fun `updates stops when location changes significantly`() = runTest {
        val location1 = mockk<Location> {
            every { latitude } returns 51.529168
            every { longitude } returns -0.180107
            every { distanceTo(any()) } returns 500f // Significant distance change
        }
        val location2 = mockk<Location> {
            every { latitude } returns 51.525417
            every { longitude } returns -0.179694
            every { distanceTo(any()) } returns 500f // Significant distance change
        }

        val stopsAtLocation1 = listOf(
            BusStop("1", "G", "Clifton Road / Maida Vale", 51.529168, -0.180107, listOf("6"), 61)
        )
        val stopsAtLocation2 = listOf(
            BusStop("2", "M", "Clifton Road", 51.525417, -0.179694, listOf("6"), 96)
        )

        // Setup: LocationRepository will emit location updates via a Flow
        val locationFlow = kotlinx.coroutines.flow.MutableStateFlow(location1)
        coEvery { locationRepository.locationUpdates } returns locationFlow
        coEvery { tflRepository.getNearbyStops(51.529168, -0.180107) } returns Result.Success(stopsAtLocation1)
        coEvery { tflRepository.getNearbyStops(51.525417, -0.179694) } returns Result.Success(stopsAtLocation2)
        every { preferencesDataStore.getLastStop() } returns flowOf(null)

        // Create ViewModel - should load stops from location1
        viewModel = StopListViewModel(locationRepository, tflRepository, preferencesDataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify initial stops from location 1
        val state1 = viewModel.uiState.value
        assertTrue(state1 is UiState.Success)
        assertEquals("G", (state1 as UiState.Success).data.stops[0].code)

        // Simulate location change to location 2
        locationFlow.value = location2
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify stops updated to location 2 (Stop M should now appear)
        val state2 = viewModel.uiState.value
        assertTrue(state2 is UiState.Success)
        assertEquals("M", (state2 as UiState.Success).data.stops[0].code)
    }
}
