package com.team21.myapplication.ui.components.navbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.team21.myapplication.ui.components.text.BlackText
import com.team21.myapplication.ui.components.text.GrayText
import com.team21.myapplication.ui.theme.LavanderLight
import com.team21.myapplication.ui.theme.BlueCallToAction
import com.team21.myapplication.ui.components.text.BlueText

@Composable
fun StepProgressBar(
    stepLabels: List<String>,
    currentStep: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stepLabels.forEachIndexed { index, label ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Text label
                if (index <= currentStep - 1) { // actual step
                    BlueText(
                        text = label,
                        fontWeight = FontWeight.Bold,
                    )
                } else { //  further steps
                    GrayText(
                        text = label,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar segment
                val barColor = if (index < currentStep) BlueCallToAction else LavanderLight

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(8.dp)
                        .background(barColor, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StepProgressBarPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        // Preview 1: First step active
        BlackText(
            text = "Step 1 Active",
            fontWeight = FontWeight.Bold
        )
        StepProgressBar(
            stepLabels = listOf("Basic Info", "Verification", "Profile"),
            currentStep = 1
        )

        // Preview 2: Second step active
        BlackText(
            text = "Step 2 Active",
            fontWeight = FontWeight.Bold
        )
        StepProgressBar(
            stepLabels = listOf("Basic Info", "Verification", "Profile"),
            currentStep = 2
        )

        // Preview 3: Third step active
        BlackText(
            text = "Step 3 Active",
            fontWeight = FontWeight.Bold
        )
        StepProgressBar(
            stepLabels = listOf("Basic Info", "Verification", "Profile"),
            currentStep = 3
        )
    }
}