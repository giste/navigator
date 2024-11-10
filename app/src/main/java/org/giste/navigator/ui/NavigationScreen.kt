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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.giste.navigator.ui.theme.NavigatorTheme

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
            onEvent = viewModel::onAction,
        )
    }
}

@Composable
fun NavigationContent(
    state: NavigationViewModel.UiState,
    onEvent: (NavigationViewModel.UiAction) -> Unit,
) {
    Log.d("NavigationContent", "Composing with Scroll(${state.pageOffset})")
    val coroutineScope = rememberCoroutineScope()
    val pdfState = rememberLazyListState(initialFirstVisibleItemScrollOffset = state.pageOffset)
    val numberOfPixels = 317.0f

    LaunchedEffect(pdfState.isScrollInProgress) {
        if (!pdfState.isScrollInProgress) {
            Log.d("NavigationContent", "Saving scroll(${pdfState.firstVisibleItemScrollOffset})")
            onEvent(NavigationViewModel.UiAction.SetScroll(pdfState.firstVisibleItemScrollOffset))
        }
    }

    ManagePermissions()
    Scaffold(
        modifier = Modifier
            .testTag(NAVIGATION_CONTENT)
            .fillMaxSize()
            .focusable()
            .onKeyEvent {
                if (it.type == KeyEventType.KeyUp) {
                    when (it.key) {
                        Key.DirectionRight -> {
                            onEvent(NavigationViewModel.UiAction.IncreasePartial)
                            return@onKeyEvent true
                        }

                        Key.DirectionLeft -> {
                            onEvent(NavigationViewModel.UiAction.DecreasePartial)
                            return@onKeyEvent true
                        }

                        Key.F6 -> {
                            onEvent(NavigationViewModel.UiAction.ResetPartial)
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
            pdfState = pdfState,
            modifier = Modifier
                .padding(innerPadding)
                // Consume this insets so that it's not applied again when using safeDrawing in the hierarchy below
                .consumeWindowInsets(innerPadding)
        )
    }
}
