// ABOUTME: ViewModel managing stop list screen state and location-based stop fetching
package com.buswatch.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buswatch.data.local.PreferencesDataStore
import com.buswatch.data.repository.LocationRepository
import com.buswatch.data.repository.TfLRepository
import com.buswatch.domain.model.BusStop
import com.buswatch.ui.state.StopListData
import com.buswatch.ui.state.UiState
import com.buswatch.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StopListViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val tflRepository: TfLRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<StopListData>>(UiState.Loading)
    val uiState: StateFlow<UiState<StopListData>> = _uiState.asStateFlow()

    private var currentLocation: Location? = null

    init {
        loadNearbyStops()
    }

    fun loadNearbyStops() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            when (val locationResult = locationRepository.getCurrentLocation()) {
                is Result.Success -> {
                    currentLocation = locationResult.data
                    fetchNearbyStops(locationResult.data.latitude, locationResult.data.longitude)
                }
                is Result.Error -> {
                    _uiState.value = UiState.Error(locationResult.message, canRetry = false)
                }
            }
        }
    }

    private suspend fun fetchNearbyStops(latitude: Double, longitude: Double) {
        when (val stopsResult = tflRepository.getNearbyStops(latitude, longitude)) {
            is Result.Success -> {
                val stops = stopsResult.data.take(5)
                _uiState.value = UiState.Success(StopListData(stops))
                Timber.d("Loaded ${stops.size} nearby stops")
            }
            is Result.Error -> {
                _uiState.value = UiState.Error(stopsResult.message, canRetry = true)
            }
        }
    }

    suspend fun saveSelectedStop(stop: BusStop) {
        preferencesDataStore.saveLastStop(stop.id, stop.latitude, stop.longitude)
        Timber.d("Saved last stop: ${stop.code}")
    }

    suspend fun checkLastStop(): BusStop? {
        val lastStop = preferencesDataStore.getLastStop().first() ?: return null
        val currentLoc = currentLocation ?: return null

        val stopLocation = Location("").apply {
            latitude = lastStop.latitude
            longitude = lastStop.longitude
        }

        val distance = currentLoc.distanceTo(stopLocation)

        return if (distance <= 500) {
            _uiState.value.let { state ->
                if (state is UiState.Success) {
                    state.data.stops.find { it.id == lastStop.stopId }
                } else null
            }
        } else {
            null
        }
    }
}
