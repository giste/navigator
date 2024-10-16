package org.giste.navigator

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import org.giste.navigator.ui.NavigationLandscapeScreen
import org.giste.navigator.ui.NavigationViewModel
import org.giste.navigator.ui.theme.NavigatorTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: NavigationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val activity = LocalContext.current as Activity
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            NavigatorTheme {
                NavigationScreen(viewModel)
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
        NavigationContent(NavigationViewModel.UiState())
    }
}

@Composable
fun NavigationContent(
    state: NavigationViewModel.UiState
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        NavigationLandscapeScreen(
            state,
            modifier = Modifier
                .padding(innerPadding)
                // Consume this insets so that it's not applied again when using safeDrawing in the hierarchy below
                .consumeWindowInsets(innerPadding)
        )
    }
}

@Composable
fun NavigationScreen(viewModel: NavigationViewModel) {
    NavigationContent(viewModel.uiState)
}