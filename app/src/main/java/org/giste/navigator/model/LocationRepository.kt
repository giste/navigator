package org.giste.navigator.model

import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun listenToLocation(minTime: Long, minDistance: Float): Flow<Location>
    fun hasLocationPermission(): Boolean
}