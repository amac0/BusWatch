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

        coEvery { locationRepository.getCurrentLocation() } returns Result.Success(mockLocation)
        coEvery { tflRepository.getNearbyStops(any(), any()) } returns Result.Success(mockStops)
        every { preferencesDataStore.getLastStop() } returns flowOf(null)

        viewModel = StopListViewModel(locationRepository, tflRepository, preferencesDataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
    }
}
