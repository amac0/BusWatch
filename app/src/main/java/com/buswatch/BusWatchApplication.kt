// ABOUTME: Application class initializing Timber logging and Hilt dependency injection
// ABOUTME: Entry point for the BusWatch Wear OS application
package com.buswatch

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class BusWatchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("BusWatch application started")
    }
}
