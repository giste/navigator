package org.giste.navigator.model

import android.net.Uri
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface PdfRepository {
    suspend fun getRoadbookPages(): Flow<PagingData<PdfPage>>
    suspend fun loadRoadbook(uri: Uri)
}