package com.runwayiq.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.runwayiq.ui.AppState
import com.runwayiq.ui.components.SectionHeader
import com.runwayiq.ui.components.formatDollars
import com.runwayiq.ui.components.formatRunway
import com.runwayiq.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun WhatIfScreen(state: AppState) {
    val summary = state.summary

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        SectionHeader("Runway What-If")

        if (summary == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Add revenue and expense entries to run what-if scenarios.", color = TextMuted)
            }
        } else {
            var revenueDeltaPct by remember { mutableStateOf(0f) }
            var expenseDeltaPct by remember { mutableStateOf(0f) }

            val revenueDeltaCents = (summary.mrrCents * (revenueDeltaPct / 100.0)).toLong()
            val expenseDeltaCents = (summary.burnRateCents * (expenseDeltaPct / 100.0)).toLong()
            val adjustedBurnCents = (summary.burnRateCents - revenueDeltaCents + expenseDeltaCents).coerceAtLeast(0L)
            val adjustedRunway = if (adjustedBurnCents > 0) {
                summary.cashBalanceCents.toDouble() / adjustedBurnCents.toDouble()
            } else {
                Double.MAX_VALUE
            }

            Text(
                "Drag the sliders to see how a change in revenue or spending would move your runway. This is a quick approximation based on your current burn rate and MRR, not a replacement for a full model.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                WhatIfCard(
                    title = "Current runway",
                    value = if (summary.runwayMonths == Double.MAX_VALUE) "∞" else formatRunway(summary.runwayMonths),
                    accentColor = TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                WhatIfCard(
                    title = "Adjusted runway",
                    value = if (adjustedRunway == Double.MAX_VALUE) "∞" else formatRunway(adjustedRunway),
                    accentColor = if (adjustedRunway >= summary.runwayMonths) Teal else Coral,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(28.dp))

            WhatIfSlider(
                label = "Revenue growth",
                value = revenueDeltaPct,
                onValueChange = { revenueDeltaPct = it },
                valueRange = -50f..100f,
                impactLabel = "${if (revenueDeltaCents >= 0) "+" else ""}${formatDollars(revenueDeltaCents)}/mo MRR",
            )

            Spacer(Modifier.height(20.dp))

            WhatIfSlider(
                label = "Expense change",
                value = expenseDeltaPct,
                onValueChange = { expenseDeltaPct = it },
                valueRange = -50f..100f,
                impactLabel = "${if (expenseDeltaCents >= 0) "+" else ""}${formatDollars(expenseDeltaCents)}/mo burn",
            )
        }
    }
}

@Composable
private fun WhatIfCard(title: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Surface2,
        border = BorderStroke(0.5.dp, BorderDefault),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = NumericFontFamily,
                color = accentColor,
            )
        }
    }
}

@Composable
private fun WhatIfSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    impactLabel: String,
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(
                "${if (value >= 0) "+" else ""}${value.roundToInt()}%  ·  $impactLabel",
                fontFamily = NumericFontFamily,
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(thumbColor = Purple, activeTrackColor = Purple),
        )
    }
}
