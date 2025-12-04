package com.team21.myapplication.ui.components.buttons

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.BlackText
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.ui.theme.LavanderLight
import com.team21.myapplication.ui.theme.LocalDSTypography
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun GrayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,          // ← NUEVO
    compact: Boolean = false
) {
    val bg = if (selected) MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.tertiaryContainer
    val fg = if (selected) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onBackground

    val buttonHeight = if (compact) 32.dp else 40.dp
    val shape = if (compact) RoundedCornerShape(16.dp) else RoundedCornerShape(20.dp)

    val contentPadding = if (compact) {
        PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    } else {
        PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    }

    val textStyle = if (compact) {
        LocalDSTypography.current.Description.copy(fontSize = 12.sp)
    } else {
        LocalDSTypography.current.Description
    }


    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = buttonHeight),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg,
            disabledContainerColor = bg.copy(alpha = 0.6f),
            disabledContentColor = fg.copy(alpha = 0.6f)
        ),
        contentPadding = contentPadding
    ) {
        Text(
            text = text,
            style = textStyle,
            color = fg,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GrayButton_Preview() {
    AppTheme {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            GrayButton(text = "Private Backyard", onClick = {})
            GrayButton(text = "Private Backyard", onClick = {}, selected = true)
        }
    }
}