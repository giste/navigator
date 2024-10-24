package org.giste.navigator.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview


@Preview(
    name = "Tab Active 3",
    showBackground = true,
    device = "spec:width=1920px,height=1200px,dpi=360, isRound=false, orientation=landscape"
)
@Composable
fun PdfPreview() {
    PdfContent(PdfViewModel.PdfDisplayState.Loading, {})
}

@Composable
fun PdfScreen(pdfViewModel: PdfViewModel) {
    PdfContent(
        pdfViewModel.displayState.collectAsState().value,
        pdfViewModel::onUriChange,
    )
}

@Composable
fun PdfContent(
    uiState: PdfViewModel.PdfDisplayState,
    onUriChange: (Uri) -> Unit,
) {
    val path = remember { mutableStateOf("") }

    val pickPictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        path.value = uri?.path ?: ""

        if (uri != null) {
            // Update the state with the Uri
            onUriChange(uri)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                // Consume this insets so that it's not applied again when using safeDrawing in the hierarchy below
                .consumeWindowInsets(innerPadding)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { pickPictureLauncher.launch("application/pdf") },
                ) {
                    Text("Load")
                }
                Text(path.value)
            }
            Column(modifier = Modifier.weight(4f)) {
                PdfViewer(
                    state = uiState,
                )
            }
        }

    }

}


