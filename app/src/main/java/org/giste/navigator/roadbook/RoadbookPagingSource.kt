package org.giste.navigator.roadbook

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

private const val CLASS_NAME = "PagingSourcePdfRenderer"
private const val ROADBOOK_FILE = "roadbook.pdf"
private const val TARGET_DPI = 144
private const val DEFAULT_DPI = 72
private const val PDF_STARTING_PAGE_INDEX = 0

class RoadbookPagingSource(
    private val uri: String,
    private val context: Context,
) : PagingSource<Int, RoadbookPage>() {
    private val pdfRenderer by lazy {
        loadRoadbook()
        PdfRenderer(context.contentResolver.openFileDescriptor(getInternalUri(), "r")!!)
    }

    override fun getRefreshKey(state: PagingState<Int, RoadbookPage>): Int? {
        Log.d(CLASS_NAME, "Refreshing key")

        return state.anchorPosition ?: PDF_STARTING_PAGE_INDEX
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RoadbookPage> {
        val position = params.key ?: PDF_STARTING_PAGE_INDEX

        try {
            val bitmaps = this.loadPages(position, params.loadSize)
            val pageCount = pdfRenderer.pageCount
            val prevKey = if (position == PDF_STARTING_PAGE_INDEX) {
                null
            } else {
                safeStart(position - params.loadSize)
            }
            val nextKey = if ((position + params.loadSize) >= pageCount) {
                null
            } else {
                safeEnd(position + params.loadSize)
            }

            Log.d(
                CLASS_NAME,
                "position: $position; nextKey: $nextKey; prevKey: $prevKey; count: $pageCount"
            )

            return LoadResult.Page(bitmaps, prevKey, nextKey)

        } catch (e: IOException) {
            return LoadResult.Error(e)
        } catch (e: SecurityException) {
            return LoadResult.Error(e)
        } catch (e: FileNotFoundException) {
            return LoadResult.Error(e)
        }
    }

    /**
     * Indicates that jumping is supported by the `PagingSource`.
     */
    override val jumpingSupported: Boolean = true

    /**
     * Copies the roadbook pdf to internal storage.
     */
    private fun loadRoadbook() {
        val uri = Uri.parse(uri)
        val roadbookFile = File(context.filesDir, ROADBOOK_FILE)

        if (roadbookFile.exists()) roadbookFile.delete()
        roadbookFile.createNewFile()

        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Invalid URI: $uri")
        val outputStream = FileOutputStream(roadbookFile)

        inputStream.copyTo(outputStream)
        outputStream.flush()
        inputStream.close()
        outputStream.close()

        Log.i(CLASS_NAME, "Loaded roadbook: ${roadbookFile.path}")
    }

    /**
     * Gets the URI of the roadbook in internal storage.
     *
     * @return URI of the roadbook or `Uri.EMPTY` if there is no roadbook loaded.
     */
    private fun getInternalUri(): Uri {
        val file = File(context.filesDir, ROADBOOK_FILE)

        return if (file.exists()) {
            Uri.fromFile(file)
        } else {
            Uri.EMPTY
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
            page.width * TARGET_DPI / DEFAULT_DPI,
            page.height * TARGET_DPI / DEFAULT_DPI,
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

    private fun loadPages(startPosition: Int, loadSize: Int): List<RoadbookPage> {
        if (getInternalUri() == Uri.EMPTY) return emptyList()

        val pages = mutableListOf<RoadbookPage>()

        pdfRenderer.let {
            for (index in safeStart(startPosition) until safeEnd(startPosition + loadSize)) {
                Log.d(CLASS_NAME, "Processing page $index")
                it.openPage(index).use { page ->
                    val bitmap = drawBitmapLogic(page)
                    pages.add(RoadbookPage(index, bitmap))
                }
            }
        }

        return pages
    }

//    private fun Bitmap.toByteArray(): ByteArray {
//        val stream = ByteArrayOutputStream()
//        this.compress(Bitmap.CompressFormat.PNG, 100, stream)
//        return stream.toByteArray()
//    }

//    private fun ByteArray.toBitmap(): Bitmap {
//        return BitmapFactory.decodeByteArray(this, 0, this.size)
//    }

    private fun safeStart(startPosition: Int): Int =
        startPosition.coerceAtLeast(PDF_STARTING_PAGE_INDEX)

    private fun safeEnd(endPosition: Int): Int = endPosition.coerceAtMost(pdfRenderer.pageCount)
}