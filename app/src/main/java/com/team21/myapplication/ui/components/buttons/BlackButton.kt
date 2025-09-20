package com.team21.myapplication.ui.components.buttons

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.theme.AppTheme
import com.team21.myapplication.ui.theme.*
import com.team21.myapplication.ui.theme.WhiteBackground
import com.team21.myapplication.ui.theme.LocalDSTypography

@Composable
fun BlackButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BlackText,
            contentColor = WhiteBackground
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            style = LocalDSTypography.current.Description,
            color = WhiteBackground
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BlueButton_Preview() {
    AppTheme {
        BlackButton(
            text = "Map Search",
            onClick = {}
        )
    }
}
