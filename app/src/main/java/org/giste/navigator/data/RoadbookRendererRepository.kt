package org.giste.navigator.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.giste.navigator.model.PdfPage
import org.giste.navigator.model.RoadbookRepository
import org.giste.navigator.model.RoadbookScroll
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

private const val ROADBOOK_FILE = "roadbook.pdf"
private const val CLASS_NAME = "PdfRendererRepository"

class RoadbookRendererRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : RoadbookRepository {
    companion object {
        val ROADBOOK_URI = stringPreferencesKey("ROADBOOK_URI")
        val ROADBOOK_PAGE_INDEX = intPreferencesKey("ROADBOOK_PAGE_INDEX")
        val ROADBOOK_PAGE_OFFSET = intPreferencesKey("ROADBOOK_PAGE_OFFSET")
    }
    override suspend fun getPages(): Flow<PagingData<PdfPage>> {
        val internalUri = getInternalUri()

        Log.d(CLASS_NAME, "InternalUri: $internalUri")

        if (internalUri == Uri.EMPTY) return emptyFlow()

        return Pager(
            PagingConfig(
                pageSize = 5,
//                prefetchDistance = 10,
//                enablePlaceholders = false,
                initialLoadSize = 7,
                maxSize = 30,
//                jumpThreshold = 2,
            ),
            initialKey = 0,
        ) {
            PdfPagingSource(PdfRendererService(internalUri, context))
        }.flow
    }

    override suspend fun load(roadbookUri: String) {
        val uri = Uri.parse(roadbookUri)
        val roadbookFile = File(context.filesDir, ROADBOOK_FILE)

        withContext(dispatcher) {
            if (roadbookFile.exists()) roadbookFile.delete()
            roadbookFile.createNewFile()

            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Invalid URI: $roadbookUri")
            val outputStream = FileOutputStream(roadbookFile)

            inputStream.copyTo(outputStream)
            outputStream.flush()
            inputStream.close()
            outputStream.close()

            saveUri(roadbookUri)
            // New pdf, reset scroll
            setScroll(RoadbookScroll())
        }
        Log.d(CLASS_NAME, "Loaded roadbook: ${roadbookFile.canonicalFile}")
    }

    override fun getRoadbookUri(): Flow<String> {
        return dataStore.data.map { it[ROADBOOK_URI] ?: "" }
    }

    override fun getScroll(): Flow<RoadbookScroll> {
        return dataStore.data.map {
            Log.v(
                CLASS_NAME,
                "getScroll = (${it[ROADBOOK_PAGE_INDEX]}, ${it[ROADBOOK_PAGE_OFFSET]})"
            )
            RoadbookScroll(
                pageIndex = it[ROADBOOK_PAGE_INDEX] ?: 0,
                pageOffset = it[ROADBOOK_PAGE_OFFSET] ?: 0
            )
        }
    }

    override suspend fun setScroll(roadbookScroll: RoadbookScroll) {
        dataStore.edit {
            Log.v(
                CLASS_NAME,
                "setScroll(${roadbookScroll.pageIndex}, ${roadbookScroll.pageOffset})"
            )
            it[ROADBOOK_PAGE_INDEX] = roadbookScroll.pageIndex
            it[ROADBOOK_PAGE_OFFSET] = roadbookScroll.pageOffset
        }
    }

    private fun getInternalUri(): Uri {
        val file = File(context.filesDir, ROADBOOK_FILE)

        return if (file.exists()) {
            Uri.fromFile(file)
        } else {
            Uri.EMPTY
        }
    }

    private suspend fun saveUri(uri: String) {
        dataStore.edit { it[ROADBOOK_URI] = uri }
    }
}