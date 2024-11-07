package org.giste.navigator.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.giste.navigator.ui.theme.NavigatorTheme

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
fun NavigationScreen(viewModel: NavigationViewModel = viewModel()) {
    NavigationContent(
        state = viewModel.navigationState.collectAsStateWithLifecycle().value,
        onEvent = viewModel::onEvent,
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
            state = state,
            onEvent = onEvent,
            modifier = Modifier
                .padding(innerPadding)
                // Consume this insets so that it's not applied again when using safeDrawing in the hierarchy below
                .consumeWindowInsets(innerPadding)
        )
    }
}
