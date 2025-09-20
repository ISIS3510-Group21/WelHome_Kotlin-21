package com.team21.myapplication.ui.components.buttons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.ui.theme.BlueSecondary
import com.team21.myapplication.ui.theme.GrayIcon
import com.team21.myapplication.ui.theme.LavanderLight
import com.team21.myapplication.ui.theme.WhiteBackground

@Composable
fun CustomRadioButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedColor: Color = BlueCallToAction,
    unselectedColor: Color = GrayIcon,
    disabledSelectedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    disabledUnselectedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    textComposable: @Composable (String) -> Unit = { txt -> BlackText(text = txt, size = 16.sp) },
    spaceBetween: Dp = 8.dp
) {
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                disabledSelectedColor = disabledSelectedColor,
                disabledUnselectedColor = disabledUnselectedColor
            )
        )
        Spacer(modifier = Modifier.width(spaceBetween))
        textComposable(text)
    }
}


// preview of the component
@Preview(showBackground = true, name = "Radio Button Selected")
@Composable
fun CustomRadioButtonSelectedPreview() {
    Column (
        modifier = Modifier.padding(16.dp)
    ){
        CustomRadioButton(
            text = "Option 1",
            selected = true,
            onClick = { }
        )
        CustomRadioButton(
            text = "Option 2",
            selected = false,
            onClick = { },
            selectedColor = BlueSecondary
        )
        CustomRadioButton(
            text = "Option 3",
            selected = true,
            onClick = { },
            enabled = false
        )
        CustomRadioButton(
            text = "Option 4 (disabled)",
            selected = false,
            onClick = { },
            enabled = false,
            textComposable = { txt ->
                BlueText(
                    text = txt,
                    size = 14.sp
                )
            }
        )
        CustomRadioButton(
            text = "Custom option",
            selected = true,
            onClick = { },
            selectedColor = LavanderLight,
            unselectedColor = WhiteBackground
        )

    }
}

