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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import org.oscim.android.MapView
import org.oscim.backend.CanvasAdapter
import org.oscim.layers.tile.buildings.BuildingLayer
import org.oscim.layers.tile.vector.VectorTileLayer
import org.oscim.layers.tile.vector.labeling.LabelLayer
import org.oscim.renderer.GLViewport
import org.oscim.scalebar.DefaultMapScaleBar
import org.oscim.scalebar.MapScaleBar
import org.oscim.scalebar.MapScaleBarLayer
import org.oscim.theme.internal.VtmThemes
import org.oscim.tiling.source.mapfile.MapFileTileSource
import org.oscim.tiling.source.mapfile.MultiMapFileTileSource

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
    map: List<String>,
    location: Location?,
    modifier: Modifier = Modifier,
) {
    Log.d("Map", "Location: $location")

    if (map.isEmpty()) {
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
            VtmMapView(
                maps = map,
                location = location,
                modifier = modifier,
            )
        }
    }
}

@Composable
fun VtmMapView(
    maps: List<String>,
    location: Location?,
    modifier: Modifier = Modifier
) {
    val mapView = rememberMapViewWithLifecycle()

    AndroidView(
        factory = {
            with(mapView.map()) {
                // Scale bar
                val mapScaleBar: MapScaleBar = DefaultMapScaleBar(mapView.map())
                val mapScaleBarLayer = MapScaleBarLayer(mapView.map(), mapScaleBar)
                mapScaleBarLayer.renderer.setPosition(GLViewport.Position.BOTTOM_LEFT)
                mapScaleBarLayer.renderer.setOffset(5 * CanvasAdapter.getScale(), 0f)
                mapView.map().layers().add(mapScaleBarLayer)

                // Til source from maps
                val tileSource = MultiMapFileTileSource()
                maps.forEach {
                    val map = MapFileTileSource()
                    map.setMapFile(it)
                    val result = tileSource.add(map)

                    Log.d("VtmMapView", "Added map: $it with result: $result")
                }

                // Vector layer
                val tileLayer: VectorTileLayer = mapView.map().setBaseMap(tileSource)

                // Building layer
                layers().add(BuildingLayer(mapView.map(), tileLayer))

                // Label layer
                layers().add(LabelLayer(mapView.map(), tileLayer))

                // Render theme
                setTheme(VtmThemes.DEFAULT)

                // Initial position, scale and tilt
                val mapPosition = this.mapPosition
                mapPosition
                    .setPosition(40.60092, -3.70806)
                    .setScale((1 shl 19).toDouble())
                    .setTilt(60.0f)
                setMapPosition(mapPosition)
                Log.d("VtmMapView", "Initial map position: ${this.mapPosition}")
            }

            mapView
        },
        modifier = modifier.fillMaxSize(),
    ) { view ->
        with(view.map()) {
            location?.let { location ->
                val mapPosition = this.mapPosition
                mapPosition.setPosition(location.latitude, location.longitude)
                mapPosition.setBearing(location.bearing)
                this.animator().animateTo(mapPosition)
            }
        }
    }
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val observer = remember { VtmMapViewLifecycleObserver(mapView) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(Unit) {
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    return mapView
}

private class VtmMapViewLifecycleObserver(private val mapView: MapView) : DefaultLifecycleObserver {
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        mapView.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        mapView.onPause()


    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        mapView.onDestroy()
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
