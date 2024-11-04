package org.giste.navigator

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import org.giste.navigator.ui.ManagePermissions
import org.giste.navigator.ui.NavigationLandscapeScreen
import org.giste.navigator.ui.NavigationViewModel
import org.giste.navigator.ui.theme.NavigatorTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val activity = LocalContext.current as Activity
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            NavigatorTheme {
                NavigationScreen()
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=1200px,height=1920px,dpi=360, isRound=false, orientation=landscape"
)
@Composable
fun NavigationPreview() {
    NavigatorTheme {
        NavigationContent(
            state = NavigationViewModel.NavigationState(),
            onEvent = {},
        )
    }
}

@Composable
fun NavigationScreen(vm: NavigationViewModel = viewModel()) {
    NavigationContent(
        state = vm.tripState.collectAsStateWithLifecycle().value,
        onEvent = vm::onEvent,
    )
}

@Composable
fun NavigationContent(
    state: NavigationViewModel.NavigationState,
    onEvent: (NavigationViewModel.UiEvent) -> Unit,
) {
    ManagePermissions()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        NavigationLandscapeScreen(
            navigationState = state,
            onEvent = onEvent,
            modifier = Modifier
                .padding(innerPadding)
                // Consume this insets so that it's not applied again when using safeDrawing in the hierarchy below
                .consumeWindowInsets(innerPadding)
        )
    }
}
