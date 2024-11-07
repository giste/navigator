package org.giste.navigator.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import org.giste.navigator.model.PdfPage
import org.giste.navigator.model.RoadbookRepository
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

private const val ROADBOOK_FILE = "roadbook.pdf"
private const val CLASS_NAME = "PdfRendererRepository"

class RoadbookRendererRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : RoadbookRepository {
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
        }
        Log.d(CLASS_NAME, "Loaded roadbook: ${roadbookFile.canonicalFile}")
    }

    private fun getInternalUri(): Uri {
        val file = File(context.filesDir, ROADBOOK_FILE)

        return if (file.exists()) {
            Uri.fromFile(file)
        } else {
            Uri.EMPTY
        }
    }

}