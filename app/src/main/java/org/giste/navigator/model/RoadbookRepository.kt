package org.giste.navigator.model

import android.net.Uri
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface RoadbookRepository {
    suspend fun getPages(): Flow<PagingData<PdfPage>>
    suspend fun load(roadbookUri: Uri)
}