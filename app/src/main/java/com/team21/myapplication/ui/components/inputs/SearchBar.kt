package com.team21.myapplication.ui.components.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.theme.*

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onSearch: ((String) -> Unit)? = null,
    enabled: Boolean = true,
    asButton: Boolean = false,          // NUEVO
    onClick: (() -> Unit)? = null       // NUEVO
) {
    val base = Modifier
        .fillMaxWidth()
        .height(48.dp)

    val clickableMod = if (asButton && onClick != null) {
        base.clickable(enabled = enabled) { onClick() }
    } else base

    TextField(
        value = query,
        onValueChange = onQueryChange,
        enabled = enabled,
        readOnly = asButton,                 // <- NO abre teclado si es botón
        modifier = modifier.then(clickableMod),
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = GrayIcon) },
        placeholder = { Text(text = placeholder, style = LocalDSTypography.current.Description, color = GrayIcon) },
        textStyle = LocalDSTypography.current.Description,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search, keyboardType = KeyboardType.Text),
        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke(query) }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = LavanderLight,
            unfocusedContainerColor = LavanderLight,
            disabledContainerColor = LavanderLight.copy(alpha = 0.6f),
            focusedTextColor = GrayIcon,
            unfocusedTextColor = GrayIcon,
            disabledTextColor = GrayIcon.copy(alpha = 0.6f),
            cursorColor = GrayIcon,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = GrayIcon,
            unfocusedLeadingIconColor = GrayIcon,
            disabledLeadingIconColor = GrayIcon.copy(alpha = 0.6f),
            focusedPlaceholderColor = GrayIcon,
            unfocusedPlaceholderColor = GrayIcon
        )
    )
}


@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SearchBar_Preview() {
    AppTheme {
        SearchBar(
            query = "",
            onQueryChange = {},
            placeholder = "Search"
        )
    }
}
