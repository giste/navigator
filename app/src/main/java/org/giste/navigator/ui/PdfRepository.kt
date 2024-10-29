package org.giste.navigator.ui

import android.net.Uri
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface PdfRepository {
    fun getPdfStream(uri: Uri): Flow<PagingData<PdfPage>>
}