// ABOUTME: Main activity managing navigation between stop list and arrival screens
package com.buswatch

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buswatch.domain.model.BusStop
import com.buswatch.ui.screens.ArrivalScreen
import com.buswatch.ui.screens.StopListScreen
import com.buswatch.ui.theme.BusWatchTheme
import com.buswatch.ui.viewmodel.StopListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Timber.d("Location permission granted")
        } else {
            Timber.w("Location permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkLocationPermission()

        setContent {
            BusWatchTheme {
                var selectedStop by remember { mutableStateOf<BusStop?>(null) }
                val scope = rememberCoroutineScope()
                val stopListViewModel: StopListViewModel = viewModel()

                if (selectedStop == null) {
                    StopListScreen(
                        onStopSelected = { stop ->
                            scope.launch {
                                stopListViewModel.saveSelectedStop(stop)
                                selectedStop = stop
                            }
                        }
                    )
                } else {
                    ArrivalScreen(
                        stopId = selectedStop!!.id,
                        stopCode = selectedStop!!.code,
                        stopName = selectedStop!!.name,
                        onChangeStop = {
                            selectedStop = null
                        }
                    )
                }
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                Timber.d("Location permission already granted")
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}
