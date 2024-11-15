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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import org.giste.navigator.R
import org.giste.navigator.model.Location
import org.giste.navigator.model.PdfPage
import org.giste.navigator.model.Settings
import org.mapsforge.core.model.LatLong
import org.mapsforge.core.model.Rotation
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.datastore.MapDataStore
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.rendertheme.internal.MapsforgeThemes

const val TRIP_PARTIAL = "TRIP_PARTIAL"
const val INCREASE_PARTIAL = "INCREASE_PARTIAL"
const val DECREASE_PARTIAL = "DECREASE_PARTIAL"
const val RESET_PARTIAL = "RESET_PARTIAL"
const val RESET_TRIP = "RESET_TRIP"

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
    map: MapDataStore?,
    location: Location?,
    modifier: Modifier = Modifier,
) {
    Log.d("Map", "Location: $location")

    if (map == null) {
        Text(
            text = location?.toString() ?: "Map",
            modifier = modifier
                .fillMaxSize()
                .wrapContentHeight(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            style = if (location == null) {
                MaterialTheme.typography.displayLarge
            } else {
                MaterialTheme.typography.labelMedium
            },
            textAlign = TextAlign.Center,
        )
    } else {
        Surface(
            modifier = modifier.fillMaxSize(),
        ) {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        mapScaleBar.isVisible = false
                        setBuiltInZoomControls(true)

                        val tileCache = AndroidUtil.createTileCache(
                            context, "mapcache",
                            model.displayModel.tileSize, 1f,
                            model.frameBufferModel.overdrawFactor
                        )

                        val tileRendererLayer = TileRendererLayer(
                            tileCache,
                            map,
                            model.mapViewPosition,
                            AndroidGraphicFactory.INSTANCE
                        )
                        tileRendererLayer.setXmlRenderTheme(MapsforgeThemes.MOTORIDER)

                        layerManager.layers.add(tileRendererLayer)

                        setCenter(LatLong(40.60092, -3.70806))
                        setZoomLevel(19)
                    }
                },
                modifier = modifier.fillMaxSize(),
                update = { view ->
                    view.apply {
                        location?.let {
                            setCenter(LatLong(it.latitude, it.longitude))
                            rotate(Rotation(it.bearing, 0.0f, 0.0f))
                        }
                    }
                }
            )
        }
    }

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
                text = stringResource(R.string.roadbook_load_message),
                modifier = modifier
                    .fillMaxSize()
                    .wrapContentHeight(),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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
                            ?: stringResource(R.string.roadbook_error_message),
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
                        text = stringResource(R.string.roadbook_loading_message),
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
    settings: Settings,
    onEvent: (NavigationViewModel.UiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showSettingsDialog = remember { mutableStateOf(false) }

    val selectRoadbookLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Update the state with the Uri
            onEvent(NavigationViewModel.UiAction.SetUri(uri))
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        CommandBarButton(
            onClick = { onEvent(NavigationViewModel.UiAction.DecreasePartial) },
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.partial_decrement_description),
            modifier = Modifier
                .weight(1f)
                .testTag(DECREASE_PARTIAL)
        )
        CommandBarButton(
            onClick = { onEvent(NavigationViewModel.UiAction.ResetPartial) },
            icon = Icons.Default.Refresh,
            contentDescription = stringResource(R.string.partial_reset_description),
            modifier = Modifier
                .weight(1f)
                .testTag(RESET_PARTIAL)
        )
        CommandBarButton(
            onClick = { onEvent(NavigationViewModel.UiAction.IncreasePartial) },
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(R.string.partial_increment_description),
            modifier = Modifier
                .weight(1f)
                .testTag(INCREASE_PARTIAL)
        )
        CommandBarButton(
            onClick = { onEvent(NavigationViewModel.UiAction.ResetTrip) },
            icon = Icons.Default.Clear,
            contentDescription = stringResource(R.string.trip_reset_description),
            modifier = Modifier
                .weight(1f)
                .testTag(RESET_TRIP)
        )
        CommandBarButton(
            onClick = { selectRoadbookLauncher.launch("application/pdf") },
            icon = Icons.Default.Search,
            contentDescription = stringResource(R.string.load_roadbook_description),
            modifier = Modifier.weight(1f)
        )
        CommandBarButton(
            onClick = { showSettingsDialog.value = true },
            icon = Icons.Default.Settings,
            contentDescription = stringResource(R.string.settings_description),
            modifier = Modifier.weight(1f)
        )
    }

    if (showSettingsDialog.value) {
        SettingsDialogScreen(
            showDialog = showSettingsDialog,
            settings = settings,
            onAccept = { onEvent(NavigationViewModel.UiAction.SetSettings(it)) }
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
