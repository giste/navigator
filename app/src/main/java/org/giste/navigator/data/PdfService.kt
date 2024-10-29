package org.giste.navigator.data

import org.giste.navigator.ui.PdfPage

interface PdfService {
    suspend fun getPageCount(): Int
    suspend fun load(startPosition: Int, loadSize: Int): List<PdfPage>
}