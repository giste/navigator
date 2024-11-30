package org.giste.navigator.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.giste.navigator.R
import org.giste.navigator.model.Settings
import org.giste.navigator.model.Trip
import org.giste.navigator.ui.theme.NavigatorTheme

@Preview(
    name = "Tab Active 3",
    showBackground = true,
    device = "spec:width=1920px,height=1200px,dpi=360, isRound=false, orientation=landscape"
)
@Composable
fun NavigationLandscapePreview() {
    NavigatorTheme {
        NavigationLandscapeScreen(
            state = NavigationViewModel.UiState(trip = Trip(123456, 1234567)),
            onEvent = {},
            pdfState = LazyListState(),
            settings = Settings(),
            maps = listOf(),
        )
    }
}

@Composable
fun NavigationLandscapeScreen(
    state: NavigationViewModel.UiState,
    onEvent: (NavigationViewModel.UiAction) -> Unit,
    pdfState: LazyListState,
    settings: Settings,
    maps: List<String>,
    modifier: Modifier = Modifier,
) {
    val padding = 4.dp
    val showPartialSettingDialog = remember { mutableStateOf(false) }
    val showTotalSettingDialog = remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
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
                    distance = "%,.2f".format(state.trip.total.div(1000f)),
                    onClick = {},
                    modifier = Modifier
                        .weight(.9f)
                        .padding(horizontal = padding),
                )
                HorizontalDivider()
                TripPartial(
                    distance = "%,.2f".format(state.trip.partial.div(1000f)),
                    onClick = { showPartialSettingDialog.value = true },
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(horizontal = padding),
                )
                HorizontalDivider()
                Map(
                    location = state.location,
                    map = maps,
                    modifier = Modifier
                        .weight(5f)
                        .padding(padding),
                )
            }
            VerticalDivider()
            Roadbook(
                roadbookState = state.roadbookState,
                state = pdfState,
                modifier = Modifier
                    .weight(5f)
                    .padding(padding)
            )
        }
        CommandBar(
            settings = settings,
            onEvent = onEvent,
            modifier = Modifier.weight(1f)
        )
    }

    if (showPartialSettingDialog.value) {
        DistanceSettingDialog(
            showDialog = showPartialSettingDialog,
            title = stringResource(R.string.partial_label),
            text = state.trip.partial.div(10).toString(),
            numberOfIntegerDigits = 3,
            numberOfDecimals = 2,
            onAccept = { onEvent(NavigationViewModel.UiAction.SetPartial(it)) }
        )
    }

    if (showTotalSettingDialog.value) {
        DistanceSettingDialog(
            showDialog = showTotalSettingDialog,
            title = stringResource(R.string.total_label),
            text = state.trip.total.div(10).toString(),
            numberOfIntegerDigits = 4,
            numberOfDecimals = 2,
            onAccept = { onEvent(NavigationViewModel.UiAction.SetTotal(it)) }
        )
    }

}
