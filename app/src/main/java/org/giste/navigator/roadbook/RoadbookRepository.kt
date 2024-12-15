package org.giste.navigator.roadbook

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.giste.navigator.model.RoadbookScroll

interface RoadbookRepository {
    fun getPages() : Flow<RoadbookResult>
    suspend fun load(uri: String)
    suspend fun saveScroll(scroll: RoadbookScroll)
}

sealed class RoadbookResult {
    data object NotLoaded : RoadbookResult()
    data class Loaded(
        val pages: Flow<PagingData<RoadbookPage>>,
        val initialScroll: RoadbookScroll,
    ) : RoadbookResult()
}