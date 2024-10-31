package org.giste.navigator.ui

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import org.giste.navigator.R
import org.giste.navigator.model.PdfPage
import org.giste.navigator.ui.theme.NavigatorTheme

@Preview(
    name = "Tab Active 3",
    showBackground = true,
    device = "spec:width=1920px,height=1200px,dpi=360, isRound=false, orientation=landscape"
)
@Composable
fun NavigationLandscapePreview() {
    NavigatorTheme {
        NavigationLandscapeContent(
            state = NavigationViewModel.TripState(123456, 1234567),
            roadbookState = NavigationViewModel.RoadbookState.NotLoaded,
            onEvent = {},
        )
    }
}

@Composable
fun NavigationLandscapeScreen(
    tripState: NavigationViewModel.TripState,
    roadbookState: NavigationViewModel.RoadbookState,
    onEvent: (NavigationViewModel.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationLandscapeContent(
        state = tripState,
        roadbookState = roadbookState,
        onEvent = onEvent,
        modifier = modifier,
    )
}

@Composable
fun NavigationLandscapeContent(
    state: NavigationViewModel.TripState,
    roadbookState: NavigationViewModel.RoadbookState,
    onEvent: (NavigationViewModel.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val padding = 4.dp
    val showPartialSettingDialog = remember { mutableStateOf(false) }
    val showTotalSettingDialog = remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .weight(9f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            ) {
                TripTotal(
                    distance = "%,.2f".format(state.total.div(1000f)),
                    onClick = {},
                    modifier = Modifier
                        .weight(.9f)
                        .padding(horizontal = padding),
                )
                HorizontalDivider()
                TripPartial(
                    distance = "%,.2f".format(state.partial.div(1000f)),
                    onClick = { showPartialSettingDialog.value = true },
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(horizontal = padding),
                )
                HorizontalDivider()
                Map(
                    modifier = Modifier
                        .weight(5f)
                        .padding(padding)
                )
            }
            VerticalDivider()
            Roadbook(
                roadbookState = roadbookState,
                modifier = Modifier
                    .weight(5f)
                    .padding(padding)
            )
        }
        CommandBar(
            onEvent = onEvent,
            modifier = Modifier.weight(1f)
        )
    }

    if (showPartialSettingDialog.value) {
        DistanceSettingDialog(
            showDialog = showPartialSettingDialog,
            title = stringResource(R.string.partial_label),
            text = state.partial.div(10).toString(),
            numberOfIntegerDigits = 3,
            numberOfDecimals = 2,
            onAccept = { onEvent(NavigationViewModel.UiEvent.SetPartial(it)) }
        )
    }

    if (showTotalSettingDialog.value) {
        DistanceSettingDialog(
            showDialog = showTotalSettingDialog,
            title = stringResource(R.string.total_label),
            text = state.total.div(10).toString(),
            numberOfIntegerDigits = 4,
            numberOfDecimals = 2,
            onAccept = { onEvent(NavigationViewModel.UiEvent.SetTotal(it)) }
        )
    }
}

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
            RoadbookViewer(
                pages = roadbookState.pages.collectAsLazyPagingItems(),
                modifier = modifier
            )
        }
    }
}

@Composable
fun RoadbookViewer(
    pages: LazyPagingItems<PdfPage>,
    modifier: Modifier = Modifier,
) {
    when (pages.loadState.refresh) {
        is LoadState.Error -> {
            Text(
                text = (pages.loadState.refresh as LoadState.Error).error.message
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
            LazyColumn(modifier = modifier) {
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
            icon = Icons.Default.KeyboardArrowDown,
            contentDescription = "Decrease partial",
            modifier = Modifier.weight(1f)
        )
        CommandBarButton(
            onClick = { onEvent(NavigationViewModel.UiEvent.ResetPartial) },
            icon = Icons.Default.Refresh,
            contentDescription = "Reset partial",
            modifier = Modifier.weight(1f)
        )
        CommandBarButton(
            onClick = { onEvent(NavigationViewModel.UiEvent.IncreasePartial) },
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Increase partial",
            modifier = Modifier.weight(1f)
        )
        CommandBarButton(
            onClick = { onEvent(NavigationViewModel.UiEvent.ResetAll) },
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
