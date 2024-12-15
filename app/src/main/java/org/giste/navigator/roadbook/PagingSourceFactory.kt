package org.giste.navigator.roadbook

import androidx.paging.PagingSource

interface PagingSourceFactory {
    fun createPagingSource(uri: String): PagingSource<Int, RoadbookPage>
}