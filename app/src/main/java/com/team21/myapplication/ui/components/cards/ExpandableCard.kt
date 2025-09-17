package com.team21.myapplication.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.team21.myapplication.ui.theme.*

@Composable
fun ExpandableCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(
                width = 1.dp,
                color = GrayIcon,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = WhiteBackground),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BlackText,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = LocalDSTypography.current.Description,
                    color = BlackText,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = BlackText,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 4.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpandableCard_Preview() {
    AppTheme {
        Column {
            ExpandableCard(
                title = "General Questions",
                icon = Icons.Default.AccountCircle
            ) {
                Text(
                    text = "Here goes the content for general questions.",
                    style = LocalDSTypography.current.IconText,
                    color = BlackText
                )
            }

            ExpandableCard(
                title = "Food",
                icon = Icons.Default.MailOutline
            ) {
                Text(
                    text = "Details about food services go here.",
                    style = LocalDSTypography.current.IconText,
                    color = BlackText
                )
            }
        }
    }
}
