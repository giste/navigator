package org.giste.navigator.roadbook

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RoadbookModule {
    @Singleton
    @Provides
    fun providePagingSourceFactory(@ApplicationContext context: Context): PagingSourceFactory {
        return RoadbookPagingSourceFactory(context)
    }
}