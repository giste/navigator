package org.giste.navigator.roadbook

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RoadbookPagingSourceFactory @Inject constructor(
    @ApplicationContext private val context: Context
) : PagingSourceFactory {
    override fun createPagingSource(uri: String): RoadbookPagingSource {
        return RoadbookPagingSource(
            uri = uri,
            context = context
        )
    }
}