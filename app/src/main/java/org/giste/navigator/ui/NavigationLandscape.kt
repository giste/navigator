package org.giste.navigator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
            state = NavigationViewModel.UiState(123456, 1234567 ),
            onEvent = {},
        )
    }
}

@Composable
fun NavigationLandscapeScreen (
    state: NavigationViewModel.UiState,
    onEvent: (NavigationViewModel.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationLandscapeContent(
        state = state,
        onEvent =  onEvent,
        modifier =  modifier,
    )
}

@Composable
fun NavigationLandscapeContent(
    state: NavigationViewModel.UiState,
    onEvent: (NavigationViewModel.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val padding = 4.dp

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
                DistanceTotal(
                    distance = "%,.2f".format(state.total.div(1000f)),
                    onClick = {},
                    modifier = Modifier.weight(.9f).padding(horizontal = padding),
                )
                HorizontalDivider()
                DistancePartial(
                    distance = "%,.2f".format(state.partial.div(1000f)),
                    onClick = {},
                    modifier = Modifier.weight(1.2f).padding(horizontal = padding),
                )
                HorizontalDivider()
                Map(
                    modifier = Modifier.weight(5f).padding(padding)
                )
            }
            VerticalDivider()
            Roadbook(
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
}

@Composable
fun DistanceTotal(
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
fun DistancePartial(
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
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Roadbook",
        style = MaterialTheme.typography.displayLarge,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxSize()
            .wrapContentHeight()
    )
}

@Composable
fun CommandBar(
    onEvent: (NavigationViewModel.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            onClick = {},
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
