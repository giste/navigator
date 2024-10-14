package org.giste.navigator.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.giste.navigator.ui.theme.NavigatorTheme

@Preview(
    name = "Tab Active 3",
    showBackground = true,
    device = "spec:width=1920px,height=1200px,dpi=360, isRound=false, orientation=landscape"
)
@Composable
fun NavigationLandscapePreview() {
    NavigatorTheme {
        NavigationLandscape()
    }
}

@Composable
fun NavigationLandscape(modifier: Modifier = Modifier) {
    val padding = 4.dp

    Row(modifier = modifier.fillMaxSize()) {
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
                DistanceTotal("1.234,56")
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    //.padding(padding)
                    .border(2.dp, Color.Green, RoundedCornerShape(10.dp))
            ) {
                DistancePartial("789,01")
            }
            Row(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxWidth()
                    //.padding(padding)
                    .border(2.dp, Color.Green, RoundedCornerShape(10.dp))
            ) {
                Map()
            }
        }
        Column(
            modifier = Modifier
                .weight(5f)
                .fillMaxHeight()
                .padding(padding)
                .border(2.dp, Color.Blue, RoundedCornerShape(10.dp))
        ) {
            Row(
                modifier = Modifier
                    .weight(9f)
                    .fillMaxWidth()
                    //.padding(padding)
                    .border(2.dp, Color.Green, RoundedCornerShape(10.dp))
            ) {
                Roadbook()
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    //.padding(padding)
                    .border(2.dp, Color.Green, RoundedCornerShape(10.dp))
            ) {
                CommandBar()
            }
        }
    }
}

@Composable
fun DistanceTotal(distance: String) {
    Text(
        text = distance,
        style = MaterialTheme.typography.displayMedium,
        textAlign = TextAlign.End,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight()
            .border(2.dp, Color.Red, RoundedCornerShape(10.dp))
    )
}

@Composable
fun DistancePartial(distance: String) {
    Text(
        text = distance,
        style = MaterialTheme.typography.displayLarge,
        textAlign = TextAlign.End,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight()
            .border(2.dp, Color.Red, RoundedCornerShape(10.dp))
    )
}

@Composable
fun Map() {
    Text(
        text = "Map",
        style = MaterialTheme.typography.displayLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight()
    )
}

@Composable
fun Roadbook() {
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

@Composable
fun CommandBar() {
    Row {
        CommandBarColumn (modifier = Modifier.weight(1f)) {
            CommandBarButton(
                icon = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease partial"
            )
        }
        CommandBarColumn (modifier = Modifier.weight(1f)) {
            CommandBarButton(
                icon = Icons.Default.Refresh,
                contentDescription = "Reset partial"
            )
        }
        CommandBarColumn (modifier = Modifier.weight(1f)) {
            CommandBarButton(
                icon = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase partial"
            )
        }
        CommandBarColumn (modifier = Modifier.weight(1f)) {
            CommandBarButton(
                icon = Icons.Default.Clear,
                contentDescription = "Reset All"
            )
        }
        CommandBarColumn (modifier = Modifier.weight(1f)) {
            CommandBarButton(
                icon = Icons.Default.Search,
                contentDescription = "Load roadbook"
            )
        }
    }
}

@Composable
fun CommandBarColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        content()
    }
}

@Composable
fun CommandBarButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = { },
        modifier = modifier
            .fillMaxSize()
            .border(2.dp, Color.Red, RoundedCornerShape(10.dp)),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(72.dp),
        )
    }
}
