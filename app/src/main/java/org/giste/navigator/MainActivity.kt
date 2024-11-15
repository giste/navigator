package org.giste.navigator

import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import org.giste.navigator.ui.ManagePermissions
import org.giste.navigator.ui.NavigationScreen
import org.giste.navigator.ui.theme.NavigatorTheme
import org.mapsforge.map.android.graphics.AndroidGraphicFactory

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Mapsforge
        AndroidGraphicFactory.createInstance(application)

        enableEdgeToEdge()
        setContent {
            val activity = LocalContext.current as Activity
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            NavigatorTheme {
                val multiplePermission = rememberMultiplePermissionsState(
                    permissions = listOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    )
                )

                ManagePermissions(multiplePermission)
                if (multiplePermission.allPermissionsGranted) {
                    NavigationScreen()
                }
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
