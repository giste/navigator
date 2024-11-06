package org.giste.navigator.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.giste.navigator.model.PdfPage

private const val CLASS_NAME = "PdfRendererService"
private const val TARGET_DPI = 144
private const val DEFAULT_DPI = 72

class PdfRendererService(
    private val uri: Uri,
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PdfService {
    private val pdfRenderer by lazy {
        Log.d(CLASS_NAME, "uri: $uri")
        PdfRenderer(context.contentResolver.openFileDescriptor(uri, "r")!!)
    }

    override suspend fun getPageCount(): Int {
        return withContext(dispatcher) {
            pdfRenderer.pageCount
        }
    }

    override suspend fun load(startPosition: Int, loadSize: Int): List<PdfPage> {
        if (uri == Uri.EMPTY) return emptyList()

        val bitmaps = mutableListOf<PdfPage>()

        withContext(dispatcher) {
            pdfRenderer.let {
                for (index in startPosition.coerceAtLeast(0) until (startPosition + loadSize).coerceAtMost(
                    it.pageCount
                )) {
                    Log.d(CLASS_NAME, "Processing page $index")
                    it.openPage(index).use { page ->
                        val bitmap = drawBitmapLogic(page)
                        bitmaps.add(PdfPage(index, bitmap))
                    }
                }
            }
        }

        return bitmaps
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
            //page.width * 2,
            //page.height * 2,
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

    protected fun finalize() {
        Log.v(CLASS_NAME, "finalize() called")
        pdfRenderer.close()
    }
}