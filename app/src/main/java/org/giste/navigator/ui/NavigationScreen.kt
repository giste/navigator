package org.giste.navigator.ui

import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.giste.navigator.model.Settings
import org.giste.navigator.ui.theme.NavigatorTheme
import org.mapsforge.map.datastore.MapDataStore

const val NAVIGATION_CONTENT = "NAVIGATION_CONTENT"

@Preview(
    showBackground = true,
    device = "spec:width=1200px,height=1920px,dpi=360, isRound=false, orientation=landscape"
)
@Composable
fun NavigationPreview() {
    NavigatorTheme {
        NavigationContent(
            state = NavigationViewModel.UiState(),
            map = null,
            settings = Settings(),
            onEvent = {},
        )
    }
}

@Composable
fun NavigationScreen(viewModel: NavigationViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    if (viewModel.initialized) {
        NavigationContent(
            state = viewModel.uiState.collectAsStateWithLifecycle().value,
            map = viewModel.mapState.collectAsStateWithLifecycle().value,
            settings = viewModel.settingState.collectAsStateWithLifecycle().value,
            onEvent = viewModel::onAction,
        )
    }
}

@Composable
fun NavigationContent(
    state: NavigationViewModel.UiState,
    map: MapDataStore?,
    settings: Settings,
    onEvent: (NavigationViewModel.UiAction) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val roadbookState = rememberLazyListState(
        initialFirstVisibleItemIndex = state.pageIndex,
        initialFirstVisibleItemScrollOffset = state.pageOffset
    )
    val numberOfPixels = 317.0f
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(state.roadbookState) {
        // Move to top on roadbook change
        roadbookState.animateScrollToItem(0,0)
    }

    LaunchedEffect(roadbookState.isScrollInProgress) {
        if (!roadbookState.isScrollInProgress) {
            Log.d(
                "NavigationContent",
                "Saving scroll(${roadbookState.firstVisibleItemScrollOffset})"
            )
            onEvent(
                NavigationViewModel.UiAction.SetScroll(
                    pageIndex = roadbookState.firstVisibleItemIndex,
                    pageOffset = roadbookState.firstVisibleItemScrollOffset
                )
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .testTag(NAVIGATION_CONTENT)
            .fillMaxSize()
            .focusable()
            .focusRequester(focusRequester)
            .onKeyEvent {
                if (it.type == KeyEventType.KeyUp) {
                    when (it.key.nativeKeyCode) {
                        NativeKeyEvent.KEYCODE_DPAD_RIGHT -> {
                            onEvent(NavigationViewModel.UiAction.IncreasePartial)
                            return@onKeyEvent true
                        }

                        NativeKeyEvent.KEYCODE_DPAD_LEFT -> {
                            onEvent(NavigationViewModel.UiAction.DecreasePartial)
                            return@onKeyEvent true
                        }

                        NativeKeyEvent.KEYCODE_F6 -> {
                            onEvent(NavigationViewModel.UiAction.ResetPartial)
                            return@onKeyEvent true
                        }

                        NativeKeyEvent.KEYCODE_DPAD_UP -> {
                            coroutineScope.launch {
                                roadbookState.animateScrollBy(numberOfPixels)
                            }
                            return@onKeyEvent true
                        }

                        NativeKeyEvent.KEYCODE_DPAD_DOWN -> {
                            coroutineScope.launch {
                                roadbookState.animateScrollBy(-numberOfPixels)
                            }
                            return@onKeyEvent true
                        }

                        else -> {
                            Log.d("NavigationContent", "KeyEvent( ${it.key.nativeKeyCode}) up not processed")
                            return@onKeyEvent false
                        }
                    }
                } else {
                    Log.d("NavigationContent", "KeyEvent(${it.type}, ${it.key.nativeKeyCode}) not processed")
                    false
                }
            },
    ) { innerPadding ->
        NavigationLandscapeScreen(
            state = state,
            onEvent = onEvent,
            pdfState = roadbookState,
            map = map,
            settings = settings,
            modifier = Modifier
                .padding(innerPadding)
                // Consume this insets so that it's not applied again
                // when using safeDrawing in the hierarchy below
                .consumeWindowInsets(innerPadding)
        )
    }
}
