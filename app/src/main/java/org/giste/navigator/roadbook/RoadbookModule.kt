package org.giste.navigator.roadbook

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RoadbookModule {
    @Singleton
    @Binds
    abstract fun bindRoadbookDatasource(
        roadbookDatasourceLocal: RoadbookDatasourceLocal
    ): RoadbookDatasource
}