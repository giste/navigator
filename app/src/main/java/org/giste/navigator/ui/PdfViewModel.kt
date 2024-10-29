package org.giste.navigator.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PdfViewModel @Inject constructor(
    private val pdfRepository: PdfRepository,
) : ViewModel() {

    sealed class PdfDisplayState() {
        data object NoPdf : PdfDisplayState()
        data class LoadedContent(val pages: Flow<PagingData<PdfPage>>) : PdfDisplayState()
    }

    private var uri = mutableStateOf(Uri.EMPTY)

    /**
     * The internal mutable state flow that holds the current display state of the PDF.
     */
    private val _displayState = MutableStateFlow<PdfDisplayState>(PdfDisplayState.NoPdf)

    /**
     * A public immutable state flow that exposes the current display state of the PDF.
     */
    val displayState = _displayState.asStateFlow()

    fun setUri(uri: Uri) {
        this.uri.value = uri
        this._displayState.value = PdfDisplayState.LoadedContent(pdfRepository.getPdfStream(uri))
    }

}