package org.giste.navigator.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.giste.navigator.model.MapRepository
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

private const val CLASS_NAME = "MapLocalRepository"
private const val MAPS_DIR = "maps"
private const val MAP_EXTENSION = "map"

class MapLocalRepository @Inject constructor(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MapRepository {
    companion object {
        val MAP_LIST = listOf(
            "madrid.map",
        )
    }

    override fun getMaps(): List<String> {
        val maps = mutableListOf<String>()
        val mapsDir = File(context.filesDir, MAPS_DIR)

        copyMaps()

        mapsDir.walkTopDown().forEach {
            Log.d(CLASS_NAME, "Evaluating file: ${it.path}")
            if (it.isFile && it.extension == MAP_EXTENSION) {
                maps.add(it.path)
            }
        }

        Log.d(CLASS_NAME, "getMaps() = $maps")

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

}