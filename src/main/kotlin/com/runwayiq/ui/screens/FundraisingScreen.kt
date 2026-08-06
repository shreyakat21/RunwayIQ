package com.runwayiq.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.runwayiq.data.model.Investor
import com.runwayiq.data.model.InvestorStage
import com.runwayiq.ui.AppState
import com.runwayiq.ui.components.*
import com.runwayiq.ui.theme.*

@Composable
fun FundraisingScreen(
    state: AppState,
    onAdd: (name: String, firm: String, stage: InvestorStage, amountDollars: Double, notes: String, lastContactDate: String) -> Unit,
    onUpdateStage: (id: Long, stage: InvestorStage) -> Unit,
    onDelete: (Long) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    val investors = state.investors

    val committedCents = investors.filter { it.stage == InvestorStage.COMMITTED }.sumOf { it.amountCents }
    val pipelineCents = investors
        .filter { it.stage != InvestorStage.COMMITTED && it.stage != InvestorStage.PASSED }
        .sumOf { it.amountCents }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        SectionHeader("Fundraising") {
            Button(onClick = { showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
                Text("+ Add investor")
            }
        }

        if (investors.isEmpty()) {
            FundraisingEmptyState(onAdd = { showDialog = true })
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(title = "Committed", value = formatDollars(committedCents), deltaText = null, modifier = Modifier.weight(1f))
                SummaryCard(title = "In pipeline", value = formatDollars(pipelineCents), deltaText = null, modifier = Modifier.weight(1f))
                SummaryCard(title = "Investors", value = investors.size.toString(), deltaText = null, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                InvestorStage.entries.forEach { stage ->
                    val group = investors.filter { it.stage == stage }
                    if (group.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "${stage.displayName} (${group.size})",
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    group.forEach { investor ->
                                        InvestorRow(
                                            investor = investor,
                                            onUpdateStage = { newStage -> onUpdateStage(investor.id, newStage) },
                                            onDelete = { onDelete(investor.id) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddInvestorDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, firm, stage, amount, notes, date ->
                onAdd(name, firm, stage, amount, notes, date)
                showDialog = false
            },
        )
    }
}

@Composable
private fun FundraisingEmptyState(onAdd: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.widthIn(max = 360.dp).padding(32.dp),
        ) {
            Box(
                Modifier.size(80.dp).background(PurpleLight, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Handshake, contentDescription = null, tint = Purple, modifier = Modifier.size(36.dp))
            }
            Text(
                "No investors yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                "Track investor conversations from first contact through commitment.",
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
                Text("+ Add your first investor", fontSize = 15.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvestorRow(investor: Investor, onUpdateStage: (InvestorStage) -> Unit, onDelete: () -> Unit) {
    var stageMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Surface2,
        border = BorderStroke(0.5.dp, BorderDefault),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp, 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(investor.name, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                val subtitle = listOfNotNull(
                    investor.firm.ifBlank { null },
                    "last contact ${investor.lastContactDate}",
                ).joinToString(" · ")
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
                if (investor.notes.isNotBlank()) {
                    Text(investor.notes, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (investor.amountCents > 0) {
                    Text(
                        formatDollars(investor.amountCents),
                        fontFamily = NumericFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                    )
                }
                Box {
                    OutlinedButton(onClick = { stageMenuExpanded = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(investor.stage.displayName, fontSize = 12.sp)
                    }
                    DropdownMenu(expanded = stageMenuExpanded, onDismissRequest = { stageMenuExpanded = false }) {
                        InvestorStage.entries.forEach { stage ->
                            DropdownMenuItem(
                                text = { Text(stage.displayName) },
                                onClick = { onUpdateStage(stage); stageMenuExpanded = false },
                            )
                        }
                    }
                }
                TextButton(onClick = onDelete) {
                    Text("Remove", color = Coral, fontSize = 13.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddInvestorDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, firm: String, stage: InvestorStage, amountDollars: Double, notes: String, lastContactDate: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var firm by remember { mutableStateOf("") }
    var stage by remember { mutableStateOf(InvestorStage.CONTACTED) }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var lastContactDate by remember { mutableStateOf(currentIsoDate()) }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }
    var stageMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add investor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FundraisingField(value = name, onValueChange = { name = it; errors = errors - "name" }, label = "Name", error = errors["name"])
                FundraisingField(value = firm, onValueChange = { firm = it }, label = "Firm (optional)", error = null)

                ExposedDropdownMenuBox(expanded = stageMenuExpanded, onExpandedChange = { stageMenuExpanded = it }) {
                    OutlinedTextField(
                        value = stage.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Stage") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(stageMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = stageMenuExpanded, onDismissRequest = { stageMenuExpanded = false }) {
                        InvestorStage.entries.forEach { s ->
                            DropdownMenuItem(text = { Text(s.displayName) }, onClick = { stage = s; stageMenuExpanded = false })
                        }
                    }
                }

                FundraisingField(
                    value = amount,
                    onValueChange = { amount = it; errors = errors - "amount" },
                    label = "Check size / amount ($)",
                    error = errors["amount"],
                )
                FundraisingField(
                    value = lastContactDate,
                    onValueChange = { lastContactDate = it; errors = errors - "date" },
                    label = "Last contact (YYYY-MM-DD)",
                    error = errors["date"],
                )
                FundraisingField(value = notes, onValueChange = { notes = it }, label = "Notes (optional)", error = null)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validationErrors = buildMap {
                        if (name.isBlank()) put("name", "Name is required")
                        val amountValue = amount.toDoubleOrNull()
                        if (amount.isNotBlank() && (amountValue == null || amountValue < 0)) put("amount", "Must be a non-negative number")
                        if (!Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(lastContactDate)) put("date", "Use YYYY-MM-DD format")
                    }
                    if (validationErrors.isNotEmpty()) {
                        errors = validationErrors
                        return@Button
                    }
                    onConfirm(name.trim(), firm.trim(), stage, amount.toDoubleOrNull() ?: 0.0, notes.trim(), lastContactDate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun FundraisingField(value: String, onValueChange: (String) -> Unit, label: String, error: String?) {
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

private fun currentIsoDate(): String {
    val now = java.time.LocalDate.now()
    return "%04d-%02d-%02d".format(now.year, now.monthValue, now.dayOfMonth)
}
