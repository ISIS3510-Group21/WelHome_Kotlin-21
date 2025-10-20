package com.team21.myapplication.ui.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.theme.LavanderLight
import com.team21.myapplication.ui.theme.GrayIcon
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.ui.components.icons.AppIcons
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.BlueText
import com.team21.myapplication.ui.components.text.GrayText
import com.team21.myapplication.ui.components.text.WhiteText
import com.team21.myapplication.ui.theme.BlackText
import com.team21.myapplication.ui.theme.WhiteBackground
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle
import com.team21.myapplication.ui.theme.Poppins
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun PlaceholderTextField(
    placeholderText: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 1.dp,
    height: Dp = 52.dp,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,// Optional Composable for the icon
    visualTransformation: VisualTransformation = VisualTransformation.None,
    maxChars: Int? = null,
    maxCharsMessage: String = "You have reached the maximum number of characters."

) {

    val shape = RoundedCornerShape(8.dp)
    val coercedHeight = if (height < 56.dp) 56.dp else height
    val tfSize = 16.sp
    val endPaddingForIcon = if (trailingIcon != null) 36.dp else 0.dp
    var showMaxMsg by remember { mutableStateOf(false) }

    LaunchedEffect(value, maxChars) {
        showMaxMsg = if (maxChars != null) value.length >= maxChars else false
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(coercedHeight)
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 4.dp)
        ) {
            TextField(
                value = value,
                onValueChange = { newValue ->
                    if (maxChars != null) {
                        when {
                            newValue.length <= maxChars -> {
                                onValueChange(newValue)  // update value
                                showMaxMsg = (newValue.length == maxChars) //shows warning of limit
                            }
                            else -> {
                                showMaxMsg = true //allow visible message
                            }
                        }
                    } else {
                        onValueChange(newValue)
                        showMaxMsg = false
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .defaultMinSize(minHeight = coercedHeight)
                    .padding(end = endPaddingForIcon),
                textStyle = TextStyle(
                    fontFamily = Poppins,
                    fontSize = tfSize,
                    fontWeight = FontWeight.Normal,
                    color = textColor
                ),
                visualTransformation = visualTransformation,
                placeholder = {
                    when (textColor) {
                        BlackText -> BlackText(text = placeholderText, size = tfSize)
                        BlueCallToAction -> BlueText(text = placeholderText, size = tfSize)
                        GrayIcon -> GrayText(text = placeholderText, size = tfSize)
                        WhiteBackground -> WhiteText(text = placeholderText, size = tfSize)
                        else -> GrayText(text = placeholderText, size = tfSize)
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = backgroundColor,
                    unfocusedContainerColor = backgroundColor,
                    disabledContainerColor = backgroundColor,
                    errorContainerColor = backgroundColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    cursorColor = textColor
                ),
                shape = shape,
                singleLine = height <= 56.dp,
                minLines = when {
                    height >= 100.dp -> 4
                    height >= 76.dp -> 3
                    height > 56.dp -> 2
                    else -> 1
                },
                keyboardOptions = keyboardOptions,
            )
            if (trailingIcon != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 14.dp, end = 16.dp)
                )
                {
                    trailingIcon()
                }
            }
        }
        if (showMaxMsg) {
            Text(
                text = maxCharsMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

// A preview of the component
@Preview(showBackground = true)
@Composable
private fun PlaceholderTextFieldPreview() {
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("") }
    var text3 by remember { mutableStateOf("") }
    var text4 by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Default placeholder (no icon)
        PlaceholderTextField(
            placeholderText = "Ex: Cozy Home",
            value = text1,
            onValueChange = { text1 = it }
        )

        // 2. Placeholder with a custom icon
        PlaceholderTextField(
            placeholderText = "Enter a valid address",
            value = text2,
            onValueChange = { text2 = it },
            trailingIcon = {
                Icon(
                    imageVector = AppIcons.Notification,
                    contentDescription = "Info",
                    tint = GrayIcon
                )
            }
        )

        // 3. Placeholder with a different height and an icon on top-right
        PlaceholderTextField(
            placeholderText = "Long description",
            value = text3,
            onValueChange = { text3 = it },
            height = 100.dp,
            trailingIcon = {
                Icon(
                    imageVector = AppIcons.Notification,
                    contentDescription = "Info",
                    tint = BlueCallToAction
                )
            },
            borderColor = BlueCallToAction,
            maxChars = 40
        )

        // 4. Placeholder with a different height and an icon on top-right. Black text
        PlaceholderTextField(
            placeholderText = "Long description",
            value = text4,
            onValueChange = { text4 = it },
            height = 100.dp,
            textColor = BlackText,
            trailingIcon = {
                Icon(
                    imageVector = AppIcons.Notification,
                    contentDescription = "Info",
                    tint = BlueCallToAction
                )
            },
            borderColor = BlueCallToAction
        )
    }
}
