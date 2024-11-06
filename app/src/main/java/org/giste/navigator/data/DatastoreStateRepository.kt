package org.giste.navigator.data

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.giste.navigator.model.State
import org.giste.navigator.model.StateRepository
import javax.inject.Inject

class DataStoreStateRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : StateRepository {
    companion object {
        val TRIP_PARTIAL = intPreferencesKey("TRIP_PARTIAL")
        val TRIP_TOTAL = intPreferencesKey("TRIP_TOTAL")
        val ROADBOOK_URI = stringPreferencesKey("ROADBOOK_URI")
    }

    override fun getState(): Flow<State> {
         return dataStore.data.map {
             State(
                 partial = it[TRIP_PARTIAL] ?: 0,
                 total = it[TRIP_TOTAL] ?: 0,
                 roadbookUri = it[ROADBOOK_URI]?.let { uri ->
                     Uri.parse(uri)
                 } ?: Uri.EMPTY
             )
         }
    }

    override fun getPartial(): Flow<Int> {
        return dataStore.data.map { it[TRIP_PARTIAL] ?: 0 }
    }

    override fun getTotal(): Flow<Int> {
        return dataStore.data.map { it[TRIP_TOTAL] ?: 0 }
    }

    override fun getRoadbookUri(): Flow<Uri> {
        return dataStore.data.map {
            it[ROADBOOK_URI]?.let { uri -> Uri.parse(uri) } ?: Uri.EMPTY
        }
    }

    override suspend fun setPartial(partial: Int) {
        dataStore.edit { it[TRIP_PARTIAL] = partial }
    }

    override suspend fun setTotal(total: Int) {
        dataStore.edit { it[TRIP_TOTAL] = total }
    }

    override suspend fun setRoadbookUri(roadbookUri: Uri) {
        dataStore.edit { it[ROADBOOK_URI] = roadbookUri.toString() }
    }

}