package org.giste.navigator.data

import org.giste.navigator.model.PdfPage

interface PdfService {
    suspend fun getPageCount(): Int
    suspend fun load(startPosition: Int, loadSize: Int): List<PdfPage>
}