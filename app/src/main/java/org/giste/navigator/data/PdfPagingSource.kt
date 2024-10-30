package org.giste.navigator.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.giste.navigator.model.PdfPage
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

private const val PDF_STARTING_PAGE_INDEX = 0
private const val CLASS_NAME = "PdfPagingSource"

class PdfPagingSource @Inject constructor(
    private val pdfService: PdfService,
) : PagingSource<Int, PdfPage>() {
    override fun getRefreshKey(state: PagingState<Int, PdfPage>): Int {
        Log.d(CLASS_NAME, "Refreshing key")

        return state.anchorPosition ?: PDF_STARTING_PAGE_INDEX
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PdfPage> {
        val position = params.key ?: PDF_STARTING_PAGE_INDEX

        try {
            val bitmaps = pdfService.load(position, params.loadSize)
            val pageCount = pdfService.getPageCount()
            val prevKey = if (position == PDF_STARTING_PAGE_INDEX) {
                null
            } else {
                (position - params.loadSize).coerceAtLeast(PDF_STARTING_PAGE_INDEX)
            }
            val nextKey = if ((position + params.loadSize) >= pageCount) {
                null
            } else {
                (position + params.loadSize).coerceAtMost(pageCount)
            }

            Log.d(CLASS_NAME, "position: $position; nextKey: $nextKey; prevKey: $prevKey; count: $pageCount")

            return LoadResult.Page(bitmaps, prevKey, nextKey)

        } catch (e: IOException) {
            return LoadResult.Error(e)
        } catch (e: SecurityException) {
            return LoadResult.Error(e)
        } catch(e: FileNotFoundException) {
            return LoadResult.Error(e)
        }
    }

    /**
     * Indicates that jumping is supported by the `PagingSource`.
     */
    override val jumpingSupported: Boolean = true
}