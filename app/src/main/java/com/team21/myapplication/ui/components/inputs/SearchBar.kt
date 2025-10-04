package com.team21.myapplication.ui.components.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.Alignment
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
    asButton: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    if (asButton) {
        // Botón con apariencia de barra de búsqueda
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClick = { onClick?.invoke() }
                ),
            shape = RoundedCornerShape(24.dp),
            color = LavanderLight,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = GrayIcon
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = placeholder,
                    style = LocalDSTypography.current.Description,
                    color = GrayIcon
                )
            }
        }
        return
    }

    // Modo TextField “clásico”
    TextField(
        value = query,
        onValueChange = onQueryChange,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = GrayIcon
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                style = LocalDSTypography.current.Description,
                color = GrayIcon
            )
        },
        textStyle = LocalDSTypography.current.Description,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch?.invoke(query) }
        ),
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
private fun SearchBar_Preview_Button() {
    AppTheme {
        SearchBar(
            query = "",
            onQueryChange = {},
            placeholder = "Search homes...",
            asButton = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SearchBar_Preview_TextField() {
    AppTheme {
        SearchBar(
            query = "",
            onQueryChange = {},
            placeholder = "Search",
            onSearch = {}
        )
    }
}
