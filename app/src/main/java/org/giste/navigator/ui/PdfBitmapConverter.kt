package org.giste.navigator.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import androidx.collection.lruCache
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfBitmapConverter(
    private val context: Context,
    //private val uri: Uri,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PagingSource<Int, Bitmap>() {
    companion object {
        private const val CACHE_SIZE = 20
    }

    private var renderer: PdfRenderer? = null
    var uri: Uri = Uri.EMPTY
        set(value) {
            clearCache()
            field = value
            Log.d("PdfBitmapConverter:setUri", "uri = ${uri.path}")
        }

    /**
     * LRU cache for storing rendered `Bitmap` images of PDF pages. It automatically recycles
     * bitmaps when they are evicted from the cache.
     */
    private val bitmapCache = lruCache<Int, Bitmap>(
        maxSize = CACHE_SIZE,
        onEntryRemoved = { evicted, _, oldBitmap, _ ->
            if (evicted) {
                oldBitmap.recycle()
            }
        },
    )

    /**
     * Retrieves the total number of pages in the PDF.
     *
     * @return The total number of pages in the PDF.
     */
    suspend fun getPageCount(): Int {
        Log.d("PdfBitmapConverter:setUri", "uri = ${uri.path}")

        if(uri == Uri.EMPTY) return 0

        Log.d("PdfBitmapConverter:getPageCount", "uri not empty")

        return withContext(dispatcher) {
            context.contentResolver
                .openFileDescriptor(uri, "r")?.use { descriptor ->
                    with(PdfRenderer(descriptor)) {
                        return@with pageCount
                    }
                } ?: 0
        }
    }

    /**
     * Loads all pages of the PDF as a list of `Bitmap` images.
     *
     * @return A list of `Bitmap` images representing all pages of the PDF.
     */
    suspend fun loadAllPages(): List<Bitmap> {
        if(uri == Uri.EMPTY) return emptyList()

        return withContext(dispatcher) {
            val bitmaps = mutableListOf<Bitmap>()

            renderer?.close()

            context
                .contentResolver
                .openFileDescriptor(uri, "r")
                ?.use { descriptor ->
                    with(PdfRenderer(descriptor)) {
                        renderer = this

                        return@with (0 until pageCount).map { index ->
                            openPage(index).use { page ->
                                if (bitmapCache[index] != null) {
                                    return@map bitmapCache[index]!!
                                } else {
                                    val bitmap = drawBitmapLogic(page)

                                    bitmapCache.put(index, bitmap)
                                    bitmaps.add(bitmap)
                                }
                            }
                        }
                    }
                }
            return@withContext bitmaps
        }
    }

    /**
     * Loads a specific section of pages from the PDF as a list of `Bitmap` images.
     *
     * @param startPosition The start position (page number) to begin loading from.
     * @param loadSize The number of pages to load.
     * @return A list of `Bitmap` images representing the loaded pages.
     */
    suspend fun loadSection(startPosition: Int, loadSize: Int): List<Bitmap> {
        if(uri == Uri.EMPTY) return emptyList()

        return withContext(dispatcher) {
            val bitmaps = mutableListOf<Bitmap>()
            renderer?.close()

            context
                .contentResolver
                .openFileDescriptor(uri, "r")
                ?.use { descriptor ->
                    with(PdfRenderer(descriptor)) {
                        renderer = this

                        for (index in startPosition until (startPosition + loadSize).coerceAtMost(pageCount)) {
                            openPage(index).use { page ->
                                val bitmap = drawBitmapLogic(page)
                                bitmaps.add(bitmap)
                            }
                        }
                    }
                }
            return@withContext bitmaps
        }
    }

    /**
     * Draws the contents of a `PdfRenderer.Page` onto a `Bitmap`.
     *
     * @param page The `PdfRenderer.Page` to render.
     * @return The rendered `Bitmap`.
     */
    private fun drawBitmapLogic(page: PdfRenderer.Page): Bitmap {
        val bitmap = Bitmap.createBitmap(
            page.width,
            page.height,
            Bitmap.Config.ARGB_8888,
        )

        Canvas(bitmap).apply {
            drawColor(Color.WHITE)
            drawBitmap(bitmap, 0f, 0f, null)
        }

        page.render(
            bitmap,
            null,
            null,
            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY,
        )
        return bitmap
    }

    /**
     * Retrieves the key to be used for refreshing the paging state.
     *
     * @param state The current [PagingState].
     * @return The key representing the position to start refreshing from.
     */
    override fun getRefreshKey(state: PagingState<Int, Bitmap>): Int {
        return ((state.anchorPosition ?: 0) - state.config.initialLoadSize / 2).coerceAtLeast(0)
    }

    /**
     * Loads a page of data from the PDF.
     *
     * @param params The parameters defining the load request.
     * @return A [LoadResult] containing the loaded bitmaps and pagination keys.
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Bitmap> {
        val position = params.key ?: 0
        val bitmaps = loadSection(position, params.loadSize)
        return LoadResult.Page(
            data = bitmaps,
            prevKey = if (position == 0) null else position - params.loadSize,
            nextKey = if (position + params.loadSize >= getPageCount()) null else position + params.loadSize,
        )
    }

    /**
     * Indicates that jumping is supported by the `PagingSource`.
     */
    override val jumpingSupported: Boolean = true

    /**
     * Clears the bitmap cache, recycling all cached bitmaps.
     */
    fun clearCache() {
        bitmapCache.evictAll()
    }

}