package org.giste.navigator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Preview(
    name = "Tab Active 3",
    showBackground = true,
    device = "spec:width=1920px,height=1200px,dpi=360, isRound=false, orientation=landscape"
)
@Composable
fun DistanceSettingPreview() {
    val showDialog = remember { mutableStateOf(true) }
    DistanceSettingDialog(
        showDialog = showDialog,
        title = "Partial",
        text = "99999",
        numberOfIntegerDigits = 3,
        numberOfDecimals = 2,
        onAccept = {},
    )
}

@Composable
fun DistanceSettingDialog(
    showDialog: MutableState<Boolean>,
    title: String,
    text: String,
    numberOfIntegerDigits: Int,
    numberOfDecimals: Int,
    onAccept: (String) -> Unit,
) {
    var distance by remember { mutableStateOf(text) }
    val distanceLength by remember {
        mutableIntStateOf(numberOfIntegerDigits + numberOfDecimals)
    }

    Dialog(
        onDismissRequest = { showDialog.value = false },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        )
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displayMedium,
            )

            TextField(
                value = distance,
                onValueChange = {
                    distance = if (it.startsWith("0")) {
                        ""
                    } else if (distance.length == distanceLength) {
                        if (it.length < distanceLength) {
                            it
                        } else {
                            distance
                        }
                    } else {
                        it
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                textStyle = MaterialTheme.typography.displayLarge.copy(
                    textAlign = TextAlign.End,
                ),
                visualTransformation = DecimalVisualTransformation(
                    numberOfIntegerDigits = 4,
                    numberOfDecimals = 2,
                    fixedCursorAtTheEnd = true,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                ),
                singleLine = true,
            )
            Row(
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                IconButton(
                    onClick = { showDialog.value = false },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .fillMaxWidth(),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(64.dp),
                    )
                }
                IconButton(
                    onClick = {
                        onAccept(distance)
                        showDialog.value = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .fillMaxWidth(),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Accept",
                        modifier = Modifier.size(64.dp),
                    )
                }
            }
        }
    }
}