package org.giste.navigator.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.giste.navigator.model.Settings
import org.giste.navigator.model.SettingsRepository
import javax.inject.Inject

private const val CLASS_NAME = "SettingsDataStoreRepository"
private val LOCATION_MIN_TIME = longPreferencesKey("SETTINGS_LOCATION_MIN_TIME")
private val LOCATION_MIN_DISTANCE = intPreferencesKey("SETTINGS_LOCATION_MIN_DISTANCE")
private val DISTANCE_USE_ALTITUDE = booleanPreferencesKey("SETTINGS_DISTANCE_USE_ALTITUDE")

class SettingsDataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    override fun get(): Flow<Settings> {
        return dataStore.data.map {
            Log.d(CLASS_NAME, "Reading settings")

            Settings(
                locationMinTime = it[LOCATION_MIN_TIME] ?: 1_000L,
                locationMinDistance = it[LOCATION_MIN_DISTANCE] ?: 10,
                distanceUseAltitude = it[DISTANCE_USE_ALTITUDE] ?: true
            )
        }
    }

    override suspend fun save(settings: Settings) {
        Log.d(CLASS_NAME, "Saving $settings")

        dataStore.edit {
            it[LOCATION_MIN_TIME] = settings.locationMinTime
            it[LOCATION_MIN_DISTANCE] = settings.locationMinDistance
            it[DISTANCE_USE_ALTITUDE] = settings.distanceUseAltitude
        }
    }

}