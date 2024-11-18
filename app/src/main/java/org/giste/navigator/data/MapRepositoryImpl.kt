package org.giste.navigator.data

import android.content.Context
import android.util.Log
import org.giste.navigator.model.MapRepository
import org.mapsforge.map.datastore.MapDataStore
import org.mapsforge.map.datastore.MultiMapDataStore
import org.mapsforge.map.reader.MapFile
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

private const val CLASS_NAME = "MapRepositoryImpl"
private const val MAPS_DIR = "maps"

class MapRepositoryImpl @Inject constructor(
    private val context: Context,
) : MapRepository {
    companion object {
        val MAP_LIST = listOf(
            "spain.map",
//            "madrid.map",
//            "castilla-la-mancha.map",
        )
    }

    override suspend fun getMap(): MapDataStore {
        val maps = MultiMapDataStore(MultiMapDataStore.DataPolicy.RETURN_ALL)
        getAllMaps().forEach { maps.addMapDataStore(it, false, false) }

        return maps
    }

    private fun copyMap(mapName: String) {
        val filesDir = context.filesDir
        val mapsDir = File(filesDir, MAPS_DIR)
        val file = File(mapsDir, mapName)

        Log.d(CLASS_NAME, "Copying map: ${file.path}")

        if (file.exists()) file.delete()
        file.createNewFile()

        val inputStream = context.assets.open("maps/$mapName")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        outputStream.flush()
        inputStream.close()
        outputStream.close()

        mapExists(mapName)
    }

    private fun mapExists(mapName: String): Boolean {
        val filesDir = context.filesDir
        val mapsDir = File(filesDir, MAPS_DIR)
        val file = File(mapsDir, mapName)

        Log.d(CLASS_NAME, "Checking map: ${file.path}; Exists: ${file.exists()}")

        return file.exists()
    }

    private fun copyMaps() {
        createMapsDir()

        MAP_LIST.forEach {
            if (!mapExists(it)) copyMap(it)
        }
    }

    private fun createMapsDir() {
        val filesDir = context.filesDir
        val mapsDir = File(filesDir, MAPS_DIR)

        Log.d(CLASS_NAME, "Creating maps dir: ${mapsDir.path}")

        //if (!mapsDir.exists()) mapsDir.mkdirs()
        mapsDir.mkdirs()

        Log.d(CLASS_NAME, "Maps dir exists: ${mapsDir.exists()}")
    }

    private fun getAllMaps(): List<MapDataStore> {
        val maps: MutableList<MapDataStore> = mutableListOf()
        val filesDir = context.filesDir
        val mapsDir = File(filesDir, MAPS_DIR)

        copyMaps()
        MAP_LIST.forEach {
            val file = File(mapsDir, it)
            maps.add(MapFile(file))
        }

        return maps
    }
}