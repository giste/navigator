package org.giste.navigator.data

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.giste.navigator.ui.LocationRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DataModule {
    @Provides
    @Singleton
    fun provideLocationRepository(@ApplicationContext appContext: Context): LocationRepository =
        LocationManagerRepository(appContext)
}