package org.giste.navigator.data

import android.content.Context
import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import org.giste.navigator.ui.PdfPage
import org.giste.navigator.ui.PdfRepository
import javax.inject.Inject

class PdfRendererRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : PdfRepository {
    override fun getPdfStream(uri: Uri): Flow<PagingData<PdfPage>> {
        return Pager(
            PagingConfig(
                pageSize = 5,
//                prefetchDistance = 10,
//                enablePlaceholders = false,
                initialLoadSize = 7,
                maxSize = 30,
//                jumpThreshold = 2,
            ),
            initialKey = 0,
        ) {
            PdfPagingSource(PdfRendererService(uri, context))
        }.flow
    }
}