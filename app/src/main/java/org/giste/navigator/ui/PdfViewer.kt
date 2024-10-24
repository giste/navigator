package org.giste.navigator.ui

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage


@Composable
fun PdfViewer(
    state: PdfViewModel.PdfDisplayState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is PdfViewModel.PdfDisplayState.AllLoadedContent -> {
            Log.d("PdfViewer", "AllLoadedContent")

            LazyColumn(modifier = modifier) {
                items(state.pages.size) { index ->
                    PdfPage(page = state.pages[index])
                }
            }
        }

        is PdfViewModel.PdfDisplayState.Error -> {
            Log.d("PdfViewer", "Error")
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        modifier = Modifier.size(48.dp),
                    )
                    Text(text = state.message)
                }
            }
        }

        PdfViewModel.PdfDisplayState.Loading -> {
            Log.d("PdfViewer", "Loading")
            LoadingUi()
        }

        PdfViewModel.PdfDisplayState.NoPdf -> {
            Log.d("PdfViewer", "NoPdf")
            Text("Please, load a pdf")
        }

        is PdfViewModel.PdfDisplayState.PartiallyLoadedContent -> {
            Log.d("PdfViewer", "PartiallyLoadedContent")
            val items = state.pages.collectAsLazyPagingItems()
            when (val pageState = items.loadState.refresh) {
                is LoadState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                modifier = Modifier.size(48.dp),
                            )
                            Text(text = "Error loading pages")
                        }
                    }
                }

                LoadState.Loading -> {
                    LoadingUi()
                }

                is LoadState.NotLoading -> {
                    LazyColumn(modifier = modifier) {
                        items(items.itemCount) { index ->
                            val page = items[index]
                            if (page != null) {
                                PdfPage(page = page)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingUi() {
    Text(text = "Loading", modifier = Modifier.fillMaxSize())
}

@Composable
private fun PdfPage(
    page: Bitmap,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = page,
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(page.width.toFloat() / page.height.toFloat())
            .drawWithContent {
                drawContent()

                val scaleFactorX = size.width / page.width
                val scaleFactorY = size.height / page.height
            },
        contentScale = ContentScale.FillWidth
    )
}