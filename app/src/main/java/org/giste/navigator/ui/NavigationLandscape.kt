package org.giste.navigator.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.giste.navigator.R
import org.giste.navigator.ui.theme.NavigatorTheme

const val NAVIGATION_LANDSCAPE = "NAVIGATION_LANDSCAPE"

@Preview(
    name = "Tab Active 3",
    showBackground = true,
    device = "spec:width=1920px,height=1200px,dpi=360, isRound=false, orientation=landscape"
)
@Composable
fun NavigationLandscapePreview() {
    NavigatorTheme {
        NavigationLandscapeContent(
            state = NavigationViewModel.NavigationState(123456, 1234567),
            onEvent = {},
        )
    }
}

@Composable
fun NavigationLandscapeScreen(
    navigationState: NavigationViewModel.NavigationState,
    onEvent: (NavigationViewModel.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationLandscapeContent(
        state = navigationState,
        onEvent = onEvent,
        modifier = modifier,
    )
}

@Composable
fun NavigationLandscapeContent(
    state: NavigationViewModel.NavigationState,
    onEvent: (NavigationViewModel.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val padding = 4.dp
    val showPartialSettingDialog = remember { mutableStateOf(false) }
    val showTotalSettingDialog = remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val pdfState = rememberLazyListState()
    val numberOfPixels = 317.0f

    Column(
        modifier = modifier
            .testTag(NAVIGATION_LANDSCAPE)
            .fillMaxSize()
            .focusable()
            .onKeyEvent {
                if (it.type == KeyEventType.KeyUp) {
                    when (it.key) {
                        Key.DirectionRight -> {
                            onEvent(NavigationViewModel.UiEvent.IncreasePartial)
                            return@onKeyEvent true
                        }

                        Key.DirectionLeft -> {
                            onEvent(NavigationViewModel.UiEvent.DecreasePartial)
                            return@onKeyEvent true
                        }

                        Key.F6 -> {
                            onEvent(NavigationViewModel.UiEvent.ResetPartial)
                            return@onKeyEvent true
                        }

                        Key.DirectionUp -> {
                            coroutineScope.launch {
                                pdfState.animateScrollBy(numberOfPixels)
                            }
                            return@onKeyEvent true
                        }

                        Key.DirectionDown -> {
                            coroutineScope.launch {
                                pdfState.animateScrollBy(-numberOfPixels)
                            }
                            return@onKeyEvent true
                        }

                        else -> return@onKeyEvent false
                    }
                } else {
                    false
                }
            }
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
                roadbookState = state.roadbookState,
                state = pdfState,
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
