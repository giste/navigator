package org.giste.navigator.roadbook

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.giste.navigator.model.RoadbookScroll
import javax.inject.Inject

private const val CLASS_NAME = "RoadbookRepositoryImpl"

class RoadbookRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val pagingSourceFactory: PagingSourceFactory,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : RoadbookRepository {
    companion object {
        val ROADBOOK_URI = stringPreferencesKey("ROADBOOK_URI")
        val ROADBOOK_PAGE_INDEX = intPreferencesKey("ROADBOOK_PAGE_INDEX")
        val ROADBOOK_PAGE_OFFSET = intPreferencesKey("ROADBOOK_PAGE_OFFSET")
    }

    override fun getPages(): Flow<RoadbookResult> {
        return getRoadbookUri().map { uri ->
            if (uri.isEmpty()) {
                RoadbookResult.NotLoaded
            } else {
                RoadbookResult.Loaded(
                    pages = Pager(
                        config = PagingConfig(pageSize = 5),
                        initialKey = 0,
                    ) {
                        pagingSourceFactory.createPagingSource(uri)
                    }.flow,
                    initialScroll = getScroll().first()
                )
            }
        }
    }

    override suspend fun load(uri: String) {
        withContext(dispatcher) {
            // New roadbook, reset scroll
            saveScroll(RoadbookScroll())
            // Save uri
            saveRoadbookUri(uri)
        }
    }

    override suspend fun saveScroll(scroll: RoadbookScroll) {
        dataStore.edit {
            Log.v(CLASS_NAME, "setScroll(${scroll.pageIndex}, ${scroll.pageOffset})")

            it[ROADBOOK_PAGE_INDEX] = scroll.pageIndex
            it[ROADBOOK_PAGE_OFFSET] = scroll.pageOffset
        }
    }

    private fun getScroll(): Flow<RoadbookScroll> {
        return dataStore.data.map {
            Log.v(
                CLASS_NAME,
                "getScroll = (${it[ROADBOOK_PAGE_INDEX]}, ${it[ROADBOOK_PAGE_OFFSET]})"
            )
            RoadbookScroll(
                pageIndex = it[ROADBOOK_PAGE_INDEX] ?: 0,
                pageOffset = it[ROADBOOK_PAGE_OFFSET] ?: 0
            )
        }
    }

    private fun getRoadbookUri(): Flow<String> {
        return dataStore.data.map { it[ROADBOOK_URI] ?: "" }
    }

    private suspend fun saveRoadbookUri(uri: String) {
        dataStore.edit { it[ROADBOOK_URI] = uri }
    }

}