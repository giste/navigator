package org.giste.navigator.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PdfViewModel @Inject constructor(
    private val converter: PdfBitmapConverter
) : ViewModel() {

    sealed class PdfDisplayState() {
        data object Loading: PdfDisplayState()
        data class AllLoadedContent(val pages: List<Bitmap> = listOf()): PdfDisplayState()
        data class Error(val message: String): PdfDisplayState()
        data class PartiallyLoadedContent(val pages: Flow<PagingData<Bitmap>>): PdfDisplayState()
        data object NoPdf: PdfDisplayState()
    }

    private var uri: Uri? = null

    /**
     * The internal mutable state flow that holds the current display state of the PDF.
     */
    private val _displayState = MutableStateFlow<PdfDisplayState>(PdfDisplayState.NoPdf)

    /**
     * A public immutable state flow that exposes the current display state of the PDF.
     */
    val displayState = _displayState.asStateFlow()

    /**
     * A lazy-loaded `Pager` instance that handles pagination of PDF pages, using the `PdfBitmapConverter` as the paging source.
     */
    private val pager by lazy {
        Pager(
            PagingConfig(
                pageSize = 5,
                prefetchDistance = 10,
                enablePlaceholders = false,
                initialLoadSize = 7,
                maxSize = 100,
                jumpThreshold = 2,
            ),
            initialKey = 0,
            pagingSourceFactory = {
                converter
            },
        )
    }

    private fun showPdf() {
        viewModelScope.launch {
            val pageCount = converter.getPageCount()
            when {
                pageCount == 0 -> {
                    // No pages found in the PDF
                    _displayState.value = PdfDisplayState.Error("No pages found")
                }

                pageCount < 5 -> {
                    // Load all pages at once if there are fewer than 5
                    _displayState.value = PdfDisplayState.AllLoadedContent(converter.loadAllPages())
                }

                else -> {
                    // Use paging to load pages incrementally
                    _displayState.value = PdfDisplayState.PartiallyLoadedContent(pager.flow)
                }
            }
        }
    }

    fun onUriChange(uri: Uri) {
        converter.uri = uri
        showPdf()
    }
}