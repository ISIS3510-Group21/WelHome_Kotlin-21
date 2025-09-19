package com.team21.myapplication.ui.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText
import com.team21.myapplication.ui.components.text.GrayText
import com.team21.myapplication.ui.components.text.WhiteText
import com.team21.myapplication.ui.theme.BlackText
import com.team21.myapplication.ui.theme.LavanderLight
import com.team21.myapplication.ui.theme.GrayIcon
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.ui.theme.WhiteBackground
import com.team21.myapplication.ui.components.icons.AppIcons

@Composable
fun CustomDropdownMenu(
    placeholderText: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LavanderLight,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 1.dp,
    textColor: Color = GrayIcon,
    height: Dp = 52.dp,
    menuBackgroundColor: Color = LavanderLight
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(placeholderText) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .clickable { expanded = true } // Makes the whole Box clickable
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.TopStart
    ) {
        // Use a Row to position the text and icon
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Text to display inside the box
            TextByColor(
                text = selectedText,
                textColor = textColor,
                modifier = Modifier.weight(1f)
            )


            // Dropdown arrow icon
            Icon(
                imageVector = AppIcons.ArrowDropDown,
                contentDescription = "Dropdown arrow",
                tint = textColor
            )
        }

        // Dropdown Menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(menuBackgroundColor)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                        )
                    },
                    onClick = {
                        selectedText = item
                        expanded = false
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}
// function to pick the right color of text
@Composable
private fun TextByColor(text: String, textColor: Color, modifier: Modifier = Modifier) {
    when (textColor) {
        BlueCallToAction -> BlueText(text = text, modifier = modifier)
        WhiteBackground -> WhiteText(text = text, modifier = modifier)
        GrayIcon -> GrayText(text = text, modifier = modifier)
        BlackText -> BlackText(text = text, modifier = modifier)
        else -> GrayText(text = text, modifier = modifier) // Default value
    }
}
// Preview to demonstrate the component
@Preview(showBackground = true)
@Composable
private fun CustomDropdownMenuPreview() {
    val cities = listOf("Bogotá", "Medellín", "Cali", "Barranquilla")
    var selectedCity by remember { mutableStateOf("Select city") }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        CustomDropdownMenu(
            placeholderText = selectedCity,
            items = cities,
            onItemSelected = { city -> selectedCity = city }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Example with custom colors
        var selectedItem by remember { mutableStateOf("Select color") }
        CustomDropdownMenu(
            placeholderText = selectedItem,
            items = listOf("Red", "Green", "Blue"),
            onItemSelected = { color -> selectedItem = color },
            backgroundColor = Color.Transparent,
            borderColor = BlueCallToAction,
            textColor = BlueCallToAction,
            menuBackgroundColor = Color.White
        )
    }
}