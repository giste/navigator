package org.giste.navigator

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.AndroidEntryPoint
import org.giste.navigator.ui.ManagePermissions
import org.giste.navigator.ui.NavigationScreen
import org.giste.navigator.ui.theme.NavigatorTheme
import org.mapsforge.map.android.graphics.AndroidGraphicFactory

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Mapsforge
        AndroidGraphicFactory.createInstance(application)

        enableEdgeToEdge()
        setContent {
            val activity = LocalContext.current as Activity
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            NavigatorTheme {
                ManagePermissions()
                NavigationScreen()
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
