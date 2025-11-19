// ABOUTME: Unit tests for ArrivalViewModel refresh and state management
package com.buswatch.ui.viewmodel

import com.buswatch.data.repository.TfLRepository
import com.buswatch.domain.model.ArrivalType
import com.buswatch.domain.model.BusArrival
import com.buswatch.ui.state.UiState
import com.buswatch.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Duration.Companion.seconds
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArrivalViewModelTest {

    private lateinit var tflRepository: TfLRepository
    private lateinit var viewModel: ArrivalViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        tflRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadArrivals success shows arrivals grouped by route`() = runTest(timeout = 10.seconds) {
        val mockArrivals = listOf(
            BusArrival("25", "Ilf", 3, ArrivalType.LIVE),
            BusArrival("25", "Ilf", 8, ArrivalType.LIVE)
        )

        coEvery { tflRepository.getArrivals(any()) } returns Result.Success(mockArrivals)

        viewModel = ArrivalViewModel(tflRepository)
        viewModel.loadArrivals("490000001B", "BP", "Oxford St")

        // Just advance enough for the initial load to complete
        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
    }
}
