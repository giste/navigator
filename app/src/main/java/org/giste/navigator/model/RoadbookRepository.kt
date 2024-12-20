package org.giste.navigator.model

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface RoadbookRepository {
    suspend fun getPages(): Flow<PagingData<PdfPage>>
    suspend fun load(roadbookUri: String)
    fun getRoadbookUri(): Flow<String>
    fun getScroll(): Flow<RoadbookScroll>
    suspend fun setScroll(roadbookScroll: RoadbookScroll)
}