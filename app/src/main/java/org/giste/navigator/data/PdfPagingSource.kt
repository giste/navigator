package org.giste.navigator.data

import android.graphics.Bitmap
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

private const val PDF_STARTING_PAGE_INDEX = 0
private const val CLASS_NAME = "PdfPagingSource"

class PdfPagingSource @Inject constructor(
    private val pdfService: PdfService,
) : PagingSource<Int, Bitmap>() {
    override fun getRefreshKey(state: PagingState<Int, Bitmap>): Int {
        Log.d(CLASS_NAME, "Refreshing key")

        // New uri always has to load first page of pdf
        //return PDF_STARTING_PAGE_INDEX
        return ((state.anchorPosition ?: 0) - state.config.initialLoadSize / 2).coerceAtLeast(0)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Bitmap> {
        val position = params.key ?: PDF_STARTING_PAGE_INDEX

        try {
            val bitmaps = pdfService.load(position, params.loadSize)
            val pageCount = pdfService.getPageCount()
            val prevKey = when(position) {
                PDF_STARTING_PAGE_INDEX -> null
                else -> {
                    when(val prevKey = ensureMinKey(position - params.loadSize)) {
                        PDF_STARTING_PAGE_INDEX -> null
                        else -> prevKey
                    }
                }
            }
            val nextKey = when(position) {
                pageCount -> null
                else -> {
                    when(val nextKey = ensureMaxKey((position + params.loadSize), pageCount)) {
                        pageCount -> null
                        else -> nextKey
                    }
                }
            }

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

    private fun ensureMinKey(key: Int) = key.coerceAtLeast(PDF_STARTING_PAGE_INDEX)

    private fun ensureMaxKey(key: Int, maxKey: Int) = key.coerceAtMost(maxKey)
}