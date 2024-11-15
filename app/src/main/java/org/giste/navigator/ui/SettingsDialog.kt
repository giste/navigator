package org.giste.navigator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.giste.navigator.ui.theme.NavigatorTheme

@Preview(
    name = "Tab Active 3",
    showBackground = true,
    device = "spec:width=1920px,height=1200px,dpi=360, isRound=false, orientation=landscape"
)
@Composable
fun SettingsDialogPreview() {
    NavigatorTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            val showDialog = remember { mutableStateOf(true) }

            SettingsDialogScreen(
                showDialog = showDialog,
                locationMinTime = 1_000L,
                locationMinDistance = 10,
                distanceUseAltitude = true,
                onAccept = {},
            )
        }
    }
}

@Composable
fun SettingsDialogScreen(
    showDialog: MutableState<Boolean>,
    locationMinTime: Long,
    locationMinDistance: Int,
    distanceUseAltitude: Boolean,
    onAccept: () -> Unit,
    width: Dp = 600.dp,
    height: Dp = 400.dp,
) {
    val minTime = remember { mutableLongStateOf(locationMinTime) }
    val minDistance = remember { mutableIntStateOf(locationMinDistance) }
    val useAltitude = remember { mutableStateOf(distanceUseAltitude) }

    Dialog(
        onDismissRequest = { showDialog.value = false },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Column(
            modifier = Modifier
                .size(width, height)
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Text(
                text = "Settings",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary)
                    .padding(4.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.displayMedium,
            )
            HorizontalDivider()
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(4f)
                    .padding(4.dp),
            ) {
                MinTimeSetting(minTime)
                MinDistanceSetting(minDistance)
                UseAltitudeSetting(useAltitude)
            }
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(role = Role.Button) { showDialog.value = false },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(64.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(role = Role.Button) { onAccept() }
                        .background(color = MaterialTheme.colorScheme.primary),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Accept",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

}

@Composable
fun MinTimeSetting(
    minTime: MutableLongState,
    modifier: Modifier = Modifier,
) {
    SettingRow(
        label = "Minimum time (ms):",
        modifier = modifier
    ) {
        TextField(
            value = minTime.longValue.toString(),
            onValueChange = { minTime.longValue = it.toLong() },
            textStyle = MaterialTheme.typography.displaySmall.copy(
                textAlign = TextAlign.End,
            ),
            singleLine = true,
        )
    }
}

@Composable
fun MinDistanceSetting(
    minDistance: MutableIntState,
    modifier: Modifier = Modifier,
) {
    SettingRow(
        label = "Minimum distance (m):",
        modifier = modifier
    ) {
        TextField(
            value = minDistance.intValue.toString(),
            onValueChange = { minDistance.intValue = it.toInt() },
            textStyle = MaterialTheme.typography.displaySmall.copy(
                textAlign = TextAlign.End,
            ),
            singleLine = true,
        )
    }
}

@Composable
fun UseAltitudeSetting(
    useAltitude: MutableState<Boolean>,
    modifier: Modifier = Modifier,
) {
    SettingRow(
        label = "Use altitude:",
        modifier = modifier
    ) {
        Switch(
            checked = useAltitude.value,
            onCheckedChange = { useAltitude.value = it },
        )
    }
}

@Composable
fun SettingRow(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(88.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(7f),
            style = MaterialTheme.typography.displaySmall,
        )
        Box(
            modifier = Modifier.weight(3f),
            contentAlignment = Alignment.CenterEnd,
        ) {
            content()
        }
    }
}
