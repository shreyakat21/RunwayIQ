package com.runwayiq.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.runwayiq.ui.theme.*

private enum class OnboardingStep { WELCOME, SCENARIO_NAME, STARTING_CASH, ADD_REVENUE }

@Composable
fun OnboardingScreen(
    onSetupScenario: (name: String, cash: Double) -> Unit,
    onFinish: () -> Unit,
    onSkip: () -> Unit,
) {
    var step by remember { mutableStateOf(OnboardingStep.WELCOME) }
    var scenarioName by remember { mutableStateOf("") }
    var startingCash by remember { mutableStateOf("") }

    Box(
        Modifier
            .fillMaxSize()
            .background(Surface0),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 480.dp).padding(32.dp),
            shape = RoundedCornerShape(16.dp),
            color = Surface2,
            border = BorderStroke(0.5.dp, BorderDefault),
            shadowElevation = 8.dp,
        ) {
            Column(
                Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                when (step) {
                    OnboardingStep.WELCOME -> {
                        Box(
                            Modifier
                                .size(72.dp)
                                .background(PurpleLight, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("R", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Purple)
                        }
                        Text(
                            "RunwayIQ",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            color = Purple,
                        )
                        Text(
                            "Your AI-powered startup CFO",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { step = OnboardingStep.SCENARIO_NAME },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Purple),
                        ) {
                            Text("Get started", fontSize = 15.sp)
                        }
                    }

                    OnboardingStep.SCENARIO_NAME -> {
                        StepIndicator(current = 1, total = 3)
                        StepIcon(Icons.Default.Edit)
                        Text("Name your scenario", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(
                            "Scenarios let you model different funding or growth paths with the same revenue and expense data.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        OutlinedTextField(
                            value = scenarioName,
                            onValueChange = { scenarioName = it },
                            label = { Text("Scenario name") },
                            placeholder = { Text("e.g. Base case, Series A plan") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { step = OnboardingStep.WELCOME },
                                modifier = Modifier.weight(1f),
                            ) { Text("Back") }
                            Button(
                                onClick = { step = OnboardingStep.STARTING_CASH },
                                enabled = scenarioName.isNotBlank(),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                            ) { Text("Continue") }
                        }
                    }

                    OnboardingStep.STARTING_CASH -> {
                        StepIndicator(current = 2, total = 3)
                        StepIcon(Icons.Default.AttachMoney)
                        Text("Enter starting cash", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(
                            "How much cash does \"${scenarioName}\" have in the bank today?",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        OutlinedTextField(
                            value = startingCash,
                            onValueChange = { startingCash = it },
                            label = { Text("Starting cash ($)") },
                            placeholder = { Text("e.g. 500000") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { step = OnboardingStep.SCENARIO_NAME },
                                modifier = Modifier.weight(1f),
                            ) { Text("Back") }
                            Button(
                                onClick = {
                                    val cash = startingCash.toDoubleOrNull() ?: return@Button
                                    onSetupScenario(scenarioName.trim(), cash)
                                    step = OnboardingStep.ADD_REVENUE
                                },
                                enabled = startingCash.toDoubleOrNull() != null,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                            ) { Text("Continue") }
                        }
                    }

                    OnboardingStep.ADD_REVENUE -> {
                        StepIndicator(current = 3, total = 3)
                        StepIcon(Icons.Default.TrendingUp)
                        Text("Add your first revenue", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(
                            "RunwayIQ needs at least one revenue entry to calculate MRR, burn, and runway. You can add expenses later.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = onFinish,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Purple),
                        ) {
                            Text("+ Add your first revenue entry", fontSize = 15.sp)
                        }
                        TextButton(onClick = onSkip) {
                            Text("Skip for now — go to dashboard", color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { i ->
            Box(
                Modifier
                    .size(if (i + 1 == current) 24.dp else 8.dp, 8.dp)
                    .background(
                        if (i + 1 <= current) Purple else BorderDefault,
                        RoundedCornerShape(4.dp),
                    ),
            )
        }
    }
}

@Composable
private fun StepIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        Modifier
            .size(56.dp)
            .background(PurpleLight, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Purple, modifier = Modifier.size(28.dp))
    }
}
