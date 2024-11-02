package org.giste.navigator.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.giste.navigator.ui.theme.NavigatorTheme
import kotlin.math.sqrt

//@Preview(name = "NEXUS_7", device = Devices.NEXUS_7)
//@Preview(name = "NEXUS_7_2013", device = Devices.NEXUS_7_2013)
//@Preview(name = "NEXUS_5", device = Devices.NEXUS_5)
//@Preview(name = "NEXUS_6", device = Devices.NEXUS_6)
//@Preview(name = "NEXUS_9", device = Devices.NEXUS_9)
//@Preview(name = "NEXUS_10", device = Devices.NEXUS_10)
//@Preview(name = "NEXUS_5X", device = Devices.NEXUS_5X)
//@Preview(name = "NEXUS_6P", device = Devices.NEXUS_6P)
//@Preview(name = "PIXEL_C", device = Devices.PIXEL_C)
//@Preview(name = "PIXEL", device = Devices.PIXEL)
//@Preview(name = "PIXEL_XL", device = Devices.PIXEL_XL)
//@Preview(name = "PIXEL_2", device = Devices.PIXEL_2)
//@Preview(name = "PIXEL_2_XL", device = Devices.PIXEL_2_XL)
//@Preview(name = "PIXEL_3", device = Devices.PIXEL_3)
//@Preview(name = "PIXEL_3_XL", device = Devices.PIXEL_3_XL)
//@Preview(name = "PIXEL_3A", device = Devices.PIXEL_3A)
//@Preview(name = "PIXEL_3A_XL", device = Devices.PIXEL_3A_XL)
//@Preview(name = "PIXEL_4", device = Devices.PIXEL_4)
//@Preview(name = "PIXEL_4_XL", device = Devices.PIXEL_4_XL)
//@Preview(name = "AUTOMOTIVE_1024p", device = Devices.AUTOMOTIVE_1024p)
@Preview(showBackground = true, name = "PIXEL 7 PRO", device = Devices.PIXEL_7_PRO)
@Preview(
    showBackground = true,
    name = "Pixel",
    device = "spec:shape=Normal,width=3120,height=1440,unit=px,dpi=560"
)
@Preview(
    showBackground = true,
    name = "TAB Active 3",
    device = "spec:width=1920px,height=1200px,dpi=283"
)
@Composable
fun PolygonPreview() {
    NavigatorTheme {
        Polygon()
    }
}


@Composable
fun Polygon() {
    val dm = LocalContext.current.resources.displayMetrics
    val widthPixels = dm.widthPixels
    val heightPixels = dm.heightPixels
    val densityDpi = dm.densityDpi
    val density = dm.density
    val xdpi = dm.xdpi
    val ydpi = dm.ydpi

    val widthInch = widthPixels.toFloat() / xdpi
    val heightInch = heightPixels.toFloat() / ydpi
    val diagonalInch =
        sqrt(widthInch * widthInch + heightInch * heightInch).times(100).toInt().toFloat().div(100)

    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    val style = MaterialTheme.typography.headlineSmall


    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(1f),
    ) {
        Text(text = "widthPixels = $widthPixels", style = style)
        Text(text = "heightPixels = $heightPixels", style = style)
        Text(text = "densityDpi = $densityDpi", style = style)
        Text(text = "density = $density", style = style)
        Text(text = "xdpi = $xdpi", style = style)
        Text(text = "ydpi = $ydpi", style = style)
        Text(text = "dpWidth = ${widthPixels.toFloat() / density}", style = style)
        Text(text = "dpHeight = ${heightPixels.toFloat() / density}", style = style)
        Text(text = "diagonalInch = $diagonalInch", style = style)
        Text(text = "isPortrait = $isPortrait", style = style)
    }
}