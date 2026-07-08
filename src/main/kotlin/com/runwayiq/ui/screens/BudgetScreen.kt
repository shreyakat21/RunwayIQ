package com.runwayiq.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.runwayiq.data.model.BudgetLine
import com.runwayiq.ui.AppState
import com.runwayiq.ui.components.*
import com.runwayiq.ui.theme.*

private val REVENUE_CATEGORIES = listOf("mrr", "one_time", "grant")
private val EXPENSE_CATEGORIES = listOf("salaries", "cloud", "marketing", "office", "software", "opex")

@Composable
fun BudgetScreen(
    state: AppState,
    onSetBudget: (category: String, entryType: String, monthlyTargetDollars: Double) -> Unit,
    onDeleteBudget: (Long) -> Unit,
) {
    var editingCategory by remember { mutableStateOf<Pair<String, String>?>(null) }
    val linesByCategory = remember(state.budgetLines) {
        state.budgetLines.associateBy { it.category to it.entryType }
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        SectionHeader("Budget vs. Actual")
        Text(
            "Set a monthly target per category and track how this month's actuals compare.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        if (state.revenues.isEmpty() && state.expenses.isEmpty()) {
            EntryEmptyState(
                title = "Budget vs. Actual",
                hint = "Add some revenue or expense entries first, then set monthly targets here.",
                icon = Icons.Default.PieChart,
                onAdd = {},
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                item {
                    BudgetCategoryGroup(
                        title = "Revenue targets",
                        entryType = "revenue",
                        categories = REVENUE_CATEGORIES,
                        linesByCategory = linesByCategory,
                        onEdit = { category -> editingCategory = category to "revenue" },
                        onDelete = onDeleteBudget,
                    )
                }
                item {
                    BudgetCategoryGroup(
                        title = "Expense targets",
                        entryType = "expense",
                        categories = EXPENSE_CATEGORIES,
                        linesByCategory = linesByCategory,
                        onEdit = { category -> editingCategory = category to "expense" },
                        onDelete = onDeleteBudget,
                    )
                }
            }
        }
    }

    editingCategory?.let { (category, entryType) ->
        val existingTargetCents = linesByCategory[category to entryType]?.targetCents
        SetBudgetDialog(
            category = category,
            existingTargetDollars = existingTargetCents?.let { it / 100.0 },
            onDismiss = { editingCategory = null },
            onConfirm = { dollars ->
                onSetBudget(category, entryType, dollars)
                editingCategory = null
            },
        )
    }
}

@Composable
private fun BudgetCategoryGroup(
    title: String,
    entryType: String,
    categories: List<String>,
    linesByCategory: Map<Pair<String, String>, BudgetLine>,
    onEdit: (String) -> Unit,
    onDelete: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        categories.forEach { category ->
            val line = linesByCategory[category to entryType]
            BudgetRow(category = category, line = line, onEdit = { onEdit(category) }, onDelete = onDelete)
        }
    }
}

@Composable
private fun BudgetRow(
    category: String,
    line: BudgetLine?,
    onEdit: () -> Unit,
    onDelete: (Long) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Surface2,
        border = BorderStroke(0.5.dp, BorderDefault),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp, 12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(category.replace('_', ' '), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (line != null) {
                        Text(
                            "${formatDollars(line.actualCents)} / ${formatDollars(line.targetCents)}",
                            fontFamily = NumericFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary,
                        )
                        Text(
                            varianceLabel(line),
                            fontFamily = NumericFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (line.isOverBudget) Coral else Teal,
                        )
                    } else {
                        Text("No target set", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    TextButton(onClick = onEdit) {
                        Text(if (line != null) "Edit" else "Set target", fontSize = 13.sp)
                    }
                }
            }
            if (line != null && line.targetCents > 0) {
                Spacer(Modifier.height(8.dp))
                BudgetProgressBar(line)
            }
        }
    }
}

@Composable
private fun BudgetProgressBar(line: BudgetLine) {
    val fraction = (line.actualCents.toFloat() / line.targetCents.toFloat()).coerceIn(0f, 1.5f) / 1.5f
    val barColor = if (line.isOverBudget) Coral else Teal
    Box(
        Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Surface1),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(barColor),
        )
    }
}

private fun varianceLabel(line: BudgetLine): String = signedDollars(line.varianceCents)

@Composable
private fun SetBudgetDialog(
    category: String,
    existingTargetDollars: Double?,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    var amount by remember { mutableStateOf(existingTargetDollars?.let { "%.2f".format(it) } ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly target · ${category.replace('_', ' ')}") },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it; error = null },
                    label = { Text("Monthly target ($)") },
                    singleLine = true,
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (error != null) {
                    Text(
                        error!!,
                        color = Coral,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val value = amount.toDoubleOrNull()
                    if (value == null || value <= 0) {
                        error = "Must be a positive number"
                        return@Button
                    }
                    onConfirm(value)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
