package org.giste.navigator.data

import android.graphics.Bitmap

data class PdfLoadResponse(
    val pageCount : Int,
    val pages: List<Bitmap> = emptyList(),
)