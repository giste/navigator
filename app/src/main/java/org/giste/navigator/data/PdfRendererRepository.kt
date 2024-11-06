package org.giste.navigator.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.giste.navigator.model.PdfPage
import org.giste.navigator.model.PdfRepository
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

private const val ROADBOOK_FILE = "roadbook.pdf"
private const val CLASS_NAME = "PdfRendererRepository"

class PdfRendererRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : PdfRepository {

    override suspend fun getPdfStream(uri: Uri): Flow<PagingData<PdfPage>> {
        Log.d(CLASS_NAME, "Uri: $uri")

        val internalUri = openRoadbook(uri)

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

    private suspend fun openRoadbook(uri: Uri): Uri {
        val internalDir = context.filesDir
        val roadbookFile = File(internalDir, ROADBOOK_FILE)

        Log.d(CLASS_NAME, "Internal file: ${roadbookFile.canonicalFile}")

        withContext(Dispatchers.IO) {
            if (roadbookFile.exists()) roadbookFile.delete()
            roadbookFile.createNewFile()

            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Invalid URI: $uri")
            val outputStream = FileOutputStream(roadbookFile)

            inputStream.copyTo(outputStream)
            outputStream.flush()
            inputStream.close()
            outputStream.close()
        }

        return Uri.fromFile(roadbookFile)
    }
}