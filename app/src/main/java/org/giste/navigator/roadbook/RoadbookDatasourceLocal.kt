package org.giste.navigator.roadbook

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.use

private const val TAG = "RoadbookDatasourceLocal"
private const val ROADBOOK_FILE = "roadbook.pdf"
private const val TARGET_DPI = 144
private const val DEFAULT_DPI = 72
private const val PDF_STARTING_PAGE_INDEX = 0

class RoadbookDatasourceLocal(
    @ApplicationContext private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RoadbookDatasource {
    private var renderer: PdfRenderer? = null

    override suspend fun loadRoadbook(uri: String) {
        withContext(dispatcher) {
            val uri = Uri.parse(uri)
            val roadbookFile = File(context.filesDir, ROADBOOK_FILE)

            renderer?.close()
            if (roadbookFile.exists()) roadbookFile.delete()
            roadbookFile.createNewFile()

            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Invalid URI: $uri")
            val outputStream = FileOutputStream(roadbookFile)

            inputStream.copyTo(outputStream)
            outputStream.flush()
            inputStream.close()
            outputStream.close()

            renderer = PdfRenderer(context.contentResolver.openFileDescriptor(uri, "r")!!)

            Log.i(TAG, "Loaded roadbook: ${roadbookFile.path}")
        }
    }

    override fun getPageCount(): Int {
        return renderer?.pageCount ?: 0
    }

    override suspend fun loadPages(startPosition: Int, loadSize: Int): List<RoadbookPage> {
        if (getInternalUri() == Uri.EMPTY) return emptyList()

        val pages = mutableListOf<RoadbookPage>()

        withContext(dispatcher) {
            renderer?.let {
                val start = startPosition.coerceAtLeast(PDF_STARTING_PAGE_INDEX)
                val end = (startPosition + loadSize).coerceAtMost(it.pageCount)
                for (index in start until end) {
                    Log.d(TAG, "Processing page $index")
                    it.openPage(index).use { page ->
                        val bitmap = drawBitmapLogic(page)
                        pages.add(RoadbookPage(index, bitmap))
                    }
                }
            }
        }

        return pages
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
}