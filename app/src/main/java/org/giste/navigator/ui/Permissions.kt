package org.giste.navigator.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import org.giste.navigator.MainActivity
import org.giste.navigator.R
import kotlin.system.exitProcess

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ManagePermissions(
    multiplePermission: MultiplePermissionsState,
) {
//    val multiplePermission = rememberMultiplePermissionsState(
//        permissions = listOf(
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//        )
//    )
    val context = LocalContext.current
    val showRationalDialog = remember { mutableStateOf(false) }

    if (showRationalDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showRationalDialog.value = false
            },
            title = {
                Text(
                    text = stringResource(R.string.permissions_title),
                    style = MaterialTheme.typography.displaySmall
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.permissions_request),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            confirmButton = {
                IconButton(
                    onClick = {
                        showRationalDialog.value = false
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent, null)
                    },
                    modifier = Modifier
                        .fillMaxWidth(.45f)
                        .border(2.dp, Color.Blue)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Accept",
                        modifier = Modifier.size(64.dp),
                    )
                }
            },
            dismissButton = {
                IconButton(
                    onClick = {
                        showRationalDialog.value = false
                        // Permissions not granted, close application
                        MainActivity().finish()
                        exitProcess(0)
                    },
                    modifier = Modifier
                        .fillMaxWidth(.45f)
                        .border(2.dp, Color.Blue)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(64.dp),
                    )
                }
            },
        )
    }

    if (!multiplePermission.allPermissionsGranted) {
        if (multiplePermission.shouldShowRationale) {
            // Show a rationale if needed (optional)
            showRationalDialog.value = true
        } else {
            LaunchedEffect(showRationalDialog.value) {
                // Request the permission
                multiplePermission.launchMultiplePermissionRequest()
            }
        }
    }

}