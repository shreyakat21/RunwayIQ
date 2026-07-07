package com.runwayiq.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.runwayiq.data.model.*
import com.runwayiq.ui.AppState
import com.runwayiq.ui.components.*
import com.runwayiq.ui.theme.*

@Composable
fun RevenueScreen(
    state: AppState,
    onAdd: (month: String, amount: Double, label: String, category: String) -> Unit,
    onDelete: (Long) -> Unit,
    onImportCsv: (List<CsvImportRow>) -> Unit,
    onError: (String) -> Unit,
) {
    EntryScreen(
        title = "Revenue",
        entries = state.revenues.map { EntryRow(it.id, it.month, it.amountCents, it.label, it.category) },
        categories = listOf("mrr", "one_time", "grant"),
        addEntryTrigger = state.addEntryTrigger,
        emptyStateIcon = Icons.Default.TrendingUp,
        emptyStateHint = "Track MRR, one-time deals, and grants to calculate your growth metrics.",
        onAdd = onAdd,
        onDelete = onDelete,
        onImportCsv = onImportCsv,
        onError = onError,
    )
}

@Composable
fun ExpenseScreen(
    state: AppState,
    onAdd: (month: String, amount: Double, label: String, category: String) -> Unit,
    onDelete: (Long) -> Unit,
    onImportCsv: (List<CsvImportRow>) -> Unit,
    onError: (String) -> Unit,
) {
    EntryScreen(
        title = "Expenses",
        entries = state.expenses.map { EntryRow(it.id, it.month, it.amountCents, it.label, it.category) },
        categories = listOf("salaries", "cloud", "marketing", "office", "software", "opex"),
        addEntryTrigger = state.addEntryTrigger,
        emptyStateIcon = Icons.Default.Receipt,
        emptyStateHint = "Log salaries, cloud costs, and other spending to calculate burn rate and runway.",
        onAdd = onAdd,
        onDelete = onDelete,
        onImportCsv = onImportCsv,
        onError = onError,
    )
}

data class EntryRow(val id: Long, val month: String, val amountCents: Long, val label: String, val category: String)

@Composable
fun EntryEmptyState(
    title: String,
    hint: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAdd: () -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.widthIn(max = 360.dp).padding(32.dp),
        ) {
            Box(
                Modifier
                    .size(80.dp)
                    .background(PurpleLight, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Purple, modifier = Modifier.size(36.dp))
            }
            Text(
                "No ${title.lowercase()} yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                hint,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("+ Add your first entry", fontSize = 15.sp)
            }
            Text("or press ⌘/Ctrl+N", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
    }
}

@Composable
fun EntryScreen(
    title: String,
    entries: List<EntryRow>,
    categories: List<String>,
    addEntryTrigger: Int,
    emptyStateIcon: androidx.compose.ui.graphics.vector.ImageVector,
    emptyStateHint: String,
    onAdd: (String, Double, String, String) -> Unit,
    onDelete: (Long) -> Unit,
    onImportCsv: (List<CsvImportRow>) -> Unit,
    onError: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var csvPreview by remember { mutableStateOf<List<CsvImportRow>?>(null) }

    LaunchedEffect(addEntryTrigger) {
        if (addEntryTrigger > 0) showDialog = true
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        SectionHeader(title) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val file = com.runwayiq.ui.FilePicker.pickCsvFile() ?: return@OutlinedButton
                        val rows = try {
                            com.runwayiq.data.CsvParser.parse(file)
                        } catch (e: Exception) {
                            onError("Couldn't read ${file.name}: ${e.message ?: "unknown error"}")
                            return@OutlinedButton
                        }
                        if (rows.isEmpty()) {
                            onError("No valid rows found in ${file.name}. Expected columns: month, amount, label, category.")
                        } else {
                            csvPreview = rows
                        }
                    },
                ) { Text("Import CSV") }
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                ) { Text("+ Add entry") }
            }
        }

        if (entries.isEmpty()) {
            EntryEmptyState(
                title = title,
                hint = emptyStateHint,
                icon = emptyStateIcon,
                onAdd = { showDialog = true },
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(entries) { entry ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Surface2,
                        border = BorderStroke(0.5.dp, BorderDefault)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp, 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(entry.label, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                                Text("${entry.month} · ${entry.category}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(formatDollars(entry.amountCents), fontWeight = FontWeight.Medium, fontSize = 15.sp, color = TextPrimary)
                                TextButton(onClick = { onDelete(entry.id) }) {
                                    Text("Remove", color = Coral, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddEntryDialog(categories, onDismiss = { showDialog = false }, onConfirm = { m, a, l, c ->
            onAdd(m, a, l, c)
            showDialog = false
        })
    }

    csvPreview?.let { rows ->
        CsvPreviewDialog(
            rows = rows,
            onDismiss = { csvPreview = null },
            onConfirm = {
                onImportCsv(rows)
                csvPreview = null
            },
        )
    }
}

@Composable
fun CsvPreviewDialog(
    rows: List<CsvImportRow>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import preview (${rows.size} rows)") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(rows) { row ->
                    Text(
                        "${row.month} · ${formatDollars(row.amountCents)} · ${row.label} · ${row.category}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
                Text("Import ${rows.size} rows")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (month: String, amount: Double, label: String, category: String) -> Unit
) {
    var month by remember { mutableStateOf(currentYearMonth()) }
    var amount by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.first()) }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ValidatedTextField(
                    value = label,
                    onValueChange = {
                        label = it
                        errors = errors - "label"
                    },
                    label = "Label",
                    error = errors["label"],
                )
                ValidatedTextField(
                    value = month,
                    onValueChange = {
                        month = it
                        errors = errors - "month"
                    },
                    label = "Month (YYYY-MM)",
                    error = errors["month"],
                )
                ValidatedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        errors = errors - "amount"
                    },
                    label = "Amount ($)",
                    error = errors["amount"],
                )
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validationErrors = buildMap {
                        validateLabel(label)?.let { put("label", it) }
                        validateMonth(month)?.let { put("month", it) }
                        validateAmount(amount)?.let { put("amount", it) }
                    }
                    if (validationErrors.isNotEmpty()) {
                        errors = validationErrors
                        return@Button
                    }
                    onConfirm(month, amount.toDouble(), label, category)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddScenarioDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, cash: Double) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var cash by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New scenario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ValidatedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errors = errors - "name"
                    },
                    label = "Scenario name",
                    error = errors["name"],
                )
                ValidatedTextField(
                    value = cash,
                    onValueChange = {
                        cash = it
                        errors = errors - "cash"
                    },
                    label = "Starting cash ($)",
                    error = errors["cash"],
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validationErrors = buildMap {
                        validateLabel(name)?.let { put("name", it) }
                        validateStartingCash(cash)?.let { put("cash", it) }
                    }
                    if (validationErrors.isNotEmpty()) {
                        errors = validationErrors
                        return@Button
                    }
                    onConfirm(name, cash.toDouble())
                },
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Text(
                error,
                color = Coral,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
    }
}

private val MONTH_PATTERN = Regex("^\\d{4}-\\d{2}$")

private fun currentYearMonth(): String {
    val now = java.time.YearMonth.now()
    return "%04d-%02d".format(now.year, now.monthValue)
}

private fun validateLabel(label: String): String? =
    if (label.isBlank()) "Label is required" else null

private fun validateMonth(month: String): String? = when {
    month.isBlank() -> "Month is required"
    !MONTH_PATTERN.matches(month) -> "Month must be in YYYY-MM format (e.g. 2024-10)"
    else -> null
}

private fun validateAmount(amount: String): String? = when {
    amount.isBlank() -> "Amount is required"
    amount.toDoubleOrNull()?.let { it > 0 } != true -> "Amount must be a positive number"
    else -> null
}

private fun validateStartingCash(cash: String): String? = when {
    cash.isBlank() -> "Starting cash is required"
    cash.toDoubleOrNull()?.let { it > 0 } != true -> "Must be a positive number"
    else -> null
}

@Composable
fun ScenariosScreen(
    state: AppState,
    onAdd: (name: String, cash: Double) -> Unit,
    onSwitch: (Long) -> Unit,
    onCompareWithAi: () -> Unit,
    onDismissComparison: () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        SectionHeader("Scenarios") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.scenarios.size >= 2) {
                    OutlinedButton(
                        onClick = onCompareWithAi,
                        enabled = !state.isComparingScenarios && state.apiKey.isNotBlank(),
                    ) { Text(if (state.isComparingScenarios) "Comparing…" else "Compare with AI") }
                }
                Button(onClick = { showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
                    Text("+ New scenario")
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.scenarios) { scenario ->
                val isActive = scenario.id == state.activeScenario?.id
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isActive) PurpleLight else Surface2,
                    border = BorderStroke(if (isActive) 1.5.dp else 0.5.dp, if (isActive) Purple else BorderDefault),
                    onClick = { onSwitch(scenario.id) }
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(scenario.name, fontWeight = FontWeight.Medium, color = if (isActive) PurpleDark else TextPrimary)
                            Text("Starting cash: ${formatDollars(scenario.cashBalanceCents)}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (isActive) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Purple) {
                                Text("Active", Modifier.padding(8.dp, 4.dp), color = androidx.compose.ui.graphics.Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddScenarioDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, cash ->
                onAdd(name, cash)
                showDialog = false
            },
        )
    }

    if (state.showScenarioComparison) {
        ScenarioComparisonDialog(
            text = state.scenarioComparisonResult ?: "Analyzing scenarios…",
            isLoading = state.isComparingScenarios,
            onDismiss = onDismissComparison,
        )
    }
}

@Composable
fun ScenarioComparisonDialog(
    text: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI Scenario Comparison") },
        text = {
            Box(Modifier.heightIn(min = 120.dp, max = 400.dp).verticalScroll(rememberScrollState())) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isLoading) TextMuted else TextPrimary,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Close") }
        },
    )
}

@Composable
fun SettingsScreen(state: AppState, onSaveApiKey: (String) -> Unit) {
    var key by remember(state.apiKey) { mutableStateOf(state.apiKey) }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionHeader("Settings")

        Surface(shape = RoundedCornerShape(12.dp), color = Surface2, border = BorderStroke(0.5.dp, BorderDefault)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Groq API key", fontWeight = FontWeight.Medium, color = TextPrimary)
                Text("Required for the CFO chat panel. Your key is stored locally.", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("gsk_...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onSaveApiKey(key) },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple)
                ) { Text("Save key") }
            }
        }

        Surface(shape = RoundedCornerShape(12.dp), color = Surface2, border = BorderStroke(0.5.dp, BorderDefault)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Data storage", fontWeight = FontWeight.Medium, color = TextPrimary)
                Text("All financial data is stored locally in ~/.runwayiq/runway.db — nothing leaves your machine.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
