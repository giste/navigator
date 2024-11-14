package org.giste.navigator.model

import org.mapsforge.map.datastore.MapDataStore

interface MapRepository {
    suspend fun getMap(): MapDataStore
}