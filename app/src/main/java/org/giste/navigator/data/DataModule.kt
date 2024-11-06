package org.giste.navigator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.giste.navigator.model.LocationRepository
import org.giste.navigator.model.PdfRepository
import org.giste.navigator.model.StateRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DataModule {
    @Provides
    @Singleton
    fun provideLocationRepository(@ApplicationContext appContext: Context): LocationRepository =
        LocationManagerRepository(appContext)

    @Provides
    @Singleton
    fun providePdfRepository(@ApplicationContext appContext: Context): PdfRepository =
        PdfRendererRepository(appContext)

    @Singleton
    @Provides
    fun provideStateDatastore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            produceFile = { context.preferencesDataStoreFile("state.preferences_pb") }
        )
    }

    @Singleton
    @Provides
    fun provideTripRepository(stateDataStore: DataStore<Preferences>): StateRepository {
        return DataStoreStateRepository(stateDataStore)
    }
}