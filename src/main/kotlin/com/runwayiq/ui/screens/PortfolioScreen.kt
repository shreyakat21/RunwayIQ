package com.runwayiq.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.runwayiq.data.model.HoldingWithQuote
import com.runwayiq.ui.AppState
import com.runwayiq.ui.components.*
import com.runwayiq.ui.theme.*

@Composable
fun PortfolioScreen(
    state: AppState,
    onAdd: (ticker: String, shares: Double, costBasisDollars: Double, purchaseDate: String) -> Unit,
    onDelete: (Long) -> Unit,
    onRefreshPrices: () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    val hasStockApiKey = state.stockApiKey.isNotBlank()

    val rows = remember(state.holdings, state.holdingQuotes) {
        state.holdings.map { HoldingWithQuote(it, state.holdingQuotes[it.ticker]) }
    }
    val totalValueCents = rows.sumOf { it.currentValueCents }
    val totalCostCents = rows.sumOf { it.holding.costBasisCents }
    val totalGainLossCents = totalValueCents - totalCostCents
    val totalGainLossPct = if (totalCostCents == 0L) 0.0 else (totalGainLossCents.toDouble() / totalCostCents) * 100.0

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        SectionHeader("Portfolio") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRefreshPrices, enabled = hasStockApiKey && !state.isLoadingQuotes && rows.isNotEmpty()) {
                    Text(if (state.isLoadingQuotes) "Refreshing…" else "Refresh prices")
                }
                Button(onClick = { showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
                    Text("+ Add holding")
                }
            }
        }

        if (!hasStockApiKey) {
            Text(
                "Add a Finnhub API key in Settings to fetch live prices. You can still track holdings manually without one.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }

        if (rows.isEmpty()) {
            PortfolioEmptyState(onAdd = { showDialog = true })
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    title = "Total value",
                    value = formatDollars(totalValueCents),
                    deltaText = null,
                    modifier = Modifier.weight(1f),
                )
                SummaryCard(
                    title = "Cost basis",
                    value = formatDollars(totalCostCents),
                    deltaText = null,
                    modifier = Modifier.weight(1f),
                )
                SummaryCard(
                    title = "Gain / loss",
                    value = signedDollars(totalGainLossCents),
                    deltaText = "${if (totalGainLossPct >= 0) "+" else ""}${"%.1f".format(totalGainLossPct)}%",
                    deltaIncreased = totalGainLossPct >= 0,
                    deltaFavorable = totalGainLossPct >= 0,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(20.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(rows) { row ->
                    HoldingRow(row = row, onDelete = { onDelete(row.holding.id) })
                }
            }
        }
    }

    if (showDialog) {
        AddHoldingDialog(
            onDismiss = { showDialog = false },
            onConfirm = { ticker, shares, cost, date ->
                onAdd(ticker, shares, cost, date)
                showDialog = false
            },
        )
    }
}

@Composable
private fun PortfolioEmptyState(onAdd: () -> Unit) {
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
                Icon(Icons.Default.ShowChart, contentDescription = null, tint = Purple, modifier = Modifier.size(36.dp))
            }
            Text(
                "No holdings yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                "Track stocks or ETFs the company holds as treasury investments alongside its cash.",
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
                Text("+ Add your first holding", fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun HoldingRow(row: HoldingWithQuote, onDelete: () -> Unit) {
    val quote = row.quote
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
            Column {
                Text(row.holding.ticker, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                Text(
                    "${row.holding.shares} sh · cost ${formatDollars(row.holding.costBasisCents)} · ${row.holding.purchaseDate}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (quote != null) formatDollars(row.currentValueCents) else "—",
                        fontFamily = NumericFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                    )
                    if (quote != null) {
                        Text(
                            "${signedDollars(row.gainLossCents)} (${if (row.gainLossPct >= 0) "+" else ""}${"%.1f".format(row.gainLossPct)}%)",
                            fontFamily = NumericFontFamily,
                            fontSize = 12.sp,
                            color = if (row.gainLossCents >= 0) Teal else Coral,
                        )
                    }
                }
                TextButton(onClick = onDelete) {
                    Text("Remove", color = Coral, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun AddHoldingDialog(
    onDismiss: () -> Unit,
    onConfirm: (ticker: String, shares: Double, costBasisDollars: Double, purchaseDate: String) -> Unit,
) {
    var ticker by remember { mutableStateOf("") }
    var shares by remember { mutableStateOf("") }
    var costBasis by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf(currentIsoDate()) }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add holding") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PortfolioField(
                    value = ticker,
                    onValueChange = { ticker = it.uppercase(); errors = errors - "ticker" },
                    label = "Ticker (e.g. AAPL)",
                    error = errors["ticker"],
                )
                PortfolioField(
                    value = shares,
                    onValueChange = { shares = it; errors = errors - "shares" },
                    label = "Shares",
                    error = errors["shares"],
                )
                PortfolioField(
                    value = costBasis,
                    onValueChange = { costBasis = it; errors = errors - "cost" },
                    label = "Total cost basis ($)",
                    error = errors["cost"],
                )
                PortfolioField(
                    value = purchaseDate,
                    onValueChange = { purchaseDate = it; errors = errors - "date" },
                    label = "Purchase date (YYYY-MM-DD)",
                    error = errors["date"],
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validationErrors = buildMap {
                        if (ticker.isBlank()) put("ticker", "Ticker is required")
                        val sharesValue = shares.toDoubleOrNull()
                        if (sharesValue == null || sharesValue <= 0) put("shares", "Must be a positive number")
                        val costValue = costBasis.toDoubleOrNull()
                        if (costValue == null || costValue <= 0) put("cost", "Must be a positive number")
                        if (!Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(purchaseDate)) put("date", "Use YYYY-MM-DD format")
                    }
                    if (validationErrors.isNotEmpty()) {
                        errors = validationErrors
                        return@Button
                    }
                    onConfirm(ticker.trim(), shares.toDouble(), costBasis.toDouble(), purchaseDate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun PortfolioField(value: String, onValueChange: (String) -> Unit, label: String, error: String?) {
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
