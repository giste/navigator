package org.giste.navigator.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.giste.navigator.model.Trip
import org.giste.navigator.model.TripRepository
import javax.inject.Inject

private val TRIP_PARTIAL = intPreferencesKey("TRIP_PARTIAL")
private val TRIP_TOTAL = intPreferencesKey("TRIP_TOTAL")
private const val PARTIAL_MAX = 999990
private const val TOTAL_MAX = 9999990

class TripDataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : TripRepository {
    override fun get(): Flow<Trip> {
        return dataStore.data.map {
            Trip(
                partial = it[TRIP_PARTIAL] ?: 0,
                total = it[TRIP_TOTAL] ?: 0,
            )
        }
    }

    override suspend fun incrementPartial() {
        dataStore.edit { it[TRIP_PARTIAL] = getSafePartial(getCurrentPartial() + 10) }
    }

    override suspend fun decrementPartial() {
        dataStore.edit { it[TRIP_PARTIAL] = getSafePartial(getCurrentPartial() - 10) }
    }

    override suspend fun resetPartial() {
        dataStore.edit { it[TRIP_PARTIAL] = 0 }
    }

    override suspend fun resetTrip() {
        dataStore.edit { it[TRIP_PARTIAL] = 0 }
        dataStore.edit { it[TRIP_TOTAL] = 0 }
    }

    override suspend fun addDistance(distance: Int) {
        dataStore.edit {
            it[TRIP_PARTIAL] = getSafePartial(getCurrentPartial() + distance)
        }
        dataStore.edit {
            it[TRIP_TOTAL] = getSafeTotal(getCurrentTotal() + distance)
        }
    }

    override suspend fun setPartial(partial: Int) {
        dataStore.edit { it[TRIP_PARTIAL] = getSafePartial(partial) }
    }

    override suspend fun setTotal(total: Int) {
        dataStore.edit { it[TRIP_TOTAL] = getSafeTotal(total) }
    }

    private suspend fun getCurrentPartial(): Int {
        return dataStore.data.first()[TRIP_PARTIAL] ?: 0
    }

    private suspend fun getCurrentTotal(): Int {
        return dataStore.data.first()[TRIP_TOTAL] ?: 0
    }

    private fun getSafePartial(partial: Int): Int{
        return partial.coerceAtLeast(0).coerceAtMost(PARTIAL_MAX)
    }

    private fun getSafeTotal(total: Int): Int{
        return total.coerceAtLeast(0).coerceAtMost(TOTAL_MAX)
    }
}