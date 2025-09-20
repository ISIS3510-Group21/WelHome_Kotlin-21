package com.team21.myapplication.ui.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText

import com.team21.myapplication.ui.theme.*

@Composable
fun CustomToggleButton(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checkedTrackColor: Color = BlueCallToAction,
    checkedThumbColor: Color = WhiteBackground,
    uncheckedTrackColor: Color = LavanderLight,
    uncheckedThumbColor: Color = WhiteBackground,
    disabledCheckedTrackColor: Color = BlueCallToAction,
    disabledCheckedThumbColor: Color = WhiteBackground,
    disabledUncheckedTrackColor: Color = LavanderLight,
    disabledUncheckedThumbColor: Color = WhiteBackground,
    textComposable: @Composable (String) -> Unit = { txt -> BlackText(text = txt, size = 16.sp) },
    spaceBetween: Dp = 12.dp,
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp), // Padding for better touchability
        verticalAlignment = Alignment.CenterVertically
    ) {

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = checkedTrackColor,
                checkedThumbColor = checkedThumbColor,
                uncheckedTrackColor = uncheckedTrackColor,
                uncheckedThumbColor = uncheckedThumbColor,
                disabledCheckedTrackColor = disabledCheckedTrackColor,
                disabledCheckedThumbColor = disabledCheckedThumbColor,
                disabledUncheckedTrackColor = disabledUncheckedTrackColor,
                disabledUncheckedThumbColor = disabledUncheckedThumbColor
            )
        )

        Spacer(modifier = Modifier.width(spaceBetween))
        textComposable(text)
    }
}

//Preview for various toggle states and color variations
@Preview(showBackground = true, widthDp = 320)
@Composable
fun CustomToggleButtonVariationsPreview() {
    // Dummy states for preview
    var checkedState1 by remember { mutableStateOf(true) }
    var checkedState2 by remember { mutableStateOf(false) }
    var checkedState3 by remember { mutableStateOf(true) }
    var checkedState4 by remember { mutableStateOf(false) }
    var checkedState5 by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomToggleButton(
                text = "Default On",
                checked = checkedState1,
                onCheckedChange = { checkedState1 = it }
            )

            CustomToggleButton(
                text = "Default Off",
                checked = checkedState2,
                onCheckedChange = { checkedState2 = it }
            )

            CustomToggleButton(
                text = "Custom Colors On",
                checked = checkedState3,
                onCheckedChange = { checkedState3 = it },
                checkedTrackColor = LavanderLight,
                checkedThumbColor = BlueCallToAction,
                uncheckedTrackColor = GrayIcon,
                uncheckedThumbColor = WhiteBackground
            )

            CustomToggleButton(
                text = "Custom Text Off",
                checked = checkedState4,
                onCheckedChange = { checkedState4 = it },
                textComposable = { txt ->
                    BlueText(
                        text = txt,
                        size = 14.sp
                    )
                }
            )

            CustomToggleButton(
                text = "Disabled On",
                checked = checkedState5,
                onCheckedChange = { checkedState5 = it },
                enabled = false
            )

            CustomToggleButton(
                text = "Disabled Off",
                checked = false, // Directly set for preview clarity
                onCheckedChange = { /* No-op */ },
                enabled = false
            )
        }
}
