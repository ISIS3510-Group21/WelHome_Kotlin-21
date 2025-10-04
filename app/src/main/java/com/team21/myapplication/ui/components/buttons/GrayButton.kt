package com.team21.myapplication.ui.components.buttons

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

@Composable
fun GrayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false          // ← NUEVO
) {
    val bg = if (selected) LavanderLight.copy(alpha = 0.7f) else LavanderLight
    val fg = if (selected) BlueCallToAction else BlackText

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg,
            disabledContainerColor = bg.copy(alpha = 0.6f),
            disabledContentColor = fg.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = text,
            style = LocalDSTypography.current.Description,
            color = fg
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