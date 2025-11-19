// ABOUTME: ViewModel managing arrival screen state with auto-refresh logic
package com.buswatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buswatch.data.repository.TfLRepository
import com.buswatch.domain.model.BusArrival
import com.buswatch.ui.state.ArrivalData
import com.buswatch.ui.state.UiState
import com.buswatch.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ArrivalViewModel @Inject constructor(
    private val tflRepository: TfLRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ArrivalData>>(UiState.Loading)
    val uiState: StateFlow<UiState<ArrivalData>> = _uiState.asStateFlow()

    private var refreshJob: Job? = null
    private var stopId: String = ""
    private var stopCode: String = ""
    private var stopName: String = ""
    private var lastActivityTime: Long = System.currentTimeMillis()

    // Allow tests to disable auto-refresh
    internal var enableAutoRefresh: Boolean = true

    fun loadArrivals(stopId: String, stopCode: String, stopName: String) {
        this.stopId = stopId
        this.stopCode = stopCode
        this.stopName = stopName
        fetchArrivals()
        if (enableAutoRefresh) {
            startAutoRefresh()
        }
    }

    fun onUserActivity() {
        lastActivityTime = System.currentTimeMillis()
    }

    private fun fetchArrivals() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            when (val result = tflRepository.getArrivals(stopId)) {
                is Result.Success -> {
                    if (result.data.isEmpty()) {
                        _uiState.value = UiState.Error("No buses currently scheduled", canRetry = false)
                    } else {
                        val groupedArrivals = result.data
                            .groupBy { it.route }
                            .mapValues { (_, arrivals) -> arrivals.take(2) }

                        _uiState.value = UiState.Success(
                            ArrivalData(stopCode, stopName, groupedArrivals)
                        )
                        Timber.d("Loaded arrivals for ${groupedArrivals.size} routes")
                    }
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(result.message, canRetry = true)
                }
            }
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(60_000) // 60 seconds

                val inactiveDuration = System.currentTimeMillis() - lastActivityTime
                if (inactiveDuration >= 300_000) { // 5 minutes
                    Timber.d("Stopping auto-refresh due to inactivity")
                    break
                }

                fetchArrivals()
            }
        }
    }

    fun retry() {
        fetchArrivals()
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
