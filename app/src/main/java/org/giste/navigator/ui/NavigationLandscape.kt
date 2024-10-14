package org.giste.navigator.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.giste.navigator.ui.theme.NavigatorTheme

@Preview(
    name = "Tablet",
    showBackground = true,
    device = Devices.TABLET
)
@Preview(
    name = "Tab Active 3",
    showBackground = true,
    device = "spec:id=reference_tablet,shape=Normal,width=1920,height=1200,unit=px,dpi=283"
)
//@Preview(
//    showBackground = true,
//    name = "Pixel 7 PRO",
//    device = "spec:shape=Normal,width=3120,height=1440,unit=px,dpi=560"
//)
@Composable
fun NavigationLandscapePreview() {
    NavigatorTheme {
        NavigationLandscape()
    }
}

@Composable
fun NavigationLandscape() {
//    val activity = LocalContext.current as Activity
//    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    val padding = 4.dp

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(padding)
                .border(2.dp, Color.Blue, RoundedCornerShape(10.dp))
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    //.padding(padding)
                    .border(2.dp, Color.Green, RoundedCornerShape(10.dp))
            ) {
                Distance("1.234,56", MaterialTheme.typography.displayMedium)
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    //.padding(padding)
                    .border(2.dp, Color.Green, RoundedCornerShape(10.dp))
            ) {
                Distance("789,01", MaterialTheme.typography.displayLarge)
            }
            Row(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxWidth()
                    //.padding(padding)
                    .border(2.dp, Color.Green, RoundedCornerShape(10.dp))
            ) {
                Text(
                    text = "Map",
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight()
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(5f)
                .fillMaxHeight()
                .padding(padding)
                .border(2.dp, Color.Blue, RoundedCornerShape(10.dp))
        ) {
            Text(
                text = "Roadbook",
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight()
                    .border(2.dp, Color.Red, RoundedCornerShape(10.dp))
            )
        }
    }
}

@Composable
fun Distance(distance: String, style: TextStyle = MaterialTheme.typography.displayLarge) {
    Text(
        text = distance,
        style = style,
        textAlign = TextAlign.End,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight()
            .border(2.dp, Color.Red, RoundedCornerShape(10.dp))
    )
}
