package org.giste.navigator.data

import android.graphics.Bitmap

interface PdfService {
    suspend fun getPageCount(): Int
    suspend fun load(startPosition: Int, loadSize: Int): List<Bitmap>
}