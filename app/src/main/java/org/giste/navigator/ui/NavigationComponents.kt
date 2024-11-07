package org.giste.navigator.ui

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import org.giste.navigator.model.PdfPage

const val TRIP_PARTIAL = "TRIP_PARTIAL"
const val INCREASE_PARTIAL = "INCREASE_PARTIAL"
const val DECREASE_PARTIAL = "DECREASE_PARTIAL"
const val RESET_PARTIAL = "RESET_PARTIAL"

@Composable
fun TripTotal(
    distance: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = distance,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.displayMedium,
        textAlign = TextAlign.End,
        modifier = modifier
            .fillMaxSize()
            .wrapContentHeight()
            .clickable { onClick() }
    )
}

@Composable
fun TripPartial(
    distance: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = distance,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.displayLarge,
        textAlign = TextAlign.End,
        modifier = modifier
            .testTag(TRIP_PARTIAL)
            .fillMaxSize()
            .wrapContentHeight()
            .clickable { onClick() }
    )
}

@Composable
fun Map(
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Map",
        style = MaterialTheme.typography.displayLarge,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxSize()
            .wrapContentHeight()
    )
}

@Composable
fun Roadbook(
    roadbookState: NavigationViewModel.RoadbookState,
    state: LazyListState,
    modifier: Modifier = Modifier,
) {
    when (roadbookState) {
        is NavigationViewModel.RoadbookState.NotLoaded -> {
            Text(
                text = "Load a Roadbook",
                modifier = modifier
                    .fillMaxSize()
                    .wrapContentHeight(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
            )
        }

        is NavigationViewModel.RoadbookState.Loaded -> {
            val pages = roadbookState.pages.collectAsLazyPagingItems()

            when (val loadState = pages.loadState.refresh) {
                is LoadState.Error -> {
                    Text(
                        text = loadState.error.message
                            ?: "Unexpected error",
                        modifier = modifier
                            .fillMaxSize()
                            .wrapContentHeight(),
                        color = MaterialTheme.colorScheme.onError,
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center,
                    )
                }

                is LoadState.Loading -> {
                    Text(
                        text = "Loading...",
                        modifier = modifier
                            .fillMaxSize()
                            .wrapContentHeight(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> {
                    RoadbookViewer(
                        pages = pages,
                        state = state,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}

@Composable
fun RoadbookViewer(
    pages: LazyPagingItems<PdfPage>,
    state: LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        state = state,
    ) {
        items(
            count = pages.itemCount,
            key = pages.itemKey(),
            contentType = pages.itemContentType()
        ) { index ->
            Log.d("PdfViewer", "index: $index")
            val pdfPage = pages[index]
            pdfPage?.let {
                RoadbookPage(it.bitmap)
            }
        }
    }
}

@Composable
private fun RoadbookPage(
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

@Composable
fun CommandBar(
    onEvent: (NavigationViewModel.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectRoadbookLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Update the state with the Uri
            onEvent(NavigationViewModel.UiEvent.SetUri(uri))
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        CommandBarButton(
            onClick = { onEvent(NavigationViewModel.UiEvent.DecreasePartial) },
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Decrease partial",
            modifier = Modifier
                .weight(1f)
                .testTag(DECREASE_PARTIAL)
        )
        CommandBarButton(
            onClick = { onEvent(NavigationViewModel.UiEvent.ResetPartial) },
            icon = Icons.Default.Refresh,
            contentDescription = "Reset partial",
            modifier = Modifier
                .weight(1f)
                .testTag(RESET_PARTIAL)
        )
        CommandBarButton(
            onClick = { onEvent(NavigationViewModel.UiEvent.IncreasePartial) },
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Increase partial",
            modifier = Modifier
                .weight(1f)
                .testTag(INCREASE_PARTIAL)
        )
        CommandBarButton(
            onClick = { onEvent(NavigationViewModel.UiEvent.ResetTrip) },
            icon = Icons.Default.Clear,
            contentDescription = "Reset All",
            modifier = Modifier.weight(1f)
        )
        CommandBarButton(
            onClick = { selectRoadbookLauncher.launch("application/pdf") },
            icon = Icons.Default.Search,
            contentDescription = "Load roadbook",
            modifier = Modifier.weight(1f)
        )
        CommandBarButton(
            onClick = {},
            icon = Icons.Default.Settings,
            contentDescription = "Settings",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CommandBarButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { onClick() },
        modifier = modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
