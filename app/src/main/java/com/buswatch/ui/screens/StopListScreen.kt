// ABOUTME: Stop list screen displaying nearby bus stops in scrollable list
package com.buswatch.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.items
import com.buswatch.domain.model.BusStop
import com.buswatch.ui.components.ErrorScreen
import com.buswatch.ui.components.LoadingScreen
import com.buswatch.ui.state.UiState
import com.buswatch.ui.viewmodel.StopListViewModel
import android.view.HapticFeedbackConstants

@Composable
fun StopListScreen(
    onStopSelected: (BusStop) -> Unit,
    viewModel: StopListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val view = LocalView.current

    when (val state = uiState) {
        is UiState.Loading -> {
            LoadingScreen(message = "Getting your location...")
        }
        is UiState.Error -> {
            ErrorScreen(
                message = state.message,
                canRetry = state.canRetry,
                onRetry = if (state.canRetry) {
                    { viewModel.loadNearbyStops() }
                } else null
            )
        }
        is UiState.Success -> {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.data.stops) { stop ->
                    StopListItem(
                        stop = stop,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            onStopSelected(stop)
                        }
                    )
                }
                item {
                    Chip(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            viewModel.loadNearbyStops()
                        },
                        label = {
                            Text(text = "Refresh")
                        },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StopListItem(
    stop: BusStop,
    onClick: () -> Unit
) {
    Chip(
        onClick = onClick,
        label = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Stop ${stop.code}",
                    fontWeight = FontWeight.Bold
                )
                Text(text = stop.name)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Routes: ${stop.routes.joinToString(", ")}")
            }
        },
        colors = ChipDefaults.primaryChipColors(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    )
}
