// ABOUTME: Arrival screen displaying bus arrival times with auto-refresh
package com.buswatch.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import com.buswatch.domain.model.ArrivalType
import com.buswatch.domain.model.BusArrival
import com.buswatch.ui.components.ErrorScreen
import com.buswatch.ui.components.LoadingScreen
import com.buswatch.ui.state.UiState
import com.buswatch.ui.theme.LiveGreen
import com.buswatch.ui.theme.ScheduledWhite
import com.buswatch.ui.viewmodel.ArrivalViewModel
import android.view.HapticFeedbackConstants

@Composable
fun ArrivalScreen(
    stopId: String,
    stopCode: String,
    stopName: String,
    onChangeStop: () -> Unit,
    viewModel: ArrivalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val view = LocalView.current

    DisposableEffect(stopId) {
        viewModel.loadArrivals(stopId, stopCode, stopName)
        onDispose { }
    }

    when (val state = uiState) {
        is UiState.Loading -> {
            LoadingScreen(message = "Loading arrivals...")
        }
        is UiState.Error -> {
            ErrorScreen(
                message = state.message,
                canRetry = state.canRetry,
                onRetry = if (state.canRetry) {
                    { viewModel.retry() }
                } else null
            )
        }
        is UiState.Success -> {
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Stop ${state.data.stopCode}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = state.data.stopName)
                    }
                }

                state.data.arrivalsByRoute.forEach { (route, arrivals) ->
                    items(arrivals.size) { index ->
                        ArrivalItem(
                            arrival = arrivals[index],
                            onClick = { viewModel.onUserActivity() }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            onChangeStop()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Stop")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ArrivalItem(
    arrival: BusArrival,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = arrival.route,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )
        Text(text = "→")
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = arrival.destinationShort)
        Spacer(modifier = Modifier.weight(1f))

        val timeText = if (arrival.minutesUntil < 1) "Due" else "${arrival.minutesUntil} min"
        val timeColor = when (arrival.arrivalType) {
            ArrivalType.LIVE -> LiveGreen
            ArrivalType.SCHEDULED -> ScheduledWhite
        }

        Text(
            text = timeText,
            color = timeColor,
            fontWeight = FontWeight.Medium
        )
    }
}
