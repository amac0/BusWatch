// ABOUTME: DataStore wrapper for storing last selected bus stop preferences
package com.buswatch.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bus_watch_prefs")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LAST_STOP_ID = stringPreferencesKey("last_stop_id")
        val LAST_STOP_LAT = doublePreferencesKey("last_stop_lat")
        val LAST_STOP_LON = doublePreferencesKey("last_stop_lon")
    }

    suspend fun saveLastStop(stopId: String, latitude: Double, longitude: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_STOP_ID] = stopId
            prefs[Keys.LAST_STOP_LAT] = latitude
            prefs[Keys.LAST_STOP_LON] = longitude
        }
    }

    fun getLastStop(): Flow<LastStop?> {
        return context.dataStore.data.map { prefs ->
            val stopId = prefs[Keys.LAST_STOP_ID]
            val lat = prefs[Keys.LAST_STOP_LAT]
            val lon = prefs[Keys.LAST_STOP_LON]

            if (stopId != null && lat != null && lon != null) {
                LastStop(stopId, lat, lon)
            } else {
                null
            }
        }
    }

    suspend fun clearLastStop() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.LAST_STOP_ID)
            prefs.remove(Keys.LAST_STOP_LAT)
            prefs.remove(Keys.LAST_STOP_LON)
        }
    }
}

data class LastStop(
    val stopId: String,
    val latitude: Double,
    val longitude: Double
)
