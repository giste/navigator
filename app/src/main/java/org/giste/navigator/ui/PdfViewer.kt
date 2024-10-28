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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage


@Composable
fun PdfViewer(
    bitmaps: LazyPagingItems<Bitmap>,
    modifier: Modifier = Modifier,
) {
    when (bitmaps.loadState.refresh) {
        is LoadState.Error -> {
            Log.d("PdfViewer", "Error")
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        modifier = Modifier.size(48.dp),
                    )
                    Text(text = (bitmaps.loadState.refresh as LoadState.Error).error.message ?: "Unexpected error")
                }
            }
        }

        is LoadState.Loading -> {
            Log.d("PdfViewer", "Loading")
            LoadingUi()
        }

        else -> {
            Log.d("PdfViewer", "Display")
            LazyColumn(modifier = modifier) {
                items(
                    count = bitmaps.itemCount,
                    key = bitmaps.itemKey(),
                    contentType = bitmaps.itemContentType()
                ) { index ->
                    PdfPage(bitmaps[index]!!)
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
            .drawWithContent { drawContent() },
        contentScale = ContentScale.FillWidth
    )
}