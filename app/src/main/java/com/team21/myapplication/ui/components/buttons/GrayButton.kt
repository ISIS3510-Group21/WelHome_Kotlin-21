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
import com.team21.myapplication.ui.theme.LavanderLight
import com.team21.myapplication.ui.theme.BlackText
import com.team21.myapplication.ui.theme.LocalDSTypography

@Composable
fun GrayButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LavanderLight,
            contentColor = BlackText
        )
    ) {
        Text(
            text = text,
            style = LocalDSTypography.current.Description,
            color = BlackText
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GrayButton_Preview() {
    AppTheme {
        GrayButton(
            text = "Private Backyard",
            onClick = {}
        )
    }
}
